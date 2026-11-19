package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocalDining
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.Park
import androidx.compose.material.icons.filled.Psychology
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.Style
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.GameMode
import com.example.data.WordBank
import com.example.data.WordEntry
import com.example.ui.components.GameRoleIdentity
import com.example.ui.components.GoldCtaButton
import com.example.ui.components.HighlightedHeadline
import com.example.ui.components.PlumStepperControl
import com.example.ui.components.PlumTogglePill
import com.example.ui.components.RoleEmblemBadge
import com.example.ui.components.WizardStepHeader
import com.example.ui.components.style
import com.example.ui.theme.LocalImpostorPalette
import com.example.viewmodel.LocalGameUiState

@Composable
internal fun RulesSetupStep(
    state: LocalGameUiState,
    onBack: () -> Unit,
    onNext: () -> Unit,
    onSelectMode: (GameMode) -> Unit,
    onSetImpostors: (Int) -> Unit,
    onSetDuration: (Int) -> Unit,
    onToggleHints: (Boolean) -> Unit,
    onToggleCategoryClues: (Boolean) -> Unit,
    onToggleTimer: (Boolean) -> Unit,
    onToggleImpostorAbility: (Boolean) -> Unit,
    onToggleSaviorAgent: (Boolean) -> Unit,
    onToggleAgentDirector: (Boolean) -> Unit,
    onToggleAgentPrediction: (Boolean) -> Unit,
    trailingHeader: @Composable () -> Unit
) {
    val palette = LocalImpostorPalette.current
    val maxImpostors = (state.players.size - 1).coerceAtLeast(1)

    Column(modifier = Modifier.fillMaxSize()) {
        WizardStepHeader(
            stepLabel = "2 / YOUR RULES",
            onBack = onBack,
            trailing = trailingHeader
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                HighlightedHeadline(
                    prefix = "Make it ",
                    highlighted = "your night.",
                    textAlign = TextAlign.Start,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 4.dp)
                )
            }

            // Game Mode compact selector card
            item {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Text(
                            text = "PACE & STYLE",
                            style = MaterialTheme.typography.labelMedium,
                            color = palette.goldAccent
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            GameMode.entries.forEach { mode ->
                                val selected = state.gameMode == mode
                                val modeIcon = when (mode) {
                                    GameMode.CLASSIC -> Icons.Filled.SportsEsports
                                    GameMode.SPEED_ROUND -> Icons.Filled.Bolt
                                    GameMode.EXPERT -> Icons.Filled.Psychology
                                }
                                val bg by animateColorAsState(
                                    targetValue = if (selected) palette.goldAccent else MaterialTheme.colorScheme.surfaceContainerLowest,
                                    label = "mode_pill_bg"
                                )
                                val fg by animateColorAsState(
                                    targetValue = if (selected) palette.goldDarkInk else MaterialTheme.colorScheme.onSurface,
                                    label = "mode_pill_fg"
                                )
                                Surface(
                                    onClick = { onSelectMode(mode) },
                                    color = bg,
                                    shape = RoundedCornerShape(14.dp),
                                    border = BorderStroke(
                                        1.dp,
                                        if (selected) palette.goldAccent else MaterialTheme.colorScheme.outline
                                    ),
                                    modifier = Modifier
                                        .weight(1f)
                                        .testTag("mode_chip_${mode.name.lowercase()}")
                                ) {
                                    Column(
                                        modifier = Modifier.padding(vertical = 10.dp, horizontal = 6.dp),
                                        horizontalAlignment = Alignment.CenterHorizontally,
                                        verticalArrangement = Arrangement.spacedBy(4.dp)
                                    ) {
                                        Icon(
                                            imageVector = modeIcon,
                                            contentDescription = null,
                                            tint = fg,
                                            modifier = Modifier.size(18.dp)
                                        )
                                        Text(
                                            text = when (mode) {
                                                GameMode.CLASSIC -> "Classic"
                                                GameMode.SPEED_ROUND -> "Speed"
                                                GameMode.EXPERT -> "Expert"
                                            },
                                            style = MaterialTheme.typography.labelLarge,
                                            fontWeight = FontWeight.ExtraBold,
                                            color = fg,
                                            maxLines = 1
                                        )
                                        Text(
                                            text = "${mode.defaultTurnSeconds}s",
                                            style = MaterialTheme.typography.labelSmall,
                                            color = if (selected) palette.goldDarkInk.copy(alpha = 0.8f) else MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            }
                        }
                        Text(
                            text = state.gameMode.subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            // Impostor Count Stepper Row
            item {
                RuleActionCard(
                    title = "Impostors",
                    subtitle = "1 to $maxImpostors hidden liars this round",
                    icon = Icons.Filled.VisibilityOff,
                    iconTint = palette.impostorGlow
                ) {
                    PlumStepperControl(
                        valueText = "${state.impostorCount}",
                        canDecrease = state.impostorCount > 1,
                        canIncrease = state.impostorCount < maxImpostors,
                        onDecrease = { onSetImpostors(state.impostorCount - 1) },
                        onIncrease = { onSetImpostors(state.impostorCount + 1) },
                        decreaseTag = "decrease_impostor_button",
                        increaseTag = "increase_impostor_button",
                        valueTag = "impostor_count_value"
                    )
                }
            }

            // Turn Timer Toggle + Duration
            item {
                Surface(
                    color = MaterialTheme.colorScheme.surfaceContainer,
                    shape = RoundedCornerShape(20.dp),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Surface(
                                    color = palette.goldSurface,
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Timer,
                                        contentDescription = null,
                                        tint = palette.goldAccent,
                                        modifier = Modifier
                                            .padding(10.dp)
                                            .size(20.dp)
                                    )
                                }
                                Column {
                                    Text(
                                        text = "Turn Timer",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = if (state.timerEnabled) "${state.turnDurationSeconds}s countdown per speaker" else "Untimed relaxed discussion",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            PlumTogglePill(
                                checked = state.timerEnabled,
                                onCheckedChange = onToggleTimer,
                                modifier = Modifier.testTag("toggle_timer_switch")
                            )
                        }

                        AnimatedVisibility(
                            visible = state.timerEnabled,
                            enter = fadeIn() + expandVertically(spring(stiffness = Spring.StiffnessMediumLow)),
                            exit = fadeOut() + shrinkVertically(spring(stiffness = Spring.StiffnessMediumLow))
                        ) {
                            Row(
                                modifier = Modifier.horizontalScroll(rememberScrollState()),
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                listOf(10, 20, 30, 45, 60, 90, 120).forEach { sec ->
                                    FilterChip(
                                        selected = state.turnDurationSeconds == sec,
                                        onClick = { onSetDuration(sec) },
                                        label = { Text("${sec}s") },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = palette.goldAccent,
                                            selectedLabelColor = palette.goldDarkInk
                                        )
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // Hints & Category Labels
            item {
                RuleToggleCard(
                    title = "Agent Hints",
                    subtitle = "Provide a 1-word hint for Agents (hidden from Impostor)",
                    checked = state.hintsEnabled,
                    onCheckedChange = onToggleHints,
                    testTag = "toggle_hints_switch"
                )
            }

            item {
                RuleToggleCard(
                    title = "Category & Genre Labels",
                    subtitle = "Display the word's Category & Genre frame",
                    checked = state.categoryCluesEnabled,
                    onCheckedChange = onToggleCategoryClues,
                    testTag = "toggle_category_switch"
                )
            }

            // Special Roles Section
            item {
                Text(
                    text = "SPECIAL ROLES",
                    style = MaterialTheme.typography.labelMedium,
                    color = palette.goldAccent,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }

            item {
                RuleToggleCard(
                    title = "Agent Killer",
                    subtitle = "One impostor silences 1 player's vote during voting",
                    checked = state.impostorAbilityEnabled,
                    onCheckedChange = onToggleImpostorAbility,
                    testTag = "toggle_impostor_ability_switch",
                    roleIdentity = GameRoleIdentity.AGENT_KILLER
                )
            }

            item {
                RuleToggleCard(
                    title = "Savior Agent",
                    subtitle = "Shields 1 teammate from Agent Killer (+1 bonus if saved)",
                    checked = state.saviorAgentEnabled,
                    onCheckedChange = onToggleSaviorAgent,
                    testTag = "toggle_savior_agent_switch",
                    roleIdentity = GameRoleIdentity.SAVIOR_AGENT
                )
            }

            item {
                RuleToggleCard(
                    title = "Agent Director",
                    subtitle = "One agent's vote carries 2x weight (0 deduction)",
                    checked = state.agentDirectorEnabled,
                    onCheckedChange = onToggleAgentDirector,
                    testTag = "toggle_agent_director_switch",
                    roleIdentity = GameRoleIdentity.AGENT_DIRECTOR
                )
            }

            item {
                RuleToggleCard(
                    title = "Agent Prediction",
                    subtitle = "Agents lock a pre-discussion guess (+3/+1 or -2 pts)",
                    checked = state.agentPredictionEnabled,
                    onCheckedChange = onToggleAgentPrediction,
                    testTag = "toggle_agent_prediction_switch",
                    roleIdentity = GameRoleIdentity.AGENT
                )
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
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                GoldCtaButton(
                    text = "Pick your packs",
                    onClick = onNext,
                    modifier = Modifier.testTag("rules_next_button")
                )
                Text(
                    text = "You can change these before every round.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

private enum class PackFilterTab(val label: String) {
    ALL("All"),
    BUILT_IN("Built-in"),
    CUSTOM("Custom"),
    FAVORITES("Favorites")
}

@Composable
internal fun PacksSetupStep(
    state: LocalGameUiState,
    customCategoryName: String,
    customWords: List<WordEntry>,
    onBack: () -> Unit,
    onSelectCategory: (String) -> Unit,
    onToggleFavoritePack: (String) -> Unit,
    onStartRound: () -> Unit,
    trailingHeader: @Composable () -> Unit
) {
    val palette = LocalImpostorPalette.current
    val context = LocalContext.current
    var searchQuery by remember { mutableStateOf("") }
    var activeFilter by remember { mutableStateOf(PackFilterTab.ALL) }
    var peekCategory by remember { mutableStateOf<String?>(null) }
    var showAddCustomDialog by remember { mutableStateOf(false) }

    val allCategories = remember(customCategoryName, customWords) { WordBank.categories }
    val filteredCategories = remember(allCategories, searchQuery, activeFilter, state.favoritePacks, customWords) {
        val q = searchQuery.trim().lowercase()
        allCategories.filter { cat ->
            val isCustom = WordBank.isCustomCategory(cat)
            val isAll = cat == WordBank.CATEGORY_ALL
            val tabMatches = when (activeFilter) {
                PackFilterTab.ALL -> true
                PackFilterTab.BUILT_IN -> !isCustom
                PackFilterTab.CUSTOM -> isCustom || isAll
                PackFilterTab.FAVORITES -> cat in state.favoritePacks
            }
            if (!tabMatches) return@filter false
            if (q.isEmpty()) return@filter true
            val displayName = if (isCustom) customCategoryName else if (isAll) "All Categories Mixed" else cat
            val wordsInPack = WordBank.wordsForCategory(cat)
            displayName.lowercase().contains(q) || wordsInPack.any { it.word.lowercase().contains(q) }
        }
    }

    val selectedCount = WordBank.wordsForCategory(state.selectedCategory).size

    Column(modifier = Modifier.fillMaxSize()) {
        WizardStepHeader(
            stepLabel = "3 / YOUR PACKS",
            onBack = onBack,
            trailing = trailingHeader
        )

        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                HighlightedHeadline(
                    prefix = "Set the ",
                    highlighted = "mood.",
                    textAlign = TextAlign.Start,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Search Bar
            item {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("Search packs or words") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Search,
                            contentDescription = "Search packs",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(Icons.Filled.Close, contentDescription = "Clear search")
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainer,
                        focusedBorderColor = palette.goldAccent,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("packs_search_input")
                )
            }

            // Filter Tabs Row
            item {
                Row(
                    modifier = Modifier.horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    PackFilterTab.entries.forEach { tab ->
                        val selected = activeFilter == tab
                        FilterChip(
                            selected = selected,
                            onClick = { activeFilter = tab },
                            label = { Text(tab.label) },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = palette.goldAccent,
                                selectedLabelColor = palette.goldDarkInk
                            ),
                            modifier = Modifier.testTag("pack_filter_${tab.name.lowercase()}")
                        )
                    }
                    FilterChip(
                        selected = false,
                        onClick = { showAddCustomDialog = true },
                        leadingIcon = {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = null,
                                tint = palette.goldAccent,
                                modifier = Modifier.size(16.dp)
                            )
                        },
                        label = { Text("New Word", color = palette.goldAccent) },
                        modifier = Modifier.testTag("add_custom_word_button")
                    )
                }
            }

            if (filteredCategories.isEmpty()) {
                item {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        shape = RoundedCornerShape(20.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(24.dp),
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "No matching packs",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Clear the filter or tap a heart icon to favorite a pack.",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                textAlign = TextAlign.Center
                            )
                        }
                    }
                }
            }

            items(filteredCategories, key = { it }) { cat ->
                val isAll = cat == WordBank.CATEGORY_ALL
                val isCustom = WordBank.isCustomCategory(cat)
                val isSelected = when {
                    isAll -> state.selectedCategory == WordBank.CATEGORY_ALL
                    isCustom -> WordBank.isCustomCategory(state.selectedCategory)
                    else -> state.selectedCategory.equals(cat, ignoreCase = true)
                }
                val isFavorite = cat in state.favoritePacks
                val words = WordBank.wordsForCategory(cat)
                val visual = packVisualMeta(cat, customCategoryName)

                PackSelectionCard(
                    title = visual.title,
                    subtitle = "${words.size} words • ${visual.tagline}",
                    badgeIcon = visual.icon,
                    tileColor = visual.tileColor,
                    isSelected = isSelected,
                    isFavorite = isFavorite,
                    onSelect = { onSelectCategory(cat) },
                    onPeek = { peekCategory = cat },
                    onToggleFavorite = { onToggleFavoritePack(cat) },
                    modifier = Modifier.testTag("pack_card_${cat.lowercase().replace(" ", "_")}")
                )
            }
        }

        // Sticky Footer Summary + Launch CTA
        Surface(
            color = MaterialTheme.colorScheme.background.copy(alpha = 0.96f),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    val activePackTitle = if (state.selectedCategory == WordBank.CATEGORY_ALL) {
                        "All Packs"
                    } else if (WordBank.isCustomCategory(state.selectedCategory)) {
                        customCategoryName
                    } else {
                        state.selectedCategory
                    }
                    Text(
                        text = "Selected: $activePackTitle ($selectedCount words)",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Surface(
                        onClick = { onSelectCategory(WordBank.CATEGORY_ALL) },
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Text(
                            text = "Use All",
                            style = MaterialTheme.typography.labelMedium,
                            color = palette.goldAccent,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        )
                    }
                }
                GoldCtaButton(
                    text = "Pass the phone",
                    onClick = onStartRound,
                    modifier = Modifier.testTag("local_start_round_button")
                )
            }
        }
    }

    val openPeek = peekCategory
    if (openPeek != null) {
        PackPeekBottomSheet(
            category = openPeek,
            customCategoryName = customCategoryName,
            customWords = customWords,
            isSelected = if (WordBank.isCustomCategory(openPeek)) {
                WordBank.isCustomCategory(state.selectedCategory)
            } else {
                state.selectedCategory.equals(openPeek, ignoreCase = true)
            },
            onSelectPack = {
                onSelectCategory(openPeek)
                peekCategory = null
            },
            onRequestAddCustomWord = { showAddCustomDialog = true },
            onRenameCustomCategory = { newName ->
                val wasCustom = WordBank.isCustomCategory(state.selectedCategory)
                val saved = WordBank.setCustomCategoryName(context, newName)
                if (wasCustom) onSelectCategory(saved)
            },
            onRemoveCustomWord = { word ->
                WordBank.removeCustomWord(context, word)
            },
            onDismiss = { peekCategory = null }
        )
    }

    if (showAddCustomDialog) {
        PackAddCustomWordDialog(
            initialCategoryName = customCategoryName,
            onDismiss = { showAddCustomDialog = false },
            onConfirm = { categoryName, word, genre, hint ->
                val wasCustom = WordBank.isCustomCategory(state.selectedCategory)
                val savedCategory = WordBank.setCustomCategoryName(context, categoryName)
                WordBank.addCustomWord(
                    context = context,
                    rawWord = word,
                    rawGenre = genre,
                    rawHint = hint,
                    rawCategoryName = savedCategory
                )
                if (wasCustom || WordBank.isCustomCategory(peekCategory ?: "")) {
                    onSelectCategory(savedCategory)
                }
                showAddCustomDialog = false
            }
        )
    }
}

private data class PackVisualMeta(
    val title: String,
    val tagline: String,
    val icon: ImageVector,
    val tileColor: Color
)

private fun packVisualMeta(category: String, customCategoryName: String): PackVisualMeta = when {
    category == WordBank.CATEGORY_ALL -> PackVisualMeta(
        title = "All Categories",
        tagline = "MIXED DECK • FREE",
        icon = Icons.Filled.AutoAwesome,
        tileColor = Color(0xFF473762)
    )
    WordBank.isCustomCategory(category) -> PackVisualMeta(
        title = customCategoryName,
        tagline = "YOUR WORDS • FREE",
        icon = Icons.Filled.Style,
        tileColor = Color(0xFF4B381B)
    )
    category.equals("Food", ignoreCase = true) -> PackVisualMeta(
        title = "Food & Street Snacks",
        tagline = "FREE",
        icon = Icons.Filled.LocalDining,
        tileColor = Color(0xFF512B2B)
    )
    category.equals("Nature", ignoreCase = true) -> PackVisualMeta(
        title = "Nature & Wildlife",
        tagline = "FREE",
        icon = Icons.Filled.Park,
        tileColor = Color(0xFF1E4434)
    )
    category.equals("Culture", ignoreCase = true) -> PackVisualMeta(
        title = "Festivals & Culture",
        tagline = "FREE",
        icon = Icons.Filled.Star,
        tileColor = Color(0xFF46305E)
    )
    category.equals("Landmarks", ignoreCase = true) -> PackVisualMeta(
        title = "Iconic Landmarks",
        tagline = "FREE",
        icon = Icons.Filled.Explore,
        tileColor = Color(0xFF1F3B52)
    )
    category.equals("Bollywood", ignoreCase = true) -> PackVisualMeta(
        title = "Bollywood Stars & Hits",
        tagline = "FREE",
        icon = Icons.Filled.Movie,
        tileColor = Color(0xFF4F223A)
    )
    category.equals("World Trends", ignoreCase = true) -> PackVisualMeta(
        title = "World Trends",
        tagline = "FREE",
        icon = Icons.Filled.Public,
        tileColor = Color(0xFF23395D)
    )
    else -> PackVisualMeta(
        title = category,
        tagline = "FREE",
        icon = Icons.Filled.Style,
        tileColor = Color(0xFF392D4E)
    )
}

@Composable
private fun PackSelectionCard(
    title: String,
    subtitle: String,
    badgeIcon: ImageVector,
    tileColor: Color,
    isSelected: Boolean,
    isFavorite: Boolean,
    onSelect: () -> Unit,
    onPeek: () -> Unit,
    onToggleFavorite: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalImpostorPalette.current
    val borderColor by animateColorAsState(
        targetValue = if (isSelected) palette.goldAccent else MaterialTheme.colorScheme.outline,
        label = "pack_border"
    )
    val bgColor by animateColorAsState(
        targetValue = if (isSelected) Color(0xFF31253E) else MaterialTheme.colorScheme.surfaceContainer,
        label = "pack_bg"
    )

    Surface(
        onClick = onSelect,
        color = bgColor,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(if (isSelected) 2.dp else 1.dp, borderColor),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Surface(
                color = tileColor,
                shape = RoundedCornerShape(14.dp),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.14f)),
                modifier = Modifier.size(56.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = badgeIcon,
                        contentDescription = null,
                        tint = palette.goldAccent,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            Column(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f, fill = false)
                    )
                    Surface(
                        onClick = onPeek,
                        color = MaterialTheme.colorScheme.surfaceContainerLowest,
                        shape = RoundedCornerShape(50),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Peek",
                                style = MaterialTheme.typography.labelSmall,
                                color = palette.goldAccent,
                                fontWeight = FontWeight.Bold
                            )
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.KeyboardArrowRight,
                                contentDescription = "Peek pack words",
                                tint = palette.goldAccent,
                                modifier = Modifier.size(14.dp)
                            )
                        }
                    }
                }
            }

            IconButton(
                onClick = onToggleFavorite,
                modifier = Modifier.size(44.dp)
            ) {
                Icon(
                    imageVector = if (isFavorite) Icons.Filled.Favorite else Icons.Filled.FavoriteBorder,
                    contentDescription = if (isFavorite) "Remove favorite" else "Favorite pack",
                    tint = if (isFavorite) palette.impostorGlow else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Surface(
                color = if (isSelected) palette.goldAccent else MaterialTheme.colorScheme.surfaceContainerLowest,
                shape = CircleShape,
                border = BorderStroke(
                    1.2.dp,
                    if (isSelected) palette.goldAccent else MaterialTheme.colorScheme.outline
                ),
                modifier = Modifier.size(34.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isSelected) Icons.Filled.Check else Icons.Filled.Add,
                        contentDescription = if (isSelected) "Selected" else "Select pack",
                        tint = if (isSelected) palette.goldDarkInk else MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PackPeekBottomSheet(
    category: String,
    customCategoryName: String,
    customWords: List<WordEntry>,
    isSelected: Boolean,
    onSelectPack: () -> Unit,
    onRequestAddCustomWord: () -> Unit,
    onRenameCustomCategory: (String) -> Unit,
    onRemoveCustomWord: (String) -> Unit,
    onDismiss: () -> Unit
) {
    val palette = LocalImpostorPalette.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val isCustom = WordBank.isCustomCategory(category)
    val words = if (isCustom) customWords else WordBank.wordsForCategory(category)
    var showRenameDialog by remember { mutableStateOf(false) }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = Modifier.testTag("pack_peek_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 20.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (isCustom) customCategoryName else category,
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "${words.size} words in deck",
                        style = MaterialTheme.typography.bodySmall,
                        color = palette.goldAccent
                    )
                }
                if (isCustom) {
                    IconButton(onClick = { showRenameDialog = true }, modifier = Modifier.size(48.dp)) {
                        Icon(Icons.Filled.Edit, contentDescription = "Rename custom pack", tint = palette.goldAccent)
                    }
                    OutlinedButton(
                        onClick = onRequestAddCustomWord,
                        shape = RoundedCornerShape(12.dp),
                        border = BorderStroke(1.dp, palette.goldAccent)
                    ) {
                        Icon(Icons.Filled.Add, contentDescription = null, tint = palette.goldAccent, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("Add", color = palette.goldAccent)
                    }
                }
            }

            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(260.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                if (words.isEmpty()) {
                    item {
                        Text(
                            text = "No words yet. Tap + Add above to build your own deck!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    items(words.take(40), key = { "${it.word}_${it.genre}" }) { entry ->
                        Surface(
                            color = MaterialTheme.colorScheme.surfaceContainer,
                            shape = RoundedCornerShape(14.dp),
                            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Row(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp),
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = entry.word,
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold
                                    )
                                    Text(
                                        text = "${entry.genre} • Hint: ${entry.hint}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                                if (isCustom) {
                                    IconButton(
                                        onClick = { onRemoveCustomWord(entry.word) },
                                        modifier = Modifier.size(40.dp)
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Close,
                                            contentDescription = "Remove ${entry.word}",
                                            tint = palette.impostorGlow
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }

            GoldCtaButton(
                text = if (isSelected) "Currently Selected" else "Use This Pack",
                onClick = onSelectPack,
                showArrow = false
            )
        }
    }

    if (showRenameDialog) {
        var draft by remember(customCategoryName) { mutableStateOf(customCategoryName) }
        AlertDialog(
            onDismissRequest = { showRenameDialog = false },
            title = { Text("Name Custom Pack") },
            text = {
                OutlinedTextField(
                    value = draft,
                    onValueChange = { draft = it.take(30) },
                    singleLine = true,
                    label = { Text("Pack Name") },
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        onRenameCustomCategory(draft)
                        showRenameDialog = false
                    },
                    enabled = draft.trim().isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(containerColor = palette.goldAccent, contentColor = palette.goldDarkInk)
                ) {
                    Text("Save")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRenameDialog = false }) { Text("Cancel") }
            }
        )
    }
}

@Composable
private fun PackAddCustomWordDialog(
    initialCategoryName: String,
    onDismiss: () -> Unit,
    onConfirm: (categoryName: String, word: String, genre: String, hint: String) -> Unit
) {
    val palette = LocalImpostorPalette.current
    var categoryDraft by remember(initialCategoryName) { mutableStateOf(initialCategoryName) }
    var wordDraft by remember { mutableStateOf("") }
    var genreDraft by remember { mutableStateOf("") }
    var hintDraft by remember { mutableStateOf("") }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Add Custom Word") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                OutlinedTextField(
                    value = categoryDraft,
                    onValueChange = { categoryDraft = it.take(30) },
                    label = { Text("Custom Pack Name") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("custom_category_name_input")
                )
                OutlinedTextField(
                    value = wordDraft,
                    onValueChange = { wordDraft = it.take(40) },
                    label = { Text("Secret Word") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("custom_word_input")
                )
                OutlinedTextField(
                    value = genreDraft,
                    onValueChange = { genreDraft = it.take(40) },
                    label = { Text("Genre") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("custom_genre_input")
                )
                OutlinedTextField(
                    value = hintDraft,
                    onValueChange = { hintDraft = it.take(60) },
                    label = { Text("Hint (1 word recommended)") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("custom_hint_input")
                )
            }
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(categoryDraft, wordDraft, genreDraft, hintDraft) },
                enabled = wordDraft.trim().isNotEmpty(),
                colors = ButtonDefaults.buttonColors(containerColor = palette.goldAccent, contentColor = palette.goldDarkInk),
                modifier = Modifier.testTag("confirm_add_custom_word_button")
            ) {
                Text("Add Word")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) { Text("Cancel") }
        }
    )
}

@Composable
private fun RuleActionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    iconTint: Color,
    trailing: @Composable () -> Unit
) {
    val palette = LocalImpostorPalette.current
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 10.dp)
            ) {
                Surface(
                    color = palette.goldSurface,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier
                            .padding(10.dp)
                            .size(20.dp)
                    )
                }
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            trailing()
        }
    }
}

@Composable
private fun RuleToggleCard(
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    testTag: String,
    roleIdentity: GameRoleIdentity? = null
) {
    val palette = LocalImpostorPalette.current
    val roleStyle = roleIdentity?.style(palette)

    Surface(
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = RoundedCornerShape(20.dp),
        border = BorderStroke(
            width = if (checked && roleStyle != null) 1.4.dp else 1.dp,
            color = if (checked && roleStyle != null) roleStyle.glow.copy(alpha = 0.65f) else MaterialTheme.colorScheme.outline
        ),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            if (roleIdentity != null) {
                RoleEmblemBadge(
                    identity = roleIdentity,
                    size = 34.dp,
                    animated = checked,
                    modifier = Modifier.padding(end = 12.dp)
                )
            }
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp)
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (checked && roleStyle != null) roleStyle.glow else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            PlumTogglePill(
                checked = checked,
                onCheckedChange = onCheckedChange,
                modifier = Modifier.testTag(testTag)
            )
        }
    }
}
