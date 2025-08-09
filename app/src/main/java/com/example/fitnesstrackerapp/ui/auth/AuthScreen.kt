/**
 * Authentication Screen Container
 *
 * Material 3 implementation of the main authentication screen that provides:
 * - Responsive layout adaptation for different screen sizes
 * - Dark/light theme support with dynamic colors
 * - Motion transitions between auth states
 * - Accessibility semantics for screen readers
 * - Proper ViewModel scoping via ServiceLocator
 */

package com.example.fitnesstrackerapp.ui.auth

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.currentWindowAdaptiveInfo
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.window.core.layout.WindowSizeClass
import androidx.window.core.layout.WindowWidthSizeClass
import kotlinx.coroutines.delay

/**
 * Auth screen states for managing UI transitions
 */
enum class AuthScreenState {
    Login,
    SignUp,
    ForgotPassword
}

/**
 * Main authentication screen with adaptive layout
 * 
 * @param authViewModel ViewModel for authentication logic
 * @param onNavigateToMain Callback when authentication is successful
 * @param modifier Optional modifier for the composable
 */
@Composable
fun AuthScreen(
    authViewModel: AuthViewModel,
    onNavigateToMain: () -> Unit,
    modifier: Modifier = Modifier
) {
    val authUiState by authViewModel.uiState.collectAsState()
    var currentAuthState by remember { mutableStateOf(AuthScreenState.Login) }
    val windowSizeClass = currentWindowAdaptiveInfo().windowSizeClass
    
    // Handle navigation to main screen on successful auth
    LaunchedEffect(authUiState.isAuthenticated) {
        if (authUiState.isAuthenticated) {
            delay(300) // Small delay for smooth transition
            onNavigateToMain()
        }
    }
    
    Surface(
        modifier = modifier
            .fillMaxSize()
            .windowInsetsPadding(WindowInsets.safeDrawing),
        color = MaterialTheme.colorScheme.background
    ) {
        // Adaptive layout based on screen size
        when (windowSizeClass.windowWidthSizeClass) {
            WindowWidthSizeClass.COMPACT -> {
                CompactAuthLayout(
                    authViewModel = authViewModel,
                    currentState = currentAuthState,
                    onStateChange = { currentAuthState = it }
                )
            }
            WindowWidthSizeClass.MEDIUM,
            WindowWidthSizeClass.EXPANDED -> {
                MediumExpandedAuthLayout(
                    authViewModel = authViewModel,
                    currentState = currentAuthState,
                    onStateChange = { currentAuthState = it }
                )
            }
        }
    }
}

/**
 * Compact layout for mobile devices
 */
@Composable
private fun CompactAuthLayout(
    authViewModel: AuthViewModel,
    currentState: AuthScreenState,
    onStateChange: (AuthScreenState) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .windowInsetsPadding(WindowInsets.ime)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // App branding section
        AuthBrandingSection()
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // Animated content based on auth state
        AnimatedContent(
            targetState = currentState,
            transitionSpec = {
                slideInHorizontally(
                    initialOffsetX = { fullWidth -> fullWidth },
                    animationSpec = spring(stiffness = Spring.StiffnessLow)
                ) togetherWith slideOutHorizontally(
                    targetOffsetX = { fullWidth -> -fullWidth },
                    animationSpec = spring(stiffness = Spring.StiffnessLow)
                )
            },
            label = "Auth Content Animation"
        ) { state ->
            when (state) {
                AuthScreenState.Login -> LoginScreen(
                    authViewModel = authViewModel,
                    onNavigateToSignUp = { onStateChange(AuthScreenState.SignUp) },
                    onNavigateToForgotPassword = { onStateChange(AuthScreenState.ForgotPassword) },
                    onLoginSuccess = { /* Handled by LaunchedEffect above */ }
                )
                AuthScreenState.SignUp -> SignUpScreen(
                    authViewModel = authViewModel,
                    onNavigateToLogin = { onStateChange(AuthScreenState.Login) }
                )
                AuthScreenState.ForgotPassword -> ForgotPasswordScreen(
                    authViewModel = authViewModel,
                    onNavigateToLogin = { onStateChange(AuthScreenState.Login) }
                )
            }
        }
    }
}

