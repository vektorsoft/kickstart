/*
 * Copyright (c) 2019. Vladimir Djurovic
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.vektorsoft.xapps.kickstart.http

import com.vektorsoft.xapps.kickstart.appDirLocation
import com.vektorsoft.xapps.kickstart.logger
import com.vektorsoft.xapps.kickstart.model.App
import com.vektorsoft.xapps.kickstart.model.BinaryData
import java.io.File
import java.nio.file.Path

class LauncherDownloadHandler : DownloadHandler {

    private val logger by logger(LauncherDownloadHandler::class.java)

    override val target: File

    constructor(app : App, binaryData: BinaryData) : super(binaryData) {
        target = Path.of(appDirLocation(app).toString(), binaryData.fileName).toFile()
    }

    override fun postprocess() {
        if(target.setExecutable(true)) {
            logger.debug("Setting executable flag for launcher {}", target.absolutePath)
        } else {
            logger.error("Could not set executable flag for file {}", target.absolutePath)
        }
    }

}