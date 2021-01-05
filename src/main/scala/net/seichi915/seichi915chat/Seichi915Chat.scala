package net.seichi915.seichi915chat

import java.util.concurrent.TimeUnit

import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.plugin.Plugin
import net.seichi915.seichi915chat.antispam.AntiSpam
import net.seichi915.seichi915chat.command._
import net.seichi915.seichi915chat.database.Database
import net.seichi915.seichi915chat.listener._
import net.seichi915.seichi915chat.playerdata.PlayerData
import net.seichi915.seichi915chat.task._

import scala.collection.mutable

object Seichi915Chat {
  var instance: Seichi915Chat = _

  var playerDataMap: mutable.HashMap[ProxiedPlayer, PlayerData] =
    mutable.HashMap()
}

class Seichi915Chat extends Plugin {
  Seichi915Chat.instance = this

  override def onEnable(): Unit = {
    if (!Database.saveDefaultDatabase) {
      getLogger.severe("デフォルトのデータベースファイルのコピーに失敗しました。プロキシを停止します。")
      getProxy.stop()
      return
    }
    Seq(
      new ChatListener,
      new PostLoginListener,
      new ServerDisconnectListener
    ).foreach(getProxy.getPluginManager.registerListener(this, _))
    Map(
      (300, 300) -> new PlayerDataSaveTask,
      (1, 1) -> new AntiSpam.Timer
    ).foreach {
      case ((delay: Int, period: Int), runnable: Runnable) =>
        getProxy.getScheduler.schedule(this,
                                       runnable,
                                       delay,
                                       period,
                                       TimeUnit.SECONDS)
    }
    Seq(
      new BlockCommand,
      new GlobalChatCommand,
      new GlobalMsgCommand,
      new JpCommand,
      new MsgCommand,
      new UnblockCommand
    ).foreach(getProxy.getPluginManager.registerCommand(this, _))

    getLogger.info("seichi915Chatが有効になりました。")
  }

  override def onDisable(): Unit = {
    getLogger.info("seichi915Chatが無効になりました。")
  }
}
