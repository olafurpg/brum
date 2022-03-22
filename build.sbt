def scala211 = "2.11.12"
def scala212 = "2.12.15"
def scala213 = "2.13.8"
inThisBuild(
  List(
    scalaVersion := scala213,
    semanticdbEnabled := true,
    semanticdbVersion := "4.5.1",
    scalafixDependencies += "com.github.liancheng" %% "organize-imports" % "0.6.0"
  )
)

val projectSettings = List[Def.Setting[_]](
  scalacOptions ++= {
    if (scalaVersion.value.startsWith("2.13"))
      List("-Wunused:imports", "-Yrangepos")
    else
      List("-Ywarn-unused-import", "-Yrangepos")
  }
)

lazy val cli = project
  .in(file("brum-cli"))
  .settings(
    projectSettings,
    crossScalaVersions := List(scala213, scala212, scala211),
    fork := true,
    libraryDependencies ++= List(
      "org.scala-lang" % "scala-compiler" % scalaVersion.value,
      "com.lihaoyi" %% "pprint" % "0.7.2" // only used for debugging, can be removed
    )
  )

commands += Command.command("benchFast") { s =>
  "bench/Jmh/run -i 3 -wi 3 -f1 -t1" :: s
}
commands += Command.command("benchSlow") { s =>
  "bench/Jmh/run -i 10 -wi 10 -f1 -t1" :: s
}

lazy val bench = project
  .in(file("brum-bench"))
  .enablePlugins(JmhPlugin)
  .dependsOn(cli)
  .settings(
    projectSettings,
    libraryDependencies += "com.lihaoyi" %% "pprint" % "0.7.2"
  )
