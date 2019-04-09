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
                    DownloadResult(it, 0, data.scope)
                }
    }
}