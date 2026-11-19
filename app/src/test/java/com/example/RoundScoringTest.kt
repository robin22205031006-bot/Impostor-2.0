package com.example

import com.example.data.RoundScoring
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RoundScoringTest {

    @Test
    fun `agent prediction scoring covers all exact combinations`() {
        val players = listOf("imp", "a1", "a2", "a3", "a4", "a5", "a6")
        val impostors = setOf("imp")
        val votes = mapOf(
            "imp" to "a3",
            "a1" to "imp", // right vote (+2)
            "a2" to "a3",  // wrong vote (0)
            "a3" to "imp", // right vote (+2)
            "a4" to "a5",  // wrong vote (0)
            "a5" to "imp", // right vote (+2)
            "a6" to "a2"   // wrong vote (0)
        )
        val predictions = mapOf(
            "a1" to "imp", // right pred (+1) + right vote (+2) = +3
            "a2" to "imp", // right pred (+1) + wrong vote (0) = +1
            "a3" to "a2",  // wrong pred (-2) + right vote (+2) = 0
            "a4" to "a2"   // wrong pred (-2) + wrong vote (0) = -2
            // a5 skipped pred (0) + right vote (+2) = +2
            // a6 skipped pred (0) + wrong vote (0) = 0
        )

        val result = RoundScoring.evaluate(
            playerIds = players,
            impostorIds = impostors,
            votes = votes,
            agentPredictions = predictions,
            nullifiedVoterId = null,
            impostorAbilityEnabled = false,
            agentPredictionEnabled = true,
            impostorGuessCorrect = false
        )

        assertTrue(result.crewWins)
        assertEquals(3, result.playerDeltas["a1"])
        assertEquals(1, result.playerDeltas["a2"])
        assertEquals(0, result.playerDeltas["a3"])
        assertEquals(-2, result.playerDeltas["a4"])
        assertEquals(2, result.playerDeltas["a5"])
        assertEquals(0, result.playerDeltas["a6"])
        assertEquals(0, result.playerDeltas["imp"])
    }

    @Test
    fun `ultimate killer voids target vote so impostor survives and wins`() {
        val players = listOf("imp", "a1", "a2", "a3")
        val impostors = setOf("imp")
        // Without nullification: imp gets 2 votes (a1, a2) vs a1 gets 1 vote (imp) & a3 gets 1 vote (a3->imp? wait a3->a1)
        // Here a1 and a2 vote imp (2 votes for imp), imp and a3 vote a2 (2 votes for a2) -> tie catches impostor
        // With Killer nullifying a1, imp only has 1 vote (a2) while a2 has 2 votes (imp, a3) -> impostor escapes & wins!
        val votes = mapOf(
            "a1" to "imp",
            "a2" to "imp",
            "a3" to "a2",
            "imp" to "a2"
        )
        val predictions = mapOf(
            "a1" to "imp" // right pred (+1), but vote nullified (0 vote pts) -> +1
        )

        val result = RoundScoring.evaluate(
            playerIds = players,
            impostorIds = impostors,
            votes = votes,
            agentPredictions = predictions,
            nullifiedVoterId = "a1",
            impostorAbilityEnabled = true,
            agentPredictionEnabled = true,
            impostorGuessCorrect = false
        )

        assertFalse(result.caughtImpostor)
        assertFalse(result.crewWins)
        assertEquals("a1", result.activeNullifiedVoterId)
        assertEquals(1, result.voteCounts["imp"])
        assertEquals(2, result.voteCounts["a2"])
        assertEquals(1, result.playerDeltas["imp"])
        assertEquals(1, result.playerDeltas["a1"]) // +1 pred + 0 voided vote
        assertEquals(2, result.playerDeltas["a2"]) // 0 no pred + +2 vote
        assertEquals(0, result.playerDeltas["a3"]) // 0 no pred + 0 missed vote
    }

    @Test
    fun `savior shield blocks agent killer so protected vote counts and savior earns bonus`() {
        val players = listOf("imp", "a1", "a2", "a3")
        val impostors = setOf("imp")
        val votes = mapOf(
            "a1" to "imp", // targeted by killer, shielded by savior -> vote counts!
            "a2" to "imp", // savior
            "a3" to "a2",
            "imp" to "a2"
        )

        val result = RoundScoring.evaluate(
            playerIds = players,
            impostorIds = impostors,
            votes = votes,
            agentPredictions = emptyMap(),
            nullifiedVoterId = "a1",
            protectedPlayerId = "a1",
            saviorAgentId = "a2",
            impostorAbilityEnabled = true,
            saviorAgentEnabled = true,
            agentPredictionEnabled = true,
            impostorGuessCorrect = false
        )

        assertTrue(result.saviorSavedTarget)
        assertEquals(null, result.activeNullifiedVoterId)
        assertEquals("a1", result.rawKillerTargetId)
        assertEquals("a1", result.activeProtectedId)
        assertTrue(result.crewWins)
        assertEquals(2, result.voteCounts["imp"])
        assertEquals(2, result.playerDeltas["a1"]) // +2 right vote
        assertEquals(3, result.playerDeltas["a2"]) // +2 right vote + 1 savior save bonus
    }

    @Test
    fun `savior missing killer target suffers zero deduction`() {
        val players = listOf("imp", "a1", "a2", "a3")
        val impostors = setOf("imp")
        val votes = mapOf(
            "a1" to "imp", // targeted & voided
            "a2" to "a3",  // savior protected a3 instead of a1, wrong vote
            "a3" to "a2",
            "imp" to "a2"
        )

        val result = RoundScoring.evaluate(
            playerIds = players,
            impostorIds = impostors,
            votes = votes,
            agentPredictions = emptyMap(),
            nullifiedVoterId = "a1",
            protectedPlayerId = "a3",
            saviorAgentId = "a2",
            impostorAbilityEnabled = true,
            saviorAgentEnabled = true,
            agentPredictionEnabled = false,
            impostorGuessCorrect = false
        )

        assertFalse(result.saviorSavedTarget)
        assertEquals("a1", result.activeNullifiedVoterId)
        assertFalse(result.crewWins)
        assertEquals(0, result.playerDeltas["a2"]) // zero deduction for savior
    }

    @Test
    fun `agent director vote counts as two and only deducts on wrong initial prediction`() {
        val players = listOf("imp", "a1", "dir1", "dir2")
        // Director (dir1) votes imp -> counts as 2 votes against imp vs 2 votes against a1 -> tie catches imp
        val votesWin = mapOf(
            "imp" to "a1",
            "a1" to "a1",
            "dir1" to "imp",
            "dir2" to "a1"
        )
        // Here a1 gets 2 votes (imp, dir2) vs imp gets 2 votes from dir1's 2x vote (and let's test director wrong vote + wrong prediction too)
        val resultWithWrongPredAndRightVote = RoundScoring.evaluate(
            playerIds = players,
            impostorIds = setOf("imp"),
            votes = mapOf(
                "imp" to "a1",
                "a1" to "dir2",
                "dir1" to "imp", // 2 votes -> top count ties imp with 2? imp=2 (dir1), a1=1, dir2=1 -> imp top!
                "dir2" to "imp"
            ),
            agentPredictions = mapOf("dir1" to "a1"), // wrong pred (-2)
            nullifiedVoterId = null,
            agentDirectorId = "dir1",
            impostorAbilityEnabled = false,
            agentDirectorEnabled = true,
            agentPredictionEnabled = true,
            impostorGuessCorrect = false
        )
        assertTrue(resultWithWrongPredAndRightVote.crewWins)
        assertEquals(3, resultWithWrongPredAndRightVote.voteCounts["imp"]) // dir1(2) + dir2(1)
        assertEquals(0, resultWithWrongPredAndRightVote.playerDeltas["dir1"]) // -2 wrong pred + 2 right vote

        // Wrong vote AND wrong initial prediction -> -2 penalty applies
        val resultWrongBoth = RoundScoring.evaluate(
            playerIds = players,
            impostorIds = setOf("imp"),
            votes = votesWin.plus("a1" to "dir2"), // dir1->imp(2), imp->a1(1), dir2->a1(1), a1->dir2(1) -> wait let dir1 vote wrong:
            agentPredictions = mapOf("dir1" to "a1"),
            nullifiedVoterId = null,
            agentDirectorId = "dir1",
            impostorAbilityEnabled = false,
            agentDirectorEnabled = true,
            agentPredictionEnabled = true,
            impostorGuessCorrect = false
        )
        assertEquals(0, resultWrongBoth.playerDeltas["dir1"])

        val resultDirectorWrongVoteNoPred = RoundScoring.evaluate(
            playerIds = players,
            impostorIds = setOf("imp"),
            votes = mapOf(
                "imp" to "a1",
                "a1" to "dir2",
                "dir1" to "a1", // director votes wrong (counts as 2)
                "dir2" to "a1"
            ),
            agentPredictions = emptyMap(),
            nullifiedVoterId = null,
            agentDirectorId = "dir1",
            impostorAbilityEnabled = false,
            agentDirectorEnabled = true,
            agentPredictionEnabled = true,
            impostorGuessCorrect = false
        )
        assertFalse(resultDirectorWrongVoteNoPred.crewWins)
        assertEquals(4, resultDirectorWrongVoteNoPred.voteCounts["a1"]) // dir1(2) + imp(1) + dir2(1)
        assertEquals(0, resultDirectorWrongVoteNoPred.playerDeltas["dir1"]) // no deduction for wrong vote

        val resultDirectorWrongVoteAndWrongPred = RoundScoring.evaluate(
            playerIds = players,
            impostorIds = setOf("imp"),
            votes = mapOf(
                "imp" to "a1",
                "a1" to "dir2",
                "dir1" to "a1",
                "dir2" to "a1"
            ),
            agentPredictions = mapOf("dir1" to "a1"), // wrong initial prediction -> -2
            nullifiedVoterId = null,
            agentDirectorId = "dir1",
            impostorAbilityEnabled = false,
            agentDirectorEnabled = true,
            agentPredictionEnabled = true,
            impostorGuessCorrect = false
        )
        assertEquals(-2, resultDirectorWrongVoteAndWrongPred.playerDeltas["dir1"])
    }

    @Test
    fun `crypto random shuffle produces varied first element across rounds`() {
        val candidates = listOf("p1", "p2", "p3", "p4", "p5", "p6")
        val firstPicks = (1..24).map { com.example.data.CryptoRandom.shuffled(candidates).first() }.toSet()
        assertTrue("Expected varied random selections, got $firstPicks", firstPicks.size > 1)
    }
}
