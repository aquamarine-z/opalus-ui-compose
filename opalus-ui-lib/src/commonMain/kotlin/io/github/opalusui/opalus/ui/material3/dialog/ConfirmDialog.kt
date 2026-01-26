package io.github.opalusui.opalus.ui.material3.dialog

import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.DialogProperties

class ConfirmDialogOptions(
    enterTransition: EnterTransition = fadeIn(
        animationSpec = tween(
            durationMillis = 200, easing = FastOutSlowInEasing
        )
    ) + slideInVertically(
        initialOffsetY = { it / 12 }, animationSpec = tween(
            durationMillis = 200, easing = FastOutSlowInEasing
        )
    ),
    exitTransition: ExitTransition = fadeOut(
        animationSpec = tween(
            durationMillis = 150, easing = FastOutSlowInEasing
        )
    ) + slideOutVertically(
        targetOffsetY = { it / 12 }, animationSpec = tween(
            durationMillis = 150, easing = FastOutSlowInEasing
        )
    ),
    properties: DialogProperties = DialogProperties(
        dismissOnClickOutside = true,
        usePlatformDefaultWidth = true,
        dismissOnBackPress = true,
    ),
    val type: ConfirmDialogType = ConfirmDialogType.Common,

    ) : CustomDialogOptions(
    enterTransition = enterTransition, exitTransition = exitTransition, properties = properties
)

enum class ConfirmDialogType {
    Dangerous, Common,
}

suspend fun Dialogs.confirm(
    header: (@Composable () -> Unit) = { DialogHeader() },
    message: (@Composable () -> Unit) = { Text("Are you sure?") },
    confirm: (@Composable (close: (Boolean?) -> Unit) -> Unit)? = null,
    cancel: (@Composable (close: (Boolean?) -> Unit) -> Unit)? = null,
    options: ConfirmDialogOptions = ConfirmDialogOptions(),
): Boolean? {
    return this.custom(
        options = options
    ) { close ->
        DialogContent(
            header = header, actions = listOf<@Composable () -> Unit>({
                cancel?.invoke(close) ?: DefaultCancelButton(close)
            }, {
                confirm?.invoke(close) ?: DefaultConfirmButton(close, type = options.type)
            })
        ) {
            message()
        }
    }
}

suspend fun Dialogs.confirm(
    title: String,
    message: String,
    confirmText: String = "Confirm",
    cancelText: String = "Cancel",
    options: ConfirmDialogOptions = ConfirmDialogOptions(),
): Boolean? = this.confirm(
    header = { DialogHeader(title = title) },
    message = { Text(message) },
    confirm = { close -> DefaultConfirmButton(close, confirmText, options.type) },
    cancel = { close -> DefaultCancelButton(close, cancelText) },
    options = options
)

@Composable
private fun DefaultConfirmButton(
    close: (Boolean?) -> Unit, text: String = "Confirm", type: ConfirmDialogType
) {
    val type: DialogActionType = when (type) {
        ConfirmDialogType.Dangerous -> DialogActionType.Dangerous
        ConfirmDialogType.Common -> DialogActionType.Common
    }
    DialogAction({
        close(true)
    }, type) {
        Text(text)
    }
}


@Composable
private fun DefaultCancelButton(
    close: (Boolean?) -> Unit, text: String = "Cancel",
) {
    DialogAction({
        close(false)
    }, DialogActionType.Common) {
        Text(text)
    }
}
