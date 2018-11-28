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
import com.vektorsoft.xapps.kickstart.http.*
import com.vektorsoft.xapps.kickstart.logger
import com.vektorsoft.xapps.kickstart.model.App
import com.vektorsoft.xapps.kickstart.model.BinaryData
import com.vektorsoft.xapps.kickstart.model.RuntimeConfig
import javafx.concurrent.Task
import java.io.File
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.util.concurrent.Flow

class InstallTask(val app: App) : Task<Void>(), Flow.Subscriber<DownloadResult> {

    companion object {
        val LOGGER by logger(Companion::class.java)
    }

    private val mapper = XmlMapper()
    private val installProcessor = LinuxInstallationProcessor()
    private var subscription : Flow.Subscription? = null
    private var totalDownload : Long = 0
    private var currentDownload : Long = 0


    override fun call(): Void? {
        try {
            updateMessage("Initializing installation...")
            val appDir = createDirectories()
            updateMessage("Created application directory ${appDir.absolutePath}")
            val config = getApplicationConfig(appDir)
            updateMessage("Fetched application config")
            val appConfig = mapper.readValue<RuntimeConfig>(config, RuntimeConfig::class.java)
            totalDownload = calculateTotalDownloadSize(appConfig)
            LOGGER.info("Total download size for application {} is {}", app.name, totalDownload)
            updateMessage("Downloading application...")
            download(appConfig)
            updateMessage("Performing installation...")
            installProcessor.performInstall(appConfig, appDir)

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
            LOGGER.info("Creating app directory {}", appDir.absolutePath)
            val success = appDir.mkdirs()
            return if (success) appDir else throw IOException("Could not create application directory")
        } else {
            return appDir
        }
    }

    private fun getApplicationConfig(targetDir: File): String {
        val data = DefaultHttpClient.getAppConfig(app.id)
        val filePath = Path.of(targetDir.absolutePath, "application.xml")
        Files.write(filePath, data.toByteArray(StandardCharsets.UTF_8))
        return data
    }

    private fun calculateTotalDownloadSize(config : RuntimeConfig) : Long {
        var total = 0L
        config.info.icon.forEach { total += it.size }
        total += config.jvm.launcher.size
        config.jvm.jar.forEach { total += it.size }

        return total
    }

    private fun download(appConfig : RuntimeConfig) {
        val dataList = mutableListOf<Pair<BinaryData, DownloadHandler>>(
                Pair(appConfig.info.icon[0], SimpleDownloadHandler(app, appConfig.info.icon[0])))
        dataList.add(Pair(appConfig.jvm.launcher, LauncherDownloadHandler(app, appConfig.jvm.launcher)))

        // get dependencies to download
        for(jar in appConfig.jvm.jar) {
            dataList.add(Pair(jar, DependencyDownloadHandler(app, jar)))
        }

        val results = dataList.map {
            it.second.publisher.subscribe(this)
            DefaultHttpClient.downloadBinaryData(it.first, it.second)
        }
        results.forEach { it.join() } // wait for all futures to complete
    }

}