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

import com.vektorsoft.xapps.kickstart.*
import com.vektorsoft.xapps.kickstart.model.App
import com.vektorsoft.xapps.kickstart.model.JvmDependency
import com.vektorsoft.xapps.kickstart.model.JvmDependencyScope
import com.vektorsoft.xapps.kickstart.model.SymbolicLink
import java.io.File

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
        val link = SymbolicLink(target.absolutePath, data.fileName)
        link.create(linkDir)
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