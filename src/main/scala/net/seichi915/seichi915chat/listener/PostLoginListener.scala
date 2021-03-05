package net.seichi915.seichi915chat.listener

import cats.effect.IO
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.event.PostLoginEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler
import net.seichi915.seichi915chat.Seichi915Chat
import net.seichi915.seichi915chat.database.Database
import net.seichi915.seichi915chat.util.Implicits._

import scala.concurrent.ExecutionContext

class PostLoginListener extends Listener {
  @EventHandler
  def onPostLogin(event: PostLoginEvent): Unit = {
    val task = IO {
      try {
        Database.getPlayerData(event.getPlayer) match {
          case Some(playerData) =>
            Seichi915Chat.playerDataMap += event.getPlayer -> playerData
          case None =>
            Seichi915Chat.instance.getLogger
              .info(s"${event.getPlayer.getName}さんのプレイヤーデータが見つかりませんでした。作成します。")
            Database.createNewPlayerData(event.getPlayer)
            onPostLogin(event)
        }
      } catch {
        case e: Exception =>
          e.printStackTrace()
          event.getPlayer.disconnect(
            TextComponent
              .fromLegacyText("プレイヤーデータの読み込みに失敗しました。".toErrorMessage): _*)
      }
    }
    val contextShift = IO.contextShift(ExecutionContext.global)
    IO.shift(contextShift).flatMap(_ => task).unsafeRunAsyncAndForget()
  }
}
