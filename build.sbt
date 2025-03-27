val scala3Version = "3.6.4"

lazy val root = project
  .in(file("."))
  .settings(
    name := "project",
    version := "0.1.0-SNAPSHOT",
    scalaVersion := scala3Version,
    libraryDependencies ++= Seq(
      "org.scalameta" %% "munit" % "1.0.0" % Test,
      "org.slf4j" % "slf4j-simple" % "2.0.7",
      "com.typesafe.slick" %% "slick" % "3.6.0", 
      "com.h2database" % "h2" % "2.3.232",
      "org.scalafx" %% "scalafx" % "21.0.0-R32"
    ),

    javaOptions ++= Seq(
      "--module-path", "C:/Program Files/javafx-sdk-21.0.6/lib",
      "--add-modules", "javafx.controls,javafx.fxml,javafx.graphics"
    ),

    Compile / run / fork := true
  )