package com.example.ui.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.AssistChip
import androidx.compose.material3.AssistChipDefaults
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.WordBank
import com.example.data.WordEntry
import com.example.ui.theme.LocalImpostorPalette

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun WordCategoryBox(
    selectedCategory: String,
    customCategoryName: String,
    customWords: List<WordEntry>,
    canEdit: Boolean = true,
    onSelectCategory: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val palette = LocalImpostorPalette.current
    var showAddDialog by remember { mutableStateOf(false) }
    var showRenameCategoryDialog by remember { mutableStateOf(false) }

    val isAllSelected = selectedCategory == WordBank.CATEGORY_ALL
    val isCustomSelected = WordBank.isCustomCategory(selectedCategory)
    val categories = remember(customCategoryName) { WordBank.categories }

    val poolBadgeText = when {
        isAllSelected -> "${WordBank.builtInCount + customWords.size} words"
        isCustomSelected -> "${customWords.size} custom"
        else -> selectedCategory
    }

    GlassSectionCard(
        title = "WORD CATEGORY",
        icon = Icons.Filled.Category,
        accentColor = if (isCustomSelected) palette.cyanAccent else palette.normalAccent,
        trailing = {
            Surface(
                color = (if (isCustomSelected) palette.cyanAccent else palette.normalAccent).copy(alpha = 0.14f),
                shape = RoundedCornerShape(50),
                border = BorderStroke(
                    1.dp,
                    (if (isCustomSelected) palette.cyanAccent else palette.normalAccent).copy(alpha = 0.4f)
                )
            ) {
                AnimatedContent(
                    targetState = poolBadgeText,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "pool_badge"
                ) { label ->
                    Text(
                        text = label,
                        style = MaterialTheme.typography.labelSmall,
                        color = if (isCustomSelected) palette.cyanAccent else palette.normalGlow,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                    )
                }
            }
        },
        modifier = modifier
    ) {
        // Category selection chips (Built-in + Named Custom + All incl. Custom + quick Add)
        FlowRow(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            categories.forEach { cat ->
                val isAll = cat == WordBank.CATEGORY_ALL
                val isCustom = WordBank.isCustomCategory(cat)
                val selected = when {
                    isAll -> isAllSelected
                    isCustom -> isCustomSelected
                    else -> selectedCategory.equals(cat, ignoreCase = true)
                }
                val chipLabel = when {
                    isAll && customWords.isNotEmpty() -> "All (+${customWords.size} Custom)"
                    isAll -> "All Categories"
                    isCustom -> "$customCategoryName (${customWords.size})"
                    else -> cat
                }
                val activeContainer = if (isCustom) palette.cyanSurface else palette.normalSurface
                val activeText = if (isCustom) palette.cyanAccent else palette.normalGlow

                FilterChip(
                    selected = selected,
                    onClick = { if (canEdit) onSelectCategory(cat) },
                    enabled = canEdit,
                    leadingIcon = if (selected) {
                        {
                            Icon(
                                imageVector = Icons.Filled.Check,
                                contentDescription = null,
                                tint = activeText,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    } else null,
                    label = { Text(chipLabel) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = activeContainer,
                        selectedLabelColor = activeText,
                        disabledSelectedContainerColor = activeContainer.copy(alpha = 0.7f),
                        disabledLabelColor = if (selected) activeText.copy(alpha = 0.85f) else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
            }

            if (canEdit) {
                AssistChip(
                    onClick = { showAddDialog = true },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Filled.Add,
                            contentDescription = "Add custom word",
                            tint = palette.cyanAccent,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    label = {
                        Text(
                            text = "Add Word",
                            color = palette.cyanAccent
                        )
                    },
                    border = BorderStroke(1.dp, palette.cyanAccent.copy(alpha = 0.65f)),
                    colors = AssistChipDefaults.assistChipColors(
                        containerColor = palette.cyanSurface.copy(alpha = 0.45f)
                    ),
                    modifier = Modifier.testTag("category_chip_add_word")
                )
            }
        }

        // Clear notice when All is selected and custom words are present
        AnimatedVisibility(
            visible = isAllSelected && customWords.isNotEmpty(),
            enter = fadeIn() + expandVertically(),
            exit = fadeOut() + shrinkVertically()
        ) {
            Surface(
                color = palette.normalSurface.copy(alpha = 0.5f),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, palette.normalAccent.copy(alpha = 0.35f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(
                        imageVector = Icons.Filled.AutoAwesome,
                        contentDescription = null,
                        tint = palette.normalGlow,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = "All Categories active — ${WordBank.builtInCount} built-in + ${customWords.size} \"$customCategoryName\" words included.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.normalGlow
                    )
                }
            }
        }

        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.7f))

        // Unified nested box containing ALL custom words + custom category naming
        val customBoxBorderColor by animateColorAsState(
            targetValue = when {
                isCustomSelected -> palette.cyanAccent
                customWords.isNotEmpty() -> palette.cyanAccent.copy(alpha = 0.45f)
                else -> MaterialTheme.colorScheme.outlineVariant
            },
            label = "custom_box_border"
        )

        Surface(
            color = MaterialTheme.colorScheme.surfaceContainerLowest.copy(alpha = 0.75f),
            shape = RoundedCornerShape(16.dp),
            border = BorderStroke(1.dp, customBoxBorderColor),
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(
                    animationSpec = spring(
                        dampingRatio = Spring.DampingRatioNoBouncy,
                        stiffness = Spring.StiffnessMediumLow
                    )
                )
        ) {
            Column(
                modifier = Modifier.padding(14.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Column(modifier = Modifier.weight(1f, fill = false)) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(6.dp)
                            ) {
                                Text(
                                    text = customCategoryName.uppercase(),
                                    style = MaterialTheme.typography.labelMedium,
                                    color = palette.cyanAccent
                                )
                                Surface(
                                    color = palette.cyanSurface,
                                    shape = RoundedCornerShape(50)
                                ) {
                                    Text(
                                        text = "${customWords.size}",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = palette.cyanAccent,
                                        modifier = Modifier.padding(horizontal = 7.dp, vertical = 2.dp)
                                    )
                                }
                                if (canEdit) {
                                    IconButton(
                                        onClick = { showRenameCategoryDialog = true },
                                        modifier = Modifier
                                            .size(32.dp)
                                            .testTag("rename_custom_category_button")
                                    ) {
                                        Icon(
                                            imageVector = Icons.Filled.Edit,
                                            contentDescription = "Name custom category",
                                            tint = palette.cyanAccent,
                                            modifier = Modifier.size(15.dp)
                                        )
                                    }
                                }
                            }
                            Text(
                                text = "All custom words in one box — tap pencil to name category",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }

                    if (canEdit) {
                        Spacer(modifier = Modifier.width(8.dp))
                        OutlinedButton(
                            onClick = { showAddDialog = true },
                            shape = RoundedCornerShape(12.dp),
                            border = BorderStroke(1.dp, palette.cyanAccent),
                            contentPadding = PaddingValues(horizontal = 12.dp, vertical = 6.dp),
                            modifier = Modifier
                                .height(42.dp)
                                .testTag("add_custom_word_button")
                        ) {
                            Icon(
                                imageVector = Icons.Filled.Add,
                                contentDescription = "Add custom word",
                                tint = palette.cyanAccent,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text("Add", color = palette.cyanAccent)
                        }
                    }
                }

                if (customWords.isEmpty()) {
                    Text(
                        text = "No custom words yet. Tap + Add to add your own secret word, genre & 1-word hint.",
                        style = MaterialTheme.typography.bodyMedium,
                        color = palette.textMuted
                    )
                } else {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        customWords.forEach { entry ->
                            Surface(
                                color = MaterialTheme.colorScheme.surfaceContainer,
                                shape = RoundedCornerShape(12.dp),
                                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        Text(
                                            text = entry.word,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.SemiBold,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )
                                        val meta = buildString {
                                            append("Genre: ${entry.genre}")
                                            if (entry.hint.isNotBlank()) {
                                                append(" • Hint: ${entry.hint}")
                                            }
                                        }
                                        Text(
                                            text = meta,
                                            style = MaterialTheme.typography.bodyMedium,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                    if (canEdit) {
                                        IconButton(
                                            onClick = { WordBank.removeCustomWord(context, entry.word) },
                                            modifier = Modifier
                                                .size(40.dp)
                                                .testTag("remove_custom_${entry.word}")
                                        ) {
                                            Icon(
                                                imageVector = Icons.Filled.Close,
                                                contentDescription = "Remove ${entry.word}",
                                                tint = palette.impostorAccent
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    if (canEdit) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = { onSelectCategory(WordBank.CATEGORY_ALL) },
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(
                                    1.dp,
                                    if (isAllSelected) palette.normalAccent else MaterialTheme.colorScheme.outlineVariant
                                ),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .testTag("select_all_with_custom_button")
                            ) {
                                Text(
                                    text = "All + Custom",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (isAllSelected) palette.normalGlow else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            OutlinedButton(
                                onClick = { onSelectCategory(customCategoryName) },
                                shape = RoundedCornerShape(10.dp),
                                border = BorderStroke(
                                    1.dp,
                                    if (isCustomSelected) palette.cyanAccent else MaterialTheme.colorScheme.outlineVariant
                                ),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                modifier = Modifier
                                    .weight(1f)
                                    .height(40.dp)
                                    .testTag("select_custom_category_button")
                            ) {
                                Text(
                                    text = "$customCategoryName Only",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = if (isCustomSelected) palette.cyanAccent else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    if (showAddDialog) {
        AddCustomWordDialog(
            initialCategoryName = customCategoryName,
            onDismiss = { showAddDialog = false },
            onConfirm = { categoryName, word, genre, hint ->
                val wasCustomSelected = isCustomSelected
                val updatedCategoryName = WordBank.setCustomCategoryName(context, categoryName)
                WordBank.addCustomWord(
                    context = context,
                    rawWord = word,
                    rawGenre = genre,
                    rawHint = hint,
                    rawCategoryName = updatedCategoryName
                )
                if (wasCustomSelected) {
                    onSelectCategory(updatedCategoryName)
                }
                showAddDialog = false
            }
        )
    }

    if (showRenameCategoryDialog) {
        RenameCustomCategoryDialog(
            currentName = customCategoryName,
            onDismiss = { showRenameCategoryDialog = false },
            onConfirm = { newName ->
                val wasCustomSelected = isCustomSelected
                val saved = WordBank.setCustomCategoryName(context, newName)
                if (wasCustomSelected) {
                    onSelectCategory(saved)
                }
                showRenameCategoryDialog = false
            }
        )
    }
}

@Composable
private fun AddCustomWordDialog(
    initialCategoryName: String,
    onDismiss: () -> Unit,
    onConfirm: (categoryName: String, word: String, genre: String, hint: String) -> Unit
) {
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
                    label = { Text("Custom Category Name") },
                    placeholder = { Text("e.g., Squad, Movies, Anime") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("custom_category_name_input")
                )
                OutlinedTextField(
                    value = wordDraft,
                    onValueChange = { wordDraft = it.take(40) },
                    label = { Text("Secret Word") },
                    placeholder = { Text("e.g., Biriyani, Pathaan") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("custom_word_input")
                )
                OutlinedTextField(
                    value = genreDraft,
                    onValueChange = { genreDraft = it.take(40) },
                    label = { Text("Genre") },
                    placeholder = { Text("e.g., Cuisine, Cinema") },
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("custom_genre_input")
                )
                OutlinedTextField(
                    value = hintDraft,
                    onValueChange = { hintDraft = it.take(60) },
                    label = { Text("Hint / Clue (1 word recommended)") },
                    placeholder = { Text("e.g., Steam, Vault") },
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
                modifier = Modifier.testTag("confirm_add_custom_word_button")
            ) {
                Text("Add Word")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}

@Composable
private fun RenameCustomCategoryDialog(
    currentName: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    var nameDraft by remember(currentName) { mutableStateOf(currentName) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Name Custom Category") },
        text = {
            OutlinedTextField(
                value = nameDraft,
                onValueChange = { nameDraft = it.take(30) },
                label = { Text("Category Name") },
                placeholder = { Text("e.g., Squad, Movies, Anime") },
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("rename_custom_category_input")
            )
        },
        confirmButton = {
            Button(
                onClick = { onConfirm(nameDraft) },
                enabled = nameDraft.trim().isNotEmpty(),
                modifier = Modifier.testTag("confirm_rename_custom_category_button")
            ) {
                Text("Save Name")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel")
            }
        }
    )
}
