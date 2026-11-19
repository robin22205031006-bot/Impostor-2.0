package com.example.ui.screens

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.HelpOutline
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.zIndex
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.AvatarCatalog
import com.example.data.AvatarPose
import com.example.data.GameMode
import com.example.data.LocalPlayer
import com.example.data.WordBank
import com.example.ui.components.AddOrRenamePlayerDialog
import com.example.ui.components.AvatarPickerSheet
import com.example.ui.components.CharacterAvatarThumbnail
import com.example.ui.components.FloatingHeroSprite
import com.example.ui.components.GoldCtaButton
import com.example.ui.components.HighlightedHeadline
import com.example.ui.components.HowToPlaySheet
import com.example.ui.components.ImpostorWordmark
import com.example.ui.components.PlumStepperControl
import com.example.ui.components.WizardStepHeader
import com.example.ui.theme.LocalImpostorPalette
import com.example.viewmodel.LocalGameUiState
import com.example.viewmodel.SetupWizardStep

@Composable
fun GameSetupScreen(
    state: LocalGameUiState,
    onStepChange: (SetupWizardStep) -> Unit = {},
    onOpenRoleLobby: () -> Unit = {},
    onToggleFavoritePack: (String) -> Unit = {},
    onOpenSettings: () -> Unit = {},
    soundEnabled: Boolean = true,
    onToggleSound: () -> Unit = {},
    onAddPlayer: (String) -> Unit,
    onRenamePlayer: (String, String) -> Unit,
    onRemovePlayer: (String) -> Unit,
    onSetPlayerCount: (Int) -> Unit = {},
    onSetPlayerAvatar: (String, String, AvatarPose?) -> Unit = { _, _, _ -> },
    onCyclePlayerPose: (String) -> Unit = {},
    onSelectMode: (GameMode) -> Unit,
    onSetImpostors: (Int) -> Unit,
    onSetDuration: (Int) -> Unit,
    onSelectCategory: (String) -> Unit,
    onToggleHints: (Boolean) -> Unit,
    onToggleCategoryClues: (Boolean) -> Unit,
    onToggleTimer: (Boolean) -> Unit,
    onToggleImpostorAbility: (Boolean) -> Unit,
    onToggleSaviorAgent: (Boolean) -> Unit,
    onToggleAgentDirector: (Boolean) -> Unit,
    onToggleAgentPrediction: (Boolean) -> Unit,
    onStartRound: () -> Unit,
    modifier: Modifier = Modifier
) {
    val customWords by WordBank.customWordsFlow.collectAsStateWithLifecycle()
    val customCategoryName by WordBank.customCategoryNameFlow.collectAsStateWithLifecycle()
    var showHowToPlay by remember { mutableStateOf(false) }

    BackHandler(enabled = state.setupStep != SetupWizardStep.WELCOME) {
        val prev = when (state.setupStep) {
            SetupWizardStep.WELCOME -> SetupWizardStep.WELCOME
            SetupWizardStep.MODE_PICK -> SetupWizardStep.WELCOME
            SetupWizardStep.PLAYERS -> SetupWizardStep.MODE_PICK
            SetupWizardStep.RULES -> SetupWizardStep.PLAYERS
            SetupWizardStep.PACKS -> SetupWizardStep.RULES
        }
        onStepChange(prev)
    }

    val headerActions: @Composable () -> Unit = {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("settings_menu_button")
            ) {
                Icon(
                    imageVector = Icons.Filled.Settings,
                    contentDescription = "Open settings menu",
                    tint = LocalImpostorPalette.current.goldAccent
                )
            }
            IconButton(
                onClick = onToggleSound,
                modifier = Modifier
                    .size(48.dp)
                    .testTag("local_sound_toggle_button")
            ) {
                Icon(
                    imageVector = if (soundEnabled) Icons.AutoMirrored.Filled.VolumeUp else Icons.AutoMirrored.Filled.VolumeOff,
                    contentDescription = if (soundEnabled) "Mute sound cues" else "Enable sound cues",
                    tint = if (soundEnabled) LocalImpostorPalette.current.goldAccent else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }

    AnimatedContent(
        targetState = state.setupStep,
        transitionSpec = {
            val forward = targetState.ordinal >= initialState.ordinal
            val inOffset: (Int) -> Int = { if (forward) it / 4 else -it / 4 }
            val outOffset: (Int) -> Int = { if (forward) -it / 4 else it / 4 }
            (fadeIn(tween(240)) + slideInHorizontally(tween(240), inOffset))
                .togetherWith(fadeOut(tween(180)) + slideOutHorizontally(tween(180), outOffset))
                .using(SizeTransform(clip = false))
        },
        label = "setup_wizard_step",
        modifier = modifier
            .fillMaxSize()
            .testTag("game_setup_screen")
    ) { step ->
        when (step) {
            SetupWizardStep.WELCOME -> WelcomeSetupStep(
                onStartGame = { onStepChange(SetupWizardStep.MODE_PICK) },
                onHowToPlay = { showHowToPlay = true },
                trailingHeader = headerActions
            )

            SetupWizardStep.MODE_PICK -> ModePickSetupStep(
                onBack = { onStepChange(SetupWizardStep.WELCOME) },
                onOnePhone = { onStepChange(SetupWizardStep.PLAYERS) },
                onRoleLobby = onOpenRoleLobby,
                trailingHeader = headerActions
            )

            SetupWizardStep.PLAYERS -> PlayersSetupStep(
                state = state,
                onBack = { onStepChange(SetupWizardStep.MODE_PICK) },
                onNext = { onStepChange(SetupWizardStep.RULES) },
                onAddPlayer = onAddPlayer,
                onRenamePlayer = onRenamePlayer,
                onRemovePlayer = onRemovePlayer,
                onSetPlayerCount = onSetPlayerCount,
                onSetPlayerAvatar = onSetPlayerAvatar,
                onCyclePlayerPose = onCyclePlayerPose,
                trailingHeader = headerActions
            )

            SetupWizardStep.RULES -> RulesSetupStep(
                state = state,
                onBack = { onStepChange(SetupWizardStep.PLAYERS) },
                onNext = { onStepChange(SetupWizardStep.PACKS) },
                onSelectMode = onSelectMode,
                onSetImpostors = onSetImpostors,
                onSetDuration = onSetDuration,
                onToggleHints = onToggleHints,
                onToggleCategoryClues = onToggleCategoryClues,
                onToggleTimer = onToggleTimer,
                onToggleImpostorAbility = onToggleImpostorAbility,
                onToggleSaviorAgent = onToggleSaviorAgent,
                onToggleAgentDirector = onToggleAgentDirector,
                onToggleAgentPrediction = onToggleAgentPrediction,
                trailingHeader = headerActions
            )

            SetupWizardStep.PACKS -> PacksSetupStep(
                state = state,
                customCategoryName = customCategoryName,
                customWords = customWords,
                onBack = { onStepChange(SetupWizardStep.RULES) },
                onSelectCategory = onSelectCategory,
                onToggleFavoritePack = onToggleFavoritePack,
                onStartRound = onStartRound,
                trailingHeader = headerActions
            )
        }
    }

    if (showHowToPlay) {
        HowToPlaySheet(onDismiss = { showHowToPlay = false })
    }
}

@Composable
private fun WelcomeSetupStep(
    onStartGame: () -> Unit,
    onHowToPlay: () -> Unit,
    trailingHeader: @Composable () -> Unit
) {
    val palette = LocalImpostorPalette.current
    val heroes = remember {
        listOf(
            AvatarCatalog.findById("avatar_09") to AvatarPose.ACTION_STANCE,
            AvatarCatalog.findById("avatar_08") to AvatarPose.ORIGINAL,
            AvatarCatalog.findById("avatar_05") to AvatarPose.ACTION_STANCE,
            AvatarCatalog.findById("avatar_13") to AvatarPose.ORIGINAL,
            AvatarCatalog.findById("avatar_22") to AvatarPose.CREST_AURA
        )
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 10.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Top Brand Bar with discreet Settings & Sound toggles
        Box(modifier = Modifier.fillMaxWidth()) {
            ImpostorWordmark(
                modifier = Modifier
                    .align(Alignment.Center)
                    .padding(vertical = 8.dp)
            )
            Box(modifier = Modifier.align(Alignment.CenterEnd)) {
                trailingHeader()
            }
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // Overlapping Fan Lineup of 5 Transparent Anime Hero Cutouts
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(210.dp),
                contentAlignment = Alignment.BottomCenter
            ) {
                FloatingHeroSprite(
                    character = heroes[0].first,
                    pose = heroes[0].second,
                    size = 132.dp,
                    bobOffsetDp = 3f,
                    phaseOffset = 0.8f,
                    isFocal = false,
                    modifier = Modifier
                        .offset(x = (-118).dp, y = (-6).dp)
                        .zIndex(1f)
                )
                FloatingHeroSprite(
                    character = heroes[4].first,
                    pose = heroes[4].second,
                    size = 132.dp,
                    bobOffsetDp = 3f,
                    phaseOffset = 2.4f,
                    isFocal = false,
                    modifier = Modifier
                        .offset(x = 118.dp, y = (-6).dp)
                        .zIndex(1f)
                )
                FloatingHeroSprite(
                    character = heroes[1].first,
                    pose = heroes[1].second,
                    size = 158.dp,
                    bobOffsetDp = 4f,
                    phaseOffset = 1.6f,
                    isFocal = true,
                    modifier = Modifier
                        .offset(x = (-58).dp, y = (-2).dp)
                        .zIndex(2f)
                )
                FloatingHeroSprite(
                    character = heroes[3].first,
                    pose = heroes[3].second,
                    size = 158.dp,
                    bobOffsetDp = 4f,
                    phaseOffset = 3.8f,
                    isFocal = true,
                    modifier = Modifier
                        .offset(x = 58.dp, y = (-2).dp)
                        .zIndex(2f)
                )
                FloatingHeroSprite(
                    character = heroes[2].first,
                    pose = heroes[2].second,
                    size = 188.dp,
                    bobOffsetDp = 5f,
                    phaseOffset = 0f,
                    isFocal = true,
                    modifier = Modifier.zIndex(3f)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            HighlightedHeadline(
                prefix = "Your friends.\n",
                highlighted = "One",
                suffix = " terrible liar.",
                style = MaterialTheme.typography.displayMedium.copy(lineHeight = 36.sp)
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Take turns giving a clue. Don't say the word.",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 8.dp, bottom = 6.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            GoldCtaButton(
                text = "Start a game",
                onClick = onStartGame,
                modifier = Modifier.testTag("welcome_start_button")
            )
            Surface(
                onClick = onHowToPlay,
                color = MaterialTheme.colorScheme.surfaceContainer,
                shape = RoundedCornerShape(50),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier.testTag("welcome_how_to_play_button")
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 18.dp, vertical = 9.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                        contentDescription = null,
                        tint = palette.goldAccent,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "How to play",
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    }
}

@Composable
private fun ModePickSetupStep(
    onBack: () -> Unit,
    onOnePhone: () -> Unit,
    onRoleLobby: () -> Unit,
    trailingHeader: @Composable () -> Unit
) {
    val palette = LocalImpostorPalette.current

    Column(modifier = Modifier.fillMaxSize()) {
        WizardStepHeader(
            stepLabel = "CHOOSE MODE",
            onBack = onBack,
            trailing = trailingHeader
        )

        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 20.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            HighlightedHeadline(
                prefix = "Your people.\n",
                highlighted = "Your way",
                suffix = " to play.",
                textAlign = TextAlign.Start,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(4.dp))

            // Option 1: One Phone (Pass & Play full wizard)
            Surface(
                onClick = onOnePhone,
                color = MaterialTheme.colorScheme.surfaceContainer,
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.5.dp, palette.goldAccent.copy(alpha = 0.72f)),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("mode_one_phone_button")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            color = palette.goldAccent,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.PhoneAndroid,
                                    contentDescription = null,
                                    tint = palette.goldDarkInk,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Text(
                            text = "One phone",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "Pass the phone around the table with 3D flip role cards, Special Roles, Custom Packs & live timer.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    FloatingHeroSprite(
                        character = AvatarCatalog.findById("avatar_05"),
                        pose = AvatarPose.ACTION_STANCE,
                        size = 112.dp,
                        isFocal = false
                    )
                }
            }

            // Option 2: Everyone's Phone / Quick Role Lobby
            Surface(
                onClick = onRoleLobby,
                color = MaterialTheme.colorScheme.surfaceContainer,
                shape = RoundedCornerShape(24.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("tab_role_lobby")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(20.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    Column(
                        modifier = Modifier.weight(1f),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Surface(
                            color = palette.goldSurface,
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, palette.goldAccent.copy(alpha = 0.5f)),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Filled.Groups,
                                    contentDescription = null,
                                    tint = palette.goldAccent,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                        }
                        Text(
                            text = "Role Assignment",
                            style = MaterialTheme.typography.headlineSmall,
                            fontWeight = FontWeight.ExtraBold
                        )
                        Text(
                            text = "Randomly assign Impostor & Agents roles at session start, generate category words with Agent hints, and run live voting.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    FloatingHeroSprite(
                        character = AvatarCatalog.findById("avatar_08"),
                        pose = AvatarPose.CREST_AURA,
                        size = 112.dp,
                        isFocal = false
                    )
                }
            }
        }
    }
}

@Composable
private fun PlayersSetupStep(
    state: LocalGameUiState,
    onBack: () -> Unit,
    onNext: () -> Unit,
    onAddPlayer: (String) -> Unit,
    onRenamePlayer: (String, String) -> Unit,
    onRemovePlayer: (String) -> Unit,
    onSetPlayerCount: (Int) -> Unit,
    onSetPlayerAvatar: (String, String, AvatarPose?) -> Unit,
    onCyclePlayerPose: (String) -> Unit,
    trailingHeader: @Composable () -> Unit
) {
    val palette = LocalImpostorPalette.current
    var focusedPlayerId by remember(state.players) {
        mutableStateOf(state.players.firstOrNull()?.id.orEmpty())
    }
    val focusedPlayer = state.players.firstOrNull { it.id == focusedPlayerId }
        ?: state.players.firstOrNull()
    val focusedIndex = state.players.indexOfFirst { it.id == focusedPlayer?.id }.coerceAtLeast(0)

    var showAddDialog by remember { mutableStateOf(false) }
    var editingPlayer by remember { mutableStateOf<LocalPlayer?>(null) }
    var avatarPickerPlayerId by remember { mutableStateOf<String?>(null) }

    Column(modifier = Modifier.fillMaxSize()) {
        WizardStepHeader(
            stepLabel = "1 / YOUR PEOPLE",
            onBack = onBack,
            trailing = trailingHeader
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    HighlightedHeadline(
                        prefix = "Who's ",
                        highlighted = "in?",
                        textAlign = TextAlign.Start
                    )
                    PlumStepperControl(
                        valueText = "${state.players.size}",
                        canDecrease = state.players.size > 2,
                        canIncrease = state.players.size < 16,
                        onDecrease = { onSetPlayerCount(state.players.size - 1) },
                        onIncrease = { onSetPlayerCount(state.players.size + 1) },
                        decreaseTag = "setup_decrease_players_button",
                        increaseTag = "setup_increase_players_button",
                        valueTag = "players_count_value"
                    )
                }
            }

            // Interactive Floating Hero Cutout Stage (video lineup centerpiece)
            if (focusedPlayer != null) {
                item {
                    val leftNeighbor = if (state.players.size > 1) {
                        state.players[(focusedIndex - 1 + state.players.size) % state.players.size]
                    } else null
                    val rightNeighbor = if (state.players.size > 2) {
                        state.players[(focusedIndex + 1) % state.players.size]
                    } else null
                    val focusedHero = AvatarCatalog.findById(focusedPlayer.avatarId)

                    Surface(
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        shape = RoundedCornerShape(24.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(178.dp),
                                contentAlignment = Alignment.BottomCenter
                            ) {
                                if (leftNeighbor != null) {
                                    FloatingHeroSprite(
                                        character = AvatarCatalog.findById(leftNeighbor.avatarId),
                                        pose = leftNeighbor.avatarPose,
                                        size = 114.dp,
                                        bobOffsetDp = 3f,
                                        phaseOffset = 1.4f,
                                        isFocal = false,
                                        modifier = Modifier
                                            .offset(x = (-92).dp)
                                            .clickable { focusedPlayerId = leftNeighbor.id }
                                    )
                                }
                                if (rightNeighbor != null) {
                                    FloatingHeroSprite(
                                        character = AvatarCatalog.findById(rightNeighbor.avatarId),
                                        pose = rightNeighbor.avatarPose,
                                        size = 114.dp,
                                        bobOffsetDp = 3f,
                                        phaseOffset = 2.9f,
                                        isFocal = false,
                                        modifier = Modifier
                                            .offset(x = 92.dp)
                                            .clickable { focusedPlayerId = rightNeighbor.id }
                                    )
                                }
                                FloatingHeroSprite(
                                    character = focusedHero,
                                    pose = focusedPlayer.avatarPose,
                                    size = 172.dp,
                                    bobOffsetDp = 5f,
                                    isFocal = true,
                                    modifier = Modifier
                                        .zIndex(2f)
                                        .clickable { onCyclePlayerPose(focusedPlayer.id) }
                                )
                            }

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "${focusedPlayer.name} • ${focusedHero.name}",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "Pose: ${focusedPlayer.avatarPose.label} (tap hero to pose)",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = palette.goldAccent
                                    )
                                }
                                OutlinedButton(
                                    onClick = { avatarPickerPlayerId = focusedPlayer.id },
                                    shape = RoundedCornerShape(12.dp),
                                    border = BorderStroke(1.dp, palette.goldAccent.copy(alpha = 0.7f)),
                                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                                    modifier = Modifier.testTag("open_avatar_deck_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Style,
                                        contentDescription = null,
                                        tint = palette.goldAccent,
                                        modifier = Modifier
                                            .padding(end = 5.dp)
                                            .size(16.dp)
                                    )
                                    Text("Avatar Deck", color = palette.goldAccent)
                                }
                            }
                        }
                    }
                }
            }

            // Player Cards List
            itemsIndexed(state.players, key = { _, p -> p.id }) { idx, player ->
                val avatarChar = AvatarCatalog.findById(player.avatarId)
                val isFocused = player.id == focusedPlayer?.id

                Surface(
                    onClick = { focusedPlayerId = player.id },
                    color = if (isFocused) Color(0xFF31253E) else MaterialTheme.colorScheme.surfaceContainer,
                    shape = RoundedCornerShape(18.dp),
                    border = BorderStroke(
                        if (isFocused) 1.5.dp else 1.dp,
                        if (isFocused) palette.goldAccent else MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .animateItem()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 10.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Box(
                            modifier = Modifier
                                .clickable { avatarPickerPlayerId = player.id }
                                .testTag("player_avatar_button_${player.id}")
                        ) {
                            CharacterAvatarThumbnail(
                                character = avatarChar,
                                pose = player.avatarPose,
                                size = 46.dp,
                                borderColor = if (isFocused) palette.goldAccent else avatarChar.glowColor,
                                isHighlighted = isFocused
                            )
                        }

                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "PLAYER ${idx + 1}",
                                style = MaterialTheme.typography.labelSmall,
                                color = palette.goldAccent
                            )
                            Text(
                                text = player.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }

                        Surface(
                            onClick = { onCyclePlayerPose(player.id) },
                            color = MaterialTheme.colorScheme.surfaceContainerLowest,
                            shape = RoundedCornerShape(10.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            modifier = Modifier.testTag("cycle_pose_${player.id}")
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Filled.AutoAwesome,
                                    contentDescription = "Cycle pose",
                                    tint = palette.goldAccent,
                                    modifier = Modifier.size(13.dp)
                                )
                                Text(
                                    text = player.avatarPose.badgeCode,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = palette.goldAccent
                                )
                            }
                        }

                        IconButton(
                            onClick = { editingPlayer = player },
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("rename_player_${player.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Edit,
                                contentDescription = "Rename ${player.name}",
                                tint = palette.goldAccent
                            )
                        }
                        IconButton(
                            onClick = { onRemovePlayer(player.id) },
                            enabled = state.players.size > 2,
                            modifier = Modifier
                                .size(48.dp)
                                .testTag("remove_player_${player.id}")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Close,
                                contentDescription = "Remove ${player.name}",
                                tint = if (state.players.size > 2) palette.impostorGlow else MaterialTheme.colorScheme.outline
                            )
                        }
                    }
                }
            }

            // "+ Add a friend" button card matching the video
            if (state.players.size < 16) {
                item {
                    Surface(
                        onClick = { showAddDialog = true },
                        color = MaterialTheme.colorScheme.surfaceContainerLowest,
                        shape = RoundedCornerShape(18.dp),
                        border = BorderStroke(1.2.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(54.dp)
                            .testTag("add_friend_button")
                    ) {
                        Row(
                            modifier = Modifier.fillMaxSize(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = null,
                                tint = palette.goldAccent,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Add a friend",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = palette.goldAccent
                            )
                        }
                    }
                }
            }
        }

        // Sticky Footer
        Surface(
            color = MaterialTheme.colorScheme.background.copy(alpha = 0.94f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp)
            ) {
                GoldCtaButton(
                    text = "Everyone's here",
                    onClick = onNext,
                    modifier = Modifier.testTag("players_next_button")
                )
            }
        }
    }

    if (showAddDialog) {
        AddOrRenamePlayerDialog(
            titleTag = "ADD NAME",
            headline = "What should we call them?",
            confirmLabel = "GOT IT",
            onDismiss = { showAddDialog = false },
            onConfirm = { name ->
                onAddPlayer(name)
                showAddDialog = false
            }
        )
    }

    val renameTarget = editingPlayer
    if (renameTarget != null) {
        AddOrRenamePlayerDialog(
            titleTag = "EDIT NAME",
            headline = "Rename player",
            initialName = renameTarget.name,
            confirmLabel = "SAVE",
            onDismiss = { editingPlayer = null },
            onConfirm = { name ->
                onRenamePlayer(renameTarget.id, name)
                editingPlayer = null
            }
        )
    }

    if (avatarPickerPlayerId != null) {
        AvatarPickerSheet(
            players = state.players,
            initialPlayerId = avatarPickerPlayerId,
            onSelectAvatarForPlayer = { pid, avatarId, pose ->
                onSetPlayerAvatar(pid, avatarId, pose)
            },
            onDismiss = { avatarPickerPlayerId = null }
        )
    }
}
