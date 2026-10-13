package io.github.opalusui.opalus.ui.liquid

import androidx.compose.runtime.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collectLatest
import kotlin.math.floor
import kotlin.math.tanh

/**
 * iOS HIG 规范：流体导航栏交互与动力学状态管理机 (LiquidTabBarState)
 */
@Stable
class LiquidTabBarState(
    val animationScope: CoroutineScope,
    initialIndex: Int,
    val tabsCount: Int,
    initialPhysics: LiquidTabBarPhysics = LiquidTabBarDefaults.physics(),
    initialEvents: LiquidTabBarEvents = LiquidTabBarDefaults.events()
) {
    var currentIndex by mutableIntStateOf(initialIndex)
        private set

    var physics by mutableStateOf(initialPhysics)
    var events by mutableStateOf(initialEvents)

    private var lastHoveredSlot: Int = initialIndex
    private var startBoundaryHit: Boolean = false
    private var endBoundaryHit: Boolean = false
    private var startMaxOverhangHit: Boolean = false
    private var endMaxOverhangHit: Boolean = false

    val dampedAnimation = DampedDragAnimation(
        animationScope = animationScope,
        initialValue = initialIndex.toFloat(),
        initialValueRange = (-0.38f)..((tabsCount - 1).toFloat() + 0.38f),
        visibilityThreshold = physics.dragVisibilityThreshold,
        initialScale = 1f,
        pressedScale = physics.platterActiveScale,
        pressedScaleX = physics.platterActiveScaleX,
        pressedScaleY = physics.platterActiveScaleY,
        tabsCount = tabsCount,
        initialPhysics = initialPhysics,
        onDragStarted = {},
        onDragStopped = {},
        onDrag = { _, _ -> },
        onAnimationSettled = { settledIndex ->
            events.onDockingSettled?.invoke(settledIndex)
        }
    )

    fun updatePhysics(newPhysics: LiquidTabBarPhysics) {
        if (physics != newPhysics) {
            physics = newPhysics
            dampedAnimation.physics = newPhysics
        }
    }

    fun updateEvents(newEvents: LiquidTabBarEvents) {
        if (events != newEvents) {
            events = newEvents
        }
    }

    /**
     * 根据当前容器宽度与 maxOverhang/overhangCushion 物理参数，动态约束水滴 X 轴允许移动的绝对物理区间
     */
    fun updateOverhangBounds(tabWidth: Float, maxOverhangPx: Float, overhangCushionPx: Float = 0f) {
        if (tabWidth <= 0f) return
        val maxOverhangSlots = (maxOverhangPx / tabWidth).coerceAtLeast(0.001f)
        val cushionSlots = (overhangCushionPx / tabWidth).coerceAtLeast(0f)
        val minBound = -maxOverhangSlots - cushionSlots
        val maxBound = (tabsCount - 1).toFloat() + maxOverhangSlots + cushionSlots
        val currentRange = dampedAnimation.valueRange
        if (currentRange.start != minBound || currentRange.endInclusive != maxBound) {
            dampedAnimation.valueRange = minBound..maxBound
        }
    }

    val animatedValue: Float
        get() = dampedAnimation.value.coerceIn(dampedAnimation.valueRange)

    val pressProgress: Float
        get() = dampedAnimation.pressProgress

    val scaleX: Float
        get() = dampedAnimation.scaleX

    val scaleY: Float
        get() = dampedAnimation.scaleY

    val velocity: Float
        get() = dampedAnimation.velocity

    val jelly: Float
        get() = dampedAnimation.jelly

    val targetValue: Float
        get() = dampedAnimation.targetValue

    /**
     * 计算光标在 Tab 坐标系下的原始非约束相对位置
     */
    fun computeRawTarget(
        x: Float,
        totalWidth: Float,
        tabWidth: Float,
        padHorizontalPx: Float,
        isLtr: Boolean
    ): Float {
        return if (isLtr) {
            (x - padHorizontalPx) / tabWidth - 0.5f
        } else {
            (totalWidth - padHorizontalPx - x) / tabWidth - 0.5f
        }
    }

    /**
     * 将光标屏幕横坐标转换为带物理阻尼的水滴目标插值位置
     */
    fun computeTargetFromPointer(
        x: Float,
        totalWidth: Float,
        tabWidth: Float,
        padHorizontalPx: Float,
        isLtr: Boolean,
        maxOverhangPx: Float = tabWidth * 0.38f
    ): Float {
        val maxTab = (tabsCount - 1).toFloat()
        val rawTarget = computeRawTarget(x, totalWidth, tabWidth, padHorizontalPx, isLtr)
        val maxOverhangSlots = (maxOverhangPx / tabWidth).coerceAtLeast(0.001f)
        val computed = when {
            rawTarget < 0f -> {
                val excess = -rawTarget
                -maxOverhangSlots * tanh(excess / (maxOverhangSlots * physics.overhangResistance))
            }
            rawTarget > maxTab -> {
                val excess = rawTarget - maxTab
                maxTab + maxOverhangSlots * tanh(excess / (maxOverhangSlots * physics.overhangResistance))
            }
            else -> rawTarget
        }
        return computed.coerceIn(-maxOverhangSlots, maxTab + maxOverhangSlots)
    }

    /**
     * 根据光标最终停靠的绝对物理坐标计算所属 Tab 槽位
     */
    fun computeSlotFromPointer(
        x: Float,
        totalWidth: Float,
        tabWidth: Float,
        padHorizontalPx: Float,
        isLtr: Boolean
    ): Int {
        val relX = if (isLtr) x - padHorizontalPx else totalWidth - padHorizontalPx - x
        return floor(relX / tabWidth).toInt().coerceIn(0, tabsCount - 1)
    }

    /**
     * 响应手指/光标按下
     */
    fun onPointerDown(
        x: Float,
        totalWidth: Float,
        tabWidth: Float,
        padHorizontalPx: Float,
        isLtr: Boolean,
        maxOverhangPx: Float = tabWidth * 0.38f,
        overhangCushionPx: Float = 0f
    ) {
        updateOverhangBounds(tabWidth, maxOverhangPx, overhangCushionPx)
        dampedAnimation.press()
        val target = computeTargetFromPointer(x, totalWidth, tabWidth, padHorizontalPx, isLtr, maxOverhangPx)
        dampedAnimation.updateValue(target)

        val initialSlot = computeSlotFromPointer(x, totalWidth, tabWidth, padHorizontalPx, isLtr)
        lastHoveredSlot = initialSlot
        startBoundaryHit = false
        endBoundaryHit = false
        startMaxOverhangHit = false
        endMaxOverhangHit = false

        events.onDropletActivated?.invoke(initialSlot)
    }

    /**
     * 响应手指/光标移动
     */
    fun onPointerMove(
        x: Float,
        totalWidth: Float,
        tabWidth: Float,
        padHorizontalPx: Float,
        isLtr: Boolean,
        maxOverhangPx: Float = tabWidth * 0.38f,
        overhangCushionPx: Float = 0f
    ) {
        updateOverhangBounds(tabWidth, maxOverhangPx, overhangCushionPx)
        val target = computeTargetFromPointer(x, totalWidth, tabWidth, padHorizontalPx, isLtr, maxOverhangPx)
        dampedAnimation.updateValue(target)

        val currentSlot = computeSlotFromPointer(x, totalWidth, tabWidth, padHorizontalPx, isLtr)
        if (currentSlot != lastHoveredSlot) {
            val prevSlot = lastHoveredSlot
            lastHoveredSlot = currentSlot
            events.onTabCrossed?.invoke(prevSlot, currentSlot)
            events.onSlotHovered?.invoke(currentSlot)
        }

        val maxTab = (tabsCount - 1).toFloat()
        val rawTarget = computeRawTarget(x, totalWidth, tabWidth, padHorizontalPx, isLtr)
        val maxOverhangSlots = (maxOverhangPx / tabWidth).coerceAtLeast(0.001f)

        if (rawTarget < 0f) {
            val edge = LiquidBoundaryEdge.Start
            if (!startBoundaryHit) {
                startBoundaryHit = true
                events.onBoundaryReached?.invoke(edge)
            }
            val excess = -rawTarget
            val pullRatio = tanh(excess / (maxOverhangSlots * physics.overhangResistance)).coerceIn(0f, 1f)
            events.onBoundaryPull?.invoke(edge, pullRatio)
            if (pullRatio >= 0.95f && !startMaxOverhangHit) {
                startMaxOverhangHit = true
                events.onMaxOverhangReached?.invoke(edge)
            }
        } else {
            startBoundaryHit = false
            startMaxOverhangHit = false
        }

        if (rawTarget > maxTab) {
            val edge = LiquidBoundaryEdge.End
            if (!endBoundaryHit) {
                endBoundaryHit = true
                events.onBoundaryReached?.invoke(edge)
            }
            val excess = rawTarget - maxTab
            val pullRatio = tanh(excess / (maxOverhangSlots * physics.overhangResistance)).coerceIn(0f, 1f)
            events.onBoundaryPull?.invoke(edge, pullRatio)
            if (pullRatio >= 0.95f && !endMaxOverhangHit) {
                endMaxOverhangHit = true
                events.onMaxOverhangReached?.invoke(edge)
            }
        } else {
            endBoundaryHit = false
            endMaxOverhangHit = false
        }
    }

    /**
     * 响应手指/光标松开，决定最终选中的槽位并平滑回弹
     */
    fun onPointerUp(
        lastX: Float,
        totalWidth: Float,
        tabWidth: Float,
        padHorizontalPx: Float,
        isLtr: Boolean,
        onTabSelected: (Int) -> Unit
    ) {
        val finalIndex = computeSlotFromPointer(lastX, totalWidth, tabWidth, padHorizontalPx, isLtr)
        currentIndex = finalIndex
        startBoundaryHit = false
        endBoundaryHit = false
        startMaxOverhangHit = false
        endMaxOverhangHit = false

        events.onDropletDeactivated?.invoke(finalIndex)
        events.onDockingStarted?.invoke(finalIndex)

        dampedAnimation.animateToValue(finalIndex.toFloat())
        onTabSelected(finalIndex)
    }

    /**
     * 编程式跳转到指定 Tab
     */
    fun animateToTab(index: Int) {
        val target = index.coerceIn(0, tabsCount - 1)
        currentIndex = target
        events.onDockingStarted?.invoke(target)
        dampedAnimation.animateToValue(target.toFloat())
    }
}

/**
 * 记住 LiquidTabBarState
 */
@Composable
fun rememberLiquidTabBarState(
    selectedTabIndex: () -> Int,
    tabsCount: Int,
    physics: LiquidTabBarPhysics = LiquidTabBarDefaults.physics(),
    events: LiquidTabBarEvents = LiquidTabBarDefaults.events()
): LiquidTabBarState {
    val animationScope = rememberCoroutineScope()
    val initial = remember { selectedTabIndex().coerceIn(0, (tabsCount - 1).coerceAtLeast(0)) }
    val state = remember(animationScope, tabsCount) {
        LiquidTabBarState(
            animationScope = animationScope,
            initialIndex = initial,
            tabsCount = tabsCount,
            initialPhysics = physics,
            initialEvents = events
        )
    }

    // 动态同步物理参数与生命周期事件，避免状态机被销毁重建
    SideEffect {
        state.updatePhysics(physics)
        state.updateEvents(events)
    }

    LaunchedEffect(state, selectedTabIndex) {
        snapshotFlow { selectedTabIndex() }
            .collectLatest { targetIndex ->
                if (targetIndex != state.currentIndex) {
                    state.animateToTab(targetIndex)
                }
            }
    }

    return state
}
