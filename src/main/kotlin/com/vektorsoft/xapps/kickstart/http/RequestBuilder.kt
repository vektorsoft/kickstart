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
import com.vektorsoft.xapps.kickstart.model.BinaryData
import java.net.URI
import java.net.http.HttpRequest

class RequestBuilder {

    fun buildAppListRequest() : HttpRequest {
        val uri = URI("http://localhost:8080/applications")
        return HttpRequest.newBuilder(uri)
                .header("Accept", "application/json")
                .GET()
                .build()
    }

    fun buildAppImageRequest(appId : String) : HttpRequest {
        val uri = URI("http://localhost:8080/applications/$appId/img")
        return HttpRequest.newBuilder(uri)
                .GET()
                .build();
    }

    fun buildAppConfigFileRequest(applicationId : String) : HttpRequest {
        var uri = URI("http://localhost:8080/apps/$applicationId/content/config/${detectOs().toString().toLowerCase()}/${detectCpuArch()}")
        return HttpRequest.newBuilder(uri)
                .header("Accept", "application/xml")
                .GET()
                .build()
    }

    fun buildBinaryDownloadUrl(data : BinaryData, serverUrl : String) : HttpRequest {
        val uri = URI("http://localhost:8080/apps/content/${data.hash}")
        return HttpRequest.newBuilder(uri)
                .GET()
                .build()
    }
}