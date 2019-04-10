package com.vektorsoft.xapps.model

import com.vektorsoft.xapps.kickstart.model.DeploymentDescriptor
import org.junit.Before
import org.junit.Test
import java.io.File
import java.nio.file.Path
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class DeploymentConfigTest {

	private val CONFIG_FILE_NAME = "config.xml"
	private lateinit var configFile : File

	@Before
	fun setup() {
		val currentDir = System.getProperty("user.dir")
		configFile = Path.of(currentDir, "src","test", "resources", CONFIG_FILE_NAME).toFile()
	}

	@Test
	fun testDeploymentConfig() {
		val deploymentConfig = DeploymentDescriptor(configFile)
		deploymentConfig.processConfig()

		// verify icons
		assertEquals(1, deploymentConfig.icons.size)
		assertEquals("maicon.icns", deploymentConfig.icons[0].fileName)
		// verify dependencies
		assertEquals(26, deploymentConfig.jvmDescriptor.dependencies.size)
		// verify launcher
		assertNotNull(deploymentConfig.jvmDescriptor.launcher)
		// verify splash screen
		assertNotNull(deploymentConfig.jvmDescriptor.splashScreen)
	}
}