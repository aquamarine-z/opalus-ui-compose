package io.github.opalusui.opalus.ui.liquid

import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.remember
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.util.lerp
import com.kyant.shapes.RoundedRectangularShape

/**
 * 判断形状是否支持 Kyant Backdrop 的 SDF 硬件加速透镜 (Lens) 效果。
 * Kyant Backdrop 对 RoundedRectangularShape 与 CornerBasedShape 内置了完整的 SDF 半径求值。
 */
internal fun Shape.supportsLiquidLens(): Boolean =
    this is CornerBasedShape || this is RoundedRectangularShape

/**
 * 判断形状是否支持连续几何参数级丝滑插值 (圆角矩形、胶囊、微弧胶囊等)。
 */
internal fun Shape.supportsRoundedInterpolation(): Boolean =
    this is CornerBasedShape || this is RoundedRectangularShape

@Composable
internal fun rememberIndicatorShapeTransition(
    inactiveShape: Shape,
    activeShape: Shape
): IndicatorShapeTransition = remember(inactiveShape, activeShape) {
    IndicatorShapeTransition(inactiveShape, activeShape)
}

/**
 * 水滴指示器形状过渡器。
 * 内部复用几何缓存与 Shape 实例，杜绝每一帧交互过程中的内存垃圾分配。
 */
internal class IndicatorShapeTransition(
    val inactiveShape: Shape,
    val activeShape: Shape
) {
    private val canInterpolate = inactiveShape.supportsRoundedInterpolation() &&
            activeShape.supportsRoundedInterpolation()

    val interpolatedShape: InterpolatedIndicatorShape? = if (canInterpolate && inactiveShape != activeShape) {
        InterpolatedIndicatorShape(inactiveShape, activeShape)
    } else {
        null
    }

    fun shape(progress: Float): Shape {
        val nextFraction = progress.coerceIn(0f, 1f)
        if (nextFraction <= 0f || inactiveShape == activeShape) return inactiveShape
        if (nextFraction >= 1f) return activeShape

        val shape = interpolatedShape
        if (shape == null) {
            return if (nextFraction < 0.5f) inactiveShape else activeShape
        }

        shape.fraction = nextFraction
        return shape
    }
}

/**
 * 4 角圆角半径数据
 */
@Immutable
internal data class RoundedCornerRadii(
    val topLeft: Float,
    val topRight: Float,
    val bottomRight: Float,
    val bottomLeft: Float
)

private enum class Corner {
    TopStart,
    TopEnd,
    BottomEnd,
    BottomStart
}

internal class FractionHolder(var fraction: Float = 0f)

private class InterpolatedCornerSize(
    private val inactiveShape: Shape,
    private val activeShape: Shape,
    private val corner: Corner,
    private val fractionHolder: FractionHolder
) : CornerSize {
    override fun toPx(shapeSize: Size, density: Density): Float {
        val startRadii = inactiveShape.extractCornerRadii(shapeSize, LayoutDirection.Ltr, density)
        val endRadii = activeShape.extractCornerRadii(shapeSize, LayoutDirection.Ltr, density)
        val startRadius = when (corner) {
            Corner.TopStart -> startRadii.topLeft
            Corner.TopEnd -> startRadii.topRight
            Corner.BottomEnd -> startRadii.bottomRight
            Corner.BottomStart -> startRadii.bottomLeft
        }
        val endRadius = when (corner) {
            Corner.TopStart -> endRadii.topLeft
            Corner.TopEnd -> endRadii.topRight
            Corner.BottomEnd -> endRadii.bottomRight
            Corner.BottomStart -> endRadii.bottomLeft
        }
        return lerp(startRadius, endRadius, fractionHolder.fraction)
    }
}

/**
 * 丝滑连续插值形状。
 * 继承自 [CornerBasedShape]，与 Backdrop SDF Lens 着色器 100% 硬件加速兼容，
 * 并支持向外部 [Path] 零开销直接填充几何数据。
 */
