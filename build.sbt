ThisBuild / scalaVersion := "2.13.4"
ThisBuild / version := "1.0.1"
ThisBuild / description := "seichi915Network チャットプラグイン"

resolvers ++= Seq(
  "hub.spigotmc.org" at "https://hub.spigotmc.org/nexus/content/repositories/snapshots/",
  "oss.sonatype.org" at "https://oss.sonatype.org/content/repositories/snapshots",
  "maven.elmakers.com" at "https://maven.elmakers.com/repository/",
  "papermc.io" at "https://papermc.io/repo/repository/maven-public/"
)

libraryDependencies ++= Seq(
  "net.md-5" % "bungeecord-api" % "1.16-R0.3",
  "org.scalikejdbc" %% "scalikejdbc" % "3.5.0",
  "org.xerial" % "sqlite-jdbc" % "3.7.2",
  "ch.qos.logback" % "logback-classic" % "1.2.3",
  "commons-lang" % "commons-lang" % "2.6",
  "commons-codec" % "commons-codec" % "1.10",
  "org.scalatest" %% "scalatest" % "3.2.2" % "test"
)

assemblyMergeStrategy in assembly := {
  case PathList("javax", "servlet", _ @ _*) => MergeStrategy.first
  case PathList(ps @ _*) if ps.last endsWith ".properties" => MergeStrategy.first
  case PathList(ps @ _*) if ps.last endsWith ".xml" => MergeStrategy.first
  case PathList(ps @ _*) if ps.last endsWith ".types" => MergeStrategy.first
  case PathList(ps @ _*) if ps.last endsWith ".class" => MergeStrategy.first
  case PathList(ps @ _*) if ps.last endsWith "plugin.yml" => MergeStrategy.first
  case PathList(ps @ _*) if ps.last endsWith "config.yml" => MergeStrategy.first
  case PathList(ps @ _*) if ps.last endsWith "Syntax.java" => MergeStrategy.first
  case "application.conf" => MergeStrategy.concat
  case "unwanted.txt" => MergeStrategy.discard
  case x =>
    val oldStrategy = (assemblyMergeStrategy in assembly).value
    oldStrategy(x)
}

lazy val root = (project in file("."))
  .settings(
    name := "seichi915Chat",
    assemblyOutputPath in assembly := baseDirectory.value / "target" / "build" / s"seichi915Chat-${version.value}.jar"
  )
