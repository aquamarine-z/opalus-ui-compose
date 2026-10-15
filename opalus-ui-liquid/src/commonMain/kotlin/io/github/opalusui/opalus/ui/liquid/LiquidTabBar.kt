package io.github.opalusui.opalus.ui.liquid

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.emptyBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.shapes.Capsule

/**
 * iOS HIG 规范：顶级流体导航栏组件 (LiquidTabBar)
 */
@Composable
fun <T> LiquidTabBar(
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
    shapes: LiquidTabBarShapes = LiquidTabBarDefaults.shapes(),
    state: LiquidTabBarState = rememberLiquidTabBarState(selectedTabIndex, items.size, physics, events),
    inactiveItem: @Composable (index: Int, item: T) -> Unit,
    activeItem: @Composable (index: Int, item: T) -> Unit = inactiveItem,
    barShape: Shape = shapes.barShape,
    indicatorShape: Shape = Capsule(),
    inactiveIndicatorShape: Shape = if (barShape != shapes.barShape || indicatorShape !is Capsule) indicatorShape else shapes.inactiveIndicatorShape,
    activeIndicatorShape: Shape = if (barShape != shapes.barShape || indicatorShape !is Capsule) (if (indicatorShape is Capsule) CurvedCapsule() else indicatorShape) else shapes.activeIndicatorShape
) {
    if (items.isEmpty()) return

    SideEffect {
        state.updatePhysics(physics)
        state.updateEvents(events)
    }

    LaunchedEffect(state) {
        state.prewarm()
    }

    val dockBackdrop = rememberLayerBackdrop()
    val activeButtonsBackdrop = rememberLayerBackdrop()

    BoxWithConstraints(
        modifier = modifier.height(dimensions.containerHeight),
        contentAlignment = Alignment.CenterStart
    ) {
        val density = LocalDensity.current
        val padHorizontalPx = with(density) { dimensions.horizontalPadding.toPx() }
        val maxOverhangPx = with(density) { physics.maxOverhang.toPx() }
        val overhangCushionPx = with(density) { physics.overhangCushion.toPx() }
        val tabWidth = with(density) {
            (constraints.maxWidth.toFloat() - padHorizontalPx * 2f) / items.size
        }

        SideEffect {
            state.updateOverhangBounds(tabWidth, maxOverhangPx, overhangCushionPx)
        }

        val isLtr = LocalLayoutDirection.current == LayoutDirection.Ltr

        // 统一手势处理与图层容器
        DisableSelection {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(dimensions.containerHeight)
                    .graphicsLayer {
                        scaleX = state.scaleX
                        scaleY = state.scaleY
                    }
                    .pointerInput(tabWidth, items.size, isLtr, state, maxOverhangPx, overhangCushionPx) {
                        if (tabWidth <= 0f) return@pointerInput
                        val totalW = constraints.maxWidth.toFloat()

                        awaitEachGesture {
                            val down = awaitFirstDown(requireUnconsumed = false)
                            down.consume()

                            val downX = down.position.x
                            var lastX = downX

                            // 1. 按下瞬间：水滴立即激活，水滴几何中心立刻精准对齐鼠标 X 坐标
                            state.onPointerDown(downX, totalW, tabWidth, padHorizontalPx, isLtr, maxOverhangPx, overhangCushionPx)

                            try {
                                while (true) {
                                    val event = awaitPointerEvent()
                                    val change = event.changes.firstOrNull { it.id == down.id }
                                    if (change == null || !change.pressed) {
                                        break
                                    }
                                    change.consume()
                                    val currentX = change.position.x
                                    lastX = currentX

                                    // 2. 持续追踪：水滴几何中心跟随光标，左右边缘允许带弹性限度拉出
                                    state.onPointerMove(currentX, totalW, tabWidth, padHorizontalPx, isLtr, maxOverhangPx, overhangCushionPx)
                                }
                            } finally {
                                // 3. 鼠标松开或手势中断：依据手指/鼠标光标最后停留的区域决定目标槽位并触发回调，保证吸附
                                state.onPointerUp(
                                    lastX = lastX,
                                    totalWidth = totalW,
                                    tabWidth = tabWidth,
                                    padHorizontalPx = padHorizontalPx,
                                    isLtr = isLtr,
                                    onTabSelected = { targetIndex ->
                                        onTabSelected(targetIndex, items[targetIndex])
                                    }
                                )
                            }
                        }
                    },
                contentAlignment = Alignment.CenterStart
            ) {
                // 1. 底层：全局唯一实体底盘胶囊 (录制为 dockBackdrop) + 非激活态按钮 (反向几何遮罩剔除水滴覆盖区)
                LiquidTabBarPlatter(
                    dockBackdrop = dockBackdrop,
                    backdrop = backdrop,
                    items = items,
                    colors = colors,
                    dimensions = dimensions,
                    optics = optics,
                    physics = physics,
                    state = state,
                    tabWidth = tabWidth,
                    isLtr = isLtr,
                    barShape = barShape,
                    inactiveIndicatorShape = inactiveIndicatorShape,
                    activeIndicatorShape = activeIndicatorShape,
                    inactiveItem = inactiveItem
                )

                // 2. 中层：纯净激活态高亮按钮离屏捕获层 (被 activeButtonsBackdrop 录制，100% 透明背景，零底板)
                LiquidTabBarHighlightLayer(
                    activeButtonsBackdrop = activeButtonsBackdrop,
                    items = items,
                    colors = colors,
                    dimensions = dimensions,
                    physics = physics,
                    state = state,
                    activeItem = activeItem
                )

                // 3. 顶层：3D 物理流体水滴透镜 (三重物理组合采样：页面 + 实体底盘 + 激活高亮按钮)
                LiquidSelectionIndicator(
                    backdrop = backdrop,
                    dockBackdrop = dockBackdrop,
                    activeButtonsBackdrop = activeButtonsBackdrop,
                    colors = colors,
                    dimensions = dimensions,
                    optics = optics,
                    physics = physics,
                    state = state,
                    tabWidth = tabWidth,
                    tabsCount = items.size,
                    isLtr = isLtr,
                    inactiveIndicatorShape = inactiveIndicatorShape,
                    activeIndicatorShape = activeIndicatorShape
                )
            }
        }
    }
}

