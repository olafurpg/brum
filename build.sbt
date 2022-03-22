def scala211 = "2.11.12"
def scala212 = "2.12.15"
def scala213 = "2.13.8"
inThisBuild(List(
  scalaVersion := scala213,
  ))

lazy val cli = project
  .in(file("brum-cli"))
  .settings(
    crossScalaVersions := List(scala213, scala212, scala211),
    libraryDependencies ++= List(
      "org.scala-lang" % "scala-compiler" % scalaVersion.value
    )
  )
