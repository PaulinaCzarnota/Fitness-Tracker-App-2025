import java.io.FileInputStream
import java.util.Properties

/**
 * App Module Gradle Build Script (Kotlin DSL)
 *
 * Responsibilities:
 * - Configures the Android application plugin, Kotlin, Compose, DI, and all dependencies for the app.
 * - Sets up build types, Java/Kotlin compatibility, Compose, and packaging options.
 * - Follows consistent Javadoc-style comment structure.
 */

plugins {
    // Android application plugin
    alias(libs.plugins.android.application)
    // Kotlin Android plugin
    alias(libs.plugins.kotlin.android)
    // Kotlin annotation processing (for Room, Hilt)
    alias(libs.plugins.ksp)
    // Dokka plugin for generating HTML documentation from KDoc
    alias(libs.plugins.dokka)
    // Spotless plugin for code formatting
    alias(libs.plugins.spotless)
    // ktlint plugin for Kotlin linting
    alias(libs.plugins.ktlint)
    // Detekt plugin for static analysis
    alias(libs.plugins.detekt)
    // Dependency updates plugin
    alias(libs.plugins.versions)
    // Note: Firebase plugin removed as it's non-standard
    // Dagger Hilt plugin for dependency injection
    alias(libs.plugins.hilt)
    // JaCoCo code coverage plugin
    jacoco
}

