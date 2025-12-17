package io.github.aquamarinez.opalus.ui.surface

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.runtime.*
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

data class CustomDialogOptions(
    val fadeInDuration: Duration = 150.milliseconds,
    val fadeOutDuration: Duration = 150.milliseconds,
    val properties: DialogProperties = DialogProperties(
        dismissOnClickOutside = false,
        usePlatformDefaultWidth = false,
        dismissOnBackPress = true,
    ),
)

val SurfaceController.dialogs: Dialogs
    get() = Dialogs(this)

class Dialogs(val controller: SurfaceController) {
    suspend fun <T : Any?> custom(
        options: CustomDialogOptions = CustomDialogOptions(), content: @Composable (close: (value: T?) -> Unit) -> Unit
    ): T? {
        val surface = Surface.create<T>() { state ->
            val scope = rememberCoroutineScope()
            var visible by remember { mutableStateOf(false) }
            var result: T? by remember { mutableStateOf(null) }

            val closeWithAnimation: (value: T?) -> Unit = { value ->
                result = value
                visible = false
                scope.launch {
                    delay(options.fadeOutDuration.inWholeMilliseconds)
                    state.resolve(result)
                    state.close()
                }
            }
            LaunchedEffect(Unit) {
                visible = true
            }
            Dialog(
                onDismissRequest = {
                    if (options.properties.dismissOnClickOutside) {
                        closeWithAnimation(null)
                    }
                }, properties = options.properties
            ) {
                AnimatedVisibility(
                    visible = visible,
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
        return this.controller.show(surface)
    }
}