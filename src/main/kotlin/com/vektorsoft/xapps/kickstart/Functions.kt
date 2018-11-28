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
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.File
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.channels.ReadableByteChannel
import java.nio.channels.WritableByteChannel
import java.nio.file.Path
import java.security.MessageDigest


fun defaultBaseDirLocation() : Path? = Path.of(System.getProperty(HOME_DIR_PROPERTY), DEFAULT_BASE_DIR_NAME)
fun appDirLocation(app : App) : Path = Path.of(defaultBaseDirLocation().toString(), APP_DIR_NAME, app.name)

fun jarDirLocation(binary : BinaryData) : Path {
    val parts = arrayOf(
            binary.sha1.substring(0,2),
            binary.sha1.substring(2,4),
            binary.sha1.substring(4,6)
    )
    return Path.of(defaultBaseDirLocation().toString(),
            DEPENDENCY_DIR_NAME,
            JAR_DIR_NAME,
            parts[0], parts[1],parts[2],binary.sha1)
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
    try {
        val digest = MessageDigest.getInstance("SHA-1")
        FileInputStream(file).use {
            val bytes = ByteArray(8192)
            var read : Int = it.read(bytes)
            while(read != -1) {
                digest.update(bytes, 0 , read)
                read = it.read(bytes)
            }
            val hashBytes = digest.digest()
            return toHex(hashBytes)
        }
    } catch( ex : Exception) {
        ex.printStackTrace()
    }
    return null
}

fun toHex(bytes : ByteArray) : String {
    val hexString: StringBuilder = StringBuilder()
    for (aMessageDigest:Byte in bytes) {
        var h: String = Integer.toHexString(0xFF and aMessageDigest.toInt())
        while (h.length < 2)
            h = "0$h"
        hexString.append(h)
    }
    return hexString.toString()
}

fun <T : Any> T.logger(clazz : Class<T>) : Lazy<Logger> {
    return lazy { LoggerFactory.getLogger(clazz) }
}