internal class InterpolatedIndicatorShape private constructor(
    val inactiveShape: Shape,
    val activeShape: Shape,
    private val fractionHolder: FractionHolder
) : CornerBasedShape(
    topStart = InterpolatedCornerSize(inactiveShape, activeShape, Corner.TopStart, fractionHolder),
    topEnd = InterpolatedCornerSize(inactiveShape, activeShape, Corner.TopEnd, fractionHolder),
    bottomEnd = InterpolatedCornerSize(inactiveShape, activeShape, Corner.BottomEnd, fractionHolder),
    bottomStart = InterpolatedCornerSize(inactiveShape, activeShape, Corner.BottomStart, fractionHolder)
) {
    constructor(inactiveShape: Shape, activeShape: Shape) : this(
        inactiveShape = inactiveShape,
        activeShape = activeShape,
        fractionHolder = FractionHolder(0f)
    )

    var fraction: Float
        get() = fractionHolder.fraction
        set(value) {
            fractionHolder.fraction = value
        }

    // 内部轮廓缓存
    private var cachedWidth: Float = -1f
    private var cachedHeight: Float = -1f
    private var cachedFraction: Float = -1f
    private var cachedOutline: Outline? = null
    private val cachedPath = Path()

    override fun copy(
        topStart: CornerSize,
        topEnd: CornerSize,
        bottomEnd: CornerSize,
        bottomStart: CornerSize
    ): CornerBasedShape = this

    override fun createOutline(
        size: Size,
        topStart: Float,
        topEnd: Float,
        bottomEnd: Float,
        bottomStart: Float,
        layoutDirection: LayoutDirection
    ): Outline {
        val w = size.width
        val h = size.height
        if (w <= 0f || h <= 0f) return Outline.Rectangle(Rect.Zero)

        val topLeft = if (layoutDirection == LayoutDirection.Ltr) topStart else topEnd
        val topRight = if (layoutDirection == LayoutDirection.Ltr) topEnd else topStart
        val bottomRight = if (layoutDirection == LayoutDirection.Ltr) bottomEnd else bottomStart
        val bottomLeft = if (layoutDirection == LayoutDirection.Ltr) bottomStart else bottomEnd
        val corners = RoundedCornerRadii(topLeft, topRight, bottomRight, bottomLeft)

        val curFraction = fractionHolder.fraction
        val inactiveCurvatureDp = (inactiveShape as? CurvedCapsule)?.curvature?.value ?: 0f
        val activeCurvatureDp = (activeShape as? CurvedCapsule)?.curvature?.value ?: 0f
        val c = if (inactiveCurvatureDp > 0f || activeCurvatureDp > 0f) {
            val maxCap = minOf(topLeft, topRight, bottomRight, bottomLeft)
            val maxC = (h / 2f - maxCap).coerceIn(0f, (h * 0.45f).coerceAtLeast(0f))
            lerp(
                if (inactiveCurvatureDp > 0f) maxC else 0f,
                if (activeCurvatureDp > 0f) maxC else 0f,
                curFraction
            )
        } else {
            0f
        }

        if (c <= 0.001f) {
            return Outline.Rounded(
                RoundRect(
                    left = 0f,
                    top = 0f,
                    right = w,
                    bottom = h,
                    topLeftCornerRadius = CornerRadius(topLeft, topLeft),
                    topRightCornerRadius = CornerRadius(topRight, topRight),
                    bottomRightCornerRadius = CornerRadius(bottomRight, bottomRight),
                    bottomLeftCornerRadius = CornerRadius(bottomLeft, bottomLeft)
                )
            )
        }

        if (w == cachedWidth && h == cachedHeight && curFraction == cachedFraction && cachedOutline != null) {
            return cachedOutline!!
        }

        cachedPath.reset()
        populatePath(
            path = cachedPath,
            left = 0f,
            top = 0f,
            right = w,
            bottom = h,
            c = c,
            corners = corners
        )
        val outline = Outline.Generic(cachedPath)
        cachedWidth = w
        cachedHeight = h
        cachedFraction = curFraction
        cachedOutline = outline
        return outline
    }

    /**
     * 直接向外部 [Path] 填充插值几何数据，零对象分配。
     */
    fun populatePath(
        path: Path,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        layoutDirection: LayoutDirection,
        density: Density
    ) {
        val w = right - left
        val h = bottom - top
        if (w <= 0f || h <= 0f) return

        val inactiveCurvature = inactiveShape.extractCurvaturePx(density)
        val activeCurvature = activeShape.extractCurvaturePx(density)
        val c = lerp(inactiveCurvature, activeCurvature, fractionHolder.fraction).coerceIn(0f, (h * 0.45f).coerceAtLeast(0f))

        val size = Size(w, h)
        val isLtr = layoutDirection == LayoutDirection.Ltr
        val ts = topStart.toPx(size, density)
        val te = topEnd.toPx(size, density)
        val be = bottomEnd.toPx(size, density)
        val bs = bottomStart.toPx(size, density)
        val corners = RoundedCornerRadii(
            topLeft = if (isLtr) ts else te,
            topRight = if (isLtr) te else ts,
            bottomRight = if (isLtr) be else bs,
            bottomLeft = if (isLtr) bs else be
        )

        if (c <= 0.001f) {
            path.addRoundRect(
                RoundRect(
                    left = left,
                    top = top,
                    right = right,
                    bottom = bottom,
                    topLeftCornerRadius = CornerRadius(corners.topLeft, corners.topLeft),
                    topRightCornerRadius = CornerRadius(corners.topRight, corners.topRight),
                    bottomRightCornerRadius = CornerRadius(corners.bottomRight, corners.bottomRight),
                    bottomLeftCornerRadius = CornerRadius(corners.bottomLeft, corners.bottomLeft)
                )
            )
            return
        }

        populatePath(path, left, top, right, bottom, c, corners)
    }

    private fun populatePath(
        path: Path,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        c: Float,
        corners: RoundedCornerRadii
    ) {
        val w = right - left
        val h = bottom - top
        if (w <= 0f || h <= 0f) return

        val rL = minOf(maxOf(corners.topLeft, corners.bottomLeft), w / 2f)
        val rR = minOf(maxOf(corners.topRight, corners.bottomRight), w / 2f)
        val midX = left + w / 2f
        val dxL = (w / 2f - rL).coerceAtLeast(0f)
        val dxR = (w / 2f - rR).coerceAtLeast(0f)

        // 顶弧：从左端点至中心隆起顶点，再至右端点
        path.moveTo(left + rL, top + c)
        path.cubicTo(
            x1 = left + rL + dxL * 0.55f,
            y1 = top + c,
            x2 = midX - dxL * 0.55f,
            y2 = top,
            x3 = midX,
            y3 = top
        )
        path.cubicTo(
            x1 = midX + dxR * 0.55f,
            y1 = top,
            x2 = right - rR - dxR * 0.55f,
            y2 = top + c,
            x3 = right - rR,
            y3 = top + c
        )

        // 右端圆角/半圆
        path.arcTo(
            rect = Rect(
                left = right - 2f * rR,
                top = top + h / 2f - rR,
                right = right,
                bottom = top + h / 2f + rR
            ),
            startAngleDegrees = -90f,
            sweepAngleDegrees = 180f,
            forceMoveTo = false
        )

        // 底弧：从右端点至中心底部顶点，再至左端点
        path.cubicTo(
            x1 = right - rR - dxR * 0.55f,
            y1 = bottom - c,
            x2 = midX + dxR * 0.55f,
            y2 = bottom,
            x3 = midX,
            y3 = bottom
        )
        path.cubicTo(
            x1 = midX - dxL * 0.55f,
            y1 = bottom,
            x2 = left + rL + dxL * 0.55f,
            y2 = bottom - c,
            x3 = left + rL,
            y3 = bottom - c
        )

        // 左端圆角/半圆
        path.arcTo(
            rect = Rect(
                left = left,
                top = top + h / 2f - rL,
                right = left + 2f * rL,
                bottom = top + h / 2f + rL
            ),
            startAngleDegrees = 90f,
            sweepAngleDegrees = 180f,
            forceMoveTo = false
        )

        path.close()
    }
}

