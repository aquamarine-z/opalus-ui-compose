package io.github.opalusui.opalus.ui.liquid

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.spring
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.util.VelocityTracker
import androidx.compose.ui.unit.IntSize
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
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

    private var moveJob: Job? = null
    private var snapJob: Job? = null
    private var stateJob: Job? = null

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

    fun press() {
        velocityTracker.resetTracking()
        stateJob?.cancel()
        stateJob = animationScope.launch {
            launch { pressProgressAnimation.animateTo(1f, pressProgressAnimationSpec) }
            launch { scaleXAnimation.animateTo(pressedScale, scaleXAnimationSpec) }
            launch { scaleYAnimation.animateTo(pressedScale, scaleYAnimationSpec) }
        }
    }

    fun release() {
        stateJob?.cancel()
        stateJob = animationScope.launch {
            launch { pressProgressAnimation.animateTo(0f, pressProgressAnimationSpec) }
            launch { scaleXAnimation.animateTo(initialScale, scaleXAnimationSpec) }
            launch { scaleYAnimation.animateTo(initialScale, scaleYAnimationSpec) }
            launch { velocityAnimation.animateTo(0f, velocityAnimationSpec) }
        }
    }

    fun updateValue(value: Float) {
        val target = value.coerceIn(valueRange)
        moveJob?.cancel()
        moveJob = animationScope.launch {
            valueAnimation.animateTo(target, valueAnimationSpec) {
                updateVelocity()
            }
        }
    }

    fun animateToValue(value: Float) {
        moveJob?.cancel()
        snapJob?.cancel()
        val target = value.coerceIn(valueRange)
        snapJob = animationScope.launch {
            release()
            try {
                valueAnimation.animateTo(target, valueAnimationSpec)
            } finally {
                velocityAnimation.snapTo(0f)
            }
        }
    }

    private fun updateVelocity() {
        velocityTracker.addPosition(
            timeMark.elapsedNow().inWholeMilliseconds,
            Offset(value, 0f)
        )
        val rangeSpan = (valueRange.endInclusive - valueRange.start).coerceAtLeast(0.001f)
        val rawVel = velocityTracker.calculateVelocity().x
        if (rawVel.isFinite()) {
            val targetVelocity = (rawVel / rangeSpan).coerceIn(-1.5f, 1.5f)
            animationScope.launch { velocityAnimation.animateTo(targetVelocity, velocityAnimationSpec) }
        }
    }
}
