package com.example

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import androidx.test.core.app.ApplicationProvider
import com.example.data.PlayerRole
import com.example.ui.components.GameRoleIdentity
import com.example.ui.components.TurnExpiredBroadcast
import com.example.ui.components.TurnTimerEventBus
import com.example.ui.components.style
import com.example.ui.theme.ImpostorRolePalette
import kotlinx.coroutines.CoroutineStart
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class TimerAndRoleUiTest {

    @Test
    fun `every role resolves to a unique identity and distinct color palette`() {
        val agent = GameRoleIdentity.resolve(PlayerRole.NORMAL)
        val impostor = GameRoleIdentity.resolve(PlayerRole.IMPOSTOR)
        val killer = GameRoleIdentity.resolve(PlayerRole.IMPOSTOR, isUltimateKiller = true)
        val savior = GameRoleIdentity.resolve(PlayerRole.NORMAL, isSavior = true)
        val director = GameRoleIdentity.resolve(PlayerRole.NORMAL, isDirector = true)

        assertEquals(GameRoleIdentity.AGENT, agent)
        assertEquals(GameRoleIdentity.IMPOSTOR, impostor)
        assertEquals(GameRoleIdentity.AGENT_KILLER, killer)
        assertEquals(GameRoleIdentity.SAVIOR_AGENT, savior)
        assertEquals(GameRoleIdentity.AGENT_DIRECTOR, director)

        val palette = ImpostorRolePalette()
        val accents = GameRoleIdentity.entries.map { it.style(palette).accent }.toSet()
        val glows = GameRoleIdentity.entries.map { it.style(palette).glow }.toSet()
        assertEquals(5, accents.size)
        assertEquals(5, glows.size)
    }

    @Test
    fun `turn timer event bus broadcasts both shared flow event and android intent on expiration`() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        var receivedIntent: Intent? = null
        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                receivedIntent = intent
            }
        }
        ContextCompat.registerReceiver(
            context,
            receiver,
            IntentFilter(TurnTimerEventBus.ACTION_TURN_TIMER_EXPIRED),
            ContextCompat.RECEIVER_NOT_EXPORTED
        )

        val deferredBroadcast = async(start = CoroutineStart.UNDISPATCHED) { TurnTimerEventBus.events.first() }
        TurnTimerEventBus.emitExpired(
            context = context,
            playerName = "Alex",
            turnIndex = 1,
            totalTurns = 4
        )
        Shadows.shadowOf(context.mainLooper).idle()

        val flowEvent: TurnExpiredBroadcast = deferredBroadcast.await()
        assertEquals("Alex", flowEvent.playerName)
        assertEquals(1, flowEvent.turnIndex)
        assertEquals(4, flowEvent.totalTurns)

        assertNotNull(receivedIntent)
        assertEquals("Alex", receivedIntent?.getStringExtra(TurnTimerEventBus.EXTRA_PLAYER_NAME))
        assertEquals(1, receivedIntent?.getIntExtra(TurnTimerEventBus.EXTRA_TURN_INDEX, -1))
        assertEquals(4, receivedIntent?.getIntExtra(TurnTimerEventBus.EXTRA_TOTAL_TURNS, -1))

        context.unregisterReceiver(receiver)
    }
}
