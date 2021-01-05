package net.seichi915.seichi915chat.playerdata

import java.util.UUID

import net.md_5.bungee.api.connection.ProxiedPlayer
import net.seichi915.seichi915chat.database.Database

import scala.concurrent.Future

case class PlayerData(var japaneseConversionEnabled: Boolean,
                      var blockingUUIDList: Set[UUID]) {
  def isJapaneseConversionEnabled: Boolean = japaneseConversionEnabled

  def setJapaneseConversionEnabled(enabled: Boolean): Unit =
    japaneseConversionEnabled = enabled

  def getBlockingUUIDList: Set[UUID] = blockingUUIDList

  def setBlockingUUIDList(uuidList: Set[UUID]): Unit =
    blockingUUIDList = uuidList

  def save(proxiedPlayer: ProxiedPlayer): Future[Unit] =
    Database.savePlayerData(proxiedPlayer, this)
}
