/**
 * Progress Tracking UI Components
 *
 * Material 3 implementation of progress tracking components that provide:
 * - Animated progress indicators and charts
 * - Responsive data visualization
 * - Performance metrics display
 * - Achievement tracking
 * - Dark/light theme support
 * - Motion transitions and accessibility
 */

package com.example.fitnesstrackerapp.ui.progress

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ElevatedCard
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import java.time.LocalDate

/**
 * Data class for progress metrics
 */
data class ProgressMetric(
    val title: String,
    val value: String,
    val icon: ImageVector,
    val change: Float = 0f,
    val isIncrease: Boolean = true,
)

/**
 * Data class for chart data points
 */
data class ChartDataPoint(
    val date: LocalDate,
    val value: Float,
    val label: String = "",
)

/**
 * Comprehensive progress overview card
 */
@OptIn(ExperimentalLayoutApi::class)
@Composable
fun ProgressOverviewCard(
    metrics: List<ProgressMetric>,
    modifier: Modifier = Modifier,
) {
    ElevatedCard(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.elevatedCardElevation(defaultElevation = 4.dp),
        colors = CardDefaults.elevatedCardColors(
            containerColor = MaterialTheme.colorScheme.surfaceContainer,
        ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Progress Overview",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )

            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                metrics.forEach { metric ->
                    ProgressMetricItem(
                        metric = metric,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

/**
 * Individual progress metric display
 */
@Composable
fun ProgressMetricItem(
    metric: ProgressMetric,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .semantics { contentDescription = "${metric.title}: ${metric.value}" },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Icon with background
        Box(
            modifier = Modifier
                .size(56.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(
                            MaterialTheme.colorScheme.primaryContainer,
                            MaterialTheme.colorScheme.secondaryContainer,
                        ),
                    ),
                ),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = metric.icon,
                contentDescription = null,
                modifier = Modifier.size(28.dp),
                tint = MaterialTheme.colorScheme.onPrimaryContainer,
            )
        }

        // Value
        Text(
            text = metric.value,
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
        )

        // Title
        Text(
            text = metric.title,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )

        // Change indicator (if applicable)
        if (metric.change != 0f) {
            ChangeIndicator(
                change = metric.change,
                isIncrease = metric.isIncrease,
            )
        }
    }
}

/**
 * Change indicator with trend arrow
 */
@Composable
private fun ChangeIndicator(
    change: Float,
    isIncrease: Boolean,
    modifier: Modifier = Modifier,
) {
    val color = if (isIncrease) {
        MaterialTheme.colorScheme.primary
    } else {
        MaterialTheme.colorScheme.error
    }

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        Icon(
            imageVector = Icons.Default.TrendingUp,
            contentDescription = null,
            modifier = Modifier.size(16.dp),
            tint = color,
        )

        Text(
            text = "${if (isIncrease) "+" else ""}$change%",
            style = MaterialTheme.typography.labelSmall,
            color = color,
            fontWeight = FontWeight.Medium,
        )
    }
}

/**
 * Animated circular progress indicator
 */
@Composable
fun AnimatedCircularProgress(
    progress: Float,
    title: String,
    subtitle: String,
    modifier: Modifier = Modifier,
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow,
        ),
        label = "Circular Progress Animation",
    )

    Box(
        modifier = modifier
            .size(160.dp)
            .semantics { contentDescription = "$title: ${(progress * 100).toInt()}% complete" },
        contentAlignment = Alignment.Center,
    ) {
        // Background circle
        CircularProgressIndicator(
            progress = 1f,
            modifier = Modifier.size(160.dp),
            color = MaterialTheme.colorScheme.surfaceVariant,
            strokeWidth = 12.dp,
            strokeCap = StrokeCap.Round,
        )

        // Progress circle
        CircularProgressIndicator(
            progress = animatedProgress,
            modifier = Modifier.size(160.dp),
            color = MaterialTheme.colorScheme.primary,
            strokeWidth = 12.dp,
            strokeCap = StrokeCap.Round,
        )

        // Center content
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(4.dp),
        ) {
            Text(
                text = "${(animatedProgress * 100).toInt()}%",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
            )

            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center,
            )

            if (subtitle.isNotEmpty()) {
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textAlign = TextAlign.Center,
                )
            }
        }
    }
}

/**
 * Line chart for progress tracking
 */
