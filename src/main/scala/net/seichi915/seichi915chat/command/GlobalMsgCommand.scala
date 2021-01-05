package net.seichi915.seichi915chat.command

import java.util.Collections
import java.{lang, util}

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
import net.md_5.bungee.api.plugin.{Command, TabExecutor}
import net.seichi915.seichi915chat.Seichi915Chat
import net.seichi915.seichi915chat.antispam.AntiSpam
import net.seichi915.seichi915chat.converter.ChatConverter
import net.seichi915.seichi915chat.util.Implicits._
import net.seichi915.seichi915chat.util.Util
import org.bukkit.util.StringUtil

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.jdk.CollectionConverters._
import scala.util.{Failure, Success}

class GlobalMsgCommand
    extends Command("globalmsg", null, "globaltell", "gmsg", "gtell")
    with TabExecutor {
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
      if (!Seichi915Chat.instance.getProxy.getPlayers.asScala
            .map(_.getDisplayName)
            .toSet
            .contains(args(0))) {
        player.sendMessage(
          TextComponent.fromLegacyText(
            s"${args(0)} さんは現在オンラインではありません。".toErrorMessage): _*)
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
    Future {
      var processed = Array[BaseComponent]()
      if (optimizedMessage.charAt(0) != '#' && playerData.isJapaneseConversionEnabled && !containsJapanese && !isURL) {
        val converted = ChatConverter.convert(optimizedMessage)
        processed = new ComponentBuilder(
          Util.createGlobalPrivateChatMessage(player,
                                              destination,
                                              converted,
                                              optimizedMessage)).create()
      } else {
        if (isURL) {
          val componentBuilder =
            new ComponentBuilder(
              Util.createGlobalPrivateChatPrefix(player, destination))
              .append(
                s"${ChatColor.YELLOW}${ChatColor.UNDERLINE}$optimizedMessage")
              .event(
                new HoverEvent(
                  HoverEvent.Action.SHOW_TEXT,
                  new Text(s"クリックで $optimizedMessage にアクセスします。")
                )
              )
              .event(new ClickEvent(ClickEvent.Action.OPEN_URL,
                                    optimizedMessage))
          processed = componentBuilder.create()
        } else {
          processed = new ComponentBuilder(
            Util.createGlobalPrivateChatMessage(
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
      processed
    } onComplete {
      case Success(value) =>
        player.sendMessage(value: _*)
        destination.sendMessage(value: _*)
        AntiSpam.startSpeakIntervalTimer(player)
        AntiSpam.startSameRemarkIntervalTimer(player, optimizedMessage)
      case Failure(exception) =>
        exception.printStackTrace()
        player.sendMessage(
          TextComponent.fromLegacyText("チャットの処理に失敗しました。".toErrorMessage): _*)
    }
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
      StringUtil.copyPartialMatches(
        args(0),
        Seichi915Chat.instance.getProxy.getPlayers.asScala
          .filterNot(proxiedPlayer =>
            player.getName.equalsIgnoreCase(proxiedPlayer.getName))
          .filterNot(proxiedPlayer =>
            playerData.getBlockingUUIDList.contains(proxiedPlayer.getUniqueId))
          .map(_.getName)
          .asJava,
        completions
      )
      Collections.sort(completions)
      completions
    } else Collections.emptyList()
}
