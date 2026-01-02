package io.github.opalusui


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
import io.github.opalusui.opalus.ui.material3.dialog.DialogContent
import io.github.opalusui.opalus.ui.material3.dialog.DialogHeader
import io.github.opalusui.opalus.ui.material3.dialog.alert
import io.github.opalusui.opalus.ui.material3.dialog.confirm
import io.github.opalusui.opalus.ui.material3.dialog.custom
import io.github.opalusui.opalus.ui.material3.dialog.dialogs
import io.github.opalusui.opalus.ui.surface.SurfaceController
import io.github.opalusui.opalus.ui.surface.SurfaceHost
import io.github.opalusui.opalus.ui.surface.useSurface
import kotlinx.coroutines.launch

@Composable
fun App() {
    MaterialTheme {
        SurfaceHost {
            val controller = useSurface()
            val scope = rememberCoroutineScope()
            Column(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
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
                            val result =
                                controller.dialogs.confirm("删除此文件", "你确定要删除此文件吗，此操作无法还原！")
                            controller.dialogs.alert(
                                title = "警告", message = "你选择了 : $result", okText = "我知道了"
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
            }, actions = arrayOf({
                Button(onClick = {
                    close(false)
                }) {
                    Text("取消")
                }
            }, {
                Button(onClick = {
                    close(true)
                }) {
                    Text("确定")
                }
            })
        ) {
            Text("你确定要删除此内容吗?")
        }
    } ?: false
}
