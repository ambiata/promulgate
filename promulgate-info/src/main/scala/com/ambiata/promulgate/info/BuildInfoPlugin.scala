package com.ambiata.promulgate.info

import com.ambiata.promulgate.version.VersionPlugin._
import java.util.Date
import sbt._, Keys._
import com.ambiata.promulgate.version._

object BuildInfoPlugin extends Plugin {
  object BuildInfoKeys {
    lazy val pkg              = SettingKey[String]("primary package of this project")
  }

  import BuildInfoKeys._

  def promulgateBuildInfoSettings = Seq[Sett](
    sourceGenerators in Compile += Def.task {
          val file = (sourceManaged in Compile).value / "BuildInfo.scala"
          IO.write(file, BuildInfoPlugin.scala(BuildInfoKeys.pkg.value, (VersionKeys.user in ThisBuild).value, (VersionKeys.machine in ThisBuild).value, (VersionKeys.date in ThisBuild).value, name.value, (version in ThisBuild).value, (VersionKeys.commit in ThisBuild).value))
          Seq(file)
        }
  )

  def scala(pkg: String, user: String, machine: String, instant: Date, name: String, version: String, commit: String) = {
    val format = "yyyy-MM-dd HH:mm:ss"
    s"""package ${pkg}
       |object BuildInfo {
       |  val user = "${user}"
       |  val machine = "${machine}"
       |  val date = "${timestamp(instant, format)}"
       |  val name = "${name}"
       |  val version = "${version}"
       |  val git = "${commit}"
       |}
       |""".stripMargin
  }

  def properties(user: String, machine: String, instant: Date, name: String, version: String, commit: String) = {
    val format = "yyyy-MM-dd HH:mm:ss"
    s"""user = ${user}
       |machine = ${machine}
       |date = ${timestamp(instant, format)}
       |name = ${name}
       |version = ${version}
       |git = ${commit}
       |""".stripMargin
  }
}
