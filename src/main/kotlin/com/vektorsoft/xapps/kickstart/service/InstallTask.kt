/*
 * Copyright (c) 2019. Vladimir Djurovic
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.vektorsoft.xapps.kickstart.service

import com.vektorsoft.xapps.kickstart.*
import com.vektorsoft.xapps.kickstart.http.*
import com.vektorsoft.xapps.kickstart.model.*
import javafx.concurrent.Task
import java.io.File
import java.io.IOException
import java.io.StringWriter
import java.lang.StringBuilder
import java.lang.module.ModuleFinder
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.Flow
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.transform.OutputKeys
import javax.xml.transform.TransformerFactory
import javax.xml.transform.dom.DOMSource
import javax.xml.transform.stream.StreamResult

class InstallTask(val app: App) : Task<Void>(), Flow.Subscriber<DownloadResult> {

    val logger by logger(InstallTask::class.java)
    val DEFAULT_XML_ENCODING = "UTF-8"
    val DEFAULT_XML_INDENT = "yes"


    private val installProcessor : InstallationProcessor
    private var subscription : Flow.Subscription? = null
    private var totalDownload : Long = 0
    private var currentDownload : Long = 0

    init {
    	val os = detectOs()
        installProcessor = when(os) {
            OS.MAC ->   MacInstallationProcessor()
            OS.LINUX ->  MacInstallationProcessor()
            OS.WINDOWS -> MacInstallationProcessor()
        }
    }


    override fun call(): Void? {
        try {
            updateMessage("Initializing installation...")
            val appDir = createDirectories()
            updateMessage("Created application directory ${appDir.absolutePath}")
            val deploymentConfigFile = getDeploymentConfigFile(appDir)
            updateMessage("Fetched application deployment config")
            val deploymentConfig = DeploymentDescriptor(deploymentConfigFile)
            deploymentConfig.processConfig()
            totalDownload = deploymentConfig.totalDownloadSize
            logger.info("Total download size for application {} is {}", app.name, totalDownload)
            updateMessage("Downloading application...")
            download(deploymentConfig)
            updateMessage("Performing installation...")
            createApplicationConfigFile(appDir, deploymentConfig)
            installProcessor.performInstalation(deploymentConfig, appDir, app)

        } catch (ex: Exception) {
            ex.printStackTrace()
        }

        return null
    }

    override fun onSubscribe(subscription: Flow.Subscription?) {
        this.subscription = subscription
        subscription?.request(1)
    }

    override fun onNext(item: DownloadResult?) {
        currentDownload += item?.length ?: 0
        updateProgress(currentDownload, totalDownload)
    }

    override fun onError(throwable: Throwable?) {
        println("on error")
    }

    override fun onComplete() {
        println("on complete")
    }

    private fun createDirectories(): File {
        val appDir = appDirLocation(app).toFile()
        if (!appDir.exists()) {
            logger.info("Creating app directory {}", appDir.absolutePath)
            val success = appDir.mkdirs()
            return if (success) appDir else throw IOException("Could not create application directory")
        } else {
            return appDir
        }
    }

    private fun getDeploymentConfigFile(targetDir: File): File {
        val data = DefaultHttpClient.getAppConfig(app.id)
        val filePath = Path.of(targetDir.absolutePath, "config.xml")
        Files.write(filePath, data.toByteArray(StandardCharsets.UTF_8))
        return filePath.toFile()
    }

    private fun download(deploymentConfig : DeploymentDescriptor) {
        val dataList = mutableListOf<Pair<BinaryData, DownloadHandler>>()
        for(icon in deploymentConfig.icons) {
            dataList.add(Pair(icon, SimpleDownloadHandler(app, icon)))
        }
        dataList.add(Pair(deploymentConfig.jvmDescriptor.splashScreen, SimpleDownloadHandler(app, deploymentConfig.jvmDescriptor.splashScreen)))
        dataList.add(Pair(deploymentConfig.jvmDescriptor.launcher, LauncherDownloadHandler(app, deploymentConfig.jvmDescriptor.launcher)))

        // get dependencies to download
        for(jar in deploymentConfig.jvmDescriptor.dependencies) {
            dataList.add(Pair(jar, DependencyDownloadHandler(app, jar)))
        }
        // check if JVM download is required
        if(!isJvmPresent(deploymentConfig.jvmDescriptor)) {
            logger.info("JVM not present, requesting download")
            val jvmBinaryData = DefaultHttpClient.getJvmBinaryInfo(deploymentConfig.jvmDescriptor.provider,
                    deploymentConfig.jvmDescriptor.jdkVersion,
                    deploymentConfig.jvmDescriptor.binaryType,
                    deploymentConfig.jvmDescriptor.implementation,
                    deploymentConfig.jvmDescriptor.exactVersion)
            logger.debug("Received JVM info from server")
            dataList.add(Pair(jvmBinaryData, JvmDownloadHandler(jvmBinaryData)))
            logger.debug("Submitted JVM download request")
            totalDownload += jvmBinaryData.size
        } else {
            logger.info("Required JVM already present, no download required")
        }

        val results = dataList.map {
            it.second.publisher.subscribe(this)
            if(it.second is JvmDownloadHandler) {
                DefaultHttpClient.downloadJvm(deploymentConfig.jvmDescriptor.provider,
                        deploymentConfig.jvmDescriptor.jdkVersion,
                        deploymentConfig.jvmDescriptor.binaryType,
                        deploymentConfig.jvmDescriptor.implementation,
                        deploymentConfig.jvmDescriptor.exactVersion,
                        it.second)
            } else {
                DefaultHttpClient.downloadBinaryData(it.first, it.second)
            }
        }

        results.forEach { it.join() } // wait for all futures to complete
    }


    private fun createApplicationConfigFile(appDir : File, deploymentDescriptor: DeploymentDescriptor) {
        logger.info("Creating application runtime config file")
        val document = DocumentBuilderFactory.newInstance().newDocumentBuilder().newDocument();
        val appElement = document.createElement("application")
        document.appendChild(appElement)
        appElement.setAttribute("version", deploymentDescriptor.appVersion)

        val jvmElement = document.createElement("jvm")
        jvmElement.setAttribute("provider", deploymentDescriptor.jvmDescriptor.provider)
        jvmElement.setAttribute("jdk-version", deploymentDescriptor.jvmDescriptor.jdkVersion)
        jvmElement.setAttribute("binary-type", deploymentDescriptor.jvmDescriptor.binaryType)
        jvmElement.setAttribute("implementation", deploymentDescriptor.jvmDescriptor.implementation)
        val exactVersion = deploymentDescriptor.jvmDescriptor.exactVersion ?: ""
        if(exactVersion.isNotEmpty() && exactVersion.isNotBlank()) {
            jvmElement.setAttribute("exact-version", exactVersion)
        }
        // create JVM base dir element
        val jvmBaseDirElement = document.createElement("jvm-base-dir")
        jvmBaseDirElement.textContent = jvmDirLocation().toString()
        jvmElement.appendChild(jvmBaseDirElement)

        val classpathElement = document.createElement("classpath")
        val classpath = StringBuilder()
        for(dep in deploymentDescriptor.jvmDescriptor.dependencies) {
          if(dep.dependencyScope == JvmDependencyScope.CLASSPATH) {
              classpath.append(CLASSPATH_DIR_NAME).append(File.separator).append(dep.fileName).append(File.pathSeparator)
          }
        }
        classpathElement.textContent = classpath.toString()
        jvmElement.appendChild(classpathElement)
		// module configuration
		val modulepathElement = document.createElement("module-path")
		modulepathElement.textContent = MODULE_DIR_NAME
		jvmElement.appendChild(modulepathElement)
		val addModulesElement = document.createElement("add-modules")
		val moduleNames = StringBuilder()
		moduleNames(appDir).forEach { moduleNames.append(it).append(",") }
		addModulesElement.textContent = moduleNames.toString()
		jvmElement.appendChild(addModulesElement)
        // create main class
        val mainClassElement = document.createElement("main-class")
        mainClassElement.textContent = deploymentDescriptor.jvmDescriptor.mainClass
        jvmElement.appendChild(mainClassElement)
        // add splash screen
        val splashElement = document.createElement("splash-screen")
        splashElement.textContent = deploymentDescriptor.jvmDescriptor.splashScreen.fileName
        jvmElement.appendChild(splashElement)

        appElement.appendChild(jvmElement)

        // write file content
        val writer = StringWriter()
        val factory = TransformerFactory.newInstance()
        val transformer = factory.newTransformer()
        transformer.setOutputProperty(OutputKeys.ENCODING, DEFAULT_XML_ENCODING)
        transformer.setOutputProperty(OutputKeys.INDENT, DEFAULT_XML_INDENT)

        transformer.transform(DOMSource(document), StreamResult(writer))
        File(appDir, APP_RUNTIME_CONFIG_FILE).writeText(writer.toString(), StandardCharsets.UTF_8)
		logger.info("Successfully wrote application rutime config file")
    }

	private fun moduleNames(appDir : File) : Set<String> {
		val modulesDir = File(appDir, MODULE_DIR_NAME)
		return ModuleFinder.of(modulesDir.toPath()).findAll()
                .filter { it.descriptor().name().isNotEmpty() }
                .map { it.descriptor().name() }
                .toSet()
	}

}