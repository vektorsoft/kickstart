/*
 * Copyright (c) 2019. Vladimir Djurovic
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.vektorsoft.xapps.http

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.junit.WireMockRule
import com.vektorsoft.xapps.kickstart.http.DefaultHttpClient
import com.vektorsoft.xapps.kickstart.http.DownloadHandler
import com.vektorsoft.xapps.kickstart.http.DownloadResult
import com.vektorsoft.xapps.kickstart.http.JvmDownloadHandler
import com.vektorsoft.xapps.kickstart.model.App
import com.vektorsoft.xapps.kickstart.model.AppModel
import com.vektorsoft.xapps.kickstart.model.BinaryData
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.mockito.Matchers
import org.mockito.Mockito
import java.io.InputStream

class DefaultHttpClientTest {

	private val objectMapper = ObjectMapper()
	private val  DEFAULT_WAIT_TIMEOUT : Long = 2000

	@Rule
	@JvmField
	var server = WireMockRule()

	@Test
	fun getAppListTest() {
		val appList = listOf(App("appId", "AppName", "headline", "Some description", "http://localhost:8080/image.png"))
		val appJson = objectMapper.writeValueAsString(appList)
		server.stubFor(get(urlMatching("/applications")).willReturn(aResponse().withBody(appJson).withStatus(200)))

		DefaultHttpClient.getAppList()
		Thread.sleep(DEFAULT_WAIT_TIMEOUT) // wait until async processing is finished
		val out = AppModel.appList.get()
		assertEquals(appList.size, out.size)
		assertEquals(appList[0].id, out[0].id)
		assertEquals(appList[0].name, out[0].name)
	}

	@Test
	fun getAppImagetest() {
		val responseBody = ByteArray(100)
		server.stubFor(get(urlMatching("/applications/appid/img")).willReturn(aResponse().withBody(responseBody).withStatus(200)))

		var processed = false
		DefaultHttpClient.getAppImage("appid", {arr : ByteArray -> processed = true}, {null})
		Thread.sleep(DEFAULT_WAIT_TIMEOUT)
		assertTrue(processed)
	}

	@Test
	fun geJvmInfotest() {
		val input = BinaryData("file.txt", "1234455667", 100);
		server.stubFor(get(urlMatching("/jvminfo?.*")).willReturn(aResponse().withBody(objectMapper.writeValueAsString(input)).withStatus(200)))

		val out = DefaultHttpClient.getJvmBinaryInfo("openjdk", "11", "jre", "hotspot")
		assertEquals(out.fileName, input.fileName)
		assertEquals(out.hash, input.hash)
		assertEquals(out.size, input.size)
	}

	@Test
	fun jvmDownloadTest() {
		val data = ByteArray(100)
		server.stubFor(get(urlMatching("/jvm?.*")).willReturn(aResponse().withBody(data).withStatus(200)))

		val handler = JvmDownloadHandler(BinaryData("file.zip", "123456", 100))
		DefaultHttpClient.downloadJvm("openjdk", "11", "jre", "hotspot", null, handler)
		Thread.sleep(DEFAULT_WAIT_TIMEOUT)
	}
}