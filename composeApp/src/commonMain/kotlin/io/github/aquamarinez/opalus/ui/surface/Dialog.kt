package io.github.aquamarinez.opalus.ui.surface

import androidx.compose.animation.*
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds

data class CustomDialogOptions(
    val fadeInDuration: Duration = 150.milliseconds,
    val fadeOutDuration: Duration = 150.milliseconds,
    val scrimColor: Color = Color.Black.copy(alpha = 0.5f),  // 可自定义遮罩颜色和透明度
    val dismissOnClickOutside: Boolean = true,
    val properties: DialogProperties = DialogProperties(
        dismissOnClickOutside = false,
        usePlatformDefaultWidth = false,
        dismissOnBackPress = true,
    ),
)

class Dialogs {
    companion object {
        suspend fun <T : Any?> custom(
            options: CustomDialogOptions = CustomDialogOptions(),
            content: @Composable (close: (value: T?) -> Unit) -> Unit
        ): T? {
            val surface = Surface.create<T>() { state ->
                val scope = rememberCoroutineScope()
                var visible by remember { mutableStateOf(false) }
                var result: T? by remember { mutableStateOf(null) }

                val closeWithAnimation: (value: T?) -> Unit = { value ->
                    result = value
                    visible = false
                    scope.launch {
                        // 等待退出动画完成（取 fadeOut 和 scaleOut 的最长时长）
                        delay(options.fadeOutDuration.inWholeMilliseconds)
                        state.resolve(result)
                        state.close()
                    }
                }
                LaunchedEffect(Unit) {
                    visible = true
                }
                Dialog(
                    onDismissRequest = {
                        if (options.dismissOnClickOutside) {
                            closeWithAnimation(null)
                        }
                    }, properties = options.properties
                ) {
                    AnimatedVisibility(
                        visible = visible,
                        enter = fadeIn(tween(options.fadeInDuration.inWholeMilliseconds.toInt())) + scaleIn(
                            initialScale = 0.8f, animationSpec = spring(dampingRatio = Spring.DampingRatioMediumBouncy)
                        ),
                        exit = fadeOut(tween(options.fadeOutDuration.inWholeMilliseconds.toInt())) + scaleOut(
                            targetScale = 0.9f,
                            animationSpec = tween(options.fadeOutDuration.inWholeMilliseconds.toInt())
                        )
                    ) {
                        Card(
                            modifier = Modifier.shadow(8.dp, RoundedCornerShape(16.dp)),
                            shape = RoundedCornerShape(16.dp)
                        ) {
                            content(closeWithAnimation)
                        }
                    }


                }
            }

            return Surface.show(surface)
        }
    }
}