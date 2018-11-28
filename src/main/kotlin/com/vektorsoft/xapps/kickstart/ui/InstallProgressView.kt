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

package com.vektorsoft.xapps.kickstart.ui

import com.vektorsoft.xapps.kickstart.model.App
import com.vektorsoft.xapps.kickstart.service.InstallTask
import javafx.scene.control.Label
import javafx.scene.control.ProgressBar
import javafx.scene.control.ProgressIndicator
import javafx.scene.layout.FlowPane
import javafx.scene.layout.VBox

class InstallProgressView(val application : App) : FlowPane() {

    private val indicator : ProgressIndicator
    private val statusLabel : Label
    private val installTask = InstallTask(application)
    private val vbox = VBox()
    private val progressBar = ProgressBar(0.0)

    init {
        indicator = ProgressIndicator()
        children.add(indicator)

        statusLabel = Label("Initializing installation")
        statusLabel.textProperty().bind(installTask.messageProperty())
        vbox.children.add(statusLabel)
        progressBar.progressProperty().bind(installTask.progressProperty())
        vbox.children.add(progressBar)

        children.add(vbox)
    }

    fun start() {
        val thread = Thread(installTask)
        thread.start()
    }
}