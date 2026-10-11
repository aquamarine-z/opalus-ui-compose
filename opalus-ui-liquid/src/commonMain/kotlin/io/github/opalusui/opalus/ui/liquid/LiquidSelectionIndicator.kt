package io.github.opalusui.opalus.ui.liquid

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.util.fastCoerceIn
import androidx.compose.ui.util.lerp
import kotlin.math.abs
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.rememberCombinedBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.highlight.Highlight
import com.kyant.backdrop.shadow.InnerShadow
import com.kyant.backdrop.shadow.Shadow
import com.kyant.shapes.Capsule

/**
 * iOS HIG 规范：水滴选中指示器 (LiquidSelectionIndicator)
 */
@Composable
internal fun LiquidSelectionIndicator(
    backdrop: Backdrop,
    dockBackdrop: Backdrop,
    activeButtonsBackdrop: Backdrop,
    colors: LiquidTabBarColors,
    dimensions: LiquidTabBarDimensions,
    optics: LiquidTabBarOptics,
    physics: LiquidTabBarPhysics,
    state: LiquidTabBarState,
    tabWidth: Float,
    tabsCount: Int,
    isLtr: Boolean,
    modifier: Modifier = Modifier
) {
    val currentIndicatorHeight = lerp(
        dimensions.indicatorIdleHeight,
        dimensions.indicatorActiveHeight,
        state.pressProgress
    )

    Box(
        modifier = modifier
            .padding(horizontal = dimensions.horizontalPadding)
            .graphicsLayer {
                translationX =
                    if (isLtr) state.animatedValue * tabWidth
                    else size.width - (state.animatedValue + 1f) * tabWidth
            }
            .drawBackdrop(
                backdrop = rememberCombinedBackdrop(backdrop, dockBackdrop, activeButtonsBackdrop),
                shape = { Capsule() },
                effects = {
                    val progress = state.pressProgress
                    lens(
                        refractionHeight = lerp(
                            optics.indicatorRefractionHeightMin.toPx(),
                            optics.indicatorRefractionHeightMax.toPx(),
                            progress
                        ),
                        refractionAmount = optics.indicatorRefractionAmount.toPx() * progress,
                        depthEffect = true,
                        chromaticAberration = optics.chromaticAberration
                    )
                },
                highlight = {
                    val progress = state.pressProgress
                    Highlight.Default.copy(
                        alpha = optics.indicatorHighlightAlpha * progress
                    )
                },
                shadow = {
                    val progress = state.pressProgress
                    Shadow(
                        radius = optics.indicatorShadowRadius,
                        offset = DpOffset(0.dp, optics.indicatorShadowOffsetY * progress),
                        alpha = optics.indicatorShadowAlpha * progress
                    )
                },
                innerShadow = {
                    val progress = state.pressProgress
                    InnerShadow(
                        radius = optics.indicatorInnerShadowRadius * progress,
                        alpha = optics.indicatorInnerShadowAlpha * progress
                    )
                },
                layerBlock = {
                    val progress = state.pressProgress
                    val scale = lerp(1f, physics.indicatorActiveScale, progress)
                    scaleX = scale
                    scaleY = scale
                    if (progress > 0.001f) {
                        val speed = (abs(state.velocity) / 6f) * progress
                        val stretch = (speed * physics.velocityHorizontalFactor)
                            .coerceIn(0f, physics.maxVelocityCoerce)
                        scaleX *= (1f + stretch)
                        scaleY *= (1f - stretch * physics.velocityVerticalFactor).coerceAtLeast(0.7f)
                    }
                },
                onDrawSurface = {
                    val progress = state.pressProgress
                    // 1. 静止态：比 bar 小且贴合圆角的柔和微暗选中槽位底色
                    drawRect(
                        color = colors.indicatorIdleColor,
                        alpha = 1f - progress
                    )
                    // 2. 激活态：极轻柔的清透水润微光（纯净透亮，杜绝乳白厚重感）
                    drawRect(
                        color = colors.indicatorActiveTint,
                        alpha = progress
                    )
                }
            )
            .border(
                width = dimensions.indicatorBorderWidth,
                color = colors.indicatorBorderColor.copy(
                    alpha = colors.indicatorBorderColor.alpha * state.pressProgress
                ),
                shape = Capsule()
            )
            .height(currentIndicatorHeight)
            .fillMaxWidth(1f / tabsCount)
    )
}
