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
import javafx.geometry.Insets
import javafx.scene.control.*
import javafx.scene.image.Image
import javafx.scene.image.ImageView
import javafx.scene.layout.ColumnConstraints
import javafx.scene.layout.GridPane
import javafx.scene.layout.Priority
import javafx.scene.layout.VBox



class AppListItemPanel (val app : App) : GridPane() {

    private val appIcon: ImageView
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

        appIcon = ImageView(Image(javaClass.getResourceAsStream("/img/icon-app.png")))
        addColumn(0, appIcon)

        appDetailsPane = VBox()
        appDetailsPane.children.add(Label(app.name))
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
}