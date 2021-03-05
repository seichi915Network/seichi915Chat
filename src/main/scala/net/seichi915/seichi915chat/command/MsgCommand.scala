package net.seichi915.seichi915chat.command

import cats.effect.IO
import net.md_5.bungee.api.chat.hover.content.Text
import net.md_5.bungee.api.{ChatColor, CommandSender}
import net.md_5.bungee.api.chat.{
  BaseComponent,
  ClickEvent,
  ComponentBuilder,
  HoverEvent,
  TextComponent
}
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.plugin.Command
import net.seichi915.seichi915chat.Seichi915Chat
import net.seichi915.seichi915chat.antispam.AntiSpam
import net.seichi915.seichi915chat.converter.ChatConverter
import net.seichi915.seichi915chat.util.Implicits._
import net.seichi915.seichi915chat.util.Util

import scala.concurrent.ExecutionContext
import scala.jdk.CollectionConverters._

class MsgCommand extends Command("msg", null, "tell", "w") {
  override def execute(sender: CommandSender, args: Array[String]): Unit = {
    if (!sender.isPlayer) {
      sender.sendMessage(
        TextComponent
          .fromLegacyText("このコマンドはプレイヤーのみが実行できます。".toErrorMessage): _*)
      return
    }
    val player = sender.asInstanceOf[ProxiedPlayer]
    if (!(args.length >= 2)) {
      player.sendMessage(
        TextComponent.fromLegacyText("コマンドの使用法が間違っています。".toErrorMessage): _*)
      return
    }
    if (args(0).equalsIgnoreCase(player.getName)) {
      player.sendMessage(
        TextComponent.fromLegacyText(
          "自分自身にプライベートメッセージを送ることはできません。".toErrorMessage): _*)
      return
    }
    val playerData = Seichi915Chat.playerDataMap.getOrElse(player, {
      player.disconnect(
        TextComponent.fromLegacyText("プレイヤーデータが見つかりませんでした。".toErrorMessage): _*)
      return
    })
    val destination = {
      if (!player.getServer.getInfo.getPlayers.asScala
            .map(_.getDisplayName)
            .toSet
            .contains(args(0))) {
        player.sendMessage(
          TextComponent.fromLegacyText(
            s"${args(0)} さんは現在オンラインではありません。".toErrorMessage): _*)
        player.sendMessage(TextComponent.fromLegacyText(
          "seichi915Network内の別のサーバーにいる場合は、/gtell を使用してください。".toErrorMessage): _*)
        return
      }
      Seichi915Chat.instance.getProxy.getPlayer(args(0))
    }
    val destinationPlayerData =
      Seichi915Chat.playerDataMap.getOrElse(
        destination, {
          destination.disconnect(
            TextComponent
              .fromLegacyText("プレイヤーデータが見つかりませんでした。".toErrorMessage): _*)
          player.sendMessage(
            TextComponent.fromLegacyText(
              "相手のプレイヤーデータが見つかりませんでした。".toErrorMessage): _*)
          return
        }
      )
    if (playerData.getBlockingUUIDList.contains(destination.getUniqueId)) {
      player.sendMessage(
        TextComponent.fromLegacyText(
          s"${args(0)} さんをブロックしているため、ダイレクトメッセージを送信できません。".toErrorMessage): _*)
      return
    }
    if (destinationPlayerData.getBlockingUUIDList.contains(player.getUniqueId)) {
      player.sendMessage(
        TextComponent.fromLegacyText(
          s"${args(0)} さんにブロックされているため、ダイレクトメッセージを送信できません。".toErrorMessage): _*)
      return
    }
    if (!AntiSpam.canSpeak(player)) {
      player.sendMessage(TextComponent.fromLegacyText(
        s"発言の感覚が短すぎます。${AntiSpam.getSpeakIntervalTimerRemaining(player).getOrElse(0)}秒後に再度発言できます。".toErrorMessage): _*)
      return
    }
    val optimizedMessage = args.drop(1).mkString(" ").optimize
    if (!AntiSpam.canSpeak(player, optimizedMessage)) {
      player.sendMessage(
        TextComponent.fromLegacyText(s"同じ発言をする感覚が短すぎます。${AntiSpam
          .getSameRemarkIntervalTimerRemaining(player, optimizedMessage)
          .getOrElse(0)}秒後に再度発言できます。".toErrorMessage): _*)
      return
    }
    val containsJapanese = ChatColor
      .stripColor(optimizedMessage)
      .getBytes
      .length > ChatColor.stripColor(optimizedMessage).length ||
      ChatColor.stripColor(optimizedMessage).matches("[ \\uFF61-\\uFF9F]+")
    val isURL = optimizedMessage.matches(
      "(https?|ftp)(:\\/\\/[-_.!~*\\'()a-zA-Z0-9;\\/?:\\@&=+\\$,%#]+)")
    val task = IO {
      try {
        var processed = Array[BaseComponent]()
        if (optimizedMessage.charAt(0) != '#' && playerData.isJapaneseConversionEnabled && !containsJapanese && !isURL) {
          val converted = ChatConverter.convert(optimizedMessage)
          processed = new ComponentBuilder(
            Util.createPrivateChatMessage(player,
                                          destination,
                                          converted,
                                          optimizedMessage)).create()
        } else {
          if (isURL) {
            val componentBuilder =
              new ComponentBuilder(
                Util.createPrivateChatPrefix(player, destination))
                .append(
                  s"${ChatColor.YELLOW}${ChatColor.UNDERLINE}$optimizedMessage")
                .event(
                  new HoverEvent(
                    HoverEvent.Action.SHOW_TEXT,
                    new Text(s"クリックで $optimizedMessage にアクセスします。")
                  )
                )
                .event(
                  new ClickEvent(ClickEvent.Action.OPEN_URL, optimizedMessage))
            processed = componentBuilder.create()
          } else {
            processed = new ComponentBuilder(
              Util.createPrivateChatMessage(
                player,
                destination,
                ChatColor.translateAlternateColorCodes(
                  '&',
                  if (optimizedMessage.startsWith("#"))
                    optimizedMessage.replaceFirst("#", "")
                  else optimizedMessage)
              )).create()
          }
        }
        player.sendMessage(processed: _*)
        destination.sendMessage(processed: _*)
        AntiSpam.startSpeakIntervalTimer(player)
        AntiSpam.startSameRemarkIntervalTimer(player, optimizedMessage)
      } catch {
        case e: Exception =>
          e.printStackTrace()
          player.sendMessage(
            TextComponent.fromLegacyText("チャットの処理に失敗しました。".toErrorMessage): _*)
      }
    }
    val contextShift = IO.contextShift(ExecutionContext.global)
    IO.shift(contextShift).flatMap(_ => task).unsafeRunAsyncAndForget()
  }
}
