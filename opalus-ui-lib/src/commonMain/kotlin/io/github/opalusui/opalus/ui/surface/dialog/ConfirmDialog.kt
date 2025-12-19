package io.github.opalusui.opalus.ui.surface.dialog

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.unit.dp

class ConfirmDialogOptions(
    val type: ConfirmDialogType = ConfirmDialogType.Common,
) : CustomDialogOptions()

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
            header = header, actions = arrayOf<@Composable () -> Unit>({
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

private val buttonPadding = ButtonDefaults.TextButtonContentPadding

@Composable
private fun DefaultConfirmButton(close: (Boolean?) -> Unit, text: String = "Confirm", type: ConfirmDialogType) {
    Button(
        onClick = { close(true) }, shape = RoundedCornerShape(8.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = when (type) {
                ConfirmDialogType.Dangerous -> MaterialTheme.colorScheme.error
                ConfirmDialogType.Common -> MaterialTheme.colorScheme.primary
            }
        ),
        contentPadding = buttonPadding,

        ) {
        Text(text = text)
    }
}

@Composable
private fun DefaultCancelButton(close: (Boolean?) -> Unit, text: String = "Cancel") {
    TextButton(
        onClick = { close(false) }, shape = RoundedCornerShape(8.dp), contentPadding = buttonPadding
    ) {
        Text(text = text)
    }
}