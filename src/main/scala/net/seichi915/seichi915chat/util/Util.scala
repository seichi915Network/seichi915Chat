package net.seichi915.seichi915chat.util

import net.md_5.bungee.api.ChatColor
import net.md_5.bungee.api.connection.ProxiedPlayer

object Util {
  def createChatPrefix(proxiedPlayer: ProxiedPlayer): String =
    s"${proxiedPlayer.getDisplayName} ${ChatColor.GREEN}≫${ChatColor.RESET} "

  def createPrivateChatPrefix(from: ProxiedPlayer, to: ProxiedPlayer): String =
    s"[${ChatColor.GREEN}${from.getDisplayName}${ChatColor.RESET} -> ${ChatColor.AQUA}${to.getDisplayName}${ChatColor.RESET}] "

  def createGlobalChatPrefix(proxiedPlayer: ProxiedPlayer): String =
    s"${ChatColor.RED}[グローバルチャット] ${ChatColor.GREEN}${proxiedPlayer.getDisplayName}${ChatColor.YELLOW}(${proxiedPlayer.getServer.getInfo.getName}) ${ChatColor.GREEN}≫${ChatColor.RESET} "

  def createGlobalPrivateChatPrefix(from: ProxiedPlayer,
                                    to: ProxiedPlayer): String =
    s"${ChatColor.RED}[グローバルチャット]${ChatColor.RESET} [${ChatColor.GREEN}${from.getDisplayName}${ChatColor.YELLOW}(${from.getServer.getInfo.getName})${ChatColor.RESET} -> ${ChatColor.AQUA}${to.getDisplayName}${ChatColor.YELLOW}(${to.getServer.getInfo.getName})${ChatColor.RESET}] "

  def createChatMessage(proxiedPlayer: ProxiedPlayer, message: String): String =
    s"${createChatPrefix(proxiedPlayer)}$message"

  def createPrivateChatMessage(from: ProxiedPlayer,
                               to: ProxiedPlayer,
                               message: String): String =
    s"${createPrivateChatPrefix(from, to)}$message"

  def createGlobalChatMessage(proxiedPlayer: ProxiedPlayer,
                              message: String): String =
    s"${createGlobalChatPrefix(proxiedPlayer)}$message"

  def createGlobalPrivateChatMessage(from: ProxiedPlayer,
                                     to: ProxiedPlayer,
                                     message: String): String =
    s"${createGlobalPrivateChatPrefix(from, to)}$message"

  def createChatMessage(proxiedPlayer: ProxiedPlayer,
                        message: String,
                        original: String): String =
    s"${createChatMessage(proxiedPlayer, message)} ${ChatColor.YELLOW}($original)"

  def createPrivateChatMessage(from: ProxiedPlayer,
                               to: ProxiedPlayer,
                               message: String,
                               original: String): String =
    s"${createPrivateChatMessage(from, to, message)} ${ChatColor.YELLOW}($original)"

  def createGlobalChatMessage(proxiedPlayer: ProxiedPlayer,
                              message: String,
                              original: String): String =
    s"${createGlobalChatMessage(proxiedPlayer, message)} ${ChatColor.YELLOW}($original)"

  def createGlobalPrivateChatMessage(from: ProxiedPlayer,
                                     to: ProxiedPlayer,
                                     message: String,
                                     original: String): String =
    s"${createGlobalPrivateChatMessage(from, to, message)} ${ChatColor.YELLOW}($original)"
}
