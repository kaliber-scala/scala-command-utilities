releaseSettings

organization in ThisBuild := "net.kaliber"

scalaVersion in ThisBuild := "2.11.6"

crossScalaVersions in ThisBuild := Seq(scalaVersion.value)

lazy val root = project.in( file(".") )
  .settings(
    name := "scala-command-utilities",
    publishArtifact := false
  )
  // The release requires this setting to be present on root
  .settings(publishSettings: _*)
  .aggregate(core, play)

lazy val core = project.in( file("core") )
  .settings(
    name := "scala-command-utilities-core"
  )
  .settings(publishSettings: _*)
  .settings(testSettings: _*)

lazy val play = project.in( file("play") )
  .settings(
    name := "scala-command-utilities-play"
  )
  .settings(publishSettings: _*)
  .settings(playSettings: _*)
  .settings(testSettings: _*)
  .dependsOn(core % "compile->compile;test->test")

lazy val publishSettings = Seq(
  publishTo := {
    val repo = if (version.value endsWith "SNAPSHOT") "snapshot" else "release"
    Some("Rhinofly Internal " + repo.capitalize + " Repository" at
      "http://maven-repository.rhinofly.net:8081/artifactory/libs-" + repo + "-local")
  }
)

lazy val testSettings = Seq(
  testFrameworks += new TestFramework("org.qirx.littlespec.sbt.TestFramework"),
  libraryDependencies ++= Seq(
    "org.qirx" %% "little-spec" % "0.4" % "test",
    "org.qirx" %% "little-spec-extra-documentation" % "0.4" % "test"
  ),
  testOptions ++= Seq(
    Tests.Argument("reporter", "org.qirx.littlespec.reporter.MarkdownReporter"),
    Tests.Argument("documentationTarget", ((baseDirectory).value / "documentation").getAbsolutePath)
  )
)

lazy val playSettings = {
  val playVersion = "2.4.1"
  libraryDependencies ++= Seq(
    "com.typesafe.play" %% "play" % playVersion % "provided",
    "com.typesafe.play" %% "play-json" % playVersion % "test",
    "com.typesafe.play" %% "play-test" % playVersion % "test"
  )
}
