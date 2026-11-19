package com.example.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.GridItemSpan
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Style
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
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
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.data.AvatarCatalog
import com.example.data.AvatarCategoryFilter
import com.example.data.AvatarPose
import com.example.data.LocalPlayer
import com.example.ui.theme.LocalImpostorPalette

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AvatarPickerSheet(
    players: List<LocalPlayer>,
    initialPlayerId: String?,
    onSelectAvatarForPlayer: (playerId: String, avatarId: String, pose: AvatarPose) -> Unit,
    onDismiss: () -> Unit
) {
    val palette = LocalImpostorPalette.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    var activePlayerId by remember(initialPlayerId, players) {
        mutableStateOf(
            players.firstOrNull { it.id == initialPlayerId }?.id
                ?: players.firstOrNull()?.id.orEmpty()
        )
    }
    val activePlayer = players.firstOrNull { it.id == activePlayerId } ?: players.firstOrNull()

    var previewAvatarId by remember(activePlayerId) {
        mutableStateOf(activePlayer?.avatarId ?: AvatarCatalog.all.first().id)
    }
    var previewPose by remember(activePlayerId) {
        mutableStateOf(activePlayer?.avatarPose ?: AvatarPose.ORIGINAL)
    }
    var activeFilter by remember { mutableStateOf(AvatarCategoryFilter.ALL) }

    val previewCharacter = AvatarCatalog.findById(previewAvatarId)
    val filteredCharacters = remember(activeFilter) {
        when (activeFilter) {
            AvatarCategoryFilter.ALL -> AvatarCatalog.all
            else -> AvatarCatalog.all.filter { it.filterGroup == activeFilter }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        contentColor = MaterialTheme.colorScheme.onSurface,
        modifier = Modifier.testTag("avatar_picker_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .fillMaxHeight(0.92f)
                .padding(horizontal = 18.dp)
        ) {
            // Sheet Header
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Surface(
                        color = previewCharacter.accentColor.copy(alpha = 0.20f),
                        shape = RoundedCornerShape(10.dp),
                        border = BorderStroke(1.dp, previewCharacter.glowColor.copy(alpha = 0.6f))
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Style,
                            contentDescription = null,
                            tint = previewCharacter.glowColor,
                            modifier = Modifier
                                .padding(8.dp)
                                .size(20.dp)
                        )
                    }
                    Column {
                        Text(
                            text = "CHARACTER AVATAR DECK",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "22 Heroes • Cutout Sprites • 4 Anime Poses Each",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(48.dp)
                ) {
                    Icon(Icons.Filled.Close, contentDescription = "Close avatar deck")
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Player Target Selector Chips
            if (players.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    players.forEach { player ->
                        val selected = player.id == activePlayerId
                        val pChar = AvatarCatalog.findById(player.avatarId)
                        FilterChip(
                            selected = selected,
                            onClick = {
                                activePlayerId = player.id
                                previewAvatarId = player.avatarId
                                previewPose = player.avatarPose
                            },
                            leadingIcon = {
                                CharacterAvatarThumbnail(
                                    character = pChar,
                                    pose = player.avatarPose,
                                    size = 22.dp,
                                    isHighlighted = selected
                                )
                            },
                            label = {
                                Text(
                                    text = player.name,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                            },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = pChar.surfaceTint,
                                selectedLabelColor = pChar.glowColor
                            ),
                            modifier = Modifier.testTag("avatar_sheet_player_${player.id}")
                        )
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
            }

            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                modifier = Modifier.weight(1f),
                contentPadding = PaddingValues(bottom = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                // Featured Live Preview Card + 4-Pose Switcher
                item(span = { GridItemSpan(2) }) {
                    Surface(
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        shape = RoundedCornerShape(22.dp),
                        border = BorderStroke(1.dp, previewCharacter.glowColor.copy(alpha = 0.45f)),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(
                            modifier = Modifier.padding(14.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(14.dp),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                CharacterAvatarCard(
                                    character = previewCharacter,
                                    pose = previewPose,
                                    isSelected = true,
                                    compact = false,
                                    modifier = Modifier
                                        .width(156.dp)
                                        .testTag("avatar_preview_card")
                                )

                                Column(
                                    modifier = Modifier.weight(1f),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    Text(
                                        text = "POSE & GRAPHICS",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = previewCharacter.glowColor
                                    )
                                    AvatarPose.entries.forEach { poseOption ->
                                        val isCurrentPose = previewPose == poseOption
                                        Surface(
                                            onClick = { previewPose = poseOption },
                                            color = if (isCurrentPose) {
                                                previewCharacter.accentColor.copy(alpha = 0.24f)
                                            } else {
                                                MaterialTheme.colorScheme.surfaceContainerLow
                                            },
                                            shape = RoundedCornerShape(12.dp),
                                            border = BorderStroke(
                                                width = if (isCurrentPose) 1.5.dp else 1.dp,
                                                color = if (isCurrentPose) {
                                                    previewCharacter.glowColor
                                                } else {
                                                    MaterialTheme.colorScheme.outlineVariant
                                                }
                                            ),
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .testTag("pose_option_${poseOption.name.lowercase()}")
                                        ) {
                                            Row(
                                                modifier = Modifier.padding(horizontal = 10.dp, vertical = 8.dp),
                                                verticalAlignment = Alignment.CenterVertically,
                                                horizontalArrangement = Arrangement.SpaceBetween
                                            ) {
                                                Column(modifier = Modifier.weight(1f)) {
                                                    Row(
                                                        verticalAlignment = Alignment.CenterVertically,
                                                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                                                    ) {
                                                        Text(
                                                            text = poseOption.label,
                                                            style = MaterialTheme.typography.labelMedium,
                                                            fontWeight = FontWeight.Bold,
                                                            color = if (isCurrentPose) {
                                                                previewCharacter.glowColor
                                                            } else {
                                                                MaterialTheme.colorScheme.onSurface
                                                            }
                                                        )
                                                        if (poseOption != AvatarPose.ORIGINAL) {
                                                            Icon(
                                                                imageVector = Icons.Filled.AutoAwesome,
                                                                contentDescription = "Custom anime pose",
                                                                tint = previewCharacter.glowColor,
                                                                modifier = Modifier.size(12.dp)
                                                            )
                                                        }
                                                    }
                                                }
                                                if (isCurrentPose) {
                                                    Icon(
                                                        imageVector = Icons.Filled.Check,
                                                        contentDescription = "Selected pose",
                                                        tint = previewCharacter.glowColor,
                                                        modifier = Modifier.size(16.dp)
                                                    )
                                                }
                                            }
                                        }
                                    }
                                }
                            }

                            if (activePlayer != null) {
                                Button(
                                    onClick = {
                                        onSelectAvatarForPlayer(activePlayer.id, previewCharacter.id, previewPose)
                                        onDismiss()
                                    },
                                    shape = RoundedCornerShape(14.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = previewCharacter.accentColor,
                                        contentColor = Color.White
                                    ),
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(48.dp)
                                        .testTag("equip_avatar_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = null,
                                        modifier = Modifier.padding(end = 6.dp)
                                    )
                                    Text(
                                        text = "Equip ${previewCharacter.name} (${previewPose.label}) for ${activePlayer.name}",
                                        style = MaterialTheme.typography.labelLarge,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                }
                            }
                        }
                    }
                }

                // Filter Chips Bar
                item(span = { GridItemSpan(2) }) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState()),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        AvatarCategoryFilter.entries.forEach { filter ->
                            FilterChip(
                                selected = activeFilter == filter,
                                onClick = { activeFilter = filter },
                                label = { Text(filter.label) },
                                colors = FilterChipDefaults.filterChipColors(
                                    selectedContainerColor = palette.cyanSurface,
                                    selectedLabelColor = palette.cyanAccent
                                ),
                                modifier = Modifier.testTag("avatar_filter_${filter.name.lowercase()}")
                            )
                        }
                    }
                }

                // Grid of All 22 Character Avatar Cards
                items(filteredCharacters, key = { it.id }) { character ->
                    val isPicked = character.id == previewAvatarId
                    val cardPose = if (isPicked) previewPose else AvatarPose.ORIGINAL
                    Box(modifier = Modifier.fillMaxWidth()) {
                        CharacterAvatarCard(
                            character = character,
                            pose = cardPose,
                            isSelected = isPicked,
                            compact = true,
                            onClick = {
                                previewAvatarId = character.id
                                if (activePlayer != null) {
                                    onSelectAvatarForPlayer(activePlayer.id, character.id, previewPose)
                                }
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("avatar_grid_card_${character.id}")
                        )
                    }
                }
            }
        }
    }
}
