/*
 * Copyright (c) 2019. Vladimir Djurovic
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.vektorsoft.xapps.kickstart.model

import javafx.beans.property.SimpleListProperty
import javafx.beans.property.SimpleLongProperty
import javafx.beans.property.SimpleStringProperty
import javafx.collections.FXCollections

object AppModel {
    val appList = SimpleListProperty<App>(FXCollections.observableArrayList())
    private val downloadProgress = SimpleLongProperty()

    val currentSeverBaseUrl = SimpleStringProperty()

}