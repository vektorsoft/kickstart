/*
 * Copyright (c) 2019. Vladimir Djurovic
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.vektorsoft.xapps.kickstart.http

import com.vektorsoft.xapps.kickstart.detectCpuArch
import com.vektorsoft.xapps.kickstart.detectOs
import com.vektorsoft.xapps.kickstart.logger
import com.vektorsoft.xapps.kickstart.model.AppModel
import com.vektorsoft.xapps.kickstart.model.BinaryData
import java.net.URI
import java.net.http.HttpRequest

class RequestBuilder {

	val logger by logger(RequestBuilder::class.java)

    fun buildAppListRequest() : HttpRequest {
        val uri = URI("${AppModel.currentSeverBaseUrl.value}/applications")
		logger.debug("Created app list request URL: $uri")
        return HttpRequest.newBuilder(uri)
                .header("Accept", "application/json")
                .GET()
                .build()
    }

    fun buildAppImageRequest(appId : String) : HttpRequest {
        val uri = URI("${AppModel.currentSeverBaseUrl.value}/applications/$appId/img")
		logger.debug("Created app image request URL: $uri")
        return HttpRequest.newBuilder(uri)
                .GET()
                .build();
    }

    fun buildAppConfigFileRequest(applicationId : String) : HttpRequest {
        var uri = URI("${AppModel.currentSeverBaseUrl.value}/apps/$applicationId/content/config/${detectOs().toString().toLowerCase()}/${detectCpuArch()}")
        return HttpRequest.newBuilder(uri)
                .header("Accept", "application/xml")
                .GET()
                .build()
    }

    fun buildBinaryDownloadUrl(data : BinaryData, serverUrl : String) : HttpRequest {
        val uri = URI("${AppModel.currentSeverBaseUrl.value}/apps/content/${data.hash}")
        return HttpRequest.newBuilder(uri)
                .GET()
                .build()
    }

	fun buildJvmDownloadUrl(provider : String,
							jdkVersion : String,
							binaryType : String,
							implementation : String,
							semanticVersion : String? = null) : HttpRequest {
		val sb = StringBuilder("${AppModel.currentSeverBaseUrl.value}/jvm")
		sb.append(jvmUrlQueryString(provider, jdkVersion, binaryType, implementation, semanticVersion))
		val uri = URI(sb.toString())
		logger.debug("Created JVM download request URL: $uri")
		return HttpRequest.newBuilder(uri)
				.GET()
				.build();
	}

	fun buildJvmInfoUrl(provider : String,
						jdkVersion : String,
						binaryType : String,
						implementation : String,
						semanticVersion : String? = null) : HttpRequest {
		val sb = StringBuilder("${AppModel.currentSeverBaseUrl.value}/jvminfo")
		sb.append(jvmUrlQueryString(provider, jdkVersion, binaryType, implementation, semanticVersion))
		val uri = URI(sb.toString())
		logger.debug("Created JVM info request URL: $uri")
		return HttpRequest.newBuilder(uri)
				.GET()
				.build();
	}

	private fun jvmUrlQueryString(provider : String,
								  jdkVersion : String,
								  binaryType : String,
								  implementation : String,
								  semanticVersion : String? = null) : String {
		val sb = StringBuilder("?provider=$provider" +
				"&jdkversion=$jdkVersion" +
				"&binaryType=$binaryType" +
				"&implementation=$implementation" +
				"&os=${detectOs().toString().toLowerCase()}" +
				"&cpu=${detectCpuArch().toLowerCase()}")
		if(semanticVersion != null &&
				(semanticVersion.isNotBlank() || semanticVersion.isNotEmpty())) {
			sb.append("&semver=$semanticVersion")
		}
		return sb.toString()
	}
}