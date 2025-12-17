package io.github.aquamarinez


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import io.github.aquamarinez.opalus.ui.surface.SurfaceController
import io.github.aquamarinez.opalus.ui.surface.SurfaceHost
import io.github.aquamarinez.opalus.ui.surface.dialogs
import io.github.aquamarinez.opalus.ui.surface.useSurface
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
                                    // 递归方式：只有当前一个确认了，才会再调用自己打开下一个
                                    fun loop() {
                                        scope.launch {
                                            val confirmed = showConfirmDialog(0,controller)
                                            if (confirmed) {
                                                println("open again")
                                                loop()  // 再次打开同一个对话框
                                            }
                                        }
                                        
                                    }
                                    loop()
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
    return controller.dialogs.custom<Boolean> { close ->
        val scope = rememberCoroutineScope()
        Card(

            modifier = Modifier.padding(24.dp).padding(24.dp).clip(RoundedCornerShape(16.dp))
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