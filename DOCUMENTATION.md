# Fitness Tracker App - Technical Documentation

**Author:** Paulina Czarnota
**Student Number:** C21365726
**Course:** TU856/3 Mobile Software Development
**Due Date:** 11/08/2025

---

## Table of Contents
1. [Project Overview](#project-overview)
2. [Architecture & Design Patterns](#architecture--design-patterns)
3. [UI/UX Design](#uiux-design)
4. [Navigation Structure](#navigation-structure)
5. [Data Layer](#data-layer)
6. [Security Implementation](#security-implementation)
7. [Key Features](#key-features)
8. [Testing Strategy](#testing-strategy)
9. [Build Configuration](#build-configuration)

## Project Overview

The Fitness Tracker App is a comprehensive Android application built with Kotlin and Jetpack Compose that enables users to track their fitness activities, set goals, monitor nutrition, and maintain a healthy lifestyle.

### Key Technologies
- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM with Repository Pattern
- **Database**: Room (SQLite)
- **Dependency Injection**: Hilt
- **Asynchronous Programming**: Coroutines & Flow
- **Charts**: Vico Chart Library
- **Security**: Android Keystore, Biometric Authentication

## Architecture & Design Patterns

### MVVM Architecture
The app follows the Model-View-ViewModel (MVVM) architectural pattern:

```
┌─────────────────┐    ┌─────────────────┐    ┌─────────────────┐
│   Composables   │───▶│   ViewModels    │───▶│  Repositories   │
│   (UI Layer)    │    │ (Presentation)  │    │ (Domain Layer)  │
└─────────────────┘    └─────────────────┘    └─────────────────┘
                                                        │
                                               ┌─────────────────┐
                                               │   Data Sources  │
                                               │ (Room, Sensors) │
                                               └─────────────────┘
```

### Repository Pattern
Each domain has its own repository that abstracts data access:
- `AuthRepository`: User authentication and session management
- `WorkoutRepository`: Workout data management
- `GoalRepository`: Goal tracking and progress
- `NutritionRepository`: Food intake and nutrition data
- `StepRepository`: Step counting and activity tracking

### Dependency Injection with Hilt
All dependencies are managed through Hilt modules:
- `DatabaseModule`: Room database and DAOs
- `RepositoryModule`: Repository implementations
- `NetworkModule`: Future API integrations
- `SecurityModule`: Cryptographic services

## UI/UX Design

### Design System
The app uses Material 3 design system with:
- **Dynamic Color**: Adapts to system theme
- **Typography**: Material 3 type scale
- **Spacing**: Consistent 8dp grid system
- **Elevation**: Material 3 elevation tokens

### Theme Implementation
```kotlin
// Custom theme with dynamic colors
@Composable
fun FitnessTrackerTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = true,
    content: @Composable () -> Unit
)
```

### Responsive Design
- **Adaptive Layouts**: Different layouts for phones and tablets
- **Screen Size Handling**: Compact, medium, and expanded window classes
- **Orientation Support**: Portrait and landscape modes
- **Accessibility**: Full screen reader support and high contrast mode

### Key UI Components

#### Custom Composables
1. **StatCard**: Displays key metrics with icons and values
2. **WorkoutCard**: Shows workout summaries with progress indicators
3. **GoalProgressCard**: Visual goal progress with circular indicators
4. **NutritionSummaryCard**: Daily nutrition overview
5. **ChartContainer**: Reusable chart wrapper with consistent styling

#### Navigation Components
- **BottomNavigationBar**: Primary navigation with 5 tabs
- **TopAppBar**: Context-aware app bar with actions
- **FloatingActionButton**: Quick access to primary actions

## Navigation Structure

### Navigation Graph
```
MainActivity
├── AuthNavigation (Conditional)
│   ├── LoginScreen
│   ├── SignUpScreen
│   └── ForgotPasswordScreen
└── MainNavigation
    ├── DashboardScreen (Home)
    ├── WorkoutScreen
    ├── ProgressScreen
    ├── GoalsScreen
    ├── NutritionScreen
    └── SettingsScreen
```

### Navigation Implementation
- **Jetpack Navigation Compose**: Type-safe navigation
- **Deep Linking**: Support for external app links
- **State Preservation**: Maintains state across navigation
- **Conditional Navigation**: Auth flow vs main app flow

## Data Layer

### Room Database Schema

#### Core Entities
1. **User**: User profile and authentication data
2. **Workout**: Exercise sessions with metrics
3. **Goal**: User-defined fitness objectives
4. **FoodEntry**: Nutrition tracking entries
5. **Step**: Daily step count records
6. **Notification**: Scheduled reminders

#### Entity Relationships
```sql
User (1) ──── (N) Workout
User (1) ──── (N) Goal
User (1) ──── (N) FoodEntry
User (1) ──── (N) Step
User (1) ──── (N) Notification
```

### Data Access Objects (DAOs)
Each entity has a corresponding DAO with:
- **CRUD Operations**: Create, Read, Update, Delete
- **Complex Queries**: Aggregations, date ranges, search
- **Reactive Queries**: Flow-based for real-time updates

### Type Converters
Custom converters for complex data types:
- `DateConverter`: Date ↔ Long
- `WorkoutTypeConverter`: Enum ↔ String
- `GoalTypeConverter`: Enum ↔ String
- `MealTypeConverter`: Enum ↔ String

## Security Implementation

### Authentication Security
- **Password Hashing**: PBKDF2 with SHA-256
- **Salt Generation**: Cryptographically secure random salts
- **Session Management**: Secure token-based sessions
- **Biometric Authentication**: Fingerprint and face unlock

### Data Encryption
- **Android Keystore**: Hardware-backed key storage
- **AES Encryption**: 256-bit encryption for sensitive data
- **Encrypted SharedPreferences**: Secure local storage

### Security Features
```kotlin
class CryptoManager {
    fun generateSalt(): String
    fun hashPassword(password: String, salt: String): String
    fun encrypt(data: String): CryptoResult
    fun decrypt(encryptedData: String): CryptoResult
}
```

## Key Features

### 1. Workout Tracking
- **Multiple Workout Types**: Running, cycling, weightlifting, yoga
- **Real-time Metrics**: Duration, distance, calories, heart rate zones
- **Step Integration**: Phone sensor-based step counting
- **GPS Tracking**: Route recording for outdoor activities

### 2. Goal Management
- **Flexible Goal Types**: Distance, frequency, weight, time-based
- **Progress Tracking**: Visual progress indicators
- **Smart Reminders**: Context-aware notifications
- **Achievement System**: Milestone celebrations

### 3. Nutrition Tracking
- **Food Database**: Comprehensive nutrition information
- **Macro Tracking**: Protein, carbs, fat, calories
- **Meal Planning**: Breakfast, lunch, dinner, snacks
- **Nutrition Analysis**: Daily/weekly summaries

### 4. Progress Analytics
- **Interactive Charts**: Line, bar, and pie charts
- **Time Periods**: Daily, weekly, monthly, yearly views
- **Trend Analysis**: Progress trends and insights
- **Export Functionality**: Data export capabilities

### 5. Smart Notifications
- **Workout Reminders**: Scheduled based on user preferences
- **Goal Notifications**: Progress updates and encouragement
- **Motivational Messages**: Daily fitness tips and motivation
- **Achievement Alerts**: Goal completion celebrations

## Testing Strategy

### Test Structure
```
src/test/
├── unit/
│   ├── viewmodel/     # ViewModel unit tests
│   ├── repository/    # Repository tests with mocks
│   ├── util/          # Utility function tests
│   └── security/      # Cryptography tests
└── integration/
    ├── database/      # Room database tests
    ├── auth/          # Authentication flow tests
    └── navigation/    # Navigation tests
```

### Testing Tools
- **JUnit 4**: Core testing framework
- **Mockito**: Mocking framework
- **Robolectric**: Android unit testing
- **Truth**: Fluent assertions
- **Coroutines Test**: Async testing utilities

### Test Coverage Areas
1. **Business Logic**: ViewModels and repositories
2. **Data Layer**: Database operations and migrations
3. **Security**: Encryption and authentication
4. **Utilities**: Helper functions and extensions
5. **Integration**: End-to-end user flows

## Build Configuration

### Gradle Configuration
- **Kotlin DSL**: Modern Gradle configuration
- **Version Catalogs**: Centralized dependency management
- **Build Variants**: Debug, release, and staging
- **Code Quality**: Detekt static analysis
- **Proguard**: Code obfuscation for release builds

### Dependencies Management
Key dependencies organized by category:
- **Core**: Kotlin, Coroutines, Lifecycle
- **UI**: Compose, Material 3, Navigation
- **Data**: Room, Hilt, DataStore
- **Security**: Biometric, Keystore
- **Charts**: Vico charting library
- **Testing**: JUnit, Mockito, Robolectric

### Build Optimization
- **R8 Optimization**: Code shrinking and obfuscation
- **Resource Optimization**: Unused resource removal
- **APK Splitting**: Architecture-specific APKs
- **Bundle Generation**: Android App Bundle support

## Detailed Component Documentation

### ViewModels Architecture

#### AuthViewModel
Manages authentication state and user operations:
- **State Management**: Reactive authentication state using StateFlow
- **Operations**: Sign in, sign up, password reset, biometric auth
- **Session Handling**: Automatic session validation and refresh
- **Error Handling**: Comprehensive error states and user feedback

#### WorkoutViewModel
Handles workout tracking and management:
- **Real-time Tracking**: Live workout session monitoring
- **Data Persistence**: Automatic workout saving and history
- **Step Integration**: Phone sensor step counting integration
- **Statistics**: Workout analytics and progress calculations

#### GoalViewModel
Manages user fitness goals and progress:
- **Goal Creation**: Flexible goal types (distance, frequency, weight)
- **Progress Tracking**: Real-time progress updates and calculations
- **Notifications**: Smart reminder scheduling based on progress
- **Achievement System**: Goal completion celebrations and milestones

### Database Schema Details

#### Core Entities Structure
```
User Entity:
- id (Primary Key)
- email (Unique)
- username
- passwordHash
- salt
- profile data (age, weight, height)
- preferences
- createdAt, updatedAt

Workout Entity:
- id (Primary Key)
- userId (Foreign Key)
- title, type, startTime, endTime
- metrics (duration, distance, calories)
- sensor data (steps, heart rate)
- notes, location data

Goal Entity:
- id (Primary Key)
- userId (Foreign Key)
- title, description, type
- target/current values, unit
- dates (created, target, completed)
- status and progress tracking
```

### UI Component Library

#### Reusable Components
1. **StatCard**: Metric display with icons and values
2. **WorkoutCard**: Workout summary with progress indicators
3. **GoalProgressCard**: Visual goal progress with animations
4. **NutritionSummaryCard**: Daily nutrition overview
5. **ChartContainer**: Consistent chart styling and interactions

#### Navigation Components
- **BottomNavigationBar**: 5-tab primary navigation
- **TopAppBar**: Context-aware with dynamic actions
- **FloatingActionButton**: Quick access to primary actions
- **DrawerNavigation**: Secondary navigation for settings

### Security Implementation

#### Authentication Security
- **Password Hashing**: PBKDF2 with SHA-256, 10,000 iterations
- **Salt Generation**: Cryptographically secure random salts
- **Session Tokens**: JWT-like tokens with expiration
- **Biometric Integration**: Hardware-backed biometric authentication

#### Data Protection
- **Encryption**: AES-256 for sensitive data
- **Keystore Integration**: Hardware security module usage
- **Secure Storage**: EncryptedSharedPreferences for local data
- **Network Security**: Certificate pinning for API calls

### Performance Optimizations

#### Memory Management
- **Lazy Initialization**: ViewModels and heavy objects
- **Efficient Queries**: Optimized database operations with indexing
- **Image Optimization**: Coil library with memory/disk caching
- **State Management**: Minimal state retention in Compose

#### Database Performance
- **Strategic Indexing**: Frequently queried columns indexed
- **Query Optimization**: Efficient joins and aggregations
- **Pagination**: Large datasets loaded incrementally
- **Background Processing**: Heavy operations on IO dispatcher

#### UI Performance
- **Compose Best Practices**: Stable parameters, proper remember usage
- **Lazy Loading**: Efficient list rendering with LazyColumn
- **Recomposition Control**: Optimized state hoisting patterns
- **Animation Optimization**: Hardware-accelerated animations

### Testing Strategy

#### Test Coverage Areas
1. **Unit Tests**: ViewModels, repositories, utilities (70%+ coverage)
2. **Integration Tests**: Database operations, authentication flows
3. **UI Tests**: Critical user journeys and interactions
4. **Security Tests**: Encryption, authentication, data protection
5. **Performance Tests**: Memory usage, database query performance

#### Testing Tools & Frameworks
- **JUnit 4**: Core testing framework
- **Mockito**: Mocking dependencies
- **Robolectric**: Android unit testing without emulator
- **Truth**: Fluent assertion library
- **Coroutines Test**: Async operation testing

### Build Configuration

#### Gradle Setup
- **Kotlin DSL**: Modern build configuration
- **Version Catalogs**: Centralized dependency management
- **Build Types**: Debug, release, staging configurations
- **Flavors**: Different app variants if needed
- **Optimization**: R8 code shrinking and obfuscation

#### Quality Assurance
- **Static Analysis**: Detekt for code quality
- **Linting**: Android lint checks
- **Code Formatting**: Ktlint integration
- **Security Scanning**: Dependency vulnerability checks

### Deployment & Distribution

#### Release Process
1. **Code Review**: Pull request reviews and approvals
2. **Testing**: Automated test suite execution
3. **Quality Gates**: Code coverage and quality metrics
4. **Build Generation**: Signed APK/AAB creation
5. **Distribution**: Play Store or enterprise distribution

#### Monitoring & Analytics
- **Crash Reporting**: Firebase Crashlytics integration
- **Performance Monitoring**: App performance tracking
- **User Analytics**: Usage patterns and feature adoption
- **Error Tracking**: Comprehensive error logging

---

## Conclusion

The Fitness Tracker App represents a comprehensive, production-ready Android application built with modern development practices and technologies. The architecture ensures scalability, maintainability, and performance while providing users with a rich, intuitive fitness tracking experience.

### Key Achievements
- **Complete Feature Set**: All core fitness tracking functionality implemented
- **Modern Architecture**: MVVM with Repository pattern and Hilt DI
- **Security First**: Comprehensive security implementation
- **Performance Optimized**: Efficient database and UI operations
- **Accessible Design**: Full accessibility support and responsive layouts
- **Comprehensive Testing**: Unit, integration, and UI test coverage

### Future Enhancements
- **Cloud Sync**: Server-side data synchronization
- **Social Features**: Friend connections and challenges
- **Wearable Integration**: Smartwatch and fitness tracker support
- **AI Insights**: Machine learning-powered fitness recommendations
- **Advanced Analytics**: Detailed performance insights and trends

*This documentation serves as a complete technical reference for the Fitness Tracker App. For implementation details, refer to the well-documented source code and inline KDoc comments throughout the codebase.*
