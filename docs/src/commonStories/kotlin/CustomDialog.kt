import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.rememberCoroutineScope
import io.github.opalusui.opalus.ui.surface.SurfaceHost
import io.github.opalusui.opalus.ui.surface.dialog.DialogContent
import io.github.opalusui.opalus.ui.surface.dialog.DialogHeader
import io.github.opalusui.opalus.ui.surface.dialog.custom
import io.github.opalusui.opalus.ui.surface.dialog.dialogs
import io.github.opalusui.opalus.ui.surface.useSurface
import kotlinx.coroutines.launch
import org.jetbrains.compose.storytale.story

val `Custom Dialog default state` by story {
    MaterialTheme {
        SurfaceHost {

            val surface = useSurface()
            rememberCoroutineScope().launch {
                surface.dialogs.custom { close ->
                    DialogContent(
                        header = { DialogHeader(title = "Title") }, actions = arrayOf(
                            { Button(onClick = { close(Unit) }) { Text("OK") } })
                    ) {
                        Text("Content")
                    }
                }
            }
        }
    }
}