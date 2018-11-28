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

import com.fasterxml.jackson.annotation.JsonValue
import com.vektorsoft.xapps.kickstart.DEPENDENCY_SCOPE_CLASSPATH
import com.vektorsoft.xapps.kickstart.DEPENDENCY_SCOPE_MODULEPATH

enum class JvmDependencyScope (@JsonValue val data : String) {

    MODULE_PATH (DEPENDENCY_SCOPE_MODULEPATH),
    CLASSPATH(DEPENDENCY_SCOPE_CLASSPATH);
}