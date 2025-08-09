/**
 * Enhanced Animation Components for Fitness Tracker App
 *
 * Features:
 * - Advanced Material Motion transitions
 * - ConstraintLayout-based animations
 * - Progress animations with spring physics
 * - Gesture-based interactions
 * - Accessibility-aware animations
 * - Performance-optimized animations
 */

package com.example.fitnesstrackerapp.ui.components

import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.*
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.*
import com.example.fitnesstrackerapp.ui.theme.*

/**
 * Animated circular progress indicator with gradient colors
 */
@Composable
fun AnimatedCircularProgress(
    progress: Float,
    modifier: Modifier = Modifier,
    size: Dp = 120.dp,
    strokeWidth: Dp = 12.dp,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    progressColors: List<Color> = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
    ),
    animationDuration: Int = 1000,
    animationDelay: Int = 0,
    centerContent: @Composable (BoxScope.() -> Unit)? = null,
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(
            durationMillis = animationDuration,
            delayMillis = animationDelay,
            easing = FastOutSlowInEasing,
        ),
        label = "Progress Animation",
    )

    Box(
        modifier = modifier
            .size(size)
            .semantics {
                contentDescription = "Progress: ${(progress * 100).toInt()}%"
            },
        contentAlignment = Alignment.Center,
    ) {
        Canvas(
            modifier = Modifier.fillMaxSize(),
        ) {
            val strokeWidthPx = strokeWidth.toPx()
            val radius = (size.toPx() - strokeWidthPx) / 2
            val center = Offset(size.toPx() / 2, size.toPx() / 2)

            // Background circle
            drawCircle(
                color = backgroundColor,
                radius = radius,
                center = center,
                style = Stroke(width = strokeWidthPx),
            )

            // Progress arc
            if (animatedProgress > 0f) {
                val sweepAngle = 360f * animatedProgress
                val gradient = Brush.sweepGradient(
                    colors = progressColors,
                    center = center,
                )

                drawArc(
                    brush = gradient,
                    startAngle = -90f,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(
                        width = strokeWidthPx,
                        cap = StrokeCap.Round,
                    ),
                    topLeft = Offset(strokeWidthPx / 2, strokeWidthPx / 2),
                    size = Size(size.toPx() - strokeWidthPx, size.toPx() - strokeWidthPx),
                )
            }
        }

        centerContent?.invoke(this)
    }
}

/**
 * Bouncy button with spring animation
 */
@Composable
fun BouncyButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    colors: ButtonColors = ButtonDefaults.buttonColors(),
    elevation: ButtonElevation? = ButtonDefaults.buttonElevation(),
    shape: Shape = MaterialTheme.shapes.medium,
    contentPadding: PaddingValues = ButtonDefaults.ContentPadding,
    content: @Composable RowScope.() -> Unit,
) {
    var isPressed by remember { mutableStateOf(false) }

    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.95f else 1f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessHigh,
        ),
        label = "Button Scale Animation",
    )

    Button(
        onClick = {
            if (enabled) {
                onClick()
            }
        },
        modifier = modifier
            .scale(scale)
            .pointerInput(Unit) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    },
                )
            },
        enabled = enabled,
        colors = colors,
        elevation = elevation,
        shape = shape,
        contentPadding = contentPadding,
        content = content,
    )
}

/**
 * Animated step counter with smooth transitions
 */
@Composable
fun AnimatedStepCounter(
    currentSteps: Int,
    targetSteps: Int,
    modifier: Modifier = Modifier,
    textStyle: androidx.compose.ui.text.TextStyle = MaterialTheme.typography.headlineLarge,
    color: Color = MaterialTheme.colorScheme.primary,
    animationDuration: Int = 800,
) {
    var animatedSteps by remember { mutableStateOf(0) }

    LaunchedEffect(currentSteps) {
        val animator = Animatable(animatedSteps.toFloat())
        animator.animateTo(
            targetValue = currentSteps.toFloat(),
            animationSpec = tween(
                durationMillis = animationDuration,
                easing = FastOutSlowInEasing,
            ),
        ) {
            animatedSteps = value.toInt()
        }
    }

    Text(
        text = animatedSteps.toString(),
        style = textStyle.copy(
            fontWeight = FontWeight.Bold,
            color = color,
        ),
        modifier = modifier.semantics {
            contentDescription = "Step count: $animatedSteps out of $targetSteps"
        },
    )
}

