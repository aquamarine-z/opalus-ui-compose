package io.github.opalusui.opalus.ui.liquid

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.addOutline
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.lerp
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
    inactiveIndicatorShape: Shape,
    activeIndicatorShape: Shape,
    modifier: Modifier = Modifier
) {
    val combinedBackdrop = rememberCombinedBackdrop(backdrop, dockBackdrop, activeButtonsBackdrop)
    val lensSupported = activeIndicatorShape.supportsLiquidLens()
    val indicatorShapeTransition = rememberIndicatorShapeTransition(
        inactiveShape = inactiveIndicatorShape,
        activeShape = activeIndicatorShape
    )
    val borderPath = remember { Path() }

    val indicatorModifier = modifier
        .padding(horizontal = dimensions.horizontalPadding)
        .graphicsLayer {
            translationX =
                if (isLtr) state.animatedValue * tabWidth
                else size.width - (state.animatedValue + 1f) * tabWidth
        }
        .height(dimensions.indicatorIdleHeight)
        .fillMaxWidth(1f / tabsCount)

    Box(
        modifier = indicatorModifier
            .drawBackdrop(
                backdrop = combinedBackdrop,
                shape = {
                    indicatorShapeTransition.shape(state.shapeProgress)
                },
                effects = {
                    val visualProgress = state.pressProgress.coerceIn(0f, 1f)
                    // Keep a tiny non-zero refractionAmount (0.004f) when idle so
                    // Skia/DirectX compiles and attaches the lens RenderEffect
                    // once during window initialization. This prevents a ~0.5s EDT
                    // freeze when the user touches and drags for the first time.
                    if (lensSupported) {
                        lens(
                            refractionHeight = lerp(
                                optics.indicatorRefractionHeightMin.toPx(),
                                optics.indicatorRefractionHeightMax.toPx(),
                                visualProgress
                            ),
                            refractionAmount = optics.indicatorRefractionAmount.toPx() * visualProgress.coerceAtLeast(0.004f),
                            depthEffect = true,
                            chromaticAberration = optics.chromaticAberration
                        )
                    }
                },
                highlight = {
                    val visualProgress = state.pressProgress.coerceIn(0f, 1f)
                    Highlight.Default.copy(
                        alpha = (optics.indicatorHighlightAlpha * visualProgress).coerceAtLeast(0.004f)
                    )
                },
                shadow = {
                    val visualProgress = state.pressProgress.coerceIn(0f, 1f)
                    Shadow(
                        radius = optics.indicatorShadowRadius,
                        offset = DpOffset(0.dp, optics.indicatorShadowOffsetY * visualProgress),
                        alpha = (optics.indicatorShadowAlpha * visualProgress).coerceAtLeast(0.004f)
                    )
                },
                innerShadow = {
                    val visualProgress = state.pressProgress.coerceIn(0f, 1f)
                    // Keep constant blur radius so the blur ImageFilter is compiled
                    // and cached on the GraphicsLayer once at startup. Only modulate
                    // alpha dynamically, with a 0.004f floor to ensure Direct3D compiles
                    // the inner shadow blur effect on window load.
                    InnerShadow(
                        radius = optics.indicatorInnerShadowRadius,
                        alpha = (optics.indicatorInnerShadowAlpha * visualProgress).coerceAtLeast(0.004f)
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
                val strokeWidth = dimensions.indicatorBorderWidth.toPx()
                if (strokeWidth > 0f) {
                    val inset = strokeWidth / 2f
                    val borderSize = Size(
                        width = (size.width - strokeWidth).coerceAtLeast(0f),
                        height = (size.height - strokeWidth).coerceAtLeast(0f)
                    )
                    val alpha = state.pressProgress.coerceIn(0f, 1f)
                    val currentShape = indicatorShapeTransition.shape(state.shapeProgress)
                    borderPath.reset()
                    if (currentShape is InterpolatedIndicatorShape) {
                        currentShape.populatePath(
                            path = borderPath,
                            left = inset,
                            top = inset,
                            right = inset + borderSize.width,
                            bottom = inset + borderSize.height,
                            layoutDirection = layoutDirection,
                            density = this
                        )
                    } else if (currentShape is CurvedCapsule) {
                        currentShape.populatePath(
                            path = borderPath,
                            left = inset,
                            top = inset,
                            right = inset + borderSize.width,
                            bottom = inset + borderSize.height,
                            density = this
                        )
                    } else if (currentShape is Capsule) {
                        val r = minOf(borderSize.height / 2f, borderSize.width / 2f).coerceAtLeast(0f)
                        val left = inset
                        val top = inset
                        val right = inset + borderSize.width
                        val bottom = inset + borderSize.height
                        borderPath.moveTo(left + r, top)
                        if (right - r > left + r) {
                            borderPath.lineTo(right - r, top)
                        }
                        if (r > 0.001f) {
                            borderPath.arcTo(
                                rect = Rect(right - 2f * r, top, right, top + 2f * r),
                                startAngleDegrees = -90f,
                                sweepAngleDegrees = 90f,
                                forceMoveTo = false
                            )
                        } else {
                            borderPath.lineTo(right, top)
                        }
                        val yRightBot = bottom - r
                        val yRightTop = top + r
                        if (yRightBot > yRightTop + 0.001f) {
                            borderPath.lineTo(right, yRightBot)
                        }
                        if (r > 0.001f) {
                            borderPath.arcTo(
                                rect = Rect(right - 2f * r, bottom - 2f * r, right, bottom),
                                startAngleDegrees = 0f,
                                sweepAngleDegrees = 90f,
                                forceMoveTo = false
                            )
                        } else {
                            borderPath.lineTo(right, bottom)
                        }
                        if (right - r > left + r) {
                            borderPath.lineTo(left + r, bottom)
                        }
                        if (r > 0.001f) {
                            borderPath.arcTo(
                                rect = Rect(left, bottom - 2f * r, left + 2f * r, bottom),
                                startAngleDegrees = 90f,
                                sweepAngleDegrees = 90f,
                                forceMoveTo = false
                            )
                        } else {
                            borderPath.lineTo(left, bottom)
                        }
                        if (yRightBot > yRightTop + 0.001f) {
                            borderPath.lineTo(left, yRightTop)
                        }
                        if (r > 0.001f) {
                            borderPath.arcTo(
                                rect = Rect(left, top, left + 2f * r, top + 2f * r),
                                startAngleDegrees = 180f,
                                sweepAngleDegrees = 90f,
                                forceMoveTo = false
                            )
                        } else {
                            borderPath.lineTo(left, top)
                        }
                        borderPath.close()
                    } else {
                        val borderOutline = currentShape.createOutline(borderSize, layoutDirection, this)
                        borderPath.addOutline(borderOutline)
                        borderPath.translate(Offset(inset, inset))
                    }
                    val effectiveAlpha = (colors.indicatorBorderColor.alpha * alpha).coerceAtLeast(0.004f)
                    drawPath(
                        path = borderPath,
                        color = colors.indicatorBorderColor.copy(
                            alpha = effectiveAlpha
                        ),
                        style = Stroke(width = strokeWidth)
                    )
                }
            }
    )
}
