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

import com.vektorsoft.xapps.kickstart.appDirLocation
import com.vektorsoft.xapps.kickstart.model.App
import com.vektorsoft.xapps.kickstart.model.BinaryData
import java.io.File
import java.nio.file.Path

class SimpleDownloadHandler : DownloadHandler {

    override val target: File

    constructor(app : App, binaryData: BinaryData) : super(binaryData) {
        target = Path.of(appDirLocation(app).toString(), binaryData.fileName).toFile()
    }

    override fun postprocess() {
        // nothing to do
    }
}