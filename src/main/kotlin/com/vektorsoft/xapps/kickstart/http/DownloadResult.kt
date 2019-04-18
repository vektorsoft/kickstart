/*
 * Copyright (c) 2019. Vladimir Djurovic
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.vektorsoft.xapps.kickstart.http

import com.vektorsoft.xapps.kickstart.model.SymbolicLink

class DownloadResult (val status : Status, val length : Long) {

    enum class Status{SUCCESS, FAILURE, WARNING}
    var failureCause : Throwable? = null
    var expectedHash : String? = null
    var actualHash : String? = null
    var symLink : SymbolicLink? = null


    constructor(cause : Throwable?,  length : Long) : this(Status.FAILURE, length) {
        this.failureCause = cause
    }

    constructor(expectedHash : String, actualHash : String, length : Long) : this(Status.WARNING, length) {
        this.expectedHash = expectedHash
        this.actualHash = actualHash
    }
}