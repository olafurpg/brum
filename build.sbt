def scala211 = "2.11.12"
def scala212 = "2.12.15"
def scala213 = "2.13.8"
inThisBuild(
  List(
    scalaVersion := scala213,
    crossScalaVersions := List(scala213, scala212, scala211),
    fork := true,
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
    libraryDependencies ++= List(
      "org.scala-lang" % "scala-compiler" % scalaVersion.value,
      "com.lihaoyi" %% "pprint" % "0.7.2" // only used for debugging, can be removed
    )
  )

lazy val tests = project
  .in(file("brum-tests"))
  .dependsOn(cli)
  .enablePlugins(BuildInfoPlugin)
  .settings(
    projectSettings,
    buildInfoPackage := "brum",
    buildInfoObject := "TestBuildInfo",
    buildInfoKeys := Seq[BuildInfoKey](
      "resourceDirectory" -> (Compile / resourceDirectory).value,
      "snapshotDirectory" -> (Compile / sourceDirectory).value / "snapshots"
    ),
    libraryDependencies ++= List(
      "org.scalameta" %% "munit" % "1.0.0-M2"
    )
  )

commands += Command.args("benchFast", "extra") { (s, args) =>
  s"bench/Jmh/run -i 1 -wi 1 -f1 -t1 ${args.mkString(" ")}" :: s
}

commands += Command.args("benchSlow", "extra") { (s, args) =>
  s"bench/Jmh/run -i 10 -wi 10 -f1 -t1 ${args.mkString(" ")}" :: s
}

lazy val bench = project
  .in(file("brum-bench"))
  .enablePlugins(JmhPlugin)
  .dependsOn(cli)
  .settings(
    projectSettings,
    libraryDependencies += "com.lihaoyi" %% "pprint" % "0.7.2"
  )
