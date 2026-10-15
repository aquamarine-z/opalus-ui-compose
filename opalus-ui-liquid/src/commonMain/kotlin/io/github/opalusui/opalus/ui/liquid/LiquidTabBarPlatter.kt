package io.github.opalusui.opalus.ui.liquid

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawWithContent
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.addOutline
import androidx.compose.ui.graphics.drawscope.clipPath
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.util.fastCoerceIn
import androidx.compose.ui.util.lerp
import kotlin.math.abs
import com.kyant.backdrop.Backdrop
import com.kyant.backdrop.backdrops.LayerBackdrop
import com.kyant.backdrop.backdrops.layerBackdrop
import com.kyant.backdrop.drawBackdrop
import com.kyant.backdrop.effects.blur
import com.kyant.backdrop.effects.lens
import com.kyant.backdrop.highlight.Highlight
import com.kyant.shapes.Capsule

/**
 * iOS HIG 规范：底盘实体组件 (LiquidTabBarPlatter)
 */
@Composable
internal fun <T> LiquidTabBarPlatter(
    dockBackdrop: LayerBackdrop,
    backdrop: Backdrop,
    items: List<T>,
    colors: LiquidTabBarColors,
    dimensions: LiquidTabBarDimensions,
    optics: LiquidTabBarOptics,
    physics: LiquidTabBarPhysics,
    state: LiquidTabBarState,
    tabWidth: Float,
    isLtr: Boolean,
    barShape: Shape,
    inactiveIndicatorShape: Shape,
    activeIndicatorShape: Shape,
    modifier: Modifier = Modifier,
    inactiveItem: @Composable (index: Int, item: T) -> Unit
) {
    Row(
        modifier = modifier
            .layerBackdrop(dockBackdrop)
            .drawBackdrop(
                backdrop = backdrop,
                shape = { barShape },
                effects = {
                    blur(optics.platterBlur.toPx())
                    if (barShape.supportsLiquidLens()) {
                        lens(
                            refractionHeight = optics.platterRefractionHeight.toPx(),
                            refractionAmount = optics.platterRefractionAmount.toPx(),
                            depthEffect = true,
                            chromaticAberration = optics.chromaticAberration
                        )
                    }
                },
                highlight = {
                    Highlight.Default.copy(alpha = optics.platterHighlightAlpha)
                },
                onDrawSurface = {
                    if (barShape is Capsule) {
                        drawRoundRect(
                            color = colors.platterColor,
                            cornerRadius = CornerRadius(size.height / 2f, size.height / 2f)
                        )
                    } else {
                        val surfaceOutline = barShape.createOutline(size, layoutDirection, this)
                        val surfacePath = Path().apply {
                            addOutline(surfaceOutline)
                        }
                        drawPath(
                            path = surfacePath,
                            color = colors.platterColor,
                        )
                    }
                }
            )
            .border(
                width = dimensions.borderWidth,
                color = colors.platterBorderColor,
                shape = barShape
            )
            .height(dimensions.platterHeight)
            .fillMaxWidth()
            .padding(horizontal = dimensions.horizontalPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
        val indicatorShapeTransition = rememberIndicatorShapeTransition(
            inactiveShape = inactiveIndicatorShape,
            activeShape = activeIndicatorShape
        )
        // 反向几何遮罩：在水滴物理覆盖区域内，底层不绘制灰色按钮文字和图标，彻底杜绝重影
        val dropletPath = remember { Path() }
        Box(
            modifier = Modifier
                .fillMaxSize()
                .drawWithContent {
                    val rawProgress = state.pressProgress
                    val visualProgress = rawProgress.coerceIn(0f, 1f)
                    val elasticProgress = rawProgress.coerceIn(-0.45f, 1f)
                    val currentDropletHeight = lerp(
                        dimensions.indicatorIdleHeight.toPx(),
                        dimensions.indicatorActiveHeight.toPx(),
                        elasticProgress.coerceIn(0f, 1f)
                    )
                    val scale = lerp(1f, physics.indicatorActiveScale, visualProgress)
                    val widthScale = lerp(1f, dimensions.indicatorActiveWidthScale, visualProgress)
                    var scaleX = scale * widthScale
                    var scaleY = 1f

                    if (elasticProgress < 0f) {
                        val bottomBounce = elasticProgress * physics.dropletElasticity
                        scaleX *= (1f - bottomBounce * 0.22f)
                        scaleY *= (1f + bottomBounce * 0.28f).coerceAtLeast(0.65f)
                    }

                    if (visualProgress > 0.001f) {
                        val speed = (abs(state.velocity) / 6f) * visualProgress
                        val stretch = (speed * physics.velocityHorizontalFactor)
                            .coerceIn(0f, physics.maxVelocityCoerce)
                        scaleX *= (1f + stretch)
                    }

                    val dropletX = if (isLtr) {
                        state.animatedValue * tabWidth
                    } else {
                        size.width - (state.animatedValue + 1f) * tabWidth
                    }

                    val centerX = dropletX + tabWidth / 2f
                    val halfWidth = (tabWidth * scaleX) / 2f
                    val left = centerX - halfWidth
                    val right = centerX + halfWidth

                    val centerY = size.height / 2f
                    val halfHeight = (currentDropletHeight * scaleY) / 2f
                    val top = centerY - halfHeight
                    val bottom = centerY + halfHeight

                    val currentIndicatorShape = indicatorShapeTransition.shape(state.shapeProgress)

                    dropletPath.reset()
                    if (currentIndicatorShape is InterpolatedIndicatorShape) {
                        currentIndicatorShape.populatePath(
                            path = dropletPath,
                            left = left,
                            top = top,
                            right = right,
                            bottom = bottom,
                            layoutDirection = layoutDirection,
                            density = this
                        )
                    } else if (currentIndicatorShape is CurvedCapsule) {
                        currentIndicatorShape.populatePath(
                            path = dropletPath,
                            left = left,
                            top = top,
                            right = right,
                            bottom = bottom,
                            density = this
                        )
                    } else if (currentIndicatorShape is Capsule) {
                        val r = minOf(halfHeight, (right - left) / 2f).coerceAtLeast(0f)
                        dropletPath.moveTo(left + r, top)
                        if (right - r > left + r) {
                            dropletPath.lineTo(right - r, top)
                        }
                        if (r > 0.001f) {
                            dropletPath.arcTo(
                                rect = Rect(right - 2f * r, top, right, top + 2f * r),
                                startAngleDegrees = -90f,
                                sweepAngleDegrees = 90f,
                                forceMoveTo = false
                            )
                        } else {
                            dropletPath.lineTo(right, top)
                        }
                        val yRightBot = bottom - r
                        val yRightTop = top + r
                        if (yRightBot > yRightTop + 0.001f) {
                            dropletPath.lineTo(right, yRightBot)
                        }
                        if (r > 0.001f) {
                            dropletPath.arcTo(
                                rect = Rect(right - 2f * r, bottom - 2f * r, right, bottom),
                                startAngleDegrees = 0f,
                                sweepAngleDegrees = 90f,
                                forceMoveTo = false
                            )
                        } else {
                            dropletPath.lineTo(right, bottom)
                        }
                        if (right - r > left + r) {
                            dropletPath.lineTo(left + r, bottom)
                        }
                        if (r > 0.001f) {
                            dropletPath.arcTo(
                                rect = Rect(left, bottom - 2f * r, left + 2f * r, bottom),
                                startAngleDegrees = 90f,
                                sweepAngleDegrees = 90f,
                                forceMoveTo = false
                            )
                        } else {
                            dropletPath.lineTo(left, bottom)
                        }
                        if (yRightBot > yRightTop + 0.001f) {
                            dropletPath.lineTo(left, yRightTop)
                        }
                        if (r > 0.001f) {
                            dropletPath.arcTo(
                                rect = Rect(left, top, left + 2f * r, top + 2f * r),
                                startAngleDegrees = 180f,
                                sweepAngleDegrees = 90f,
                                forceMoveTo = false
                            )
                        } else {
                            dropletPath.lineTo(left, top)
                        }
                        dropletPath.close()
                    } else {
                        val dropletSize = Size(
                            width = (right - left).coerceAtLeast(0f),
                            height = (bottom - top).coerceAtLeast(0f)
                        )
                        val dropletOutline = currentIndicatorShape.createOutline(
                            size = dropletSize,
                            layoutDirection = layoutDirection,
                            density = this
                        )
                        dropletPath.addOutline(dropletOutline)
                        dropletPath.translate(Offset(left, top))
                    }

                    clipPath(dropletPath, clipOp = ClipOp.Difference) {
                        this@drawWithContent.drawContent()
                    }
                }
        ) {
            Row(
                modifier = Modifier.fillMaxSize(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEachIndexed { index, item ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight()
                            .graphicsLayer {
                                val scale = lerp(1f, physics.itemActiveScale, state.pressProgress.coerceIn(0f, 1f))
                                scaleX = scale
                                scaleY = scale
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        androidx.compose.runtime.CompositionLocalProvider(
                            androidx.compose.material3.LocalContentColor provides colors.inactiveItemColor
                        ) {
                            inactiveItem(index, item)
                        }
                    }
                }
            }
        }
    }
}
