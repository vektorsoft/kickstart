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

import com.vektorsoft.xapps.kickstart.model.JvmDependencyScope
import com.vektorsoft.xapps.kickstart.model.SymbolicLink

class DownloadResult (val status : Status, val length : Long, val dependencyScope: JvmDependencyScope?) {

    enum class Status{SUCCESS, FAILURE, WARNING}
    var failureCause : Throwable? = null
    var expectedHash : String? = null
    var actualHash : String? = null
    var symLink : SymbolicLink? = null


    constructor(cause : Throwable?,  length : Long,  dependencyScope: JvmDependencyScope?) : this(Status.FAILURE, length, dependencyScope) {
        this.failureCause = cause
    }

    constructor(expectedHash : String, actualHash : String, length : Long,  dependencyScope: JvmDependencyScope?) : this(Status.WARNING, length, dependencyScope) {
        this.expectedHash = expectedHash
        this.actualHash = actualHash
    }
}