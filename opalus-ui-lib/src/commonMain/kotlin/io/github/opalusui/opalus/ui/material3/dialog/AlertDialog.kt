package io.github.opalusui.opalus.ui.material3.dialog

import androidx.compose.animation.*
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.window.DialogProperties

class AlertDialogOptions(
    enterTransition: EnterTransition = fadeIn(
        animationSpec = tween(
            durationMillis = 200, easing = FastOutSlowInEasing
        )
    ) + slideInVertically(
        initialOffsetY = { it / 12 },
        animationSpec = tween(
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
) : CustomDialogOptions(
    enterTransition = enterTransition,
    exitTransition = exitTransition,
    properties = properties
)
suspend fun Dialogs.alert(
    header: (@Composable () -> Unit) = {
        DialogHeader(
            title = "Alert"
        )
    },
    message: (@Composable () -> Unit) = { Text("") },
    ok: (@Composable (close: (Unit) -> Unit) -> Unit)? = null,
    options: AlertDialogOptions = AlertDialogOptions(),
) {
    this.custom(
        options = options
    ) { close ->
        DialogContent(
            header = header, actions = listOf<@Composable () -> Unit>({
                ok?.invoke(close) ?: DefaultOkButton(close)
            })
        ) {
            message()
        }
    }
}

suspend fun Dialogs.alert(
    title: String,
    message: String,
    okText: String = "OK",
    options: AlertDialogOptions = AlertDialogOptions(),
) = this.alert(
    header = { DialogHeader(title = title) },
    message = { Text(message) },
    ok = { close -> DefaultOkButton(close, okText) },
    options = options
)

@Composable
private fun DefaultOkButton(
    close: (Unit) -> Unit, text: String = "OK"
) {
    DialogAction({ close(Unit) }) {
        Text(text)
    }
}
