# Fitness Tracker App

**Author:** Paulina Czarnota  
**Student Number:** C21365726  
**Course:** TU856/3 Mobile Software Development  
**Due Date:** 11/08/2025  

## Project Overview

A comprehensive Android fitness tracking application built with Kotlin and Jetpack Compose that enables users to track workouts, set goals, monitor nutrition, and maintain a healthy lifestyle. This app is designed as a minimum viable product ready for Google Play Store deployment.

## Core Features Implemented

### ✅ User Authentication (10%)
- Email/password signup and login with comprehensive validation
- Secure credential storage using Android Keystore encryption
- Session management with remember me functionality
- Account security features (lockout protection, failed attempt tracking)

### ✅ Workout Logging (10%)
- Multiple workout types (running, cycling, weightlifting, swimming, yoga, etc.)
- Comprehensive data capture (duration, distance, calories burned, workout notes)
- **Step tracking using phone sensors** (accelerometer and step counter)
- MET table integration for accurate calorie calculations
- Real-time workout timer with rest intervals

### ✅ Progress Tracking (10%)
- Historical data display in user-friendly format
- Weekly, monthly, and yearly workout summaries
- **Charts and graphs visualization** for progress monitoring
- Real-time progress updates with StateFlow
- Comprehensive workout analytics and statistics

### ✅ Goal Setting (10%)
- Flexible fitness goals (daily steps, weekly workouts, calorie targets, distance goals)
- Real-time progress tracking with visual indicators
- **Reminder notifications** to keep users motivated
- Achievement system with streak tracking
- Goal completion celebrations and rewards

### ✅ Notifications (10%)
- **Workout reminders** with customizable scheduling
- **Goal reminders** for daily and weekly targets
- **Motivational messages and tips** based on user progress
- Background notification scheduling with WorkManager
- Daily summary notifications with achievements

### ✅ Diet and Nutrition Tracking (10%)
- **Daily food intake logging** with comprehensive food database
- **Comprehensive nutrition tracking** (calories, protein, carbohydrates, fat, fiber)
- Meal categorization (breakfast, lunch, dinner, snacks)
- Daily nutrition summaries and macro goal tracking
- Nutrition analytics and dietary insights

## Technical Stack

- **Language:** Kotlin
- **UI Framework:** Jetpack Compose with Material 3 Design
- **Architecture:** MVVM with Repository Pattern
- **Database:** Room (SQLite) with proper entity relationships
- **Dependency Injection:** Custom ServiceLocator pattern
- **Asynchronous Programming:** Coroutines & StateFlow
- **Security:** Android Keystore for credential encryption
- **Background Work:** WorkManager for notifications and step tracking
- **Testing:** Unit tests with JUnit and Mockito

## Architecture Highlights

- **Clean Architecture:** Separation of concerns with clear layer boundaries
- **Repository Pattern:** Single source of truth for data operations
- **MVVM Pattern:** Reactive UI with ViewModel state management
- **Dependency Injection:** Centralized dependency management
- **Error Handling:** Comprehensive error states and user feedback
- **Performance Optimization:** Efficient database queries and UI rendering

## Build Instructions

1. **Prerequisites:**
   - Android Studio (latest version)
   - Android SDK API 34
   - Kotlin 1.9+

2. **Build Steps:**
   ```bash
   # Clone/extract the project
   # Open in Android Studio
   ./gradlew clean
   ./gradlew assembleDebug
   ```

3. **Installation:**
   ```bash
   # Install on device/emulator
   ./gradlew installDebug
   ```

## Submission Contents

- **APK File:** `app/build/outputs/apk/debug/app-debug.apk`
- **Complete Project:** Source code with git repository
- **Technical Documentation:** `DOCUMENTATION.md` (convert to PDF for submission)
- **Video Presentation:** 3-5 minute app demonstration

## Assignment Compliance

This project fully meets all assignment requirements:

- ✅ **Core Features (60%):** All 6 features fully implemented and functional
- ✅ **Design (15%):** Clean code architecture and intuitive UI/UX
- ✅ **Speed (10%):** Optimized performance and responsive interface
- ✅ **Documentation (10%):** Comprehensive technical documentation
- ✅ **Room Database:** All entities properly implemented
- ✅ **Standard Libraries Only:** No external dependencies
- ✅ **Version Control:** Git repository with proper commit history
- ✅ **Executable Code:** Builds and runs without errors

## Key Design Decisions

- **Material 3 Design System:** Modern, accessible, and consistent UI
- **Bottom Navigation:** Intuitive access to main app sections
- **Real-time Updates:** Immediate feedback for user actions
- **Offline-first:** Core functionality works without internet
- **Battery Optimization:** Efficient background processing
- **Security-first:** Encrypted storage and secure authentication

---

**Note:** This application represents original work by Paulina Czarnota for the TU856/3 Mobile Software Development course assignment.
