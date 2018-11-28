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

import com.vektorsoft.xapps.kickstart.calculateFileHash
import com.vektorsoft.xapps.kickstart.copyData
import com.vektorsoft.xapps.kickstart.logger
import com.vektorsoft.xapps.kickstart.model.BinaryData
import com.vektorsoft.xapps.kickstart.model.SymbolicLink
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.nio.channels.Channels
import java.nio.channels.ReadableByteChannel
import java.nio.channels.WritableByteChannel
import java.util.concurrent.SubmissionPublisher

abstract class DownloadHandler(protected val data: BinaryData) {

    private val logger by logger(DownloadHandler::class.java)

    val publisher = SubmissionPublisher<DownloadResult>()
    abstract val target : File

    protected abstract fun postprocess();

    fun handleDownloadResponse(input: InputStream) : DownloadResult {
        var result : DownloadResult
        var bytesWritten : Long = 0
        try {
            createDirectoriesIfNeeded()
            val src : ReadableByteChannel = Channels.newChannel(input)
            val dest : WritableByteChannel = Channels.newChannel(FileOutputStream(target))
            bytesWritten = copyData(src, dest)
            src.close()
            dest.close()
            logger.debug("Successfully downloaded ${data.fileName} to file ${target.absolutePath}")
            result = processResult()
        } catch(ex : Exception) {
            result = DownloadResult(ex, bytesWritten, data.scope)
            logger.error("Failed to download ${data.fileName} to file ${target.absolutePath}", ex)
        }
        publisher.submit(result)
        publisher.close()
        postprocess()
        return result
    }

    private fun processResult() : DownloadResult {
        val fileHash = calculateFileHash(target)
        var result : DownloadResult
        if(fileHash != data.sha1) {
            result = DownloadResult(data.sha1, fileHash ?: "", target.length(), data.scope)
        } else {
            result = DownloadResult(DownloadResult.Status.SUCCESS, target.length(), data.scope)
        }
        result.symLink = SymbolicLink(target.absolutePath, data.fileName)
        return result
    }

    private fun createDirectoriesIfNeeded() {
        if(!target.parentFile.exists()){
            target.parentFile.mkdirs()
        }
    }


}