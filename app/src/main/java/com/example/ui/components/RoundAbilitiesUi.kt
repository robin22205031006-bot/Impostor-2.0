package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.LocalImpostorPalette

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun UltimateKillerAbilityCard(
    candidates: List<Pair<String, String>>,
    selectedTargetId: String?,
    onSelectTarget: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalImpostorPalette.current
    val style = GameRoleIdentity.AGENT_KILLER.style(palette)
    val selectedName = candidates.firstOrNull { it.first == selectedTargetId }?.second

    Box(
        modifier = modifier
            .fillMaxWidth()
            .drawBehind {
                val spread = 10.dp.toPx()
                drawRoundRect(
                    brush = Brush.radialGradient(
                        colors = listOf(style.glow.copy(alpha = 0.22f), Color.Transparent),
                        radius = maxOf(size.width, size.height) * 0.72f
                    ),
                    cornerRadius = CornerRadius(24.dp.toPx(), 24.dp.toPx()),
                    size = size.copy(width = size.width + spread, height = size.height + spread),
                    topLeft = Offset(-spread / 2f, -spread / 2f)
                )
            }
            .testTag("ultimate_killer_card")
    ) {
        Surface(
            color = style.deepSurface.copy(alpha = 0.92f),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.5.dp, style.glow.copy(alpha = 0.85f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    RoleEmblemBadge(
                        identity = GameRoleIdentity.AGENT_KILLER,
                        size = 38.dp,
                        animated = true
                    )
                    Text(
                        text = "ULTIMATE AGENT KILLER",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = style.glow
                    )
                }

                Text(
                    text = "Tap one player below to void their vote. If they vote for an impostor during voting, their vote will not be counted!",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = selectedTargetId == null,
                        onClick = { onSelectTarget(null) },
                        label = { Text("No Target") },
                        leadingIcon = if (selectedTargetId == null) {
                            {
                                Icon(
                                    imageVector = Icons.Filled.Block,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            selectedLabelColor = MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.testTag("killer_skip_chip")
                    )

                    candidates.forEach { (id, name) ->
                        val isSelected = id == selectedTargetId
                        val chipBg by animateColorAsState(
                            targetValue = if (isSelected) style.accent else MaterialTheme.colorScheme.surfaceContainer,
                            animationSpec = tween(220),
                            label = "killer_chip_bg"
                        )
                        FilterChip(
                            selected = isSelected,
                            onClick = { onSelectTarget(id) },
                            label = { Text(name) },
                            leadingIcon = if (isSelected) {
                                {
                                    Icon(
                                        imageVector = Icons.Filled.GpsFixed,
                                        contentDescription = "Silenced target",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = chipBg,
                                selectedLabelColor = Color.White,
                                selectedLeadingIconColor = Color.White
                            ),
                            modifier = Modifier.testTag("killer_target_$id")
                        )
                    }
                }

                Text(
                    text = if (selectedName != null) {
                        "Locked Target: $selectedName's vote will be voided."
                    } else {
                        "Tap a player's name to silence their vote (optional)."
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = if (selectedName != null) style.glow else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun AgentPredictionCard(
    candidates: List<Pair<String, String>>,
    selectedSuspectId: String?,
    onSelectSuspect: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalImpostorPalette.current
    val style = GameRoleIdentity.AGENT.style(palette)
    val selectedName = candidates.firstOrNull { it.first == selectedSuspectId }?.second

    Box(
        modifier = modifier
            .fillMaxWidth()
            .drawBehind {
                val spread = 8.dp.toPx()
                drawRoundRect(
                    brush = Brush.radialGradient(
                        colors = listOf(style.glow.copy(alpha = 0.16f), Color.Transparent),
                        radius = maxOf(size.width, size.height) * 0.72f
                    ),
                    cornerRadius = CornerRadius(24.dp.toPx(), 24.dp.toPx()),
                    size = size.copy(width = size.width + spread, height = size.height + spread),
                    topLeft = Offset(-spread / 2f, -spread / 2f)
                )
            }
            .testTag("agent_prediction_card")
    ) {
        Surface(
            color = style.deepSurface.copy(alpha = 0.86f),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.5.dp, style.accent.copy(alpha = 0.85f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    RoleEmblemBadge(
                        identity = GameRoleIdentity.AGENT,
                        size = 34.dp,
                        animated = true
                    )
                    Text(
                        text = "IMPOSTOR PREDICTION (OPTIONAL)",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = style.glow
                    )
                }

                Text(
                    text = "Lock an early impostor call or skip:\n• Right prediction + right vote = +3 pts (+1 if vote misses)\n• Wrong prediction = -2 pts (0 pts total if vote hits)\n• Skip prediction = 0 pts (+2 if vote hits)",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = selectedSuspectId == null,
                        onClick = { onSelectSuspect(null) },
                        label = { Text("Skip Prediction") },
                        leadingIcon = if (selectedSuspectId == null) {
                            {
                                Icon(
                                    imageVector = Icons.Filled.Check,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            selectedLabelColor = MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.testTag("prediction_skip_chip")
                    )

                    candidates.forEach { (id, name) ->
                        val isSelected = id == selectedSuspectId
                        val chipBg by animateColorAsState(
                            targetValue = if (isSelected) style.accent else MaterialTheme.colorScheme.surfaceContainer,
                            animationSpec = tween(220),
                            label = "prediction_chip_bg"
                        )
                        FilterChip(
                            selected = isSelected,
                            onClick = { onSelectSuspect(id) },
                            label = { Text(name) },
                            leadingIcon = if (isSelected) {
                                {
                                    Icon(
                                        imageVector = Icons.Filled.Check,
                                        contentDescription = "Predicted impostor",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = chipBg,
                                selectedLabelColor = MaterialTheme.colorScheme.onSecondary,
                                selectedLeadingIconColor = MaterialTheme.colorScheme.onSecondary
                            ),
                            modifier = Modifier.testTag("prediction_target_$id")
                        )
                    }
                }

                Text(
                    text = if (selectedName != null) {
                        "Prediction locked: $selectedName"
                    } else {
                        "No prediction selected (safe 0 pts baseline)."
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = if (selectedName != null) style.glow else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SaviorAbilityCard(
    candidates: List<Pair<String, String>>,
    selectedProtectedId: String?,
    onSelectProtected: (String?) -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalImpostorPalette.current
    val style = GameRoleIdentity.SAVIOR_AGENT.style(palette)
    val selectedName = candidates.firstOrNull { it.first == selectedProtectedId }?.second

    Box(
        modifier = modifier
            .fillMaxWidth()
            .drawBehind {
                val spread = 10.dp.toPx()
                drawRoundRect(
                    brush = Brush.radialGradient(
                        colors = listOf(style.glow.copy(alpha = 0.22f), Color.Transparent),
                        radius = maxOf(size.width, size.height) * 0.72f
                    ),
                    cornerRadius = CornerRadius(24.dp.toPx(), 24.dp.toPx()),
                    size = size.copy(width = size.width + spread, height = size.height + spread),
                    topLeft = Offset(-spread / 2f, -spread / 2f)
                )
            }
            .testTag("savior_ability_card")
    ) {
        Surface(
            color = style.deepSurface.copy(alpha = 0.92f),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.5.dp, style.glow.copy(alpha = 0.85f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    RoleEmblemBadge(
                        identity = GameRoleIdentity.SAVIOR_AGENT,
                        size = 38.dp,
                        animated = true
                    )
                    Text(
                        text = "SAVIOR AGENT — PROTECTION SHIELD",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = style.glow
                    )
                }

                Text(
                    text = "Select one agent to protect. If the Agent Killer tries to void their vote, the shield saves them (+1 bonus pt, no deduction if missed).",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )

                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = selectedProtectedId == null,
                        onClick = { onSelectProtected(null) },
                        label = { Text("No Shield (Skip)") },
                        leadingIcon = if (selectedProtectedId == null) {
                            {
                                Icon(
                                    imageVector = Icons.Filled.Block,
                                    contentDescription = null,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        } else null,
                        colors = FilterChipDefaults.filterChipColors(
                            selectedContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
                            selectedLabelColor = MaterialTheme.colorScheme.onSurface
                        ),
                        modifier = Modifier.testTag("savior_skip_chip")
                    )

                    candidates.forEach { (id, name) ->
                        val isSelected = id == selectedProtectedId
                        val chipBg by animateColorAsState(
                            targetValue = if (isSelected) style.accent else MaterialTheme.colorScheme.surfaceContainer,
                            animationSpec = tween(220),
                            label = "savior_chip_bg"
                        )
                        FilterChip(
                            selected = isSelected,
                            onClick = { onSelectProtected(id) },
                            label = { Text(name) },
                            leadingIcon = if (isSelected) {
                                {
                                    Icon(
                                        imageVector = Icons.Filled.Shield,
                                        contentDescription = "Protected player",
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            } else null,
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = chipBg,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimary,
                                selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimary
                            ),
                            modifier = Modifier.testTag("savior_target_$id")
                        )
                    }
                }

                Text(
                    text = if (selectedName != null) {
                        "Protection Shield active on: $selectedName"
                    } else {
                        "Tap a player's name to grant them your Protection Shield (optional)."
                    },
                    style = MaterialTheme.typography.labelMedium,
                    color = if (selectedName != null) style.glow else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

@Composable
fun AgentDirectorBadge(modifier: Modifier = Modifier) {
    val palette = LocalImpostorPalette.current
    val style = GameRoleIdentity.AGENT_DIRECTOR.style(palette)
    Box(
        modifier = modifier
            .fillMaxWidth()
            .drawBehind {
                val spread = 10.dp.toPx()
                drawRoundRect(
                    brush = Brush.radialGradient(
                        colors = listOf(style.glow.copy(alpha = 0.22f), Color.Transparent),
                        radius = maxOf(size.width, size.height) * 0.72f
                    ),
                    cornerRadius = CornerRadius(24.dp.toPx(), 24.dp.toPx()),
                    size = size.copy(width = size.width + spread, height = size.height + spread),
                    topLeft = Offset(-spread / 2f, -spread / 2f)
                )
            }
            .testTag("agent_director_badge")
    ) {
        Surface(
            color = style.deepSurface.copy(alpha = 0.92f),
            shape = RoundedCornerShape(20.dp),
            border = BorderStroke(1.5.dp, style.glow.copy(alpha = 0.85f)),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    RoleEmblemBadge(
                        identity = GameRoleIdentity.AGENT_DIRECTOR,
                        size = 38.dp,
                        animated = true
                    )
                    Text(
                        text = "AGENT DIRECTOR — 2X VOTE POWER",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.Bold,
                        color = style.glow
                    )
                }
                Text(
                    text = "Your vote counts as 2 votes in the tally! No deduction if your vote is wrong (only wrong start-of-round prediction deducts -2 pts).",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    }
}

@Composable
fun OtherImpostorAllyNote(modifier: Modifier = Modifier) {
    val palette = LocalImpostorPalette.current
    val style = GameRoleIdentity.IMPOSTOR.style(palette)
    Surface(
        color = style.deepSurface.copy(alpha = 0.72f),
        shape = RoundedCornerShape(16.dp),
        border = BorderStroke(1.dp, style.accent.copy(alpha = 0.65f)),
        modifier = modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            RoleEmblemBadge(
                identity = GameRoleIdentity.IMPOSTOR,
                size = 32.dp,
                animated = true
            )
            Text(
                text = "Another impostor holds the Ultimate Agent Killer ability this round.",
                style = MaterialTheme.typography.bodyMedium,
                color = style.glow,
                modifier = Modifier.weight(1f)
            )
        }
    }
}
