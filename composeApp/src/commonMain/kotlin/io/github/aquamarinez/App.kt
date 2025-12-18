package io.github.aquamarinez


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import io.github.aquamarinez.opalus.ui.surface.*
import kotlinx.coroutines.launch

@Composable
fun App() {
    MaterialTheme {
        val scope = rememberCoroutineScope()
        SurfaceHost {
            Scaffold { padding ->
                Box(
                    modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        val controller = useSurface()
                        Button(onClick = {

                            scope.launch {
                                showConfirmDialog(0, controller)
                            }
                        }) {
                            Text("打开确认弹窗")
                        }
                        Button(
                            onClick = {
                                scope.launch {
                                    var value = showConfirmDialog(0, controller)
                                    while (value) {
                                        value = showConfirmDialog(0, controller)
                                            }
                                        }


                            }) {
                            Text("打开一个还有一个弹窗")
                        }
                    }

                }
            }
        }

    }

}

suspend fun showConfirmDialog(layer: Int, controller: SurfaceController): Boolean {
    return controller.dialogs.custom<Boolean>(
        options = CustomDialogOptions(
            properties = DialogProperties(
                dismissOnBackPress = true,
                dismissOnClickOutside = true
            )
        )
    ) { close ->
        val scope = rememberCoroutineScope()
        Card(

            modifier = Modifier.clip(RoundedCornerShape(16.dp))
        ) {
            Column(
                modifier = Modifier.padding(24.dp)
            ) {
                var result by remember { mutableStateOf(false) }
                Text("确认弹窗")
                Text("这是第 $layer 层")
                Text("上一层级的结果是 $result")

                Row(
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(onClick = {
                        close(true)
                    }) {
                        Text("确认")
                    }
                    Button(onClick = {
                        close(false)
                    }) {
                        Text("取消")
                    }
                    Button(onClick = {
                        scope.launch {
                            val value = showConfirmDialog(layer + 1, controller)
                            result = value
                        }
                    }) {
                        Text("打开下一级")
                    }
                }
            }
        }
    } ?: false
}