/**
 * Enhanced Progress Tracking Components with MPAndroidChart Integration
 *
 * This file provides advanced data visualization components for fitness progress tracking
 * using MPAndroidChart library. Features include:
 * - Interactive line and bar charts
 * - Real-time data visualization
 * - Daily, weekly, and monthly summaries
 * - Goal progress tracking with animations
 * - Comprehensive workout and nutrition analytics
 */

package com.example.fitnesstrackerapp.ui.progress

import android.graphics.Color
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.*
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.utils.ColorTemplate
import java.time.LocalDate
import java.time.format.DateTimeFormatter

/**
 * Data classes for chart data
 */
data class ProgressChartData(
    val date: LocalDate,
    val value: Float,
    val label: String = "",
    val goal: Float? = null,
)

data class WorkoutProgressData(
    val workouts: List<Float>,
    val calories: List<Float>,
    val duration: List<Float>,
    val dates: List<String>,
)

data class NutritionProgressData(
    val calories: List<Float>,
    val protein: List<Float>,
    val carbs: List<Float>,
    val fat: List<Float>,
    val dates: List<String>,
)

/**
 * Main progress tracking screen with enhanced charts
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EnhancedProgressScreen(
    workoutData: WorkoutProgressData,
    nutritionData: NutritionProgressData,
    stepData: List<ProgressChartData>,
    goalProgress: Map<String, Float>,
    onPeriodChanged: (String) -> Unit = {},
    modifier: Modifier = Modifier,
) {
    var selectedPeriod by remember { mutableStateOf("Weekly") }
    val periods = listOf("Daily", "Weekly", "Monthly")

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        // Header with period selector
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "Progress Analytics",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
            )

            // Period selector
            Card {
                Row(
                    modifier = Modifier.padding(4.dp),
                ) {
                    periods.forEach { period ->
                        FilterChip(
                            onClick = {
                                selectedPeriod = period
                                onPeriodChanged(period)
                            },
                            label = { Text(period) },
                            selected = selectedPeriod == period,
                            modifier = Modifier.padding(horizontal = 4.dp),
                        )
                    }
                }
            }
        }

        // Goal progress overview
        GoalProgressSection(goalProgress = goalProgress)

        // Workout progress chart
        WorkoutProgressChart(
            workoutData = workoutData,
            title = "Workout Progress",
        )

        // Steps progress chart
        StepsProgressChart(
            stepData = stepData,
            title = "Daily Steps",
        )

        // Nutrition progress chart
        NutritionProgressChart(
            nutritionData = nutritionData,
            title = "Nutrition Tracking",
        )

        // Weekly comparison
        WeeklyComparisonChart(
            workoutData = workoutData,
            title = "Weekly Comparison",
        )
    }
}

/**
 * Goal progress section with circular progress indicators
 */
