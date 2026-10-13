package io.github.opalusui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import io.github.opalusui.opalus.ui.liquid.LiquidBoundaryEdge
import io.github.opalusui.opalus.ui.liquid.LiquidTabBar
import io.github.opalusui.opalus.ui.liquid.LiquidTabBarDefaults
import io.github.opalusui.opalus.ui.liquid.LiquidTabBarDimensions
import io.github.opalusui.opalus.ui.liquid.LiquidTabBarEvents
import io.github.opalusui.opalus.ui.liquid.LiquidTabItem

@Composable
fun LiquidDemoPage(isDarkTheme: Boolean) {
    var selectedTab1 by remember { mutableStateOf(0) }
    val tabs1 = remember { listOf("Home", "Explore", "Favorites", "Profile") }

    var selectedTab2 by remember { mutableStateOf(0) }
    val tabs2 = remember { listOf("Dashboard", "Analytics", "Settings") }
    val eventLogState = remember { mutableStateOf("Ready (Idle)") }

    var physics by remember { mutableStateOf(LiquidTabBarDefaults.physics()) }
    var optics by remember {
        mutableStateOf(LiquidTabBarDefaults.optics(isDark = isDarkTheme))
    }
    var dimensions by remember { mutableStateOf(LiquidTabBarDefaults.dimensions()) }
    var showPhysicsDialog by remember { mutableStateOf(false) }
    var showExportDialog by remember { mutableStateOf(false) }

    val playgroundBackdrop = rememberLayerBackdrop()

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
            // Section Header
            M3SectionHeader(
                title = "LiquidTabBar Component",
                subtitle = "iOS 27 HIG-compliant suspended fluid glass tab bar featuring optical refraction lens, triple-backdrop sampling, and anti-ghosting difference cutout.",
                badge = "opalus-ui-liquid"
            )

            // Demo 1: Full-Featured Wallpaper Refraction Playground
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(if (isMobile) 18.dp else 24.dp)) {
                    if (isMobile) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Column {
                                Text(
                                    text = "Demo 1: Real-Time Optical Refraction Playground",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Scroll the vibrant reference cards underneath the floating dock to inspect live optical distortion and lens magnification.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Chromatic Aberration",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Switch(
                                        checked = optics.chromaticAberration,
                                        onCheckedChange = { optics = optics.copy(chromaticAberration = it) }
                                    )
                                }

                                FilledTonalButton(
                                    onClick = { showPhysicsDialog = true },
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("参数调优", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                }

                                OutlinedButton(
                                    onClick = { showExportDialog = true },
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("导出", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Demo 1: Real-Time Optical Refraction Playground",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "Scroll the vibrant reference cards underneath the floating dock to inspect live optical distortion and lens magnification.",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(12.dp)
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(
                                        text = "Chromatic Aberration",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = MaterialTheme.colorScheme.onSurface
                                    )
                                    Spacer(modifier = Modifier.width(6.dp))
                                    Switch(
                                        checked = optics.chromaticAberration,
                                        onCheckedChange = { optics = optics.copy(chromaticAberration = it) }
                                    )
                                }

                                FilledTonalButton(
                                    onClick = { showPhysicsDialog = true },
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Physics & Optics Tuner / 参数调优", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }

                                OutlinedButton(
                                    onClick = { showExportDialog = true },
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("导出配置 (HTML/JSON)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    // Scrollable Viewport with suspended LiquidTabBar
                    DisableSelection {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(if (isMobile) 400.dp else 480.dp)
                                .clip(RoundedCornerShape(20.dp))
                                .border(1.dp, MaterialTheme.colorScheme.surfaceVariant, RoundedCornerShape(20.dp))
                                .background(Color(0xFF0B0F17)),
                            contentAlignment = Alignment.BottomCenter
                        ) {
                    // 1. Long Scrollable Reference Gallery
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .layerBackdrop(playgroundBackdrop)
                            .verticalScroll(rememberScrollState())
                            .padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(16.dp)
                    ) {
                        ArtworkTile(
                            tag = "SPECTRUM I",
                            title = "PACIFIC SUNSET GRADIENT",
                            description = "High-contrast radial warmth for optical meniscus testing",
                            brush = Brush.verticalGradient(
                                listOf(Color(0xFFFF512F), Color(0xFFDD2476), Color(0xFFFF9900))
                            )
                        )

                        ArtworkTile(
                            tag = "SPECTRUM II",
                            title = "CYBER NEON AURORA",
                            description = "Electric cyan, ultraviolet and vivid fuchsia mesh",
                            brush = Brush.linearGradient(
                                listOf(Color(0xFF00F2FE), Color(0xFF4FACFE), Color(0xFF9B51E0))
                            )
                        )

                        ArtworkTile(
                            tag = "SPECTRUM III",
                            title = "EMERALD PRISM MATRIX",
                            description = "Deep botanical greens revealing subtle edge dispersion",
                            brush = Brush.radialGradient(
                                listOf(Color(0xFF00B09B), Color(0xFF96C93D), Color(0xFF0B1F19))
                            )
                        )

                        // High frequency typography benchmark
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(16.dp))
                                .background(Color(0xFF1E293B))
                                .padding(24.dp)
                        ) {
                            Column {
                                Text(
                                    text = "3D LENS BENCHMARK PATTERN",
                                    fontSize = 17.sp,
                                    fontWeight = FontWeight.Black,
                                    color = Color.White
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Apple HIG specifies a 3D convex lens profile with physical displacement. Notice how text glyphs deform smoothly as the fluid droplet sweeps over them.",
                                    fontSize = 12.sp,
                                    color = Color(0xFFCBD5E1),
                                    lineHeight = 18.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(96.dp))
                    }

                    // 2. Suspended Floating LiquidTabBar
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 24.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        LiquidTabBar(
                            items = tabs1,
                            selectedTabIndex = { selectedTab1 },
                            onTabSelected = { index, _ -> selectedTab1 = index },
                            backdrop = playgroundBackdrop,
                            colors = LiquidTabBarDefaults.colors(
                                isDark = isDarkTheme,
                                activeItemColor = MaterialTheme.colorScheme.primary,
                                inactiveItemColor = if (isDarkTheme) Color(0xFFCBD5E1) else Color(0xFF64748B)
                            ),
                            optics = optics,
                            physics = physics,
                            dimensions = dimensions,
                            modifier = Modifier
                                .widthIn(max = 440.dp)
                                .fillMaxWidth(0.92f),
                            inactiveItem = { _, item ->
                                LiquidTabItem(
                                    label = {
                                        Text(
                                            text = item,
                                            fontSize = 12.sp,
                                            color = if (isDarkTheme) Color(0xFFCBD5E1) else Color(0xFF64748B)
                                        )
                                    }
                                )
                            },
                            activeItem = { _, item ->
                                LiquidTabItem(
                                    label = {
                                        Text(
                                            text = item,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                )
                            }
                        )
                    }
                }
            }
        }
    }

        // Demo 2: Clean Minimalist 3-Tab Dock
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Demo 2: Minimalist Navigation Dock",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "Stand-alone navigation bar with subtle frosted blur platter, primary active highlight, and responsive pointer tracking.",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    FilledTonalButton(
                        onClick = { showPhysicsDialog = true },
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("调优参数", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                DisableSelection {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(16.dp))
                            .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                            .padding(vertical = 36.dp, horizontal = 16.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        LiquidTabBar(
                            items = tabs2,
                            selectedTabIndex = { selectedTab2 },
                            onTabSelected = { index, _ -> selectedTab2 = index },
                            colors = LiquidTabBarDefaults.colors(
                                isDark = isDarkTheme,
                                activeItemColor = MaterialTheme.colorScheme.primary,
                                inactiveItemColor = MaterialTheme.colorScheme.onSurfaceVariant
                            ),
                            optics = optics,
                            physics = physics,
                            dimensions = dimensions,
                            events = remember {
                                LiquidTabBarEvents(
                                    onDropletActivated = { index ->
                                        eventLogState.value = "onDropletActivated(index = $index)"
                                    },
                                    onDropletDeactivated = { finalIndex ->
                                        eventLogState.value = "onDropletDeactivated(finalIndex = $finalIndex)"
                                    },
                                    onTabCrossed = { from, to ->
                                        eventLogState.value = "onTabCrossed(from = $from, to = $to)"
                                    },
                                    onSlotHovered = { slot ->
                                        eventLogState.value = "onSlotHovered(slot = $slot)"
                                    },
                                    onBoundaryReached = { edge ->
                                        eventLogState.value = "onBoundaryReached(edge = $edge)"
                                    },
                                    onMaxOverhangReached = { edge ->
                                        eventLogState.value = "onMaxOverhangReached(edge = $edge)"
                                    },
                                    onBoundaryPull = { edge, ratio ->
                                        eventLogState.value = "onBoundaryPull(edge = $edge, ratio = ${((ratio * 100).toInt())}%)"
                                    },
                                    onDockingStarted = { target ->
                                        eventLogState.value = "onDockingStarted(target = $target)"
                                    },
                                    onDockingSettled = { settled ->
                                        eventLogState.value = "onDockingSettled(settled = $settled)"
                                    }
                                )
                            },
                            modifier = Modifier.widthIn(max = 380.dp),
                            inactiveItem = { _, item ->
                                LiquidTabItem(
                                    label = {
                                        Text(
                                            text = item,
                                            fontSize = 12.sp,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                )
                            },
                            activeItem = { _, item ->
                                LiquidTabItem(
                                    label = {
                                        Text(
                                            text = item,
                                            fontSize = 12.sp,
                                            fontWeight = FontWeight.Bold,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                    }
                                )
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Active Tab: Index $selectedTab2 (${tabs2[selectedTab2]})",
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                    LiquidTabBarEventLog(eventLogState)
                }
            }
        }

        // Section: Exhaustive Class-by-Class API Documentation
        M3SectionHeader(
            title = "Class & Component API Reference",
            subtitle = "Detailed documentation for every class, factory, design token, state machine, and architectural layer in opalus-ui-liquid.",
            badge = "HIG 27 Specification"
        )

        // 1. LiquidTabBar
        M3ClassDocCard(
            className = "LiquidTabBar",
            kind = "Composable Component",
            signature = """@Composable fun <T> LiquidTabBar(
    items: List<T>,
    selectedTabIndex: () -> Int,
    onTabSelected: (index: Int, item: T) -> Unit,
    backdrop: Backdrop = emptyBackdrop(),
    modifier: Modifier = Modifier,
    colors: LiquidTabBarColors = LiquidTabBarDefaults.colors(),
    dimensions: LiquidTabBarDimensions = LiquidTabBarDefaults.dimensions(),
    optics: LiquidTabBarOptics = LiquidTabBarDefaults.optics(),
    physics: LiquidTabBarPhysics = LiquidTabBarDefaults.physics(),
    events: LiquidTabBarEvents = LiquidTabBarDefaults.events(),
    state: LiquidTabBarState = rememberLiquidTabBarState(selectedTabIndex, items.size, physics, events),
    inactiveItem: @Composable (index: Int, item: T) -> Unit,
    activeItem: @Composable (index: Int, item: T) -> Unit = inactiveItem
)""",
            description = "The primary entrypoint composable for the iOS 27 suspended fluid glass tab bar. Coordinates the 3-layer optical pipeline: the physical platter base, the 100% transparent offscreen active buttons layer, and the top 3D convex droplet lens. Handles low-level drag gestures with pointer down snapping and elastic rubber-band resistance.",
            tips = "TIP: For maximum visual fidelity, wrap the underlying scrollable page UI with `Modifier.layerBackdrop(backdrop)` and pass that backdrop instance into `LiquidTabBar(backdrop = backdrop)`. Place the tab bar inside a `Box` with `Alignment.BottomCenter`.",
            parameters = listOf(
                ParamItem("items", "List<T>", "Required", "List of data items, navigation destinations, or models to render into tab slots."),
                ParamItem("selectedTabIndex", "() -> Int", "Required", "Lambda provider returning the currently active tab index (0-based integer)."),
                ParamItem("onTabSelected", "(Int, T) -> Unit", "Required", "Callback triggered when pointer/finger is released over a tab slot, returning selected index and item."),
                ParamItem("backdrop", "Backdrop", "emptyBackdrop()", "Backdrop instance capturing the underlying screen UI to refract through the 3D fluid lens."),
                ParamItem("colors", "LiquidTabBarColors", "Defaults.colors()", "Design tokens specifying platter, border, indicator, and button colors for light/dark mode."),
                ParamItem("dimensions", "LiquidTabBarDimensions", "Defaults.dimensions()", "Spatial geometry tokens defining container height (84dp), platter height (58dp), and margins."),
                ParamItem("optics", "LiquidTabBarOptics", "Defaults.optics()", "Refraction height, micro-matte blur, specular highlight, shadows, and chromatic aberration."),
                ParamItem("physics", "LiquidTabBarPhysics", "Defaults.physics()", "Kinematics constants: spring stiffness, damping, overhang resistance, and velocity factor."),
                ParamItem("events", "LiquidTabBarEvents", "Defaults.events()", "Lifecycle event listeners for pointer activation, tab crossing, boundary pulling, and docking settlement."),
                ParamItem("state", "LiquidTabBarState", "rememberState()", "State machine tracking touch pointer position, velocity, and animated droplet translation."),
                ParamItem("inactiveItem", "@Composable (Int, T)", "Required", "Slot Composable for unselected tabs; rendered inside platter with anti-ghosting difference cutout."),
                ParamItem("activeItem", "@Composable (Int, T)", "inactiveItem", "Slot Composable for selected tabs; rendered onto offscreen transparent layer and magnified by lens.")
            ),
            codeExample = """val pageBackdrop = rememberLayerBackdrop()
var selectedIndex by remember { mutableStateOf(0) }
val items = listOf("Home", "Search", "Profile")

Box(modifier = Modifier.fillMaxSize()) {
    // 1. Capture scrollable UI content
    Column(
        modifier = Modifier
            .fillMaxSize()
            .layerBackdrop(pageBackdrop)
            .verticalScroll(rememberScrollState())
    ) {
        // App Content
    }

    // 2. Suspended LiquidTabBar
    LiquidTabBar(
        items = items,
        selectedTabIndex = { selectedIndex },
        onTabSelected = { index, _ -> selectedIndex = index },
        backdrop = pageBackdrop,
        colors = LiquidTabBarDefaults.colors(
            isDark = true,
            activeItemColor = MaterialTheme.colorScheme.primary
        ),
        optics = LiquidTabBarDefaults.optics(chromaticAberration = true),
        modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 24.dp),
        inactiveItem = { _, item -> LiquidTabItem(label = { Text(item) }) },
        activeItem = { _, item -> LiquidTabItem(label = { Text(item, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) }) }
    )
}"""
        )

        // 2. LiquidTabItem & LiquidTabBarItem
        M3ClassDocCard(
            className = "LiquidTabItem & LiquidTabBarItem",
            kind = "Slot Layout & Data Model",
            signature = """@Composable fun LiquidTabItem(
    icon: @Composable () -> Unit = {},
    label: @Composable () -> Unit = {},
    modifier: Modifier = Modifier,
    spacing: Dp = 2.dp
)

data class LiquidTabBarItem(
    val inactive: @Composable () -> Unit,
    val active: @Composable () -> Unit = inactive
)""",
            description = "`LiquidTabItem` is the standard HIG column slot for individual tab buttons. It vertically centers and spaces icons and labels without overflowing the platter bounds. `LiquidTabBarItem` is a declarative model for applications preferring pre-built item lists instead of generic types `<T>`.",
            tips = "TIP: When using `LiquidTabItem`, keep text font sizes between 11.sp and 13.sp and icon sizes between 18.dp and 22.dp to comply with Apple HIG mobile touch target guidelines.",
            parameters = listOf(
                ParamItem("icon", "@Composable () -> Unit", "{}", "Slot Composable for the tab icon (e.g. Vector icon or Image)."),
                ParamItem("label", "@Composable () -> Unit", "{}", "Slot Composable for the tab text label."),
                ParamItem("modifier", "Modifier", "Modifier", "Modifier applied to the tab slot container."),
                ParamItem("spacing", "Dp", "2.dp", "Vertical spacing between icon and label inside the column."),
                ParamItem("inactive", "@Composable () -> Unit", "Required", "Composable rendered when the item is unselected."),
                ParamItem("active", "@Composable () -> Unit", "inactive", "Composable rendered when the item is selected under the fluid droplet.")
            ),
            codeExample = """// Using LiquidTabBarItem list overload:
val tabItems = listOf(
    LiquidTabBarItem(
        inactive = { LiquidTabItem(label = { Text("Home", color = Color.Gray) }) },
        active = { LiquidTabItem(label = { Text("Home", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) }) }
    ),
    LiquidTabBarItem(
        inactive = { LiquidTabItem(label = { Text("Settings", color = Color.Gray) }) },
        active = { LiquidTabItem(label = { Text("Settings", fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary) }) }
    )
)

LiquidTabBar(
    items = tabItems,
    selectedTabIndex = { selectedIndex },
    onTabSelected = { selectedIndex = it }
)"""
        )

        // 3. LiquidTabBarDefaults
        M3ClassDocCard(
            className = "LiquidTabBarDefaults",
            kind = "Factory Object",
            signature = """object LiquidTabBarDefaults {
    fun colors(isDark: Boolean = false, ...): LiquidTabBarColors
    fun dimensions(...): LiquidTabBarDimensions
    fun optics(isDark: Boolean = false, ...): LiquidTabBarOptics
    fun physics(...): LiquidTabBarPhysics
}""",
            description = "The centralized token factory object providing default values tuned against Apple HIG specifications. Automatically adjusts platter opacities, specular highlights, and shadows depending on whether `isDark = true` or `isDark = false`.",
            parameters = listOf(
                ParamItem("colors(isDark)", "LiquidTabBarColors", "Defaults", "Generates palette tokens tuned for dark or light backgrounds."),
                ParamItem("dimensions()", "LiquidTabBarDimensions", "Defaults", "Generates geometry dimensions with 84dp container and 58dp platter."),
                ParamItem("optics(isDark)", "LiquidTabBarOptics", "Defaults", "Generates lens refraction, shadow, and micro-blur parameters."),
                ParamItem("physics()", "LiquidTabBarPhysics", "Defaults", "Generates spring damping, stretch factor, and overhang resistance.")
            ),
            codeExample = """val customColors = LiquidTabBarDefaults.colors(
    isDark = isSystemInDarkTheme(),
    activeItemColor = MaterialTheme.colorScheme.primary,
    platterColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.45f)
)

val customOptics = LiquidTabBarDefaults.optics(
    isDark = isSystemInDarkTheme(),
    chromaticAberration = true,
    indicatorRefractionAmount = 10.dp
)"""
        )

        // 4. LiquidTabBarColors
        M3ClassDocCard(
            className = "LiquidTabBarColors",
            kind = "Design Tokens (Immutable)",
            signature = """@Immutable data class LiquidTabBarColors(
    val platterColor: Color,
    val platterBorderColor: Color,
    val indicatorIdleColor: Color,
    val indicatorActiveTint: Color,
    val indicatorBorderColor: Color,
    val activeItemColor: Color,
    val inactiveItemColor: Color
)""",
            description = "Encapsulates color and opacity tokens for every visual layer of the navigation dock. Distinguishes between the static idle slot tint and the luminous fluid tint applied while dragging.",
            parameters = listOf(
                ParamItem("platterColor", "Color", "White 33% / #1C1C1E 37%", "Translucent frosted background color of the bottom dock capsule."),
                ParamItem("platterBorderColor", "Color", "Black 6% / White 12%", "Subtle physical border stroke outlining the platter."),
                ParamItem("indicatorIdleColor", "Color", "Black 6% / White 8%", "Static slot backdrop visible when the dock is at rest."),
                ParamItem("indicatorActiveTint", "Color", "White 2%", "Ultra-clean luminous tint drawn over the droplet lens during drag gestures."),
                ParamItem("indicatorBorderColor", "Color", "White 14% / White 8%", "Perimeter stroke around the droplet lens."),
                ParamItem("activeItemColor", "Color", "#007AFF / #0A84FF", "Accent color supplied to active tab icons and labels."),
                ParamItem("inactiveItemColor", "Color", "Black 60% / White 60%", "Muted color supplied to inactive tab icons and labels.")
            ),
            codeExample = """val myColors = LiquidTabBarColors(
    platterColor = Color(0xFF1E293B).copy(alpha = 0.5f),
    platterBorderColor = Color.White.copy(alpha = 0.15f),
    indicatorIdleColor = Color.White.copy(alpha = 0.1f),
    indicatorActiveTint = Color.White.copy(alpha = 0.05f),
    indicatorBorderColor = Color.White.copy(alpha = 0.2f),
    activeItemColor = MaterialTheme.colorScheme.primary,
    inactiveItemColor = MaterialTheme.colorScheme.onSurfaceVariant
)"""
        )

        // 5. LiquidTabBarDimensions
        M3ClassDocCard(
            className = "LiquidTabBarDimensions",
            kind = "Design Tokens (Immutable)",
            signature = """@Immutable data class LiquidTabBarDimensions(
    val containerHeight: Dp = 84.dp,
    val platterHeight: Dp = 58.dp,
    val indicatorIdleHeight: Dp = 50.dp,
    val indicatorActiveHeightScale: Float = 1.44f,
    val indicatorActiveWidthScale: Float = 1.0f,
    val horizontalPadding: Dp = 4.dp,
    val borderWidth: Dp = 0.6.dp,
    val indicatorBorderWidth: Dp = 0.4.dp
) {
    val indicatorActiveHeight: Dp get() = indicatorIdleHeight * indicatorActiveHeightScale
}""",
            description = "Defines the exact physical proportions and scaling ratios of the navigation dock. `containerHeight` (84dp) is intentionally taller than `platterHeight` (58dp) to provide vertical headroom for active droplet expansion. `indicatorActiveHeightScale` (1.44x) scales active droplet height to 72dp, while `indicatorActiveWidthScale` (1.0x) governs horizontal active expansion.",
            parameters = listOf(
                ParamItem("containerHeight", "Dp", "84.dp", "Total bounding box height accommodating droplet expansion."),
                ParamItem("platterHeight", "Dp", "58.dp", "Height of the frosted capsule dock platter."),
                ParamItem("indicatorIdleHeight", "Dp", "50.dp", "Height of the droplet indicator when resting inside the platter."),
                ParamItem("indicatorActiveHeightScale", "Float", "1.44f", "Vertical scaling factor when active (50dp * 1.44 = 72dp)."),
                ParamItem("indicatorActiveWidthScale", "Float", "1.0f", "Horizontal scaling factor relative to tab slot width when active."),
                ParamItem("horizontalPadding", "Dp", "4.dp", "Horizontal inset between platter edges and tab slots."),
                ParamItem("borderWidth", "Dp", "0.6.dp", "Stroke width of the outer platter border."),
                ParamItem("indicatorBorderWidth", "Dp", "0.4.dp", "Stroke width of the droplet lens border.")
            ),
            codeExample = """val compactDimensions = LiquidTabBarDefaults.dimensions(
    containerHeight = 72.dp,
    platterHeight = 50.dp,
    indicatorIdleHeight = 44.dp,
    indicatorActiveHeightScale = 1.35f,
    indicatorActiveWidthScale = 1.05f
)"""
        )

        // 6. LiquidTabBarOptics
        M3ClassDocCard(
            className = "LiquidTabBarOptics",
            kind = "Shader & Lens Tokens (Immutable)",
            signature = """@Immutable data class LiquidTabBarOptics(
    val platterBlur: Dp = 1.dp,
    val platterRefractionHeight: Dp = 14.dp,
    val platterRefractionAmount: Dp = 10.dp,
    val platterHighlightAlpha: Float = 0.24f,
    val indicatorRefractionHeightMin: Dp = 6.dp,
    val indicatorRefractionHeightMax: Dp = 14.dp,
    val indicatorRefractionAmount: Dp = 8.dp,
    val indicatorHighlightAlpha: Float = 0.20f,
    val indicatorShadowRadius: Dp = 8.dp,
    val indicatorShadowOffsetY: Dp = 2.5.dp,
    val indicatorShadowAlpha: Float = 0.45f,
    val indicatorInnerShadowRadius: Dp = 4.dp,
    val indicatorInnerShadowAlpha: Float = 0.28f,
    val chromaticAberration: Boolean = true
)""",
            description = "Configures SkSL hardware-accelerated shader properties. Controls background Gaussian blur, 3D convex lens meniscus curvature, specular rim light, and RGB chromatic aberration wavelength dispersion.",
            parameters = listOf(
                ParamItem("platterBlur", "Dp", "1.dp", "Gaussian blur radius applied to the dock base platter."),
                ParamItem("platterRefractionHeight", "Dp", "14.dp", "Virtual depth for platter 3D optical lens distortion."),
                ParamItem("platterRefractionAmount", "Dp", "10.dp", "Magnification magnitude for the platter background."),
                ParamItem("platterHighlightAlpha", "Float", "0.24f / 0.40f", "Opacity of the specular rim reflection on the platter."),
                ParamItem("indicatorRefractionHeightMin/Max", "Dp", "6.dp / 14.dp", "Dynamic meniscus depth lerped by pointer press progress."),
                ParamItem("indicatorRefractionAmount", "Dp", "8.dp", "Magnification factor of the 3D droplet lens."),
                ParamItem("indicatorHighlightAlpha", "Float", "0.16f / 0.10f", "Specular edge highlight intensity on the droplet."),
                ParamItem("indicatorShadowRadius / Offset", "Dp", "8.dp / 2.5.dp", "Drop shadow radius and Y offset casting underneath the droplet."),
                ParamItem("indicatorInnerShadowRadius", "Dp", "4.dp", "Inner shadow radius creating 3D volume depth."),
                ParamItem("chromaticAberration", "Boolean", "true", "Enables optical dispersion splitting RGB channels at lens edges.")
            ),
            codeExample = """val highContrastOptics = LiquidTabBarDefaults.optics(
    chromaticAberration = true,
    indicatorRefractionAmount = 12.dp,
    indicatorHighlightAlpha = 0.30f
)"""
        )

        // 7. LiquidTabBarPhysics
        M3ClassDocCard(
            className = "LiquidTabBarPhysics",
            kind = "Kinematics Tokens (Immutable)",
            signature = """@Immutable data class LiquidTabBarPhysics(
    val maxOverhang: Dp = 14.dp,
    val overhangResistance: Float = 1.8f,
    val overhangBounceAmplitude: Float = 1.90f,
    val overhangCushion: Dp = 4.dp,
    val overhangSettlingStiffness: Float = 85f,
    val overhangSettlingDampingRatio: Float = 0.60f,
    val platterActiveScaleX: Float = 1.03f,
    val platterActiveScaleY: Float = 1.03f,
    val itemActiveScale: Float = 1.05f,
    val activeItemScale: Float = 1.15f,
    val indicatorActiveScale: Float = 1.05f,
    val velocityHorizontalFactor: Float = 0.65f,
    val velocityVerticalFactor: Float = 0.098f,
    val maxVelocityCoerce: Float = 0.16f,
    val dragVisibilityThreshold: Float = 0.001f,
    val dragDampingRatio: Float = 0.70f,
    val dragStiffness: Float = 205f,
    val dockingDampingRatio: Float = 0.90f,
    val dockingStiffness: Float = 550f,
    val dockingInertiaFactor: Float = 0.027f,
    val growDampingRatio: Float = 0.45f,
    val growStiffness: Float = 1470f,
    val shrinkDampingRatio: Float = 0.55f,
    val shrinkStiffness: Float = 150f,
    val dropletElasticity: Float = 1.0f,
    val dockingShrinkThreshold: Float = 0.20f
)""",
            description = "Defines the physical kinematics governing drag damping, spring stiffness, rubber-band resistance at container borders, grow/shrink speeds, docking inertia deviation, and droplet squash-and-stretch elasticity.",
            parameters = listOf(
                ParamItem("maxOverhang", "Dp", "14.dp", "Maximum distance in Dp the droplet is allowed to be dragged past the navbar edge along the X axis."),
                ParamItem("overhangResistance", "Float", "1.8f", "Rubber-band exponent dampening pointer drag outside boundary slots."),
                ParamItem("overhangBounceAmplitude", "Float", "1.90f", "Fixed collision rebound impulse independent of pointer fling speed upon hitting boundary."),
                ParamItem("overhangCushion", "Dp", "4.dp", "Fixed outward cushion displacement margin allowing subtle fluid compression at edge."),
                ParamItem("overhangSettlingStiffness / Damping", "Float", "85f / 0.60f", "Softer returning spring and gentle jelly oscillation when settling back from boundary."),
                ParamItem("platterActiveScaleX", "Float", "1.03f", "Horizontal scale multiplier of the navbar platter body when active/pressed."),
                ParamItem("platterActiveScaleY", "Float", "1.03f", "Vertical scale multiplier of the navbar platter body when active/pressed."),
                ParamItem("itemActiveScale", "Float", "1.05f", "Scale applied to inactive items during drag."),
                ParamItem("activeItemScale", "Float", "1.15f", "Breathing zoom scale applied to the active tab button."),
                ParamItem("indicatorActiveScale", "Float", "1.05f", "Scale applied to the fluid droplet during drag."),
                ParamItem("velocityHorizontalFactor", "Float", "0.65f", "Horizontal aspect-ratio elongation driven by drag velocity."),
                ParamItem("velocityVerticalFactor", "Float", "0.098f", "Vertical aspect-ratio compression driven by drag velocity."),
                ParamItem("maxVelocityCoerce", "Float", "0.16f", "Clamp limit preventing extreme distortion during fast flicks."),
                ParamItem("dragDampingRatio / dragStiffness", "Float", "0.70f / 205f", "Spring physics for droplet transit motion during pointer drag."),
                ParamItem("dockingDampingRatio / dockingStiffness", "Float", "0.90f / 550f", "Under-damped spring governing gentle and swift settling into target slot."),
                ParamItem("dockingInertiaFactor", "Float", "0.027f", "Inertial impulse multiplier applied to fling velocity and docking displacement."),
                ParamItem("growDampingRatio / growStiffness", "Float", "0.45f / 1470f", "High-speed snappy expansion dynamics with liquid surface tension bounce."),
                ParamItem("shrinkDampingRatio / shrinkStiffness", "Float", "0.55f / 150f", "Soft, gradual easing dynamics when settling into a docked slot with elastic rebound."),
                ParamItem("dropletElasticity", "Float", "1.0f", "Controls settling duration and harmonic jelly bounce dynamics when returning from active state to inactive rest."),
                ParamItem("dockingShrinkThreshold", "Float", "0.20f", "Distance threshold to target slot before the traveling droplet begins shrinking.")
            ),
            codeExample = """val fluidPhysics = LiquidTabBarDefaults.physics(
    dockingDampingRatio = 0.90f,
    dockingStiffness = 550f,
    dropletElasticity = 1.0f,
    shrinkDampingRatio = 0.55f
)"""
        )

        // 8. LiquidTabBarEvents & LiquidBoundaryEdge
        M3ClassDocCard(
            className = "LiquidTabBarEvents & LiquidBoundaryEdge",
            kind = "Lifecycle Event Callbacks (Immutable)",
            signature = """enum class LiquidBoundaryEdge { Start, End }

@Immutable data class LiquidTabBarEvents(
    val onDropletActivated: ((index: Int) -> Unit)? = null,
    val onDropletDeactivated: ((finalIndex: Int) -> Unit)? = null,
    val onTabCrossed: ((fromIndex: Int, toIndex: Int) -> Unit)? = null,
    val onSlotHovered: ((slotIndex: Int) -> Unit)? = null,
    val onBoundaryReached: ((edge: LiquidBoundaryEdge) -> Unit)? = null,
    val onMaxOverhangReached: ((edge: LiquidBoundaryEdge) -> Unit)? = null,
    val onBoundaryPull: ((edge: LiquidBoundaryEdge, pullRatio: Float) -> Unit)? = null,
    val onDockingStarted: ((targetIndex: Int) -> Unit)? = null,
    val onDockingSettled: ((settledIndex: Int) -> Unit)? = null
)""",
            description = "Provides pure lifecycle hooks across the entire fluid kinematics pipeline: from pointer press expansion, crossing adjacent tab midlines, stretching past container bounds, to spring docking settlement. All callbacks default to null so callers only listen to what they need with zero bundled animations, haptic, or sound side-effects.",
            parameters = listOf(
                ParamItem("onDropletActivated", "((Int) -> Unit)?", "null", "Triggered the instant pointer presses down and the fluid droplet expands into its active state."),
                ParamItem("onDropletDeactivated", "((Int) -> Unit)?", "null", "Triggered when pointer lifts up, releasing the droplet from drag mode."),
                ParamItem("onTabCrossed", "((Int, Int) -> Unit)?", "null", "Fires when droplet center crosses the midline between two tab slots (fromIndex -> toIndex). Ideal for haptic ticks."),
                ParamItem("onSlotHovered", "((Int) -> Unit)?", "null", "Triggered when the candidate drop slot changes during active dragging."),
                ParamItem("onBoundaryReached", "((LiquidBoundaryEdge) -> Unit)?", "null", "Fires once upon pulling the droplet past the Start or End container edge into elastic overhang zone."),
                ParamItem("onMaxOverhangReached", "((LiquidBoundaryEdge) -> Unit)?", "null", "Fires once when droplet displacement reaches near-maximum overhang limit (pullRatio >= 0.95f)."),
                ParamItem("onBoundaryPull", "((LiquidBoundaryEdge, Float) -> Unit)?", "null", "Continuous stream of normalized pull ratio (0.0f..1.0f) while stretching beyond container borders."),
                ParamItem("onDockingStarted", "((Int) -> Unit)?", "null", "Fires when release occurs and the fluid spring docking animation begins toward the target slot."),
                ParamItem("onDockingSettled", "((Int) -> Unit)?", "null", "Fires when docking impulse, under-damped rebound, and jelly bounce completely settle to rest.")
            ),
            codeExample = """val events = LiquidTabBarEvents(
    onDropletActivated = { index -> println("Droplet active at: ${'$'}index") },
    onTabCrossed = { from, to -> hapticFeedback.performHapticFeedback(HapticFeedbackType.TextHandleMove) },
    onBoundaryReached = { edge -> println("Hit boundary: ${'$'}edge") },
    onDockingSettled = { settledIndex -> println("Settled at tab: ${'$'}settledIndex") }
)"""
        )

        // 9. LiquidTabBarState
        M3ClassDocCard(
            className = "LiquidTabBarState & rememberLiquidTabBarState",
            kind = "Motion State Machine",
            signature = """@Composable fun rememberLiquidTabBarState(
    selectedTabIndex: () -> Int,
    tabsCount: Int,
    physics: LiquidTabBarPhysics = LiquidTabBarDefaults.physics(),
    events: LiquidTabBarEvents = LiquidTabBarDefaults.events()
): LiquidTabBarState

class LiquidTabBarState {
    val animatedValue: Float
    val pressProgress: Float
    val velocity: Float
    val scaleX: Float
    val scaleY: Float
    suspend fun animateTo(targetIndex: Int)
    fun snapTo(targetIndex: Int)
}""",
            description = "The core reactive state machine orchestrating pointer tracking and spring animations. Calculates real-time drag velocity, updates animated droplet positions, and handles programmatic navigation transitions.",
            parameters = listOf(
                ParamItem("selectedTabIndex", "() -> Int", "Required", "Lambda provider supplying current selected tab index."),
                ParamItem("tabsCount", "Int", "Required", "Total number of tabs displayed in the dock."),
                ParamItem("physics", "LiquidTabBarPhysics", "Defaults", "Physics configuration used for velocity coercion and damping."),
                ParamItem("events", "LiquidTabBarEvents", "Defaults.events()", "Lifecycle events configuration synced dynamically across recompositions."),
                ParamItem("animatedValue", "Float (Get)", "Observable", "Continuous float representing droplet center position (0.0 to N-1)."),
                ParamItem("pressProgress", "Float (Get)", "0f..1f", "Animated progress from resting (0f) to active dragging (1f)."),
                ParamItem("velocity", "Float (Get)", "Points/sec", "Instantaneous horizontal pointer velocity used for lens squash-and-stretch."),
                ParamItem("animateTo(index)", "suspend fun", "Target", "Programmatically springs the droplet to the given tab index.")
            ),
            codeExample = """val state = rememberLiquidTabBarState(
    selectedTabIndex = { selectedIndex },
    tabsCount = items.size
)

// Programmatic tab switching from coroutine
LaunchedEffect(deepLinkTab) {
    state.animateTo(deepLinkTab)
}"""
        )

        // 10. Triple-Backdrop Sampling Architecture
        M3ClassDocCard(
            className = "Triple-Backdrop & Anti-Ghosting Architecture",
            kind = "Internal Rendering Engine",
            signature = """// 1. Platter Layer
LiquidTabBarPlatter (dockBackdrop + clipPath(dropletPath, ClipOp.Difference))

// 2. Highlight Layer
LiquidTabBarHighlightLayer (activeButtonsBackdrop + alpha = 0f)

// 3. Selection Droplet Lens
LiquidSelectionIndicator (rememberCombinedBackdrop(page, dockBackdrop, activeButtonsBackdrop))""",
            description = "To solve Compose multi-layer blending artifacts (such as color bleed, washed-out frosted glass, or ghosting overlaps), LiquidTabBar employs a 3-layer architecture:\n\n1. Platter Layer: Renders a single capsule with micro-blur. Inactive buttons are drawn using clipPath(dropletPath, ClipOp.Difference) to physically cut out the droplet zone, guaranteeing that gray labels never overlap with colored active labels.\n2. Highlight Layer: Rendered offscreen at 100% transparency (alpha = 0f) and captured into activeButtonsBackdrop. Only active icons and labels are rendered here with elastic breathing scale.\n3. Selection Indicator: Top-level 3D convex droplet lens that samples the combined backdrop (page + dock + active buttons) and refracts them together, producing genuine optical magnification without ghosting.",
            parameters = listOf(
                ParamItem("dockBackdrop", "LayerBackdrop", "Internal", "Captures the platter capsule so the droplet can refract the platter texture without cutting holes to the page."),
                ParamItem("activeButtonsBackdrop", "LayerBackdrop", "Internal", "Captures isolated colored labels for optical lens magnification."),
                ParamItem("ClipOp.Difference", "Skia ClipOp", "Automatic", "Subtracts the droplet footprint from inactive labels to achieve zero ghosting.")
            ),
            codeExample = """// Conceptual flow inside LiquidTabBar:
val dockBackdrop = rememberLayerBackdrop()
val activeButtonsBackdrop = rememberLayerBackdrop()

Box {
    // 1. Bottom: Platter + Difference Cutout
    LiquidTabBarPlatter(dockBackdrop = dockBackdrop, ...)

    // 2. Middle: Transparent Highlight Layer
    LiquidTabBarHighlightLayer(activeButtonsBackdrop = activeButtonsBackdrop, ...)

    // 3. Top: 3D Droplet Lens Sampling All 3 Layers
    LiquidSelectionIndicator(
        backdrop = pageBackdrop,
        dockBackdrop = dockBackdrop,
        activeButtonsBackdrop = activeButtonsBackdrop,
        ...
    )
}"""
        )
    }

    if (showPhysicsDialog) {
        LiquidPhysicsTuningDialog(
            isDarkTheme = isDarkTheme,
            currentPhysics = physics,
            onPhysicsChange = { physics = it },
            currentOptics = optics,
            onOpticsChange = { optics = it },
            currentDimensions = dimensions,
            onDimensionsChange = { dimensions = it },
            onExportClick = { showExportDialog = true },
            onDismissRequest = { showPhysicsDialog = false }
        )
    }

    if (showExportDialog) {
        LiquidConfigExportDialog(
            physics = physics,
            dimensions = dimensions,
            optics = optics,
            isDarkTheme = isDarkTheme,
            onDismissRequest = { showExportDialog = false }
        )
    }
}
}

@Composable
private fun LiquidTabBarEventLog(eventLogState: State<String>) {
    Text(
        text = "Live Event: ${eventLogState.value}",
        fontFamily = FontFamily.Monospace,
        fontSize = 11.sp,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun ArtworkTile(
    tag: String,
    title: String,
    description: String,
    brush: Brush
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(160.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(brush)
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = tag,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White.copy(alpha = 0.85f)
                )
                Box(
                    modifier = Modifier
                        .size(10.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                )
            }
            Column {
                Text(
                    text = title,
                    fontSize = 17.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color.White
                )
                Text(
                    text = description,
                    fontSize = 12.sp,
                    color = Color.White.copy(alpha = 0.85f)
                )
            }
        }
    }
}
