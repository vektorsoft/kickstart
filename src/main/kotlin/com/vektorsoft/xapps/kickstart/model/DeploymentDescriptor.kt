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
import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import javax.xml.xpath.XPath
import javax.xml.xpath.XPathConstants
import javax.xml.xpath.XPathFactory

/**
 * Creates application deployment descriptor from downloaded deployment configuration file.
 */
class DeploymentDescriptor(private val configFile : File) {

	val icons = mutableListOf<BinaryData>()
	lateinit var jvmDescriptor: JvmDescriptor
	lateinit var appVersion : String
	var totalDownloadSize = 0L

	fun processConfig() {
		val docBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder()
		val configDoc = docBuilder.parse(configFile)
		val xpath = XPathFactory.newInstance().newXPath()
		appVersion = xpath.evaluate("/application/@jvmVersion", configDoc, XPathConstants.STRING) as String

		processIcons(configDoc, xpath)
		jvmDescriptor = JvmDescriptor(configDoc)
		jvmDescriptor.processConfiguration()
		totalDownloadSize += jvmDescriptor.totalSize
	}

	private fun processIcons(doc : Document, xpath : XPath) {
		val iconNodes = xpath.evaluate("//application/info/icons/*", doc, XPathConstants.NODESET) as NodeList
		for (i in 0 until iconNodes.length) {
			val iconNode = iconNodes.item(i) as Element
			val data = createBinaryData(iconNode)
			icons.add(data)
			totalDownloadSize += data.size
		}
	}

	private fun createBinaryData(element : Element) : BinaryData {
		return BinaryData(element.getAttribute("file-name"),
				element.getAttribute("hash"),
				element.getAttribute("size").toLong())
	}
}