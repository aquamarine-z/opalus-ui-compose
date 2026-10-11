package io.github.opalusui.opalus.ui.material3.dialog

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ProvideTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.unit.dp

@Composable
fun DialogHeader(
    icon: (@Composable () -> Unit)? = null,
    content: (@Composable () -> Unit) = {
        Text(text = "Dialog Title")
    },
) {
    val styledContent = @Composable {
        ProvideTextStyle(
            value = MaterialTheme.typography.headlineSmall
        ) {
            CompositionLocalProvider(
                LocalContentColor provides MaterialTheme.colorScheme.onSurface
            ) {
                content()
            }
        }
    }
    if (icon != null) {
        Column(
            verticalArrangement = Arrangement.spacedBy(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            icon()
            styledContent()
        }
    } else {
        Column(
            verticalArrangement = Arrangement.spacedBy(0.dp),
            horizontalAlignment = Alignment.Start,
        ) {
            styledContent()
        }
    }
}

@Composable
fun DialogHeader(
    icon: (@Composable () -> Unit) = {},
    title: String,
) = DialogHeader(icon, { Text(text = title) })