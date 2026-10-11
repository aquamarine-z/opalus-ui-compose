# Opalus UI Compose
## Description
Opalus UI Compose is the Compose Multiplatform version of Opalus UI in React.  
We prepared the following components to provide a better experience for your project:
- [x] **Surface System** : A register and render system for surfaces such as Modals, Dialogs and Drawers.
- [x] **Dialog** : A better async function-based dialog group to handle dialogs, which containing following dialog types:
    - **AlertDialog**
    - **ConfirmDialog**
- [x] **Liquid Tab Bar** (`opalus-ui-liquid`) : iOS 27 Human Interface Guidelines fluid glass optics navigation bar with Triple-Backdrop rendering, SkSL shaders, and spring physics.

## Installation

### Version Catalog (`gradle/libs.versions.toml`)
```toml
[versions]
opalusUi = "0.2.1"

[libraries]
opalus-ui-core = { module = "io.github.opalusui:opalus-ui-core", version.ref = "opalusUi" }
opalus-ui-liquid = { module = "io.github.opalusui:opalus-ui-liquid", version.ref = "opalusUi" }
```

### Module `build.gradle.kts`
```kotlin
kotlin {
    sourceSets {
        commonMain.dependencies {
            implementation(libs.opalus.ui.core)
            implementation(libs.opalus.ui.liquid)
        }
    }
}
```

Or direct dependency coordinates:
```kotlin
implementation("io.github.opalusui:opalus-ui-core:0.2.1")
implementation("io.github.opalusui:opalus-ui-liquid:0.2.1")
```