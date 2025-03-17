val scala3Version = "3.6.4"

lazy val root = project
  .in(file("."))
  .settings(
    name := "project",
    version := "0.1.0-SNAPSHOT",
    scalaVersion := scala3Version,
    libraryDependencies += "org.scalameta" %% "munit" % "1.0.0" % Test,
    libraryDependencies ++= Seq(
      "com.typesafe.slick" %% "slick" % "3.6.0", 
      "com.h2database" % "h2" % "2.3.232"
    )
  )
