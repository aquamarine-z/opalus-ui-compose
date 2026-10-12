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
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
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
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import io.github.opalusui.opalus.ui.liquid.LiquidTabBar
import io.github.opalusui.opalus.ui.liquid.LiquidTabBarDefaults
import io.github.opalusui.opalus.ui.liquid.LiquidTabItem

@Composable
fun LiquidDemoPage(isDarkTheme: Boolean) {
    var chromaticAberration by remember { mutableStateOf(true) }
    var selectedTab1 by remember { mutableStateOf(0) }
    val tabs1 = remember { listOf("Home", "Explore", "Favorites", "Profile") }

    var selectedTab2 by remember { mutableStateOf(0) }
    val tabs2 = remember { listOf("Dashboard", "Analytics", "Settings") }

    var physics by remember { mutableStateOf(LiquidTabBarDefaults.physics()) }
    var showPhysicsDialog by remember { mutableStateOf(false) }

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
                                        checked = chromaticAberration,
                                        onCheckedChange = { chromaticAberration = it }
                                    )
                                }

                                FilledTonalButton(
                                    onClick = { showPhysicsDialog = true },
                                    shape = RoundedCornerShape(10.dp)
                                ) {
                                    Text("动力学调优", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
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
                                        checked = chromaticAberration,
                                        onCheckedChange = { chromaticAberration = it }
                                    )
                                }

                                FilledTonalButton(
                                    onClick = { showPhysicsDialog = true },
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Physics Tuner / 动力学调优", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
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
                            optics = LiquidTabBarDefaults.optics(
                                isDark = isDarkTheme,
                                chromaticAberration = chromaticAberration
                            ),
                            physics = physics,
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
                            physics = physics,
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
                Text(
                    text = "Active Tab: Index $selectedTab2 (${tabs2[selectedTab2]})",
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    color = MaterialTheme.colorScheme.primary
                )
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
    state: LiquidTabBarState = rememberLiquidTabBarState(selectedTabIndex, items.size, physics),
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
    val indicatorActiveHeight: Dp = 72.dp,
    val horizontalPadding: Dp = 4.dp,
    val borderWidth: Dp = 0.6.dp,
    val indicatorBorderWidth: Dp = 0.4.dp
)""",
            description = "Defines the exact physical proportions of the navigation dock. Notice that `containerHeight` (84dp) is intentionally taller than `platterHeight` (58dp): this creates vertical headroom for the fluid droplet to expand (+7dp top, +7dp bottom) during interactive dragging without being clipped by parent bounds.",
            parameters = listOf(
                ParamItem("containerHeight", "Dp", "84.dp", "Total bounding box height accommodating droplet expansion."),
                ParamItem("platterHeight", "Dp", "58.dp", "Height of the frosted capsule dock platter."),
                ParamItem("indicatorIdleHeight", "Dp", "50.dp", "Height of the droplet indicator when resting inside the platter."),
                ParamItem("indicatorActiveHeight", "Dp", "72.dp", "Height of the droplet lens while dragged (+7dp vertical overhang)."),
                ParamItem("horizontalPadding", "Dp", "4.dp", "Horizontal inset between platter edges and tab slots."),
                ParamItem("borderWidth", "Dp", "0.6.dp", "Stroke width of the outer platter border."),
                ParamItem("indicatorBorderWidth", "Dp", "0.4.dp", "Stroke width of the droplet lens border.")
            ),
            codeExample = """val compactDimensions = LiquidTabBarDefaults.dimensions(
    containerHeight = 72.dp,
    platterHeight = 50.dp,
    indicatorIdleHeight = 44.dp,
    indicatorActiveHeight = 62.dp
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
    val chromaticAberration: Boolean = false
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
                ParamItem("chromaticAberration", "Boolean", "false", "Enables optical dispersion splitting RGB channels at lens edges.")
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
    val maxOverhang: Float = 0.38f,
    val overhangResistance: Float = 1.5f,
    val platterActiveScale: Float = 1.03f,
    val itemActiveScale: Float = 1.05f,
    val activeItemScale: Float = 1.15f,
    val indicatorActiveScale: Float = 1.05f,
    val velocityHorizontalFactor: Float = 0.75f,
    val velocityVerticalFactor: Float = 0.25f,
    val maxVelocityCoerce: Float = 0.2f,
    val dragVisibilityThreshold: Float = 0.001f,
    val dragDampingRatio: Float = 0.85f,
    val dragStiffness: Float = 950f,
    val dockingDampingRatio: Float = 0.62f,
    val dockingStiffness: Float = 600f,
    val dockingInertiaFactor: Float = 1.0f,
    val growDampingRatio: Float = 0.68f,
    val growStiffness: Float = 1350f,
    val shrinkDampingRatio: Float = 0.65f,
    val shrinkStiffness: Float = 420f,
    val dropletElasticity: Float = 0.35f,
    val dockingShrinkThreshold: Float = 0.35f
)""",
            description = "Defines the physical kinematics governing drag damping, spring stiffness, rubber-band resistance at container borders, grow/shrink speeds, docking inertia deviation, and droplet squash-and-stretch elasticity.",
            parameters = listOf(
                ParamItem("maxOverhang", "Float", "0.38f", "Maximum fraction of tab width the droplet can stretch past the first or last tab."),
                ParamItem("overhangResistance", "Float", "1.5f", "Rubber-band exponent dampening pointer drag outside boundary slots."),
                ParamItem("platterActiveScale", "Float", "1.03f", "Scale applied to the entire dock when pointer is pressed."),
                ParamItem("itemActiveScale", "Float", "1.05f", "Scale applied to inactive items during drag."),
                ParamItem("activeItemScale", "Float", "1.15f", "Breathing zoom scale applied to the active tab button."),
                ParamItem("indicatorActiveScale", "Float", "1.05f", "Scale applied to the fluid droplet during drag."),
                ParamItem("velocityHorizontalFactor", "Float", "0.75f", "Horizontal aspect-ratio elongation driven by drag velocity."),
                ParamItem("velocityVerticalFactor", "Float", "0.25f", "Vertical aspect-ratio compression driven by drag velocity."),
                ParamItem("maxVelocityCoerce", "Float", "0.2f", "Clamp limit preventing extreme distortion during fast flicks."),
                ParamItem("dragDampingRatio / dragStiffness", "Float", "0.85f / 950f", "Spring physics for droplet transit motion during pointer drag."),
                ParamItem("dockingDampingRatio / dockingStiffness", "Float", "0.62f / 600f", "Under-damped spring governing inertial entry overshoot (left entry leans right, right entry leans left)."),
                ParamItem("dockingInertiaFactor", "Float", "1.0f", "Inertial impulse multiplier applied to fling velocity and docking displacement."),
                ParamItem("growDampingRatio / growStiffness", "Float", "0.68f / 1350f", "High-speed snappy expansion dynamics with liquid surface tension bounce."),
                ParamItem("shrinkDampingRatio / shrinkStiffness", "Float", "0.65f / 420f", "Soft, gradual easing dynamics when settling into a docked slot with elastic rebound."),
                ParamItem("dropletElasticity", "Float", "0.45f", "Controls settling duration and harmonic jelly bounce dynamics when returning from active state to inactive rest."),
                ParamItem("dockingShrinkThreshold", "Float", "0.35f", "Distance threshold to target slot before the traveling droplet begins shrinking.")
            ),
            codeExample = """val fluidPhysics = LiquidTabBarDefaults.physics(
    dockingDampingRatio = 0.58f,
    dockingInertiaFactor = 1.2f,
    dropletElasticity = 0.45f,
    shrinkDampingRatio = 0.62f
)"""
        )

        // 8. LiquidTabBarState
        M3ClassDocCard(
            className = "LiquidTabBarState & rememberLiquidTabBarState",
            kind = "Motion State Machine",
            signature = """@Composable fun rememberLiquidTabBarState(
    selectedTabIndex: () -> Int,
    tabsCount: Int,
    physics: LiquidTabBarPhysics = LiquidTabBarDefaults.physics()
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

        // 9. Triple-Backdrop Sampling Architecture
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
            onDismissRequest = { showPhysicsDialog = false }
        )
    }
}
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
