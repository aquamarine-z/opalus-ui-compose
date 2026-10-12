package io.github.opalusui.opalus.ui.liquid

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.MutatorMutex
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.math.abs
import kotlin.time.TimeSource

class DampedDragAnimation(
    private val animationScope: CoroutineScope,
    val initialValue: Float,
    val valueRange: ClosedRange<Float>,
    val visibilityThreshold: Float,
    val initialScale: Float,
    val pressedScale: Float,
    initialPhysics: LiquidTabBarPhysics = LiquidTabBarDefaults.physics(),
    val onDragStarted: DampedDragAnimation.(position: Offset) -> Unit = {},
    val onDragStopped: DampedDragAnimation.() -> Unit = {},
    val onDrag: DampedDragAnimation.(size: IntSize, dragAmount: Offset) -> Unit = { _, _ -> },
) {
    var physics: LiquidTabBarPhysics by mutableStateOf(initialPhysics)

    private val dragAnimationSpec
        get() = spring(physics.dragDampingRatio, physics.dragStiffness, visibilityThreshold)
    private val dockingAnimationSpec
        get() = spring(physics.dockingDampingRatio, physics.dockingStiffness, visibilityThreshold)
    private val velocityAnimationSpec =
        spring(0.65f, 380f, visibilityThreshold * 10f)
    private val growProgressAnimationSpec
        get() = spring(physics.growDampingRatio, physics.growStiffness, 0.001f)
    private val shrinkProgressAnimationSpec
        get() = spring(physics.shrinkDampingRatio, physics.shrinkStiffness, 0.001f)
    private val scaleXAnimationSpec =
        spring(0.65f, 280f, 0.001f)
    private val scaleYAnimationSpec =
        spring(0.70f, 280f, 0.001f)

    private val valueAnimation =
        Animatable(initialValue, visibilityThreshold)
    private val velocityAnimation =
        Animatable(0f, 5f)
    private val pressProgressAnimation =
        Animatable(0f, 0.001f)
    private val scaleXAnimation =
        Animatable(initialScale, 0.001f)
    private val scaleYAnimation =
        Animatable(initialScale, 0.001f)
    private val jellyAnimation =
        Animatable(0f, 0.001f)

    private val mutatorMutex = MutatorMutex()

    private val velocityTracker = VelocityTracker()
    private val timeMark = TimeSource.Monotonic.markNow()

    val value: Float get() = valueAnimation.value
    val progress: Float get() = (value - valueRange.start) / (valueRange.endInclusive - valueRange.start)
    val targetValue: Float get() = valueAnimation.targetValue
    val pressProgress: Float get() = pressProgressAnimation.value
    val scaleX: Float get() = scaleXAnimation.value
    val scaleY: Float get() = scaleYAnimation.value
    val velocity: Float get() = velocityAnimation.value
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
    private var animationToken: Long = 0L

    fun press() {
        velocityTracker.resetTracking()
        snapJob?.cancel()
        pressJob?.cancel()
        val token = ++animationToken
        pressJob = animationScope.launch {
            launch {
                jellyAnimation.snapTo(-0.30f)
                jellyAnimation.animateTo(
                    targetValue = 0f,
                    animationSpec = spring(
                        dampingRatio = physics.growDampingRatio,
                        stiffness = physics.growStiffness * 0.8f,
                        visibilityThreshold = 0.001f
                    )
                )
            }
            launch {
                pressProgressAnimation.animateTo(1f, growProgressAnimationSpec)
            }
            launch {
                scaleXAnimation.animateTo(pressedScale, scaleXAnimationSpec)
            }
            launch {
                scaleYAnimation.animateTo(pressedScale, scaleYAnimationSpec)
            }
        }
    }

    fun release() {
        animateToValue(valueAnimation.targetValue)
    }

    private var dragJob: Job? = null

    fun updateValue(value: Float) {
        val targetValue = value.coerceIn(valueRange)
        dragJob?.cancel()
        dragJob = animationScope.launch {
            val currentVel = valueAnimation.velocity
            valueAnimation.animateTo(
                targetValue = targetValue,
                animationSpec = dragAnimationSpec,
                initialVelocity = currentVel
            ) {
                updateVelocity()
            }
        }
    }

    fun animateToValue(value: Float) {
        val target = value.coerceIn(valueRange)
        dragJob?.cancel()
        pressJob?.cancel()
        snapJob?.cancel()
        val token = ++animationToken

        snapJob = animationScope.launch {
            try {
                coroutineScope {
                    launch { scaleXAnimation.animateTo(initialScale, scaleXAnimationSpec) }
                    launch { scaleYAnimation.animateTo(initialScale, scaleYAnimationSpec) }
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
                        launch {
                            // 水珠缩小触底果冻回弹：进入吸附收拢区间时，瞬时注入果冻形变脉冲 (1.0f)
                            // 欠阻尼弹簧驱动横向与纵向交替回弹振荡，在缩小与完全进入 inactive 时呈现水滴/果冻弹性质感
                            jellyAnimation.snapTo(1f)
                            jellyAnimation.animateTo(
                                targetValue = 0f,
                                animationSpec = spring(
                                    dampingRatio = (physics.shrinkDampingRatio * 0.78f).coerceIn(0.40f, 0.75f),
                                    stiffness = (physics.shrinkStiffness * 0.75f).coerceIn(160f, 600f),
                                    visibilityThreshold = 0.001f
                                )
                            )
                        }
                        pressProgressAnimation.animateTo(0f, shrinkProgressAnimationSpec)
                    }

                    launch {
                        val currentVel = valueAnimation.velocity
                        val rangeSpan = (valueRange.endInclusive - valueRange.start).coerceAtLeast(0.001f)
                        val trackerVel = velocityTracker.calculateVelocity().x / rangeSpan
                        val initialVel = if (trackerVel.isFinite() && abs(trackerVel) > 0.01f) {
                            trackerVel * physics.dockingInertiaFactor
                        } else {
                            currentVel * physics.dockingInertiaFactor
                        }

                        valueAnimation.animateTo(
                            targetValue = target,
                            animationSpec = dockingAnimationSpec,
                            initialVelocity = initialVel
                        )
                    }
                }
            } finally {
                // 核心保障：手指松开吸附 item 的时候必须 100% 还原到最开始 inactive 的状态
                if (token == animationToken) {
                    valueAnimation.snapTo(target)
                    pressProgressAnimation.snapTo(0f)
                    scaleXAnimation.snapTo(initialScale)
                    scaleYAnimation.snapTo(initialScale)
                    velocityAnimation.snapTo(0f)
                    jellyAnimation.snapTo(0f)
                }
            }
        }
    }

    private var velocityJob: Job? = null

    private fun updateVelocity() {
        velocityTracker.addPosition(
            timeMark.elapsedNow().inWholeMilliseconds,
            Offset(value, 0f)
        )
        val rangeSpan = (valueRange.endInclusive - valueRange.start).coerceAtLeast(0.001f)
        val targetVelocity = velocityTracker.calculateVelocity().x / rangeSpan
        if (targetVelocity.isFinite()) {
            velocityJob?.cancel()
            velocityJob = animationScope.launch {
                velocityAnimation.animateTo(targetVelocity, velocityAnimationSpec)
            }
        }
    }
}
