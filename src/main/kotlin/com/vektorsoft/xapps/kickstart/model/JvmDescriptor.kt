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

package com.vektorsoft.xapps.kickstart.model

import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.NodeList
import javax.xml.xpath.XPath
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory

class JvmDescriptor(val configDoc : Document) {

    private val xpath = XPathFactory.newInstance().newXPath()
    val dependencies = mutableListOf<JvmDependency>()
    lateinit var launcher : BinaryData
    lateinit var splashScreen : BinaryData
    var totalSize = 0L

    fun processConfiguration() {
        processDependencies(configDoc, xpath)
        processLauncher(configDoc, xpath)
        processSplashScreen(configDoc, xpath)
    }

    private fun processDependencies(doc : Document, xpath: XPath) {
        val depNodes = xpath.evaluate("//application/jvm/dependencies/*", doc, XPathConstants.NODESET) as NodeList
        for (i in 0 until depNodes.length) {
            val dependencyElement = depNodes.item(i) as Element
            val dependency = JvmDependency(dependencyElement.getAttribute("file-name"),
                    dependencyElement.getAttribute("hash"),
                    dependencyElement.getAttribute("size").toLong(),
                    JvmDependencyScope.valueOf(dependencyElement.getAttribute("scope")))
            dependencies.add(dependency)
            totalSize += dependency.size
        }
    }

    private fun processLauncher(doc: Document, xpath: XPath) {
        val launcherNode = xpath.evaluate("//application/jvm/launcher", doc, XPathConstants.NODE)

        launcher = createBinaryData(launcherNode as Element)
        totalSize += launcher.size
    }

    private fun processSplashScreen(doc: Document, xpath: XPath) {
        val splashNode = xpath.evaluate("//application/jvm/splash-screen", doc, XPathConstants.NODE)

        splashScreen = createBinaryData(splashNode as Element)
        totalSize += splashScreen.size
    }

    private fun createBinaryData(element : Element) : BinaryData {
        return BinaryData(element.getAttribute("file-name"),
                element.getAttribute("hash"),
                element.getAttribute("size").toLong())
    }

}