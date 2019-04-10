/*
 * Copyright (c) 2019. Vladimir Djurovic
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
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
            result = DownloadResult(ex, bytesWritten)
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
        if(fileHash != data.hash) {
            result = DownloadResult(data.hash, fileHash ?: "", target.length())
        } else {
            result = DownloadResult(DownloadResult.Status.SUCCESS, target.length())
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