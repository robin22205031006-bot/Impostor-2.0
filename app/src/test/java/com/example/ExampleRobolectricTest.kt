package com.example

import android.content.Context
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertTextEquals
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.test.core.app.ApplicationProvider
import com.example.audio.SoundCuePlayer
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun `read string from context`() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val appName = context.getString(R.string.app_name)
        assertEquals("Impostor", appName)
    }

    @Test
    fun `main activity launches and navigates between pass and play and role lobby`() {
        composeRule.onNodeWithTag("game_setup_screen").assertIsDisplayed()
        composeRule.onNodeWithTag("welcome_start_button").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("tab_role_lobby").performClick()
        composeRule.onNodeWithTag("game_lobby_screen").assertIsDisplayed()

        composeRule.onNodeWithTag("lobby_toggle_all_roles_button").performClick()
        composeRule.onNodeWithTag("lobby_role_card_p1").assertIsDisplayed()

        composeRule.onNodeWithTag("start_discussion_phase_button").performClick()
        composeRule.onNodeWithTag("circular_countdown_timer").assertIsDisplayed()

        composeRule.onNodeWithTag("go_to_voting_phase_button").performClick()
        composeRule.onNodeWithTag("return_to_lobby_button").assertIsDisplayed()

        composeRule.onNodeWithTag("return_to_lobby_button").performClick()
        composeRule.onNodeWithTag("tab_pass_and_play").performClick()
        composeRule.onNodeWithTag("game_setup_screen").assertIsDisplayed()
    }

    @Test
    fun `emergency meeting overlay transitions to voting screen and victory or defeat outcome screen`() {
        composeRule.onNodeWithTag("welcome_start_button").performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("tab_role_lobby").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("lobby_increase_players_button").performClick()
        composeRule.onNodeWithTag("start_discussion_phase_button").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("emergency_meeting_button").performClick()
        composeRule.onNodeWithTag("report_player_p1").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("emergency_meeting_overlay").assertIsDisplayed()
        composeRule.onNodeWithTag("emergency_proceed_to_vote_button").performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("voting_screen").assertIsDisplayed()
        composeRule.onNodeWithTag("voting_player_card_p2").performScrollTo().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithTag("reveal_voting_verdict_button").assertIsDisplayed().performClick()
        composeRule.waitForIdle()

        composeRule.onNodeWithTag("round_outcome_screen").assertIsDisplayed()
        composeRule.onNodeWithTag("outcome_headline_text").assertIsDisplayed()
    }

    @Test
    fun `settings menu toggles emergency alert sound and updates discussion timer duration`() {
        try {
            composeRule.onNodeWithTag("settings_menu_button").performClick()
            composeRule.waitForIdle()

            composeRule.onNodeWithTag("settings_menu_sheet").assertIsDisplayed()
            composeRule.onNodeWithTag("settings_emergency_sound_switch").performClick()
            composeRule.waitForIdle()
            assertFalse(SoundCuePlayer.emergencyAlertSoundEnabled.value)

            composeRule.onNodeWithTag("settings_timer_preset_60").performScrollTo().performClick()
            composeRule.waitForIdle()
            composeRule.onNodeWithTag("settings_timer_duration_text").assertTextEquals("60s")
        } finally {
            SoundCuePlayer.setEmergencyAlertSoundEnabled(true)
            SoundCuePlayer.setSoundEnabled(true)
        }
    }

    @Test
    fun `full pass and play wizard completes 3d role flip discussion voting and results`() {
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

        composeRule.onNodeWithTag("role_reveal_screen").assertIsDisplayed()
        repeat(4) {
            composeRule.onNodeWithTag("local_tap_to_reveal_card").performClick()
            composeRule.waitForIdle()
            composeRule.onNodeWithTag("local_hide_and_pass_button").assertIsDisplayed().performClick()
            composeRule.waitForIdle()
        }

        composeRule.onNodeWithTag("local_go_to_voting_button").assertIsDisplayed().performClick()
        composeRule.waitForIdle()

        val voteTargets = listOf("p2", "p1", "p1", "p1")
        voteTargets.forEach { targetId ->
            composeRule.onNodeWithTag("local_open_ballot_card").performClick()
            composeRule.waitForIdle()
            composeRule.onNodeWithTag("local_vote_player_$targetId").performScrollTo().performClick()
            composeRule.waitForIdle()
            composeRule.onNodeWithTag("local_lock_vote_button").performClick()
            composeRule.waitForIdle()
        }

        composeRule.onNodeWithTag("local_reveal_results_button").assertIsDisplayed().performClick()
        composeRule.waitForIdle()
        composeRule.onNodeWithText("THE BIG REVEAL").assertIsDisplayed()
    }
}
