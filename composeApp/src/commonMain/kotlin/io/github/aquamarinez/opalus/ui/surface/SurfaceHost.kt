package io.github.aquamarinez.opalus.ui.surface

import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import kotlinx.coroutines.CompletableDeferred


val surfaceOrder = mutableStateListOf<String>()
val surfaceRegistry = mutableStateMapOf<String, Surface<Any?>>()
val surfaceStates = mutableStateMapOf<String, SurfaceState<Any?>>()

data class SurfaceState<T:Any?>(
    var visible: Boolean = false,
    val resolve: (value: T?) -> Unit,
    val id: String,
    val close: () -> Unit,
    val hide: () -> Unit
)

class Surface<T:Any?>(
    val id: String, val content: @Composable (SurfaceState<T>) -> Unit
) {
    companion object {
        private var nextId = 0
        fun generateId(): String {
            return "surface_${nextId++}"
        }

        fun <T> create(content: @Composable (SurfaceState<T>) -> Unit): Surface<T> {
            val id = Surface.generateId()
            return Surface(id, content)
        }

        suspend fun <T> show(surface: Surface<T>): T? {
            if (surfaceStates.containsKey(surface.id)) {
                throw Exception("Surface ${surface.id} is already visible")
            }
            val deferred = CompletableDeferred<T?>()
            val state = SurfaceState<T>(
                visible = true,
                resolve = { value -> deferred.complete(value) },
                id = surface.id,
                close = {
                    surfaceStates.remove(surface.id)
                    surfaceRegistry.remove(surface.id)
                    surfaceOrder.remove(surface.id)  // 同时移除顺序
                },
                hide = {
                    val state = surfaceStates[surface.id]!!
                    state.visible = false
                    surfaceStates[surface.id] = state
                })
            surfaceStates[surface.id] = state as SurfaceState<Any?>
            surfaceRegistry[surface.id] = surface as Surface<Any?>
            surfaceOrder.add(surface.id)  // 添加到底部，确保最后绘制（最上层）
            return deferred.await()
        }

        fun remove(id: String) {
            surfaceStates.remove(id)
            surfaceRegistry.remove(id)
            surfaceOrder.remove(id)
        }
    }

}

@Composable
fun SurfaceHost(
    content: @Composable () -> Unit
) {
    content()
    surfaceOrder.forEach { id ->
        val surface = surfaceRegistry[id] ?: return@forEach
        val state = surfaceStates[id] ?: return@forEach
        surface.content(state)
    }
}