package io.github.opalusui.opalus.ui.liquid

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

/**
 * iOS HIG 规范：流体导航栏颜色与材质设计令牌 (LiquidTabBarColors)
 */
@Immutable
data class LiquidTabBarColors(
    val platterColor: Color,
    val platterBorderColor: Color,
    val indicatorIdleColor: Color,
    val indicatorActiveTint: Color,
    val indicatorBorderColor: Color,
    val activeItemColor: Color,
    val inactiveItemColor: Color
)

/**
 * iOS HIG 规范：流体导航栏几何尺寸设计令牌 (LiquidTabBarDimensions)
 */
@Immutable
data class LiquidTabBarDimensions(
    val containerHeight: Dp = 84.dp,
    val platterHeight: Dp = 58.dp,
    val indicatorIdleHeight: Dp = 50.dp,
    val indicatorActiveHeight: Dp = 72.dp,
    val horizontalPadding: Dp = 4.dp,
    val borderWidth: Dp = 0.6.dp,
    val indicatorBorderWidth: Dp = 0.4.dp
)

/**
 * iOS HIG 规范：流体导航栏光学折射与透镜效果设计令牌 (LiquidTabBarOptics)
 */
@Immutable
data class LiquidTabBarOptics(
    // Platter 底盘光学
    val platterBlur: Dp = 1.dp,
    val platterRefractionHeight: Dp = 14.dp,
    val platterRefractionAmount: Dp = 10.dp,
    val platterHighlightAlpha: Float = 0.24f,

    // SelectionIndicator 选中水滴透镜光学
    val indicatorRefractionHeightMin: Dp = 6.dp,
    val indicatorRefractionHeightMax: Dp = 14.dp,
    val indicatorRefractionAmount: Dp = 8.dp,
    val indicatorHighlightAlpha: Float = 0.20f,
    val indicatorShadowRadius: Dp = 8.dp,
    val indicatorShadowOffsetY: Dp = 2.5.dp,
    val indicatorShadowAlpha: Float = 0.45f,
    val indicatorInnerShadowRadius: Dp = 4.dp,
    val indicatorInnerShadowAlpha: Float = 0.28f,
    val chromaticAberration: Boolean = false
)

/**
 * iOS HIG 规范：流体导航栏物理阻尼与手势动力学设计令牌 (LiquidTabBarPhysics)
 */
@Immutable
data class LiquidTabBarPhysics(
    val maxOverhang: Float = 0.38f,
    val overhangResistance: Float = 1.5f,
    val platterActiveScale: Float = 1.03f,
    val itemActiveScale: Float = 1.05f,
    val activeItemScale: Float = 1.15f,
    val indicatorActiveScale: Float = 1.05f,
    val velocityHorizontalFactor: Float = 0.75f,
    val velocityVerticalFactor: Float = 0.25f,
    val maxVelocityCoerce: Float = 0.2f,
    val dragVisibilityThreshold: Float = 0.001f,

    // 水滴位移物理参数：拖拽流动阻尼与跟手刚度 (0.58f 欠阻尼带来拉动流动滞后与追赶弹性；260f 刚度呈现真实水珠质感)
    val dragDampingRatio: Float = 0.58f,
    val dragStiffness: Float = 260f,

    // 槽位吸附惯性参数：0.62f 欠阻尼赋予进入 item 时自然的惯性冲量（左入偏右、右入偏左）
    val dockingDampingRatio: Float = 0.62f,
    val dockingStiffness: Float = 600f,
    val dockingInertiaFactor: Float = 1.0f,

    // 水滴按下变大速度与弹性：1350f 刚度迅速成型，0.68f 阻尼比呈现水珠饱满弹动
    val growDampingRatio: Float = 0.68f,
    val growStiffness: Float = 1350f,

    // 水滴吸附变小速度与弹性：420f 刚度柔和收拢，0.65f 阻尼比带来果冻回弹缩小质感
    val shrinkDampingRatio: Float = 0.65f,
    val shrinkStiffness: Float = 420f,

    // 水滴果冻回弹弹性程度：控制缩小触底时体积守恒的果冻形变弹性幅度
    val dropletElasticity: Float = 0.35f,

    // 吸附 item 时开始变小的距离阈值 (以 Tab 槽位跨度归一化单位计)
    val dockingShrinkThreshold: Float = 0.35f
)

/**
 * iOS HIG 规范：流体导航栏默认配置工厂 (LiquidTabBarDefaults)
 */
object LiquidTabBarDefaults {

    /**
     * 颜色与材质工厂 (原生自适应明暗模式)
     */
    fun colors(
        isDark: Boolean = false,
        platterColor: Color = if (!isDark) Color.White.copy(alpha = 0.33f) else Color(0xFF1C1C1E).copy(alpha = 0.37f),
        platterBorderColor: Color = if (!isDark) Color.Black.copy(alpha = 0.06f) else Color.White.copy(alpha = 0.12f),
        indicatorIdleColor: Color = if (!isDark) Color.Black.copy(alpha = 0.06f) else Color.White.copy(alpha = 0.08f),
        indicatorActiveTint: Color = Color.White.copy(alpha = 0.02f),
        indicatorBorderColor: Color = if (!isDark) Color.White.copy(alpha = 0.14f) else Color.White.copy(alpha = 0.08f),
        activeItemColor: Color = if (!isDark) Color(0xFF007AFF) else Color(0xFF0A84FF),
        inactiveItemColor: Color = if (!isDark) Color(0x99000000) else Color(0x99FFFFFF)
    ): LiquidTabBarColors = LiquidTabBarColors(
        platterColor = platterColor,
        platterBorderColor = platterBorderColor,
        indicatorIdleColor = indicatorIdleColor,
        indicatorActiveTint = indicatorActiveTint,
        indicatorBorderColor = indicatorBorderColor,
        activeItemColor = activeItemColor,
        inactiveItemColor = inactiveItemColor
    )