@Composable
fun ProgressLineChart(
    data: List<ChartDataPoint>,
    title: String,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
            )

            if (data.isNotEmpty()) {
                LineChart(
                    data = data,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "No data available",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

/**
 * Custom line chart implementation using Canvas
 */
@Composable
private fun LineChart(
    data: List<ChartDataPoint>,
    modifier: Modifier = Modifier,
) {
    val primary = MaterialTheme.colorScheme.primary
    val surface = MaterialTheme.colorScheme.surface
    val onSurface = MaterialTheme.colorScheme.onSurface

    var animationProgress by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(data) {
        animationProgress = 0f
        // Animate line drawing
        while (animationProgress < 1f) {
            animationProgress += 0.02f
            delay(16) // ~60fps
        }
        animationProgress = 1f
    }

    Canvas(modifier = modifier) {
        if (data.size < 2) return@Canvas

        val padding = 40.dp.toPx()
        val chartWidth = size.width - 2 * padding
        val chartHeight = size.height - 2 * padding

        val maxValue = data.maxOfOrNull { it.value } ?: 1f
        val minValue = data.minOfOrNull { it.value } ?: 0f
        val valueRange = maxValue - minValue

        // Draw grid lines
        drawGridLines(
            chartWidth = chartWidth,
            chartHeight = chartHeight,
            padding = padding,
            color = onSurface.copy(alpha = 0.1f),
        )

        // Calculate points
        val points = data.mapIndexed { index, dataPoint ->
            val x = padding + (index * chartWidth / (data.size - 1))
            val y = padding + chartHeight - ((dataPoint.value - minValue) / valueRange) * chartHeight
            Offset(x, y)
        }

        // Draw animated line
        val animatedPoints = points.take((points.size * animationProgress).toInt().coerceAtLeast(2))
        if (animatedPoints.size >= 2) {
            drawLineChart(
                points = animatedPoints,
                color = primary,
                strokeWidth = 4.dp.toPx(),
            )
        }

        // Draw data points
        animatedPoints.forEach { point ->
            drawCircle(
                color = primary,
                radius = 6.dp.toPx(),
                center = point,
            )
            drawCircle(
                color = surface,
                radius = 3.dp.toPx(),
                center = point,
            )
        }
    }
}

/**
 * Draw grid lines for the chart
 */
private fun DrawScope.drawGridLines(
    chartWidth: Float,
    chartHeight: Float,
    padding: Float,
    color: Color,
) {
    val gridLines = 4

    // Horizontal grid lines
    repeat(gridLines + 1) { i ->
        val y = padding + (i * chartHeight / gridLines)
        drawLine(
            color = color,
            start = Offset(padding, y),
            end = Offset(padding + chartWidth, y),
            strokeWidth = 1.dp.toPx(),
        )
    }

    // Vertical grid lines
    repeat(5) { i ->
        val x = padding + (i * chartWidth / 4)
        drawLine(
            color = color,
            start = Offset(x, padding),
            end = Offset(x, padding + chartHeight),
            strokeWidth = 1.dp.toPx(),
        )
    }
}

/**
 * Draw the line chart path
 */
private fun DrawScope.drawLineChart(
    points: List<Offset>,
    color: Color,
    strokeWidth: Float,
) {
    val path = Path()

    if (points.isNotEmpty()) {
        path.moveTo(points[0].x, points[0].y)

        for (i in 1 until points.size) {
            val currentPoint = points[i]
            val previousPoint = points[i - 1]

            // Create smooth curves using quadratic bezier
            val controlX = (previousPoint.x + currentPoint.x) / 2
            val controlY = previousPoint.y

            path.quadraticBezierTo(
                controlX,
                controlY,
                currentPoint.x,
                currentPoint.y,
            )
        }

        drawPath(
            path = path,
            color = color,
            style = Stroke(
                width = strokeWidth,
                cap = StrokeCap.Round,
            ),
        )
    }
}

/**
 * Weekly progress summary
 */
@Composable
fun WeeklyProgressSummary(
    weeklyData: List<Float>,
    currentWeekIndex: Int,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp),
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
        ) {
            Text(
                text = "Weekly Progress",
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.SemiBold,
            )

            LazyRow(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                contentPadding = PaddingValues(horizontal = 4.dp),
            ) {
                items(weeklyData.size) { index ->
                    WeeklyProgressBar(
                        progress = weeklyData[index],
                        isCurrentWeek = index == currentWeekIndex,
                        weekLabel = "W${index + 1}",
                    )
                }
            }
        }
    }
}

/**
 * Individual weekly progress bar
 */
@Composable
private fun WeeklyProgressBar(
    progress: Float,
    isCurrentWeek: Boolean,
    weekLabel: String,
    modifier: Modifier = Modifier,
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        animationSpec = tween(durationMillis = 1000, delayMillis = 100),
        label = "Weekly Progress Animation",
    )

    Column(
        modifier = modifier
            .width(32.dp)
            .semantics { contentDescription = "$weekLabel: ${(progress * 100).toInt()}% complete" },
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        // Progress bar
        Box(
            modifier = Modifier
                .width(24.dp)
                .height(100.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxSize(animatedProgress)
                    .clip(RoundedCornerShape(12.dp))
                    .background(
                        if (isCurrentWeek) {
                            MaterialTheme.colorScheme.primary
                        } else {
                            MaterialTheme.colorScheme.secondary
                        },
                    )
                    .align(Alignment.BottomCenter),
            )
        }

        // Week label
        Text(
            text = weekLabel,
            style = MaterialTheme.typography.labelSmall,
            color = if (isCurrentWeek) {
                MaterialTheme.colorScheme.primary
            } else {
                MaterialTheme.colorScheme.onSurfaceVariant
            },
            fontWeight = if (isCurrentWeek) FontWeight.SemiBold else FontWeight.Normal,
        )
    }
}
