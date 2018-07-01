package com.ambiata.promulgate.notify

import sbt._, Keys._
import com.ambiata.promulgate.notify._

object NotifyPlugin extends Plugin {

  object NotifyKeys {
    lazy val echoversion = TaskKey[Unit]("echo-version")
    lazy val token = SettingKey[String]("hipchat-token")
    lazy val room = SettingKey[String]("hipchat-room")
  }

  import NotifyKeys._

  def promulgateNotifySettings =
    infer

  def promulgateNotifyHipchatSettings = Seq[Sett](
    echoversion := HipChat.version(name.value, token.value, room.value, (version in ThisBuild).value)
  )

  def infer: Seq[Sett] = (for {
    token <- Option(System.getenv("HIPCHAT_TOKEN"))
    room <- Option(System.getenv("HIPCHAT_ROOM"))
  } yield Seq(
    NotifyKeys.token := token
    , NotifyKeys.room := room
    , echoversion :=
      HipChat.version(name.value, NotifyKeys.token.value, NotifyKeys.room.value, (version in ThisBuild).value))
    )
    .getOrElse(Seq(echoversion := HipChat.console(name.value, (version in ThisBuild).value)))

}