    /**
     * 几何尺寸默认工厂
     */
    fun dimensions(
        containerHeight: Dp = 84.dp,
        platterHeight: Dp = 58.dp,
        indicatorIdleHeight: Dp = 50.dp,
        indicatorActiveHeight: Dp = 72.dp,
        horizontalPadding: Dp = 4.dp,
        borderWidth: Dp = 0.6.dp,
        indicatorBorderWidth: Dp = 0.4.dp
    ): LiquidTabBarDimensions = LiquidTabBarDimensions(
        containerHeight = containerHeight,
        platterHeight = platterHeight,
        indicatorIdleHeight = indicatorIdleHeight,
        indicatorActiveHeight = indicatorActiveHeight,
        horizontalPadding = horizontalPadding,
        borderWidth = borderWidth,
        indicatorBorderWidth = indicatorBorderWidth
    )

    /**
     * 光学透镜与微磨砂默认工厂
     */
    fun optics(
        isDark: Boolean = false,
        platterBlur: Dp = 1.dp,
        platterRefractionHeight: Dp = 14.dp,
        platterRefractionAmount: Dp = 10.dp,
        platterHighlightAlpha: Float = if (!isDark) 0.24f else 0.40f,
        indicatorRefractionHeightMin: Dp = 6.dp,
        indicatorRefractionHeightMax: Dp = 14.dp,
        indicatorRefractionAmount: Dp = 8.dp,
        indicatorHighlightAlpha: Float = if (!isDark) 0.16f else 0.10f,
        indicatorShadowRadius: Dp = 8.dp,
        indicatorShadowOffsetY: Dp = 2.5.dp,
        indicatorShadowAlpha: Float = if (!isDark) 0.45f else 0.55f,
        indicatorInnerShadowRadius: Dp = 4.dp,
        indicatorInnerShadowAlpha: Float = 0.28f,
        chromaticAberration: Boolean = false
    ): LiquidTabBarOptics = LiquidTabBarOptics(
        platterBlur = platterBlur,
        platterRefractionHeight = platterRefractionHeight,
        platterRefractionAmount = platterRefractionAmount,
        platterHighlightAlpha = platterHighlightAlpha,
        indicatorRefractionHeightMin = indicatorRefractionHeightMin,
        indicatorRefractionHeightMax = indicatorRefractionHeightMax,
        indicatorRefractionAmount = indicatorRefractionAmount,
        indicatorHighlightAlpha = indicatorHighlightAlpha,
        indicatorShadowRadius = indicatorShadowRadius,
        indicatorShadowOffsetY = indicatorShadowOffsetY,
        indicatorShadowAlpha = indicatorShadowAlpha,
        indicatorInnerShadowRadius = indicatorInnerShadowRadius,
        indicatorInnerShadowAlpha = indicatorInnerShadowAlpha,
        chromaticAberration = chromaticAberration
    )

    /**
     * 动力学物理与手势默认工厂
     */
    fun physics(
        maxOverhang: Float = 0.38f,
        overhangResistance: Float = 1.5f,
        platterActiveScale: Float = 1.03f,
        itemActiveScale: Float = 1.05f,
        activeItemScale: Float = 1.15f,
        indicatorActiveScale: Float = 1.05f,
        velocityHorizontalFactor: Float = 0.75f,
        velocityVerticalFactor: Float = 0.25f,
        maxVelocityCoerce: Float = 0.2f,
        dragVisibilityThreshold: Float = 0.001f,
        dragDampingRatio: Float = 0.58f,
        dragStiffness: Float = 260f,
        dockingDampingRatio: Float = 0.62f,
        dockingStiffness: Float = 600f,
        dockingInertiaFactor: Float = 1.0f,
        growDampingRatio: Float = 0.68f,
        growStiffness: Float = 1350f,
        shrinkDampingRatio: Float = 0.65f,
        shrinkStiffness: Float = 420f,
        dropletElasticity: Float = 0.35f,
        dockingShrinkThreshold: Float = 0.35f
    ): LiquidTabBarPhysics = LiquidTabBarPhysics(
        maxOverhang = maxOverhang,
        overhangResistance = overhangResistance,
        platterActiveScale = platterActiveScale,
        itemActiveScale = itemActiveScale,
        activeItemScale = activeItemScale,
        indicatorActiveScale = indicatorActiveScale,
        velocityHorizontalFactor = velocityHorizontalFactor,
        velocityVerticalFactor = velocityVerticalFactor,
        maxVelocityCoerce = maxVelocityCoerce,
        dragVisibilityThreshold = dragVisibilityThreshold,
        dragDampingRatio = dragDampingRatio,
        dragStiffness = dragStiffness,
        dockingDampingRatio = dockingDampingRatio,
        dockingStiffness = dockingStiffness,
        dockingInertiaFactor = dockingInertiaFactor,
        growDampingRatio = growDampingRatio,
        growStiffness = growStiffness,
        shrinkDampingRatio = shrinkDampingRatio,
        shrinkStiffness = shrinkStiffness,
        dropletElasticity = dropletElasticity,
        dockingShrinkThreshold = dockingShrinkThreshold
    )
}
