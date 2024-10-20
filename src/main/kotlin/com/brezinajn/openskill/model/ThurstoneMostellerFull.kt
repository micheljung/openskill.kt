package com.brezinajn.openskill.model

import com.brezinajn.openskill.*
import com.brezinajn.openskill.util.Getter
import com.brezinajn.openskill.util.Setter
import kotlin.math.sqrt

/**
 * Thurstone-Mosteller rating models follow a gaussian distribution, similar to TrueSkill. Gaussian
 * CDF/PDF functions differ in implementation from system to system (they're all just chebyshev
 * approximations anyway). The accuracy of this model isn't usually as great either, but tuning this
 * with an alternative gamma function can improve the accuracy if you really want to get into it.
 */
interface ThurstoneMostellerFull<TEAM, PLAYER> : Model<TEAM, PLAYER> {
  override fun invoke(game: List<TEAM>): List<TEAM> {
    val teamRatings = teamRating(game)

    return teamRatings.map { (iMu, iSigmaSq, iTeam, iRank) ->
      val (iOmega, iDelta) = teamRatings.filter { (_, _, _, qRank) -> qRank != iRank }
        .fold(.0 to .0) { (omega, delta), (qMu, qSigmaSq, _, qRank) ->
          val ciq = sqrt(iSigmaSq + qSigmaSq + TWOBETASQ)
          val deltaMu = (iMu - qMu) / ciq
          val sigSqToCiq = iSigmaSq / ciq
          val gamma = gamma(ciq, teamRatings.size.toDouble())

          if (qRank == iRank)
            Pair(
              omega + sigSqToCiq * vt(deltaMu, EPSILON / ciq),
              delta + ((gamma * sigSqToCiq) / ciq) * wt(deltaMu, EPSILON / ciq),
            )
          else {
            val sign = if (qRank > iRank) 1 else -1
            Pair(
              omega + sign * sigSqToCiq * v(sign * deltaMu, EPSILON / ciq),
              delta + ((gamma * sigSqToCiq) / ciq) * w(sign * deltaMu, EPSILON / ciq),
            )
          }
        }
      iTeam.setPlayers(
        iSigmaSq = iSigmaSq,
        iDelta = iDelta,
        iOmega = iOmega,
      )
    }
  }

  companion object {
    inline operator fun <TEAM, PLAYER> invoke(
      noinline sigmaSetter: Setter<PLAYER, Double>,
      crossinline sigmaGetter: Getter<PLAYER, Double>,
      noinline muSetter: Setter<PLAYER, Double>,
      crossinline muGetter: Getter<PLAYER, Double>,
      noinline playersSetter: Setter<TEAM, List<PLAYER>>,
      crossinline playersGetter: Getter<TEAM, List<PLAYER>>,
      crossinline rankGetter: Getter<List<TEAM>, List<Int>> = { it.indices.toList() },
      constants: Constants,
      noinline gamma: (Double, Double) -> Double = { _, k -> 1 / k },
    ): ThurstoneMostellerFull<TEAM, PLAYER> =
      object : ThurstoneMostellerFull<TEAM, PLAYER>, Constants by constants {
        override val sigmaSetter = sigmaSetter
        override val muSetter = muSetter
        override val playersSetter = playersSetter

        override val gamma: (Double, Double) -> Double = gamma
        override val TEAM.players: List<PLAYER>
          get() = playersGetter(this)
        override val PLAYER.mu: Double
          get() = muGetter(this)
        override val PLAYER.sigma: Double
          get() = sigmaGetter(this)
        override val List<TEAM>.rank: List<Int>
          get() = rankGetter(this)
      }
  }
}
