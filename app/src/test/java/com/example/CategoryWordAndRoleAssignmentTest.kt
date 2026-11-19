package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.onFirst
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToIndex
import androidx.compose.ui.test.performScrollToNode
import com.example.data.CategoryWordGenerator
import com.example.data.GameMode
import com.example.data.PlayerRole
import com.example.viewmodel.GameViewModel
import com.example.viewmodel.LocalGameViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class CategoryWordAndRoleAssignmentTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun `category word generator provides hints for agents and hides secret word from impostor`() {
        val round = CategoryWordGenerator.generate(
            category = "Food",
            mode = GameMode.CLASSIC,
            hintsEnabled = true,
            categoryCluesEnabled = true
        )
        assertEquals("Food", round.entry.category)
        assertTrue(round.agentHintText.isNotBlank())

        val agentBriefing = round.briefingFor(PlayerRole.NORMAL)
        assertFalse(agentBriefing.isSecretWordHidden)
        assertEquals(round.entry.word, agentBriefing.secretWord)
        assertEquals(round.entry.hint, agentBriefing.agentHint)
        assertEquals("Food", agentBriefing.category)

        val impostorBriefing = round.briefingFor(PlayerRole.IMPOSTOR)
        assertTrue(impostorBriefing.isSecretWordHidden)
        assertNull(impostorBriefing.secretWord)
        assertNull(impostorBriefing.agentHint)
        assertEquals("Food", impostorBriefing.category)
    }

    @Test
    fun `game view model generates category words and assigns randomized impostor and agent roles`() {
        val viewModel = GameViewModel()
        viewModel.setCategory("Nature")
        val afterCategory = viewModel.gameState.value
        assertEquals("Nature", afterCategory.selectedCategory)
        assertEquals("Nature", afterCategory.locationWord?.category)

        val impostorId = afterCategory.impostorIds.first()
        val agentId = afterCategory.agentIds.first()
        assertNull(afterCategory.referenceWordFor(impostorId))
        assertNull(afterCategory.referenceHintFor(impostorId))
        assertNotNull(afterCategory.referenceWordFor(agentId))
        assertNotNull(afterCategory.referenceHintFor(agentId))

        // Disable hints -> Agent hint becomes null while word remains visible to Agents only
        viewModel.setHintsEnabled(false)
        assertNull(viewModel.gameState.value.referenceHintFor(agentId))
        assertNotNull(viewModel.gameState.value.referenceWordFor(agentId))

        // Repeated Deal calls produce random Impostor assignments across players
        val seenImpostors = mutableSetOf<String>()
        repeat(20) {
            viewModel.assignRoles()
            seenImpostors.addAll(viewModel.gameState.value.impostorIds)
        }
        assertTrue("Expected multiple distinct players assigned as Impostor, saw $seenImpostors", seenImpostors.size > 1)
    }

    @Test
    fun `local game view model scopes word and hint briefing strictly to agents`() {
        val localVm = LocalGameViewModel()
        localVm.setCategory("World Trends")
        localVm.setHintsEnabled(true)
        localVm.startRound()

        val state = localVm.uiState.value
        assertEquals("World Trends", state.activeWord?.category)
        val impId = state.impostorIds.first()
        val agentId = state.players.first { it.id !in state.impostorIds }.id

        val impBriefing = state.wordBriefingFor(impId)
        val agentBriefing = state.wordBriefingFor(agentId)
        assertNotNull(impBriefing)
        assertNotNull(agentBriefing)
        assertNull(impBriefing?.secretWord)
        assertNull(impBriefing?.agentHint)
        assertTrue(impBriefing?.isSecretWordHidden == true)

        assertEquals(state.activeWord?.word, agentBriefing?.secretWord)
        assertTrue(!agentBriefing?.agentHint.isNullOrBlank())
        assertFalse(agentBriefing?.isSecretWordHidden == true)
    }

    @Test
    fun `role assignment lobby switches categories deals roles and hides secret word from impostors`() {
        composeRule.onNodeWithTag("welcome_start_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("tab_role_lobby").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("role_assignment_banner").assertIsDisplayed()
        composeRule.onNodeWithTag("lobby_role_split_summary").assertIsDisplayed()

        // Scroll LazyColumn to Category Word Generator card and select Food
        composeRule.onNodeWithTag("lobby_lazy_column")
            .performScrollToNode(hasTestTag("category_word_generator_card"))
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("lobby_category_chip_food").performScrollTo().performClick()
        composeRule.waitForIdle()

        // Scroll back up to Role Assignment Banner, re-deal roles, and reveal all cards
        composeRule.onNodeWithTag("lobby_lazy_column").performScrollToIndex(0)
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("lobby_shuffle_roles_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("lobby_toggle_all_roles_button").performClick()
        composeRule.waitForIdle()

        // Verify across all 4 player cards that Impostor word stays hidden and Agents are shown
        var foundImpostor = false
        var foundAgent = false
        listOf("p1", "p2", "p3", "p4").forEach { playerId ->
            composeRule.onNodeWithTag("lobby_lazy_column")
                .performScrollToNode(hasTestTag("lobby_role_card_$playerId"))
            composeRule.waitForIdle()
            if (composeRule.onAllNodesWithText("ROLE: IMPOSTOR").fetchSemanticsNodes().isNotEmpty()) {
                foundImpostor = true
            }
            if (composeRule.onAllNodesWithText("ROLE: AGENT").fetchSemanticsNodes().isNotEmpty()) {
                foundAgent = true
            }
        }
        assertTrue("Expected at least one Impostor card in lobby", foundImpostor)
        assertTrue("Expected at least one Agent card in lobby", foundAgent)
        composeRule.onAllNodesWithText("Secret Word: HIDDEN (Blend in!)").onFirst().assertExists()
    }

    @Test
    fun `pass and play role reveal shows agent hint to agents and hides secret word from impostor`() {
        composeRule.onNodeWithTag("welcome_start_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("mode_one_phone_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("players_next_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("rules_next_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("local_start_round_button").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("reveal_session_roles_summary").assertIsDisplayed()
        composeRule.onNodeWithTag("reveal_reshuffle_roles_button").assertIsDisplayed().performClick()
        composeRule.waitForIdle()

        var sawImpostorHidden = false
        var sawAgentHint = false

        repeat(4) {
            composeRule.onNodeWithTag("local_tap_to_reveal_card").performScrollTo().performClick()
            composeRule.waitForIdle()

            val isImpostorCard = composeRule.onAllNodesWithTag("role_card_impostor").fetchSemanticsNodes().isNotEmpty()
            if (isImpostorCard) {
                sawImpostorHidden = true
                composeRule.onNodeWithTag("impostor_hidden_word_badge").performScrollTo().assertIsDisplayed()
                assertTrue(composeRule.onAllNodesWithTag("secret_word_text").fetchSemanticsNodes().isEmpty())
                assertTrue(composeRule.onAllNodesWithTag("hint_box_green").fetchSemanticsNodes().isEmpty())
            } else {
                sawAgentHint = true
                composeRule.onNodeWithTag("secret_word_text").performScrollTo().assertIsDisplayed()
                composeRule.onNodeWithTag("hint_box_green").performScrollTo().assertIsDisplayed()
            }

            composeRule.onNodeWithTag("local_hide_and_pass_button").performClick()
            composeRule.waitForIdle()
        }

        assertTrue("Expected at least 1 Impostor card with hidden secret word", sawImpostorHidden)
        assertTrue("Expected at least 1 Agent card with word and hint", sawAgentHint)
    }
}
