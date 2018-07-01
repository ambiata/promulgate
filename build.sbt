import com.amazonaws.services.s3.model.Region
import com.amazonaws.auth.profile.ProfileCredentialsProvider
import com.amazonaws.auth.{EnvironmentVariableCredentialsProvider, InstanceProfileCredentialsProvider}
import sbt.Def
import ohnosequences.sbt.SbtS3Resolver.autoImport.{s3 => ss33, _}

lazy val ossBucket: String =
  sys.env.getOrElse("AMBIATA_IVY_OSS", "ambiata-oss")

lazy val standardSettings: Seq[Def.Setting[_]] =
  Defaults.coreDefaultSettings ++ promulgateSettings

lazy val promulgateSettings: Seq[Def.Setting[_]] = Seq(
    organization := "com.ambiata"
  , sbtPlugin := true
  , version in ThisBuild := "0.12.0"
  , scalaVersion := "2.10.7"
  , scalacOptions := Seq(
    "-deprecation"
    , "-unchecked"
    , "-optimise"
//    , "-Ywarn-all"
    , "-Xlint"
    , "-Xfatal-warnings"
    , "-feature"
    , "-language:_"
  )
  , publishMavenStyle := false
  , publishArtifact in Test := false
  , pomIncludeRepository := { _ => false }
  , awsProfile := "default"
  , s3region := Region.AP_Sydney
  , s3overwrite := false
  , s3acl := com.amazonaws.services.s3.model.CannedAccessControlList.BucketOwnerFullControl
  , s3sse := false
  , s3credentials := new EnvironmentVariableCredentialsProvider() |
    InstanceProfileCredentialsProvider.getInstance() |
    new ProfileCredentialsProvider(awsProfile.value)
  , publishTo := Some(s3resolver.value("ambiata-oss-publish", ss33(ossBucket)).withIvyPatterns)
  )

lazy val promulgate = (project in file("."))
  .settings(
    name := "promulgate"
    , standardSettings
    , version in ThisBuild := "0.12.0"
    , VersionPlugin.uniqueVersionSettings
  )
  .aggregate(source, info, notify_, assembly, s3, version_, project_)
  .dependsOn(source)
  .dependsOn(info)
  .dependsOn(notify_)
  .dependsOn(assembly)
  .dependsOn(s3)
  .dependsOn(version_)
  .dependsOn(project_)

lazy val source = (project in file("promulgate-source"))
  .settings(
    standardSettings
    , name := "promulgate-source"
  )

lazy val info = (project in file("promulgate-info"))
  .settings(
    standardSettings
    , name := "promulgate-info"
  ).dependsOn(version_)


lazy val assembly = (project in file("promulgate-assembly"))
  .settings(
    standardSettings
    , name := "promulgate-assembly"
    , addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.11.2")
  )

lazy val s3 = (project in file("promulgate-s3"))
  .settings(
  standardSettings
  , name := "promulgate-s3"
  , resolvers += "Era7 maven releases" at "http://releases.era7.com.s3.amazonaws.com"
  , libraryDependencies ++= Seq("joda-time" % "joda-time" % "2.2")
  , resolvers += Resolver.url("sbts3 ivy resolver", url("https://dl.bintray.com/emersonloureiro/sbt-plugins"))(Resolver.ivyStylePatterns)
  , addSbtPlugin(("cf.janga" % "sbts3" % "0.10") exclude("joda-time", "joda-time"))
  , addSbtPlugin(("ohnosequences" % "sbt-s3-resolver" % "0.16.0") exclude("joda-time", "joda-time"))
).dependsOn(assembly)

lazy val notify_ = (project in file("promulgate-notify"))
  .settings(
    standardSettings
    , name := "promulgate-notify"
    , libraryDependencies := Seq("net.databinder.dispatch" %% "dispatch-core" % "0.11.0")
  )

lazy val version_ = (project in file("promulgate-version"))
  .settings(
    standardSettings
    , name := "promulgate-version"
  )

lazy val project_ = (project in file("promulgate-project"))
  .settings(
    standardSettings
    , name := "promulgate-project"
  ).dependsOn(source)
  .dependsOn(info)
  .dependsOn(notify_)
  .dependsOn(assembly)
  .dependsOn(s3)
  .dependsOn(version_)
