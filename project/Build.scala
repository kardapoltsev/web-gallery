import sbt._
import Keys._


object ApplicationBuild extends Build {
  import Versions._

  val appName       = "webgallery"
  val isSnapshot = true
  val version = "1.0.0" + (if (isSnapshot) "-SNAPSHOT" else "")



  val resolvers = Seq(
    "Typesafe releases" at "http://repo.typesafe.com/typesafe/releases/",
    "Typesafe Maven" at "http://repo.typesafe.com/typesafe/maven-releases",
    "Maven central" at "http://oss.sonatype.org/content/repositories/releases",
    "spray" at "http://repo.spray.io/",
    "spray nightlies repo" at "http://nightlies.spray.io/",
    "The New Motion Repository" at "http://nexus.thenewmotion.com/content/repositories/releases-public"
  )

  import scoverage.ScoverageSbtPlugin._

  val scoverageSettings = Seq(
    ScoverageKeys.coverageExcludedPackages := "com.github.kardapoltsev.webgallery.http.html.*",
    ScoverageKeys.coverageHighlighting := true
  )


  import twirl.sbt.TwirlPlugin._
  import com.typesafe.sbt.packager.Keys._
  import com.typesafe.sbt.packager.linux.Mapper._
  import com.typesafe.sbt.SbtNativePackager._
  import com.typesafe.sbt.packager.archetypes.ServerLoader.{SystemV}

  val nativePackSettings = Seq(
    packageDescription in Debian := "Photos gallery",
    packageArchitecture in Debian := "amd64",
    maintainer in Debian := "Alexey Kardapoltsev <alexey.kardapoltsev@gmail.com>",
    serverLoading in Debian := SystemV,
    debianPackageDependencies in Debian ++= Seq("postgresql (>= 9.1)"),
    //add application
    mappings in Universal <++= baseDirectory map { base =>
      val dir = base / "web" / "app-build"
      (dir.***) pair relativeTo(base)
    },
    //add static resources
    mappings in Universal <++= baseDirectory map { base =>
      val dir = base / "web" / "static"
      (dir.***) pair relativeTo(base)
    },
    linuxPackageMappings in Debian <+= (target in Compile, daemonUser in Linux, daemonGroup in Linux) map { (target, user, group) =>
      val f = target / "var" / "lib" / appName
      f.mkdirs()
      packageMapping(f -> s"/var/lib/$appName").withUser(user).withGroup(group)
    }
  )


  import com.typesafe.sbt.SbtScalariform.{ scalariformSettings, ScalariformKeys }
  import scalariform.formatter.preferences._

  ScalariformKeys.preferences := ScalariformKeys.preferences.value
      .setPreference(IndentWithTabs, false)
      .setPreference(PreserveDanglingCloseParenthesis, true)

  val skipJavaDocSettings = Seq(
    //skip scala doc generation
    publishArtifact in (Compile, packageDoc) := false,
    publishArtifact in packageDoc := false,
    sources in (Compile, doc) := Seq.empty,
    publishArtifact in (Compile, packageSrc) := false,
    publishArtifact in packageSrc := false
  )


  val buildSettings = skipJavaDocSettings ++ Twirl.settings ++ nativePackSettings ++ scalariformSettings ++
                      scoverageSettings ++ Seq (
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
    updateOptions := updateOptions.value.withCachedResolution(true),
    retrieveManaged := true,
    parallelExecution in Test := false,
//    fork in Test := true,
    Keys.externalResolvers := Resolver.withDefaultResolvers(resolvers)
  )

  val appDependencies = Seq(
    "com.typesafe.akka"     %% "akka-actor"                     % AkkaVersion,
    "com.typesafe.akka"     %% "akka-slf4j"                     % AkkaVersion,
    "com.typesafe.akka"     %% "akka-testkit"                   % AkkaVersion                 % "test",
    "org.scalatest"         %% "scalatest"                      % ScalaTestVersion            % "test",
    "ch.qos.logback"        %  "logback-classic"                % LogbackVersion,
    "io.spray"              %% "spray-json"                     % SprayJson,
    "io.spray"              %%  "spray-can"                     % SprayVersion,
    "io.spray"              %%  "spray-routing"                 % SprayVersion,
    "io.spray"              %%  "spray-caching"                 % SprayVersion,
    "io.spray"              %%  "spray-client"                  % SprayVersion,
    "io.spray"              %%  "spray-testkit"                 % SprayVersion                % "test",
    //image processing
    "com.drewnoakes"        %  "metadata-extractor"             % MetadataExtractorVersion,
    "commons-io"            %  "commons-io"                     % CommonsIoVersion,
    "com.mortennobel"       %  "java-image-scaling"             % "0.8.6",

    "org.mindrot"           %  "jbcrypt" % "0.3m",
    //database
    "postgresql"            %  "postgresql"                     % "9.1-901-1.jdbc4",
    "org.scalikejdbc"       %% "scalikejdbc"                    % ScalikejdbcVersion,
    "org.scalikejdbc"       %% "scalikejdbc-config"             % ScalikejdbcVersion,
    "org.scalikejdbc"       %% "scalikejdbc-test"               % ScalikejdbcVersion          % "test"
  )


  import spray.revolver.RevolverPlugin._


  import com.typesafe.sbt.packager.debian.JDebPackaging
  import com.typesafe.sbt.packager.archetypes.JavaServerAppPackaging
  val main = Project(
    appName,
    file(".")).enablePlugins(JavaServerAppPackaging, JDebPackaging).settings(buildSettings ++ Revolver.settings ++ Seq(
      mainClass := Some("com.github.kardapoltsev.webgallery.Server"),
      libraryDependencies ++= appDependencies):_*
      ) 


}

object Versions {
  val ScalikejdbcVersion = "2.2.7"
  val CommonsIoVersion = "2.4"
  val MetadataExtractorVersion = "2.8.1"
  val LogbackVersion = "1.1.3"
  val scalaVer = "2.11.7"
  val AkkaVersion = "2.3.12"
  val SprayJson = "1.3.2"
  val SprayVersion = "1.3.3"
  val ScalaTestVersion = "2.2.4"
}
