/*
 * Copyright (c) 2019. Vladimir Djurovic
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.vektorsoft.xapps.kickstart.model

const val JDK_8_VALUE = "8"
const val JDK_9_VALUE = "9"
const val JDK_10_VALUE = "10"
const val JDK_11_VALUE = "11"
const val JDK_12_VALUE = "12"

enum class JdkVersion(val display : String) {
	JDK_8(JDK_8_VALUE),
	JDK_9 (JDK_9_VALUE),
	JDK_10 (JDK_10_VALUE),
	JDK_11 (JDK_11_VALUE),
	JDK_12 (JDK_12_VALUE)
}