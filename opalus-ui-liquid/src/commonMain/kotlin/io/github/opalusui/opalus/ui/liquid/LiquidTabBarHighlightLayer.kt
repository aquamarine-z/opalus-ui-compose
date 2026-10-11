package io.github.opalusui.opalus.ui.liquid

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.util.lerp
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.backdrops.layerBackdrop

/**
 * iOS HIG 规范：高亮离屏捕获层 (LiquidTabBarHighlightLayer)
 */
@Composable
internal fun <T> LiquidTabBarHighlightLayer(
    activeButtonsBackdrop: LayerBackdrop,
    items: List<T>,
    colors: LiquidTabBarColors,
    dimensions: LiquidTabBarDimensions,
    physics: LiquidTabBarPhysics,
    state: LiquidTabBarState,
    modifier: Modifier = Modifier,
    activeItem: @Composable (index: Int, item: T) -> Unit
) {
    val scale = lerp(1f, physics.activeItemScale, state.pressProgress.coerceIn(0f, 1f))

    Row(
        modifier = modifier
            .clearAndSetSemantics {}
            .alpha(0f)
            .layerBackdrop(activeButtonsBackdrop)
            .height(dimensions.platterHeight)
            .fillMaxWidth()
            .padding(horizontal = dimensions.horizontalPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items.forEachIndexed { index, item ->
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .graphicsLayer {
                        scaleX = scale
                        scaleY = scale
                    },
                contentAlignment = Alignment.Center
            ) {
                androidx.compose.runtime.CompositionLocalProvider(
                    androidx.compose.material3.LocalContentColor provides colors.activeItemColor
                ) {
                    activeItem(index, item)
                }
            }
        }
    }
}
