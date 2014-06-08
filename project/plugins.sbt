resolvers ++= Seq(
  "Sonatype snapshots" at "http://oss.sonatype.org/content/repositories/snapshots/",
  "Typesafe Repository" at "http://repo.typesafe.com/typesafe/releases/",
  "Scala sbt" at "http://scalasbt.artifactoryonline.com/scalasbt",
  "Typesafe Snaphot Repository" at "http://repo.typesafe.com/typesafe/snapshots/",
  "spray repo" at "http://repo.spray.io",
  "sbt-plugin-releases" at "http://scalasbt.artifactoryonline.com/scalasbt/sbt-plugin-releases/")


addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "0.6.3")

addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "0.4.0")

addSbtPlugin("com.timushev.sbt" % "sbt-updates" % "0.1.6")

addSbtPlugin("io.spray" % "sbt-revolver" % "0.7.2")

addSbtPlugin("org.scoverage" %% "sbt-scoverage" % "0.99.5.1")

libraryDependencies += "postgresql" % "postgresql" % "9.1-901-1.jdbc4"

addSbtPlugin("org.scalikejdbc" %% "scalikejdbc-mapper-generator" % "2.0.1")
