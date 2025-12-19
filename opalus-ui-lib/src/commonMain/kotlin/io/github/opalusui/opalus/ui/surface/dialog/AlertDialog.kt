package io.github.opalusui.opalus.ui.surface.dialog

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

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
            header = header, actions = arrayOf<@Composable () -> Unit>({
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
    TextButton(
        onClick = {
            close(Unit)
        }, shape = RoundedCornerShape(8.dp), contentPadding = ButtonDefaults.TextButtonContentPadding
    ) {
        Text(
            text = text
        )
    }
}
