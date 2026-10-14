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
        val c = with(density) { curvature.toPx() }.coerceIn(0f, (h * 0.45f).coerceAtLeast(0f))
        return ((h - 2f * c) / 2f).coerceAtLeast(0f)
    }
}

/**
 * 有机微弧流体胶囊 (CurvedCapsule)
 *
 * 相比两端半圆、上下为完全水平直线的传统标准胶囊 (Capsule)，
 * CurvedCapsule 的上下边缘具有极其微妙自然的外向有机微弧度 (curvature)。
 * 在流体玻璃态下，这一微小弧度能够还原真实水滴在表面张力下的微饱满凸起质感，
 * 消除生硬的工业平直感，同时与两端半圆在切线方向保持严格 C1 连续光滑衔接。
 *
 * 继承自 [CornerBasedShape]，与 Backdrop 的 SDF 透镜着色器 (Lens)
 * 100% 硬件加速兼容。
 */
@Immutable
class CurvedCapsule(
    val curvature: Dp = 1.5.dp
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

        val c = (h / 2f - topStart).coerceIn(0f, (h * 0.45f).coerceAtLeast(0f))
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
        val c = with(density) { curvature.toPx() }.coerceIn(0f, (h * 0.45f).coerceAtLeast(0f))
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

        val r = ((h - 2f * c) / 2f).coerceAtLeast(0f)
        val rCap = minOf(r, w / 2f)
        val dx = (w / 2f - rCap).coerceAtLeast(0f)
        val midX = left + w / 2f

        // 1. 移动至顶弧起点 (左半圆端点处，切线方向严格水平 (1, 0))
        path.moveTo(left + rCap, top + c)

        // 2. 顶弧左半段：从 (left + rCap, top + c) 向上隆起至中心顶点 (midX, top)
        path.cubicTo(
            x1 = left + rCap + dx * 0.55f,
            y1 = top + c,
            x2 = midX - dx * 0.55f,
            y2 = top,
            x3 = midX,
            y3 = top
        )

        // 3. 顶弧右半段：从 (midX, top) 平滑过渡回右半圆端点 (right - rCap, top + c)
        path.cubicTo(
            x1 = midX + dx * 0.55f,
            y1 = top,
            x2 = right - rCap - dx * 0.55f,
            y2 = top + c,
            x3 = right - rCap,
            y3 = top + c
        )

        // 4. 右端半圆：从 -90度 顺时针旋转 180度 到 90度
        path.arcTo(
            rect = Rect(
                left = right - 2f * rCap,
                top = top + h / 2f - rCap,
                right = right,
                bottom = top + h / 2f + rCap
            ),
            startAngleDegrees = -90f,
            sweepAngleDegrees = 180f,
            forceMoveTo = false
        )

        // 5. 底弧右半段：从 (right - rCap, bottom - c) 向下隆起至中心底部顶点 (midX, bottom)
        path.cubicTo(
            x1 = right - rCap - dx * 0.55f,
            y1 = bottom - c,
            x2 = midX + dx * 0.55f,
            y2 = bottom,
            x3 = midX,
            y3 = bottom
        )

        // 6. 底弧左半段：从 (midX, bottom) 平滑过渡回左半圆底部端点 (left + rCap, bottom - c)
        path.cubicTo(
            x1 = midX - dx * 0.55f,
            y1 = bottom,
            x2 = left + rCap + dx * 0.55f,
            y2 = bottom - c,
            x3 = left + rCap,
            y3 = bottom - c
        )

        // 7. 左端半圆：从 90度 顺时针旋转 180度 到 270度
        path.arcTo(
            rect = Rect(
                left = left,
                top = top + h / 2f - rCap,
                right = left + 2f * rCap,
                bottom = top + h / 2f + rCap
            ),
            startAngleDegrees = 90f,
            sweepAngleDegrees = 180f,
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