@Composable
fun GoalProgressSection(
    goalProgress: Map<String, Float>,
    modifier: Modifier = Modifier,
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
        ) {
            Text(
                text = "Goal Progress",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                goalProgress.forEach { (goalName, progress) ->
                    GoalProgressIndicator(
                        goalName = goalName,
                        progress = progress,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

/**
 * Individual goal progress indicator
 */
@Composable
fun GoalProgressIndicator(
    goalName: String,
    progress: Float,
    modifier: Modifier = Modifier,
) {
    val animatedProgress by animateFloatAsState(
        targetValue = progress.coerceIn(0f, 1f),
        label = "Goal Progress Animation",
    )

    Column(
        modifier = modifier.padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box(
            modifier = Modifier.size(80.dp),
            contentAlignment = Alignment.Center,
        ) {
            CircularProgressIndicator(
                progress = { 1f },
                modifier = Modifier.size(80.dp),
                color = MaterialTheme.colorScheme.surfaceVariant,
                strokeWidth = 8.dp,
            )
            CircularProgressIndicator(
                progress = { animatedProgress },
                modifier = Modifier.size(80.dp),
                color = MaterialTheme.colorScheme.primary,
                strokeWidth = 8.dp,
            )
            Text(
                text = "${(animatedProgress * 100).toInt()}%",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = goalName,
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
        )
    }
}

/**
 * Enhanced workout progress chart using MPAndroidChart
 */
@Composable
fun WorkoutProgressChart(
    workoutData: WorkoutProgressData,
    title: String,
    modifier: Modifier = Modifier,
) {
    LocalContext.current
    val primaryColor = MaterialTheme.colorScheme.primary.toArgb()
    val secondaryColor = MaterialTheme.colorScheme.secondary.toArgb()

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 16.dp),
            )

            AndroidView(
                factory = { ctx ->
                    LineChart(ctx).apply {
                        // Configure chart appearance
                        description.isEnabled = false
                        setTouchEnabled(true)
                        isDragEnabled = true
                        setScaleEnabled(true)
                        setPinchZoom(true)
                        setDrawGridBackground(false)

                        // Configure axes
                        xAxis.apply {
                            position = XAxis.XAxisPosition.BOTTOM
                            setDrawGridLines(false)
                            granularity = 1f
                            valueFormatter = IndexAxisValueFormatter(workoutData.dates)
                        }

                        axisLeft.apply {
                            setDrawGridLines(true)
                            axisMinimum = 0f
                        }

                        axisRight.isEnabled = false

                        // Configure legend
                        legend.isEnabled = true
                    }
                },
                update = { lineChart ->
                    val entries1 = workoutData.workouts.mapIndexed { index, value ->
                        Entry(index.toFloat(), value)
                    }

                    val entries2 = workoutData.calories.mapIndexed { index, value ->
                        Entry(index.toFloat(), value / 10) // Scale for better visualization
                    }

                    val dataSet1 = LineDataSet(entries1, "Workouts").apply {
                        color = primaryColor
                        setCircleColor(primaryColor)
                        lineWidth = 3f
                        circleRadius = 4f
                        setDrawCircleHole(false)
                        valueTextSize = 10f
                        setDrawFilled(true)
                        fillColor = primaryColor
                        fillAlpha = 50
                    }

                    val dataSet2 = LineDataSet(entries2, "Calories (÷10)").apply {
                        color = secondaryColor
                        setCircleColor(secondaryColor)
                        lineWidth = 3f
                        circleRadius = 4f
                        setDrawCircleHole(false)
                        valueTextSize = 10f
                    }

                    lineChart.data = LineData(dataSet1, dataSet2)
                    lineChart.animateX(1000)
                    lineChart.invalidate()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
            )
        }
    }
}

/**
 * Steps progress chart with daily goals
 */
@Composable
fun StepsProgressChart(
    stepData: List<ProgressChartData>,
    title: String,
    modifier: Modifier = Modifier,
) {
    LocalContext.current
    val primaryColor = MaterialTheme.colorScheme.primary.toArgb()
    val goalColor = MaterialTheme.colorScheme.tertiary.toArgb()

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 16.dp),
            )

            AndroidView(
                factory = { ctx ->
                    BarChart(ctx).apply {
                        description.isEnabled = false
                        setTouchEnabled(true)
                        setDrawGridBackground(false)

                        xAxis.apply {
                            position = XAxis.XAxisPosition.BOTTOM
                            setDrawGridLines(false)
                            granularity = 1f
                            valueFormatter = IndexAxisValueFormatter(
                                stepData.map { it.date.format(DateTimeFormatter.ofPattern("MM/dd")) },
                            )
                        }

                        axisLeft.apply {
                            setDrawGridLines(true)
                            axisMinimum = 0f
                        }

                        axisRight.isEnabled = false
                        legend.isEnabled = true
                    }
                },
                update = { barChart ->
                    val stepEntries = stepData.mapIndexed { index, data ->
                        BarEntry(index.toFloat(), data.value)
                    }

                    val goalEntries = stepData.mapIndexed { index, data ->
                        BarEntry(index.toFloat(), data.goal ?: 0f)
                    }

                    val stepDataSet = BarDataSet(stepEntries, "Steps").apply {
                        color = primaryColor
                        valueTextSize = 10f
                    }

                    val goalDataSet = BarDataSet(goalEntries, "Goal").apply {
                        color = goalColor
                        valueTextSize = 10f
                    }

                    val barData = BarData(stepDataSet, goalDataSet).apply {
                        barWidth = 0.35f
                    }

                    barChart.data = barData
                    barChart.groupBars(0f, 0.3f, 0.05f)
                    barChart.animateY(1000)
                    barChart.invalidate()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp),
            )
        }
    }
}

