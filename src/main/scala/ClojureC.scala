package com.unhandledexpression.sbtclojure

import sbt.*
import sbt.Keys.*
import sbt.internal.inc.classpath.ClasspathUtilities
import sbt.internal.inc.classpath._
import java.io.File


class ClojureC(val classpath : Seq[File], val sourceDirectory : File, val stubDirectory : File, val destinationDirectory : File) {

    lazy val oldContextClassLoader = Thread.currentThread.getContextClassLoader

    lazy val classLoader = ClasspathUtil.toLoader(classpath)
    lazy val clojureClass = classLoader.loadClass("org.clojure.core")
    lazy val rt =  classLoader.loadClass("clojure.lang.RT")
    lazy val varClass =  classLoader.loadClass("clojure.lang.Var")
    lazy val varFunction = rt.getDeclaredMethod("var", classOf[java.lang.String], classOf[java.lang.String])
    lazy val loadResourceFunction = rt.getDeclaredMethod("loadResourceScript", classOf[java.lang.String])
    lazy val rtLoadFunction = rt.getDeclaredMethod("load", classOf[java.lang.String])
    lazy val rtInitFunction = rt.getDeclaredMethod("init")
    lazy val rtMap          = rt.getDeclaredMethod("map", classOf[Array[Object]])

    def getRecursiveListOfFiles(dir: File): Array[File] = {
      val files = dir.listFiles
      files ++ files.filter(_.isDirectory).flatMap(getRecursiveListOfFiles)
    }

    def compile() : Unit =  {
        val files = getRecursiveListOfFiles(sourceDirectory)

        IO.createDirectory(sourceDirectory)
        IO.createDirectory(destinationDirectory)
        try{
          Thread.currentThread().setContextClassLoader(classLoader)
          rtInitFunction.invoke(null)
          val compilerClass   = classLoader.loadClass("clojure.lang.Compiler")
          val loadFunction    = compilerClass.getDeclaredMethod("load", classOf[java.io.Reader])
          val compileFunction = compilerClass.getDeclaredMethod("compile", classOf[java.io.Reader], classOf[java.lang.String], classOf[java.lang.String])

          val associativeClass = classLoader.loadClass("clojure.lang.Associative")
          val pushTBFunction = varClass.getDeclaredMethod("pushThreadBindings", associativeClass)
          val popTBFunction  = varClass.getDeclaredMethod("popThreadBindings")

          val compilePath = varFunction.invoke(null, "clojure.core", "*compile-path*")
          val compileFiles = varFunction.invoke(null, "clojure.core", "*compile-files*")

          val newMap = rtMap.invoke(null, Array(compilePath, destinationDirectory.getAbsolutePath(), compileFiles, true:java.lang.Boolean))
          pushTBFunction.invoke(null, newMap)

          files.filter(!_.isDirectory).map { f =>
            //val suffix = f.toString.stripPrefix(sourceDirectory.toString)
            compileFunction.invoke(null, new java.io.FileReader(f.getAbsolutePath()), f.getName(), f.getName())
          }

          popTBFunction.invoke(null)
        }
        finally{
          Thread.currentThread.setContextClassLoader(oldContextClassLoader)
        }
    }
}
