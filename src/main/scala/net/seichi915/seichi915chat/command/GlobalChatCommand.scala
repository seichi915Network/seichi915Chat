package net.seichi915.seichi915chat.command

import cats.effect.IO
import com.google.common.io.ByteStreams
import net.md_5.bungee.api.{ChatColor, CommandSender}
import net.md_5.bungee.api.chat.hover.content.Text
import net.md_5.bungee.api.chat.{
  BaseComponent,
  ClickEvent,
  ComponentBuilder,
  HoverEvent,
  TextComponent
}
import net.md_5.bungee.api.config.ServerInfo
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.plugin.{Command, TabExecutor}
import net.seichi915.seichi915chat.Seichi915Chat
import net.seichi915.seichi915chat.antispam.AntiSpam
import net.seichi915.seichi915chat.converter.ChatConverter
import net.seichi915.seichi915chat.util.Implicits._
import net.seichi915.seichi915chat.util.Util

import java.lang
import java.util.Collections
import scala.concurrent.ExecutionContext
import scala.jdk.CollectionConverters._

class GlobalChatCommand
    extends Command("globalchat", null, "gchat")
    with TabExecutor {
  @SuppressWarnings(Array("UnstableApiUsage"))
  override def execute(sender: CommandSender, args: Array[String]): Unit = {
    if (!sender.isPlayer) {
      sender.sendMessage(
        TextComponent
          .fromLegacyText("このコマンドはプレイヤーのみが実行できます。".toErrorMessage): _*)
      return
    }
    val player = sender.asInstanceOf[ProxiedPlayer]
    if (!(args.length >= 1)) {
      player.sendMessage(
        TextComponent.fromLegacyText("コマンドの使用法が間違っています。".toErrorMessage): _*)
      return
    }
    val playerData = Seichi915Chat.playerDataMap.getOrElse(player, {
      player.disconnect(
        TextComponent.fromLegacyText("プレイヤーデータが見つかりませんでした。".toErrorMessage): _*)
      return
    })
    if (!AntiSpam.canSpeak(player)) {
      player.sendMessage(TextComponent.fromLegacyText(
        s"発言の感覚が短すぎます。${AntiSpam.getSpeakIntervalTimerRemaining(player).getOrElse(0)}秒後に再度発言できます。".toErrorMessage): _*)
      return
    }
    val optimizedMessage = args.mkString(" ").optimize
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
        var processedText = ""
        if (optimizedMessage.charAt(0) != '#' && playerData.isJapaneseConversionEnabled && !containsJapanese && !isURL) {
          val converted = ChatConverter.convert(optimizedMessage)
          processedText =
            Util.createGlobalChatMessage(player, converted, optimizedMessage)
          processed = new ComponentBuilder(processedText).create()
        } else {
          if (isURL) {
            val componentBuilder =
              new ComponentBuilder(Util.createGlobalChatPrefix(player))
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
            processedText =
              s"${Util.createGlobalChatPrefix(player)}${ChatColor.YELLOW}${ChatColor.UNDERLINE}$optimizedMessage"
          } else {
            processedText = Util.createGlobalChatMessage(
              player,
              ChatColor.translateAlternateColorCodes(
                '&',
                if (optimizedMessage.startsWith("#"))
                  optimizedMessage.replaceFirst("#", "")
                else optimizedMessage))
            processed = new ComponentBuilder(processedText).create()
          }
        }
        val byteArrayDataOutput = ByteStreams.newDataOutput()
        byteArrayDataOutput.writeUTF("Seichi915BungeeLogger-INFO")
        byteArrayDataOutput.writeUTF(processedText)
        Seichi915Chat.instance.getProxy.getServers.asScala.foreach {
          case (_: String, serverInfo: ServerInfo) =>
            serverInfo.sendData("BungeeCord", byteArrayDataOutput.toByteArray)
        }
        Seichi915Chat.instance.getProxy.getPlayers.asScala.foreach {
          proxiedPlayer =>
            val destinationPlayerData =
              Seichi915Chat.playerDataMap.getOrElse(proxiedPlayer, {
                proxiedPlayer.disconnect(
                  TextComponent.fromLegacyText(
                    "プレイヤーデータが見つかりませんでした。".toErrorMessage): _*)
                return
              })
            if (destinationPlayerData.getBlockingUUIDList.contains(
                  player.getUniqueId)) {
              val componentBuilder =
                new ComponentBuilder(Util.createGlobalChatPrefix(player))
                  .append(
                    s"${ChatColor.GRAY}${ChatColor.ITALIC}ブロック中(メッセージホバーで内容を表示)")
                  .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                        new Text(processedText)))
              proxiedPlayer.sendMessage(componentBuilder.create(): _*)
            } else proxiedPlayer.sendMessage(processed: _*)
        }
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

  override def onTabComplete(sender: CommandSender,
                             args: Array[String]): lang.Iterable[String] =
    Collections.emptyList()
}
