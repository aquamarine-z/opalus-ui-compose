package io.github.opalusui.opalus.ui.material3.dialog

import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.MutableTransitionState
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import io.github.opalusui.opalus.ui.surface.Surface

open class CustomDialogOptions(
    val enterTransition: EnterTransition = fadeIn(
        animationSpec = tween(
            durationMillis = 200, easing = FastOutSlowInEasing
        )
    ) + slideInVertically(
        initialOffsetY = { it / 12 },
        animationSpec = tween(
            durationMillis = 200, easing = FastOutSlowInEasing
        )
    ),
    val exitTransition: ExitTransition = fadeOut(
        animationSpec = tween(
            durationMillis = 150, easing = FastOutSlowInEasing
        )
    ) + slideOutVertically(
        targetOffsetY = { it / 12 }, animationSpec = tween(
            durationMillis = 150, easing = FastOutSlowInEasing
        )
    ),
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
                    visibleState = visibleState, enter = options.enterTransition, exit = options.exitTransition
                ) {
                    content(closeWithAnimation)
                }
            }

        }
    }
    return controller.show(surface)
}