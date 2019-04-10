/*
 * Copyright (c) 2019. Vladimir Djurovic
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
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