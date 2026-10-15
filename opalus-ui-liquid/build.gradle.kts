group = "io.github.opalusui"
version = "0.2.8"
plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    id("signing")
    id("com.vanniktech.maven.publish") version "0.35.0"
}

kotlin {
    // Android
    androidTarget()

    // Desktop (JVM)
    jvm("desktop")

    // iOS
    iosArm64()
    iosSimulatorArm64()

    // Web (JavaScript)
    js {
        browser()
    }

    // Web (WasmJs)
    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class)
    wasmJs {
        browser()
    }

    sourceSets {
        commonMain.dependencies {
            api(project(":opalus-ui-core"))
            api(libs.kyant.backdrop)
            api(libs.kyant.shapes)
            api(compose.runtime)
            api(compose.foundation)
            api(compose.material3)
            api(compose.ui)
            api(compose.components.resources)
        }
    }
}

android {
    namespace = "io.github.opalusui.opalus.ui.liquid"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}

mavenPublishing {
    publishToMavenCentral()

    if (project.hasProperty("signingInMemoryKey") || project.hasProperty("signing.keyId")) {
        signAllPublications()
    }
    coordinates(group.toString(), "opalus-ui-liquid", version.toString())
    pom {
        name.set("Opalus UI Liquid")
        description.set("iOS 27 Fluid Glass LiquidTabBar for Compose Multiplatform")
        url.set("https://github.com/opalusui/opalus-ui-compose")

        licenses {
            license {
                name = "The Apache License, Version 2.0"
                url = "https://www.apache.org/licenses/LICENSE-2.0.txt"
                distribution = "https://www.apache.org/licenses/LICENSE-2.0.txt"
            }
        }

        developers {
            developer {
                id.set("aquamarinez")
                name.set("Aquamarinez")
            }
        }

        scm {
            url.set("https://github.com/opalusui/opalus-ui-compose")
        }
    }
}
