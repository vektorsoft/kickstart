/*
 * Copyright (c) 2019. Vladimir Djurovic
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.vektorsoft.xapps.kickstart

import com.vektorsoft.xapps.kickstart.model.App
import com.vektorsoft.xapps.kickstart.model.BinaryData
import com.vektorsoft.xapps.kickstart.model.JvmDescriptor
import com.vektorsoft.xapps.kickstart.model.OS
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.codec.digest.MessageDigestAlgorithms
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.lang.StringBuilder
import java.nio.ByteBuffer
import java.nio.channels.ReadableByteChannel
import java.nio.channels.WritableByteChannel
import java.nio.file.Path


fun defaultBaseDirLocation(): Path? = Path.of(System.getProperty(HOME_DIR_PROPERTY), DEFAULT_BASE_DIR_NAME)
fun appDirLocation(app: App): Path = Path.of(defaultBaseDirLocation().toString(), APP_DIR_NAME, app.name)
fun jvmDirLocation() = Path.of(defaultBaseDirLocation().toString(), JVM_DIR_NAME)

fun jarDirLocation(binary: BinaryData): Path {
	val parts = arrayOf(
			binary.hash.substring(0, 2),
			binary.hash.substring(2, 4),
			binary.hash.substring(4, 6)
	)
	return Path.of(defaultBaseDirLocation().toString(),
			DEPENDENCY_DIR_NAME,
			JAR_DIR_NAME,
			parts[0], parts[1], parts[2], binary.hash)
}


fun isJvmPresent(jvmDescriptor: JvmDescriptor) : Boolean {
	val sb = StringBuilder(jvmDescriptor.provider).append("-")
			.append(jvmDescriptor.jdkVersion).append("-")
			.append(jvmDescriptor.binaryType).append("-")
			.append(jvmDescriptor.implementation)
	if(jvmDescriptor.exactVersion != null) {
		sb.append("-").append(jvmDescriptor.exactVersion)
	}
	val fileName = sb.toString()
	val target = jvmDirLocation().toFile().listFiles().find { it.name.startsWith(fileName) }
	return target != null
}

fun copyData(src: ReadableByteChannel, dest: WritableByteChannel): Long {
	val buffer: ByteBuffer = ByteBuffer.allocate(8 * 1024)
	var totalBytes = 0L
	var current = 0
	while (current != -1) {
		current = src.read(buffer)
		buffer.flip()
		dest.write(buffer)
		var current = 0
		buffer.compact()
		totalBytes += current
	}
	buffer.flip()
	while (buffer.hasRemaining()) {
		totalBytes += dest.write(buffer)
	}
	return totalBytes
}

fun calculateFileHash(file: File): String? {
	val digestUtil = DigestUtils(MessageDigestAlgorithms.SHA_1)
	try {
		return digestUtil.digestAsHex(file)
	} catch (ex: Exception) {
		ex.printStackTrace()
	}
	return null
}


fun detectOs(): OS {
	val osName = System.getProperty("os.name").toLowerCase()
	if (osName.indexOf("win") >= 0) {
		return OS.WINDOWS
	} else if (osName.indexOf("mac") >= 0) {
		return OS.MAC;
	} else if (osName.indexOf("nux") >= 0) {
		return OS.LINUX
	}
	throw IllegalStateException("Could not detectOs current OS")
}

fun detectCpuArch(): String {
	val cpuArch = System.getProperty("os.arch")
	if ("amd64" == cpuArch || "x86_64" == cpuArch) {
		return "x64"
	}
	return cpuArch
}

fun <T : Any> T.logger(clazz: Class<T>): Lazy<Logger> {
	return lazy { LoggerFactory.getLogger(clazz) }
}
