import java.io.File
import java.net.InetAddress
import java.text.SimpleDateFormat
import java.util.{Date, TimeZone}

import sbt._, Keys._

object VersionPlugin extends Plugin {
  object VersionKeys {
    lazy val date             = SettingKey[Date]("current build date")
    lazy val user             = SettingKey[String]("current build user")
    lazy val machine          = SettingKey[String]("current build machine")
    lazy val commish          = SettingKey[String]("current git short commit hash")
    lazy val commit           = SettingKey[String]("current git full commit hash")
    lazy val pkg              = SettingKey[String]("primary package of this project")
  }

  import VersionKeys._

  def uniqueVersionSettings = Seq(
    VersionKeys.date in ThisBuild           :=   VersionPlugin.now,
    VersionKeys.user in ThisBuild           :=   VersionPlugin.user,
    VersionKeys.machine in ThisBuild        :=   VersionPlugin.machine,
    VersionKeys.commish in ThisBuild        :=   VersionPlugin.commish(baseDirectory.value),
    VersionKeys.commit in ThisBuild         :=   VersionPlugin.commit(baseDirectory.value),
    version in ThisBuild                    := 
      s"${(version in ThisBuild).value}-${VersionPlugin.timestamp(VersionKeys.date.value)}-${VersionKeys.commish.value}"
  )

  def user =
    Option(System.getProperty("user.name")).getOrElse("<unknown>")

  def machine =
    InetAddress.getLocalHost.getHostName

  def timestamp(instant: Date, format: String = "yyyyMMddHHmmss") = {
    val formatter = new SimpleDateFormat("yyyyMMddHHmmss")
    formatter.setTimeZone(TimeZone.getTimeZone("UTC"))
    formatter.format(instant)
  }

  def commish(root: File) =
    gitlog(root, "%h")

  def commit(root: File) =
    gitlog(root, "%H")

  def gitlog(root: File, format: String) =
    Process(s"git log --pretty=format:${format} -n  1", Some(root)).lines.head

  lazy val now =
    new Date
}
