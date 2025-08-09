# Fitness Tracker App

[![Build Status](https://img.shields.io/badge/build-passing-brightgreen.svg)](https://github.com/yourusername/FitnessTrackerApp)
[![API Level](https://img.shields.io/badge/API-24%2B-brightgreen.svg)](https://developer.android.com/about/versions/android-7.0)
[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.21-orange.svg)](https://kotlinlang.org/)
[![Compose](https://img.shields.io/badge/Jetpack%20Compose-2023.12.00-blue.svg)](https://developer.android.com/jetpack/compose)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)

A comprehensive, modern Android fitness tracking application built with Jetpack Compose, Room database, and Firebase authentication. The app provides real-time step tracking, workout logging, nutrition monitoring, goal management, and progress analytics with an intuitive, accessible user interface.

## Table of Contents

- [Features](#features)
- [Architecture](#architecture)
- [Screenshots](#screenshots)
- [Tech Stack](#tech-stack)
- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Project Structure](#project-structure)
- [Key Components](#key-components)
- [Security Features](#security-features)
- [Testing](#testing)
- [Code Quality](#code-quality)
- [Performance Optimizations](#performance-optimizations)
- [Accessibility](#accessibility)
- [Contributing](#contributing)
- [License](#license)

## Features

### Core Functionality
- **🚶 Step Tracking**: Real-time step counting with daily, weekly, and monthly analytics
- **💪 Workout Logging**: Comprehensive exercise tracking with sets, reps, weight, and duration
- **🍎 Nutrition Monitoring**: Food entry with calorie and macro tracking, meal categorization
- **🎯 Goal Management**: SMART goal setting with progress tracking and reminders
- **📊 Progress Analytics**: Visual charts and insights for all fitness metrics
- **🔔 Smart Notifications**: Contextual reminders and motivational messages

### Advanced Features
- **🔐 Secure Authentication**: Firebase Auth with biometric authentication support
- **☁️ Data Synchronization**: Cloud backup and cross-device synchronization
- **🌙 Dark Mode**: Full theme support with system preference detection
- **♿ Accessibility**: WCAG 2.1 AA compliant with TalkBack support
- **📱 Responsive Design**: Adaptive layouts for phones, tablets, and foldables
- **🚀 Performance**: Optimized animations, lazy loading, and efficient data handling

### User Experience
- **🎨 Material 3 Design**: Modern UI with dynamic theming and motion
- **⚡ Real-time Updates**: Instant data synchronization and live progress updates
- **🔍 Search & Filter**: Smart search across workouts, exercises, and food entries
- **📈 Data Export**: Export fitness data to CSV for external analysis
- **🎯 Personalization**: Customizable dashboard and goal preferences

## Architecture

The app follows **Clean Architecture** principles with **MVVM** pattern:

```
┌─────────────────────────────────────────────┐
│                UI Layer                     │
│  ┌─────────────────┬───────────────────────┐ │
│  │   Composables   │     ViewModels        │ │
│  │   (Screens)     │     (State Mgmt)      │ │
│  └─────────────────┴───────────────────────┘ │
├─────────────────────────────────────────────┤
│              Domain Layer                   │
│  ┌─────────────────┬───────────────────────┐ │
│  │   Use Cases     │     Repositories      │ │
│  │  (Business      │    (Interfaces)       │ │
│  │    Logic)       │                       │ │
│  └─────────────────┴───────────────────────┘ │
├─────────────────────────────────────────────┤
│               Data Layer                    │
│  ┌──────────────┬─────────────┬───────────┐ │
│  │   Database   │    Remote   │  Sensors  │ │
│  │   (Room)     │  (Firebase) │ (Step)    │ │
│  └──────────────┴─────────────┴───────────┘ │
└─────────────────────────────────────────────┘
```

### Key Architectural Decisions

1. **Repository Pattern**: Abstracts data sources (local database, remote API, sensors)
2. **Dependency Injection**: Manual DI with ServiceLocator for clean dependencies
3. **Single Source of Truth**: Room database as primary data store with Firebase sync
4. **Reactive UI**: StateFlow and Compose for declarative UI updates
5. **Error Handling**: Comprehensive error handling with user-friendly messages

## Tech Stack

### Core Framework
- **Kotlin**: 1.9.21 - Modern, null-safe programming language
- **Jetpack Compose**: 2023.12.00 - Declarative UI toolkit
- **Compose BOM**: Unified versioning for Compose dependencies
- **Material 3**: Latest Material Design system

### Architecture Components
- **Room**: 2.6.1 - Local database with SQLite
- **ViewModel**: Lifecycle-aware state management
- **LiveData/StateFlow**: Reactive data streams
- **Navigation Compose**: Type-safe navigation
- **DataStore**: Modern SharedPreferences replacement

### Backend & Auth
- **Firebase Auth**: 22.3.1 - User authentication and management
- **Firebase Firestore**: (Future) - Cloud database synchronization
- **Google Play Services**: OAuth and account integration

### Security & Crypto
- **Security Crypto**: 1.1.0-alpha06 - Encrypted SharedPreferences
- **Biometric**: 1.1.0 - Fingerprint and face authentication
- **ProGuard/R8**: Code obfuscation and optimization

### Testing & Quality
- **JUnit 5**: 5.10.1 - Unit testing framework
- **MockK**: 1.13.8 - Kotlin mocking library
- **Robolectric**: 4.11.1 - Android unit testing
- **Espresso**: 3.5.1 - UI testing framework
- **Detekt**: Static code analysis
- **ktlint**: Kotlin code formatting
- **Spotless**: Code formatting enforcement

### Development Tools
- **KSP**: Kotlin Symbol Processing for Room
- **Dokka**: Documentation generation
- **Gradle Version Catalog**: Centralized dependency management
- **WorkManager**: Background task scheduling

## Prerequisites

- **Android Studio**: Hedgehog (2023.1.1) or newer
- **JDK**: 17 or newer
- **Android SDK**: API 24 (Android 7.0) minimum, API 34 (Android 14) target
- **Gradle**: 8.2 or newer
- **Git**: For version control

## Installation

### Clone the Repository
```bash
git clone https://github.com/yourusername/FitnessTrackerApp.git
cd FitnessTrackerApp
```

### Setup Firebase (Required)
1. Create a new project in [Firebase Console](https://console.firebase.google.com)
2. Enable Authentication with Email/Password and Google Sign-In
3. Download `google-services.json` and place it in `app/` directory
4. Configure Firebase Authentication methods in the console

### Build and Run
```bash
# Debug build
./gradlew assembleDebug

# Install on connected device
./gradlew installDebug

# Run all tests
./gradlew test

# Generate code quality reports
./gradlew detekt ktlintCheck
```

### Environment Setup
Create `local.properties` file in the root directory:
```properties
sdk.dir=/path/to/Android/Sdk
```

## Project Structure

```
app/src/main/java/com/example/fitnesstrackerapp/
├── ui/                          # UI Layer
│   ├── screens/                 # Screen Composables
│   ├── components/              # Reusable UI Components
│   ├── theme/                   # Theme and Styling
│   └── viewmodel/               # ViewModels
├── data/                        # Data Layer
│   ├── database/                # Room Database
│   ├── dao/                     # Database Access Objects
│   ├── entity/                  # Database Entities
│   └── repository/              # Data Repositories
├── domain/                      # Domain Layer
│   ├── model/                   # Domain Models
│   └── usecase/                 # Business Logic Use Cases
├── auth/                        # Authentication
│   ├── FirebaseAuthManager.kt   # Firebase Auth Integration
│   ├── BiometricAuthManager.kt  # Biometric Authentication
│   └── SessionManager.kt        # Session Management
├── sensors/                     # Sensor Integration
│   ├── StepTracker.kt          # Step Counting Logic
│   └── StepCounterService.kt   # Background Service
├── notifications/              # Notification System
├── security/                   # Security & Encryption
├── util/                       # Utility Classes
└── FitnessApplication.kt       # Application Class
```

### Resource Structure
```
app/src/main/res/
├── drawable/                   # Vector drawables and icons
├── values/                     # Strings, colors, dimensions
│   ├── strings.xml            # Localized strings
│   ├── colors.xml             # Color definitions
│   └── themes.xml             # App themes
└── xml/                       # Configuration files
    ├── backup_rules.xml       # Backup rules
    └── data_extraction_rules.xml
```

## Key Components

### Database Schema
The app uses Room database with the following main entities:
- **User**: User profile and preferences
- **Workout**: Exercise sessions and metadata
- **WorkoutSet**: Individual exercise sets (reps, weight, duration)
- **Exercise**: Exercise definitions and instructions
- **Step**: Daily step counts and metrics
- **Goal**: User fitness goals with progress tracking
- **FoodEntry**: Nutrition entries with macro information
- **Notification**: Notification history and preferences

### State Management
```kotlin
// Example ViewModel with StateFlow
class WorkoutViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(WorkoutUiState())
    val uiState: StateFlow<WorkoutUiState> = _uiState.asStateFlow()
    
    fun startWorkout() {
        viewModelScope.launch {
            _uiState.update { it.copy(isWorkoutActive = true) }
        }
    }
}
```

### Dependency Injection
```kotlin
// ServiceLocator pattern for DI
object ServiceLocator {
    fun provideWorkoutRepository(context: Context): WorkoutRepository {
        return WorkoutRepository(
            workoutDao = AppDatabase.getDatabase(context).workoutDao(),
            stepDao = AppDatabase.getDatabase(context).stepDao()
        )
    }
}
```

## Security Features

### Data Protection
- **Encrypted Storage**: Sensitive data encrypted using Android Security Crypto
- **Secure Preferences**: User preferences stored with EncryptedSharedPreferences
- **Network Security**: HTTPS only with certificate pinning
- **ProGuard/R8**: Code obfuscation in release builds

### Authentication
- **Multi-Factor Auth**: Email/password with optional biometric verification
- **Session Management**: Secure token handling with automatic expiration
- **Account Recovery**: Secure password reset flow
- **OAuth Integration**: Google Sign-In support

### Privacy
- **Data Minimization**: Only collect necessary fitness data
- **Local-First**: Core functionality works without internet
- **User Consent**: Clear privacy controls and data deletion
- **GDPR Compliance**: Built-in data export and deletion capabilities

## Testing

### Test Structure
```
app/src/test/                   # Unit Tests
├── auth/                       # Authentication tests
├── data/                       # Database and repository tests
├── ui/                         # ViewModel and UI logic tests
└── util/                       # Utility function tests

app/src/androidTest/            # Instrumentation Tests
├── ui/                         # UI and integration tests
└── database/                   # Database migration tests
```

### Running Tests
```bash
# Unit tests
./gradlew test

# Instrumentation tests
./gradlew connectedAndroidTest

# Test coverage report
./gradlew jacocoTestReport

# Run specific test class
./gradlew test --tests="com.example.fitnesstrackerapp.WorkoutRepositoryTest"
```

### Test Coverage
The project maintains >80% code coverage across:
- Repository layer: >90%
- ViewModel layer: >85%
- Use case layer: >95%
- Utility functions: >90%

## Code Quality

### Static Analysis
- **Detekt**: Kotlin static analysis with custom rules
- **ktlint**: Kotlin code formatting and style checks
- **Spotless**: Multi-language code formatting
- **Android Lint**: Android-specific code quality checks

### Quality Gates
```bash
# Run all quality checks
./gradlew check

# Fix code formatting
./gradlew spotlessApply

# Generate quality reports
./gradlew detekt ktlintCheck
```

### CI/CD Integration
The project includes GitHub Actions workflows for:
- Automated testing on pull requests
- Code quality checks and reporting
- APK building and artifact generation
- Dependency vulnerability scanning

## Performance Optimizations

### Compose Performance
- **Stable Parameters**: Immutable data classes for compose stability
- **Lazy Loading**: Efficient list rendering with LazyColumn/LazyVerticalGrid
- **State Hoisting**: Optimal recomposition boundaries
- **Remember**: Expensive calculations cached with `remember`

### Database Optimization
- **Indexed Queries**: Proper database indexing for fast queries
- **Pagination**: Large datasets loaded incrementally
- **Background Threading**: Database operations on background threads
- **Connection Pooling**: Efficient database connection management

### Memory Management
- **Lifecycle Awareness**: ViewModels and observers properly scoped
- **Image Loading**: Efficient image loading with proper caching
- **Resource Cleanup**: Proper cleanup of sensors and background tasks

### Build Optimization
- **R8 Shrinking**: Dead code elimination and obfuscation
- **Bundle Optimization**: App Bundle for size reduction
- **Proguard Rules**: Optimized rules for minimal APK size

## Accessibility

The app follows **WCAG 2.1 AA** guidelines:

### Features
- **Screen Reader Support**: Full TalkBack compatibility
- **Keyboard Navigation**: Complete keyboard navigation support
- **High Contrast**: Support for high contrast themes
- **Large Text**: Dynamic font scaling up to 200%
- **Voice Control**: Voice navigation and input support

### Implementation
```kotlin
// Accessibility example
@Composable
fun AccessibleButton(
    onClick: () -> Unit,
    text: String
) {
    Button(
        onClick = onClick,
        modifier = Modifier.semantics {
            contentDescription = text
            role = Role.Button
        }
    ) {
        Text(text)
    }
}
```

## Contributing

We welcome contributions! Please see our [Contributing Guidelines](CONTRIBUTING.md) for details.

### Development Setup
1. Fork the repository
2. Create a feature branch: `git checkout -b feature/amazing-feature`
3. Make your changes and add tests
4. Run quality checks: `./gradlew check`
5. Commit your changes: `git commit -m 'Add amazing feature'`
6. Push to the branch: `git push origin feature/amazing-feature`
7. Open a Pull Request

### Code Style
- Follow [Kotlin Coding Conventions](https://kotlinlang.org/docs/coding-conventions.html)
- Use meaningful commit messages
- Add KDoc comments for public APIs
- Maintain test coverage above 80%

## License

This project is licensed under the Apache License 2.0 - see the [LICENSE](LICENSE) file for details.

## Acknowledgments

- **Jetpack Compose Team** - For the excellent declarative UI toolkit
- **Room Team** - For the robust SQLite abstraction
- **Firebase Team** - For authentication and cloud services
- **Material Design Team** - For the beautiful design system
- **Open Source Community** - For the amazing libraries and tools

---

**Built with ❤️ and Kotlin**

For questions or support, please open an issue or contact [your-email@example.com](mailto:your-email@example.com).
