/**
 * Responsive Layout Utilities for Fitness Tracker App
 *
 * Features:
 * - Adaptive layouts based on screen size and orientation
 * - Dynamic column counts for different screen widths
 * - Responsive padding and spacing
 * - Constraint-based layouts for complex UIs
 * - Material 3 breakpoint specifications
 */

package com.example.fitnesstrackerapp.ui.layout

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyGridScope
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.material3.windowsizeclass.WindowSizeClass
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.ConstraintLayoutScope

/**
 * Responsive breakpoints following Material Design guidelines
 */
object ResponsiveBreakpoints {
    val Compact = 0.dp..599.dp
    val Medium = 600.dp..839.dp
    val Expanded = 840.dp..1199.dp
    val Large = 1200.dp..1599.dp
    val ExtraLarge = 1600.dp..Int.MAX_VALUE.dp
}

/**
 * Screen size classifications
 */
enum class ScreenSize {
    Compact, // Phone in portrait
    Medium, // Phone in landscape or small tablet
    Expanded, // Tablet
    Large, // Large tablet or small desktop
    ExtraLarge, // Desktop and larger screens
}

/**
 * Get current screen size classification
 */
@Composable
fun getScreenSize(): ScreenSize {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp

    return when {
        screenWidth in ResponsiveBreakpoints.Compact -> ScreenSize.Compact
        screenWidth in ResponsiveBreakpoints.Medium -> ScreenSize.Medium
        screenWidth in ResponsiveBreakpoints.Expanded -> ScreenSize.Expanded
        screenWidth in ResponsiveBreakpoints.Large -> ScreenSize.Large
        else -> ScreenSize.ExtraLarge
    }
}

/**
 * Responsive padding values
 */
@Composable
fun responsivePadding(): PaddingValues {
    val screenSize = getScreenSize()

    return when (screenSize) {
        ScreenSize.Compact -> PaddingValues(16.dp)
        ScreenSize.Medium -> PaddingValues(24.dp)
        ScreenSize.Expanded -> PaddingValues(32.dp)
        ScreenSize.Large -> PaddingValues(40.dp)
        ScreenSize.ExtraLarge -> PaddingValues(48.dp)
    }
}

/**
 * Responsive content padding
 */
@Composable
fun responsiveContentPadding(): PaddingValues {
    val screenSize = getScreenSize()

    return when (screenSize) {
        ScreenSize.Compact -> PaddingValues(horizontal = 16.dp, vertical = 8.dp)
        ScreenSize.Medium -> PaddingValues(horizontal = 24.dp, vertical = 12.dp)
        ScreenSize.Expanded -> PaddingValues(horizontal = 32.dp, vertical = 16.dp)
        ScreenSize.Large -> PaddingValues(horizontal = 48.dp, vertical = 20.dp)
        ScreenSize.ExtraLarge -> PaddingValues(horizontal = 64.dp, vertical = 24.dp)
    }
}

/**
 * Get responsive column count for grid layouts
 */
@Composable
fun getGridColumnCount(
    minItemWidth: Dp = 280.dp,
    maxColumns: Int = 4,
): Int {
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp
    val contentPadding = responsiveContentPadding()
    val availableWidth = screenWidth - contentPadding.calculateStartPadding(androidx.compose.ui.unit.LayoutDirection.Ltr) -
        contentPadding.calculateEndPadding(androidx.compose.ui.unit.LayoutDirection.Ltr)

    val columns = (availableWidth / minItemWidth).toInt().coerceAtLeast(1)
    return columns.coerceAtMost(maxColumns)
}

/**
 * Responsive grid spacing
 */
@Composable
fun responsiveGridSpacing(): Dp {
    val screenSize = getScreenSize()

    return when (screenSize) {
        ScreenSize.Compact -> 8.dp
        ScreenSize.Medium -> 12.dp
        ScreenSize.Expanded -> 16.dp
        ScreenSize.Large -> 20.dp
        ScreenSize.ExtraLarge -> 24.dp
    }
}

/**
 * Responsive card width for single-column layouts
 */
@Composable
fun responsiveCardMaxWidth(): Dp {
    val screenSize = getScreenSize()

    return when (screenSize) {
        ScreenSize.Compact -> Dp.Unspecified
        ScreenSize.Medium -> 480.dp
        ScreenSize.Expanded -> 640.dp
        ScreenSize.Large -> 720.dp
        ScreenSize.ExtraLarge -> 800.dp
    }
}

/**
 * Adaptive grid layout that adjusts based on screen size
 */
@Composable
fun AdaptiveGrid(
    modifier: Modifier = Modifier,
    minItemWidth: Dp = 280.dp,
    maxColumns: Int = 4,
    verticalArrangement: Arrangement.Vertical = Arrangement.spacedBy(responsiveGridSpacing()),
    horizontalArrangement: Arrangement.Horizontal = Arrangement.spacedBy(responsiveGridSpacing()),
    contentPadding: PaddingValues = responsiveContentPadding(),
    content: LazyGridScope.() -> Unit,
) {
    val columnCount = getGridColumnCount(minItemWidth, maxColumns)

    LazyVerticalGrid(
        columns = GridCells.Fixed(columnCount),
        modifier = modifier,
        contentPadding = contentPadding,
        verticalArrangement = verticalArrangement,
        horizontalArrangement = horizontalArrangement,
        content = content,
    )
}

