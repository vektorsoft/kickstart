/*
 * Copyright (c) 2019. Vladimir Djurovic
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.vektorsoft.xapps

import com.vektorsoft.xapps.kickstart.APP_DIR_NAME
import com.vektorsoft.xapps.kickstart.appDirLocation
import com.vektorsoft.xapps.kickstart.defaultBaseDirLocation
import com.vektorsoft.xapps.kickstart.jarDirLocation
import com.vektorsoft.xapps.kickstart.model.App
import com.vektorsoft.xapps.kickstart.model.BinaryData
import org.junit.Test
import java.nio.file.Path
import kotlin.test.assertTrue

class FunctionTests {

    @Test
    fun testDependencyTargetFolder() {
        val binary = BinaryData("test-file.jar", "0123456abcdef1233", 1000)

        val target = jarDirLocation(binary)
        assertTrue(target.toString().endsWith("/jars/01/23/45/0123456abcdef1233"))
    }

    @Test
    fun testAppDirLocation() {
        val app = App("123", "Test App", "Description")
        val baseDir = defaultBaseDirLocation().toString()

        val target = appDirLocation(app)
        assertTrue(target.toString() == Path.of(baseDir, APP_DIR_NAME, app.name).toString())
    }
}