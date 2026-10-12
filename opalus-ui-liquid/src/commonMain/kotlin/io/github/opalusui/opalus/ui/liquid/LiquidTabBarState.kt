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
    val physics: LiquidTabBarPhysics = LiquidTabBarDefaults.physics()
) {
    var currentIndex by mutableIntStateOf(initialIndex)
        private set

    val dampedAnimation = DampedDragAnimation(
        animationScope = animationScope,
        initialValue = initialIndex.toFloat(),
        valueRange = (-physics.maxOverhang)..(tabsCount - 1 + physics.maxOverhang),
        visibilityThreshold = physics.dragVisibilityThreshold,
        initialScale = 1f,
        pressedScale = physics.platterActiveScale,
        physics = physics,
        onDragStarted = {},
        onDragStopped = {},
        onDrag = { _, _ -> }
    )

    val animatedValue: Float
        get() = dampedAnimation.value

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
     * 将光标屏幕横坐标转换为带物理阻尼的水滴目标插值位置
     */
    fun computeTargetFromPointer(
        x: Float,
        totalWidth: Float,
        tabWidth: Float,
        padHorizontalPx: Float,
        isLtr: Boolean
    ): Float {
        val maxTab = (tabsCount - 1).toFloat()
        val rawTarget = if (isLtr) {
            (x - padHorizontalPx) / tabWidth - 0.5f
        } else {
            (totalWidth - padHorizontalPx - x) / tabWidth - 0.5f
        }

        return when {
            rawTarget < 0f -> {
                val excess = -rawTarget
                -physics.maxOverhang * tanh(excess / (physics.maxOverhang * physics.overhangResistance))
            }
            rawTarget > maxTab -> {
                val excess = rawTarget - maxTab
                maxTab + physics.maxOverhang * tanh(excess / (physics.maxOverhang * physics.overhangResistance))
            }
            else -> rawTarget
        }
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
        isLtr: Boolean
    ) {
        dampedAnimation.press()
        val target = computeTargetFromPointer(x, totalWidth, tabWidth, padHorizontalPx, isLtr)
        dampedAnimation.updateValue(target)
    }

    /**
     * 响应手指/光标移动
     */
    fun onPointerMove(
        x: Float,
        totalWidth: Float,
        tabWidth: Float,
        padHorizontalPx: Float,
        isLtr: Boolean
    ) {
        val target = computeTargetFromPointer(x, totalWidth, tabWidth, padHorizontalPx, isLtr)
        dampedAnimation.updateValue(target)
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
        dampedAnimation.animateToValue(finalIndex.toFloat())
        onTabSelected(finalIndex)
    }

    /**
     * 编程式跳转到指定 Tab
     */
    fun animateToTab(index: Int) {
        val target = index.coerceIn(0, tabsCount - 1)
        currentIndex = target
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
    physics: LiquidTabBarPhysics = LiquidTabBarDefaults.physics()
): LiquidTabBarState {
    val animationScope = rememberCoroutineScope()
    val initial = remember { selectedTabIndex().coerceIn(0, (tabsCount - 1).coerceAtLeast(0)) }
    val state = remember(animationScope, tabsCount, physics) {
        LiquidTabBarState(
            animationScope = animationScope,
            initialIndex = initial,
            tabsCount = tabsCount,
            physics = physics
        )
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
