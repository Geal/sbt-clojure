package com.unhandledexpression.sbtclojure

import sbt._
import Keys._
import java.io.File

object ClojurePlugin extends AutoPlugin {

  private object ClojureDefaults extends Keys {
    val settings = Seq(
      clojureVersion := "1.11.1",
      libraryDependencies ++= Seq[ModuleID](
        "org.clojure" % "clojure" % clojureVersion.value % Config.name
      )
    )
  }

  object clojure extends Keys {
    val settings = Seq(ivyConfigurations += Config) ++ ClojureDefaults.settings ++ Seq(
      Compile/clojureSource  := (Compile/sourceDirectory ).value / "clojure",
      Compile/unmanagedResourceDirectories  += {(Compile/clojureSource ).value},
      Compile/clojurec  := {
        val s: TaskStreams = streams.value
        val sourceDirectory : File = (Compile/clojureSource ).value
        val nb = (sourceDirectory ** "*.clj").get.size
        if(nb > 0){
          val s: TaskStreams = streams.value
          s.log.info("Start Compiling Clojure sources")
          val classpath : Seq[File] = update.value.select( configurationFilter(name = "*") ) ++ Seq((Compile/classDirectory ).value)
          val stubDirectory : File = (Compile/sourceManaged ).value
          val destinationDirectory : File = (Compile/classDirectory ).value

          def clojureClazz(file : File) : File = {
            val p = file.getAbsolutePath()
            new File(destinationDirectory.getAbsolutePath() + p.substring(sourceDirectory.getAbsolutePath().length(), p.length() - ".clj".length()) + ".class")
          }

          (sourceDirectory ** "*.clj").get map (clojureClazz) foreach {f => if(f.exists()){IO.delete(f)}}

          new ClojureC(classpath, sourceDirectory, stubDirectory, destinationDirectory).compile
        }
      },
      Compile / compile  := ((Compile/compile) dependsOn (Compile/clojurec )).value
    )
  }

  object testClojure extends TestKeys {
    val settings = Seq(ivyConfigurations += Config) ++ inConfig(Config)(Defaults.testTasks ++ ClojureDefaults.settings ++ Seq(
      definedTests := (Test/definedTests).value ,
      definedTestNames := (Test/definedTestNames).value ,
      fullClasspath := (Test/fullClasspath ).value,

      Test/clojureSource  := (Test/sourceDirectory ).value / "clojure",
      Test/unmanagedResourceDirectories  += {(Test/clojureSource ).value},
      Test/clojurec  := {
        val sourceDirectory : File = (Test/clojureSource ).value
        val nb = (sourceDirectory ** "*.clj").get.size
        val s: TaskStreams = streams.value
        if(nb > 0){
          s.log.info("Start Compiling Test Clojure sources")
          val classpath : Seq[File] = update.value.select( configurationFilter(name = "*") ) ++ Seq((Test/classDirectory ).value) ++ Seq((Compile/classDirectory ).value)
          val stubDirectory : File = (Test/sourceManaged ).value
          val destinationDirectory : File = (Test/classDirectory ).value

          def clojureClazz(file : File) : File = {
            val p = file.getAbsolutePath()
            new File(destinationDirectory.getAbsolutePath() + p.substring(sourceDirectory.getAbsolutePath().length(), p.length() - ".clj".length()) + ".class")
          }

          (sourceDirectory ** "*.clj").get map (clojureClazz) foreach {f => if(f.exists()){IO.delete(f)}}

          new ClojureC(classpath, sourceDirectory, stubDirectory, destinationDirectory).compile
        }
      },
      Test/clojurec  := ((Test/clojurec ) dependsOn (Test/compile )).value,
      Test/test  := ((Test/test ) dependsOn (Test/clojurec) ).value
    ))
  }
}
