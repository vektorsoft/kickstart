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
import mslinks.ShellLink
import java.io.File
import java.nio.file.Path

/**
 * Installation processor for Windows platform.
 */
class WindowsInstallationProcessor : InstallationProcessor {

	val logger by logger(WindowsInstallationProcessor::class.java)

	override fun performInstalation(descriptor: DeploymentDescriptor, appDir: File, application: App) {
		logger.info("Performing installation")
		val desktopShortcutPath = Path.of(System.getProperty("user.home"), "Desktop", application.name + ".lnk")
		val starMenuShortcutPath = Path.of(System.getProperty("user.home"), "AppData", "Roaming", "Microsoft", "Windows", "Start Menu", "Programs", application.name + ".lnk")
		val targetPath = Path.of(appDir.absolutePath, descriptor.jvmDescriptor.launcher.fileName)
		ShellLink.createLink(targetPath.toString(), desktopShortcutPath.toString())
		ShellLink.createLink(targetPath.toString(), starMenuShortcutPath.toString())
		logger.info("Installation completed")
	}

	override fun cleanup(descriptor: DeploymentDescriptor, appDir: File) {
		// remove downloaded config file
		val configFile = File(appDir, "config.xml")
		configFile.delete()
	}
}