/**
 * 提取任意形状的边缘微弧度 (像素)。
 */
private fun Shape.extractCurvaturePx(density: Density): Float =
    if (this is CurvedCapsule) with(density) { curvature.toPx() } else 0f

/**
 * 提取任意形状的 4 角圆角半径。
 */
private fun Shape.extractCornerRadii(
    size: Size,
    layoutDirection: LayoutDirection,
    density: Density
): RoundedCornerRadii {
    if (this is RoundedRectangularShape) {
        val c = corners(size, layoutDirection, density)
        return RoundedCornerRadii(c.topLeft, c.topRight, c.bottomRight, c.bottomLeft)
    }

    if (this is CornerBasedShape) {
        val isLtr = layoutDirection == LayoutDirection.Ltr
        val ts = topStart.toPx(size, density)
        val te = topEnd.toPx(size, density)
        val be = bottomEnd.toPx(size, density)
        val bs = bottomStart.toPx(size, density)
        return RoundedCornerRadii(
            topLeft = if (isLtr) ts else te,
            topRight = if (isLtr) te else ts,
            bottomRight = if (isLtr) be else bs,
            bottomLeft = if (isLtr) bs else be
        )
    }

    return when (val outline = createOutline(size, layoutDirection, density)) {
        is Outline.Rounded -> {
            val r = outline.roundRect
            RoundedCornerRadii(
                topLeft = maxOf(r.topLeftCornerRadius.x, r.topLeftCornerRadius.y),
                topRight = maxOf(r.topRightCornerRadius.x, r.topRightCornerRadius.y),
                bottomRight = maxOf(r.bottomRightCornerRadius.x, r.bottomRightCornerRadius.y),
                bottomLeft = maxOf(r.bottomLeftCornerRadius.x, r.bottomLeftCornerRadius.y)
            )
        }
        else -> RoundedCornerRadii(0f, 0f, 0f, 0f)
    }
}
