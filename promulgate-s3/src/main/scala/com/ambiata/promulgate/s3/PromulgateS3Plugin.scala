package com.ambiata.promulgate.s3

import sbt._, Keys._
import sbtassembly.Plugin._, AssemblyKeys._
import com.typesafe.sbt.S3Plugin._
import ohnosequences.sbt.SbtS3Resolver._
import com.amazonaws.services.s3.model.Region

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
    S3DistKeys.path             :=   "",
    credentials                 +=   Credentials(Path.userHome / ".s3credentials"),
    S3.progress in S3.upload    :=   false,
    S3.host in S3.upload        <<=  S3DistKeys.bucket.apply(bucket => s"${bucket}.s3.amazonaws.com"),
    mappings in S3.upload       <<=  (S3DistKeys.path, assembly, name, version).map((p, a, n, v) =>
      Seq((a, s"${p}${n}/${v}/${n}-${v}.jar")))
  )

  def promulgateS3LibSettings: Seq[Sett] = Seq(
    S3LibKeys.region            := Region.AP_Sydney,
    publishMavenStyle           := false,
    publishArtifact in Test     := false,
    pomIncludeRepository        := { _ => false },
    publishTo                   <<= (S3LibKeys.bucket, s3credentials, S3LibKeys.region).apply((bucket, creds, region) =>
      Some(S3Resolver(creds, false, region)("promulgate-s3-publish", s3(bucket)).withIvyPatterns))
  )
}
