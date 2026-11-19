package com.example.ui.components

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.data.AvatarCharacter
import com.example.data.AvatarPose
import com.example.ui.theme.LocalImpostorPalette
import com.example.ui.theme.SpaceGroteskFontFamily

@Composable
fun ImpostorWordmark(
    modifier: Modifier = Modifier
) {
    val palette = LocalImpostorPalette.current
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            text = "IMP",
            fontFamily = SpaceGroteskFontFamily,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 20.sp,
            letterSpacing = 1.6.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        // Custom Detective Spy Hat inside the O
        Box(
            modifier = Modifier
                .padding(horizontal = 1.dp)
                .size(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                val cx = size.width / 2f
                val cy = size.height / 2f
                val r = size.minDimension * 0.44f
                drawCircle(
                    color = palette.goldAccent,
                    radius = r,
                    center = Offset(cx, cy),
                    style = Stroke(width = 2.4.dp.toPx())
                )
                // Tiny fedora brim + crown silhouette inside O
                val brimY = cy + r * 0.16f
                drawLine(
                    color = palette.goldAccent,
                    start = Offset(cx - r * 0.68f, brimY),
                    end = Offset(cx + r * 0.68f, brimY),
                    strokeWidth = 2.dp.toPx()
                )
                val crown = Path().apply {
                    moveTo(cx - r * 0.44f, brimY)
                    lineTo(cx - r * 0.28f, cy - r * 0.46f)
                    lineTo(cx, cy - r * 0.32f)
                    lineTo(cx + r * 0.28f, cy - r * 0.46f)
                    lineTo(cx + r * 0.44f, brimY)
                    close()
                }
                drawPath(crown, color = palette.goldAccent)
            }
        }
        Text(
            text = "ST",
            fontFamily = SpaceGroteskFontFamily,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 20.sp,
            letterSpacing = 1.6.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
        Text(
            text = "O",
            fontFamily = SpaceGroteskFontFamily,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 20.sp,
            letterSpacing = 1.6.sp,
            color = palette.goldAccent
        )
        Text(
            text = "R",
            fontFamily = SpaceGroteskFontFamily,
            fontWeight = FontWeight.ExtraBold,
            fontSize = 20.sp,
            letterSpacing = 1.6.sp,
            color = MaterialTheme.colorScheme.onBackground
        )
    }
}

@Composable
fun WizardStepHeader(
    stepLabel: String,
    onBack: (() -> Unit)? = null,
    backTestTag: String = "wizard_back_button",
    trailing: (@Composable () -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val palette = LocalImpostorPalette.current
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        if (onBack != null) {
            Surface(
                onClick = onBack,
                color = MaterialTheme.colorScheme.surfaceContainer,
                shape = CircleShape,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                modifier = Modifier
                    .size(48.dp)
                    .testTag(backTestTag)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        } else {
            Spacer(modifier = Modifier.size(48.dp))
        }

        Surface(
            color = MaterialTheme.colorScheme.surfaceContainer,
            shape = RoundedCornerShape(50),
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline)
        ) {
            Text(
                text = stepLabel.uppercase(),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.4.sp,
                color = palette.goldAccent,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 7.dp)
            )
        }

        if (trailing != null) {
            trailing()
        } else {
            Spacer(modifier = Modifier.size(48.dp))
        }
    }
}

@Composable
fun HighlightedHeadline(
    prefix: String,
    highlighted: String,
    suffix: String = "",
    style: TextStyle = MaterialTheme.typography.displayMedium,
    textAlign: TextAlign = TextAlign.Center,
    modifier: Modifier = Modifier
) {
    val palette = LocalImpostorPalette.current
    val annotated = buildAnnotatedString {
        withStyle(SpanStyle(color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.ExtraBold)) {
            append(prefix)
        }
        withStyle(SpanStyle(color = palette.goldAccent, fontWeight = FontWeight.ExtraBold)) {
            append(highlighted)
        }
        if (suffix.isNotEmpty()) {
            withStyle(SpanStyle(color = MaterialTheme.colorScheme.onBackground, fontWeight = FontWeight.ExtraBold)) {
                append(suffix)
            }
        }
    }
    Text(
        text = annotated,
        style = style,
        textAlign = textAlign,
        modifier = modifier
    )
}

