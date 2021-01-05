package net.seichi915.seichi915chat.antispam

import net.md_5.bungee.api.connection.ProxiedPlayer

import scala.collection.mutable

object AntiSpam {
  private var speakIntervalTimer = mutable.Map[ProxiedPlayer, Int]()
  private var sameRemarkIntervalTimer =
    mutable.Map[ProxiedPlayer, mutable.Map[String, Int]]()

  class Timer extends Runnable {
    override def run(): Unit = {
      speakIntervalTimer.foreach {
        case (proxiedPlayer: ProxiedPlayer, remaining: Int) =>
          if (remaining == 1) speakIntervalTimer.remove(proxiedPlayer)
          else speakIntervalTimer.update(proxiedPlayer, remaining - 1)
      }
      sameRemarkIntervalTimer.foreach {
        case (proxiedPlayer: ProxiedPlayer, map: mutable.Map[String, Int]) =>
          val remarkMap = map.clone()
          remarkMap.foreach {
            case (remark: String, remaining: Int) =>
              if (remaining == 1) remarkMap.remove(remark)
              else remarkMap.update(remark, remaining - 1)
          }
          sameRemarkIntervalTimer.update(proxiedPlayer, remarkMap)
      }
    }
  }

  def startSpeakIntervalTimer(proxiedPlayer: ProxiedPlayer): Unit =
    speakIntervalTimer.get(proxiedPlayer) match {
      case Some(_) => speakIntervalTimer.update(proxiedPlayer, 5)
      case None    => speakIntervalTimer += proxiedPlayer -> 5
    }

  def startSameRemarkIntervalTimer(proxiedPlayer: ProxiedPlayer,
                                   remark: String): Unit = {
    sameRemarkIntervalTimer.get(proxiedPlayer) match {
      case Some(map) =>
        val remarkMap = map.clone()
        remarkMap.get(remark.toLowerCase) match {
          case Some(_) => remarkMap.update(remark.toLowerCase, 30)
          case None    => remarkMap += remark.toLowerCase -> 30
        }
        sameRemarkIntervalTimer.update(proxiedPlayer, remarkMap)
      case None =>
        sameRemarkIntervalTimer += proxiedPlayer -> mutable.Map(
          remark.toLowerCase -> 30)
    }
  }

  def canSpeak(proxiedPlayer: ProxiedPlayer): Boolean =
    !speakIntervalTimer.contains(proxiedPlayer)

  def canSpeak(proxiedPlayer: ProxiedPlayer, remark: String): Boolean =
    sameRemarkIntervalTimer.get(proxiedPlayer) match {
      case Some(map) => !map.contains(remark.toLowerCase)
      case None      => true
    }

  def getSpeakIntervalTimerRemaining(
      proxiedPlayer: ProxiedPlayer): Option[Int] =
    speakIntervalTimer.get(proxiedPlayer)

  def getSameRemarkIntervalTimerRemaining(proxiedPlayer: ProxiedPlayer,
                                          remark: String): Option[Int] =
    sameRemarkIntervalTimer.get(proxiedPlayer) match {
      case Some(map) => map.get(remark.toLowerCase)
      case None      => None
    }

  def reset(proxiedPlayer: ProxiedPlayer): Unit = {
    if (speakIntervalTimer.contains(proxiedPlayer))
      speakIntervalTimer.remove(proxiedPlayer)
    if (sameRemarkIntervalTimer.contains(proxiedPlayer))
      sameRemarkIntervalTimer.remove(proxiedPlayer)
  }
}
