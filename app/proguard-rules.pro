########################################
# General Debug & Optimization Settings
########################################

# Keep line numbers for stack traces
-keepattributes SourceFile,LineNumberTable

# Optionally hide the original source file names
#-renamesourcefileattribute SourceFile


########################################
# Jetpack Compose
########################################

# Required to keep Compose runtime code
-keep class androidx.compose.** { *; }
-dontwarn androidx.compose.**

# Needed for preview support (optional)
-keep class androidx.compose.ui.tooling.** { *; }
-dontwarn androidx.compose.ui.tooling.**

########################################
# Kotlin & Coroutines
########################################

# Keep Kotlin metadata for reflection and annotations
-keep class kotlin.Metadata { *; }

# Keep all suspend functions and coroutine state machines
-keep class kotlinx.coroutines.** { *; }
-dontwarn kotlinx.coroutines.**

########################################
# AndroidX Lifecycle & ViewModel
########################################

-keep class androidx.lifecycle.** { *; }
-dontwarn androidx.lifecycle.**

# Keep ViewModel subclasses
-keep class * extends androidx.lifecycle.ViewModel

########################################
# Room Database
########################################

# Keep Room entities, DAOs, and database
-keep class androidx.room.** { *; }
-dontwarn androidx.room.**

-keep class * extends androidx.room.RoomDatabase
-keep class * extends androidx.room.Entity
-keep interface * implements androidx.room.Dao

########################################
# Material3 & Jetpack UI
########################################

-keep class androidx.compose.material3.** { *; }
-dontwarn androidx.compose.material3.**

########################################
# Optional: Keep for Retrofit, Gson, Moshi, etc.
########################################

# Uncomment below if you use Retrofit or Gson
# -keepattributes Signature
# -keepattributes *Annotation*

# Retrofit interfaces
# -keep interface retrofit2.* { *; }
# -keep class retrofit2.converter.gson.** { *; }
# -dontwarn retrofit2.**

# Gson models
# -keep class com.yourpackage.model.** { *; }

########################################
# WebView JS Interface (if applicable)
########################################

#-keepclassmembers class fqcn.of.javascript.interface.for.webview {
#   public *;
#}
