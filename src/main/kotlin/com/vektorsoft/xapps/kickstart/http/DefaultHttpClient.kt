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
import com.vektorsoft.xapps.kickstart.model.App
import com.vektorsoft.xapps.kickstart.model.AppModel
import com.vektorsoft.xapps.kickstart.model.BinaryData
import java.net.http.HttpClient
import java.net.http.HttpResponse
import java.time.Duration
import java.util.concurrent.CompletableFuture
import java.util.concurrent.Executors
import java.util.function.Consumer

object DefaultHttpClient {

    private val requestBuilder = RequestBuilder()
    private val objectMapper = ObjectMapper()
    private val executor = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors() * 2)

    val client = HttpClient.newBuilder()
            .followRedirects(HttpClient.Redirect.ALWAYS)
            .connectTimeout(Duration.ofSeconds(10))
            .executor(executor)
            .build()

    fun getAppList() {
        client.sendAsync(requestBuilder.buildAppListRequest(), HttpResponse.BodyHandlers.ofString())
                .thenAccept {
                    val appList : List<App> = objectMapper.readValue(it.body(), object : TypeReference<List<App>>(){})
                    AppModel.appList.clear();
                    AppModel.appList.addAll(appList)
                }
                .exceptionally { it.printStackTrace()
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
}