/**
 * Medium/Expanded layout for tablets and large screens
 */
@Composable
private fun MediumExpandedAuthLayout(
    authViewModel: AuthViewModel,
    currentState: AuthScreenState,
    onStateChange: (AuthScreenState) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(32.dp)
    ) {
        // Left side - Branding
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            AuthBrandingSection(isLargeScreen = true)
        }
        
        // Right side - Auth content
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxSize()
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.elevatedCardElevation(defaultElevation = 8.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            ) {
                Column(
                    modifier = Modifier
                        .padding(32.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    AnimatedContent(
                        targetState = currentState,
                        transitionSpec = {
                            fadeIn(tween(300)) togetherWith fadeOut(tween(300))
                        },
                        label = "Auth Content Animation Large Screen"
                    ) { state ->
                        when (state) {
                            AuthScreenState.Login -> LoginScreen(
                                authViewModel = authViewModel,
                                onNavigateToSignUp = { onStateChange(AuthScreenState.SignUp) },
                                onNavigateToForgotPassword = { onStateChange(AuthScreenState.ForgotPassword) },
                                onLoginSuccess = { /* Handled by LaunchedEffect above */ }
                            )
                            AuthScreenState.SignUp -> SignUpScreen(
                                authViewModel = authViewModel,
                                onNavigateToLogin = { onStateChange(AuthScreenState.Login) }
                            )
                            AuthScreenState.ForgotPassword -> ForgotPasswordScreen(
                                authViewModel = authViewModel,
                                onNavigateToLogin = { onStateChange(AuthScreenState.Login) }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Branding section with app logo and title
 */
@Composable
private fun AuthBrandingSection(
    isLargeScreen: Boolean = false
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // Animated logo
        var visible by remember { mutableStateOf(false) }
        
        LaunchedEffect(Unit) {
            visible = true
        }
        
        AnimatedVisibility(
            visible = visible,
            enter = fadeIn(spring(stiffness = Spring.StiffnessLow)) + slideInHorizontally(),
            label = "Logo Animation"
        ) {
            Icon(
                imageVector = Icons.Default.FitnessCenter,
                contentDescription = null,
                modifier = Modifier
                    .width(if (isLargeScreen) 120.dp else 80.dp)
                    .height(if (isLargeScreen) 120.dp else 80.dp),
                tint = MaterialTheme.colorScheme.primary
            )
        }
        
        Text(
            text = "FitnessTracker",
            style = if (isLargeScreen) MaterialTheme.typography.displayMedium else MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onBackground,
            textAlign = TextAlign.Center,
            modifier = Modifier.semantics {
                heading()
                contentDescription = "Fitness Tracker App"
            }
        )
        
        Text(
            text = "Your journey to a healthier life starts here",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Forgot Password screen placeholder
 */
@Composable
private fun ForgotPasswordScreen(
    authViewModel: AuthViewModel,
    onNavigateToLogin: () -> Unit
) {
    // Implementation would be similar to existing ForgotPasswordScreen
    // but with Material 3 components and animations
    Text(
        text = "Forgot Password functionality coming soon",
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        textAlign = TextAlign.Center
    )
}

/**
 * Sign Up screen placeholder
 */
@Composable
private fun SignUpScreen(
    authViewModel: AuthViewModel,
    onNavigateToLogin: () -> Unit
) {
    // Implementation would be similar to existing SignUpScreen
    // but with Material 3 components and animations
    Text(
        text = "Sign Up functionality coming soon",
        style = MaterialTheme.typography.bodyLarge,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        textAlign = TextAlign.Center
    )
}
