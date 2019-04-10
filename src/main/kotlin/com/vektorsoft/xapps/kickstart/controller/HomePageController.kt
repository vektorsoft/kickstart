/*
 * Copyright (c) 2019. Vladimir Djurovic
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.vektorsoft.xapps.kickstart.controller

import com.vektorsoft.xapps.kickstart.model.App
import com.vektorsoft.xapps.kickstart.model.AppModel
import com.vektorsoft.xapps.kickstart.ui.AppListItemPanel
import javafx.application.Platform
import javafx.collections.ListChangeListener
import javafx.fxml.FXML
import javafx.geometry.Pos
import javafx.scene.control.ProgressIndicator
import javafx.scene.layout.VBox

class HomePageController : ListChangeListener<App> {

    @FXML
    var progressIndicator : ProgressIndicator? = null

    @FXML
    var listContainer : VBox? = null

    constructor() {
        AppModel.appList.addListener(this)
    }

    override fun onChanged(change: ListChangeListener.Change<out App>?) {
        println("in list apps")
        Platform.runLater {
            listContainer?.children?.clear()
            listContainer?.alignment = Pos.TOP_CENTER
            listContainer?.alignment = Pos.TOP_CENTER

            change?.list?.forEach { listContainer?.children?.add(AppListItemPanel(it)) }
        }

    }
}