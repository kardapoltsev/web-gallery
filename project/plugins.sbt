resolvers ++= Seq(
  "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/",
  "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
  "Scala sbt" at "http://scalasbt.artifactoryonline.com/scalasbt",
  "Typesafe Snaphot Repository" at "http://repo.typesafe.com/typesafe/snapshots/",
  "spray repo" at "http://repo.spray.io",
  "sbt-plugin-releases" at "http://scalasbt.artifactoryonline.com/scalasbt/sbt-plugin-releases/")


addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "0.6.3")

addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "0.3.3-SNAPSHOT")

addSbtPlugin("io.spray" % "sbt-twirl" % "0.7.0")

addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.1.6")
