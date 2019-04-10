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

package com.vektorsoft.xapps.kickstart

import com.vektorsoft.xapps.kickstart.model.App
import com.vektorsoft.xapps.kickstart.model.BinaryData
import com.vektorsoft.xapps.kickstart.model.OS
import org.apache.commons.codec.digest.DigestUtils
import org.apache.commons.codec.digest.MessageDigestAlgorithms
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.nio.ByteBuffer
import java.nio.channels.ReadableByteChannel
import java.nio.channels.WritableByteChannel
import java.nio.file.Path


fun defaultBaseDirLocation() : Path? = Path.of(System.getProperty(HOME_DIR_PROPERTY), DEFAULT_BASE_DIR_NAME)
fun appDirLocation(app : App) : Path = Path.of(defaultBaseDirLocation().toString(), APP_DIR_NAME, app.name)

fun jarDirLocation(binary : BinaryData) : Path {
    val parts = arrayOf(
            binary.hash.substring(0,2),
            binary.hash.substring(2,4),
            binary.hash.substring(4,6)
    )
    return Path.of(defaultBaseDirLocation().toString(),
            DEPENDENCY_DIR_NAME,
            JAR_DIR_NAME,
            parts[0], parts[1],parts[2],binary.hash)
}



fun copyData(src : ReadableByteChannel, dest: WritableByteChannel) : Long {
    val buffer : ByteBuffer = ByteBuffer.allocate(8 * 1024)
    var totalBytes = 0L
    var current = 0
    while (current != -1) {
        current = src.read(buffer)
        buffer.flip()
        dest.write(buffer)
        buffer.compact()
        totalBytes += current
    }
    buffer.flip()
    while(buffer.hasRemaining()) {
        totalBytes += dest.write(buffer)
    }
    return totalBytes
}

fun calculateFileHash(file : File) : String? {
    val digestUtil = DigestUtils(MessageDigestAlgorithms.SHA_1)
    try {
        return digestUtil.digestAsHex(file)
    } catch(ex: Exception) {
        ex.printStackTrace()
    }
    return null
}


fun detectOs() : OS {
    val osName = System.getProperty("os.name").toLowerCase()
    if(osName.indexOf("win") >= 0) {
        return OS.WINDOWS
    } else if (osName.indexOf("mac") >= 0) {
        return OS.MAC;
    } else if(osName.indexOf("nux") >= 0) {
        return OS.LINUX
    }
    throw IllegalStateException("Could not detectOs current OS")
}

fun detectCpuArch() : String {
    val cpuArch = System.getProperty("os.arch")
    if("amd64" == cpuArch || "x86_64" == cpuArch) {
        return "x64"
    }
    return cpuArch
}

fun <T : Any> T.logger(clazz : Class<T>) : Lazy<Logger> {
    return lazy { LoggerFactory.getLogger(clazz) }
}
