/**
 * Custom Charts Implementation using Compose Canvas
 *
 * Material 3 implementation of custom charts for fitness data visualization:
 * - Line charts for progress tracking
 * - Bar charts for workout/calorie data
 * - Pie charts for macro breakdown
 * - Circular progress indicators
 * - Animated chart elements with Material 3 colors
 * - Responsive design and accessibility
 */

package com.example.fitnesstrackerapp.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
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
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.drawIntoCanvas
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.delay
import java.time.LocalDate

/**
 * Data class for chart data points
 */
data class ChartDataPoint(
    val value: Float,
    val label: String,
    val date: LocalDate? = null,
)

/**
 * Animated Line Chart with Material 3 styling
 */
@Composable
fun AnimatedLineChart(
    data: List<ChartDataPoint>,
    modifier: Modifier = Modifier,
    lineColor: Color = MaterialTheme.colorScheme.primary,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    gridColor: Color = MaterialTheme.colorScheme.outlineVariant,
    showPoints: Boolean = true,
    animationDuration: Int = 1500,
) {
    val density = LocalDensity.current
    MaterialTheme.colorScheme.primary
    val onSurface = MaterialTheme.colorScheme.onSurface
    MaterialTheme.colorScheme.outline

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

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .semantics { contentDescription = "Line chart with ${data.size} data points" },
    ) {
        if (data.isEmpty()) return@Canvas

        val padding = 40.dp.toPx()
        val chartWidth = size.width - 2 * padding
        val chartHeight = size.height - 2 * padding

        val maxValue = data.maxOfOrNull { it.value } ?: 1f
        val minValue = data.minOfOrNull { it.value } ?: 0f
        val valueRange = maxValue - minValue

        // Draw background
        drawRect(
            color = backgroundColor,
            size = size,
        )

        // Draw grid lines
        drawGridLines(
            chartWidth = chartWidth,
            chartHeight = chartHeight,
            padding = padding,
            gridColor = gridColor,
        )

        // Calculate points
        val points = data.mapIndexed { index, dataPoint ->
            val x = padding + (index * chartWidth / (data.size - 1).coerceAtLeast(1))
            val y = padding + chartHeight - ((dataPoint.value - minValue) / valueRange.coerceAtLeast(0.001f)) * chartHeight
            Offset(x, y)
        }

        // Draw animated line with gradient
        if (points.size >= 2) {
            val animatedPoints = points.take((points.size * animationProgress).toInt().coerceAtLeast(2))

            // Draw gradient area under the line
            drawLineChartGradient(
                points = animatedPoints,
                color = lineColor,
                chartHeight = chartHeight,
                padding = padding,
            )

            // Draw main line
            drawLineChart(
                points = animatedPoints,
                color = lineColor,
                strokeWidth = 4.dp.toPx(),
            )
        }

        // Draw animated data points
        if (showPoints) {
            val animatedPointCount = (points.size * animationProgress).toInt()
            points.take(animatedPointCount).forEach { point ->
                // Outer circle
                drawCircle(
                    color = lineColor,
                    radius = 8.dp.toPx(),
                    center = point,
                )
                // Inner circle
                drawCircle(
                    color = backgroundColor,
                    radius = 4.dp.toPx(),
                    center = point,
                )
            }
        }

        // Draw value labels
        drawValueLabels(
            data = data,
            points = points,
            animationProgress = animationProgress,
            textColor = onSurface,
            density = density,
        )
    }
}

/**
 * Draw grid lines for the chart
 */
private fun DrawScope.drawGridLines(
    chartWidth: Float,
    chartHeight: Float,
    padding: Float,
    gridColor: Color,
) {
    val gridLines = 4

    // Horizontal grid lines
    repeat(gridLines + 1) { i ->
        val y = padding + (i * chartHeight / gridLines)
        drawLine(
            color = gridColor,
            start = Offset(padding, y),
            end = Offset(padding + chartWidth, y),
            strokeWidth = 1.dp.toPx(),
        )
    }

    // Vertical grid lines
    repeat(5) { i ->
        val x = padding + (i * chartWidth / 4)
        drawLine(
            color = gridColor,
            start = Offset(x, padding),
            end = Offset(x, padding + chartHeight),
            strokeWidth = 1.dp.toPx(),
        )
    }
}

/**
 * Draw the main line chart path
 */
