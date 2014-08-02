package org.gcouprie.sbt.plugins

import sbt._
import sbt.Keys._
import java.io.File

import sbt.classpath.ClasspathUtilities

//import clojure.lang.RT
import clojure.lang.Var
//import clojure.lang.Compiler
//import clojure.lang.RT._
//import clojure.java.api

class ClojureC(val classpath : Seq[File], val sourceDirectory : File, val stubDirectory : File, val destinationDirectory : File) {

    lazy val oldContextClassLoader = Thread.currentThread.getContextClassLoader

    classpath.map(println)
    lazy val classLoader = ClasspathUtilities.toLoader(classpath)
    lazy val clojureClass = classLoader.loadClass("org.clojure.core")
    lazy val rt =  classLoader.loadClass("clojure.lang.RT")
    lazy val varClass =  classLoader.loadClass("clojure.lang.Var")
    lazy val varFunction = rt.getDeclaredMethod("var", classOf[java.lang.String], classOf[java.lang.String])
    lazy val loadResourceFunction = rt.getDeclaredMethod("loadResourceScript", classOf[java.lang.String])
    lazy val rtLoadFunction = rt.getDeclaredMethod("load", classOf[java.lang.String])
    lazy val rtInitFunction = rt.getDeclaredMethod("init")
    lazy val rtMap          = rt.getDeclaredMethod("map", classOf[Array[Object]])

    /*lazy val projectClass = classLoader.loadClass("org.apache.tools.ant.Project")
    lazy val generateStubsClass = classLoader.loadClass("org.codehaus.groovy.ant.GenerateStubsTask")
    lazy val groovycClass = classLoader.loadClass("org.codehaus.groovy.ant.Groovyc")
    lazy val javacClass = classLoader.loadClass("org.apache.tools.ant.taskdefs.Javac")
    lazy val pathClass = classLoader.loadClass("org.apache.tools.ant.types.Path")

    lazy val pathConstructor = pathClass.getConstructor(projectClass)
    lazy val setLocationMethod = pathClass.getMethod("setLocation", classOf[java.io.File])

    lazy val setGroovycSrcdirMethod = groovycClass.getMethod("setSrcdir", pathClass)
    lazy val setGroovycStubdirMethod = groovycClass.getMethod("setStubdir", classOf[java.io.File])
    lazy val setGroovycDestdirMethod = groovycClass.getMethod("setDestdir", classOf[java.io.File])
    lazy val setGroovycProjectMethod = groovycClass.getMethod("setProject", projectClass)
    lazy val addGroovycConfiguredJavacMethod = groovycClass.getMethod("addConfiguredJavac", javacClass)
    lazy val setGroovycKeepStubsMethod = groovycClass.getMethod("setKeepStubs", java.lang.Boolean.TYPE)
    lazy val setGroovycVerboseMethod = groovycClass.getMethod("setVerbose", java.lang.Boolean.TYPE)
    lazy val executeGroovycMethod = groovycClass.getMethod("execute")*/

    def compile() : Unit =  {
        IO.createDirectory(sourceDirectory)
        IO.createDirectory(destinationDirectory)
        val comp = varFunction.invoke("clojure.core", "compile").asInstanceOf[Var]
        try{
          //Thread.currentThread.setContextClassLoader(classLoader)
          /*val project = projectClass.newInstance()
          val javac = javacClass.newInstance()
          val groovyc = groovycClass.newInstance()
          val path = pathConstructor.newInstance(project.asInstanceOf[AnyRef])
          setLocationMethod.invoke(path, sourceDirectory)
          setGroovycSrcdirMethod.invoke(groovyc, path.asInstanceOf[AnyRef])
          setGroovycStubdirMethod.invoke(groovyc, stubDirectory)
          setGroovycDestdirMethod.invoke(groovyc, destinationDirectory)
          setGroovycProjectMethod.invoke(groovyc, project.asInstanceOf[AnyRef])
          addGroovycConfiguredJavacMethod.invoke(groovyc, javac.asInstanceOf[AnyRef])
          setGroovycKeepStubsMethod.invoke(groovyc, true.asInstanceOf[AnyRef])
          setGroovycVerboseMethod.invoke(groovyc, true.asInstanceOf[AnyRef])
          executeGroovycMethod.invoke(groovyc)*/

          println("pouet")
          comp.invoke("main")
        }
        finally{
          //Thread.currentThread.setContextClassLoader(oldContextClassLoader)          
        }
    }

