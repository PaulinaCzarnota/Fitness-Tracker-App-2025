########################################
# General Debug & Optimization Settings
########################################

# Keep source file and line numbers for easier debugging (stack traces)
-keepattributes SourceFile,LineNumberTable

# Optional: Remove original source file name from stack trace (privacy)
#-renamesourcefileattribute SourceFile


########################################
# Jetpack Compose Runtime & UI Tooling
########################################

# Keep all Compose runtime classes and suppress related warnings
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# Keep Compose UI tooling classes for Previews and UI inspector
-keep class androidx.compose.ui.tooling.** { *; }
-dontwarn androidx.compose.ui.tooling.**


########################################
# Kotlin & Coroutines
########################################

# Preserve Kotlin metadata for reflection, annotations, etc.
-keep class kotlin.Metadata { *; }

# Preserve all coroutine internal state and suppress warnings
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**


########################################
# AndroidX Lifecycle, ViewModel, LiveData
########################################

# Keep lifecycle classes (ViewModel, LiveData, etc.)
-keep class androidx.lifecycle.** { *; }
-dontwarn androidx.lifecycle.**

# Preserve any custom ViewModel subclasses (used in Compose navigation or state)
-keep class * extends androidx.lifecycle.ViewModel


########################################
# Room Database Annotations & Classes
########################################

# Preserve Room’s core annotations and generated classes
-keep class androidx.room.** { *; }
-dontwarn androidx.room.**

# Keep Room database, entity, and DAO structures
-keep class * extends androidx.room.RoomDatabase
-keep class * extends androidx.room.Entity
-keep interface * implements androidx.room.Dao


########################################
# Material3 UI Components
########################################

# Keep Material3 component classes to avoid UI loss in release builds
-keep class androidx.compose.material3.** { *; }
-dontwarn androidx.compose.material3.**


########################################
# Optional: Retrofit, Gson, Moshi (Uncomment if used)
########################################

# Uncomment these rules if using Retrofit + Gson for networking
#-keepattributes Signature
#-keepattributes *Annotation*

# Retrofit interfaces (API declarations)
#-keep interface retrofit2.* { *; }
#-keep class retrofit2.converter.gson.** { *; }
#-dontwarn retrofit2.**

# Gson POJOs
#-keep class com.yourpackage.model.** { *; }


########################################
# Optional: WebView JavaScript Interface
########################################

# Uncomment this if using JavaScript interfaces in WebViews
#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#    public *;
#}


########################################
# Security & Biometric Authentication
########################################

# Keep security crypto classes to prevent R8 issues
-keep class androidx.security.crypto.** { *; }
-dontwarn androidx.security.crypto.**
-keep class com.google.crypto.tink.** { *; }
-dontwarn com.google.crypto.tink.**

# Keep javax annotations to prevent missing class errors
-keep class javax.annotation.** { *; }
-dontwarn javax.annotation.**
-keep class com.google.errorprone.annotations.** { *; }
-dontwarn com.google.errorprone.annotations.**

# Keep biometric classes
-keep class androidx.biometric.** { *; }
-dontwarn androidx.biometric.**

########################################
# WorkManager & Data Storage
########################################

# Keep WorkManager classes
-keep class androidx.work.** { *; }
-dontwarn androidx.work.**

# Keep DataStore classes
-keep class androidx.datastore.** { *; }
-dontwarn androidx.datastore.**

# Keep multidex classes
-keep class androidx.multidex.** { *; }
-dontwarn androidx.multidex.**

########################################
# App-specific Entity Classes
########################################

# Keep all data classes and entities
-keep class com.example.fitnesstrackerapp.data.** { *; }
-keep class com.example.fitnesstrackerapp.ui.**.ViewModel { *; }
-keep class com.example.fitnesstrackerapp.ui.**.ViewModel$* { *; }

# Keep all enum classes
-keepclassmembers enum * {
    public static **[] values();
    public static ** valueOf(java.lang.String);
}

########################################
# Testing Tools (if needed)
########################################

# Required if you use Compose UI testing or Espresso
#-keep class androidx.test.** { *; }
#-dontwarn androidx.test.**