@Composable
fun GoldCtaButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    showArrow: Boolean = true,
    containerColor: Color = LocalImpostorPalette.current.goldAccent,
    contentColor: Color = LocalImpostorPalette.current.goldDarkInk
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()
    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.97f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "cta_scale"
    )

    Button(
        onClick = onClick,
        enabled = enabled,
        interactionSource = interactionSource,
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = containerColor,
            contentColor = contentColor,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceContainerHigh,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        contentPadding = PaddingValues(horizontal = 20.dp),
        modifier = modifier
            .fillMaxWidth()
            .height(56.dp)
            .graphicsLayer {
                scaleX = scale
                scaleY = scale
            }
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold
        )
        if (showArrow) {
            Spacer(modifier = Modifier.width(8.dp))
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = null,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
fun PlumTogglePill(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalImpostorPalette.current
    val bg by animateColorAsState(
        targetValue = if (checked) palette.goldAccent else MaterialTheme.colorScheme.surfaceContainerLowest,
        animationSpec = tween(220),
        label = "pill_bg"
    )
    val textColor by animateColorAsState(
        targetValue = if (checked) palette.goldDarkInk else MaterialTheme.colorScheme.onSurfaceVariant,
        animationSpec = tween(220),
        label = "pill_text"
    )
    val borderColor by animateColorAsState(
        targetValue = if (checked) palette.goldAccent else MaterialTheme.colorScheme.outline,
        animationSpec = tween(220),
        label = "pill_border"
    )

    Surface(
        onClick = { onCheckedChange(!checked) },
        color = bg,
        shape = RoundedCornerShape(50),
        border = BorderStroke(1.2.dp, borderColor),
        modifier = modifier.height(48.dp)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier.padding(horizontal = 20.dp)
        ) {
            Text(
                text = if (checked) "ON" else "OFF",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.ExtraBold,
                color = textColor
            )
        }
    }
}

@Composable
fun PlumStepperControl(
    valueText: String,
    canDecrease: Boolean,
    canIncrease: Boolean,
    onDecrease: () -> Unit,
    onIncrease: () -> Unit,
    decreaseTag: String = "stepper_decrease",
    increaseTag: String = "stepper_increase",
    valueTag: String = "stepper_value",
    modifier: Modifier = Modifier
) {
    val palette = LocalImpostorPalette.current
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Surface(
            onClick = { if (canDecrease) onDecrease() },
            enabled = canDecrease,
            color = MaterialTheme.colorScheme.surfaceContainerLowest,
            shape = CircleShape,
            border = BorderStroke(
                1.dp,
                if (canDecrease) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            ),
            modifier = Modifier
                .size(48.dp)
                .testTag(decreaseTag)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "−",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (canDecrease) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                )
            }
        }

        Text(
            text = valueText,
            style = MaterialTheme.typography.titleLarge,
            fontWeight = FontWeight.ExtraBold,
            color = palette.goldAccent,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(horizontal = 6.dp)
                .testTag(valueTag)
        )

        Surface(
            onClick = { if (canIncrease) onIncrease() },
            enabled = canIncrease,
            color = MaterialTheme.colorScheme.surfaceContainerLowest,
            shape = CircleShape,
            border = BorderStroke(
                1.dp,
                if (canIncrease) MaterialTheme.colorScheme.outline else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f)
            ),
            modifier = Modifier
                .size(48.dp)
                .testTag(increaseTag)
        ) {
            Box(contentAlignment = Alignment.Center) {
                Text(
                    text = "+",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = if (canIncrease) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                )
            }
        }
    }
}

@Composable
fun FloatingHeroSprite(
    character: AvatarCharacter,
    pose: AvatarPose,
    modifier: Modifier = Modifier,
    size: Dp = 170.dp,
    bobOffsetDp: Float = 4f,
    phaseOffset: Float = 0f,
    isFocal: Boolean = true
) {
    val transition = rememberInfiniteTransition(label = "hero_float_${character.id}")
    val wave by transition.animateFloat(
        initialValue = 0f,
        targetValue = (2 * Math.PI).toFloat(),
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "float_wave"
    )
    val bobY = kotlin.math.sin((wave + phaseOffset).toDouble()).toFloat() * bobOffsetDp

    Box(
        modifier = modifier.size(size),
        contentAlignment = Alignment.BottomCenter
    ) {
        if (isFocal) {
            Canvas(modifier = Modifier.fillMaxSize()) {
                drawOval(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            character.glowColor.copy(alpha = 0.34f),
                            character.accentColor.copy(alpha = 0.10f),
                            Color.Transparent
                        )
                    ),
                    topLeft = Offset(this.size.width * 0.16f, this.size.height * 0.82f),
                    size = androidx.compose.ui.geometry.Size(this.size.width * 0.68f, this.size.height * 0.14f)
                )
            }
        }
        Image(
            painter = painterResource(id = character.drawableFor(pose)),
            contentDescription = "${character.name} (${pose.label})",
            contentScale = ContentScale.Fit,
            alignment = Alignment.BottomCenter,
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer {
                    translationY = bobY.dp.toPx()
                    alpha = if (isFocal) 1f else 0.78f
                }
        )
    }
}

