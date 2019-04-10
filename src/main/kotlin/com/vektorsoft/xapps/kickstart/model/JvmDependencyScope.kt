/*
 * Copyright (c) 2019. Vladimir Djurovic
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.vektorsoft.xapps.kickstart.model

import com.fasterxml.jackson.annotation.JsonValue
import com.vektorsoft.xapps.kickstart.DEPENDENCY_SCOPE_CLASSPATH
import com.vektorsoft.xapps.kickstart.DEPENDENCY_SCOPE_MODULEPATH

enum class JvmDependencyScope (@JsonValue val data : String) {

    MODULE_PATH (DEPENDENCY_SCOPE_MODULEPATH),
    CLASSPATH(DEPENDENCY_SCOPE_CLASSPATH);
}