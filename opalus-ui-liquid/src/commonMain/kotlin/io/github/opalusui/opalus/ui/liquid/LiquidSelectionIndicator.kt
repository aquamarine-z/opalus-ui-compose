package io.github.opalusui.opalus.ui.liquid

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.addOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
import androidx.compose.ui.util.fastCoerceIn
import androidx.compose.ui.util.lerp
import androidx.compose.runtime.withFrameNanos
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
    inactiveIndicatorShape: Shape,
    activeIndicatorShape: Shape,
    modifier: Modifier = Modifier
) {
    val combinedBackdrop = rememberCombinedBackdrop(backdrop, dockBackdrop, activeButtonsBackdrop)
    val lensSupported = activeIndicatorShape.supportsLiquidLens()
    var shaderWarmupVisible by remember(
        optics.chromaticAberration,
        inactiveIndicatorShape,
        activeIndicatorShape
    ) { mutableStateOf(true) }

    LaunchedEffect(optics.chromaticAberration, inactiveIndicatorShape, activeIndicatorShape) {
        // Let the real indicator node render its lens once before interaction.
        // Backdrop keeps its RuntimeShader cache per drawBackdrop node, so a
        // separate hidden warmup node would not remove the first-touch hitch.
        withFrameNanos { }
        shaderWarmupVisible = false
    }

    val indicatorModifier = modifier
        .padding(horizontal = dimensions.horizontalPadding)
        .graphicsLayer {
            translationX =
                if (isLtr) state.animatedValue * tabWidth
                else size.width - (state.animatedValue + 1f) * tabWidth
            // The first frame is an offscreen shader warmup. Keeping the
            // actual drawBackdrop node alive makes its cached shader available
            // when the user performs the first click.
            alpha = if (shaderWarmupVisible) 0f else 1f
        }
        .height(dimensions.indicatorIdleHeight)
        .fillMaxWidth(1f / tabsCount)

    Box(
        modifier = indicatorModifier
            .drawBackdrop(
                backdrop = combinedBackdrop,
                shape = {
                    // Keep the original idle geometry until the droplet is
                    // actually activated. The active shape then takes over
                    // without adding a recomposition to pointer frames.
                    if (shaderWarmupVisible || state.pressProgress > 0.001f) {
                        activeIndicatorShape
                    } else {
                        inactiveIndicatorShape
                    }
                },
                effects = {
                    val visualProgress = state.pressProgress.coerceIn(0f, 1f)
                    val effectProgress = if (shaderWarmupVisible) 1f else visualProgress
                    // A zero-strength lens still changes backdrop sampling on
                    // WASM. Keep the inactive indicator as a plain glass fill.
                    if (lensSupported && effectProgress > 0.001f) {
                        lens(
                            refractionHeight = lerp(
                                optics.indicatorRefractionHeightMin.toPx(),
                                optics.indicatorRefractionHeightMax.toPx(),
                                effectProgress
                            ),
                            refractionAmount = optics.indicatorRefractionAmount.toPx() * effectProgress,
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
                    val rawProgress = state.pressProgress
                    val visualProgress = rawProgress.coerceIn(0f, 1f)
                    val elasticProgress = rawProgress.coerceIn(-0.45f, 1f)
                    val scale = lerp(1f, physics.indicatorActiveScale, visualProgress)
                    val widthScale = lerp(1f, dimensions.indicatorActiveWidthScale, visualProgress)
                    val heightScale = lerp(
                        1f,
                        dimensions.indicatorActiveHeightScale,
                        elasticProgress.coerceIn(0f, 1f)
                    )
                    var sX = scale * widthScale
                    var sY = scale * heightScale

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
                    val visualProgress = state.pressProgress.coerceIn(0f, 1f)
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
            // Keep animated border alpha in the draw phase so pointer frames
            // do not recompose the indicator's backdrop pipeline.
            .drawWithContent {
                drawContent()
                val alpha = state.pressProgress.coerceIn(0f, 1f)
                val strokeWidth = dimensions.indicatorBorderWidth.toPx()
                if (alpha > 0.001f && strokeWidth > 0f) {
                    val inset = strokeWidth / 2f
                    val borderSize = Size(
                        width = (size.width - strokeWidth).coerceAtLeast(0f),
                        height = (size.height - strokeWidth).coerceAtLeast(0f)
                    )
                    val currentShape = if (state.pressProgress > 0.001f) {
                        activeIndicatorShape
                    } else {
                        inactiveIndicatorShape
                    }
                    if (currentShape is Capsule) {
                        drawRoundRect(
                            color = colors.indicatorBorderColor.copy(
                                alpha = colors.indicatorBorderColor.alpha * alpha
                            ),
                            topLeft = Offset(inset, inset),
                            size = borderSize,
                            cornerRadius = CornerRadius(borderSize.height / 2f, borderSize.height / 2f),
                            style = Stroke(width = strokeWidth)
                        )
                    } else {
                        val borderOutline = currentShape.createOutline(borderSize, layoutDirection, this)
                        val borderPath = Path().apply {
                            addOutline(borderOutline)
                            translate(Offset(inset, inset))
                        }
                        drawPath(
                            path = borderPath,
                            color = colors.indicatorBorderColor.copy(
                                alpha = colors.indicatorBorderColor.alpha * alpha
                            ),
                            style = Stroke(width = strokeWidth)
                        )
                    }
                }
            }
    )
}
