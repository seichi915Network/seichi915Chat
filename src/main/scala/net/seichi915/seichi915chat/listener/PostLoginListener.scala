package net.seichi915.seichi915chat.listener

import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.event.PostLoginEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler
import net.seichi915.seichi915chat.Seichi915Chat
import net.seichi915.seichi915chat.util.Implicits._

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

class PostLoginListener extends Listener {
  @EventHandler
  def onPostLogin(event: PostLoginEvent): Unit =
    event.getPlayer.getPlayerData onComplete {
      case Success(value) =>
        if (value.isEmpty) {
          Seichi915Chat.instance.getLogger
            .info(s"${event.getPlayer.getName}さんのプレイヤーデータが見つかりませんでした。作成します。")
          event.getPlayer.createNewPlayerData onComplete {
            case Success(_) =>
              onPostLogin(event)
            case Failure(exception) =>
              exception.printStackTrace()
              event.getPlayer.disconnect(
                TextComponent.fromLegacyText(
                  "プレイヤーデータの作成に失敗しました。".toErrorMessage): _*)
          }
        } else
          Seichi915Chat.playerDataMap += event.getPlayer -> value.get
      case Failure(exception) =>
        exception.printStackTrace()
        event.getPlayer.disconnect(
          TextComponent
            .fromLegacyText("プレイヤーデータの読み込みに失敗しました。".toErrorMessage): _*)
    }
}
