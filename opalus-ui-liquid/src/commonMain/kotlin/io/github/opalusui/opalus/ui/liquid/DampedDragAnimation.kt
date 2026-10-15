package io.github.opalusui.opalus.ui.liquid

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.SpringSpec
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.MutatorMutex
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.math.abs
import kotlin.math.roundToInt

class DampedDragAnimation(
    private val animationScope: CoroutineScope,
    val initialValue: Float,
    initialValueRange: ClosedRange<Float>,
    val visibilityThreshold: Float,
    val initialScale: Float,
    val pressedScale: Float = 1.03f,
    val pressedScaleX: Float = pressedScale,
    val pressedScaleY: Float = pressedScale,
    val tabsCount: Int = 1,
    initialPhysics: LiquidTabBarPhysics = LiquidTabBarDefaults.physics(),
    val onDragStarted: DampedDragAnimation.(position: Offset) -> Unit = {},
    val onDragStopped: DampedDragAnimation.() -> Unit = {},
    val onDrag: DampedDragAnimation.(size: IntSize, dragAmount: Offset) -> Unit = { _, _ -> },
    val onAnimationSettled: (Int) -> Unit = {}
) {
    var valueRange: ClosedRange<Float> by mutableStateOf(initialValueRange)
    var physics: LiquidTabBarPhysics by mutableStateOf(initialPhysics)

    private val dragAnimationSpec
        get() = spring(physics.dragDampingRatio, physics.dragStiffness, visibilityThreshold)
    private val dockingAnimationSpec
        get() = spring(physics.dockingDampingRatio, physics.dockingStiffness, visibilityThreshold)
    private val velocityAnimationSpec: SpringSpec<Float>
        get() = spring(physics.dragDampingRatio, physics.dragStiffness * 1.5f, visibilityThreshold * 10f)
    private val growProgressAnimationSpec: SpringSpec<Float>
        get() = spring(physics.growDampingRatio, physics.growStiffness, 0.001f)
    private val shrinkProgressAnimationSpec: SpringSpec<Float>
        get() {
            val elasticity = physics.dropletElasticity.coerceIn(0f, 1f)
            val damping = physics.shrinkDampingRatio * (1.15f - 0.50f * elasticity)
            val stiffness = physics.shrinkStiffness * (1.50f - 0.70f * elasticity)
            return spring(
                dampingRatio = damping.coerceIn(0.35f, 1.0f),
                stiffness = stiffness.coerceIn(100f, 850f),
                visibilityThreshold = 0.001f
            )
        }
    private val scaleXGrowSpec: AnimationSpec<Float>
        get() = physics.platterActivationDurationMillis?.let { durationMillis ->
            tween(
                durationMillis = durationMillis.coerceAtLeast(1),
                easing = FastOutSlowInEasing
            )
        } ?: spring(physics.growDampingRatio, physics.growStiffness * 0.9f, 0.001f)
    private val scaleYGrowSpec: AnimationSpec<Float>
        get() = physics.platterActivationDurationMillis?.let { durationMillis ->
            tween(
                durationMillis = durationMillis.coerceAtLeast(1),
                easing = FastOutSlowInEasing
            )
        } ?: spring(physics.growDampingRatio, physics.growStiffness * 0.9f, 0.001f)
    private val scaleXShrinkSpec: SpringSpec<Float>
        get() = spring(physics.shrinkDampingRatio, physics.shrinkStiffness, 0.001f)
    private val scaleYShrinkSpec: SpringSpec<Float>
        get() = spring(physics.shrinkDampingRatio, physics.shrinkStiffness, 0.001f)

    private val valueAnimation =
        Animatable(initialValue, visibilityThreshold)
    private val velocityAnimation =
        Animatable(0f, 5f)
    private val pressProgressAnimation =
        Animatable(0f, 0.001f)
    private val shapeProgressAnimation =
        Animatable(0f, 0.001f)
    private val scaleXAnimation =
        Animatable(initialScale, 0.001f)
    private val scaleYAnimation =
        Animatable(initialScale, 0.001f)
    private val jellyAnimation =
        Animatable(0f, 0.001f)

    private val mutatorMutex = MutatorMutex()

    // Drag simulation is already integrated manually. Keeping its two values in
    // primitive snapshot states avoids invoking Animatable's mutator machinery
    // twice for every pointer frame on WASM.
    private var dragValue by mutableFloatStateOf(initialValue)
    private var dragVelocity by mutableFloatStateOf(0f)
    private var dragFrameActive by mutableStateOf(false)

    val value: Float get() = if (dragFrameActive) dragValue else valueAnimation.value
    val progress: Float get() = (value - valueRange.start) / (valueRange.endInclusive - valueRange.start)
    val targetValue: Float get() = if (dragFrameActive) dragTargetValue else valueAnimation.targetValue
    val pressProgress: Float get() = pressProgressAnimation.value
    val shapeProgress: Float get() = shapeProgressAnimation.value.coerceIn(0f, 1f)
    val scaleX: Float get() = scaleXAnimation.value
    val scaleY: Float get() = scaleYAnimation.value
    val velocity: Float get() = if (dragFrameActive) dragVelocity else velocityAnimation.value
    val jelly: Float get() = jellyAnimation.value

    val modifier: Modifier = Modifier.pointerInput(Unit) {
        inspectDragGestures(
            onDragStart = { down ->
                onDragStarted(down.position)
                press()
            },
            onDragEnd = {
                onDragStopped()
                release()
            },
            onDragCancel = {
                onDragStopped()
                release()
            }
        ) { change, dragAmount ->
            onDrag(size, dragAmount)
        }
    }

    private var pressJob: Job? = null
    private var snapJob: Job? = null
    private var prewarmJob: Job? = null
    private var animationToken: Long = 0L

    suspend fun prewarm() {
        if (isDragging) return
        coroutineScope {
            val job = launch {
                withFrameNanos { }
                // 1. 预热所有弹簧与补间动画向量化求值器及 JVM 类加载 (Zero GC, Zero JIT hitch on first touch)
                growProgressAnimationSpec.vectorize(Float.VectorConverter)
                shrinkProgressAnimationSpec.vectorize(Float.VectorConverter)
                scaleXGrowSpec.vectorize(Float.VectorConverter)
                scaleXShrinkSpec.vectorize(Float.VectorConverter)
                scaleYGrowSpec.vectorize(Float.VectorConverter)
                scaleYShrinkSpec.vectorize(Float.VectorConverter)
                dragAnimationSpec.vectorize(Float.VectorConverter)
                dockingAnimationSpec.vectorize(Float.VectorConverter)
                velocityAnimationSpec.vectorize(Float.VectorConverter)

                // 2. 调度微帧：将 Animatable 与 GraphicsLayer 缩放参数微调 0.001f 并等待 1 帧绘制，
                // 确保 Direct3D 11 / Skiko 完成非恒等缩放矩阵 (scale != 1.0f) 的 RenderTarget 及变换图层构建，
                // 随后立即微调回静止态并提交，保证首屏在用户交互前彻底完成所有 GPU 图层管线预热。
                pressProgressAnimation.snapTo(0.001f)
                shapeProgressAnimation.snapTo(0.001f)
                scaleXAnimation.snapTo(initialScale + 0.0001f)
                scaleYAnimation.snapTo(initialScale + 0.0001f)
                withFrameNanos { }

                pressProgressAnimation.snapTo(0f)
                shapeProgressAnimation.snapTo(0f)
                scaleXAnimation.snapTo(initialScale)
                scaleYAnimation.snapTo(initialScale)
                withFrameNanos { }
            }
            prewarmJob = job
            job.join()
        }
    }

    fun press() {
        prewarmJob?.cancel()
        snapJob?.cancel()
        pressJob?.cancel()
        dragJob?.cancel()
        isDragging = true
        currentSimVelocity = 0f
        dragValue = valueAnimation.value
        dragVelocity = 0f
        dragFrameActive = true
        val token = ++animationToken
        pressJob = animationScope.launch {
            launch {
                pressProgressAnimation.animateTo(1f, growProgressAnimationSpec)
            }
            launch {
                shapeProgressAnimation.animateTo(1f, growProgressAnimationSpec)
            }
            launch {
                scaleXAnimation.animateTo(physics.platterActiveScaleX, scaleXGrowSpec)
            }
            launch {
                scaleYAnimation.animateTo(physics.platterActiveScaleY, scaleYGrowSpec)
            }
        }
    }

    fun release() {
        isDragging = false
        animateToValue(dragTargetValue)
    }

    private var dragJob: Job? = null
    private var dragTargetValue: Float = initialValue
    private var isDragging: Boolean = false
    private var currentSimVelocity: Float = 0f

    private fun startDragSimulationIfNeeded() {
        if (dragJob != null && dragJob!!.isActive) return
        dragJob = animationScope.launch {
            var lastFrameTimeNanos = 0L
            var currentPos = dragValue

            simulation@ while (isActive && isDragging) {
                val nowNanos = withFrameNanos { it }
                if (lastFrameTimeNanos == 0L) {
                    lastFrameTimeNanos = nowNanos
                    continue
                }
                val dt = ((nowNanos - lastFrameTimeNanos) / 1_000_000_000.0).toFloat().coerceIn(0.001f, 0.05f)
                lastFrameTimeNanos = nowNanos

                // 4 次子迭代保证连续物理积分的稳定性
                val subSteps = 4
                val subDt = dt / subSteps
                val target = dragTargetValue
                val currentPhysics = physics
                val minBound = valueRange.start
                val maxBound = valueRange.endInclusive
                val maxTab = (tabsCount - 1).toFloat().coerceAtLeast(0f)
                val fixedBounceSpeed = currentPhysics.overhangBounceAmplitude *
                    currentPhysics.dropletElasticity.coerceIn(0.4f, 1.6f)

                for (i in 0 until subSteps) {
                    // 当处于边界伸缩区时，平滑过渡到回正刚度与阻尼，回正速度更柔和舒缓
                    val overhangDepth = when {
                        currentPos < 0f -> -currentPos
                        currentPos > maxTab -> currentPos - maxTab
                        else -> 0f
                    }
                    val blend = (overhangDepth * 2.5f).coerceIn(0f, 1f)
                    val k = (1f - blend) * currentPhysics.dragStiffness + blend * currentPhysics.overhangSettlingStiffness
                    val zeta = (1f - blend) * currentPhysics.dragDampingRatio + blend * currentPhysics.overhangSettlingDampingRatio
                    val c = 2f * zeta * kotlin.math.sqrt(k)

                    val fSpring = k * (target - currentPos)
                    val fDamping = -c * currentSimVelocity
                    val a = fSpring + fDamping
                    currentSimVelocity += a * subDt
                    currentPos += currentSimVelocity * subDt

                    // 严密物理边界碰撞约束：定值反弹冲量与固定缓冲限位（拉过之后轻微弹一下，回正更慢，且弹性幅度严格与手速解耦）
                    if (currentPos < minBound) {
                        currentPos = minBound
                        if (currentSimVelocity < -0.05f) {
                            // 撞击左侧最大物理缓冲限位：以定值冲量轻微反弹，杜绝随手势速度无限放大
                            currentSimVelocity = fixedBounceSpeed
                        } else {
                            currentSimVelocity = 0f
                        }
                    } else if (currentPos > maxBound) {
                        currentPos = maxBound
                        if (currentSimVelocity > 0.05f) {
                            // 撞击右侧最大物理缓冲限位：以定值冲量轻微反弹，杜绝随手势速度无限放大
                            currentSimVelocity = -fixedBounceSpeed
                        } else {
                            currentSimVelocity = 0f
                        }
                    }
                }

                currentPos = currentPos.coerceIn(minBound, maxBound)
                dragValue = currentPos
                dragVelocity = currentSimVelocity

                if (abs(currentPos - target) < 0.0005f && abs(currentSimVelocity) < 0.005f) {
                    currentPos = target
                    currentSimVelocity = 0f
                    valueAnimation.snapTo(target)
                    velocityAnimation.snapTo(0f)
                    dragValue = target
                    dragVelocity = 0f
                    dragFrameActive = false
                    // Stop scheduling frames while a stationary pointer keeps the
                    // same target. A later pointer move starts the simulation again.
                    isDragging = false
                    break@simulation
                }
            }
        }
    }

    fun updateValue(value: Float) {
        if (!dragFrameActive) {
            dragValue = valueAnimation.value
            dragVelocity = 0f
            dragFrameActive = true
        }
        dragTargetValue = value.coerceIn(valueRange)
        isDragging = true
        startDragSimulationIfNeeded()
    }

    fun animateToValue(value: Float) {
        val target = value.coerceIn(valueRange)
        isDragging = false
        dragJob?.cancel()
        pressJob?.cancel()
        snapJob?.cancel()
        val token = ++animationToken

        snapJob = animationScope.launch {
            try {
                if (dragFrameActive) {
                    // Transfer the current pointer position to the existing
                    // Animatable once, then keep the original docking springs.
                    valueAnimation.snapTo(dragValue)
                    velocityAnimation.snapTo(currentSimVelocity)
                    dragFrameActive = false
                }
                coroutineScope {
                    launch { scaleXAnimation.animateTo(initialScale, scaleXShrinkSpec) }
                    launch { scaleYAnimation.animateTo(initialScale, scaleYShrinkSpec) }
                    launch { velocityAnimation.animateTo(0f, velocityAnimationSpec) }

                    launch {
                        val initialDist = abs(valueAnimation.value - target)
                        if (initialDist > physics.dockingShrinkThreshold) {
                            withTimeoutOrNull(600L) {
                                snapshotFlow { valueAnimation.value }
                                    .filter { abs(it - target) < physics.dockingShrinkThreshold }
                                    .first()
                            }
                        }
                        // 启动收拢动画：严格从当前状态尺寸直接往回收缩，杜绝任何超出当前尺寸的向外扩张
                        pressProgressAnimation.animateTo(0f, shrinkProgressAnimationSpec)
                    }

                    launch {
                        val initialDist = abs(valueAnimation.value - target)
                        if (initialDist > physics.dockingShrinkThreshold) {
                            withTimeoutOrNull(600L) {
                                snapshotFlow { valueAnimation.value }
                                    .filter { abs(it - target) < physics.dockingShrinkThreshold }
                                    .first()
                            }
                        }
                        // 形状过渡：严格使用平滑单调动画，保证丝滑连续回到 inactive 形状，
                        // 杜绝因果冻弹簧在 0 附近振荡过冲而在 active 与 inactive 两种形状之间来回闪动
                        shapeProgressAnimation.animateTo(
                            targetValue = 0f,
                            animationSpec = tween(
                                durationMillis = 240,
                                easing = FastOutSlowInEasing
                            )
                        )
                    }

                    launch {
                        val currentPos = valueAnimation.value
                        val dist = target - currentPos
                        val dir = when {
                            abs(dist) > 0.02f -> kotlin.math.sign(dist)
                            abs(currentSimVelocity) > 0.02f -> kotlin.math.sign(currentSimVelocity)
                            else -> 0f
                        }
                        // 定值过冲偏转：无论飞行距离是 0.2 个 Tab 还是 4 个 Tab，过冲幅度严格为定值，杜绝随距离无限放大
                        val fixedOvershoot = dir * (0.12f * physics.dockingInertiaFactor)

                        if (abs(dir) > 0f && abs(fixedOvershoot) > 0.005f) {
                            val peakTarget = (target + fixedOvershoot).coerceIn(valueRange)
                            // 阶段 1：平滑飞向目标吸附点峰值（使用单调临界阻尼 1.0f，绝不产生与距离成正比的多余过冲）
                            valueAnimation.animateTo(
                                targetValue = peakTarget,
                                animationSpec = spring(
                                    dampingRatio = 1.0f,
                                    stiffness = physics.dockingStiffness.coerceAtLeast(360f)
                                )
                            )
                            // 阶段 2：以定值冲量从峰值回弹归正到目标槽位（回弹幅度严格为定值，与飞行距离和时长无关）
                            valueAnimation.animateTo(
                                targetValue = target,
                                animationSpec = spring(
                                    dampingRatio = physics.dockingDampingRatio,
                                    stiffness = physics.dockingStiffness
                                )
                            )
                        } else {
                            valueAnimation.animateTo(
                                targetValue = target,
                                animationSpec = spring(
                                    dampingRatio = physics.dockingDampingRatio,
                                    stiffness = physics.dockingStiffness
                                )
                            )
                        }
                    }
                }
            } finally {
                // 核心保障：手指松开吸附 item 的时候必须 100% 还原到最开始 inactive 的状态
                if (token == animationToken) {
                    val settledTarget = target.coerceIn(valueRange)
                    valueAnimation.snapTo(settledTarget)
                    pressProgressAnimation.snapTo(0f)
                    shapeProgressAnimation.snapTo(0f)
                    scaleXAnimation.snapTo(initialScale)
                    scaleYAnimation.snapTo(initialScale)
                    velocityAnimation.snapTo(0f)
                    jellyAnimation.snapTo(0f)
                    currentSimVelocity = 0f
                    onAnimationSettled(settledTarget.roundToInt())
                }
            }
        }
    }

}
