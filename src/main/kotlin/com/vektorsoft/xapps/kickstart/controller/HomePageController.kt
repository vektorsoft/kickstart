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