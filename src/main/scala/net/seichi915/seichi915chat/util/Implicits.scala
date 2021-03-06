package net.seichi915.seichi915chat.util

import net.md_5.bungee.api.{ChatColor, CommandSender}
import net.md_5.bungee.api.connection.ProxiedPlayer

object Implicits {
  implicit class StringOps(string: String) {
    def toNormalMessage: String =
      s"${ChatColor.AQUA}[${ChatColor.WHITE}seichi915Chat${ChatColor.AQUA}]${ChatColor.RESET} $string"

    def toSuccessMessage: String =
      s"${ChatColor.AQUA}[${ChatColor.GREEN}seichi915Chat${ChatColor.AQUA}]${ChatColor.RESET} $string"

    def toWarningMessage: String =
      s"${ChatColor.AQUA}[${ChatColor.GOLD}seichi915Chat${ChatColor.AQUA}]${ChatColor.RESET} $string"

    def toErrorMessage: String =
      s"${ChatColor.AQUA}[${ChatColor.RED}seichi915Chat${ChatColor.AQUA}]${ChatColor.RESET} $string"

    def optimize: String = string.replace("　", " ").trim
  }

  implicit class BooleanOps(boolean: Boolean) {
    def toInt: Int = if (boolean) 1 else 0
  }

  implicit class CommandSenderOps(commandSender: CommandSender) {
    def isPlayer: Boolean = commandSender.isInstanceOf[ProxiedPlayer]
  }
}