private fun DrawScope.drawLineChart(
    points: List<Offset>,
    color: Color,
    strokeWidth: Float,
) {
    if (points.size < 2) return

    val path = Path().apply {
        moveTo(points[0].x, points[0].y)

        for (i in 1 until points.size) {
            val currentPoint = points[i]
            val previousPoint = points[i - 1]

            // Create smooth curves using quadratic bezier
            val controlX = (previousPoint.x + currentPoint.x) / 2
            val controlY = previousPoint.y

            quadraticBezierTo(
                controlX,
                controlY,
                currentPoint.x,
                currentPoint.y,
            )
        }
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

/**
 * Draw gradient area under the line
 */
private fun DrawScope.drawLineChartGradient(
    points: List<Offset>,
    color: Color,
    chartHeight: Float,
    padding: Float,
) {
    if (points.size < 2) return

    val path = Path().apply {
        moveTo(points[0].x, padding + chartHeight)
        lineTo(points[0].x, points[0].y)

        for (i in 1 until points.size) {
            val currentPoint = points[i]
            val previousPoint = points[i - 1]

            val controlX = (previousPoint.x + currentPoint.x) / 2
            val controlY = previousPoint.y

            quadraticBezierTo(
                controlX,
                controlY,
                currentPoint.x,
                currentPoint.y,
            )
        }

        lineTo(points.last().x, padding + chartHeight)
        close()
    }

    drawPath(
        path = path,
        brush = Brush.verticalGradient(
            colors = listOf(
                color.copy(alpha = 0.3f),
                color.copy(alpha = 0.1f),
                Color.Transparent,
            ),
        ),
    )
}

/**
 * Draw value labels on the chart
 */
private fun DrawScope.drawValueLabels(
    data: List<ChartDataPoint>,
    points: List<Offset>,
    animationProgress: Float,
    textColor: Color,
    density: androidx.compose.ui.unit.Density,
) {
    val animatedCount = (points.size * animationProgress).toInt()

    drawIntoCanvas { canvas ->
        val paint = android.graphics.Paint().apply {
            this.color = textColor.toArgb()
            textAlign = android.graphics.Paint.Align.CENTER
            textSize = with(density) { 12.dp.toPx() }
        }

        data.take(animatedCount).forEachIndexed { index, dataPoint ->
            if (index < points.size) {
                val point = points[index]
                canvas.nativeCanvas.drawText(
                    dataPoint.value.toInt().toString(),
                    point.x,
                    point.y - 20.dp.toPx(),
                    paint,
                )
            }
        }
    }
}

/**
 * Animated Bar Chart with Material 3 styling
 */
@Composable
fun AnimatedBarChart(
    data: List<ChartDataPoint>,
    modifier: Modifier = Modifier,
    barColor: Color = MaterialTheme.colorScheme.primary,
    backgroundColor: Color = MaterialTheme.colorScheme.surface,
    animationDuration: Int = 1000,
) {
    val animatedValues = data.map { dataPoint ->
        val animatedValue = remember { Animatable(0f) }

        LaunchedEffect(dataPoint.value) {
            animatedValue.animateTo(
                targetValue = dataPoint.value,
                animationSpec = spring(
                    dampingRatio = Spring.DampingRatioMediumBouncy,
                    stiffness = Spring.StiffnessLow,
                ),
            )
        }

        animatedValue.value
    }

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(200.dp)
            .semantics { contentDescription = "Bar chart with ${data.size} bars" },
    ) {
        if (data.isEmpty()) return@Canvas

        val padding = 40.dp.toPx()
        val chartWidth = size.width - 2 * padding
        val chartHeight = size.height - 2 * padding

        val maxValue = data.maxOfOrNull { it.value } ?: 1f
        val barWidth = chartWidth / data.size * 0.7f
        val barSpacing = chartWidth / data.size * 0.3f

        // Draw background
        drawRect(
            color = backgroundColor,
            size = size,
        )

        animatedValues.forEachIndexed { index, animatedValue ->
            val barHeight = (animatedValue / maxValue.coerceAtLeast(0.001f)) * chartHeight
            val x = padding + index * (barWidth + barSpacing) + barSpacing / 2
            val y = padding + chartHeight - barHeight

            // Draw bar with rounded corners
            drawRoundRect(
                color = barColor,
                topLeft = Offset(x, y),
                size = Size(barWidth, barHeight),
                cornerRadius = CornerRadius(4.dp.toPx(), 4.dp.toPx()),
            )

            // Draw value label
            drawIntoCanvas { canvas ->
                val paint = android.graphics.Paint().apply {
                    color = barColor.toArgb()
                    textAlign = android.graphics.Paint.Align.CENTER
                    textSize = 24f
                }

                canvas.nativeCanvas.drawText(
                    animatedValue.toInt().toString(),
                    x + barWidth / 2,
                    y - 10,
                    paint,
                )
            }
        }
    }
}

/**
 * Animated Pie Chart with Material 3 styling
 */
@Composable
fun AnimatedPieChart(
    data: List<ChartDataPoint>,
    modifier: Modifier = Modifier,
    colors: List<Color> = listOf(
        MaterialTheme.colorScheme.primary,
        MaterialTheme.colorScheme.secondary,
        MaterialTheme.colorScheme.tertiary,
        MaterialTheme.colorScheme.error,
    ),
) {
    val total = data.sumOf { it.value.toDouble() }.toFloat()
    var animationProgress by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(data) {
        animationProgress = 0f
        while (animationProgress < 1f) {
            animationProgress += 0.02f
            delay(16)
        }
        animationProgress = 1f
    }

    Canvas(
        modifier = modifier
            .size(200.dp)
            .semantics { contentDescription = "Pie chart with ${data.size} segments" },
    ) {
        if (data.isEmpty() || total == 0f) return@Canvas

        val radius = size.minDimension / 2 - 20.dp.toPx()
        val center = Offset(size.width / 2, size.height / 2)
        val strokeWidth = 30.dp.toPx()

        var startAngle = -90f

        data.forEachIndexed { index, dataPoint ->
            val sweepAngle = (dataPoint.value / total) * 360f * animationProgress
            val color = colors[index % colors.size]

            if (sweepAngle > 0) {
                drawArc(
                    color = color,
                    startAngle = startAngle,
                    sweepAngle = sweepAngle,
                    useCenter = false,
                    style = Stroke(
                        width = strokeWidth,
                        cap = StrokeCap.Round,
                    ),
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2),
                )
            }

            startAngle += sweepAngle
        }
    }
}