    /*lazy val setGenerateStubsSrcdirMethod = generateStubsClass.getMethod("setSrcdir", pathClass)
    lazy val setGenerateStubsDestdirMethod = generateStubsClass.getMethod("setDestdir", classOf[java.io.File])
    lazy val setGenerateStubsProjectMethod = generateStubsClass.getMethod("setProject", projectClass)
    lazy val executeGenerateStubsMethod = generateStubsClass.getMethod("execute")
    */

    def generateStubs() : Seq[File] =  {
        println("source:      "+sourceDirectory)
        println("stubs:       "+stubDirectory)
        println("destination: "+destinationDirectory)
        IO.createDirectory(sourceDirectory)
        IO.createDirectory(stubDirectory)
        try{
          //Thread.currentThread.setContextClassLoader(classLoader)
          /*val project = projectClass.newInstance()
          val generateStubs = generateStubsClass.newInstance()
          val path = pathConstructor.newInstance(project.asInstanceOf[AnyRef])
          setLocationMethod.invoke(path, sourceDirectory)
          setGenerateStubsSrcdirMethod.invoke(generateStubs, path.asInstanceOf[AnyRef])
          setGenerateStubsDestdirMethod.invoke(generateStubs, stubDirectory)
          setGenerateStubsProjectMethod.invoke(generateStubs, project.asInstanceOf[AnyRef])
          executeGenerateStubsMethod.invoke(generateStubs)
          */
          Thread.currentThread().setContextClassLoader(classLoader)
          //rtLoadFunction.invoke(null, Array("clojure/core"))
          //loadResourceFunction.invoke(null, "src/main/clojure/hello.clj")
          rtInitFunction.invoke(null)
          val compilerClass   = classLoader.loadClass("clojure.lang.Compiler")
          val loadFunction    = compilerClass.getDeclaredMethod("load", classOf[java.io.Reader])
          val compileFunction = compilerClass.getDeclaredMethod("compile", classOf[java.io.Reader], classOf[java.lang.String], classOf[java.lang.String])
          //loadFunction.invoke(null, new java.io.StringReader("(ns user) (println \"Hello from compiler\")"))

          val associativeClass = classLoader.loadClass("clojure.lang.Associative")
          val pushTBFunction = varClass.getDeclaredMethod("pushThreadBindings", associativeClass)
          val popTBFunction  = varClass.getDeclaredMethod("popThreadBindings")

          val compilePath = varFunction.invoke(null, "clojure.core", "*compile-path*")
          val compileFiles = varFunction.invoke(null, "clojure.core", "*compile-files*")

          val newMap = rtMap.invoke(null, Array(compilePath, destinationDirectory.getAbsolutePath(), compileFiles, true:java.lang.Boolean))
          pushTBFunction.invoke(null, newMap)
          println("sourcepath:      "+sourceDirectory.getAbsolutePath()+"/hello.clj")
          compileFunction.invoke(null, new java.io.StringReader("(ns user) (println \"Hello from compiler\")"), sourceDirectory.getAbsolutePath()+"/hello.clj", "coincoin.clj")
          popTBFunction.invoke(null)

          println("pouetstubs")
        }
        finally{
          Thread.currentThread.setContextClassLoader(oldContextClassLoader)
        }
        (stubDirectory ** "*.java").get
    }

}
