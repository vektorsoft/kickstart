/*
 * Copyright (c) 2019. Vladimir Djurovic
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.vektorsoft.xapps.kickstart.http

import com.vektorsoft.xapps.kickstart.EXEC_MODE_MASK
import com.vektorsoft.xapps.kickstart.jvmDirLocation
import com.vektorsoft.xapps.kickstart.logger
import com.vektorsoft.xapps.kickstart.model.BinaryData
import org.apache.commons.compress.archivers.tar.TarArchiveEntry
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream
import org.apache.commons.compress.utils.IOUtils
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.nio.file.Path

class JvmDownloadHandler : DownloadHandler {

	val logger by logger(JvmDownloadHandler::class.java)

	override val target: File

	constructor(binaryData: BinaryData) : super(binaryData) {
		target = Path.of(jvmDirLocation().toString(), binaryData.fileName).toFile()
	}

	override fun postprocess() {
		logger.info("JVM downloaded successfully, starting decompress")
		val fin = TarArchiveInputStream(GzipCompressorInputStream(FileInputStream(target)))
		val extractLocation = jvmDirLocation().toFile()
		fin.use {
			var entry : TarArchiveEntry
			while(it.nextTarEntry != null) {
				entry = it.currentEntry
				if(entry.isDirectory) continue
				val curFile = File(extractLocation, entry.name)
				val parent = curFile.parentFile
				if(!parent.exists()){
					parent.mkdirs()
				}
				IOUtils.copy(it, FileOutputStream(curFile))
				logger.debug("Extracted JVM entry ${entry.name}")
				// check if file is executable
				if((EXEC_MODE_MASK and entry.mode) > 0)      {
					logger.debug("Setting executable flag for entry ${curFile.absolutePath}")
					curFile.setExecutable(true)
				}
			}
		}
		logger.info("JVM extracted successfully")
		// delete JVM archive
		if(target.delete()) {
			logger.info("Deleted JVm archive")
		} else {
			logger.warn("Failed to delete JVM archive")
		}
	}
}