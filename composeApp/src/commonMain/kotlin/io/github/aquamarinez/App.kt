package io.github.aquamarinez


import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import io.github.aquamarinez.opalus.ui.surface.Dialogs
import io.github.aquamarinez.opalus.ui.surface.SurfaceHost
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
                    Button(onClick = {

                        scope.launch {
                            showConfirmDialog(0)


                        }
                    }) {
                        Text("打开确认弹窗")
                    }
                }
            }
        }

    }

}

suspend fun showConfirmDialog(layer: Int): Boolean {
    return Dialogs.custom<Boolean> { close ->
        val scope = rememberCoroutineScope()
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
                        val value = showConfirmDialog(layer + 1)
                        result = value
                    }
                }) {
                    Text("打开下一级")
                }
            }
        }
    } ?: false
}