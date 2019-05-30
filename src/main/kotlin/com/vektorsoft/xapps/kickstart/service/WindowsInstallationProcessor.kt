/*
 * Copyright (c) 2019. Vladimir Djurovic
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.vektorsoft.xapps.kickstart.service

import com.vektorsoft.xapps.kickstart.model.App
import com.vektorsoft.xapps.kickstart.model.DeploymentDescriptor
import java.io.File

/**
 * Installation processor for Windows platform.
 */
class WindowsInstallationProcessor : InstallationProcessor {

	override fun performInstalation(descriptor: DeploymentDescriptor, appDir: File, application: App) {
		TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
	}
}