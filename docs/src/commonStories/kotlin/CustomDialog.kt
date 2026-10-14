import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.LaunchedEffect
import io.github.opalusui.opalus.ui.material3.dialog.DialogContent
import io.github.opalusui.opalus.ui.material3.dialog.DialogHeader
import io.github.opalusui.opalus.ui.material3.dialog.custom
import io.github.opalusui.opalus.ui.material3.dialog.dialogs
import io.github.opalusui.opalus.ui.surface.SurfaceHost
import io.github.opalusui.opalus.ui.surface.useSurface
import org.jetbrains.compose.storytale.story

val `Custom Dialog default state` by story {
    MaterialTheme {
        SurfaceHost {
            val surface = useSurface()
            LaunchedEffect(Unit) {
                surface.dialogs.custom<Unit> { close ->
                    DialogContent(
                        header = { DialogHeader(content = { Text("Title") }) },
                        actions = listOf(
                            { Button(onClick = { close(Unit) }) { Text("OK") } }
                        )
                    ) {
                        Text("Content")
                    }
                }
            }
        }
    }
}