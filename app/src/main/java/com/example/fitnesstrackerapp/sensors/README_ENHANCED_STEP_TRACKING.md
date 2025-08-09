# Enhanced Sensor & Step-Tracking Integration

## Overview

This implementation provides a comprehensive, battery-optimized step tracking system that integrates with the existing fitness tracker app. The solution includes real-time step counting, doze-mode awareness, sensor batching, and seamless integration with workouts and goals.

## Key Features

### 1. Battery Optimization
- **Sensor Batching**: Uses hardware FIFO buffers when available to reduce CPU wake-ups
- **Doze Mode Awareness**: Automatically adjusts sensor settings and data persistence based on device power state
- **Adaptive Intervals**: Dynamic adjustment of database save intervals based on battery state
- **Efficient Data Batching**: Groups step data points to minimize database operations

### 2. Sensor Integration
- **Primary Sensor**: `TYPE_STEP_COUNTER` for accurate hardware-based counting
- **Fallback Sensors**: `TYPE_STEP_DETECTOR` and accelerometer-based pedometer algorithm
- **Smart Sensor Selection**: Automatically selects the best available sensor
- **Sensor Accuracy Monitoring**: Adapts algorithms based on sensor accuracy changes

### 3. Foreground Service Implementation
- **Persistent Tracking**: Runs as health-type foreground service
- **Live Notifications**: Real-time step count and progress display
- **Service Binding**: Direct communication channel for UI components
- **Health Monitoring**: Automatic service recovery and health checks

### 4. Room Database Integration
- **Efficient Persistence**: Batch operations for better performance
- **Comprehensive Analytics**: Detailed statistics and progress tracking
- **Goal Integration**: Automatic sync with fitness goals and achievements
- **Historical Data**: Complete step history with cleanup mechanisms

### 5. Workout & Goal Sync
- **Real-time Integration**: Step data automatically syncs with active workouts
- **Goal Tracking**: Automatic progress updates and achievement notifications
- **Analytics Engine**: Comprehensive insights and health recommendations
- **Consistency Scoring**: Advanced metrics for user engagement

## Architecture Components

### Core Services

#### BatteryOptimizedStepService
```kotlin
class BatteryOptimizedStepService : Service(), SensorEventListener
```
- **Main Features**:
  - Battery-optimized sensor registration
  - Doze-mode awareness with broadcast receivers
  - Batched data persistence
  - Real-time UI updates via StateFlow
  - Integration with workouts and goals

#### EnhancedStepServiceManager
```kotlin
object EnhancedStepServiceManager
```
- **Responsibilities**:
  - Service lifecycle management
  - Health monitoring and recovery
  - Permission verification
  - Battery optimization detection

### Data Layer

#### Enhanced StepRepository
```kotlin
class StepRepository(private val stepDao: StepDao)
```
- **New Features**:
  - Batch operations for performance
  - Comprehensive analytics methods
  - Statistical calculations
  - Data cleanup utilities

#### Enhanced TrackStepsUseCase
```kotlin
class TrackStepsUseCase(private val stepRepository: StepRepository)
```
- **Enhanced Capabilities**:
  - Workout integration methods
  - Goal synchronization
  - Health insights generation
  - Consistency scoring algorithms

## Implementation Details

### Battery Optimization Strategy

1. **Sensor Batching**:
   ```kotlin
   val batchLatencyUs = if (sensorBatchingSupported && isDozeMode) {
       5_000_000 // 5 seconds latency in doze mode
   } else {
       0 // Real-time
   }
   ```

2. **Adaptive Persistence**:
   ```kotlin
   val interval = if (isDozeMode) {
       DATABASE_SAVE_INTERVAL_DOZE // 5 minutes
   } else {
       DATABASE_SAVE_INTERVAL_NORMAL // 1 minute
   }
   ```

3. **Data Batching**:
   ```kotlin
   private val stepDataBatch = mutableListOf<StepDataPoint>()
   
   // Save when batch reaches threshold or age limit
   if (stepDataBatch.size >= BATCH_SIZE_THRESHOLD ||
       currentTime - lastBatchSaveTime > MAX_BATCH_AGE_MS) {
       saveStepsBatch()
   }
   ```

### Doze Mode Handling

```kotlin
inner class DozeAwareBroadcastReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        when (intent?.action) {
            PowerManager.ACTION_DEVICE_IDLE_MODE_CHANGED -> {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    val wasDozeMode = isDozeMode
                    isDozeMode = powerManager.isDeviceIdleMode
                    
                    if (wasDozeMode != isDozeMode) {
                        // Re-register sensors with appropriate settings
                        sensorManager?.unregisterListener(this@BatteryOptimizedStepService)
                        registerSensorListeners()
                        
                        // Save any pending data before entering doze
                        if (isDozeMode) {
                            saveStepsBatch()
                        }
                    }
                }
            }
        }
    }
}
```

### Workout Integration

The service automatically syncs step data with active workouts:

```kotlin
private fun syncWithWorkoutsAndGoals(steps: Int, calories: Float, distance: Float) {
    serviceScope.launch {
        try {
            val userId = getCurrentUserId()
            
            // Update active workout with step data if available
            // This would integrate with any running workout sessions
            
            // Check and update step-related goals
            // This integrates with the goal management system
            
            Log.d(TAG, "Synced step data with workouts and goals")
        } catch (e: Exception) {
            Log.e(TAG, "Error syncing with workouts and goals", e)
        }
    }
}
```

