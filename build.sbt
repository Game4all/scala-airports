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

    // ajouter les dépendances sur javafx
    libraryDependencies ++= {
      val osName = System.getProperty("os.name") match {
        case n if n.startsWith("Linux")   => "linux"
        case n if n.startsWith("Mac")     => "mac"
        case n if n.startsWith("Windows") => "win"
        case _ => throw new Exception("il tourne sous quoi ton ordi ???")
      }
      Seq("base", "controls", "fxml", "graphics")
        .map(m => "org.openjfx" % s"javafx-$m" % "21.0.2" classifier osName)
    },

    // options java pour javafx
    javaOptions ++= {
      Seq(
        "--module-path",
        (Compile / dependencyClasspath).value
          .map(_.data)
          .filter(_.getName.startsWith("javafx-"))
          .map(d => {
            val p = d.toPath().getParent()
            println(p)
            d
          })
          .mkString(";"),
        "--add-modules",
        "javafx.graphics,javafx.controls,javafx.fxml"
      ),
    },
    Compile / run / fork := true
  )