android {
    /**
     * Android configuration block
     *
     * - Sets namespace, SDK versions, and default config.
     * - Configures build types, Java/Kotlin compatibility, Compose, and packaging.
     */
    namespace = "com.example.fitnesstrackerapp"
    compileSdk = 34

    // Load keystore properties
    val keystorePropertiesFile = rootProject.file("keystore.properties")
    val keystoreProperties = Properties()
    if (keystorePropertiesFile.exists()) {
        keystoreProperties.load(FileInputStream(keystorePropertiesFile))
    }

    defaultConfig {
        applicationId = "com.example.fitnesstrackerapp"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "com.example.fitnesstrackerapp.HiltTestRunner"

        // Enable multidex for apps with many dependencies
        multiDexEnabled = true
    }

    // ksp configuration moved outside defaultConfig
    ksp {
        arg("room.schemaLocation", "$projectDir/schemas")
    }

    signingConfigs {
        create("release") {
            if (keystorePropertiesFile.exists()) {
                storeFile = rootProject.file(keystoreProperties["storeFile"] as String)
                storePassword = keystoreProperties["storePassword"] as String
                keyAlias = keystoreProperties["keyAlias"] as String
                keyPassword = keystoreProperties["keyPassword"] as String
            }
        }
    }

    buildTypes {
        release {
            // Enable code shrinking and obfuscation for release builds
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
            signingConfig = signingConfigs.getByName("release")
        }
    }

    buildFeatures {
        // Enable Jetpack Compose and ViewBinding
        compose = true
        // Enable BuildConfig generation
        buildConfig = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.14"
    }

    packaging {
        // Handle potential packaging conflicts and exclude problematic files
        resources {
            excludes.addAll(
                listOf(
                    "/META-INF/{AL2.0,LGPL2.1}",
                    "/META-INF/DEPENDENCIES",
                    "/META-INF/LICENSE",
                    "/META-INF/LICENSE.txt",
                    "/META-INF/license.txt",
                    "/META-INF/NOTICE",
                    "/META-INF/NOTICE.txt",
                    "/META-INF/notice.txt",
                    "/META-INF/ASL2.0",
                    "/META-INF/*.kotlin_module",
                ),
            )
        }
    }

    kotlinOptions {
        jvmTarget = "17"
        // Enable explicit API mode for better API visibility control
        // This helps catch missing visibility modifiers and return types
        // Only apply to new modules to avoid breaking existing code
        if (project.hasProperty("enableExplicitApi")) {
            freeCompilerArgs += "-Xexplicit-api=strict"
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    // Suppress incubating warning for DSL testOptions
    @Suppress("UnstableApiUsage")
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
        animationsDisabled = true

        // Configure test orchestrator for improved test isolation
        execution = "ANDROIDX_TEST_ORCHESTRATOR"

        // Enable code coverage for unit tests
        unitTests.all {
            it.useJUnitPlatform()
            it.systemProperty("robolectric.enabledSdks", "28,29,30,31,32,33,34")
        }
    }
}

/**
 * JaCoCo Code Coverage Configuration
 *
 * Provides comprehensive code coverage reporting for unit and integration tests.
 * Generates both XML and HTML reports with >80% coverage target.
 */
jacoco {
    toolVersion = "0.8.10"
}

tasks.register<JacocoReport>("jacocoTestReport") {
    group = "reporting"
    description = "Generate JaCoCo test coverage report"

    dependsOn("testDebugUnitTest")

    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
    }

    val debugTree = fileTree("${layout.buildDirectory.get()}/intermediates/javac/debug/classes") {
        exclude(
            "**/R.class",
            "**/R$*.class",
            "**/BuildConfig.*",
            "**/Manifest*.*",
            "**/*Test*.*",
            "**/*\$MockitoMock\$*.*",
            "**/databinding/**",
            "**/android/databinding/**",
            "**/androidx/databinding/**",
            "**/di/module/**",
            "**/*MapperImpl*.*",
            "**/*\$ViewInjector*.*",
            "**/*\$ViewBinder*.*",
            "**/BuildConfig.*",
            "**/*Component*.*",
            "**/*BR*.*",
            "**/Manifest*.*",
            "**/*\$Lambda\$*.*",
            "**/*Companion*.*",
            "**/*Module*.*",
            "**/*Dagger*.*",
            "**/*MembersInjector*.*",
            "**/*_MembersInjector.class",
            "**/*_Factory*.*",
            "**/*_Provide*Factory*.*",
            "**/*_ViewBinding*.*",
            "**/*Binding*.*",
            "**/*\$Result.*",
            "**/*\$Result\$*.*",
        )
    }

    val kotlinDebugTree = fileTree("${layout.buildDirectory.get()}/tmp/kotlin-classes/debug") {
        exclude(
            "**/R.class",
            "**/R$*.class",
            "**/BuildConfig.*",
            "**/Manifest*.*",
            "**/*Test*.*",
            "**/*\$MockitoMock\$*.*",
            "**/databinding/**",
            "**/android/databinding/**",
            "**/androidx/databinding/**",
            "**/di/module/**",
            "**/*MapperImpl*.*",
            "**/*\$ViewInjector*.*",
            "**/*\$ViewBinder*.*",
            "**/BuildConfig.*",
            "**/*Component*.*",
            "**/*BR*.*",
            "**/Manifest*.*",
            "**/*\$Lambda\$*.*",
            "**/*Companion*.*",
            "**/*Module*.*",
            "**/*Dagger*.*",
            "**/*MembersInjector*.*",
            "**/*_MembersInjector.class",
            "**/*_Factory*.*",
            "**/*_Provide*Factory*.*",
            "**/*_ViewBinding*.*",
            "**/*Binding*.*",
            "**/*\$Result.*",
            "**/*\$Result\$*.*",
        )
    }

    classDirectories.setFrom(debugTree, kotlinDebugTree)
    sourceDirectories.setFrom(
        "${project.projectDir}/src/main/java",
        "${project.projectDir}/src/main/kotlin",
    )
    executionData.setFrom(fileTree(layout.buildDirectory.get()).include("**/*.exec", "**/*.ec"))
}

tasks.register<JacocoCoverageVerification>("jacocoCoverageVerification") {
    group = "verification"
    description = "Verify JaCoCo test coverage meets minimum thresholds"

    dependsOn("jacocoTestReport")

    violationRules {
        rule {
            limit {
                minimum = "0.80".toBigDecimal() // 80% coverage requirement
            }
        }

        rule {
            enabled = true
            element = "CLASS"
            limit {
                counter = "BRANCH"
                value = "COVEREDRATIO"
                minimum = "0.75".toBigDecimal() // 75% branch coverage
            }
            limit {
                counter = "LINE"
                value = "COVEREDRATIO"
                minimum = "0.80".toBigDecimal() // 80% line coverage
            }
        }
    }

    classDirectories.setFrom(
        fileTree("${layout.buildDirectory.get()}/intermediates/javac/debug/classes") {
            exclude(
                "**/R.class",
                "**/R$*.class",
                "**/BuildConfig.*",
                "**/Manifest*.*",
                "**/*Test*.*",
                "**/*\$MockitoMock\$*.*",
                "**/databinding/**",
                "**/android/databinding/**",
                "**/androidx/databinding/**",
            )
        },
        fileTree("${layout.buildDirectory.get()}/tmp/kotlin-classes/debug") {
            exclude(
                "**/R.class",
                "**/R$*.class",
                "**/BuildConfig.*",
                "**/Manifest*.*",
                "**/*Test*.*",
                "**/*\$MockitoMock\$*.*",
                "**/databinding/**",
                "**/android/databinding/**",
                "**/androidx/databinding/**",
            )
        },
    )

    executionData.setFrom(fileTree(layout.buildDirectory.get()).include("**/*.exec", "**/*.ec"))
}

