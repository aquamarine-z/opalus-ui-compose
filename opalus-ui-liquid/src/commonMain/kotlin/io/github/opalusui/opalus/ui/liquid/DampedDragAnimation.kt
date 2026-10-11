package io.github.opalusui.opalus.ui.liquid

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.foundation.MutatorMutex
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
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
    val onDragStarted: DampedDragAnimation.(position: Offset) -> Unit,
    val onDragStopped: DampedDragAnimation.() -> Unit,
    val onDrag: DampedDragAnimation.(size: IntSize, dragAmount: Offset) -> Unit,
) {

    private val valueAnimationSpec =
        spring(1f, 1000f, visibilityThreshold)
    private val velocityAnimationSpec =
        spring(0.5f, 300f, visibilityThreshold * 10f)
    private val pressProgressAnimationSpec =
        spring(1f, 1000f, 0.001f)
    private val scaleXAnimationSpec =
        spring(0.6f, 250f, 0.001f)
    private val scaleYAnimationSpec =
        spring(0.7f, 250f, 0.001f)

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
                pressProgressAnimation.animateTo(1f, pressProgressAnimationSpec)
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

    fun updateValue(value: Float) {
        val targetValue = value.coerceIn(valueRange)
        animationScope.launch {
            valueAnimation.animateTo(targetValue, valueAnimationSpec) {
                updateVelocity()
            }
        }
    }

    fun animateToValue(value: Float) {
        val target = value.coerceIn(valueRange)
        pressJob?.cancel()
        snapJob?.cancel()
        val token = ++animationToken

        snapJob = animationScope.launch {
            try {
                launch { scaleXAnimation.animateTo(initialScale, scaleXAnimationSpec) }
                launch { scaleYAnimation.animateTo(initialScale, scaleYAnimationSpec) }
                launch { velocityAnimation.animateTo(0f, velocityAnimationSpec) }

                launch {
                    val initialDist = abs(valueAnimation.value - target)
                    if (initialDist > 0.25f) {
                        val threshold = (valueRange.endInclusive - valueRange.start) * 0.15f
                        withTimeoutOrNull(600L) {
                            snapshotFlow { valueAnimation.value }
                                .filter { abs(it - target) < threshold }
                                .first()
                        }
                    }
                    pressProgressAnimation.animateTo(0f, pressProgressAnimationSpec)
                }

                valueAnimation.animateTo(target, valueAnimationSpec) {
                    updateVelocity()
                }
            } finally {
                // 核心保障：手指松开吸附 item 的时候必须 100% 还原到最开始 inactive 的状态
                if (token == animationToken) {
                    valueAnimation.snapTo(target)
                    pressProgressAnimation.snapTo(0f)
                    scaleXAnimation.snapTo(initialScale)
                    scaleYAnimation.snapTo(initialScale)
                    velocityAnimation.snapTo(0f)
                }
            }
        }
    }

    private fun updateVelocity() {
        velocityTracker.addPosition(
            timeMark.elapsedNow().inWholeMilliseconds,
            Offset(value, 0f)
        )
        val rangeSpan = (valueRange.endInclusive - valueRange.start).coerceAtLeast(0.001f)
        val targetVelocity = velocityTracker.calculateVelocity().x / rangeSpan
        if (targetVelocity.isFinite()) {
            animationScope.launch { velocityAnimation.animateTo(targetVelocity, velocityAnimationSpec) }
        }
    }
}