/**
 * Floating Action Button with morphing animation
 */
@Composable
fun MorphingFAB(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    expanded: Boolean = false,
    icon: @Composable () -> Unit,
    text: @Composable () -> Unit = {},
    containerColor: Color = MaterialTheme.colorScheme.primaryContainer,
    contentColor: Color = MaterialTheme.colorScheme.onPrimaryContainer,
) {
    val density = LocalDensity.current

    val width by animateDpAsState(
        targetValue = if (expanded) 120.dp else 56.dp,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "FAB Width Animation",
    )

    ExtendedFloatingActionButton(
        onClick = onClick,
        modifier = modifier.width(width),
        containerColor = containerColor,
        contentColor = contentColor,
        expanded = expanded,
        icon = icon,
        text = text,
    )
}

/**
 * Shimmering loading placeholder
 */
@Composable
fun ShimmerPlaceholder(
    modifier: Modifier = Modifier,
    height: Dp = 20.dp,
    shape: Shape = RoundedCornerShape(4.dp),
) {
    val shimmerColors = listOf(
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.1f),
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
    )

    val transition = rememberInfiniteTransition(label = "Shimmer Transition")
    val translateAnim by transition.animateFloat(
        initialValue = 0f,
        targetValue = 1000f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
        ),
        label = "Shimmer Translation",
    )

    val brush = Brush.linearGradient(
        colors = shimmerColors,
        start = Offset.Zero,
        end = Offset(x = translateAnim, y = translateAnim),
    )

    Box(
        modifier = modifier
            .height(height)
            .clip(shape)
            .background(brush),
    )
}

/**
 * Pulsing achievement badge
 */
@Composable
fun PulsingAchievementBadge(
    modifier: Modifier = Modifier,
    isUnlocked: Boolean = false,
    content: @Composable () -> Unit,
) {
    val infiniteTransition = rememberInfiniteTransition(label = "Pulse Transition")

    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = if (isUnlocked) 1.1f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "Pulse Scale",
    )

    val alpha by infiniteTransition.animateFloat(
        initialValue = if (isUnlocked) 0.7f else 1f,
        targetValue = if (isUnlocked) 1f else 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse,
        ),
        label = "Pulse Alpha",
    )

    Box(
        modifier = modifier
            .scale(scale)
            .graphicsLayer { this.alpha = alpha },
        contentAlignment = Alignment.Center,
    ) {
        if (isUnlocked) {
            // Glow effect for unlocked achievements
            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clip(CircleShape)
                    .background(
                        brush = Brush.radialGradient(
                            colors = listOf(
                                AchievementGold.copy(alpha = 0.3f),
                                Color.Transparent,
                            ),
                        ),
                    ),
            )
        }
        content()
    }
}

/**
 * Animated workout progress bar
 */
@Composable
fun AnimatedWorkoutProgressBar(
    currentTime: Long,
    totalTime: Long,
    modifier: Modifier = Modifier,
    height: Dp = 8.dp,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    progressColor: Color = MaterialTheme.colorScheme.primary,
    animationDuration: Int = 300,
) {
    val progress = if (totalTime > 0) currentTime.toFloat() / totalTime.toFloat() else 0f

    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(
            durationMillis = animationDuration,
            easing = FastOutSlowInEasing,
        ),
        label = "Workout Progress Animation",
    )

    Box(
        modifier = modifier
            .height(height)
            .clip(RoundedCornerShape(height / 2))
            .background(backgroundColor)
            .semantics {
                contentDescription = "Workout progress: ${(progress * 100).toInt()}%"
            },
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(animatedProgress)
                .clip(RoundedCornerShape(height / 2))
                .background(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            progressColor,
                            progressColor.copy(alpha = 0.8f),
                        ),
                    ),
                ),
        )
    }
}

/**
 * Motion layout-based card with constraint animations
 */
