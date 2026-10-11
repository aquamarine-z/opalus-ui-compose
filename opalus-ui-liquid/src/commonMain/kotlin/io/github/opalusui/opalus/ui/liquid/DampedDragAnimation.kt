package io.github.opalusui.opalus.ui.liquid

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.time.TimeSource

/**
 * 水滴交互状态机枚举
 */
enum class LiquidInteractionState {
    IDLE,       // 静止未激活态 (progress = 0)
    PRESSING,   // 手指按住蓄力中（水滴弹性长大）
    DRAGGING,   // 手指拖拽跟随中（水滴保持激活态，1:1 跟手）
    SNAPPING    // 吸附移动飞行中（半激活透镜态，随剩余吸附距离平滑收拢归零）
}

class DampedDragAnimation(
    private val animationScope: CoroutineScope,
    val initialValue: Float,
    val valueRange: ClosedRange<Float>,
    val visibilityThreshold: Float,
    val initialScale: Float,
    val pressedScale: Float,
    val flightProgress: Float = 0.65f,
    val dockingThreshold: Float = 0.9f,
    val onDragStarted: DampedDragAnimation.(position: Offset) -> Unit = {},
    val onDragStopped: DampedDragAnimation.() -> Unit = {},
    val onDrag: DampedDragAnimation.(size: IntSize, dragAmount: Offset) -> Unit = { _, _ -> },
) {

    // 物理弹簧参数：阻尼比与刚度
    private val valueAnimationSpec =
        spring(1f, 800f, visibilityThreshold)
    private val scaleAnimationSpec =
        spring(0.7f, 300f, 0.001f)

    // 水滴长大的弹性物理曲线：damping 0.72f 赋予水滴如胶质般饱满的膨胀张力，stiffness 320f
    private val growProgressAnimationSpec =
        spring(0.72f, 320f, 0.001f)

    // 原地松手时的回弹收缩物理曲线
    private val releaseProgressAnimationSpec =
        spring(0.85f, 400f, 0.001f)

    private val valueAnimatable = Animatable(initialValue, visibilityThreshold)
    private val pressProgressAnimatable = Animatable(0f, 0.001f)
    private val scaleXAnimatable = Animatable(initialScale, 0.001f)
    private val scaleYAnimatable = Animatable(initialScale, 0.001f)

    private var pressJob: Job? = null
    private var snapJob: Job? = null
    private var animationToken: Long = 0L

    private val timeMark = TimeSource.Monotonic.markNow()
    private var lastMoveTimeNanos: Long = 0L
    private var lastMoveValue: Float = initialValue
    private var downTarget: Float = initialValue
    private var isDragging: Boolean = false
    private var currentFlightTarget: Float? = null

    // 交互状态与实时物理属性
    var interactionState by mutableStateOf(LiquidInteractionState.IDLE)
        private set

    var value by mutableFloatStateOf(initialValue)
        private set

    var targetValue by mutableFloatStateOf(initialValue)
        private set

    var pressProgress by mutableFloatStateOf(0f)
        private set

    var scaleX by mutableFloatStateOf(initialScale)
        private set

    var scaleY by mutableFloatStateOf(initialScale)
        private set

    var velocity by mutableFloatStateOf(0f)
        private set

    val progress: Float
        get() = (value - valueRange.start) / (valueRange.endInclusive - valueRange.start).coerceAtLeast(0.001f)

    val modifier: Modifier = Modifier

    /**
     * 手指/光标按下响应
     * @param target 当前光标对应的连续浮点位置
     * @param targetSlot 当前光标对应的离散 Tab 槽位整数
     */
    fun onPointerDown(target: Float, targetSlot: Float) {
        val coercedTarget = target.coerceIn(valueRange)
        val coercedSlot = targetSlot.coerceIn(valueRange)
        downTarget = coercedTarget
        isDragging = false
        lastMoveTimeNanos = timeMark.elapsedNow().inWholeNanoseconds
        lastMoveValue = value
        velocity = 0f

        val dist = abs(value - coercedSlot)
        if (dist >= 0.5f) {
            // 远距离点击不同 Tab：立即启动平滑飞行动画，水滴无需等待变大，直接以半激活透镜态起飞
            currentFlightTarget = coercedSlot
            targetValue = coercedSlot
            animateToValueInternal(coercedSlot, fromDistantTap = true)
        } else {
            // 原地或极近距离按下：水滴进入蓄力状态，有弹性地膨胀长大
            currentFlightTarget = null
            targetValue = coercedSlot
            interactionState = LiquidInteractionState.PRESSING
            snapJob?.cancel()
            pressJob?.cancel()
            val token = ++animationToken
            pressJob = animationScope.launch {
                launch {
                    pressProgressAnimatable.snapTo(pressProgress)
                    pressProgressAnimatable.animateTo(1f, growProgressAnimationSpec) {
                        if (token == animationToken) {
                            pressProgress = this.value
                        }
                    }
                }
                launch {
                    scaleXAnimatable.snapTo(scaleX)
                    scaleXAnimatable.animateTo(pressedScale, scaleAnimationSpec) {
                        if (token == animationToken) {
                            scaleX = this.value
                        }
                    }
                }
                launch {
                    scaleYAnimatable.snapTo(scaleY)
                    scaleYAnimatable.animateTo(pressedScale, scaleAnimationSpec) {
                        if (token == animationToken) {
                            scaleY = this.value
                        }
                    }
                }
            }
        }
    }

    /**
     * 手指/光标移动响应：零延迟 1:1 跟手
     * @param target 连续浮点目标位置
     */
    fun onPointerMove(target: Float) {
        val coercedTarget = target.coerceIn(valueRange)
        val moveDelta = abs(coercedTarget - downTarget)

        // 如果用户有实际位移（超过轻微防抖阈值 0.02），判定为拖拽跟手
        if (isDragging || moveDelta > 0.02f) {
            if (!isDragging) {
                isDragging = true
                snapJob?.cancel()
                pressJob?.cancel()
                val token = ++animationToken
                currentFlightTarget = null
                // 拖拽开始时，确保水滴具有激活态尺寸与透镜效果
                pressProgress = pressProgress.coerceAtLeast(flightProgress)
                animationScope.launch {
                    scaleXAnimatable.animateTo(pressedScale, scaleAnimationSpec) {
                        if (token == animationToken) scaleX = this.value
                    }
                }
                animationScope.launch {
                    scaleYAnimatable.animateTo(pressedScale, scaleAnimationSpec) {
                        if (token == animationToken) scaleY = this.value
                    }
                }
            }

            interactionState = LiquidInteractionState.DRAGGING
            targetValue = coercedTarget
            // 核心跟手性：无协程竞争，直接同步赋予连续浮点坐标，保证极致跟手与丝滑
            value = coercedTarget

            // 同步轻量级速度计算（零协程分配）
            val nowNanos = timeMark.elapsedNow().inWholeNanoseconds
            val dt = (nowNanos - lastMoveTimeNanos) / 1_000_000_000f
            if (dt > 0.001f) {
                val rangeSpan = (valueRange.endInclusive - valueRange.start).coerceAtLeast(0.001f)
                val rawVel = (value - lastMoveValue) / dt
                velocity = (rawVel / rangeSpan).coerceIn(-1.5f, 1.5f)
                lastMoveTimeNanos = nowNanos
                lastMoveValue = value
            }
        }
    }

    /**
     * 手指/光标松开响应：平滑吸附到指定槽位，根据剩余吸附距离平滑收敛归零
     * @param targetSlot 最终停靠的槽位
     */
    fun onPointerUp(targetSlot: Float) {
        val coercedSlot = targetSlot.coerceIn(valueRange)
        val wasDragging = isDragging
        isDragging = false
        targetValue = coercedSlot

        if (wasDragging) {
            // 从拖拽状态释放：平滑吸附到目标槽位
            animateToValueInternal(coercedSlot, fromDistantTap = false)
        } else {
            // 如果已经在飞行中，且目标与 targetSlot 一致，继续保持平滑飞行即可
            if (interactionState == LiquidInteractionState.SNAPPING && currentFlightTarget == coercedSlot) {
                return
            }
            val dist = abs(value - coercedSlot)
            if (dist < 0.2f) {
                // 原地松手：平滑弹性收缩归零
                snapJob?.cancel()
                pressJob?.cancel()
                val token = ++animationToken
                snapJob = animationScope.launch {
                    interactionState = LiquidInteractionState.SNAPPING
                    try {
                        launch {
                            scaleXAnimatable.animateTo(initialScale, scaleAnimationSpec) {
                                if (token == animationToken) scaleX = this.value
                            }
                        }
                        launch {
                            scaleYAnimatable.animateTo(initialScale, scaleAnimationSpec) {
                                if (token == animationToken) scaleY = this.value
                            }
                        }
                        launch {
                            pressProgressAnimatable.snapTo(pressProgress)
                            pressProgressAnimatable.animateTo(0f, releaseProgressAnimationSpec) {
                                if (token == animationToken) pressProgress = this.value
                            }
                        }
                        valueAnimatable.snapTo(value)
                        valueAnimatable.animateTo(coercedSlot, valueAnimationSpec) {
                            if (token == animationToken) this@DampedDragAnimation.value = this.value
                        }
                    } finally {
                        if (token == animationToken) {
                            this@DampedDragAnimation.value = coercedSlot
                            valueAnimatable.snapTo(coercedSlot)
                            this@DampedDragAnimation.pressProgress = 0f
                            pressProgressAnimatable.snapTo(0f)
                            this@DampedDragAnimation.scaleX = initialScale
                            scaleXAnimatable.snapTo(initialScale)
                            this@DampedDragAnimation.scaleY = initialScale
                            scaleYAnimatable.snapTo(initialScale)
                            this@DampedDragAnimation.velocity = 0f
                            currentFlightTarget = null
                            interactionState = LiquidInteractionState.IDLE
                        }
                    }
                }
            } else {
                animateToValueInternal(coercedSlot, fromDistantTap = true)
            }
        }
    }

    /**
     * 吸附并平滑移动到目标槽位
     */
    private fun animateToValueInternal(coercedSlot: Float, fromDistantTap: Boolean) {
        snapJob?.cancel()
        pressJob?.cancel()
        val token = ++animationToken

        val startValue = value
        val initialDist = abs(startValue - coercedSlot)

        snapJob = animationScope.launch {
            interactionState = LiquidInteractionState.SNAPPING
            try {
                launch {
                    scaleXAnimatable.animateTo(initialScale, scaleAnimationSpec) {
                        if (token == animationToken) this@DampedDragAnimation.scaleX = this.value
                    }
                }
                launch {
                    scaleYAnimatable.animateTo(initialScale, scaleAnimationSpec) {
                        if (token == animationToken) this@DampedDragAnimation.scaleY = this.value
                    }
                }

                if (initialDist < 0.2f) {
                    // 原地或极微距离吸附
                    launch {
                        pressProgressAnimatable.snapTo(pressProgress)
                        pressProgressAnimatable.animateTo(0f, releaseProgressAnimationSpec) {
                            if (token == animationToken) this@DampedDragAnimation.pressProgress = this.value
                        }
                    }
                    valueAnimatable.snapTo(value)
                    valueAnimatable.animateTo(coercedSlot, valueAnimationSpec) {
                        if (token == animationToken) this@DampedDragAnimation.value = this.value
                    }
                } else {
                    // 跨距离平滑移动：保持至少 flightProgress 的透镜半激活态
                    val currentFlightProgress = pressProgress.coerceAtLeast(flightProgress)
                    this@DampedDragAnimation.pressProgress = currentFlightProgress

                    valueAnimatable.snapTo(value)
                    val initialVel = if (fromDistantTap) 0f else (velocity * 3f).coerceIn(-10f, 10f)
                    valueAnimatable.animateTo(
                        targetValue = coercedSlot,
                        animationSpec = valueAnimationSpec,
                        initialVelocity = initialVel
                    ) {
                        if (token == animationToken) {
                            this@DampedDragAnimation.value = this.value
                            this@DampedDragAnimation.velocity = (this.velocity / 10f).coerceIn(-1.5f, 1.5f)
                            val remainingDist = abs(this.value - coercedSlot)
                            val distFactor = if (remainingDist >= dockingThreshold) {
                                1f
                            } else {
                                val t = (remainingDist / dockingThreshold).coerceIn(0f, 1f)
                                t * t * (3f - 2f * t) // smoothstep 平滑进入目标槽位
                            }
                            this@DampedDragAnimation.pressProgress = (currentFlightProgress * distFactor).coerceIn(0f, 1f)
                        }
                    }
                }
            } finally {
                // 核心状态锁相：无论正常到达还是意外取消，水滴必定严格归一化为完全未激活态
                if (token == animationToken) {
                    this@DampedDragAnimation.value = coercedSlot
                    valueAnimatable.snapTo(coercedSlot)
                    this@DampedDragAnimation.pressProgress = 0f
                    pressProgressAnimatable.snapTo(0f)
                    this@DampedDragAnimation.scaleX = initialScale
                    scaleXAnimatable.snapTo(initialScale)
                    this@DampedDragAnimation.scaleY = initialScale
                    scaleYAnimatable.snapTo(initialScale)
                    this@DampedDragAnimation.velocity = 0f
                    currentFlightTarget = null
                    interactionState = LiquidInteractionState.IDLE
                }
            }
        }
    }

    /**
     * 编程式吸附跳转到目标值
     */
    fun animateToValue(value: Float) {
        val coerced = value.coerceIn(valueRange)
        targetValue = coerced
        animateToValueInternal(coerced, fromDistantTap = true)
    }

    fun press() {
        onPointerDown(value, value)
    }

    fun release() {
        onPointerUp(targetValue)
    }

    fun updateValue(value: Float) {
        onPointerMove(value)
    }
}

