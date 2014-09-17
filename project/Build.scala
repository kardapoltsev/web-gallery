import sbt._
import Keys._


object ApplicationBuild extends Build {
  import Versions._

  val appName       = "web-gallery"
  val isSnapshot = true
  val version = "1.0.0" + (if (isSnapshot) "-SNAPSHOT" else "")

  import com.typesafe.sbt.SbtNativePackager._
  import com.typesafe.sbt.packager.Keys._


  val nativePackSetting = packagerSettings ++ packageArchetype.java_server ++ Seq(
    maintainer := "Alexey Kardapoltsev <alexey.kardapoltsev@frumatic.com>",
    packageSummary := "spray tests",
    packageDescription := "spray server tests"
  )


  val resolvers = Seq(
    "Typesafe releases" at "http://repo.typesafe.com/typesafe/releases/",
    "Typesafe Maven" at "http://repo.typesafe.com/typesafe/maven-releases",
    "Maven central" at "http://oss.sonatype.org/content/repositories/releases",
    "spray" at "http://repo.spray.io/",
    "spray nightlies repo" at "http://nightlies.spray.io/",
    "The New Motion Repository" at "http://nexus.thenewmotion.com/content/repositories/releases-public"
  )

  import scoverage.ScoverageSbtPlugin._

  val scoverageSettings = instrumentSettings ++ Seq(
    parallelExecution in ScoverageTest := false,
    ScoverageKeys.highlighting := true
  )

  import scalikejdbc.mapper.SbtPlugin.scalikejdbcSettings
  import twirl.sbt.TwirlPlugin._

  val buildSettings = Twirl.settings ++ nativePackSetting ++ scalikejdbcSettings ++ scoverageSettings ++ Seq (
    organization := "self.edu",
    Keys.version := version,
    scalaVersion := scalaVer,
    scalacOptions ++= Seq(
      "-feature",
//      "-Xlog-implicits",
      "-language:postfixOps",
      "-deprecation"),
    scalacOptions <++= scalaVersion map { v =>
      if(v.startsWith("2.10"))
        Seq.empty
      else Seq("-Ydelambdafy:method")
    },
    incOptions := incOptions.value.withNameHashing(true),
    retrieveManaged := true,
    parallelExecution in Test := false,
    testOptions in Test := Nil,
    Keys.externalResolvers := Resolver.withDefaultResolvers(resolvers)
  )

  val appDependencies = Seq(
    "com.typesafe.akka"     %% "akka-actor"              % AkkaVersion,
    "com.typesafe.akka"     %% "akka-slf4j"              % AkkaVersion,
    "com.typesafe.akka"     %% "akka-testkit"            % AkkaVersion      % "test",
    "org.scalatest"         %% "scalatest"               % ScalaTestVersion % "test",
    "ch.qos.logback"        %  "logback-classic"         % LogbackVersion,
    "io.spray"              %% "spray-json"              % SprayJson,
    "io.spray"              %%  "spray-can"               % SprayVersion,
    "io.spray"              %%  "spray-routing"           % SprayVersion,
    "io.spray"              %%  "spray-caching"           % SprayVersion,
    "io.spray"              %%  "spray-client"            % SprayVersion,
    "io.spray"              %%  "spray-testkit"           % SprayVersion     % "test",
    //image processing
    "com.drewnoakes"        %  "metadata-extractor"      % MetadataExtractorVersion,
    "commons-io"            %  "commons-io"              % CommonsIoVersion,
    "com.mortennobel"       %  "java-image-scaling"      % "0.8.5",

    "org.mindrot" % "jbcrypt" % "0.3m",
    //database
    "postgresql"            %  "postgresql"              % "9.1-901-1.jdbc4",
    "org.scalikejdbc" %% "scalikejdbc"         % ScalikejdbcVersion,
    "org.scalikejdbc" %% "scalikejdbc-config"  % ScalikejdbcVersion,
    "org.scalikejdbc" %% "scalikejdbc-test"    % ScalikejdbcVersion  % "test"
  )


  import spray.revolver.RevolverPlugin._


  val main = Project(
    appName,
    file("."),
    settings = buildSettings ++ Revolver.settings ++ Seq(
      mainClass := Some("com.github.kardapoltsev.webgallery.Server"),
      libraryDependencies ++= appDependencies)
  )
}

object Versions {
  val ScalikejdbcVersion = "2.1.1"
  val CommonsIoVersion = "2.4"
  val MetadataExtractorVersion = "2.6.2"
  val LogbackVersion = "1.1.2"
  val scalaVer = "2.11.2"
  val AkkaVersion = "2.3.5"
  val SprayJson = "1.2.6"
  val SprayVersion = "1.3.1"
  val ScalaTestVersion = "2.2.2"
}
