## sbt project compiled with Scala 3

### Usage

This is a normal sbt project. You can compile code with `sbt compile`, run it with `sbt run`, and `sbt console` will start a Scala 3 REPL.

For more information on the sbt-dotty plugin, see the
[scala3-example-project](https://github.com/scala/scala3-example-project/blob/main/README.md).

For running ScalaFx on this project, please make sure you have JavaFX installed on your system. You can download it from [here](https://gluonhq.com/products/javafx/). And replace the javaOptions "--module-path" in the build.sbt file with the path to your JavaFX lib folder.
Also, you need to have Java 17+ installed on your system.
