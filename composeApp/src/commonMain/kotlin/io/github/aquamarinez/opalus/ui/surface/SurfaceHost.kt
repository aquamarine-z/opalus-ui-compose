package io.github.aquamarinez.opalus.ui.surface

import androidx.compose.runtime.*
import kotlinx.coroutines.CompletableDeferred


internal val LocalSurfaceHost = compositionLocalOf<SurfaceController> {
    error("No SurfaceController provided. Please wrap with SurfaceProvider.")
}

open class SurfaceController {
    internal val surfaceOrder = mutableStateListOf<String>()
    internal val surfaceRegistry = mutableStateMapOf<String, Surface<Any?>>()
    internal val surfaceStates = mutableStateMapOf<String, SurfaceState<Any?>>()

    fun clear() {
        surfaceOrder.clear()
        surfaceRegistry.clear()
        surfaceStates.clear()
    }

    fun getSurface(id: String): Surface<Any?>? {
        return surfaceRegistry[id]
    }

    fun removeSurface(id: String) {
        surfaceRegistry.remove(id)
    }

    suspend fun <T> show(surface: Surface<T>): T? {


        if (surfaceStates.containsKey(surface.id)) {
            //Let the surface be visible
            surfaceStates[surface.id]?.visible = true
            return null
        }
        val deferred = CompletableDeferred<T?>()
        val state =
            SurfaceState<T>(visible = true, resolve = { value -> deferred.complete(value) }, id = surface.id, close = {
                surfaceStates.remove(surface.id)
                surfaceRegistry.remove(surface.id)
                surfaceOrder.remove(surface.id)
            }, hide = {
                val state = surfaceStates[surface.id]!!
                state.visible = false
                surfaceStates[surface.id] = state
            })
        @Suppress("UNCHECKED_CAST")
        surfaceStates[surface.id] = state as SurfaceState<Any?>
        @Suppress("UNCHECKED_CAST")
        surfaceRegistry[surface.id] = surface as Surface<Any?>
        surfaceOrder.add(surface.id)
        return deferred.await()
    }
}

@Composable
fun useSurface(): SurfaceController {
    return LocalSurfaceHost.current
}

data class SurfaceState<T : Any?>(
    var visible: Boolean = false,
    val resolve: (value: T?) -> Unit,
    val id: String,
    val close: () -> Unit,
    val hide: () -> Unit
)

class Surface<T : Any?>(
    val id: String, val content: @Composable (SurfaceState<T>) -> Unit
) {
    companion object {
        private var nextId = 0L
        private fun generateId(): String {
            return "surface_${nextId++}"
        }

        fun <T> create(content: @Composable (SurfaceState<T>) -> Unit): Surface<T> {
            val id = generateId()
            return Surface(id, content)
        }
    }

}

@Composable
fun SurfaceHost(
    controller: SurfaceController = remember { SurfaceController() }, content: @Composable () -> Unit
) {
    CompositionLocalProvider(LocalSurfaceHost provides controller) {
        content()
        SurfaceRenderer()
    }
}

@Composable
internal fun SurfaceRenderer() {
    val surfaceHost = LocalSurfaceHost.current
    val surfaceOrder = surfaceHost.surfaceOrder
    val surfaceRegistry = surfaceHost.surfaceRegistry
    val surfaceStates = surfaceHost.surfaceStates
    surfaceOrder.forEach { id ->
        val surface = surfaceRegistry[id] ?: return@forEach
        val state = surfaceStates[id] ?: return@forEach
        surface.content(state)
    }
}