@Composable
fun AddOrRenamePlayerDialog(
    titleTag: String,
    headline: String,
    initialName: String = "",
    confirmLabel: String = "GOT IT",
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit
) {
    val palette = LocalImpostorPalette.current
    var nameDraft by remember(initialName) { mutableStateOf(initialName) }

    Dialog(onDismissRequest = onDismiss) {
        Surface(
            color = MaterialTheme.colorScheme.surfaceContainer,
            shape = RoundedCornerShape(24.dp),
            border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.outline),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(22.dp),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Text(
                    text = titleTag.uppercase(),
                    style = MaterialTheme.typography.labelMedium,
                    color = palette.goldAccent,
                    letterSpacing = 1.4.sp
                )
                Text(
                    text = headline,
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                OutlinedTextField(
                    value = nameDraft,
                    onValueChange = { nameDraft = it.take(24) },
                    placeholder = { Text("Enter friend's name") },
                    singleLine = true,
                    shape = RoundedCornerShape(14.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surfaceContainerLowest,
                        focusedBorderColor = palette.goldAccent,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .testTag("player_name_dialog_input")
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = onDismiss,
                        shape = RoundedCornerShape(14.dp),
                        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                    ) {
                        Text("BACK", fontWeight = FontWeight.Bold)
                    }
                    Button(
                        onClick = { onConfirm(nameDraft) },
                        shape = RoundedCornerShape(14.dp),
                        colors = ButtonDefaults.buttonColors(
                            containerColor = palette.goldAccent,
                            contentColor = palette.goldDarkInk
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .height(50.dp)
                            .testTag("player_name_dialog_confirm")
                    ) {
                        Text(confirmLabel, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HowToPlaySheet(
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val palette = LocalImpostorPalette.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surfaceContainerLow,
        modifier = modifier.testTag("how_to_play_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .navigationBarsPadding()
                .padding(horizontal = 20.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.HelpOutline,
                        contentDescription = null,
                        tint = palette.goldAccent
                    )
                    Text(
                        text = "How to play",
                        style = MaterialTheme.typography.headlineSmall,
                        fontWeight = FontWeight.ExtraBold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }
                IconButton(onClick = onDismiss, modifier = Modifier.size(48.dp)) {
                    Icon(Icons.Filled.Close, contentDescription = "Close guide")
                }
            }

            HowToPlayStepCard(
                step = "01 • PASS & FLIP",
                title = "See your card in secret",
                body = "Agents see the Secret Word and a helpful hint. Impostors see no secret word — only optional category context — and must bluff to survive."
            )
            HowToPlayStepCard(
                step = "02 • CLUES & BLUFFING",
                title = "Give one clue per turn",
                body = "Say a clever clue without spoiling the word. Impostors listen closely, blend in, and may enter a Word Guess to steal the round."
            )
            HowToPlayStepCard(
                step = "03 • SPECIAL ROLES",
                title = "Tactical abilities (Optional in Rules)",
                body = "• Agent Killer: Silences 1 player's vote.\n• Savior Agent: Shields 1 player from Killer (+1 bonus if saved).\n• Agent Director: Casts a 2x weight vote.\n• Agent Prediction: Lock an early impostor call (+3/+1 or -2 pts)."
            )
            HowToPlayStepCard(
                step = "04 • VOTE & VERDICT",
                title = "Spot the impostor",
                body = "Pass the phone for private votes (or trigger an Emergency Meeting anytime). Catch the Impostor — unless they guess the Secret Word!"
            )

            GoldCtaButton(
                text = "Got it",
                onClick = onDismiss,
                showArrow = false
            )
        }
    }
}

@Composable
private fun HowToPlayStepCard(
    step: String,
    title: String,
    body: String
) {
    val palette = LocalImpostorPalette.current
    Surface(
        color = MaterialTheme.colorScheme.surfaceContainer,
        shape = RoundedCornerShape(18.dp),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = step,
                style = MaterialTheme.typography.labelSmall,
                color = palette.goldAccent,
                fontWeight = FontWeight.Bold
            )
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = body,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