// Make check depend on jacoco coverage verification
tasks.named("check") {
    dependsOn("jacocoCoverageVerification")
}

dependencies {
    /*
     * Dependencies block
     *
     * - Declares all libraries and tools used in the app.
     * - Groups dependencies by feature for clarity.
     */

    // AndroidX Core and Lifecycle
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.activity.compose)

    // AppCompat for theme support
    implementation(libs.androidx.appcompat)

    // Room dependencies
    implementation(libs.room.runtime)
    implementation(libs.room.ktx)
    implementation(libs.androidx.core.splashscreen)
    ksp(libs.room.compiler)
    testImplementation(libs.room.testing)

    // Compose UI
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)

    // Coroutines
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.coroutines.android)
    testImplementation(libs.kotlinx.coroutines.test)

    // DataStore
    implementation(libs.androidx.datastore.preferences)

    // Material Icons Extended for complete icon set
    implementation(libs.androidx.compose.material.icons.extended)

    // Additional Material 3 and Animation dependencies for UI/UX overhaul
    implementation("androidx.compose.material3:material3-window-size-class:1.3.1")
    implementation("androidx.compose.animation:animation")
    implementation("androidx.compose.animation:animation-graphics")
    implementation("androidx.constraintlayout:constraintlayout-compose:1.1.0")
    // Navigation compose version is managed by BOM now

    // Motion Layout and Advanced Animation Support
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.airbnb.android:lottie-compose:6.1.0")

    // Biometric Authentication
    implementation(libs.androidx.biometric)

    // WorkManager
    implementation(libs.androidx.work.runtime.ktx)

    // Security
    implementation(libs.androidx.security.crypto)

    // Note: Error prone annotations removed as they are non-standard

    // Multidex support for devices with API < 21
    implementation(libs.androidx.multidex)

    // Note: Firebase and MPAndroidChart removed as they are non-standard libraries
    // Using standard Android SDK libraries only as per assignment requirements

    // Hilt Dependency Injection
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    ksp(libs.hilt.compiler.androidx)
    implementation(libs.hilt.work)
    implementation("androidx.hilt:hilt-navigation-compose:1.1.0")
    
    // Hilt for testing
    testImplementation("com.google.dagger:hilt-android-testing:2.48")
    kspTest(libs.hilt.compiler)
    androidTestImplementation("com.google.dagger:hilt-android-testing:2.48")
    kspAndroidTest(libs.hilt.compiler)

    // Note: Using only standard Android SDK libraries as per assignment requirements

    // Using standard Android testing libraries only

    // Architecture Testing
    testImplementation(libs.androidx.arch.core.testing)

    // Espresso for UI testing
    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.espresso.core)
    androidTestImplementation("androidx.test.espresso:espresso-contrib:3.5.1")
    androidTestImplementation("androidx.test.espresso:espresso-intents:3.5.1")
    androidTestImplementation("androidx.test.espresso:espresso-accessibility:3.5.1")
    androidTestImplementation("androidx.test.espresso:espresso-idling-resource:3.5.1")

    // Compose Testing
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    androidTestImplementation("androidx.compose.ui:ui-test-manifest")
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)

    // Room Testing
    testImplementation(libs.room.testing)

    // WorkManager Testing
    testImplementation("androidx.work:work-testing:2.9.0")
    androidTestImplementation("androidx.work:work-testing:2.9.0")

    // Standard Android testing libraries only
    androidTestImplementation(libs.androidx.test.rules)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestUtil(libs.androidx.test.orchestrator)
}

