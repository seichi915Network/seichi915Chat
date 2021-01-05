package net.seichi915.seichi915chat.command

import java.util.Collections
import java.{lang, util}

import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.plugin.{Command, TabExecutor}
import net.seichi915.seichi915chat.Seichi915Chat
import net.seichi915.seichi915chat.util.Implicits._
import org.bukkit.util.StringUtil

import scala.jdk.CollectionConverters._

class BlockCommand extends Command("block") with TabExecutor {
  override def execute(sender: CommandSender, args: Array[String]): Unit = {
    if (!sender.isPlayer) {
      sender.sendMessage(
        TextComponent
          .fromLegacyText("このコマンドはプレイヤーのみが実行できます。".toErrorMessage): _*)
      return
    }
    val player = sender.asInstanceOf[ProxiedPlayer]
    if (args.length != 1) {
      player.sendMessage(
        TextComponent.fromLegacyText("コマンドの使用法が間違っています。".toErrorMessage): _*)
      return
    }
    if (args(0).equalsIgnoreCase(player.getName)) {
      player.sendMessage(
        TextComponent.fromLegacyText("自分自身をブロックすることはできません。".toErrorMessage): _*)
      return
    }
    val playerData = Seichi915Chat.playerDataMap.getOrElse(player, {
      player.disconnect(
        TextComponent
          .fromLegacyText("プレイヤーデータが見つかりませんでした。".toErrorMessage): _*)
      return
    })
    val destination = {
      if (!Seichi915Chat.instance.getProxy.getPlayers.asScala
            .map(_.getName)
            .toSet
            .contains(args(0))) {
        player.sendMessage(
          TextComponent.fromLegacyText(
            s"${args(0)} さんは現在オンラインではありません。".toErrorMessage): _*)
        return
      }
      Seichi915Chat.instance.getProxy.getPlayer(args(0))
    }
    if (playerData.getBlockingUUIDList.contains(destination.getUniqueId)) {
      player.sendMessage(
        TextComponent.fromLegacyText(
          s"${args(0)} さんを既にブロックしています。".toErrorMessage): _*)
      return
    }
    playerData.setBlockingUUIDList(
      playerData.getBlockingUUIDList.concat(
        Iterable.single(destination.getUniqueId)))
    player.sendMessage(
      TextComponent
        .fromLegacyText(s"${args(0)} さんをブロックしました。".toSuccessMessage): _*)
    player.sendMessage(
      TextComponent.fromLegacyText(
        "解除したい場合は /unblock を使用してください。".toSuccessMessage): _*)
  }

  override def onTabComplete(sender: CommandSender,
                             args: Array[String]): lang.Iterable[String] =
    if (args.length == 1) {
      val player = sender.asInstanceOf[ProxiedPlayer]
      val playerData = Seichi915Chat.playerDataMap.getOrElse(player, {
        player.disconnect(
          TextComponent
            .fromLegacyText("プレイヤーデータが見つかりませんでした。".toErrorMessage): _*)
        return Collections.emptyList()
      })
      val completions = new util.ArrayList[String]()
      val canBeBlocked = Seichi915Chat.instance.getProxy.getPlayers.asScala
        .filterNot(proxiedPlayer =>
          player.getName.equalsIgnoreCase(proxiedPlayer.getName))
        .filterNot(proxiedPlayer =>
          playerData.getBlockingUUIDList.contains(proxiedPlayer.getUniqueId))
        .map(_.getName)
      StringUtil.copyPartialMatches(args(0), canBeBlocked.asJava, completions)
      Collections.sort(completions)
      completions
    } else Collections.emptyList()
}
