package io.github.opalusui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.opalusui.opalus.ui.surface.SurfaceHost

enum class NavDestination(val title: String, val badge: String) {
    OVERVIEW("Overview", "Start"),
    LIQUID("Liquid Tab Bar", "HIG 27"),
    DIALOGS("Dialogs System", "Core"),
    INSTALLATION("Installation", "v0.2.0")
}

@Composable
fun App() {
    var isDarkTheme by remember { mutableStateOf(true) }
    var currentNav by remember { mutableStateOf(NavDestination.OVERVIEW) }

    // Material You (Material 3) Tonal Color Schemes
    val colorScheme = if (isDarkTheme) {
        darkColorScheme(
            primary = Color(0xFF9ECAFF),
            onPrimary = Color(0xFF003258),
            primaryContainer = Color(0xFF00497D),
            onPrimaryContainer = Color(0xFFD1E4FF),
            secondary = Color(0xFFBBC7DB),
            onSecondary = Color(0xFF253140),
            secondaryContainer = Color(0xFF3B4858),
            onSecondaryContainer = Color(0xFFD7E3F7),
            tertiary = Color(0xFFD7BEE4),
            onTertiary = Color(0xFF3B2948),
            tertiaryContainer = Color(0xFF523F5F),
            onTertiaryContainer = Color(0xFFF3DAFF),
            background = Color(0xFF111318),
            onBackground = Color(0xFFE2E2E9),
            surface = Color(0xFF191C20),
            onSurface = Color(0xFFC6C6CD),
            surfaceVariant = Color(0xFF282A2F),
            onSurfaceVariant = Color(0xFF8E9099)
        )
    } else {
        lightColorScheme(
            primary = Color(0xFF0061A4),
            onPrimary = Color(0xFFFFFFFF),
            primaryContainer = Color(0xFFD1E4FF),
            onPrimaryContainer = Color(0xFF001D36),
            secondary = Color(0xFF535F70),
            onSecondary = Color(0xFFFFFFFF),
            secondaryContainer = Color(0xFFD7E3F7),
            onSecondaryContainer = Color(0xFF101C2B),
            tertiary = Color(0xFF6B5778),
            onTertiary = Color(0xFFFFFFFF),
            tertiaryContainer = Color(0xFFF3DAFF),
            onTertiaryContainer = Color(0xFF251432),
            background = Color(0xFFFDFBFF),
            onBackground = Color(0xFF1A1C1E),
            surface = Color(0xFFFFFFFF),
            onSurface = Color(0xFF1A1C1E),
            surfaceVariant = Color(0xFFECEFF5),
            onSurfaceVariant = Color(0xFF44474F)
        )
    }

    MaterialTheme(colorScheme = colorScheme) {
        SurfaceHost {
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = MaterialTheme.colorScheme.background
            ) {
                Column(modifier = Modifier.fillMaxSize()) {
                    // Material You Top App Bar
                    M3TopAppBar(
                        isDarkTheme = isDarkTheme,
                        onToggleTheme = { isDarkTheme = !isDarkTheme }
                    )

                    // 1dp Divider Line
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(MaterialTheme.colorScheme.surfaceVariant)
                    )

                    // Body: Navigation Rail / Sidebar + Content Area
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1f)
                    ) {
                        // Left Sidebar Catalog
                        M3SidebarRail(
                            currentNav = currentNav,
                            onSelectNav = { currentNav = it },
                            modifier = Modifier
                                .width(280.dp)
                                .fillMaxHeight()
                        )

                        // 1dp Vertical Divider
                        Box(
                            modifier = Modifier
                                .fillMaxHeight()
                                .width(1.dp)
                                .background(MaterialTheme.colorScheme.surfaceVariant)
                        )

                        // Main Content Body
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                        ) {
                            SelectionContainer {
                                when (currentNav) {
                                    NavDestination.OVERVIEW -> OverviewPage(
                                        onNavigateToLiquid = { currentNav = NavDestination.LIQUID },
                                        onNavigateToDialogs = { currentNav = NavDestination.DIALOGS }
                                    )
                                    NavDestination.LIQUID -> LiquidDemoPage(isDarkTheme = isDarkTheme)
                                    NavDestination.DIALOGS -> DialogsDemoPage()
                                    NavDestination.INSTALLATION -> InstallationPage()
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun M3TopAppBar(
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 24.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Brand & Title
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "Opalus UI",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Black,
                    color = MaterialTheme.colorScheme.onSurface,
                    letterSpacing = (-0.5).sp
                )
                Spacer(modifier = Modifier.width(10.dp))
                M3Badge("v0.2.0", isPrimary = true)
            }
            Text(
                text = "Material You Design System & HIG Fluid Glass Components",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        // Actions & Targets
        Row(verticalAlignment = Alignment.CenterVertically) {
            M3Badge("Android")
            Spacer(modifier = Modifier.width(6.dp))
            M3Badge("Desktop JVM")
            Spacer(modifier = Modifier.width(6.dp))
            M3Badge("iOS")
            Spacer(modifier = Modifier.width(6.dp))
            M3Badge("Web Wasm")
            Spacer(modifier = Modifier.width(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = if (isDarkTheme) "Dark" else "Light",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.width(8.dp))
                Switch(
                    checked = isDarkTheme,
                    onCheckedChange = { onToggleTheme() }
                )
            }
        }
    }
}

@Composable
private fun M3SidebarRail(
    currentNav: NavDestination,
    onSelectNav: (NavDestination) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        Text(
            text = "COMPONENTS & DOCS",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            letterSpacing = 1.sp,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
        )

        NavDestination.entries.forEach { dest ->
            val isSelected = dest == currentNav
            // Material You pill indicator shape and color
            val pillBg = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer
            } else {
                Color.Transparent
            }
            val textCol = if (isSelected) {
                MaterialTheme.colorScheme.onPrimaryContainer
            } else {
                MaterialTheme.colorScheme.onSurface
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .background(pillBg)
                    .clickable { onSelectNav(dest) }
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = dest.title,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = textCol
                )

                M3Badge(text = dest.badge, isPrimary = isSelected)
            }
            Spacer(modifier = Modifier.height(4.dp))
        }

        Spacer(modifier = Modifier.height(32.dp))

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(MaterialTheme.colorScheme.surfaceVariant)
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "ABOUT OPALUS",
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
            letterSpacing = 1.sp,
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
        )

        Text(
            text = "Designed for seamless multiplatform deployment. Open source under Apache 2.0 license.",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(horizontal = 12.dp),
            lineHeight = 18.sp
        )
    }
}
