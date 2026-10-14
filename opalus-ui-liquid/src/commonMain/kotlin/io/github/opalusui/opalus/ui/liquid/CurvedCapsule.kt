package io.github.opalusui.opalus.ui.liquid

import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.runtime.Immutable
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp

private class CapsuleCapCornerSize(private val curvature: Dp) : CornerSize {
    override fun toPx(shapeSize: Size, density: Density): Float {
        val h = shapeSize.height
        val c = with(density) { curvature.toPx() }.coerceIn(0f, (h * 0.20f).coerceAtLeast(0f))
        return ((h - 2f * c) / 2f).coerceAtLeast(0f)
    }
}

/**
 * 有机微弧流体胶囊 (CurvedCapsule)
 *
 * 相比两端半圆、上下为完全水平直线的传统标准胶囊 (Capsule)，
 * CurvedCapsule 的上下边缘具有极其微妙自然的外向有机微弧度 (curvature)。
 * 采用严格的 4-圆弧连续凸几何模型 (Exact G1 continuous 4-arc convex oval)，
 * 彻底消除中间尖峰与肩膀拐点，全轮廓各点曲率严格大于 0 且平滑连续，
 * 在流体玻璃态下还原真实水滴在表面张力下的微饱满凸起质感。
 *
 * 继承自 [CornerBasedShape]，与 Backdrop 的 SDF 透镜着色器 (Lens)
 * 100% 硬件加速兼容。
 */
@Immutable
class CurvedCapsule(
    val curvature: Dp = 1.0.dp
) : CornerBasedShape(
    topStart = CapsuleCapCornerSize(curvature),
    topEnd = CapsuleCapCornerSize(curvature),
    bottomEnd = CapsuleCapCornerSize(curvature),
    bottomStart = CapsuleCapCornerSize(curvature)
) {

    // 缓存单次生成的 Outline 与 Path，避免在尺寸未变时的重复计算与内存分配
    private var cachedWidth: Float = -1f
    private var cachedHeight: Float = -1f
    private var cachedRadius: Float = -1f
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

        if (w == cachedWidth && h == cachedHeight && topStart == cachedRadius && cachedOutline != null) {
            return cachedOutline!!
        }

        val c = (h / 2f - topStart).coerceIn(0f, (h * 0.20f).coerceAtLeast(0f))
        val outline = if (c <= 0.001f) {
            Outline.Rounded(
                RoundRect(
                    left = 0f,
                    top = 0f,
                    right = w,
                    bottom = h,
                    cornerRadius = CornerRadius(topStart, topStart)
                )
            )
        } else {
            cachedPath.reset()
            populatePath(cachedPath, 0f, 0f, w, h, c)
            Outline.Generic(cachedPath)
        }

        cachedWidth = w
        cachedHeight = h
        cachedRadius = topStart
        cachedOutline = outline
        return outline
    }

    /**
     * 直接向外部提供的 [Path] 填充几何轮廓，零对象分配。
     * 可供 Platter 底盘遮罩与 SelectionIndicator 边框绘制直接复用。
     */
    fun populatePath(
        path: Path,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        density: Density
    ) {
        val h = bottom - top
        val c = with(density) { curvature.toPx() }.coerceIn(0f, (h * 0.20f).coerceAtLeast(0f))
        populatePath(path, left, top, right, bottom, c)
    }

    private fun populatePath(
        path: Path,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        c: Float
    ) {
        val w = right - left
        val h = bottom - top
        if (w <= 0f || h <= 0f) return

        if (c <= 0.001f) {
            val r = (h / 2f).coerceAtLeast(0f)
            path.addRoundRect(
                RoundRect(
                    left = left,
                    top = top,
                    right = right,
                    bottom = bottom,
                    cornerRadius = CornerRadius(r, r)
                )
            )
            return
        }

        val clampedC = c.coerceIn(0f, (h * 0.20f).coerceAtLeast(0f))
        val r = (h / 2f - clampedC).coerceAtLeast(0.001f)
        val rCap = minOf(r, w / 2f)

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

        // 2. 右端半圆弧：圆心位于 (right - rCap, midY)，从 (-90° + theta) 顺时针扫至 (90° - theta)
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

        // 4. 左端半圆弧：圆心位于 (left + rCap, midY)，从 (90° + theta) 顺时针扫至 (270° - theta)
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
        if (other !is CurvedCapsule) return false
        return curvature == other.curvature
    }

    override fun hashCode(): Int = curvature.hashCode()

    override fun toString(): String = "CurvedCapsule(curvature=$curvature)"
}
