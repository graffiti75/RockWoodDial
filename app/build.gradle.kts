plugins {
	alias(libs.plugins.android.application)
	alias(libs.plugins.kotlin.android)
	alias(libs.plugins.kotlin.compose)
	alias(libs.plugins.kotlin.serialization)
	alias(libs.plugins.hilt.android)
	alias(libs.plugins.ksp.android)
}

android {
	namespace = "com.cericatto.rockwooddial"
	compileSdk = 35

	defaultConfig {
		applicationId = "com.cericatto.rockwooddial"
		minSdk = 24
		targetSdk = 35
		versionCode = 1
		versionName = "1.0"

		testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
	}

	buildTypes {
		release {
			isMinifyEnabled = false
			proguardFiles(
				getDefaultProguardFile("proguard-android-optimize.txt"),
				"proguard-rules.pro"
			)
		}
	}
	compileOptions {
		sourceCompatibility = JavaVersion.VERSION_17
		targetCompatibility = JavaVersion.VERSION_17
	}
	buildFeatures {
		compose = true
	}
	packaging {
		resources {
			excludes += "META-INF/LICENSE.md"
			excludes += "META-INF/LICENSE-notice.md"
			excludes += "META-INF/NOTICE.md"
		}
	}
}

kotlin {
	compilerOptions {
		jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
	}
}

dependencies {
	// Compose
	implementation(libs.androidx.core.ktx)
	implementation(libs.androidx.lifecycle.runtime.ktx)
	implementation(libs.androidx.activity.compose)
	implementation(platform(libs.androidx.compose.bom))
	implementation(libs.androidx.ui)
	implementation(libs.androidx.ui.graphics)
	implementation(libs.androidx.ui.tooling.preview)
	implementation(libs.androidx.material3)
	implementation(libs.androidx.material.icons.extended)

	// YouTube Player
	implementation(libs.youtube.player)

	// Hilt
	implementation(libs.hilt.android)
	ksp(libs.hilt.android.compiler)
	implementation(libs.hilt.navigation.compose)

	// Navigation + Serialization
	implementation(libs.navigation.compose)
	implementation(libs.kotlinx.serialization.json)

	// Splash Screen
	implementation(libs.splash.screen)

	// Tests
	testImplementation(libs.junit)
	testImplementation(libs.mockk)
	testImplementation(libs.kotlinx.coroutines.test)
	testImplementation(libs.mockito.core)
	testImplementation(libs.mockito.kotlin)
	testImplementation(libs.hilt.android.testing)
	kspTest(libs.hilt.android.compiler)

	androidTestImplementation(libs.androidx.test.ext.junit)
	androidTestImplementation(libs.androidx.espresso.core)
	androidTestImplementation(platform(libs.androidx.compose.bom))
	androidTestImplementation(libs.androidx.ui.test.junit4)
	androidTestImplementation(libs.hilt.android.testing)
	androidTestImplementation(libs.kotlinx.coroutines.test)
	androidTestImplementation(libs.mockk)
	kspAndroidTest(libs.hilt.android.compiler)

	debugImplementation(libs.androidx.ui.tooling)
	debugImplementation(libs.androidx.ui.test.manifest)
}