/*
 * Copyright (c) 2019. Vladimir Djurovic
 *
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this
 * file, You can obtain one at http://mozilla.org/MPL/2.0/.
 */

package com.vektorsoft.xapps.kickstart

import com.vektorsoft.xapps.kickstart.controller.HomePageController
import com.vektorsoft.xapps.kickstart.http.DefaultHttpClient
import com.vektorsoft.xapps.kickstart.model.AppModel
import javafx.application.Application
import javafx.scene.Scene
import javafx.stage.Stage
import javafx.scene.Parent
import javafx.fxml.FXMLLoader
import java.util.*


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

	override fun init() {
		val input = this.javaClass.getResourceAsStream("/config.properties")
		input.use {
			val props = Properties()
			props.load(it)
			AppModel.currentSeverBaseUrl.value = props.getProperty(DEFAULT_SERVER_PROPERTY)
		}
	}
}

fun main(args : Array<String>) {
    Application.launch(XApps::class.java, *args)
}