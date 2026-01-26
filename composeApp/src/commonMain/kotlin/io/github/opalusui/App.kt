package io.github.opalusui


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.DialogProperties
import io.github.opalusui.opalus.ui.material3.dialog.*
import io.github.opalusui.opalus.ui.surface.SurfaceController
import io.github.opalusui.opalus.ui.surface.SurfaceHost
import io.github.opalusui.opalus.ui.surface.useSurface
import io.github.opalusui.ui.theme.AppTypography
import io.github.opalusui.ui.theme.lightScheme
import kotlinx.coroutines.launch

@Composable
fun App() {
    MaterialTheme(
        colorScheme = lightScheme,
        typography = AppTypography
    ) {
        SurfaceHost {
            val controller = useSurface()
            val scope = rememberCoroutineScope()
            Column(
                modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceDim),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Button(
                    onClick = {
                        scope.launch {
                            val result = controller.showCustomDialog()
                            println("Result: $result")
                        }
                    }) {
                    Text("Show Custom Dialog")
                }
                val controller = useSurface()
                Button(
                    onClick = {
                        scope.launch {
                            val result = controller.dialogs.confirm(
                                "删除此文件",
                                "你确定要删除此文件吗，此操作无法还原！",
                                options = ConfirmDialogOptions().apply {
                                    type = ConfirmDialogType.Common
                                    properties = DialogProperties(
                                        dismissOnClickOutside = true,
                                        dismissOnBackPress = true,
                                        usePlatformDefaultWidth = true,
                                    )
                                })
                            controller.dialogs.alert(
                                title = "警告", message = "你选择了 : $result", okText = "OK"
                            )

                        }
                    }) {
                    Text("Show Confirm Dialog")
                }
            }
        }

    }

}

private suspend fun SurfaceController.showCustomDialog(): Boolean {
    return this.dialogs.custom<Boolean> { close ->
        DialogContent(
            header = {
                DialogHeader {
                    Text("Title")
                }
            }, actions = listOf({
                DialogAction({
                    close(false)
                }, type = DialogActionType.Common){
                    Text("Cancel")
                }
            }, {
                DialogAction({
                    close(false)
                }, type = DialogActionType.Dangerous){
                    Text("Confirm")
                }
            })
        ) {
            Text("你确定要删除此内容吗?")
        }
    } ?: false
}
