/*
 * Copyright (c) 2018. Vladimir Djurovic
 *
 * This file is part of Kickstart.
 *
 * Kickstart is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Kickstart is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Kickstart.  If not, see <https://www.gnu.org/licenses/>
 */

package com.vektorsoft.xapps.kickstart.service

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.vektorsoft.xapps.kickstart.appDirLocation
import com.vektorsoft.xapps.kickstart.detectOs
import com.vektorsoft.xapps.kickstart.http.*
import com.vektorsoft.xapps.kickstart.logger
import com.vektorsoft.xapps.kickstart.model.App
import com.vektorsoft.xapps.kickstart.model.BinaryData
import com.vektorsoft.xapps.kickstart.model.DeploymentDescriptor
import com.vektorsoft.xapps.kickstart.model.OS
import javafx.concurrent.Task
import java.io.File
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.Flow

class InstallTask(val app: App) : Task<Void>(), Flow.Subscriber<DownloadResult> {

    val logger by logger(InstallTask::class.java)


    private val installProcessor : InstallationProcessor
    private var subscription : Flow.Subscription? = null
    private var totalDownload : Long = 0
    private var currentDownload : Long = 0

    init {
    	val os = detectOs()
        when(os) {
            OS.MAC -> installProcessor = MacInstallationProcessor()
            OS.LINUX -> installProcessor = MacInstallationProcessor()
            OS.WINDOWS -> installProcessor = MacInstallationProcessor()
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
//            installProcessor.performInstalation(deploymentConfig, appDir, app)

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
        dataList.add(Pair(deploymentConfig.jvmDescriptor.launcher, LauncherDownloadHandler(app, deploymentConfig.jvmDescriptor.launcher)))

        // get dependencies to download
        for(jar in deploymentConfig.jvmDescriptor.dependencies) {
            dataList.add(Pair(jar, DependencyDownloadHandler(app, jar)))
        }

        val results = dataList.map {
            it.second.publisher.subscribe(this)
            DefaultHttpClient.downloadBinaryData(it.first, it.second)
        }
        results.forEach { it.join() } // wait for all futures to complete
    }

}