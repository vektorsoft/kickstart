/*
 * Copyright (c) 2019. Vladimir Djurovic
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.vektorsoft.xapps.kickstart.ui


import com.vektorsoft.xapps.kickstart.http.DefaultHttpClient
import com.vektorsoft.xapps.kickstart.model.App
import javafx.application.Platform
import javafx.geometry.Insets
import javafx.scene.control.*
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.ColumnConstraints
import javafx.scene.layout.GridPane
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox
import java.net.http.HttpResponse
import java.util.function.Consumer


class AppListItemPanel (val app : App) : GridPane() {

//    private val appIcon: ImageView
    private val appDetailsPane: VBox
    private val buttonsPane: VBox
    private val installButton: Button
    private val removeButton: Button

    init {
        val iconCol = ColumnConstraints()
        iconCol.hgrow = Priority.NEVER

        val detailsCol = ColumnConstraints()
        detailsCol.hgrow = Priority.ALWAYS

        val buttonsCol = ColumnConstraints()
        buttonsCol.hgrow = Priority.NEVER

        columnConstraints.addAll(iconCol, detailsCol, buttonsCol)

        DefaultHttpClient.getAppImage(app.id, { loadAppImage(it)}, { loadDefaultImage() })

        appDetailsPane = VBox()
        appDetailsPane.children.add(Label(app.name))
        appDetailsPane.children.add(Label(app.headline))
        appDetailsPane.children.add(Label(app.description))
        addColumn(1, appDetailsPane)

        buttonsPane = VBox()
        buttonsPane.spacing = 5.0
        buttonsPane.padding = Insets(10.0)
        installButton = Button("Install")
        installButton.setOnAction { handleInstallButton() }
        removeButton = Button("Remove")

        buttonsPane.children.addAll(installButton, removeButton)

        addColumn(2, buttonsPane)


    }

    private fun handleInstallButton() {
        val alert = Alert(Alert.AlertType.CONFIRMATION, "Do you want to install " + app.name + "?", ButtonType.YES, ButtonType.NO)
        alert.title = "Confirm installation"
        alert.showAndWait().filter { response -> response == ButtonType.YES }
                .ifPresent { response -> startInstall() }
    }

    private fun startInstall() {
        val installView = InstallProgressView(app)
        add(installView, 0, 1, 3, 1)
        installView.start()
    }

    private fun loadAppImage(data : ByteArray) {
        Platform.runLater {
            if(data.isEmpty()) {
                addColumn(0, ImageView(Image(javaClass.getResourceAsStream("/img/icon-app.png"))))
            } else {
                addColumn(0, ImageView(Image(data.inputStream())))
            }
        }
    }

    private fun loadDefaultImage() : Void? {
        Platform.runLater {
            val appIcon = ImageView(Image(javaClass.getResourceAsStream("/img/icon-app.png")))
            addColumn(0, appIcon)
        }
        return null
    }

}