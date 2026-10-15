package io.github.opalusui.opalus.ui.liquid

import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.foundation.layout.size
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.addOutline
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.emptyBackdrop
import com.kyant.backdrop.backdrops.rememberCombinedBackdrop
import com.kyant.backdrop.backdrops.rememberLayerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.shadow.InnerShadow
import com.kyant.backdrop.shadow.Shadow
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
                // 0. 静默管线预热器：在初次挂载时以 0 视觉开销预热 Skia GPU 通用路径裁切与 Lens 渲染管线，
                // 彻底杜绝首次点击时 0.5s 的 Direct3D/Vulkan 着色器冷编译卡顿。
                LiquidPipelineWarmer(
                    backdrop = backdrop,
                    dockBackdrop = dockBackdrop,
                    activeButtonsBackdrop = activeButtonsBackdrop,
                    activeIndicatorShape = activeIndicatorShape,
                    optics = optics
                )

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

/**
 * 离屏渲染管线静默预热器。
 *
 * 在初次挂载时以 0 像素可见度（alpha = 0.005f，经 8-bit 色彩量化后严格截断为 0 纯透明）
 * 触发一次 Skia GPU 的通用路径裁切（Path Difference Clip）与 Lens 透镜着色器评估。
 * 促使 Direct3D/Metal/Vulkan 驱动在页面刚展示的空闲帧提前完成着色器冷编译与 PSO 显存缓存，
 * 并在 2 帧后完全卸载，从而在不损失任何微弧质感与视觉体验的前提下，彻底消除首次交互的 0.5s 停顿。
 */
@Composable
private fun LiquidPipelineWarmer(
    backdrop: Backdrop,
    dockBackdrop: Backdrop,
    activeButtonsBackdrop: Backdrop,
    activeIndicatorShape: Shape,
    optics: LiquidTabBarOptics
) {
    var isWarmedUp by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        // 预热 Compose MonotonicFrameClock 与协程调度环
        withFrameNanos { }
        withFrameNanos { }
        isWarmedUp = true
    }

    if (!isWarmedUp) {
        val combinedBackdrop = rememberCombinedBackdrop(backdrop, dockBackdrop, activeButtonsBackdrop)
        val warmerPath = remember { Path() }
        val lensSupported = activeIndicatorShape.supportsLiquidLens()

        Box(
            modifier = Modifier
                .size(4.dp)
                .graphicsLayer {
                    alpha = 0.005f
                }
                .drawWithContent {
                    warmerPath.reset()
                    if (activeIndicatorShape is CurvedCapsule) {
                        activeIndicatorShape.populatePath(
                            path = warmerPath,
                            left = 0f,
                            top = 0f,
                            right = size.width,
                            bottom = size.height,
                            density = this
                        )
                    } else if (activeIndicatorShape is InterpolatedIndicatorShape) {
                        activeIndicatorShape.populatePath(
                            path = warmerPath,
                            left = 0f,
                            top = 0f,
                            right = size.width,
                            bottom = size.height,
                            layoutDirection = layoutDirection,
                            density = this
                        )
                    } else {
                        val outline = activeIndicatorShape.createOutline(size, layoutDirection, this)
                        warmerPath.addOutline(outline)
                    }
                    clipPath(warmerPath, clipOp = ClipOp.Difference) {
                        drawRect(Color.Transparent)
                    }
                }
                .drawBackdrop(
                    backdrop = combinedBackdrop,
                    shape = { activeIndicatorShape },
                    effects = {
                        if (lensSupported) {
                            lens(
                                refractionHeight = optics.indicatorRefractionHeightMax.toPx(),
                                refractionAmount = optics.indicatorRefractionAmount.toPx(),
                                depthEffect = true,
                                chromaticAberration = optics.chromaticAberration
                            )
                        }
                    },
                    shadow = {
                        Shadow(
                            radius = optics.indicatorShadowRadius,
                            offset = DpOffset(0.dp, optics.indicatorShadowOffsetY),
                            alpha = 0.01f
                        )
                    },
                    innerShadow = {
                        InnerShadow(
                            radius = optics.indicatorInnerShadowRadius,
                            alpha = 0.01f
                        )
                    }
                )
        )
    }
}
