/*
 * Copyright (c) 2019. Vladimir Djurovic
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.vektorsoft.xapps.kickstart.http

import com.fasterxml.jackson.core.type.TypeReference
import com.fasterxml.jackson.databind.ObjectMapper
import com.vektorsoft.xapps.kickstart.jvmDirLocation
import com.vektorsoft.xapps.kickstart.logger
import com.vektorsoft.xapps.kickstart.model.App
import com.vektorsoft.xapps.kickstart.model.AppModel
import com.vektorsoft.xapps.kickstart.model.BinaryData
import java.io.File
import java.net.http.HttpClient
import java.net.http.HttpResponse
import java.time.Duration
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors

object DefaultHttpClient {

	val logger by logger(DefaultHttpClient::class.java)
    private val requestBuilder = RequestBuilder()
    private val objectMapper = ObjectMapper()
    private val executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2)

    val client = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.ALWAYS)
            .connectTimeout(Duration.ofSeconds(10))
            .executor(executor)
            .build()

    fun getAppList() {
		logger.debug("Sending request for apps list...")
        client.sendAsync(requestBuilder.buildAppListRequest(), HttpResponse.BodyHandlers.ofString())
                .thenAccept {
					logger.debug("Received app list response: {}", it.body())
                    val appList : List<App> = objectMapper.readValue(it.body(), object : TypeReference<List<App>>(){})
                    AppModel.appList.clear();
                    AppModel.appList.addAll(appList)
					logger.debug("App list request completed successfully")
                }
                .exceptionally {
					logger.error("App list request failed", it)
                    null
                }

    }

    fun getAppImage(appId : String, bodyProcessor : (ByteArray) -> Unit, errorHandler : (Throwable) -> Void?) {
        client.sendAsync(requestBuilder.buildAppImageRequest(appId), HttpResponse.BodyHandlers.ofByteArray())
                .thenAccept {
                    bodyProcessor(it.body())
                }
                .exceptionally(errorHandler)
    }

    fun getAppConfig(applicationId : String) : String {
        return client.send(requestBuilder.buildAppConfigFileRequest(applicationId), HttpResponse.BodyHandlers.ofString())
                .body()
    }

    fun downloadBinaryData(data : BinaryData, handler : DownloadHandler) : CompletableFuture<DownloadResult> {
        return client.sendAsync(requestBuilder.buildBinaryDownloadUrl(data, "http://localhost:8080"), HttpResponse.BodyHandlers.ofInputStream())
                .thenApply {response ->
                    response.body().use {
                        handler.handleDownloadResponse(it)
                    }
                }
                .exceptionally {
                    it.printStackTrace()
                    DownloadResult(it, 0)
                }
    }

	fun getJvmBinaryInfo(provider : String,
						 jdkVersion : String,
						 distribution : String,
						 implementation : String,
						 semanticVersion : String? = null) : BinaryData {
		val response = client.send(requestBuilder.buildJvmInfoUrl(provider, jdkVersion, distribution, implementation, semanticVersion), HttpResponse.BodyHandlers.ofString())
		return objectMapper.readValue(response.body(),BinaryData::class.java)
	}

	fun downloadJvm(provider : String,
					jdkVersion : String,
					distribution : String,
					implementation : String,
					semanticVersion : String? = null,
					downloadHandler: DownloadHandler) : CompletableFuture<DownloadResult> {
		return client.sendAsync(requestBuilder.buildJvmDownloadUrl(provider, jdkVersion, distribution, implementation, semanticVersion), HttpResponse.BodyHandlers.ofInputStream())
				.thenApply {
					it.body().use {
						downloadHandler.handleDownloadResponse(it)
					}
				}
				.exceptionally {
					it.printStackTrace()
					DownloadResult(it, 0)
				}
	}
}