/**
 * Circular Progress Chart with percentage display
 */
@Composable
fun CircularProgressChart(
    progress: Float,
    total: Float,
    modifier: Modifier = Modifier,
    progressColor: Color = MaterialTheme.colorScheme.primary,
    backgroundColor: Color = MaterialTheme.colorScheme.surfaceVariant,
    showPercentage: Boolean = true,
) {
    val percentage = if (total > 0) (progress / total).coerceIn(0f, 1f) else 0f
    val animatedPercentage by animateFloatAsState(
        targetValue = percentage,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow,
        ),
        label = "Circular Progress Animation",
    )

    Box(
        modifier = modifier.size(120.dp),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .semantics { contentDescription = "Progress: ${(percentage * 100).toInt()}%" },
        ) {
            val strokeWidth = 12.dp.toPx()
            val radius = (size.minDimension - strokeWidth) / 2
            val center = Offset(size.width / 2, size.height / 2)

            // Draw background circle
            drawCircle(
                color = backgroundColor,
                radius = radius,
                center = center,
                style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
            )

            // Draw progress arc
            if (animatedPercentage > 0) {
                drawArc(
                    color = progressColor,
                    startAngle = -90f,
                    sweepAngle = 360f * animatedPercentage,
                    useCenter = false,
                    style = Stroke(width = strokeWidth, cap = StrokeCap.Round),
                    topLeft = Offset(center.x - radius, center.y - radius),
                    size = Size(radius * 2, radius * 2),
                )
            }
        }

        if (showPercentage) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "${(animatedPercentage * 100).toInt()}%",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Text(
                    text = "${progress.toInt()} / ${total.toInt()}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }
    }
}

/**
 * Chart Legend Component
 */
@Composable
fun ChartLegend(
    data: List<ChartDataPoint>,
    colors: List<Color>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        data.forEachIndexed { index, dataPoint ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp),
            ) {
                Box(
                    modifier = Modifier
                        .size(16.dp)
                        .width(16.dp)
                        .height(16.dp),
                ) {
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCircle(
                            color = colors[index % colors.size],
                            radius = size.minDimension / 2,
                        )
                    }
                }

                Text(
                    text = dataPoint.label,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface,
                )

                Spacer(modifier = Modifier.weight(1f))

                Text(
                    text = dataPoint.value.toInt().toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Medium,
                    color = MaterialTheme.colorScheme.onSurface,
                )
            }
        }
    }
}
