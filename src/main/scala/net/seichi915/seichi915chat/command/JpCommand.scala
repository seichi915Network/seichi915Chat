package net.seichi915.seichi915chat.command

import java.lang
import java.util.Collections

import net.md_5.bungee.api.CommandSender
import net.md_5.bungee.api.chat.TextComponent
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.plugin.{Command, TabExecutor}
import net.seichi915.seichi915chat.Seichi915Chat
import net.seichi915.seichi915chat.util.Implicits._

class JpCommand extends Command("jp") with TabExecutor {
  override def execute(sender: CommandSender, args: Array[String]): Unit = {
    if (!sender.isPlayer) {
      sender.sendMessage(
        TextComponent
          .fromLegacyText("このコマンドはプレイヤーのみが実行できます。".toErrorMessage): _*)
      return
    }
    val player = sender.asInstanceOf[ProxiedPlayer]
    if (args.length != 0) {
      player.sendMessage(
        TextComponent.fromLegacyText("コマンドの使用法が間違っています。".toErrorMessage): _*)
      return
    }
    val playerData = Seichi915Chat.playerDataMap.getOrElse(player, {
      player.disconnect(
        TextComponent.fromLegacyText("プレイヤーデータが見つかりませんでした。".toErrorMessage): _*)
      return
    })
    if (playerData.isJapaneseConversionEnabled) {
      player.sendMessage(
        TextComponent.fromLegacyText("自動日本語変換をオフにしました。".toSuccessMessage): _*)
      playerData.setJapaneseConversionEnabled(false)
    } else {
      player.sendMessage(
        TextComponent.fromLegacyText("自動日本語変換をオンにしました。".toSuccessMessage): _*)
      playerData.setJapaneseConversionEnabled(true)
    }
  }

  override def onTabComplete(sender: CommandSender,
                             args: Array[String]): lang.Iterable[String] =
    Collections.emptyList()
}