/**
 * Responsive two-panel layout for master-detail patterns
 */
@Composable
fun ResponsiveTwoPaneLayout(
    modifier: Modifier = Modifier,
    masterPane: @Composable (Modifier) -> Unit,
    detailPane: @Composable (Modifier) -> Unit,
    showDetailPane: Boolean = true,
    masterPaneMinWidth: Dp = 300.dp,
    detailPaneMinWidth: Dp = 400.dp,
) {
    val screenSize = getScreenSize()
    val configuration = LocalConfiguration.current
    val screenWidth = configuration.screenWidthDp.dp

    when {
        screenSize == ScreenSize.Compact || !showDetailPane -> {
            // Single pane on compact screens or when detail is hidden
            masterPane(modifier.fillMaxSize())
        }

        screenWidth >= (masterPaneMinWidth + detailPaneMinWidth + 48.dp) -> {
            // Two-pane layout when screen is wide enough
            Row(modifier = modifier.fillMaxSize()) {
                masterPane(
                    Modifier
                        .width(masterPaneMinWidth)
                        .fillMaxHeight(),
                )

                detailPane(
                    Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                )
            }
        }

        else -> {
            // Detail pane takes full screen on medium screens
            detailPane(modifier.fillMaxSize())
        }
    }
}

/**
 * Responsive constraint layout for complex UIs
 */
@Composable
fun ResponsiveConstraintLayout(
    modifier: Modifier = Modifier,
    content: @Composable ConstraintLayoutScope.() -> Unit,
) {
    val screenSize = getScreenSize()

    ConstraintLayout(
        modifier = modifier.then(
            when (screenSize) {
                ScreenSize.Compact -> Modifier.padding(16.dp)
                ScreenSize.Medium -> Modifier.padding(24.dp)
                ScreenSize.Expanded -> Modifier.padding(32.dp)
                ScreenSize.Large -> Modifier.padding(40.dp)
                ScreenSize.ExtraLarge -> Modifier.padding(48.dp)
            },
        ),
        content = content,
    )
}

/**
 * Adaptive spacing values
 */
object AdaptiveSpacing {

    @Composable
    fun small(): Dp {
        val screenSize = getScreenSize()
        return when (screenSize) {
            ScreenSize.Compact -> 4.dp
            ScreenSize.Medium -> 6.dp
            ScreenSize.Expanded -> 8.dp
            ScreenSize.Large -> 10.dp
            ScreenSize.ExtraLarge -> 12.dp
        }
    }

    @Composable
    fun medium(): Dp {
        val screenSize = getScreenSize()
        return when (screenSize) {
            ScreenSize.Compact -> 8.dp
            ScreenSize.Medium -> 12.dp
            ScreenSize.Expanded -> 16.dp
            ScreenSize.Large -> 20.dp
            ScreenSize.ExtraLarge -> 24.dp
        }
    }

    @Composable
    fun large(): Dp {
        val screenSize = getScreenSize()
        return when (screenSize) {
            ScreenSize.Compact -> 16.dp
            ScreenSize.Medium -> 20.dp
            ScreenSize.Expanded -> 24.dp
            ScreenSize.Large -> 32.dp
            ScreenSize.ExtraLarge -> 40.dp
        }
    }

    @Composable
    fun extraLarge(): Dp {
        val screenSize = getScreenSize()
        return when (screenSize) {
            ScreenSize.Compact -> 24.dp
            ScreenSize.Medium -> 32.dp
            ScreenSize.Expanded -> 40.dp
            ScreenSize.Large -> 48.dp
            ScreenSize.ExtraLarge -> 56.dp
        }
    }
}

/**
 * Window size-based utilities
 */
@Composable
fun WindowSizeClass.isCompact(): Boolean = widthSizeClass == WindowWidthSizeClass.Compact

@Composable
fun WindowSizeClass.isMedium(): Boolean = widthSizeClass == WindowWidthSizeClass.Medium

@Composable
fun WindowSizeClass.isExpanded(): Boolean = widthSizeClass == WindowWidthSizeClass.Expanded

/**
 * Responsive modifier extensions
 */
fun Modifier.responsiveWidth(
    compact: Dp = Dp.Unspecified,
    medium: Dp = Dp.Unspecified,
    expanded: Dp = Dp.Unspecified,
    large: Dp = Dp.Unspecified,
    extraLarge: Dp = Dp.Unspecified,
): Modifier = this.then(
    Modifier.width(
        // Note: This would need to be implemented with a custom modifier
        // that can access Compose's current configuration
        Dp.Unspecified,
    ),
)

/**
 * Responsive height modifier
 */
fun Modifier.responsiveHeight(
    compact: Dp = Dp.Unspecified,
    medium: Dp = Dp.Unspecified,
    expanded: Dp = Dp.Unspecified,
    large: Dp = Dp.Unspecified,
    extraLarge: Dp = Dp.Unspecified,
): Modifier = this.then(
    Modifier.height(
        // Note: This would need to be implemented with a custom modifier
        // that can access Compose's current configuration
        Dp.Unspecified,
    ),
)
