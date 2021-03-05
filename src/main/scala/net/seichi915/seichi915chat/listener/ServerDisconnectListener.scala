package net.seichi915.seichi915chat.listener

import cats.effect.IO
import net.md_5.bungee.api.event.ServerDisconnectEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler
import net.seichi915.seichi915chat.Seichi915Chat
import net.seichi915.seichi915chat.antispam.AntiSpam

import scala.concurrent.ExecutionContext

class ServerDisconnectListener extends Listener {
  @EventHandler
  def onServerDisconnect(event: ServerDisconnectEvent): Unit = {
    if (event.getPlayer.isConnected) return
    AntiSpam.reset(event.getPlayer)
    val task = IO {
      try Seichi915Chat.playerDataMap
        .getOrElse(event.getPlayer, {
          Seichi915Chat.instance.getLogger
            .warning(s"${event.getPlayer}さんのプレイヤーデータが見つかりませんでした。")
          return
        })
        .save(event.getPlayer)
      catch {
        case e: Exception =>
          e.printStackTrace()
          Seichi915Chat.instance.getLogger
            .warning(s"${event.getPlayer}さんのプレイヤーデータのセーブに失敗しました。")
      } finally Seichi915Chat.playerDataMap.remove(event.getPlayer)
    }
    val contextShift = IO.contextShift(ExecutionContext.global)
    IO.shift(contextShift).flatMap(_ => task).unsafeRunAsyncAndForget()
  }
}
