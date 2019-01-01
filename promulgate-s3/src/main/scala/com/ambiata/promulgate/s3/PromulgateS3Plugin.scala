package com.ambiata.promulgate.s3

import sbt._
import Keys._
import sbtassembly.Plugin._
import AssemblyKeys._
import com.typesafe.sbt.S3Plugin._
import ohnosequences.sbt.SbtS3Resolver.autoImport._
import com.amazonaws.services.s3.model.Region
import com.amazonaws.auth._
import com.typesafe.sbt.S3Keys
import profile._
import s3._

object PromulgateS3Plugin extends Plugin {
  object S3DistKeys {
    lazy val bucket     = SettingKey[String]("s3 bucket to upload distributions to")
    lazy val path       = SettingKey[String]("s3 path to upload to")
  }

  object S3LibKeys {
    lazy val bucket     = SettingKey[String]("s3 bucket to upload published artefacts to")
    lazy val region     = SettingKey[Region]("s3 region")
  }

  def promulgateS3DistSettings: Seq[Sett] = s3Settings ++ Seq(
    S3DistKeys.path := "",
    credentials += Credentials(Path.userHome / ".s3credentials"),
    S3Keys.s3Progress in S3Keys.s3Upload := false,
    S3Keys.s3Host in S3Keys.s3Upload := s"${S3DistKeys.bucket.value}.s3.amazonaws.com",
    mappings in S3Keys.s3Upload := Seq((assembly.value, s"${S3DistKeys.path.value}${name.value}/${version.value}/${name.value}-${version.value}.jar"))
  )

  def promulgateS3LibSettings: Seq[Sett] = Seq(
    S3LibKeys.region            := Region.AP_Sydney,
    publishMavenStyle           := false,
    publishArtifact in Test     := false,
    pomIncludeRepository        := { _ => false },
    s3resolver := S3Resolver(
      new ProfileCredentialsProvider("default") |
        new EnvironmentVariableCredentialsProvider() |
        InstanceProfileCredentialsProvider.getInstance(),
      false,
      S3LibKeys.region.value,
      com.amazonaws.services.s3.model.CannedAccessControlList.BucketOwnerFullControl,
      s3sse.value
    ),
    publishTo                   := Some(s3resolver.value("promulgate-s3-publish", s3(S3LibKeys.bucket.value)).withIvyPatterns)
  )

}
