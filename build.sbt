sbtPlugin := true

name := "sbt-clojure"

organization := "com.unhandledexpression"

version := "0.2-SNAPSHOT"

scalaVersion := "2.10.2"

publishMavenStyle := true

publishTo := {
  val nexus = "https://oss.sonatype.org/"
  if (version.value.trim.endsWith("SNAPSHOT"))
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases"  at nexus + "service/local/staging/deploy/maven2")
}

libraryDependencies += "org.clojure" % "clojure" % "1.5.1"

publishArtifact in Test := false

pomIncludeRepository := { _ => false }

pomExtra := (
  <url>https://github.com/Geal/sbt-clojure</url>
  <licenses>
    <license>
      <name>MIT</name>
      <url>http://opensource.org/licenses/mit-license.php</url>
      <distribution>repo</distribution>
    </license>
  </licenses>
  <scm>
    <url>git@github.com:Geal/sbt-clojure.git</url>
    <connection>scm:git:git@github.com:Geal/sbt-clojure.git</connection>
  </scm>
  <developers>
    <developer>
      <id>gcouprie</id>
      <name>Geoffroy Couprie</name>
      <url>http://geoffroycouprie.com</url>
    </developer>
  </developers>)
