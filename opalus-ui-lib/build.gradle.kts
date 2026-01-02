group = "io.github.opalusui"
version = "0.1.3"
plugins {
    alias(libs.plugins.kotlinMultiplatform)
    // 如果你希望纯 Android 项目也能直接依赖这个库（生成 AAR），可以取消下面这行的注释
    alias(libs.plugins.androidLibrary)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    //id("maven-publish")
    id("signing")
    id("com.vanniktech.maven.publish") version "0.35.0"
}

kotlin {
    // Android
    androidTarget()

    // Desktop (JVM)
    jvm("desktop")

    // iOS 全系列（推荐这样写，覆盖真机 + 模拟器）
    iosX64()               // 老 Intel Mac 模拟器（可选，如果你不需要可以删）
    iosArm64()             // iPhone 真机
    iosSimulatorArm64()    // Apple Silicon Mac 模拟器

    // Web (JavaScript)
    js(IR) {
        browser()
        binaries.executable()
    }

    // Web (WasmJs) - 实验性，但 Compose 已经支持得越来越好
    @OptIn(org.jetbrains.kotlin.gradle.ExperimentalWasmDsl::class) wasmJs {
        browser()
        binaries.executable()
    }

    sourceSets {
        val commonMain by getting {
            dependencies {
                //implementation(libs.androidx.material.icons.extended)
                api(compose.runtime)
                api(compose.foundation)
                api(compose.material3)
                api(compose.ui)
                api(compose.components.resources)  // 如果你会用到资源，建议加上
                // api(compose.components.uiToolingPreview) // 预览相关，通常只在 demo 中用
            }
        }

        // 如果将来有平台特有实现，可以在这里添加
        // val androidMain by getting { }
        // val iosMain by getting { }
        // val desktopMain by getting { }
        // val jsMain by getting { }
        // val wasmJsMain by getting { }
    }
}

// 如果你取消了上面 androidLibrary 插件的注释，这里需要保留 android 块
android {
    namespace = "io.github.opalusui.opalus.ui"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}
mavenPublishing {
    publishToMavenCentral()

    signAllPublications()
    coordinates(group.toString(), "opalus-ui", version.toString())
    pom {
        name.set("Opalus UI")
        description.set("A Compose Multiplatform modal/surface UI library")
        url.set("https://github.com/aquamarine-z/opalus-ui-compose")

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
            url.set("https://github.com/aquamarine-z/opalus-ui-compose")
        }
    }


}
