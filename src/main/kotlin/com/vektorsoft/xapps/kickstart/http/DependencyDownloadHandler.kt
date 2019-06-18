/*
 * Copyright (c) 2019. Vladimir Djurovic
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.vektorsoft.xapps.kickstart.http

import com.vektorsoft.xapps.kickstart.*
import com.vektorsoft.xapps.kickstart.model.*
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.StandardCopyOption

class DependencyDownloadHandler : DownloadHandler  {

    private val logger by logger(DependencyDownloadHandler::class.java)

    override val target: File
    val appDir : File
    val scope : JvmDependencyScope


    constructor(app : App,binaryData: JvmDependency) : super(binaryData) {
        target = jarDirLocation(binaryData).toFile()
        appDir = appDirLocation(app).toFile()
        scope = binaryData.dependencyScope
    }

    /**
     * Make symbolic links to dependencies.
     */
    override fun postprocess() {
        val linkDir =  linkDirectory()
        if(detectOs() == OS.WINDOWS) {
            // on Windows, we can't use symlinks. Need to copy actual file
            Files.copy(target.toPath(), Path.of(linkDir.absolutePath, data.fileName), StandardCopyOption.REPLACE_EXISTING)
        } else {
            val link = SymbolicLink(target.absolutePath, data.fileName)
            link.create(linkDir)
        }
    }

    private fun linkDirectory() : File {
        val linkDir : File

        if(scope == JvmDependencyScope.MODULE_PATH) {
            linkDir = File(appDir, MODULE_DIR_NAME)
        } else  {
            linkDir = File(appDir, CLASSPATH_DIR_NAME)
        }
        if(!linkDir.exists()) {
            logger.info("Creating dependnecy link directory {}", linkDir.absolutePath)
            linkDir.mkdirs()
        }
        return  linkDir
    }
}