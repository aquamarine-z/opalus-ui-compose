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
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.graphics.ClipOp
import androidx.compose.ui.graphics.Path
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
    modifier: Modifier = Modifier,
    inactiveItem: @Composable (index: Int, item: T) -> Unit
) {
    Row(
        modifier = modifier
            .layerBackdrop(dockBackdrop)
            .drawBackdrop(
                backdrop = backdrop,
                shape = { Capsule() },
                effects = {
                    blur(optics.platterBlur.toPx())
                    lens(
                        refractionHeight = optics.platterRefractionHeight.toPx(),
                        refractionAmount = optics.platterRefractionAmount.toPx(),
                        depthEffect = true,
                        chromaticAberration = optics.chromaticAberration
                    )
                },
                highlight = {
                    Highlight.Default.copy(alpha = optics.platterHighlightAlpha)
                },
                onDrawSurface = {
                    drawRoundRect(
                        color = colors.platterColor,
                        cornerRadius = CornerRadius(size.height / 2f, size.height / 2f)
                    )
                }
            )
            .border(
                width = dimensions.borderWidth,
                color = colors.platterBorderColor,
                shape = Capsule()
            )
            .height(dimensions.platterHeight)
            .fillMaxWidth()
            .padding(horizontal = dimensions.horizontalPadding),
        verticalAlignment = Alignment.CenterVertically
    ) {
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

                    dropletPath.reset()
                    dropletPath.addRoundRect(
                        RoundRect(
                            left = left,
                            top = top,
                            right = right,
                            bottom = bottom,
                            radiusX = halfHeight,
                            radiusY = halfHeight
                        )
                    )

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
