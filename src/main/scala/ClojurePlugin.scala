package org.gcouprie.sbt.plugins

import sbt._
import Keys._
import java.io.File

object ClojurePlugin extends Plugin {

  private object ClojureDefaults extends Keys {
    val settings = Seq(
      //FIXME
      //groovyVersion := "2.1.7",
      clojureVersion := "1.5.1",
      libraryDependencies ++= Seq[ModuleID](
        //FIXME
        "org.clojure" % "clojure" % clojureVersion.value % Config.name
        //"org.codehaus.groovy" % "groovy-all" % groovyVersion.value % Config.name,
        //"org.apache.ant" % "ant" % "1.8.4" % Config.name
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
      generateStubs in Compile := {
        val sourceDirectory : File = (clojureSource in Compile).value
        val nb = (sourceDirectory ** "*.clj").get.size
        if(nb > 0){
          val s: TaskStreams = streams.value
          s.log.info("Start Generating Stubs from Clojure sources")
          val classpath : Seq[File] = update.value.select( configurationFilter(name = "*") )
          val stubDirectory : File = (sourceManaged in Compile).value / "clojure"
          val destinationDirectory : File = stubDirectory
          new ClojureC(classpath, sourceDirectory, stubDirectory, destinationDirectory).generateStubs
        }
        else{
          Nil
        }
      },
      sourceGenerators in Compile <+= generateStubs in Compile,
      clojurec in Compile := {
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
      compile in Compile <<= (compile in Compile) dependsOn (generateStubs in Compile),
      clojurec in Compile <<= (clojurec in Compile) dependsOn (compile in Compile)
    )
  }

  object testClojure extends TestKeys {
    val settings = Seq(ivyConfigurations += Config) ++ inConfig(Config)(Defaults.testTasks ++ ClojureDefaults.settings ++ Seq(
      definedTests <<= definedTests in Test,
      definedTestNames <<= definedTestNames in Test,
      fullClasspath <<= fullClasspath in Test,

      clojureSource in Test := (sourceDirectory in Test).value / "clojure",
      unmanagedResourceDirectories in Test += {(clojureSource in Test).value},
      generateStubs in Test := {
        val sourceDirectory : File = (clojureSource in Test).value
        val nb = (sourceDirectory ** "*.clj").get.size
        if(nb > 0){
	        val s: TaskStreams = streams.value
	        s.log.info("Start Generating Stubs from Test Clojure sources")
	        val classpath : Seq[File] = update.value.select( configurationFilter(name = "*") )
	        val stubDirectory : File = (sourceManaged in Test).value
	        val destinationDirectory : File = stubDirectory
	        new ClojureC(classpath, sourceDirectory, stubDirectory, destinationDirectory).generateStubs
        }
        else{
          Nil
        }
      },
      sourceGenerators in Test <+= generateStubs in Test,
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
      generateStubs in Test <<= (generateStubs in Test) dependsOn (clojurec in Compile),
      compile in Test <<= (compile in Test) dependsOn (generateStubs in Test),
      clojurec in Test <<= (clojurec in Test) dependsOn (compile in Test),
      test in Test <<= (test in Test) dependsOn (clojurec in Test)
    ))
  }
}
