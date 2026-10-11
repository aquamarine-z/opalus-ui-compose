package io.github.opalusui.opalus.ui.material3.dialog

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

class ConfirmDialogOptions(
    var type: ConfirmDialogType = ConfirmDialogType.Common,
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
