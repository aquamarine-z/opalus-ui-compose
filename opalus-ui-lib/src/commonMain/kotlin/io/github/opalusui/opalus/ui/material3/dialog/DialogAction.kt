package io.github.opalusui.opalus.ui.material3.dialog

import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable

enum class DialogActionType {
    Dangerous, Common,
}

@Composable
fun DialogAction(
    onClick: () -> Unit, type: DialogActionType = DialogActionType.Common, content: @Composable () -> Unit
) {
    TextButton(
        onClick = onClick,
        shape = MaterialTheme.shapes.small,
        contentPadding = ButtonDefaults.TextButtonContentPadding,
        colors = ButtonDefaults.textButtonColors(
            contentColor = when (type) {
                DialogActionType.Common -> MaterialTheme.colorScheme.primary
                DialogActionType.Dangerous -> MaterialTheme.colorScheme.error
            }
        )
    ) {
        ProvideTextStyle(
            value = MaterialTheme.typography.labelLarge
        ) {
            content()
        }
    }
}


