package net.seichi915.seichi915chat.listener

import net.md_5.bungee.api.chat.hover.content.Text
import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.chat.{
  BaseComponent,
  ClickEvent,
  ComponentBuilder,
  HoverEvent,
  TextComponent
}
import net.md_5.bungee.api.connection.ProxiedPlayer
import net.md_5.bungee.api.event.ChatEvent
import net.md_5.bungee.api.plugin.Listener
import net.md_5.bungee.event.EventHandler
import net.seichi915.seichi915bungeelogger.Seichi915BungeeLogger
import net.seichi915.seichi915chat.Seichi915Chat
import net.seichi915.seichi915chat.antispam.AntiSpam
import net.seichi915.seichi915chat.converter.ChatConverter
import net.seichi915.seichi915chat.util.Implicits._
import net.seichi915.seichi915chat.util.Util

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.jdk.CollectionConverters._
import scala.util.{Failure, Success}

class ChatListener extends Listener {
  @EventHandler
  def onChat(event: ChatEvent): Unit = {
    if (!event.getSender.isInstanceOf[ProxiedPlayer]) return
    if (event.isCommand || event.isProxyCommand) return
    event.setCancelled(true)
    val player = event.getSender.asInstanceOf[ProxiedPlayer]
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
    val optimizedMessage = event.getMessage.optimize
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
      var processedText = ""
      if (optimizedMessage.charAt(0) != '#' && playerData.isJapaneseConversionEnabled && !containsJapanese && !isURL) {
        val converted = ChatConverter.convert(optimizedMessage)
        processedText =
          Util.createChatMessage(player, converted, optimizedMessage)
        processed = new ComponentBuilder(processedText).create()
      } else {
        if (isURL) {
          val componentBuilder =
            new ComponentBuilder(Util.createChatPrefix(player))
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
          processedText =
            s"${Util.createChatPrefix(player)}${ChatColor.YELLOW}${ChatColor.UNDERLINE}$optimizedMessage"
        } else {
          processedText =
            Util.createChatMessage(player,
                                   ChatColor.translateAlternateColorCodes(
                                     '&',
                                     if (optimizedMessage.startsWith("#"))
                                       optimizedMessage.replaceFirst("#", "")
                                     else optimizedMessage))
          processed = new ComponentBuilder(processedText).create()
        }
      }
      (processed, processedText)
    } onComplete {
      case Success(value) =>
        Seichi915BungeeLogger.getAPI.info(player.getServer, value._2)
        player.getServer.getInfo.getPlayers.asScala.foreach { proxiedPlayer =>
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
              new ComponentBuilder(Util.createChatPrefix(player))
                .append(
                  s"${ChatColor.GRAY}${ChatColor.ITALIC}ブロック中(メッセージホバーで内容を表示)")
                .event(new HoverEvent(HoverEvent.Action.SHOW_TEXT,
                                      new Text(value._2)))
            proxiedPlayer.sendMessage(componentBuilder.create(): _*)
          } else proxiedPlayer.sendMessage(value._1: _*)
        }
        AntiSpam.startSpeakIntervalTimer(player)
        AntiSpam.startSameRemarkIntervalTimer(player, optimizedMessage)
      case Failure(exception) =>
        exception.printStackTrace()
        player.sendMessage(
          TextComponent.fromLegacyText("チャットの処理に失敗しました。".toErrorMessage): _*)
    }
  }
}
