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
    val rawProgress = state.pressProgress
    val visualProgress = rawProgress.coerceIn(0f, 1f)
    val elasticProgress = rawProgress.coerceIn(-0.45f, 1f)
    val currentIndicatorHeight = lerp(
        dimensions.indicatorIdleHeight,
        dimensions.indicatorActiveHeight,
        elasticProgress.coerceIn(0f, 1f)
    )

    val indicatorModifier = modifier
        .padding(horizontal = dimensions.horizontalPadding)
        .graphicsLayer {
            translationX =
                if (isLtr) state.animatedValue * tabWidth
                else size.width - (state.animatedValue + 1f) * tabWidth
        }
        .height(currentIndicatorHeight)
        .fillMaxWidth(1f / tabsCount)

    Box(
        modifier = indicatorModifier
            .drawBackdrop(
                backdrop = rememberCombinedBackdrop(backdrop, dockBackdrop, activeButtonsBackdrop),
                shape = { Capsule() },
                effects = {
                    // A zero-strength lens still changes backdrop sampling on
                    // WASM. Keep the inactive indicator as a plain glass fill.
                    if (visualProgress > 0.001f) {
                        lens(
                            refractionHeight = lerp(
                                optics.indicatorRefractionHeightMin.toPx(),
                                optics.indicatorRefractionHeightMax.toPx(),
                                visualProgress
                            ),
                            refractionAmount = optics.indicatorRefractionAmount.toPx() * visualProgress,
                            depthEffect = true,
                            chromaticAberration = optics.chromaticAberration
                        )
                    }
                },
                highlight = {
                    val visualProgress = state.pressProgress.coerceIn(0f, 1f)
                    Highlight.Default.copy(
                        alpha = optics.indicatorHighlightAlpha * visualProgress
                    )
                },
                shadow = {
                    val visualProgress = state.pressProgress.coerceIn(0f, 1f)
                    Shadow(
                        radius = optics.indicatorShadowRadius,
                        offset = DpOffset(0.dp, optics.indicatorShadowOffsetY * visualProgress),
                        alpha = optics.indicatorShadowAlpha * visualProgress
                    )
                },
                innerShadow = {
                    val visualProgress = state.pressProgress.coerceIn(0f, 1f)
                    InnerShadow(
                        radius = optics.indicatorInnerShadowRadius * visualProgress,
                        alpha = optics.indicatorInnerShadowAlpha * visualProgress
                    )
                },
                layerBlock = {
                    val scale = lerp(1f, physics.indicatorActiveScale, visualProgress)
                    val widthScale = lerp(1f, dimensions.indicatorActiveWidthScale, visualProgress)
                    var sX = scale * widthScale
                    var sY = scale

                    // 触底微弹处理：当 elasticProgress < 0f 时，水滴触底微压扁回弹（严格不向外过量膨胀）
                    if (elasticProgress < 0f) {
                        val bottomBounce = elasticProgress * physics.dropletElasticity
                        sX *= (1f - bottomBounce * 0.22f)
                        sY *= (1f + bottomBounce * 0.28f).coerceAtLeast(0.65f)
                    }

                    if (visualProgress > 0.001f) {
                        val speed = (abs(state.velocity) / 6f) * visualProgress
                        val stretch = (speed * physics.velocityHorizontalFactor)
                            .coerceIn(0f, physics.maxVelocityCoerce)
                        sX *= (1f + stretch)
                        sY *= (1f - stretch * physics.velocityVerticalFactor).coerceAtLeast(0.7f)
                    }

                    scaleX = sX
                    scaleY = sY
                },
                onDrawSurface = {
                    // 1. 静止态：比 bar 小且贴合圆角的柔和微暗选中槽位底色
                    drawRect(
                        color = colors.indicatorIdleColor,
                        alpha = 1f - visualProgress
                    )
                    // 2. 激活态：极轻柔的清透水润微光（纯净透亮，杜绝乳白厚重感）
                    drawRect(
                        color = colors.indicatorActiveTint,
                        alpha = visualProgress
                    )
                }
            )
            .border(
                width = dimensions.indicatorBorderWidth,
                color = colors.indicatorBorderColor.copy(
                    alpha = colors.indicatorBorderColor.alpha * state.pressProgress.coerceIn(0f, 1f)
                ),
                shape = Capsule()
            )
    )
}
