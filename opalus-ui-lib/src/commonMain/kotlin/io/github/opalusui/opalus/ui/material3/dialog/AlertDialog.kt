package io.github.opalusui.opalus.ui.material3.dialog

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

class AlertDialogOptions : CustomDialogOptions()
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
