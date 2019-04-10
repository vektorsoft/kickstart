package com.vektorsoft.xapps.kickstart.service

import com.vektorsoft.xapps.kickstart.model.App
import com.vektorsoft.xapps.kickstart.model.DeploymentDescriptor
import java.io.File

interface InstallationProcessor {

	fun performInstalation(descriptor : DeploymentDescriptor, appDir : File, application : App)
}