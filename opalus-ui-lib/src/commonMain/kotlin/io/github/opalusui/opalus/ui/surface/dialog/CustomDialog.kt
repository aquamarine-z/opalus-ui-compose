package io.github.opalusui.opalus.ui.surface.dialog

import androidx.compose.animation.*
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import io.github.opalusui.opalus.ui.surface.Surface
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

open class CustomDialogOptions(
    val fadeInDuration: Duration = 150.milliseconds,
    val fadeOutDuration: Duration = 150.milliseconds,
    val properties: DialogProperties = DialogProperties(
        dismissOnClickOutside = true,
        usePlatformDefaultWidth = true,
        dismissOnBackPress = true,
    ),
)

suspend fun <T> Dialogs.custom(
    options: CustomDialogOptions = CustomDialogOptions(), content: @Composable (close: (value: T?) -> Unit) -> Unit
): T? {
    val surface = Surface.create { state ->
        val visibleState = remember { MutableTransitionState(false) }
        var firstRun by remember { mutableStateOf(true) }
        
        var result: T? by remember { mutableStateOf(null) }
        val closeWithAnimation: (value: T?) -> Unit = { value ->
            result = value
            visibleState.targetState = false
        }
        LaunchedEffect(visibleState.currentState, visibleState.targetState, firstRun) {
            if (visibleState.isIdle && !visibleState.targetState) {
                state.close()
                state.resolve(result)
            }
            if (!visibleState.targetState && !visibleState.currentState && !firstRun) {
                state.close()
                state.resolve(result)
            }
        }
        Dialog(
            onDismissRequest = {
                if (options.properties.dismissOnClickOutside) {
                    closeWithAnimation(null)
                }
            }, properties = options.properties
        ) {
            key(state.id) {
                SideEffect {
                    if (firstRun) {
                        visibleState.targetState = true
                        firstRun = false
                    }
                }
                AnimatedVisibility(
                    visibleState = visibleState,
                    enter = fadeIn(tween(options.fadeInDuration.inWholeMilliseconds.toInt())) + scaleIn(
                        initialScale = 0.8f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                    ),
                    exit = fadeOut(tween(options.fadeOutDuration.inWholeMilliseconds.toInt())) + scaleOut(
                        targetScale = 0.9f, animationSpec = tween(options.fadeOutDuration.inWholeMilliseconds.toInt())
                    )
                ) {
                    content(closeWithAnimation)
                }
            }

        }
    }
    return controller.show(surface)
}