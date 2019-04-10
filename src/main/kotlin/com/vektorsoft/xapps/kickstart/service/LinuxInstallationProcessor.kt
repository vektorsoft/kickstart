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

import com.vektorsoft.xapps.kickstart.logger
import com.vektorsoft.xapps.kickstart.model.DeploymentDescriptor
import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Path

class LinuxInstallationProcessor {

    val logger by logger(LinuxInstallationProcessor::class.java)

    fun performInstall(descriptor: DeploymentDescriptor, installDir : File) {
        val shortcutDir = findMenuShortcutDirectory()
        logger.debug("Found menu shortcut directory {}", shortcutDir.absolutePath)
        val entryData = createDesktopEntryContent(descriptor, installDir)
        logger.debug("Desktop entry data: {}", entryData)
//        File(shortcutDir, descriptor.info.name.trim() + ".desktop").writeText(entryData, StandardCharsets.UTF_8)
        logger.debug("Wrote menu shortcut file")
    }

    private fun findMenuShortcutDirectory() : File {
        return Path.of(System.getProperty("user.home"), ".local", "share", "applications").toFile()
    }

    private fun createDesktopEntryContent(descriptor : DeploymentDescriptor, installDir : File) : String {
        val input = javaClass.getResourceAsStream("/install/linux_desktop_entry.desktop")
        val content : String = input.bufferedReader(StandardCharsets.UTF_8).use { it.readText() }
        // replace placeholders with actual data
        return content.replace("{app.name}", /*descriptor.info.name*/ "some name")
                .replace("{app.desc}", "")
//                .replace("{app.launcher}", File(installDir, descriptor.jvm.launcher.fileName).absolutePath)
                .replace("{app.icon}", File(installDir, descriptor.icons[0].fileName).absolutePath)
                .replace("{app.dir}", installDir.absolutePath)
                .replace("{app.categories}", "")
    }
}