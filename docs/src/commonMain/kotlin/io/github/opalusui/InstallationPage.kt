package io.github.opalusui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun InstallationPage() {
    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isMobile = maxWidth < 768.dp
        val pagePadding = if (isMobile) 16.dp else 32.dp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(pagePadding),
            verticalArrangement = Arrangement.spacedBy(if (isMobile) 20.dp else 28.dp)
        ) {
            M3SectionHeader(
                title = "Installation & Gradle Setup",
                subtitle = "Declare Opalus UI modules in your Compose Multiplatform project to start building with fluid glass components and suspend-driven dialogs.",
                badge = "v0.2.8"
            )

            // Version Catalog
            M3CodeBlock(
                title = "gradle/libs.versions.toml",
                code = """[versions]
opalusUi = "0.2.8"
kyant-backdrop = "2.0.1"
kyant-shapes = "1.2.1"

[libraries]
# Core Dialogs & Surface System
opalus-ui-core = { module = "io.github.opalusui:opalus-ui-core", version.ref = "opalusUi" }

# iOS 27 HIG Fluid Glass Droplet Tab Bar
opalus-ui-liquid = { module = "io.github.opalusui:opalus-ui-liquid", version.ref = "opalusUi" }"""
            )

            // build.gradle.kts
            M3CodeBlock(
                title = "build.gradle.kts (Module)",
                code = """kotlin {
    sourceSets {
        commonMain.dependencies {
            // Import core module for async dialogs
            implementation(libs.opalus.ui.core)

            // Import liquid module for fluid glass navigation
            implementation(libs.opalus.ui.liquid)
        }
    }
}"""
            )

            // Platform Matrix
            M3ParameterTable(
                parameters = listOf(
                    ParamItem("Android", "API 24+", "Supported", "Full support on Android phones and tablets using Jetpack Compose."),
                    ParamItem("Desktop JVM", "Java 17+", "Supported", "Hardware-accelerated Skia rendering on macOS, Windows, and Linux."),
                    ParamItem("iOS", "iOS 15+", "Supported", "Native Metal-backed rendering via Compose Multiplatform (iosArm64 / Simulator)."),
                    ParamItem("Web Wasm", "WasmGC", "Supported", "High-performance Canvas rendering via WebAssembly and Skiko runtime."),
                    ParamItem("Web JS", "ES2020", "Supported", "JavaScript Canvas target with compatibility fallback for legacy browsers.")
                )
            )
        }
    }
}
