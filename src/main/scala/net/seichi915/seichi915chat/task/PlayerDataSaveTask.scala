package net.seichi915.seichi915chat.task

import net.md_5.bungee.api.connection.ProxiedPlayer
import net.seichi915.seichi915chat.Seichi915Chat
import net.seichi915.seichi915chat.playerdata.PlayerData

import scala.concurrent.ExecutionContext.Implicits.global
import scala.util.{Failure, Success}

class PlayerDataSaveTask extends Runnable {
  override def run(): Unit =
    Seichi915Chat.playerDataMap.foreach {
      case (proxiedPlayer: ProxiedPlayer, playerData: PlayerData) =>
        playerData.save(proxiedPlayer) onComplete {
          case Success(_) =>
          case Failure(exception) =>
            exception.printStackTrace()
            Seichi915Chat.instance.getLogger
              .warning(s"${proxiedPlayer.getName}さんのプレイヤーデーターのセーブに失敗しました。")
        }
    }
}
