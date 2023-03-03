Kotlin implementation of [Openskill](https://github.com/philihp/openskill.js).

Forked from https://github.com/brezinajn/openskill.kt.

#### Getting started:

* Add a dependency to `io.github.micheljung:openskill.kt:0.0.1`

#### Usage:

```kotlin
// Your player class
data class Player(
    val mu: Double,
    val sigma: Double,
    // …
)

// Your team class
data class Team(
    val players: List<Player>,
    // val score: Int, // optional
    // …
)

// Initialization of the model for your classes
val model = BradleyTerryFull(
    sigmaGetter = Player::sigma,
    muGetter = Player::mu,
    playersGetter = Team::players,
    sigmaSetter = { player, sigma -> player.copy(sigma = sigma) },
    muSetter = { player, mu -> player.copy(mu = mu) },
    playersSetter = { team, players -> team.copy(players = players) },
    // rankGetter = { it.map(Team::score) }, // optional 
)

// Match result - sorted from the first place
val teams: List<Team>

// Match result with updated player ratings
val teamsWithUpdatedRatings = model(teams)
```
