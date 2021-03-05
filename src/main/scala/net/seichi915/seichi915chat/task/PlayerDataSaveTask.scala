package net.seichi915.seichi915chat.task

import cats.effect.IO
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.seichi915.seichi915chat.Seichi915Chat
import net.seichi915.seichi915chat.playerdata.PlayerData

import scala.concurrent.ExecutionContext

class PlayerDataSaveTask extends Runnable {
  override def run(): Unit =
    Seichi915Chat.playerDataMap.foreach {
      case (proxiedPlayer: ProxiedPlayer, playerData: PlayerData) =>
        val task = IO {
          try playerData.save(proxiedPlayer)
          catch {
            case e: Exception =>
              e.printStackTrace()
              Seichi915Chat.instance.getLogger
                .warning(s"${proxiedPlayer.getName}さんのプレイヤーデーターのセーブに失敗しました。")
          }
        }
        val contextShift = IO.contextShift(ExecutionContext.global)
        IO.shift(contextShift).flatMap(_ => task).unsafeRunAsyncAndForget()
    }
}
