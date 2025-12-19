package io.github.opalusui.opalus.ui.surface.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp

@Composable
fun DialogHeader(
    icon: (@Composable () -> Unit) = {

    },
    content: (@Composable () -> Unit) = {
        Text(text = "Dialog Title")
    },
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
    ) {
        icon()
        ProvideTextStyle(
            value = MaterialTheme.typography.titleLarge,
        ) {
            content()
        }

    }
}

@Composable
fun DialogHeader(
    icon: (@Composable () -> Unit) = {},
    title: String,
) = DialogHeader(icon, { Text(text = title) })