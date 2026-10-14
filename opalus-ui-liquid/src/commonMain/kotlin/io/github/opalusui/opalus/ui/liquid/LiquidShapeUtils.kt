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
 * 内部采用双缓冲 Ping-Pong 实例池，杜绝每一帧交互过程中的内存垃圾分配 (0 GC allocations)，
 * 同时通过保证相邻帧间 Shape 实例判定 areEqual 恒为 false，使得 Backdrop 每一帧均能重绘
 * 插值形态 Outline 与 SDF 硬件透镜，彻底解决从激活形态到非激活形态过渡卡顿、不线性的问题。
 */
internal class IndicatorShapeTransition(
    val inactiveShape: Shape,
    val activeShape: Shape
) {
    private val canInterpolate = inactiveShape.supportsRoundedInterpolation() &&
            activeShape.supportsRoundedInterpolation()

    private val shapeA = if (canInterpolate && inactiveShape != activeShape) {
        InterpolatedIndicatorShape(inactiveShape, activeShape, poolId = 0)
    } else null

    private val shapeB = if (canInterpolate && inactiveShape != activeShape) {
        InterpolatedIndicatorShape(inactiveShape, activeShape, poolId = 1)
    } else null

    private var toggle = false

    fun shape(progress: Float): Shape {
        val nextFraction = progress.coerceIn(0f, 1f)
        if (nextFraction <= 0.001f || inactiveShape == activeShape) return inactiveShape
        if (nextFraction >= 0.999f) return activeShape

        val a = shapeA
        val b = shapeB
        if (a == null || b == null) {
            return if (nextFraction < 0.5f) inactiveShape else activeShape
        }

        toggle = !toggle
        val current = if (toggle) a else b
        current.fraction = nextFraction
        return current
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
    val poolId: Int,
    private val fractionHolder: FractionHolder
) : CornerBasedShape(
    topStart = InterpolatedCornerSize(inactiveShape, activeShape, Corner.TopStart, fractionHolder),
    topEnd = InterpolatedCornerSize(inactiveShape, activeShape, Corner.TopEnd, fractionHolder),
    bottomEnd = InterpolatedCornerSize(inactiveShape, activeShape, Corner.BottomEnd, fractionHolder),
    bottomStart = InterpolatedCornerSize(inactiveShape, activeShape, Corner.BottomStart, fractionHolder)
) {
    constructor(inactiveShape: Shape, activeShape: Shape, poolId: Int) : this(
        inactiveShape = inactiveShape,
        activeShape = activeShape,
        poolId = poolId,
        fractionHolder = FractionHolder(0f)
    )

    var fraction: Float
        get() = fractionHolder.fraction
        set(value) {
            fractionHolder.fraction = value
        }

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

        val isLtr = layoutDirection == LayoutDirection.Ltr
        val corners = RoundedCornerRadii(
            topLeft = if (isLtr) topStart else topEnd,
            topRight = if (isLtr) topEnd else topStart,
            bottomRight = if (isLtr) bottomEnd else bottomStart,
            bottomLeft = if (isLtr) bottomStart else bottomEnd
        )

        val curFraction = fractionHolder.fraction
        val inactiveCurvatureDp = (inactiveShape as? CurvedCapsule)?.curvature?.value ?: 0f
        val activeCurvatureDp = (activeShape as? CurvedCapsule)?.curvature?.value ?: 0f
        val c = if (inactiveCurvatureDp > 0f || activeCurvatureDp > 0f) {
            val maxCap = minOf(topStart, topEnd, bottomEnd, bottomStart)
            val maxC = (h / 2f - maxCap).coerceIn(0f, (h * 0.20f).coerceAtLeast(0f))
            lerp(
                if (inactiveCurvatureDp > 0f) maxC else 0f,
                if (activeCurvatureDp > 0f) maxC else 0f,
                curFraction
            )
        } else 0f

        if (c <= 0.001f) {
            return Outline.Rounded(
                RoundRect(
                    left = 0f,
                    top = 0f,
                    right = w,
                    bottom = h,
                    topLeftCornerRadius = CornerRadius(corners.topLeft, corners.topLeft),
                    topRightCornerRadius = CornerRadius(corners.topRight, corners.topRight),
                    bottomRightCornerRadius = CornerRadius(corners.bottomRight, corners.bottomRight),
                    bottomLeftCornerRadius = CornerRadius(corners.bottomLeft, corners.bottomLeft)
                )
            )
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
        return Outline.Generic(cachedPath)
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
        val c = lerp(inactiveCurvature, activeCurvature, fractionHolder.fraction).coerceIn(0f, (h * 0.20f).coerceAtLeast(0f))

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

        val clampedC = c.coerceIn(0f, (h * 0.20f).coerceAtLeast(0f))
        val rL = minOf(maxOf(corners.topLeft, corners.bottomLeft), (h / 2f - clampedC).coerceAtLeast(0.001f), w / 2f)
        val rR = minOf(maxOf(corners.topRight, corners.bottomRight), (h / 2f - clampedC).coerceAtLeast(0.001f), w / 2f)
        val rCap = (rL + rR) / 2f

        if (w <= 2f * rCap + 0.001f) {
            path.addRoundRect(
                RoundRect(
                    left = left,
                    top = top,
                    right = right,
                    bottom = bottom,
                    cornerRadius = CornerRadius(rCap, rCap)
                )
            )
            return
        }

        val dx = w / 2f - rCap
        val midX = left + w / 2f
        val midY = top + h / 2f

        // 4-圆弧连续凸几何模型 (Exact G1 continuous 4-arc convex oval):
        // 彻底消除中间尖峰与肩膀拐点，全轮廓曲率连续且严格大于 0，上下微弧凸起自然饱满
        val rTop = (dx * dx) / (2f * clampedC) + (h - clampedC) / 2f
        val d = (dx * dx + clampedC * clampedC) / (2f * clampedC)
        val sinTheta = (dx / d).coerceIn(0f, 1f)
        val thetaRad = kotlin.math.asin(sinTheta)
        val thetaDeg = (thetaRad * 180.0 / kotlin.math.PI).toFloat()

        // 1. 顶微弧：圆心位于 (midX, top + rTop)，从 (-90° - theta) 顺时针扫至 (-90° + theta)
        path.arcTo(
            rect = Rect(
                left = midX - rTop,
                top = top,
                right = midX + rTop,
                bottom = top + 2f * rTop
            ),
            startAngleDegrees = -90f - thetaDeg,
            sweepAngleDegrees = 2f * thetaDeg,
            forceMoveTo = false
        )

        // 2. 右端圆角弧：圆心位于 (right - rCap, midY)，从 (-90° + theta) 顺时针扫至 (90° - theta)
        path.arcTo(
            rect = Rect(
                left = right - 2f * rCap,
                top = midY - rCap,
                right = right,
                bottom = midY + rCap
            ),
            startAngleDegrees = -90f + thetaDeg,
            sweepAngleDegrees = 180f - 2f * thetaDeg,
            forceMoveTo = false
        )

        // 3. 底微弧：圆心位于 (midX, bottom - rTop)，从 (90° - theta) 顺时针扫至 (90° + theta)
        path.arcTo(
            rect = Rect(
                left = midX - rTop,
                top = bottom - 2f * rTop,
                right = midX + rTop,
                bottom = bottom
            ),
            startAngleDegrees = 90f - thetaDeg,
            sweepAngleDegrees = 2f * thetaDeg,
            forceMoveTo = false
        )

        // 4. 左端圆角弧：圆心位于 (left + rCap, midY)，从 (90° + theta) 顺时针扫至 (270° - theta)
        path.arcTo(
            rect = Rect(
                left = left,
                top = midY - rCap,
                right = left + 2f * rCap,
                bottom = midY + rCap
            ),
            startAngleDegrees = 90f + thetaDeg,
            sweepAngleDegrees = 180f - 2f * thetaDeg,
            forceMoveTo = false
        )

        path.close()
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (other !is InterpolatedIndicatorShape) return false
        return poolId == other.poolId &&
                fraction == other.fraction &&
                inactiveShape == other.inactiveShape &&
                activeShape == other.activeShape
    }

    override fun hashCode(): Int {
        var result = inactiveShape.hashCode()
        result = 31 * result + activeShape.hashCode()
        result = 31 * result + poolId
        result = 31 * result + fraction.hashCode()
        return result
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
