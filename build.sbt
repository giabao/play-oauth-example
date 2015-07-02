name := "play-oauth-server"

version := "1.0-SNAPSHORT"

scalaVersion := "2.11.7"

resolvers ++= Seq(
  new MavenRepository("couchbase", "http://files.couchbase.com/maven2/"),
  Resolver.bintrayRepo("scalaz", "releases")
)

libraryDependencies ++= Seq(
  "com.sandinh"             %% "play-oauth"               % "2.0.0-SNAPSHOT",
  "com.sandinh"             %% "couchbase-scala"          % "7.1.0-SNAPSHOT",
  //php compatibility? see - https://github.com/kirill1ku/jBCrypt-sbt/commit/7584c805
  //see also - http://php.net/manual/en/faq.passwords.php
  "de.svenkubiak"           % "jBCrypt"                   % "0.4",
  "com.couchbase.client"    % "java-client"               % "2.2.0-dp",
  "org.scala-lang.modules"  %% "scala-parser-combinators" % "1.0.4",
  "org.scala-lang.modules"  %% "scala-xml"                % "1.0.4",
  "org.scala-lang"          % "scala-reflect"             % "2.11.7",
  specs2 % Test
)

routesGenerator := InjectedRoutesGenerator

//misc - to mute intellij warning when load sbt project
//@see: sbt command> test:whatDependsOn xalan serializer 2.7.1
libraryDependencies += "xalan" % "serializer" % "2.7.2" % "test"
//libraryDependencies += "com.google.guava" % "guava" % "18.0" % "test"

//this will not create target/universal/stage/share when run `play stage`
sources in doc in Compile := Seq()

lazy val root = (project in file(".")).enablePlugins(PlayScala)