/**
 * 针对预定义数据模型 `LiquidTabBarItem` 的便捷重载
 */
@Composable
fun LiquidTabBar(
    items: List<LiquidTabBarItem>,
    selectedTabIndex: () -> Int,
    onTabSelected: (index: Int) -> Unit,
    backdrop: Backdrop = emptyBackdrop(),
    modifier: Modifier = Modifier,
    colors: LiquidTabBarColors = LiquidTabBarDefaults.colors(),
    dimensions: LiquidTabBarDimensions = LiquidTabBarDefaults.dimensions(),
    optics: LiquidTabBarOptics = LiquidTabBarDefaults.optics(),
    physics: LiquidTabBarPhysics = LiquidTabBarDefaults.physics(),
    events: LiquidTabBarEvents = LiquidTabBarDefaults.events(),
    state: LiquidTabBarState = rememberLiquidTabBarState(selectedTabIndex, items.size, physics, events),
    barShape: Shape = Capsule(),
    indicatorShape: Shape = Capsule(),
    inactiveIndicatorShape: Shape = indicatorShape,
    activeIndicatorShape: Shape = indicatorShape
) {
    LiquidTabBar(
        items = items,
        selectedTabIndex = selectedTabIndex,
        onTabSelected = { index, _ -> onTabSelected(index) },
        backdrop = backdrop,
        modifier = modifier,
        colors = colors,
        dimensions = dimensions,
        optics = optics,
        physics = physics,
        events = events,
        state = state,
        barShape = barShape,
        indicatorShape = indicatorShape,
        inactiveIndicatorShape = inactiveIndicatorShape,
        activeIndicatorShape = activeIndicatorShape,
        inactiveItem = { _, item -> item.inactive() },
        activeItem = { _, item -> item.active() }
    )
}
