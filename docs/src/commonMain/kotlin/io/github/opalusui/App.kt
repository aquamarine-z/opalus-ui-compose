package io.github.opalusui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
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
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.opalusui.opalus.ui.surface.SurfaceHost
import kotlinx.coroutines.launch

enum class NavDestination(val title: String, val badge: String) {
    OVERVIEW("Overview", "Start"),
    LIQUID("Liquid Tab Bar", "HIG 27"),
    DIALOGS("Dialogs System", "Core"),
    INSTALLATION("Installation", "v0.2.3")
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
                BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
                    val isMobile = maxWidth < 840.dp
                    val scope = rememberCoroutineScope()
                    val drawerState = rememberDrawerState(DrawerValue.Closed)

                    val contentComposable: @Composable () -> Unit = {
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

                    if (isMobile) {
                        ModalNavigationDrawer(
                            drawerState = drawerState,
                            drawerContent = {
                                ModalDrawerSheet(
                                    modifier = Modifier.widthIn(max = 300.dp).fillMaxHeight(),
                                    drawerContainerColor = MaterialTheme.colorScheme.surface
                                ) {
                                    M3SidebarContent(
                                        currentNav = currentNav,
                                        onSelectNav = {
                                            currentNav = it
                                            scope.launch { drawerState.close() }
                                        },
                                        onClose = { scope.launch { drawerState.close() } },
                                        isDrawer = true,
                                        modifier = Modifier.fillMaxSize()
                                    )
                                }
                            }
                        ) {
                            Column(modifier = Modifier.fillMaxSize()) {
                                M3TopAppBar(
                                    isDarkTheme = isDarkTheme,
                                    onToggleTheme = { isDarkTheme = !isDarkTheme },
                                    isMobile = true,
                                    onOpenDrawer = { scope.launch { drawerState.open() } }
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(1.dp)
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .weight(1f)
                                ) {
                                    contentComposable()
                                }
                            }
                        }
                    } else {
                        Column(modifier = Modifier.fillMaxSize()) {
                            M3TopAppBar(
                                isDarkTheme = isDarkTheme,
                                onToggleTheme = { isDarkTheme = !isDarkTheme },
                                isMobile = false,
                                onOpenDrawer = {}
                            )
                            Box(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(1.dp)
                                    .background(MaterialTheme.colorScheme.surfaceVariant)
                            )
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .weight(1f)
                            ) {
                                M3SidebarContent(
                                    currentNav = currentNav,
                                    onSelectNav = { currentNav = it },
                                    modifier = Modifier
                                        .width(280.dp)
                                        .fillMaxHeight()
                                )
                                Box(
                                    modifier = Modifier
                                        .fillMaxHeight()
                                        .width(1.dp)
                                        .background(MaterialTheme.colorScheme.surfaceVariant)
                                )
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxHeight()
                                ) {
                                    contentComposable()
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
private fun M3MenuIcon(
    tint: Color,
    modifier: Modifier = Modifier.size(24.dp)
) {
    Canvas(modifier = modifier) {
        val stroke = 2.dp.toPx()
        val w = size.width
        val h = size.height
        val startX = w * 0.15f
        val endX = w * 0.85f
        drawLine(tint, Offset(startX, h * 0.28f), Offset(endX, h * 0.28f), strokeWidth = stroke, cap = StrokeCap.Round)
        drawLine(tint, Offset(startX, h * 0.50f), Offset(endX, h * 0.50f), strokeWidth = stroke, cap = StrokeCap.Round)
        drawLine(tint, Offset(startX, h * 0.72f), Offset(endX, h * 0.72f), strokeWidth = stroke, cap = StrokeCap.Round)
    }
}

@Composable
private fun M3CloseIcon(
    tint: Color,
    modifier: Modifier = Modifier.size(20.dp)
) {
    Canvas(modifier = modifier) {
        val stroke = 2.dp.toPx()
        val w = size.width
        val h = size.height
        val inset = w * 0.22f
        val end = w * 0.78f
        drawLine(tint, Offset(inset, inset), Offset(end, end), strokeWidth = stroke, cap = StrokeCap.Round)
        drawLine(tint, Offset(inset, end), Offset(end, inset), strokeWidth = stroke, cap = StrokeCap.Round)
    }
}

@Composable
private fun M3TopAppBar(
    isDarkTheme: Boolean,
    onToggleTheme: () -> Unit,
    isMobile: Boolean,
    onOpenDrawer: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = if (isMobile) 16.dp else 24.dp, vertical = if (isMobile) 10.dp else 14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: [Drawer Button if mobile] + Title & Badge
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (isMobile) {
                IconButton(onClick = onOpenDrawer) {
                    M3MenuIcon(tint = MaterialTheme.colorScheme.onSurface)
                }
                Spacer(modifier = Modifier.width(4.dp))
            }

            Column {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Opalus UI",
                        style = if (isMobile) MaterialTheme.typography.titleMedium else MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black,
                        color = MaterialTheme.colorScheme.onSurface,
                        letterSpacing = (-0.5).sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    M3Badge("v0.2.3", isPrimary = true)
                }
                if (!isMobile) {
                    Text(
                        text = "Material You Design System & HIG Fluid Glass Components",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        // Right: Badges (if desktop) + Switch
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (!isMobile) {
                M3Badge("Android")
                Spacer(modifier = Modifier.width(6.dp))
                M3Badge("Desktop JVM")
                Spacer(modifier = Modifier.width(6.dp))
                M3Badge("iOS")
                Spacer(modifier = Modifier.width(6.dp))
                M3Badge("Web Wasm")
                Spacer(modifier = Modifier.width(16.dp))
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                if (!isMobile) {
                    Text(
                        text = if (isDarkTheme) "Dark" else "Light",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                }
                Switch(
                    checked = isDarkTheme,
                    onCheckedChange = { onToggleTheme() }
                )
            }
        }
    }
}

@Composable
private fun M3SidebarContent(
    currentNav: NavDestination,
    onSelectNav: (NavDestination) -> Unit,
    onClose: (() -> Unit)? = null,
    isDrawer: Boolean = false,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
            .verticalScroll(rememberScrollState())
    ) {
        if (isDrawer && onClose != null) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Opalus UI",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    M3Badge("v0.2.3", isPrimary = true)
                }
                IconButton(onClick = onClose) {
                    M3CloseIcon(tint = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }

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

        Spacer(modifier = Modifier.height(24.dp))

        if (isDrawer) {
            Text(
                text = "PLATFORMS",
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f),
                letterSpacing = 1.sp,
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
            )
            Row(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                M3Badge("Android")
                M3Badge("JVM")
                M3Badge("iOS")
                M3Badge("Wasm")
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

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
