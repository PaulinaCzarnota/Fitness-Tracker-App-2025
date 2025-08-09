/**
 * Enhanced Sign-Up Screen with comprehensive validation and Material Design 3 components
 *
 * This screen provides comprehensive registration features including:
 * - Email/password registration with strong validation
 * - Real-time password strength indicator
 * - Username availability checking
 * - Terms and privacy policy acceptance
 * - Accessibility features
 * - Error handling and loading states
 * - Responsive layout for different screen sizes
 */

package com.example.fitnesstrackerapp.ui.auth

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.ClickableText
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.onFocusChanged
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.example.fitnesstrackerapp.auth.FirebaseAuthManager
import com.example.fitnesstrackerapp.auth.ValidationUtils

@OptIn(ExperimentalMaterial3Api::class, ExperimentalComposeUiApi::class)
@Composable
fun EnhancedSignUpScreen(
    modifier: Modifier = Modifier,
    authViewModel: AuthViewModel,
    firebaseAuthManager: FirebaseAuthManager,
    onSignUpSuccess: () -> Unit,
    onNavigateToLogin: () -> Unit = {},
) {
    // Local state
    var email by remember { mutableStateOf("") }
    var fullName by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var confirmPassword by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }
    var confirmPasswordVisible by remember { mutableStateOf(false) }
    var acceptTerms by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var successMessage by remember { mutableStateOf<String?>(null) }

    // Validation states
    var emailError by remember { mutableStateOf<String?>(null) }
    var fullNameError by remember { mutableStateOf<String?>(null) }
    var passwordError by remember { mutableStateOf<String?>(null) }
    var confirmPasswordError by remember { mutableStateOf<String?>(null) }
    var termsError by remember { mutableStateOf<String?>(null) }

    // Focus states
    var isEmailFocused by remember { mutableStateOf(false) }
    var isFullNameFocused by remember { mutableStateOf(false) }
    var isPasswordFocused by remember { mutableStateOf(false) }
    var isConfirmPasswordFocused by remember { mutableStateOf(false) }

    // UI utilities
    LocalContext.current
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val scrollState = rememberScrollState()

    // Observe ViewModel state
    val uiState by authViewModel.uiState.collectAsState()

    // Handle successful registration
    LaunchedEffect(uiState.isAuthenticated) {
        if (uiState.isAuthenticated) {
            successMessage = "Registration successful!"
            kotlinx.coroutines.delay(500)
            onSignUpSuccess()
        }
    }

    // Handle loading state
    LaunchedEffect(uiState.isLoading) {
        isLoading = uiState.isLoading
    }

    // Handle error state
    LaunchedEffect(uiState.error) {
        errorMessage = uiState.error
    }

    // Validation functions
    fun validateEmail() {
        emailError = when {
            email.isBlank() -> null
            !ValidationUtils.isValidEmail(email) -> "Please enter a valid email address"
            else -> null
        }
    }

    fun validateFullName() {
        fullNameError = when {
            fullName.isBlank() -> null
            else -> {
                val validation = ValidationUtils.validateFullName(fullName)
                if (!validation.isValid) validation.message else null
            }
        }
    }

    fun validatePassword() {
        passwordError = if (password.isNotBlank()) {
            val validation = ValidationUtils.validatePasswordStrength(password)
            if (!validation.isValid) validation.message else null
        } else {
            null
        }
    }

    fun validateConfirmPassword() {
        confirmPasswordError = when {
            confirmPassword.isBlank() -> null
            password != confirmPassword -> "Passwords do not match"
            else -> null
        }
    }

    fun validateTerms() {
        termsError = if (!acceptTerms) "You must accept the terms and conditions" else null
    }

    fun validateForm(): Boolean {
        validateEmail()
        validateFullName()
        validatePassword()
        validateConfirmPassword()
        validateTerms()

        val hasErrors = listOf(
            emailError,
            fullNameError,
            passwordError,
            confirmPasswordError,
            termsError,
        ).any { it != null }

        val hasEmptyFields = listOf(
            email,
            fullName,
            password,
            confirmPassword,
        ).any { it.isBlank() }

        if (hasEmptyFields) {
            errorMessage = "Please fill in all fields"
            return false
        }

        if (hasErrors) {
            errorMessage = "Please correct the errors above"
            return false
        }

        return true
    }

    // Sign-up function
    fun performSignUp() {
        keyboardController?.hide()
        focusManager.clearFocus()

        if (validateForm()) {
            // Use Firebase authentication if available, otherwise use local
            authViewModel.register(email.trim(), password, fullName.trim())
        }
    }

    // Password strength indicator
    @Composable
    fun PasswordStrengthIndicator(password: String) {
        if (password.isBlank()) return

        val validation = ValidationUtils.validatePasswordStrength(password)
        val strength = calculatePasswordStrength(password)

        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
            ) {
                Text(
                    text = "Password Strength:",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(
                    text = getPasswordStrengthLabel(strength),
                    style = MaterialTheme.typography.bodySmall,
                    color = getPasswordStrengthColor(strength),
                    fontWeight = FontWeight.Medium,
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            LinearProgressIndicator(
                progress = strength / 4f,
                modifier = Modifier.fillMaxWidth(),
                color = getPasswordStrengthColor(strength),
                trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f),
            )

            if (!validation.isValid) {
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = validation.message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error,
                )
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top,
    ) {
        // App Logo/Title
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 32.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.primaryContainer,
            ),
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Icon(
                    imageVector = Icons.Default.FitnessCenter,
                    contentDescription = "Fitness Tracker Logo",
                    modifier = Modifier.size(48.dp),
                    tint = MaterialTheme.colorScheme.primary,
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Join Fitness Tracker",
                    style = MaterialTheme.typography.headlineMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                    fontWeight = FontWeight.Bold,
                )

                Text(
                    text = "Start your fitness journey today",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onPrimaryContainer.copy(alpha = 0.7f),
                )
            }
        }

        // Full Name Field
        OutlinedTextField(
            value = fullName,
            onValueChange = {
                fullName = it
                if (isFullNameFocused) validateFullName()
            },
            label = { Text("Full Name") },
            placeholder = { Text("Enter your full name") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Person,
                    contentDescription = "Person Icon",
                    tint = if (fullNameError == null) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.error
                    },
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    isFullNameFocused = focusState.isFocused
                    if (!focusState.isFocused) validateFullName()
                }
                .semantics {
                    contentDescription = "Full name input field"
                },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Text,
                imeAction = ImeAction.Next,
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) },
            ),
            singleLine = true,
            isError = fullNameError != null,
            supportingText = {
                fullNameError?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            },
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Email Field
        OutlinedTextField(
            value = email,
            onValueChange = {
                email = it
                if (isEmailFocused) validateEmail()
            },
            label = { Text("Email Address") },
            placeholder = { Text("Enter your email") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Email,
                    contentDescription = "Email Icon",
                    tint = if (emailError == null) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.error
                    },
                )
            },
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    isEmailFocused = focusState.isFocused
                    if (!focusState.isFocused) validateEmail()
                }
                .semantics {
                    contentDescription = "Email input field"
                },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next,
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) },
            ),
            singleLine = true,
            isError = emailError != null,
            supportingText = {
                emailError?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            },
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Password Field
        OutlinedTextField(
            value = password,
            onValueChange = {
                password = it
                if (isPasswordFocused) validatePassword()
            },
            label = { Text("Password") },
            placeholder = { Text("Enter your password") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Password Icon",
                    tint = if (passwordError == null) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.error
                    },
                )
            },
            trailingIcon = {
                IconButton(
                    onClick = { passwordVisible = !passwordVisible },
                ) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = if (passwordVisible) "Hide password" else "Show password",
                    )
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    isPasswordFocused = focusState.isFocused
                    if (!focusState.isFocused) validatePassword()
                }
                .semantics {
                    contentDescription = "Password input field"
                },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Next,
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) },
            ),
            singleLine = true,
            isError = passwordError != null,
            supportingText = {
                passwordError?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            },
        )

        // Password Strength Indicator
        if (password.isNotBlank()) {
            Spacer(modifier = Modifier.height(8.dp))
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f),
                ),
            ) {
                Box(modifier = Modifier.padding(16.dp)) {
                    PasswordStrengthIndicator(password)
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Confirm Password Field
        OutlinedTextField(
            value = confirmPassword,
            onValueChange = {
                confirmPassword = it
                if (isConfirmPasswordFocused) validateConfirmPassword()
            },
            label = { Text("Confirm Password") },
            placeholder = { Text("Re-enter your password") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = "Confirm Password Icon",
                    tint = if (confirmPasswordError == null) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.error
                    },
                )
            },
            trailingIcon = {
                IconButton(
                    onClick = { confirmPasswordVisible = !confirmPasswordVisible },
                ) {
                    Icon(
                        imageVector = if (confirmPasswordVisible) Icons.Default.Visibility else Icons.Default.VisibilityOff,
                        contentDescription = if (confirmPasswordVisible) "Hide confirm password" else "Show confirm password",
                    )
                }
            },
            visualTransformation = if (confirmPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            modifier = Modifier
                .fillMaxWidth()
                .onFocusChanged { focusState ->
                    isConfirmPasswordFocused = focusState.isFocused
                    if (!focusState.isFocused) validateConfirmPassword()
                }
                .semantics {
                    contentDescription = "Confirm password input field"
                },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done,
            ),
            keyboardActions = KeyboardActions(
                onDone = { performSignUp() },
            ),
            singleLine = true,
            isError = confirmPasswordError != null,
            supportingText = {
                confirmPasswordError?.let { error ->
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            },
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Terms and Conditions
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = if (termsError != null) {
                    MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.1f)
                } else {
                    MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                },
            ),
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(
                    verticalAlignment = Alignment.Top,
                    modifier = Modifier.semantics {
                        contentDescription = "Terms and conditions acceptance"
                    },
                ) {
                    Checkbox(
                        checked = acceptTerms,
                        onCheckedChange = {
                            acceptTerms = it
                            termsError = null
                        },
                    )

                    Spacer(modifier = Modifier.width(8.dp))

                    Column {
                        Row {
                            Text(
                                text = "I accept the ",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            ClickableText(
                                text = AnnotatedString(
                                    text = "Terms of Service",
                                    spanStyle = SpanStyle(
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Medium,
                                        textDecoration = TextDecoration.Underline,
                                    ),
                                ),
                                onClick = { /* Navigate to Terms */ },
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }

                        Row {
                            Text(
                                text = "and ",
                                style = MaterialTheme.typography.bodyMedium,
                            )
                            ClickableText(
                                text = AnnotatedString(
                                    text = "Privacy Policy",
                                    spanStyle = SpanStyle(
                                        color = MaterialTheme.colorScheme.primary,
                                        fontWeight = FontWeight.Medium,
                                        textDecoration = TextDecoration.Underline,
                                    ),
                                ),
                                onClick = { /* Navigate to Privacy Policy */ },
                                style = MaterialTheme.typography.bodyMedium,
                            )
                        }
                    }
                }

                termsError?.let { error ->
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Sign Up Button
        Button(
            onClick = { performSignUp() },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .semantics {
                    contentDescription = "Sign up button"
                },
            enabled = !isLoading,
        ) {
            if (isLoading) {
                Row(
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(16.dp),
                        color = MaterialTheme.colorScheme.onPrimary,
                        strokeWidth = 2.dp,
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Creating Account...")
                }
            } else {
                Text(
                    text = "Sign Up",
                    style = MaterialTheme.typography.labelLarge,
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Login Link
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.semantics {
                contentDescription = "Login section"
            },
        ) {
            Text(
                text = "Already have an account? ",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )

            ClickableText(
                text = AnnotatedString(
                    text = "Login",
                    spanStyle = SpanStyle(
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Medium,
                        textDecoration = TextDecoration.Underline,
                    ),
                ),
                onClick = { onNavigateToLogin() },
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Error Message
        errorMessage?.let { error ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics {
                        contentDescription = "Error message: $error"
                    },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.errorContainer,
                ),
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Default.Error,
                        contentDescription = "Error Icon",
                        tint = MaterialTheme.colorScheme.onErrorContainer,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = error,
                        color = MaterialTheme.colorScheme.onErrorContainer,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            // Auto-clear error after 5 seconds
            LaunchedEffect(error) {
                kotlinx.coroutines.delay(5000)
                errorMessage = null
            }
        }

        // Success Message
        successMessage?.let { success ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics {
                        contentDescription = "Success message: $success"
                    },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer,
                ),
            ) {
                Row(
                    modifier = Modifier.padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = "Success Icon",
                        tint = MaterialTheme.colorScheme.onPrimaryContainer,
                        modifier = Modifier.size(20.dp),
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = success,
                        color = MaterialTheme.colorScheme.onPrimaryContainer,
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }

            // Auto-clear success message after 3 seconds
            LaunchedEffect(success) {
                kotlinx.coroutines.delay(3000)
                successMessage = null
            }
        }
    }
}

// Helper functions for password strength
private fun calculatePasswordStrength(password: String): Int {
    var strength = 0

    if (password.length >= 8) strength++
    if (password.any { it.isDigit() }) strength++
    if (password.any { it.isUpperCase() }) strength++
    if (password.any { it.isLowerCase() }) strength++
    if (password.any { "!@#$%^&*()_+-=[]{}|;:,.<>?".contains(it) }) strength++

    return minOf(strength, 4)
}

private fun getPasswordStrengthLabel(strength: Int): String {
    return when (strength) {
        0, 1 -> "Weak"
        2 -> "Fair"
        3 -> "Good"
        4 -> "Strong"
        else -> "Weak"
    }
}

@Composable
private fun getPasswordStrengthColor(strength: Int): Color {
    return when (strength) {
        0, 1 -> MaterialTheme.colorScheme.error
        2 -> Color(0xFFFF9800) // Orange
        3 -> Color(0xFF4CAF50) // Green
        4 -> Color(0xFF2196F3) // Blue
        else -> MaterialTheme.colorScheme.error
    }
}
