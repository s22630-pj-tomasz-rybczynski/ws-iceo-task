import Dependencies._

scalaVersion := "2.13.8"

name := "ws-iceo-task"
organization := "co.iceo"
version := "1.0"

lazy val root = (project in file("."))
 .settings(libraryDependencies ++= rootDeps)