@OptIn(ExperimentalMotionApi::class)
@Composable
fun MotionCard(
    expanded: Boolean,
    onExpandChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    title: String,
    subtitle: String,
    content: @Composable () -> Unit,
) {
    val motionScene = remember {
        MotionScene {
            val titleRef = createRefFor("title")
            val subtitleRef = createRefFor("subtitle")
            val contentRef = createRefFor("content")
            val cardRef = createRefFor("card")

            defaultTransition(
                from = constraintSet {
                    constrain(titleRef) {
                        top.linkTo(parent.top, margin = 16.dp)
                        start.linkTo(parent.start, margin = 16.dp)
                    }
                    constrain(subtitleRef) {
                        top.linkTo(titleRef.bottom, margin = 4.dp)
                        start.linkTo(parent.start, margin = 16.dp)
                    }
                    constrain(contentRef) {
                        top.linkTo(subtitleRef.bottom, margin = 8.dp)
                        start.linkTo(parent.start, margin = 16.dp)
                        end.linkTo(parent.end, margin = 16.dp)
                        width = Dimension.fillToConstraints
                        height = Dimension.value(0.dp)
                    }
                },
                to = constraintSet {
                    constrain(titleRef) {
                        top.linkTo(parent.top, margin = 16.dp)
                        start.linkTo(parent.start, margin = 16.dp)
                    }
                    constrain(subtitleRef) {
                        top.linkTo(titleRef.bottom, margin = 4.dp)
                        start.linkTo(parent.start, margin = 16.dp)
                    }
                    constrain(contentRef) {
                        top.linkTo(subtitleRef.bottom, margin = 16.dp)
                        start.linkTo(parent.start, margin = 16.dp)
                        end.linkTo(parent.end, margin = 16.dp)
                        bottom.linkTo(parent.bottom, margin = 16.dp)
                        width = Dimension.fillToConstraints
                        height = Dimension.wrapContent
                    }
                },
            ) {
                keyAttributes(titleRef, subtitleRef, contentRef) {
                    frame(0) {
                        alpha(1f)
                    }
                    frame(50) {
                        alpha(0.5f)
                    }
                    frame(100) {
                        alpha(1f)
                    }
                }
            }
        }
    }

    val progress by animateFloatAsState(
        targetValue = if (expanded) 1f else 0f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium,
        ),
        label = "Motion Card Progress",
    )

    Card(
        modifier = modifier
            .clickable { onExpandChange(!expanded) }
            .shadow(
                elevation = 4.dp,
                shape = MaterialTheme.shapes.medium,
            ),
    ) {
        MotionLayout(
            motionScene = motionScene,
            progress = progress,
            modifier = Modifier
                .fillMaxWidth()
                .animateContentSize(),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.layoutId("title"),
            )

            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.layoutId("subtitle"),
            )

            Box(
                modifier = Modifier.layoutId("content"),
            ) {
                content()
            }
        }
    }
}

/**
 * Enhanced progress indicator with milestone markers
 */
@Composable
fun MilestoneProgressIndicator(
    progress: Float,
    milestones: List<Float>,
    modifier: Modifier = Modifier,
    height: Dp = 12.dp,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    progressColors: List<Color> = listOf(
        GradientStart,
        GradientMiddle,
        GradientEnd,
    ),
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow,
        ),
        label = "Milestone Progress Animation",
    )

    Canvas(
        modifier = modifier
            .height(height)
            .semantics {
                contentDescription = "Progress with milestones: ${(progress * 100).toInt()}%"
            },
    ) {
        val cornerRadius = height.toPx() / 2

        // Background
        drawRoundRect(
            color = backgroundColor,
            size = size,
            cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius),
        )

        // Progress
        if (animatedProgress > 0f) {
            val progressWidth = size.width * animatedProgress
            val gradient = Brush.horizontalGradient(progressColors)

            drawRoundRect(
                brush = gradient,
                size = Size(progressWidth, size.height),
                cornerRadius = androidx.compose.ui.geometry.CornerRadius(cornerRadius),
            )
        }

        // Milestone markers
        milestones.forEach { milestone ->
            val markerX = size.width * milestone.coerceIn(0f, 1f)
            val markerColor = if (animatedProgress >= milestone) {
                Color.White
            } else {
                backgroundColor.copy(alpha = 0.8f)
            }

            drawCircle(
                color = markerColor,
                radius = height.toPx() / 3,
                center = Offset(markerX, size.height / 2),
            )
        }
    }
}