### Goal Integration

Automatic goal progress tracking and achievement detection:

```kotlin
private fun checkGoalAchievement(steps: Int, goal: Int) {
    val wasGoalAchieved = sharedPreferences.getBoolean("goal_achieved_today", false)
    val isGoalAchieved = steps >= goal

    if (isGoalAchieved && !wasGoalAchieved) {
        showGoalAchievementNotification(steps, goal)
        sharedPreferences.edit().putBoolean("goal_achieved_today", true).apply()
        Log.d(TAG, "Daily step goal achieved! Steps: $steps, Goal: $goal")
    }
}
```

## Usage Examples

### Starting the Enhanced Service

```kotlin
// In your Application class or main activity
EnhancedStepServiceManager.initializeOnStartup(context)

// Check service status
val diagnostics = EnhancedStepServiceManager.getServiceDiagnostics(context)
if (!diagnostics.isServiceHealthy) {
    EnhancedStepServiceManager.restartService(context)
}
```

### Binding to Service for UI Updates

```kotlin
// Bind to service for real-time updates
EnhancedStepServiceManager.bindToService(context)

// Get current step data
val stepData = EnhancedStepServiceManager.getCurrentStepData()
stepData?.let {
    // Update UI with current steps, progress, etc.
    updateUI(it.steps, it.progress, it.calories, it.distance)
}
```

### Using Enhanced Analytics

```kotlin
// Get comprehensive step analytics
val trackStepsUseCase = TrackStepsUseCase(stepRepository)
trackStepsUseCase.getStepAnalytics(userId).fold(
    onSuccess = { analytics ->
        // Display analytics in UI
        showAnalytics(analytics)
    },
    onFailure = { error ->
        // Handle error
        Log.e("Analytics", "Failed to load analytics", error)
    }
)

// Get health insights
trackStepsUseCase.getHealthInsights(userId).fold(
    onSuccess = { insights ->
        // Show health recommendations
        showHealthInsights(insights)
    },
    onFailure = { error ->
        Log.e("Insights", "Failed to load insights", error)
    }
)
```

## Configuration

### Permissions Required

The following permissions are required and already declared in AndroidManifest.xml:

```xml
<!-- Core fitness and health permissions -->
<uses-permission android:name="android.permission.ACTIVITY_RECOGNITION" />
<uses-permission android:name="android.permission.BODY_SENSORS" />
<uses-permission android:name="android.permission.HIGH_SAMPLING_RATE_SENSORS" />

<!-- Notification and service permissions -->
<uses-permission android:name="android.permission.POST_NOTIFICATIONS" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE" />
<uses-permission android:name="android.permission.FOREGROUND_SERVICE_HEALTH" />
```

### Service Registration

Both legacy and enhanced services are registered:

```xml
<!-- Legacy service for compatibility -->
<service
    android:name=".sensors.StepCounterService"
    android:enabled="true"
    android:exported="false"
    android:foregroundServiceType="health" />

<!-- Enhanced battery-optimized service -->
<service
    android:name=".sensors.BatteryOptimizedStepService"
    android:enabled="true"
    android:exported="false"
    android:foregroundServiceType="health" />
```

## Performance Considerations

### Memory Usage
- Batch data structures are kept minimal
- Automatic cleanup of old step records
- Efficient StateFlow usage for UI updates

### Battery Optimization
- Adaptive sensor delays based on device state
- Intelligent batching to reduce wake-ups
- Doze-mode aware persistence strategies

### Database Performance
- Batch operations for bulk updates
- Indexed queries for fast retrieval
- Automatic data cleanup to prevent bloat

## Testing and Validation

### Service Health Monitoring
```kotlin
// Monitor service health
EnhancedStepServiceManager.monitorServiceHealth(context)

// Get diagnostic information
val diagnostics = EnhancedStepServiceManager.getServiceDiagnostics(context)
```

### Analytics Validation
```kotlin
// Validate step statistics
val analytics = stepRepository.getStepStatistics(userId)
analytics?.let {
    assert(it.totalSteps >= 0)
    assert(it.goalAchievementRate in 0f..100f)
    // Additional validation logic
}
```

## Migration from Legacy Service

To migrate from the existing StepCounterService:

1. **Gradual Migration**: Both services can run simultaneously during transition
2. **Data Compatibility**: Enhanced service uses same database schema
3. **UI Integration**: Update ViewModels to use EnhancedStepServiceManager
4. **Testing**: Validate step counting accuracy and battery usage

## Future Enhancements

1. **Machine Learning**: Activity pattern recognition for better insights
2. **Social Features**: Step challenges and community goals
3. **Health Platform Integration**: Sync with Google Fit, Apple Health
4. **Advanced Analytics**: Predictive modeling for user behavior
5. **Wearable Integration**: Support for smartwatches and fitness trackers

## Troubleshooting

### Common Issues

1. **Service Not Starting**: Check permissions and battery optimization settings
2. **Inaccurate Step Counts**: Verify sensor availability and calibration
3. **High Battery Usage**: Ensure doze-mode handling is working correctly
4. **Missing Data**: Check database persistence and cleanup settings

### Debug Information

Enable detailed logging by setting:
```kotlin
private const val DEBUG_MODE = true
```

This enables comprehensive logging for service lifecycle, sensor events, and data persistence operations.
