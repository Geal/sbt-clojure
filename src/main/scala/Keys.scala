package org.gcouprie.sbt.plugins

import sbt._
import sbt.Keys._
import java.io.File

trait Keys {

    lazy val Config = config("clojure") extend(Compile) hide
    lazy val clojureVersion = settingKey[String]("Clojure version")
    lazy val clojureSource = settingKey[File]("Default Clojure source directory")
    lazy val clojurec = taskKey[Unit]("Compile Clojure sources")

}

trait TestKeys extends Keys {
	override lazy val Config = config("test-clojure") extend(Test) hide
}

trait IntegrationTestKeys extends TestKeys {
	override lazy val Config = config("it-clojure") extend(IntegrationTest) hide
}