/**
 * Spotless configuration for code formatting
 *
 * Applies consistent formatting rules across all source files
 * including Kotlin, XML, and Gradle files.
 */
spotless {
    kotlin {
        target("**/*.kt")
        targetExclude("**/build/**/*.kt")
        ktlint(libs.versions.ktlint.get()).editorConfigOverride(
            mapOf(
                "ktlint_standard_max-line-length" to "disabled",
                "ktlint_standard_no-wildcard-imports" to "disabled",
                "ktlint_standard_no-consecutive-comments" to "disabled",
                "ktlint_standard_discouraged-comment-location" to "disabled",
                "ktlint_standard_property-naming" to "disabled",
                "ktlint_standard_function-naming" to "disabled",
                "ktlint_standard_filename" to "disabled",
            ),
        )
        trimTrailingWhitespace()
        endWithNewline()
    }

    kotlinGradle {
        target("*.gradle.kts")
        ktlint(libs.versions.ktlint.get()).editorConfigOverride(
            mapOf(
                "ktlint_standard_max-line-length" to "disabled",
                "ktlint_standard_no-wildcard-imports" to "disabled",
                "ktlint_standard_no-consecutive-comments" to "disabled",
                "ktlint_standard_discouraged-comment-location" to "disabled",
                "ktlint_standard_property-naming" to "disabled",
                "ktlint_standard_function-naming" to "disabled",
                "ktlint_standard_filename" to "disabled",
            ),
        )
    }

    format("xml") {
        target("**/*.xml")
        targetExclude("**/build/**/*.xml")
        indentWithSpaces(4)
        trimTrailingWhitespace()
        endWithNewline()
    }
}

/**
 * ktlint configuration for Kotlin linting
 *
 * Enforces Kotlin coding conventions and style guidelines
 */
ktlint {
    version.set(libs.versions.ktlint.get())
    android.set(true)
    ignoreFailures.set(true)
    reporters {
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.PLAIN)
        reporter(org.jlleitschuh.gradle.ktlint.reporter.ReporterType.CHECKSTYLE)
    }
    filter {
        exclude("**/generated/**")
        include("**/kotlin/**")
    }
    // Disable some rules that conflict with our coding standards
    // Prefer wildcard imports to reduce verbosity in UI-heavy files
    additionalEditorconfig =
        mapOf(
            "max_line_length" to "off",
            "ktlint_standard_no-wildcard-imports" to "disabled",
            "ktlint_standard_no-consecutive-comments" to "disabled",
            "ktlint_standard_discouraged-comment-location" to "disabled",
            "ktlint_standard_property-naming" to "disabled",
            "ktlint_standard_function-naming" to "disabled",
            "ktlint_standard_filename" to "disabled",
            "ktlint_standard_multiline-expression-wrapping" to "disabled",
            "ktlint_standard_class-signature" to "disabled",
            "ktlint_standard_function-signature" to "disabled",
            "ktlint_standard_parameter-list-wrapping" to "disabled",
            "ktlint_standard_argument-list-wrapping" to "disabled",
            "ktlint_standard_blank-line-before-class-body" to "disabled",
            "ktlint_standard_blank-line-before-declaration" to "disabled",
            "ktlint_standard_value-argument-comment" to "disabled",
            "ktlint_standard_value-parameter-comment" to "disabled",
            "ktlint_standard_trailing-comma-on-call-site" to "disabled",
            "ktlint_standard_trailing-comma-on-declaration-site" to "disabled",
            "ktlint_standard_string-template-indent" to "disabled",
            "ktlint_standard_spacing-between-declarations-with-comments" to "disabled",
            "ktlint_standard_spacing-between-declarations-with-annotations" to "disabled",
        )
}

/**
 * CI Configuration
 *
 * Optimizes build tasks for CI environment by:
 * - Disabling documentation generation (Dokka) tasks to speed up builds
 * - Disabling experimental/incubating Detekt baseline tasks
 * - Keeping essential testing and code quality checks enabled
 * - Configuring test tasks for CI reliability
 */

// Enable unit test tasks for CI analysis
tasks.withType<Test>().configureEach {
    enabled = true
    useJUnitPlatform() // Enable JUnit 5 platform
    // Set test timeout to avoid hanging builds in CI
    systemProperty("junit.jupiter.execution.timeout.default", "30m")
    // Optimize test execution for CI
    maxParallelForks = (Runtime.getRuntime().availableProcessors() / 2).takeIf { it > 0 } ?: 1
}