/**
 * Nutrition progress pie chart
 */
@Composable
fun NutritionProgressChart(
    nutritionData: NutritionProgressData,
    title: String,
    modifier: Modifier = Modifier,
) {
    LocalContext.current

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 16.dp),
            )

            // Show latest day's nutrition breakdown
            val latestIndex = nutritionData.dates.lastIndex
            if (latestIndex >= 0) {
                AndroidView(
                    factory = { ctx ->
                        PieChart(ctx).apply {
                            description.isEnabled = false
                            isRotationEnabled = true
                            setUsePercentValues(true)
                            setDrawEntryLabels(false)
                            centerText = "Today's\nMacros"
                            setCenterTextSize(16f)
                            holeRadius = 40f
                            transparentCircleRadius = 45f
                        }
                    },
                    update = { pieChart ->
                        val entries = mutableListOf<PieEntry>()
                        nutritionData.calories.getOrNull(latestIndex) ?: 0f
                        val protein = nutritionData.protein.getOrNull(latestIndex) ?: 0f
                        val carbs = nutritionData.carbs.getOrNull(latestIndex) ?: 0f
                        val fat = nutritionData.fat.getOrNull(latestIndex) ?: 0f

                        if (protein > 0) entries.add(PieEntry(protein, "Protein"))
                        if (carbs > 0) entries.add(PieEntry(carbs, "Carbs"))
                        if (fat > 0) entries.add(PieEntry(fat, "Fat"))

                        val dataSet = PieDataSet(entries, "").apply {
                            colors = ColorTemplate.MATERIAL_COLORS.toList()
                            valueTextSize = 12f
                            valueTextColor = Color.WHITE
                        }

                        pieChart.data = PieData(dataSet)
                        pieChart.animateY(1000)
                        pieChart.invalidate()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                )
            } else {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "No nutrition data available",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }
        }
    }
}

/**
 * Weekly comparison chart showing progress trends
 */
@Composable
fun WeeklyComparisonChart(
    workoutData: WorkoutProgressData,
    title: String,
    modifier: Modifier = Modifier,
) {
    LocalContext.current
    MaterialTheme.colorScheme.primary.toArgb()

    Card(
        modifier = modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp),
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.padding(bottom = 16.dp),
            )

            AndroidView(
                factory = { ctx ->
                    BarChart(ctx).apply {
                        description.isEnabled = false
                        setTouchEnabled(true)
                        setDrawGridBackground(false)

                        xAxis.apply {
                            position = XAxis.XAxisPosition.BOTTOM
                            setDrawGridLines(false)
                            granularity = 1f
                            valueFormatter = IndexAxisValueFormatter(
                                listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"),
                            )
                        }

                        axisLeft.apply {
                            setDrawGridLines(true)
                            axisMinimum = 0f
                        }

                        axisRight.isEnabled = false
                        legend.isEnabled = false
                    }
                },
                update = { barChart ->
                    // Take last 7 days of workout data
                    val weeklyWorkouts = workoutData.workouts.takeLast(7)
                    val entries = weeklyWorkouts.mapIndexed { index, value ->
                        BarEntry(index.toFloat(), value)
                    }

                    val dataSet = BarDataSet(entries, "Weekly Workouts").apply {
                        colors = ColorTemplate.MATERIAL_COLORS.toList()
                        valueTextSize = 12f
                    }

                    barChart.data = BarData(dataSet)
                    barChart.animateY(1000)
                    barChart.invalidate()
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(250.dp),
            )
        }
    }
}
