/*
 * Copyright (c) 2019. Vladimir Djurovic
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.vektorsoft.xapps.kickstart.service

import com.vektorsoft.xapps.kickstart.logger
import com.vektorsoft.xapps.kickstart.model.App
import com.vektorsoft.xapps.kickstart.model.DeploymentDescriptor
import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Path

/**
 * Installer for Linux platform.
 */
class LinuxInstallationProcessor : InstallationProcessor {

    val logger by logger(LinuxInstallationProcessor::class.java)

    override fun performInstalation(descriptor: DeploymentDescriptor, appDir: File, application: App) {
        val shortcutDir = findMenuShortcutDirectory()
        logger.debug("Found menu shortcut directory {}", shortcutDir.absolutePath)
        val entryData = createDesktopEntryContent(descriptor, appDir, application.name)
        logger.debug("Desktop entry data: {}", entryData)
        File(shortcutDir, application.name.trim() + ".desktop").writeText(entryData, StandardCharsets.UTF_8)
        logger.debug("Wrote menu shortcut file")
        logger.info("Installation successfull")
    }

    override fun cleanup(descriptor: DeploymentDescriptor, appDir: File) {
        // remove downloaded config file
        val configFile = File(appDir, "config.xml")
        configFile.delete()
    }

    private fun findMenuShortcutDirectory() : File {
        return Path.of(System.getProperty("user.home"), ".local", "share", "applications").toFile()
    }

    private fun createDesktopEntryContent(descriptor : DeploymentDescriptor, installDir : File, appName : String) : String {
        val input = javaClass.getResourceAsStream("/install/linux_desktop_entry.desktop")
        val content : String = input.bufferedReader(StandardCharsets.UTF_8).use { it.readText() }
        // replace placeholders with actual data
        return content.replace("{app.name}", appName)
                .replace("{app.desc}", "")
                .replace("{app.launcher}", File(installDir, descriptor.jvmDescriptor.launcher.fileName).absolutePath)
                .replace("{app.icon}", File(installDir, descriptor.icons[0].fileName).absolutePath)
                .replace("{app.dir}", installDir.absolutePath)
                .replace("{app.categories}", "")
    }
}