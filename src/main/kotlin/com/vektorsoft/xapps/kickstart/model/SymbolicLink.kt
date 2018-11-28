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

package com.vektorsoft.xapps.kickstart.model

import com.vektorsoft.xapps.kickstart.logger
import java.io.File
import java.nio.file.Files
import java.nio.file.Path

data class SymbolicLink(val target : String, val linkName : String) {

     val LOGGER by logger(SymbolicLink::class.java)

    fun create(linkDir : File) {
        val targetPath = Path.of(target)
        val linkPath = Path.of(linkDir.absolutePath, linkName)
        if(Files.exists(linkPath)) {
            Files.delete(linkPath)
        }
        Files.createSymbolicLink(linkPath, targetPath)
        LOGGER.debug("Created symbolic link {} to {}", linkPath, targetPath)

    }
}