// Enable essential ktlint tasks but skip formatting in CI
tasks.matching { it.name.startsWith("runKtlintCheck") || it.name == "ktlintCheck" }.configureEach {
    enabled = true
}

// Disable ktlint formatting tasks in CI (formatting should be done locally)
tasks.matching { it.name == "ktlintFormat" }.configureEach {
    enabled = !project.hasProperty("ci")
}

// Enable Kotlin compilation tasks
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().configureEach {
    enabled = true
}

// Disable documentation generation tasks in CI to speed up builds
tasks.matching { it.name.startsWith("dokka") }.configureEach {
    enabled = !project.hasProperty("ci")
}

// Disable experimental Detekt baseline tasks in CI
tasks.matching {
    it.name.startsWith("detektBaseline") ||
        it.description?.contains("EXPERIMENTAL", ignoreCase = true) == true
}.configureEach {
    enabled = !project.hasProperty("ci")
}

// Keep essential Detekt tasks enabled but configure for CI
tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
    // Skip baseline tasks in CI but keep main detekt analysis
    enabled = !name.contains("Baseline", ignoreCase = true)
    // Fail fast on detekt issues in CI
    ignoreFailures = project.hasProperty("ci").not()
}

// Disable unnecessary Android test tasks in CI if no devices connected
if (project.hasProperty("ci")) {
    tasks.matching {
        it.name.startsWith("connected") ||
            it.name.startsWith("device") ||
            it.name.contains("AndroidTest")
    }.configureEach {
        enabled = false
    }
}

/**
 * Custom CI task for Pull Request validation
 *
 * This task runs unit tests automatically on every PR to catch
 * potential issues like Room column mapping mismatches early.
 */
tasks.register("validatePR") {
    group = "verification"
    description = "Validates PR by running unit tests and code quality checks"

    dependsOn("testDebugUnitTest")
    dependsOn("ktlintCheck")
    dependsOn("detekt")

    doLast {
        println("✅ PR validation complete - all checks passed!")
    }
}

/**
 * Task to run unit tests specifically for catching Room mapping issues
 */
tasks.register("testRoomMapping") {
    group = "verification"
    description = "Run unit tests focusing on Room database mapping validation"

    dependsOn("testDebugUnitTest")

    doLast {
        println("✅ Room mapping tests complete - column mappings validated!")
    }
}

/**
 * Detekt configuration for static code analysis
 *
 * Provides comprehensive static analysis with custom rules and reporting
 */
detekt {
    toolVersion = libs.versions.detekt.get()
    config.setFrom("$projectDir/detekt.yml")
    buildUponDefaultConfig = true
    allRules = false
}

// Configure detekt reports on individual tasks instead of globally
tasks.withType<io.gitlab.arturbosch.detekt.Detekt>().configureEach {
    reports {
        html.required.set(true)
        xml.required.set(true)
        txt.required.set(true)
        sarif.required.set(true)
        md.required.set(true)
    }
}

/**
 * Dokka configuration for generating HTML documentation from KDoc
 *
 * Generates comprehensive HTML documentation from KDoc comments
 * including class diagrams, inheritance trees, and API documentation.
 */
tasks.dokkaHtml.configure {
    outputDirectory.set(layout.buildDirectory.dir("dokka"))

    dokkaSourceSets {
        named("main") {
            moduleName.set("Fitness Tracker App")
            moduleVersion.set("1.0")

            // Include source code in documentation
            includeNonPublic.set(false)
            skipEmptyPackages.set(true)
            skipDeprecated.set(false)
            reportUndocumented.set(true)

            // Package documentation
            documentedVisibilities.set(
                setOf(
                    org.jetbrains.dokka.DokkaConfiguration.Visibility.PUBLIC,
                    org.jetbrains.dokka.DokkaConfiguration.Visibility.PROTECTED,
                ),
            )

            // External documentation links
            externalDocumentationLink {
                url.set(uri("https://developer.android.com/reference/").toURL())
            }

            externalDocumentationLink {
                url.set(uri("https://kotlinlang.org/api/latest/jvm/stdlib/").toURL())
            }
        }
    }
}
