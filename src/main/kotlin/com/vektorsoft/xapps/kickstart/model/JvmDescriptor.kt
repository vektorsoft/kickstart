/*
 * Copyright (c) 2019. Vladimir Djurovic
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.vektorsoft.xapps.kickstart.model

import org.w3c.dom.Document
import org.w3c.dom.Element
import org.w3c.dom.NodeList
import javax.xml.xpath.XPath
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory

class JvmDescriptor(val configDoc: Document) {

	private val xpath = XPathFactory.newInstance().newXPath()
	val dependencies = mutableListOf<JvmDependency>()
	lateinit var launcher: BinaryData
	lateinit var splashScreen: BinaryData
	lateinit var mainClass: String
	var jvmOptions : String? = null
	var jvmSystemProperties : String? = null
	var programArguments : String? = null
	lateinit var provider : String
	lateinit var jdkVersion : String
	lateinit var binaryType : String
	lateinit var implementation : String
	var exactVersion : String? = null
	var totalSize = 0L

	fun processConfiguration() {
		processDependencies(configDoc, xpath)
		launcher = xmlToBinaryData(configDoc, xpath, "//application/jvm/launcher")
		splashScreen = xmlToBinaryData(configDoc, xpath, "//application/jvm/splash-screen")
		mainClass = xpath.evaluate("/application/jvm/main-class/text()", configDoc, XPathConstants.STRING) as String
		jvmOptions = xpath.evaluate("/application/jvm/jvm-options/text()", configDoc, XPathConstants.STRING) as String
		jvmSystemProperties = xpath.evaluate("/application/jvm/system-properties/text()", configDoc, XPathConstants.STRING) as String
		programArguments = xpath.evaluate("/application/jvm/arguments/text()", configDoc, XPathConstants.STRING) as String
		provider = (xpath.evaluate("/application/jvm/@provider", configDoc, XPathConstants.STRING) as String).toLowerCase()
		jdkVersion = xpath.evaluate("application/jvm/@version", configDoc, XPathConstants.STRING) as String
		jdkVersion = JdkVersion.valueOf(jdkVersion).display
		binaryType = (xpath.evaluate("/application/jvm/@binary-type", configDoc, XPathConstants.STRING) as String).toLowerCase()
		implementation = (xpath.evaluate("/application/jvm/@implementation", configDoc, XPathConstants.STRING) as String).toLowerCase()
		exactVersion = (xpath.evaluate("/application/jvm/@exact-version", configDoc, XPathConstants.STRING) as String)?.toLowerCase()
	}

	private fun processDependencies(doc: Document, xpath: XPath) {
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


	private fun xmlToBinaryData(doc : Document, xpath : XPath, expression : String) : BinaryData {
		val node = xpath.evaluate(expression, doc, XPathConstants.NODE)
		val binaryData = createBinaryData(node as Element)
		totalSize += binaryData.size
		return binaryData
	}


	private fun createBinaryData(element: Element): BinaryData {
		return BinaryData(element.getAttribute("file-name"),
				element.getAttribute("hash"),
				element.getAttribute("size").toLong())
	}

}