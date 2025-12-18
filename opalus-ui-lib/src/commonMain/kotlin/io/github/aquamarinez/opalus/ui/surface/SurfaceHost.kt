package io.github.aquamarinez.opalus.ui.surface

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock


internal val LocalSurfaceHost = compositionLocalOf<SurfaceController> {
    error("No SurfaceController provided. Please wrap with SurfaceHostProvider.")
}

open class SurfaceController {
    internal val surfaceOrder = mutableStateListOf<String>()
    internal val surfaceRegistry = mutableStateMapOf<String, Surface<Any?>>()
    internal val surfaceStates = mutableStateMapOf<String, SurfaceState<Any?>>()
    internal val mutex = Mutex()

    suspend fun clear() {
        mutex.withLock {
            surfaceOrder.clear()
            surfaceRegistry.clear()
            surfaceStates.clear()
        }
        
    }

    fun getSurface(id: String): Surface<Any?>? {
        return surfaceRegistry[id]
    }

    suspend fun removeSurface(id: String) {
        mutex.withLock {
            surfaceRegistry.remove(id)
            surfaceStates.remove(id)
            surfaceOrder.remove(id)
        }
        
    }

    suspend fun <T> show(surface: Surface<T>): T? {


        if (surfaceStates.containsKey(surface.id)) {
            mutex.withLock {
                surfaceStates[surface.id]?.visible = true
            }
          
            return null
        }
        val deferred = CompletableDeferred<T?>()
        val state =
            SurfaceState<T>(visible = true, resolve = { value -> deferred.complete(value) }, id = surface.id, close = {
                mutex.withLock {
                    surfaceStates.remove(surface.id)
                    surfaceRegistry.remove(surface.id)
                    surfaceOrder.remove(surface.id)
                }
            }, hide = {
                mutex.withLock {
                    val state = surfaceStates[surface.id]!!
                    state.visible = false
                    surfaceStates[surface.id] = state
                }
               
            })
        mutex.withLock {
            @Suppress("UNCHECKED_CAST")
            surfaceStates[surface.id] = state as SurfaceState<Any?>
            @Suppress("UNCHECKED_CAST")
            surfaceRegistry[surface.id] = surface as Surface<Any?>
            surfaceOrder.add(surface.id)
        }
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
    val close: suspend () -> Unit,
    val hide: suspend () -> Unit
)

class Surface<T : Any?>(
    val id: String, val content: @Composable (SurfaceState<T>) -> Unit
) {
    companion object {
        private var nextId = 0L
        private val mutex = Mutex()
        private suspend fun generateId(): String {
            val newNextId = mutex.withLock {
                nextId++
            }
            return "surface_${newNextId}"
        }

        suspend fun <T> create(content: @Composable (SurfaceState<T>) -> Unit): Surface<T> {
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
        Box(modifier = Modifier.fillMaxSize()) {
            content()
            SurfaceRenderer()
        }
        
    }
}

@Composable
internal fun SurfaceRenderer() {
    val surfaceHost = LocalSurfaceHost.current
    val surfaceOrder = surfaceHost.surfaceOrder
    val surfaceRegistry = surfaceHost.surfaceRegistry
    val surfaceStates = surfaceHost.surfaceStates

    Box(modifier = Modifier.fillMaxSize()) {
        surfaceOrder.forEach { id ->
            val surface = surfaceRegistry[id] ?: return@forEach
            val state = surfaceStates[id] ?: return@forEach
            key(id) {
                surface.content(state)
            }
        }
    }
}