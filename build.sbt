scalaVersion := "2.10.2"

organization := "nu.rinu"

name := "sdb"

resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"

libraryDependencies ++= Seq(
  "com.typesafe" %% "scalalogging-slf4j" % "1.0.1",
  "com.google.guava" % "guava" % "14.0.1",
  "org.json4s" %% "json4s-native" % "3.2.4",
  "nu.rinu" %% "sutil" % "0.0.9",
  "org.specs2" %% "specs2" % "2.3.7" % "test",
  "org.scalatest" %% "scalatest" % "2.0" % "test",
  "org.xerial" % "sqlite-jdbc" % "3.7.2" % "test",
  "org.apache.derby" % "derby" % "10.10.1.1" % "test",
  "ch.qos.logback" % "logback-classic" % "1.0.0" % "test"
)

publishTo <<= version { (v: String) =>
  val nexus = "https://oss.sonatype.org/"
  if (v.trim.endsWith("SNAPSHOT"))
    Some("snapshots" at nexus + "content/repositories/snapshots")
  else
    Some("releases" at nexus + "service/local/staging/deploy/maven2")
}

licenses := Seq("BSD-style" -> url("http://www.opensource.org/licenses/bsd-license.php"))

homepage := Some(url("https://github.com/rinunu/sdb"))

publishArtifact in Test := false

pomExtra :=
  <parent>
    <groupId>org.sonatype.oss</groupId>
    <artifactId>oss-parent</artifactId>
    <version>7</version>
  </parent>
    <scm>
      <connection>scm:git:git@github.com:rinunu/sdb.git</connection>
      <developerConnection>scm:git:git@github.com:rinunu/sdb.git</developerConnection>
      <url>git@github.com:rinunu/sdb.git</url>
    </scm>
    <developers>
      <developer>
        <id>rinunu</id>
        <name>Rintaro Tsuchihashi</name>
        <url>https://github.com/rinunu</url>
      </developer>
    </developers>

useGpg := true
