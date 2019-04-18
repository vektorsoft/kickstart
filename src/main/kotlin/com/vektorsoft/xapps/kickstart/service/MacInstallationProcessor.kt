/*
 * Copyright (c) 2019. Vladimir Djurovic
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.vektorsoft.xapps.kickstart.service

import com.vektorsoft.xapps.kickstart.*
import com.vektorsoft.xapps.kickstart.model.App
import com.vektorsoft.xapps.kickstart.model.DeploymentDescriptor
import java.io.File
import java.nio.charset.StandardCharsets
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

class MacInstallationProcessor : InstallationProcessor {

	val logger by logger(MacInstallationProcessor::class.java)
	private lateinit  var bundleContentsDir : File
	private lateinit var macOsDir : File
	private lateinit var resourcesDir : File

	override fun performInstalation(descriptor: DeploymentDescriptor, appDir: File, application : App) {
		logger.info("Performing Mac OS X installation process")
		// create directory structure
		createBundleDirs(application)
		// copy resources
		moveIcons(descriptor, appDir)
		Files.move(Path.of(appDir.absolutePath, descriptor.jvmDescriptor.launcher.fileName), Path.of(macOsDir.absolutePath, descriptor.jvmDescriptor.launcher.fileName), StandardCopyOption.REPLACE_EXISTING)
		Files.move(Path.of(appDir.absolutePath, APP_RUNTIME_CONFIG_FILE), Path.of(macOsDir.absolutePath, APP_RUNTIME_CONFIG_FILE), StandardCopyOption.REPLACE_EXISTING)
		Files.move(Path.of(appDir.absolutePath, descriptor.jvmDescriptor.splashScreen.fileName), Path.of(macOsDir.absolutePath, descriptor.jvmDescriptor.splashScreen.fileName), StandardCopyOption.REPLACE_EXISTING)
		moveDependencies(appDir)
		createInfoPlist(descriptor, application)

	}

	private fun createBundleDirs(application: App) {
		logger.info("Creating application bundle directory structure")
		val userHomeDir = System.getProperty(HOME_DIR_PROPERTY)
		bundleContentsDir = Path.of(userHomeDir, "Applications", application.name + ".app", "Contents").toFile()
		bundleContentsDir.mkdirs()
		macOsDir = Path.of(bundleContentsDir.toString(), "MacOS").toFile()
		macOsDir.mkdirs()
		resourcesDir = Path.of(bundleContentsDir.toString(), "Resources").toFile()
		resourcesDir.mkdirs()
	}

	private fun moveIcons(descriptor: DeploymentDescriptor, appdir : File) {
		logger.debug("Moving icons to resources directory")
		for(icon in descriptor.icons) {
			Files.move(Path.of(appdir.absolutePath, icon.fileName), Path.of(resourcesDir.absolutePath, icon.fileName), StandardCopyOption.REPLACE_EXISTING)
		}
	}

	private fun moveDependencies(appDir: File) {
		logger.debug("Moving dependency directories")
		val dirs = listOf(
				File(appDir, CLASSPATH_DIR_NAME),
				File(appDir, MODULE_DIR_NAME))
		dirs.forEach {
			if(it.exists()) {
				Files.move(it.toPath(), Path.of(macOsDir.absolutePath, it.name), StandardCopyOption.REPLACE_EXISTING)
			}
		}
	}

	private fun createInfoPlist(descriptor: DeploymentDescriptor, application: App)  {
		logger.debug("Processing Info.plist file")
		val input = javaClass.getResourceAsStream("/install/Info.plist")
		val content : String = input.bufferedReader(StandardCharsets.UTF_8).use { it.readText() }
		val replaced = content.replace("{bundle.displayname}", application.name)
				.replace("{bundle.launcher}", descriptor.jvmDescriptor.launcher.fileName)
				.replace("{bundle.icon}", descriptor.icons[0].fileName)
				.replace("{bundle.id}", application.id)
				.replace("{bundle.name}", "some.bundle.name")
				.replace("{bundle.jvmVersion}", "1.0.0")
		File(bundleContentsDir, "Info.plist").writeText(replaced,StandardCharsets.UTF_8)
	}
}