package com.example

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.hasTestTag
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performScrollToNode
import com.example.data.AvatarCatalog
import com.example.data.AvatarPose
import com.example.viewmodel.GameViewModel
import com.example.viewmodel.LocalGameViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class AvatarCatalogTest {

    @get:Rule
    val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun `catalog contains all 22 unique characters with 4 distinct custom poses each`() {
        val all = AvatarCatalog.all
        assertEquals(22, all.size)
        assertEquals(22, all.map { it.id }.toSet().size)
        assertEquals(22, all.map { it.code }.toSet().size)
        assertEquals(22, all.map { it.baseDrawableRes }.toSet().size)

        val allDrawablesAcrossDeck = mutableSetOf<Int>()
        all.forEach { character ->
            assertTrue("${character.id} must report hasGeneratedAltPose", character.hasGeneratedAltPose)
            val perCharacterDrawables = AvatarPose.entries.map { pose ->
                val res = character.drawableFor(pose)
                assertTrue("${character.id} pose $pose drawable must be non-zero", res != 0)
                res
            }
            assertEquals(
                "${character.id} must have 4 distinct pose drawables",
                4,
                perCharacterDrawables.toSet().size
            )
            allDrawablesAcrossDeck.addAll(perCharacterDrawables)
        }

        assertEquals(88, allDrawablesAcrossDeck.size)
    }

    @Test
    fun `viewmodels update player avatar and cycle through all 4 poses`() {
        val localVm = LocalGameViewModel()
        val p1 = localVm.uiState.value.players.first()
        assertEquals(AvatarPose.ORIGINAL, p1.avatarPose)

        localVm.setPlayerAvatar(p1.id, "avatar_22", AvatarPose.ACTION_STANCE)
        val updated = localVm.uiState.value.players.first { it.id == p1.id }
        assertEquals("avatar_22", updated.avatarId)
        assertEquals(AvatarPose.ACTION_STANCE, updated.avatarPose)

        localVm.cyclePlayerPose(p1.id)
        assertEquals(AvatarPose.TACTICAL_BUST, localVm.uiState.value.players.first { it.id == p1.id }.avatarPose)
        localVm.cyclePlayerPose(p1.id)
        assertEquals(AvatarPose.CREST_AURA, localVm.uiState.value.players.first { it.id == p1.id }.avatarPose)
        localVm.cyclePlayerPose(p1.id)
        assertEquals(AvatarPose.ORIGINAL, localVm.uiState.value.players.first { it.id == p1.id }.avatarPose)

        val lobbyVm = GameViewModel()
        val lobbyP1 = lobbyVm.gameState.value.players.first()
        lobbyVm.setPlayerAvatar(lobbyP1.id, "avatar_08", AvatarPose.CREST_AURA)
        val lobbyUpdated = lobbyVm.gameState.value.players.first { it.id == lobbyP1.id }
        assertEquals("avatar_08", lobbyUpdated.avatarId)
        assertEquals(AvatarPose.CREST_AURA, lobbyUpdated.avatarPose)

        lobbyVm.cyclePlayerPose(lobbyP1.id)
        assertEquals(AvatarPose.ORIGINAL, lobbyVm.gameState.value.players.first { it.id == lobbyP1.id }.avatarPose)
    }

    @Test
    fun `setup screen opens avatar deck sheet and switches pose`() {
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("welcome_start_button").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("mode_one_phone_button").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("open_avatar_deck_button").performScrollTo().performClick()
        composeTestRule.waitForIdle()

        composeTestRule.onNodeWithTag("avatar_picker_sheet").assertIsDisplayed()
        composeTestRule.onNodeWithTag("avatar_preview_card").assertIsDisplayed()
        composeTestRule.onNodeWithTag("pose_option_action_stance").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("pose_option_tactical_bust").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("pose_option_crest_aura").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("equip_avatar_button").performClick()
        composeTestRule.waitForIdle()
        composeTestRule.onNodeWithTag("avatar_picker_sheet").assertDoesNotExist()
    }
}
