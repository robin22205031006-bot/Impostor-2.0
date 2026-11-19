package com.example

import com.example.audio.SoundCuePlayer
import com.example.data.GamePhase
import com.example.data.PlayerRole
import com.example.data.RoundOutcome
import com.example.data.TurnStatus
import com.example.viewmodel.GameViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class GameViewModelTest {

    @Test
    fun `initial state is lobby with assigned impostor matching count`() {
        val viewModel = GameViewModel()
        val state = viewModel.gameState.value

        assertEquals(GamePhase.LOBBY, state.phase)
        assertEquals(TurnStatus.IDLE, state.turnStatus)
        assertEquals(4, state.players.size)
        assertEquals(4, state.playerRoles.size)
        assertEquals(1, state.playerRoles.values.count { it == PlayerRole.IMPOSTOR })
    }

    @Test
    fun `updating impostor count reassigns exact impostor count clamped to player count`() {
        val viewModel = GameViewModel()
        viewModel.setImpostorCount(2)
        assertEquals(2, viewModel.gameState.value.impostorCount)
        assertEquals(2, viewModel.gameState.value.playerRoles.values.count { it == PlayerRole.IMPOSTOR })

        // Cannot exceed players.size - 1
        viewModel.setImpostorCount(10)
        assertEquals(3, viewModel.gameState.value.impostorCount)
        assertEquals(3, viewModel.gameState.value.playerRoles.values.count { it == PlayerRole.IMPOSTOR })
    }

    @Test
    fun `transitions cleanly between lobby discussion and voting phases`() {
        val viewModel = GameViewModel()
        viewModel.setTurnDuration(10)

        // Lobby -> Discussion
        viewModel.startDiscussion()
        val discussionState = viewModel.gameState.value
        assertEquals(GamePhase.DISCUSSION, discussionState.phase)
        assertEquals(TurnStatus.ACTIVE, discussionState.turnStatus)
        assertEquals(10, discussionState.currentTimerCount)
        assertEquals(4, discussionState.turnOrderIds.size)

        // Pause & Resume turn status
        viewModel.toggleTimerPause()
        assertEquals(TurnStatus.PAUSED, viewModel.gameState.value.turnStatus)
        viewModel.toggleTimerPause()
        assertEquals(TurnStatus.ACTIVE, viewModel.gameState.value.turnStatus)

        // Tick down to 0 -> EXPIRED
        repeat(10) { viewModel.tickTimerOnceForTest() }
        assertEquals(0, viewModel.gameState.value.currentTimerCount)
        assertEquals(TurnStatus.EXPIRED, viewModel.gameState.value.turnStatus)

        // Next turn resets timer and status
        viewModel.nextTurn()
        assertEquals(1, viewModel.gameState.value.currentTurnIndex)
        assertEquals(10, viewModel.gameState.value.currentTimerCount)
        assertEquals(TurnStatus.ACTIVE, viewModel.gameState.value.turnStatus)

        // Discussion -> Voting
        viewModel.startVoting()
        val votingState = viewModel.gameState.value
        assertEquals(GamePhase.VOTING, votingState.phase)
        assertEquals(TurnStatus.IDLE, votingState.turnStatus)
        assertTrue(votingState.votesByPlayerId.isEmpty())

        // Voting -> Lobby
        viewModel.returnToLobby()
        val resetLobby = viewModel.gameState.value
        assertEquals(GamePhase.LOBBY, resetLobby.phase)
        assertEquals(TurnStatus.IDLE, resetLobby.turnStatus)
        assertEquals(10, resetLobby.currentTimerCount)
    }

    @Test
    fun `setPlayerCount resizes player list and clamps impostor count`() {
        val viewModel = GameViewModel()
        viewModel.setPlayerCount(7)
        assertEquals(7, viewModel.gameState.value.players.size)
        assertEquals(7, viewModel.gameState.value.playerRoles.size)

        viewModel.setImpostorCount(4)
        assertEquals(4, viewModel.gameState.value.impostorCount)

        viewModel.setPlayerCount(3)
        assertEquals(3, viewModel.gameState.value.players.size)
        assertEquals(2, viewModel.gameState.value.impostorCount)
        assertEquals(2, viewModel.gameState.value.playerRoles.values.count { it == PlayerRole.IMPOSTOR })
    }

    @Test
    fun `location word is assigned for crewmates only and hidden from impostors`() {
        val viewModel = GameViewModel()
        val state = viewModel.gameState.value
        val location = state.locationWord
        assertTrue(location != null && location.word.isNotBlank())
        assertEquals("Locations", location?.category)

        val impostorId = state.impostorIds.first()
        val crewmateId = state.players.first { it.id !in state.impostorIds }.id
        assertEquals(null, state.referenceWordFor(impostorId))
        assertEquals(location?.word, state.referenceWordFor(crewmateId))
    }

    @Test
    fun `advancing past final turn and emergency meeting both transition discussion to voting`() {
        val viewModel = GameViewModel()
        viewModel.startDiscussion()

        // Advance through all 4 turns -> auto-transitions to VOTING
        repeat(4) { viewModel.nextTurn() }
        assertEquals(GamePhase.VOTING, viewModel.gameState.value.phase)

        // Return to discussion and trigger emergency meeting report
        viewModel.returnToLobby()
        viewModel.startDiscussion()
        val reporter = viewModel.gameState.value.players.first()
        viewModel.triggerEmergencyMeeting(reporterId = reporter.id)

        val alert = viewModel.gameState.value.emergencyMeeting
        assertTrue(alert.isActive)
        assertEquals(reporter.name, alert.reporterName)

        viewModel.dismissEmergencyMeetingAndVote()
        assertEquals(GamePhase.VOTING, viewModel.gameState.value.phase)
        assertTrue(!viewModel.gameState.value.emergencyMeeting.isActive)
    }

    @Test
    fun `castVote tracks totalVotesPerPlayer and finalizeVoting yields VICTORY when impostor voted out`() {
        val viewModel = GameViewModel()
        viewModel.startVoting()
        val state = viewModel.gameState.value
        val impostorId = state.impostorIds.first()
        val crewmates = state.players.filter { it.id !in state.impostorIds }

        viewModel.castVote(crewmates[0].id, impostorId)
        viewModel.castVote(crewmates[1].id, impostorId)
        viewModel.castVote(impostorId, crewmates[0].id)

        val midVoting = viewModel.gameState.value
        assertEquals(2, midVoting.totalVotesPerPlayer[impostorId])
        assertEquals(1, midVoting.totalVotesPerPlayer[crewmates[0].id])
        assertEquals(0, midVoting.totalVotesPerPlayer[crewmates[1].id])

        viewModel.finalizeVoting()
        val results = viewModel.gameState.value
        assertEquals(GamePhase.RESULTS, results.phase)
        assertEquals(RoundOutcome.VICTORY, results.roundOutcome)
        assertEquals(listOf(impostorId), results.votedOutPlayerIds)
    }

    @Test
    fun `finalizeVoting yields DEFEAT when innocent crewmate is voted out`() {
        val viewModel = GameViewModel()
        viewModel.startVoting()
        val state = viewModel.gameState.value
        val impostorId = state.impostorIds.first()
        val crewmates = state.players.filter { it.id !in state.impostorIds }
        val wrongTargetId = crewmates[0].id

        viewModel.castVote(impostorId, wrongTargetId)
        viewModel.castVote(crewmates[1].id, wrongTargetId)
        viewModel.castVote(crewmates[2].id, wrongTargetId)

        viewModel.finalizeVoting()
        val results = viewModel.gameState.value
        assertEquals(GamePhase.RESULTS, results.phase)
        assertEquals(RoundOutcome.DEFEAT, results.roundOutcome)
        assertEquals(listOf(wrongTargetId), results.votedOutPlayerIds)
        assertEquals(3, results.totalVotesPerPlayer[wrongTargetId])
    }

    @Test
    fun `emergency alert sound toggle and turn duration update state and SoundCuePlayer`() {
        val viewModel = GameViewModel()
        try {
            viewModel.setEmergencyAlertSoundEnabled(false)
            assertFalse(viewModel.gameState.value.emergencyAlertSoundEnabled)
            assertFalse(SoundCuePlayer.emergencyAlertSoundEnabled.value)

            viewModel.setEmergencyAlertSoundEnabled(true)
            assertTrue(viewModel.gameState.value.emergencyAlertSoundEnabled)
            assertTrue(SoundCuePlayer.emergencyAlertSoundEnabled.value)

            viewModel.setTurnDuration(75)
            assertEquals(75, viewModel.gameState.value.turnDurationSeconds)
            assertEquals(75, viewModel.gameState.value.currentTimerCount)
        } finally {
            SoundCuePlayer.setEmergencyAlertSoundEnabled(true)
            SoundCuePlayer.setSoundEnabled(true)
        }
    }
}
