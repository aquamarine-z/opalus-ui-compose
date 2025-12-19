package io.github.opalusui.opalus.ui.surface.dialog

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.runtime.Composable
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.unit.dp


@Composable
fun DialogContent(
    header: (@Composable () -> Unit) = {
        DialogHeader()
    },
    actions: Array<@Composable () -> Unit> = emptyArray(),
    content: (@Composable () -> Unit) = {},
) {
    Card(
        Modifier.shadow(
            elevation = 8.dp, shape = RoundedCornerShape(8.dp), clip = false
        )
    ) {
        Column(
            modifier = Modifier.padding(16.dp).fillMaxWidth()

        ) {
            header()
            Spacer(modifier = Modifier.height(16.dp))
            content()
            Spacer(modifier = Modifier.height(16.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.End),
                verticalAlignment = Alignment.CenterVertically
            ) {
                for (action in actions.withIndex()) {
                    key(action.index) {
                        action.value()
                    }
                }
            }
        }

    }
}

