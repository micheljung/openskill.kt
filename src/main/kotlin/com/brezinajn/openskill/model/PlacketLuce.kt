package com.brezinajn.openskill.model

import com.brezinajn.openskill.*
import com.brezinajn.openskill.util.Getter
import com.brezinajn.openskill.util.Setter
import kotlin.math.exp

/**
 * Plackett-Luce is a generalized Bradley-Terry model for k ≥ 3 teams. It scales best.
 */
interface PlacketLuce<TEAM, PLAYER> : Model<TEAM, PLAYER> {
  override operator fun invoke(game: List<TEAM>): List<TEAM> {
    val teamRatings = teamRating(game)
    val c = utilC(teamRatings)
    val sumQ = utilSumQ(teamRatings, c)
    val a = utilA(teamRatings)

    return teamRatings.map { (iMu, iSigmaSq, iTeam, iRank) ->
      val (omegaSet, deltaSet) = teamRatings.filter { (_, _, _, qRank) -> qRank <= iRank }
        .map { (_, _, _, qRank) ->
          val quotient = exp(iMu / c) / sumQ[qRank]
          val mu = if (qRank == iRank) 1 - quotient / a[qRank] else -quotient / a[qRank]
          val sigma = (quotient * (1 - quotient)) / a[qRank]
          Pair(mu, sigma)
        }.transpose()

      val gamma = gamma(c, iSigmaSq)
      val iOmega = omegaSet.sum() * iSigmaSq / c
      val iDelta = gamma * deltaSet.sum() * iSigmaSq / c / c

      iTeam.setPlayers(
        iSigmaSq = iSigmaSq,
        iOmega = iOmega,
        iDelta = iDelta,
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
    ): PlacketLuce<TEAM, PLAYER> = object : PlacketLuce<TEAM, PLAYER>, Constants by constants {
      override val sigmaSetter = sigmaSetter
      override val muSetter = muSetter
      override val playersSetter = playersSetter
      override val gamma = gamma
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

@Suppress("RemoveExplicitTypeArguments")
private fun <A, B> List<Pair<A, B>>.transpose(): Pair<List<A>, List<B>> = fold(
  mutableListOf<A>() to mutableListOf<B>(),
) { acc, it ->
  acc.first.add(it.first)
  acc.second.add(it.second)

  acc
}
