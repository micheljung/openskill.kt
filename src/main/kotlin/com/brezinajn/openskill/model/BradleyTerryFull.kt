package com.brezinajn.openskill.model

import com.brezinajn.openskill.Constants
import com.brezinajn.openskill.teamRating
import com.brezinajn.openskill.util.Getter
import com.brezinajn.openskill.util.Setter
import kotlin.math.exp
import kotlin.math.sqrt

/**
 * Bradley-Terry rating models follow a logistic distribution over a player's skill, similar to
 * Glicko.
 */
interface BradleyTerryFull<TEAM, PLAYER> : Model<TEAM, PLAYER> {

  override fun invoke(game: List<TEAM>): List<TEAM> {
    val teamRatings = teamRating(game)

    return teamRatings.map { (iMu, iSigmaSq, iTeam, iRank) ->

      val (iOmega, iDelta) = teamRatings.filter { (_, _, _, qRank) -> qRank != iRank }
        .fold(.0 to .0) { (omega, delta), (qMu, qSigmaSq, _, qRank) ->
          val ciq = sqrt(iSigmaSq + qSigmaSq + TWOBETASQ)
          val piq = 1 / (1 + exp((qMu - iMu) / ciq))
          val sigSqToCiq = iSigmaSq / ciq
          val gamma = gamma(ciq, iSigmaSq)

          Pair(
            omega + sigSqToCiq * (score(qRank, iRank) - piq),
            delta + ((gamma * sigSqToCiq) / ciq) * piq * (1 - piq),
          )
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
      constants: Constants = Constants(),
      noinline gamma: (Double, Double) -> Double = ::gamma,
    ): BradleyTerryFull<TEAM, PLAYER> =
      object : BradleyTerryFull<TEAM, PLAYER>, Constants by constants {
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

private fun <T : Comparable<T>> score(q: T, i: T) = when {
  q < i -> .0
  q > i -> 1.0
  else -> .5
}
