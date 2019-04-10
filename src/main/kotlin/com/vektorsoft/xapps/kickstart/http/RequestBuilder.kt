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