package com.vektorsoft.xapps.model

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.vektorsoft.xapps.kickstart.model.JvmDependencyScope
import com.vektorsoft.xapps.kickstart.model.RuntimeConfig
import org.junit.Test
import java.io.InputStream
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class RuntimeConfigParserTest {

    private val mapper = XmlMapper()

    @Test
    fun parseConfigFile() {
        val input : InputStream = javaClass.getResourceAsStream("/application.xml")
        val content = input.bufferedReader().use { it.readText() }

        val config = mapper.readValue(content, RuntimeConfig::class.java)

        assertNotNull(config)
        val icons = config.info.icon
        assertNotNull(icons)
        assertEquals(1, icons.size)
        assertEquals(10162, icons[0].size)
        assertEquals(JvmDependencyScope.MODULE_PATH, config.jvm.jar[0].scope)
    }
}