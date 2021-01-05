package net.seichi915.seichi915chat.listener

import net.md_5.bungee.api.event.ServerDisconnectEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler
import net.seichi915.seichi915chat.Seichi915Chat
import net.seichi915.seichi915chat.antispam.AntiSpam

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

class ServerDisconnectListener extends Listener {
  @EventHandler
  def onServerDisconnect(event: ServerDisconnectEvent): Unit = {
    if (event.getPlayer.isConnected) return
    AntiSpam.reset(event.getPlayer)
    Seichi915Chat.playerDataMap
      .getOrElse(event.getPlayer, {
        Seichi915Chat.instance.getLogger
          .warning(s"${event.getPlayer}さんのプレイヤーデータが見つかりませんでした。")
        return
      })
      .save(event.getPlayer) onComplete {
      case Success(_) =>
        Seichi915Chat.playerDataMap.remove(event.getPlayer)
      case Failure(exception) =>
        exception.printStackTrace()
        Seichi915Chat.instance.getLogger
          .warning(s"${event.getPlayer}さんのプレイヤーデータのセーブに失敗しました。")
        Seichi915Chat.playerDataMap.remove(event.getPlayer)
    }
  }
}
