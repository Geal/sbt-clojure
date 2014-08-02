package org.gcouprie.sbt.plugins

import sbt._
import Keys._
import java.io.File

object ClojurePlugin extends Plugin {

  private object ClojureDefaults extends Keys {
    val settings = Seq(
      clojureVersion := "1.5.1",
      libraryDependencies ++= Seq[ModuleID](
        "org.clojure" % "clojure" % clojureVersion.value % Config.name
      )
    )
  }

  //FIXME
  // generateStubs in Compile -> compile in Compile -> clojurec in Compile -> generateStubs in Test -> compile in Test -> clojurec in Test -> test in Test

  // to avoid namespace clashes, use a nested object
  object clojure extends Keys {
    val settings = Seq(ivyConfigurations += Config) ++ ClojureDefaults.settings ++ Seq(
      clojureSource in Compile := (sourceDirectory in Compile).value / "clojure",
      unmanagedResourceDirectories in Compile += {(clojureSource in Compile).value},
      clojurec in Compile := {
        val s: TaskStreams = streams.value
        val sourceDirectory : File = (clojureSource in Compile).value
        val nb = (sourceDirectory ** "*.clj").get.size
        if(nb > 0){
          val s: TaskStreams = streams.value
          s.log.info("Start Compiling Clojure sources")
          val classpath : Seq[File] = update.value.select( configurationFilter(name = "*") ) ++ Seq((classDirectory in Compile).value)
          val stubDirectory : File = (sourceManaged in Compile).value
          val destinationDirectory : File = (classDirectory in Compile).value

          def clojureClazz(file : File) : File = {
            val p = file.getAbsolutePath()
            new File(destinationDirectory.getAbsolutePath() + p.substring(sourceDirectory.getAbsolutePath().length(), p.length() - ".clj".length()) + ".class")
          }

          (sourceDirectory ** "*.clj").get map (clojureClazz) foreach {f => if(f.exists()){IO.delete(f)}}

          new ClojureC(classpath, sourceDirectory, stubDirectory, destinationDirectory).compile
        }
      },
      compile in Compile <<= (compile in Compile) dependsOn (clojurec in Compile)
    )
  }

  object testClojure extends TestKeys {
    val settings = Seq(ivyConfigurations += Config) ++ inConfig(Config)(Defaults.testTasks ++ ClojureDefaults.settings ++ Seq(
      definedTests <<= definedTests in Test,
      definedTestNames <<= definedTestNames in Test,
      fullClasspath <<= fullClasspath in Test,

      clojureSource in Test := (sourceDirectory in Test).value / "clojure",
      unmanagedResourceDirectories in Test += {(clojureSource in Test).value},
      clojurec in Test := {
        val sourceDirectory : File = (clojureSource in Test).value
        val nb = (sourceDirectory ** "*.clj").get.size
        if(nb > 0){
          val s: TaskStreams = streams.value
          s.log.info("Start Compiling Test Clojure sources")
          val classpath : Seq[File] = update.value.select( configurationFilter(name = "*") ) ++ Seq((classDirectory in Test).value) ++ Seq((classDirectory in Compile).value)
          val stubDirectory : File = (sourceManaged in Test).value
          val destinationDirectory : File = (classDirectory in Test).value

          def clojureClazz(file : File) : File = {
            val p = file.getAbsolutePath()
            new File(destinationDirectory.getAbsolutePath() + p.substring(sourceDirectory.getAbsolutePath().length(), p.length() - ".clj".length()) + ".class")
          }

          (sourceDirectory ** "*.clj").get map (clojureClazz) foreach {f => if(f.exists()){IO.delete(f)}}

          new ClojureC(classpath, sourceDirectory, stubDirectory, destinationDirectory).compile
        }
      },
      clojurec in Test <<= (clojurec in Test) dependsOn (compile in Test),
      test in Test <<= (test in Test) dependsOn (clojurec in Test)
    ))
  }
}
