/*
 * Copyright (c) 2019. Vladimir Djurovic
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.vektorsoft.xapps.kickstart.model

open class JvmDependency(fileName: String,
						 hash: String,
						 size: Long,
						 val dependencyScope: JvmDependencyScope) : BinaryData(fileName, hash, size) {
}