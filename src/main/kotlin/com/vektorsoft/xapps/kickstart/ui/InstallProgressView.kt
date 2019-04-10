/*
 * Copyright (c) 2019. Vladimir Djurovic
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
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