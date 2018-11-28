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

package com.vektorsoft.xapps.kickstart

import com.vektorsoft.xapps.kickstart.controller.HomePageController
import com.vektorsoft.xapps.kickstart.http.DefaultHttpClient
import javafx.application.Application
import javafx.scene.Scene
import javafx.stage.Stage
import javafx.scene.Parent
import javafx.fxml.FXMLLoader


class XApps : Application() {

    override fun start(stage: Stage?) {
        val loader = FXMLLoader(javaClass.getResource("/fxml/home-page.fxml"))
        loader.setController(HomePageController())
        val root = loader.load<Parent>()


        val scene = Scene(root, 800.0, 600.0)
        scene.stylesheets.add("/styles/Styles.css")

        DefaultHttpClient.getAppList()

        stage?.setTitle("KickStart")
        stage?.setScene(scene)
        stage?.show()
    }
}

fun main(args : Array<String>) {
    Application.launch(XApps::class.java, *args)
}