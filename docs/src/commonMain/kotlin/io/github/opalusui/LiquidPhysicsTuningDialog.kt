package io.github.opalusui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.opalusui.opalus.ui.liquid.LiquidTabBar
import io.github.opalusui.opalus.ui.liquid.LiquidTabBarDefaults
import io.github.opalusui.opalus.ui.liquid.LiquidTabBarDimensions
import io.github.opalusui.opalus.ui.liquid.LiquidTabBarOptics
import io.github.opalusui.opalus.ui.liquid.LiquidTabBarPhysics
import io.github.opalusui.opalus.ui.liquid.LiquidTabItem
import kotlin.math.roundToInt

data class TuningParamInfo(
    val id: String,
    val name: String,
    val label: String,
    val category: String,
    val explanation: String,
    val increaseEffect: String,
    val decreaseEffect: String,
    val recommendation: String
)

typealias PhysicsParamInfo = TuningParamInfo

private val physicsParamDocs = mapOf(
    "dockingDampingRatio" to TuningParamInfo(
        id = "dockingDampingRatio",
        name = "dockingDampingRatio",
        label = "槽位吸附惯性阻尼比",
        category = "槽位吸附与惯性进入",
        explanation = "控制水滴飞入目标槽位时的振荡阻尼比。欠阻尼状态赋予水滴自然冲量过冲。",
        increaseEffect = "调高（接近 1.0f）：水滴飞入槽位时几乎没有左右过冲与摇晃，平稳直入停靠。",
        decreaseEffect = "调低（低于 0.55f）：水滴从左进入向右偏转、从右进入向左偏转的幅度显著加深，回弹抖动更加剧烈灵动。",
        recommendation = "建议 0.55f ~ 0.65f（默认 0.62f），呈现自然轻盈的液体惯性。"
    ),
    "dockingStiffness" to TuningParamInfo(
        id = "dockingStiffness",
        name = "dockingStiffness",
        label = "槽位吸附刚度",
        category = "槽位吸附与惯性进入",
        explanation = "水滴产生惯性偏转后，弹簧拉力将其回正到槽位中心的恢复速度与力量。",
        increaseEffect = "调高（高于 700f）：偏转后瞬间归位，吸附干脆利落、反应极快。",
        decreaseEffect = "调低（低于 400f）：偏转后回正周期漫长，水滴像果冻一样在槽位中心左右慢速晃动。",
        recommendation = "建议 500f ~ 700f（默认 600f），兼顾灵动冲量与高效归正。"
    ),
    "dockingInertiaFactor" to TuningParamInfo(
        id = "dockingInertiaFactor",
        name = "dockingInertiaFactor",
        label = "槽位惯性定值偏转倍率",
        category = "槽位吸附与惯性进入",
        explanation = "控制水滴飞入槽位时的定值惯性偏转/冲量过冲大小。严格定值，与飞行距离与飞行时长无关。",
        increaseEffect = "调高（高于 1.2f）：进入槽位时定值过冲幅度放大，冲击感与回弹更明显。",
        decreaseEffect = "调低（低于 0.6f / 设为 0）：定值过冲幅度缩小或关闭，水滴平稳直入槽位中心无偏转。",
        recommendation = "建议 0.8f ~ 1.3f（默认 1.0f）。"
    ),
    "dockingShrinkThreshold" to TuningParamInfo(
        id = "dockingShrinkThreshold",
        name = "dockingShrinkThreshold",
        label = "吸附变小距离阈值",
        category = "槽位吸附与惯性进入",
        explanation = "归一化 Tab 槽位跨度距离。定义水滴距目标槽位多近以内时才开始触发收拢变小动画。",
        increaseEffect = "调高（高于 0.5f）：水滴距离目标槽位很远时就提前开始缩小，飞行途中更早变小。",
        decreaseEffect = "调低（低于 0.2f）：水滴在整个跨越飞行中几乎全程保持饱满大水滴状态，直到触碰槽位瞬间才急剧收缩。",
        recommendation = "建议 0.25f ~ 0.45f（默认 0.35f）。"
    ),
    "shrinkDampingRatio" to TuningParamInfo(
        id = "shrinkDampingRatio",
        name = "shrinkDampingRatio",
        label = "水珠收拢基础阻尼比",
        category = "水珠展开与收拢弹性",
        explanation = "水珠松手吸附变小过程中的基础弹簧阻尼比。",
        increaseEffect = "调高（接近 1.0f）：缩小过程平缓线性，无回弹振荡。",
        decreaseEffect = "调低（低于 0.55f）：缩小过程伴随明显的欠阻尼表面张力抖动回弹。",
        recommendation = "建议 0.58f ~ 0.72f（默认 0.65f）。"
    ),
    "shrinkStiffness" to TuningParamInfo(
        id = "shrinkStiffness",
        name = "shrinkStiffness",
        label = "水珠收拢基础刚度",
        category = "水珠展开与收拢弹性",
        explanation = "水珠退出激活态、收拢变小的基础恢复弹簧刚度。",
        increaseEffect = "调高（高于 600f）：变小过程极速收紧，迅速回到静态 inactive。",
        decreaseEffect = "调低（低于 280f）：收拢过程悠闲柔润，像慢镜头液滴平稳缩回。",
        recommendation = "建议 320f ~ 500f（默认 420f）。"
    ),
    "dropletElasticity" to TuningParamInfo(
        id = "dropletElasticity",
        name = "dropletElasticity",
        label = "水滴收拢回弹弹性",
        category = "水珠展开与收拢弹性",
        explanation = "综合调控水滴松手后从当前尺寸回到 inactive 静态所需的时间与欠阻尼表面张力弹性。",
        increaseEffect = "调高（0.6x ~ 1.0x）：收拢耗时更长更柔润，触底时产生明显的流体欠阻尼弹性抖动（全过程严格不向外过量膨胀）。",
        decreaseEffect = "调低（0.0x ~ 0.3x）：收拢迅捷干脆，高刚度直接缩回静态，无多余弹性振荡。",
        recommendation = "建议 0.35x ~ 0.65x（默认 0.45x）。"
    ),
    "growDampingRatio" to TuningParamInfo(
        id = "growDampingRatio",
        name = "growDampingRatio",
        label = "水珠展开阻尼比",
        category = "水珠展开与收拢弹性",
        explanation = "手指或光标按下瞬间水珠向外膨胀成型的弹性阻尼比。",
        increaseEffect = "调高（高于 0.8f）：展开过程平稳渐变，无冲出弹性。",
        decreaseEffect = "调低（低于 0.6f）：水滴破膜而出时产生充满活力的饱满冲出与表面张力微弹跳。",
        recommendation = "建议 0.62f ~ 0.72f（默认 0.68f）。"
    ),
    "growStiffness" to TuningParamInfo(
        id = "growStiffness",
        name = "growStiffness",
        label = "水珠展开刚度",
        category = "水珠展开与收拢弹性",
        explanation = "光标按下瞬间水珠成型的爆发速度与响应拉力。",
        increaseEffect = "调高（高于 1400f）：手指一碰屏幕水滴瞬时爆发成型，极度跟手灵敏。",
        decreaseEffect = "调低（低于 800f）：按下时光感和水珠缓慢膨胀延展，响应略显迟钝软绵。",
        recommendation = "建议 1100f ~ 1500f（默认 1350f）。"
    ),
    "dragDampingRatio" to TuningParamInfo(
        id = "dragDampingRatio",
        name = "dragDampingRatio",
        label = "手势拖拽位移阻尼比",
        category = "手势实时拖拽跟手",
        explanation = "手指在屏幕上拖拽时水滴持续追踪光标的物理阻尼比。",
        increaseEffect = "调高（高于 0.80f）：水滴紧紧锁在光标正下方，甩手停下时几乎无多余晃动。",
        decreaseEffect = "调低（低于 0.50f）：水滴呈现极强的水体流动弹性，急停或甩动手腕时光标停住而水滴冲出并回弹。",
        recommendation = "建议 0.50f ~ 0.65f（默认 0.58f）。"
    ),
    "dragStiffness" to TuningParamInfo(
        id = "dragStiffness",
        name = "dragStiffness",
        label = "手势拖拽位移刚度",
        category = "手势实时拖拽跟手",
        explanation = "连续拖动时的弹簧拉力刚度，决定水滴在拖拽过程中的提速跟随曲线与延迟滞后感。",
        increaseEffect = "调高（高于 500f）：水滴紧随手速，拉开的滞后距离很短，贴手感极强。",
        decreaseEffect = "调低（低于 200f）：产生像橡皮筋拉动水珠般明显的流体滞后感，光标拉开一段距离水滴才加速跟上，反向时惯性显著。",
        recommendation = "建议 200f ~ 350f（默认 260f）。"
    ),
    "maxOverhang" to TuningParamInfo(
        id = "maxOverhang",
        name = "maxOverhang",
        label = "边界允许拉出极限 (像素)",
        category = "边界超界拉伸与底座形变",
        explanation = "水滴被指针拖拽拉出最左或最右槽位时，允许拉出托盘两端边缘的最大物理像素/Dp距离。",
        increaseEffect = "调高（高于 50.dp）：允许拉出容器边缘很长距离，黏滞拉伸拉丝感极度夸张。",
        decreaseEffect = "调低（低于 20.dp）：水滴受强硬物理边界拘束，几乎无法拉出两端胶囊外。",
        recommendation = "建议 25.dp ~ 50.dp（默认 38.dp）。"
    ),
    "platterActiveScaleX" to TuningParamInfo(
        id = "platterActiveScaleX",
        name = "platterActiveScaleX",
        label = "激活态底座 X 轴膨胀倍率",
        category = "边界超界拉伸与底座形变",
        explanation = "手指或光标按下拖拽水滴时，整个底层托盘在横向（X轴）上的呼吸膨胀系数。",
        increaseEffect = "调高（高于 1.05x）：按下时托盘向左右两侧明显舒展张开，弹性幅度更大。",
        decreaseEffect = "调低（1.00x）：按下时横向不发生缩放形变，保持原宽。",
        recommendation = "建议 1.01x ~ 1.05x（默认 1.03x）。"
    ),
    "platterActiveScaleY" to TuningParamInfo(
        id = "platterActiveScaleY",
        name = "platterActiveScaleY",
        label = "激活态底座 Y 轴膨胀倍率",
        category = "边界超界拉伸与底座形变",
        explanation = "手指或光标按下拖拽水滴时，整个底层托盘在纵向（Y轴）上的呼吸膨胀系数。",
        increaseEffect = "调高（高于 1.05x）：按下时托盘上下变厚隆起，立体张力感更强。",
        decreaseEffect = "调低（1.00x）：按下时纵向不发生缩放形变，保持原高。",
        recommendation = "建议 1.01x ~ 1.05x（默认 1.03x）。"
    ),
    "velocityHorizontalFactor" to TuningParamInfo(
        id = "velocityHorizontalFactor",
        name = "velocityHorizontalFactor",
        label = "拖拽速度横向拉伸倍率",
        category = "拖拽运动流体挤压与拉伸",
        explanation = "手指或光标拖拽移动时，根据瞬时移动速度沿横向拉伸水滴的倍率系数。调大时移动中水滴明显变宽拉长；设为 0 时拖拽过程完全不拉伸，保持原始宽度。",
        increaseEffect = "调高（高于 1.0f）：高速拖动时光滴被大幅向左右拉长延展，流体拉丝感极强烈（过高会导致拉成很长一条）。",
        decreaseEffect = "调低（低于 0.3f / 设为 0）：拖动时水滴几乎不被水平拉长，保持近圆形或标准胶囊体，杜绝被拉成长条。",
        recommendation = "建议 0.30f ~ 0.80f（默认 0.65f）。"
    ),
    "velocityVerticalFactor" to TuningParamInfo(
        id = "velocityVerticalFactor",
        name = "velocityVerticalFactor",
        label = "拖拽速度纵向挤压倍率",
        category = "拖拽运动流体挤压与拉伸",
        explanation = "横向拉伸水滴时，等比挤压垂直方向高度的泊松比挤压系数。模拟真实流体体积守恒（横向变长时纵向相应压扁变薄）。",
        increaseEffect = "调高（高于 0.35f）：横向拉长时上下高度急剧被压薄，水滴变得扁平狭长。",
        decreaseEffect = "调低（低于 0.10f / 设为 0）：横向拉伸时上下高度不被挤压，保持原高。",
        recommendation = "建议 0.15f ~ 0.28f（默认 0.20f）。"
    ),
    "maxVelocityCoerce" to TuningParamInfo(
        id = "maxVelocityCoerce",
        name = "maxVelocityCoerce",
        label = "拖拽最大拉伸形变上限",
        category = "拖拽运动流体挤压与拉伸",
        explanation = "限制水滴在极速拖动时允许达到的最大横向拉长和纵向压扁上限比例。例如 0.16 表示无论拖拽多快，横向最多只允许拉长 16%，防止拉成过长长条。",
        increaseEffect = "调高（高于 0.25f）：允许极限拉伸幅度更大，高速挥甩时水滴被拉得很长。",
        decreaseEffect = "调低（低于 0.10f / 设为 0）：严格约束最大形变幅度，彻底杜绝水滴被拉成过长长条覆盖相邻 Tab。",
        recommendation = "建议 0.10f ~ 0.20f（默认 0.16f）。"
    )
)

private val opticsParamDocs = mapOf(
    "platterBlur" to TuningParamInfo(
        id = "platterBlur",
        name = "platterBlur",
        label = "底盘微磨砂模糊半径",
        category = "Platter 实体底盘透镜光学",
        explanation = "外层悬浮实体底盘胶囊对背后图层施加的高斯虚化/磨砂玻璃半径（Dp）。",
        increaseEffect = "调高（高于 6.dp）：底盘背景完全虚化，磨砂玻璃厚重感显著增强，底层图案轮廓极度柔化发白。",
        decreaseEffect = "调低（低于 1.dp / 设为 0）：底盘更通透明澈，晶莹剔透度最高，清晰看清底层壁纸纹理。",
        recommendation = "建议 0.dp ~ 3.dp（默认 1.dp，保持 iOS 27 超轻量通透质感）。"
    ),
    "platterRefractionHeight" to TuningParamInfo(
        id = "platterRefractionHeight",
        name = "platterRefractionHeight",
        label = "底盘边缘透镜折射高度",
        category = "Platter 实体底盘透镜光学",
        explanation = "底盘圆角边缘曲面凸透镜的高度差（Dp），决定底盘外周光学曲面的陡峭度。",
        increaseEffect = "调高（高于 18.dp）：底盘边缘对底层内容的弧度扭曲更深邃剧烈，玻璃边缘厚度与立体感极强。",
        decreaseEffect = "调低（低于 8.dp）：底盘边缘更平坦平顺，折射畸变轻微内敛。",
        recommendation = "建议 10.dp ~ 18.dp（默认 14.dp）。"
    ),
    "platterRefractionAmount" to TuningParamInfo(
        id = "platterRefractionAmount",
        name = "platterRefractionAmount",
        label = "底盘边缘折射偏移强度",
        category = "Platter 实体底盘透镜光学",
        explanation = "底盘边缘光线被凸透镜偏折的像素位移量（Dp）。",
        increaseEffect = "调高（高于 16.dp）：底盘边缘景物拉伸位移极度明显，产生强烈的厚玻璃折射断层。",
        decreaseEffect = "调低（低于 4.dp / 设为 0）：透镜折射偏折非常细腻克制；为 0 时无偏折变形。",
        recommendation = "建议 6.dp ~ 14.dp（默认 10.dp）。"
    ),
    "platterHighlightAlpha" to TuningParamInfo(
        id = "platterHighlightAlpha",
        name = "platterHighlightAlpha",
        label = "底盘环形高光透明度",
        category = "Platter 实体底盘透镜光学",
        explanation = "底盘外轮廓环形边缘反光与流体切光的亮白高光透明度。",
        increaseEffect = "调高（高于 0.40f）：底盘外圈亮白晶莹轮廓极度醒目，在暗色背景下立体轮廓极其清晰。",
        decreaseEffect = "调低（低于 0.12f）：外圈反光非常微弱隐形，与背景融为一体更极简，但可能缺少立体边界。",
        recommendation = "建议 0.18f ~ 0.35f（浅色 0.24f，深色 0.40f）。"
    ),
    "indicatorRefractionHeightMin" to TuningParamInfo(
        id = "indicatorRefractionHeightMin",
        name = "indicatorRefractionHeightMin",
        label = "水滴静止透镜凸面高度",
        category = "SelectionIndicator 水滴光学透镜",
        explanation = "水滴在未被手指按压、处于静止选中槽位时的透镜曲面凸起高度（Dp）。",
        increaseEffect = "调高（高于 10.dp）：静止态水滴依然具有很强的球形凸起感与底层内容放大效果。",
        decreaseEffect = "调低（低于 3.dp）：静止态水滴几乎平贴底盘，视觉更内敛安静。",
        recommendation = "建议 4.dp ~ 8.dp（默认 6.dp）。"
    ),
    "indicatorRefractionHeightMax" to TuningParamInfo(
        id = "indicatorRefractionHeightMax",
        name = "indicatorRefractionHeightMax",
        label = "水滴激活透镜最大凸面高度",
        category = "SelectionIndicator 水滴光学透镜",
        explanation = "手指按压拖拽时，水滴凸透镜向外鼓胀隆起达到的最大光学高度（Dp）。",
        increaseEffect = "调高（高于 18.dp）：按压拖拽时水滴鼓胀得极度饱满，底层彩色图标被放大镜鱼眼变形效果显著放大。",
        decreaseEffect = "调低（低于 8.dp）：按压拖拽时水滴鼓胀不明显，更接近平面滑动色块。",
        recommendation = "建议 11.dp ~ 18.dp（默认 14.dp）。"
    ),
    "indicatorRefractionAmount" to TuningParamInfo(
        id = "indicatorRefractionAmount",
        name = "indicatorRefractionAmount",
        label = "水滴透镜折射偏折强度",
        category = "SelectionIndicator 水滴光学透镜",
        explanation = "水滴核心透镜对底层壁纸与激活标签内容的折射偏折拉伸距离（Dp）。",
        increaseEffect = "调高（高于 12.dp）：水珠像放大镜一样对背景产生强烈的球形鱼眼畸变与透视扭曲。",
        decreaseEffect = "调低（低于 4.dp）：折射偏折微弱，水珠透光更平顺，更接近平光镜片。",
        recommendation = "建议 5.dp ~ 11.dp（默认 8.dp）。"
    ),
    "indicatorHighlightAlpha" to TuningParamInfo(
        id = "indicatorHighlightAlpha",
        name = "indicatorHighlightAlpha",
        label = "水滴表面流光高光透明度",
        category = "SelectionIndicator 水滴光学透镜",
        explanation = "水滴顶部圆润凸面捕获外界光源形成的白色高光微晕透明度。",
        increaseEffect = "调高（高于 0.35f）：水滴表面光泽极亮，像晨露在强光下反光；过高会有塑料反光感。",
        decreaseEffect = "调低（低于 0.08f）：高光微弱，水珠极其清澈纯净透底。",
        recommendation = "建议 0.12f ~ 0.25f（默认 0.16f ~ 0.20f）。"
    ),
    "chromaticAberration" to TuningParamInfo(
        id = "chromaticAberration",
        name = "chromaticAberration",
        label = "色散/棱镜彩虹效果开关",
        category = "SelectionIndicator 水滴光学透镜",
        explanation = "模拟真实玻璃与水滴对红绿蓝光波长不同折射率产生的色散现象（RGB 彩边分离）。",
        increaseEffect = "开启（true）：水滴折射边缘呈现惊艳的红蓝棱镜色散光芒，具有顶级物理光学质感。",
        decreaseEffect = "关闭（false）：纯透镜折射无彩虹边缘，画面更克制纯净。",
        recommendation = "建议开启（true）。"
    ),
    "indicatorShadowRadius" to TuningParamInfo(
        id = "indicatorShadowRadius",
        name = "indicatorShadowRadius",
        label = "水滴悬浮外投影模糊半径",
        category = "水滴空间悬浮与立体阴影",
        explanation = "水滴投射在底盘上的主投影柔化扩散半径（Dp），决定悬浮光斑的空间弥散度。",
        increaseEffect = "调高（高于 14.dp）：阴影更轻柔发散，水珠悬浮距离底盘的空间高度感更开阔深远。",
        decreaseEffect = "调低（低于 4.dp）：阴影硬朗聚焦，水珠紧贴底盘表面。",
        recommendation = "建议 6.dp ~ 12.dp（默认 8.dp）。"
    ),
    "indicatorShadowOffsetY" to TuningParamInfo(
        id = "indicatorShadowOffsetY",
        name = "indicatorShadowOffsetY",
        label = "水滴悬浮外投影垂直下移",
        category = "水滴空间悬浮与立体阴影",
        explanation = "水滴阴影沿 Y 轴向下的偏移量（Dp），模拟自顶部上方入射的环境自然光漫反射。",
        increaseEffect = "调高（高于 5.dp）：阴影大幅下沉，模拟顶部垂直强光与更大的悬浮离地高度。",
        decreaseEffect = "调低（低于 1.dp）：阴影几乎居中包裹，悬浮高度贴地。",
        recommendation = "建议 1.5.dp ~ 4.dp（默认 2.5.dp）。"
    ),
    "indicatorShadowAlpha" to TuningParamInfo(
        id = "indicatorShadowAlpha",
        name = "indicatorShadowAlpha",
        label = "水滴悬浮外投影浓度",
        category = "水滴空间悬浮与立体阴影",
        explanation = "水滴外部悬浮投影的不透明度，决定水滴从底盘分离出来的视觉对比度。",
        increaseEffect = "调高（高于 0.65f）：阴影深邃浓郁，在浅色或复杂壁纸上立体层次感极高。",
        decreaseEffect = "调低（低于 0.25f）：阴影极淡，柔和静谧；过低时水滴与底盘粘连难以分层。",
        recommendation = "建议 0.35f ~ 0.55f（默认 0.45f）。"
    ),
    "indicatorInnerShadowRadius" to TuningParamInfo(
        id = "indicatorInnerShadowRadius",
        name = "indicatorInnerShadowRadius",
        label = "水滴边缘内阴影深度",
        category = "水滴空间悬浮与立体阴影",
        explanation = "水滴内侧周边由于全内反射形成的暗圈衰减半径（Dp），塑造水珠圆润球体边界。",
        increaseEffect = "调高（高于 7.dp）：水滴内陷暗环更宽更深，水珠厚重感与立体深度极强。",
        decreaseEffect = "调低（低于 2.dp）：水珠更单薄轻盈，内侧暗环极细窄。",
        recommendation = "建议 2.5.dp ~ 6.dp（默认 4.dp）。"
    ),
    "indicatorInnerShadowAlpha" to TuningParamInfo(
        id = "indicatorInnerShadowAlpha",
        name = "indicatorInnerShadowAlpha",
        label = "水滴边缘内阴影浓度",
        category = "水滴空间悬浮与立体阴影",
        explanation = "水滴边缘弯月面（Meniscus）内阴影的黑度浓度。",
        increaseEffect = "调高（高于 0.45f）：边缘弯月面形成鲜明的暗色立体封边，水珠边界极度立体清晰。",
        decreaseEffect = "调低（低于 0.15f）：内暗环极淡，边缘过渡极其平顺柔和。",
        recommendation = "建议 0.20f ~ 0.38f（默认 0.28f）。"
    )
)

private val dimensionsParamDocs = mapOf(
    "indicatorActiveHeightScale" to TuningParamInfo(
        id = "indicatorActiveHeightScale",
        name = "indicatorActiveHeightScale",
        label = "激活态水滴高度倍率",
        category = "水滴激活形变倍率",
        explanation = "水滴按下或拖拽处于激活状态时，相对普通静止态高度 (50dp) 的等比缩放倍率。默认 1.44x 对应 72dp 像素高度。",
        increaseEffect = "调高（高于 1.55x）：水滴纵向拔高，上下溢出底座托盘更显著，悬浮立体高度感极强。",
        decreaseEffect = "调低（低于 1.25x / 设为 1.0x）：水滴更紧凑，几乎不超出托盘高度，更显扁平内敛。",
        recommendation = "建议 1.30x ~ 1.55x（默认 1.44x，对应 72dp 黄金悬浮比例）。"
    ),
    "indicatorActiveWidthScale" to TuningParamInfo(
        id = "indicatorActiveWidthScale",
        name = "indicatorActiveWidthScale",
        label = "激活态水滴宽度倍率",
        category = "水滴激活形变倍率",
        explanation = "水滴按下或拖拽处于激活状态时，相对单个槽位静态宽度的等比缩放倍率。默认 1.0x 保持原宽。",
        increaseEffect = "调高（高于 1.15x）：水滴更饱满圆润宽厚，视觉体积更膨胀，犹如液面表面张力外张。",
        decreaseEffect = "调低（低于 0.90x）：水滴收窄苗条，定位更紧凑锐利。",
        recommendation = "建议 0.90x ~ 1.20x（默认 1.00x）。"
    ),
    "indicatorIdleHeight" to TuningParamInfo(
        id = "indicatorIdleHeight",
        name = "indicatorIdleHeight",
        label = "普通态水滴槽位高度",
        category = "基础几何尺寸",
        explanation = "水滴静止休眠在未选中/常规槽位时的物理几何高度（Dp）。",
        increaseEffect = "调高（高于 52.dp）：静止态槽位更饱满贴合底座，上下内边距变小。",
        decreaseEffect = "调低（低于 44.dp）：静止态槽位更纤巧居中，内边距更大。",
        recommendation = "建议 46.dp ~ 52.dp（默认 50.dp）。"
    ),
    "platterHeight" to TuningParamInfo(
        id = "platterHeight",
        name = "platterHeight",
        label = "托盘底座物理高度",
        category = "基础几何尺寸",
        explanation = "底部磨砂胶囊底座的物理几何高度（Dp）。",
        increaseEffect = "调高（高于 64.dp）：底座更开阔厚实，适合大字体或双行标签。",
        decreaseEffect = "调低（低于 52.dp）：底座更纤薄克制，占用屏幕垂直高度更少。",
        recommendation = "建议 54.dp ~ 64.dp（默认 58.dp）。"
    ),
    "containerHeight" to TuningParamInfo(
        id = "containerHeight",
        name = "containerHeight",
        label = "外层容器总高度",
        category = "基础几何尺寸",
        explanation = "整个导航栏外层布局容器的高度（Dp），预留足够空间防止水滴激活上下悬浮时被裁剪。",
        increaseEffect = "调高（高于 90.dp）：为水滴上下悬浮提供更大的安全裕量，杜绝被父组件溢出截断。",
        decreaseEffect = "调低（低于 78.dp）：高度若小于水滴激活高度，可能导致悬浮水滴边缘被裁切。",
        recommendation = "建议 80.dp ~ 96.dp（默认 84.dp）。"
    )
)

enum class TuningTab {
    Physics,
    Dimensions,
    Optics
}

@Composable
fun LiquidPhysicsTuningDialog(
    isDarkTheme: Boolean,
    currentPhysics: LiquidTabBarPhysics,
    onPhysicsChange: (LiquidTabBarPhysics) -> Unit,
    currentOptics: LiquidTabBarOptics = LiquidTabBarDefaults.optics(isDark = isDarkTheme),
    onOpticsChange: (LiquidTabBarOptics) -> Unit = {},
    currentDimensions: LiquidTabBarDimensions = LiquidTabBarDefaults.dimensions(),
    onDimensionsChange: (LiquidTabBarDimensions) -> Unit = {},
    onExportClick: () -> Unit = {},
    onDismissRequest: () -> Unit
) {
    var selectedTab by remember { mutableStateOf(TuningTab.Physics) }
    var activeParamId by remember { mutableStateOf("dockingDampingRatio") }
    var previewTab by remember { mutableStateOf(0) }
    val previewTabs = remember { listOf("Tab 1", "Tab 2", "Tab 3", "Tab 4") }

    val activeInfo = when (selectedTab) {
        TuningTab.Physics -> physicsParamDocs[activeParamId] ?: physicsParamDocs["dockingDampingRatio"]!!
        TuningTab.Dimensions -> dimensionsParamDocs[activeParamId] ?: dimensionsParamDocs["indicatorActiveHeightScale"]!!
        TuningTab.Optics -> opticsParamDocs[activeParamId] ?: opticsParamDocs["platterRefractionAmount"]!!
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black.copy(alpha = 0.65f))
            .clickable(
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onDismissRequest
            ),
        contentAlignment = Alignment.Center
    ) {
        val isMobile = maxWidth < 720.dp
        val cardPad = if (isMobile) 14.dp else 24.dp

        Card(
            modifier = Modifier
                .widthIn(max = 860.dp)
                .fillMaxWidth(if (isMobile) 0.98f else 0.96f)
                .fillMaxHeight(if (isMobile) 0.96f else 0.88f)
                .clip(RoundedCornerShape(24.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {}
                ),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(cardPad)
            ) {
                // 1. Top Header Row
                if (isMobile) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(
                            text = "Liquid Tab Bar 参数调优",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    when (selectedTab) {
                                        TuningTab.Physics -> onPhysicsChange(LiquidTabBarDefaults.physics())
                                        TuningTab.Dimensions -> onDimensionsChange(LiquidTabBarDefaults.dimensions())
                                        TuningTab.Optics -> onOpticsChange(LiquidTabBarDefaults.optics(isDark = isDarkTheme))
                                    }
                                },
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("重置", fontSize = 11.sp)
                            }
                            OutlinedButton(
                                onClick = onExportClick,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("导出", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                            FilledTonalButton(
                                onClick = onDismissRequest,
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.weight(0.9f)
                            ) {
                                Text("关闭", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = "Liquid Tab Bar 动力学与光学折射参数调优",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = "实时调节流体弹簧动力学、几何倍率与光学折射透镜参数，即刻查看惯性进入、拉动流动与凸透镜折射效果。",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            OutlinedButton(
                                onClick = {
                                    when (selectedTab) {
                                        TuningTab.Physics -> onPhysicsChange(LiquidTabBarDefaults.physics())
                                        TuningTab.Dimensions -> onDimensionsChange(LiquidTabBarDefaults.dimensions())
                                        TuningTab.Optics -> onOpticsChange(LiquidTabBarDefaults.optics(isDark = isDarkTheme))
                                    }
                                },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    when (selectedTab) {
                                        TuningTab.Physics -> "重置动力学默认值"
                                        TuningTab.Dimensions -> "重置尺寸默认值"
                                        TuningTab.Optics -> "重置光学默认值"
                                    },
                                    fontSize = 12.sp
                                )
                            }

                            OutlinedButton(
                                onClick = onExportClick,
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("导出配置 (HTML/JSON)", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }

                            FilledTonalButton(
                                onClick = onDismissRequest,
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("关闭", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 2. Interactive Live Test Strip (实时交互测试条)
                DisableSelection {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(18.dp))
                            .background(Color(0xFF0F172A))
                            .padding(vertical = 18.dp, horizontal = 20.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Text(
                                text = "实时测试条（点击槽位或连续拖拽，立即体验当前物理手感与光学透镜折射）",
                                fontSize = 11.sp,
                                color = Color(0xFF94A3B8),
                                fontFamily = FontFamily.Monospace
                            )

                            LiquidTabBar(
                                items = previewTabs,
                                selectedTabIndex = { previewTab },
                                onTabSelected = { index, _ -> previewTab = index },
                                colors = LiquidTabBarDefaults.colors(
                                    isDark = true,
                                    activeItemColor = Color(0xFF38BDF8),
                                    inactiveItemColor = Color(0xFF94A3B8)
                                ),
                                optics = currentOptics,
                                physics = currentPhysics,
                                dimensions = currentDimensions,
                                modifier = Modifier
                                    .widthIn(max = 420.dp)
                                    .fillMaxWidth(),
                                inactiveItem = { _, item ->
                                    LiquidTabItem(
                                        label = {
                                            Text(
                                                text = item,
                                                fontSize = 12.sp,
                                                color = Color(0xFF94A3B8)
                                            )
                                        }
                                    )
                                },
                                activeItem = { _, item ->
                                    LiquidTabItem(
                                        label = {
                                            Text(
                                                text = item,
                                                fontSize = 12.sp,
                                                fontWeight = FontWeight.Bold,
                                                color = Color(0xFF38BDF8)
                                            )
                                        }
                                    )
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 3. Tab Switcher (动力学 vs 尺寸与倍率 vs 光学折射)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilledTonalButton(
                        onClick = {
                            selectedTab = TuningTab.Physics
                            activeParamId = "dockingDampingRatio"
                        },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = if (selectedTab == TuningTab.Physics) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = if (isMobile) "动力学" else "动力学 (Physics)",
                            fontSize = if (isMobile) 11.sp else 12.sp,
                            fontWeight = if (selectedTab == TuningTab.Physics) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == TuningTab.Physics) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    FilledTonalButton(
                        onClick = {
                            selectedTab = TuningTab.Dimensions
                            activeParamId = "indicatorActiveHeightScale"
                        },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = if (selectedTab == TuningTab.Dimensions) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = if (isMobile) "尺寸倍率" else "尺寸倍率 (Dimensions)",
                            fontSize = if (isMobile) 11.sp else 12.sp,
                            fontWeight = if (selectedTab == TuningTab.Dimensions) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == TuningTab.Dimensions) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    FilledTonalButton(
                        onClick = {
                            selectedTab = TuningTab.Optics
                            activeParamId = "platterRefractionAmount"
                        },
                        colors = ButtonDefaults.filledTonalButtonColors(
                            containerColor = if (selectedTab == TuningTab.Optics) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.surfaceVariant
                        ),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Text(
                            text = if (isMobile) "光学折射" else "光学折射 (Optics)",
                            fontSize = if (isMobile) 11.sp else 12.sp,
                            fontWeight = if (selectedTab == TuningTab.Optics) FontWeight.Bold else FontWeight.Normal,
                            color = if (selectedTab == TuningTab.Optics) MaterialTheme.colorScheme.onPrimary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(10.dp))

                // 4. Hover / Active Parameter Real-Time Explanation Card (含调高/调低与建议区间)
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                    )
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(8.dp)
                                        .clip(CircleShape)
                                        .background(MaterialTheme.colorScheme.primary)
                                )
                                Text(
                                    text = "${activeInfo.label} (${activeInfo.name})",
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 13.sp,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                            }

                            Text(
                                text = "分类: ${activeInfo.category}",
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }

                        Spacer(modifier = Modifier.height(6.dp))

                        Text(
                            text = activeInfo.explanation,
                            fontSize = 12.sp,
                            color = MaterialTheme.colorScheme.onSurface,
                            lineHeight = 17.sp
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp)
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "调高效果",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFF10B981)
                                )
                                Text(
                                    text = activeInfo.increaseEffect,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 15.sp
                                )
                            }

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "调低效果",
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = Color(0xFFF59E0B)
                                )
                                Text(
                                    text = activeInfo.decreaseEffect,
                                    fontSize = 11.sp,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    lineHeight = 15.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = "建议区间:",
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = activeInfo.recommendation,
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // 5. Scrollable Sliders List
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    if (selectedTab == TuningTab.Physics) {
                        // Group 1: 槽位吸附与惯性进入
                        CategoryHeader("1. 槽位吸附与惯性进入 (左入偏右、右入偏左)")

                        ParamSliderItem(
                            id = "dockingDampingRatio",
                            name = "dockingDampingRatio",
                            label = "槽位吸附惯性阻尼比 (越小偏转越明显)",
                            value = currentPhysics.dockingDampingRatio,
                            valueRange = 0.40f..0.95f,
                            format = { "${((it * 100).roundToInt() / 100f)}f" },
                            recommendation = "0.55f ~ 0.65f (默认 0.62f)",
                            isActive = activeParamId == "dockingDampingRatio",
                            onHover = { activeParamId = "dockingDampingRatio" },
                            onValueChange = {
                                onPhysicsChange(currentPhysics.copy(dockingDampingRatio = it))
                            }
                        )

                        ParamSliderItem(
                            id = "dockingStiffness",
                            name = "dockingStiffness",
                            label = "槽位吸附刚度 (惯性偏转回正速度)",
                            value = currentPhysics.dockingStiffness,
                            valueRange = 250f..1200f,
                            format = { "${it.roundToInt()}f" },
                            recommendation = "500f ~ 700f (默认 600f)",
                            isActive = activeParamId == "dockingStiffness",
                            onHover = { activeParamId = "dockingStiffness" },
                            onValueChange = {
                                onPhysicsChange(currentPhysics.copy(dockingStiffness = it))
                            }
                        )

                        ParamSliderItem(
                            id = "dockingInertiaFactor",
                            name = "dockingInertiaFactor",
                            label = "槽位惯性定值偏转倍率 (严格定值，与飞行距离时长无关)",
                            value = currentPhysics.dockingInertiaFactor,
                            valueRange = 0.0f..2.0f,
                            format = { "${((it * 100).roundToInt() / 100f)}x (${(0.12f * it * 100).roundToInt()}% 槽宽)" },
                            recommendation = "0.8f ~ 1.3f (默认 1.0f)",
                            isActive = activeParamId == "dockingInertiaFactor",
                            onHover = { activeParamId = "dockingInertiaFactor" },
                            onValueChange = {
                                onPhysicsChange(currentPhysics.copy(dockingInertiaFactor = it))
                            }
                        )

                        ParamSliderItem(
                            id = "dockingShrinkThreshold",
                            name = "dockingShrinkThreshold",
                            label = "吸附变小距离阈值 (跨槽位飞行保持展开的区间)",
                            value = currentPhysics.dockingShrinkThreshold,
                            valueRange = 0.10f..0.80f,
                            format = { "${((it * 100).roundToInt() / 100f)} 槽位" },
                            recommendation = "0.25f ~ 0.45f (默认 0.35f)",
                            isActive = activeParamId == "dockingShrinkThreshold",
                            onHover = { activeParamId = "dockingShrinkThreshold" },
                            onValueChange = {
                                onPhysicsChange(currentPhysics.copy(dockingShrinkThreshold = it))
                            }
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Group 2: 水珠展开与收拢弹性
                        CategoryHeader("2. 水珠展开与收拢弹性 (果冻回弹与表面张力)")

                        ParamSliderItem(
                            id = "shrinkDampingRatio",
                            name = "shrinkDampingRatio",
                            label = "水珠收拢基础阻尼比 (欠阻尼赋予弹性收缩)",
                            value = currentPhysics.shrinkDampingRatio,
                            valueRange = 0.40f..0.95f,
                            format = { "${((it * 100).roundToInt() / 100f)}f" },
                            recommendation = "0.58f ~ 0.72f (默认 0.65f)",
                            isActive = activeParamId == "shrinkDampingRatio",
                            onHover = { activeParamId = "shrinkDampingRatio" },
                            onValueChange = {
                                onPhysicsChange(currentPhysics.copy(shrinkDampingRatio = it))
                            }
                        )

                        ParamSliderItem(
                            id = "shrinkStiffness",
                            name = "shrinkStiffness",
                            label = "水珠收拢基础刚度 (缓动减速时间)",
                            value = currentPhysics.shrinkStiffness,
                            valueRange = 150f..900f,
                            format = { "${it.roundToInt()}f" },
                            recommendation = "320f ~ 500f (默认 420f)",
                            isActive = activeParamId == "shrinkStiffness",
                            onHover = { activeParamId = "shrinkStiffness" },
                            onValueChange = {
                                onPhysicsChange(currentPhysics.copy(shrinkStiffness = it))
                            }
                        )

                        ParamSliderItem(
                            id = "dropletElasticity",
                            name = "dropletElasticity",
                            label = "水滴收拢回弹弹性 (回到 inactive 的时间与弹性)",
                            value = currentPhysics.dropletElasticity,
                            valueRange = 0.0f..1.0f,
                            format = { "${((it * 100).roundToInt() / 100f)}x" },
                            recommendation = "0.35x ~ 0.65x (默认 0.45x)",
                            isActive = activeParamId == "dropletElasticity",
                            onHover = { activeParamId = "dropletElasticity" },
                            onValueChange = {
                                onPhysicsChange(currentPhysics.copy(dropletElasticity = it))
                            }
                        )

                        ParamSliderItem(
                            id = "growDampingRatio",
                            name = "growDampingRatio",
                            label = "水珠展开阻尼比 (表面张力弹动)",
                            value = currentPhysics.growDampingRatio,
                            valueRange = 0.45f..0.95f,
                            format = { "${((it * 100).roundToInt() / 100f)}f" },
                            recommendation = "0.62f ~ 0.72f (默认 0.68f)",
                            isActive = activeParamId == "growDampingRatio",
                            onHover = { activeParamId = "growDampingRatio" },
                            onValueChange = {
                                onPhysicsChange(currentPhysics.copy(growDampingRatio = it))
                            }
                        )

                        ParamSliderItem(
                            id = "growStiffness",
                            name = "growStiffness",
                            label = "水珠展开刚度 (按下瞬时爆发成型速度)",
                            value = currentPhysics.growStiffness,
                            valueRange = 500f..2200f,
                            format = { "${it.roundToInt()}f" },
                            recommendation = "1100f ~ 1500f (默认 1350f)",
                            isActive = activeParamId == "growStiffness",
                            onHover = { activeParamId = "growStiffness" },
                            onValueChange = {
                                onPhysicsChange(currentPhysics.copy(growStiffness = it))
                            }
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Group 3: 手势拖拽位移实时跟手
                        CategoryHeader("3. 手势拖拽位移实时跟手与流体惯性")

                        ParamSliderItem(
                            id = "dragDampingRatio",
                            name = "dragDampingRatio",
                            label = "拖拽位移阻尼比 (手指拖动时的水体惯性)",
                            value = currentPhysics.dragDampingRatio,
                            valueRange = 0.35f..0.95f,
                            format = { "${((it * 100).roundToInt() / 100f)}f" },
                            recommendation = "0.50f ~ 0.65f (默认 0.58f)",
                            isActive = activeParamId == "dragDampingRatio",
                            onHover = { activeParamId = "dragDampingRatio" },
                            onValueChange = {
                                onPhysicsChange(currentPhysics.copy(dragDampingRatio = it))
                            }
                        )

                        ParamSliderItem(
                            id = "dragStiffness",
                            name = "dragStiffness",
                            label = "拖拽位移刚度 (手势贴合灵敏度与流体滞后感)",
                            value = currentPhysics.dragStiffness,
                            valueRange = 80f..1000f,
                            format = { "${it.roundToInt()}f" },
                            recommendation = "200f ~ 350f (默认 260f)",
                            isActive = activeParamId == "dragStiffness",
                            onHover = { activeParamId = "dragStiffness" },
                            onValueChange = {
                                onPhysicsChange(currentPhysics.copy(dragStiffness = it))
                            }
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Group 4: 拖拽速度流体挤压与拉伸 (Squash & Stretch)
                        CategoryHeader("4. 拖拽运动流体挤压与拉伸 (Squash & Stretch)")

                        ParamSliderItem(
                            id = "velocityHorizontalFactor",
                            name = "velocityHorizontalFactor",
                            label = "拖拽速度横向拉伸倍率 (移动时水滴拉长拉丝程度)",
                            value = currentPhysics.velocityHorizontalFactor,
                            valueRange = 0.0f..2.0f,
                            format = { "${((it * 100).roundToInt() / 100f)}x" },
                            recommendation = "0.30f ~ 0.80f (默认 0.65f)",
                            isActive = activeParamId == "velocityHorizontalFactor",
                            onHover = { activeParamId = "velocityHorizontalFactor" },
                            onValueChange = {
                                onPhysicsChange(currentPhysics.copy(velocityHorizontalFactor = it))
                            }
                        )

                        ParamSliderItem(
                            id = "velocityVerticalFactor",
                            name = "velocityVerticalFactor",
                            label = "拖拽速度纵向挤压倍率 (移动时水滴上下变扁程度)",
                            value = currentPhysics.velocityVerticalFactor,
                            valueRange = 0.0f..1.0f,
                            format = { "${((it * 100).roundToInt() / 100f)}x" },
                            recommendation = "0.15f ~ 0.28f (默认 0.20f)",
                            isActive = activeParamId == "velocityVerticalFactor",
                            onHover = { activeParamId = "velocityVerticalFactor" },
                            onValueChange = {
                                onPhysicsChange(currentPhysics.copy(velocityVerticalFactor = it))
                            }
                        )

                        ParamSliderItem(
                            id = "maxVelocityCoerce",
                            name = "maxVelocityCoerce",
                            label = "拖拽最大拉伸形变上限 (防止拉成过长长条的硬截断)",
                            value = currentPhysics.maxVelocityCoerce,
                            valueRange = 0.0f..0.50f,
                            format = { "+${(it * 100).roundToInt()}% 宽" },
                            recommendation = "0.10f ~ 0.20f (默认 0.16f)",
                            isActive = activeParamId == "maxVelocityCoerce",
                            onHover = { activeParamId = "maxVelocityCoerce" },
                            onValueChange = {
                                onPhysicsChange(currentPhysics.copy(maxVelocityCoerce = it))
                            }
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Group 5: 边界超界拉伸与底座形变
                        CategoryHeader("5. 边界超界拉伸与底座形变 (Overhang & Platter Scale)")

                        ParamSliderItem(
                            id = "maxOverhang",
                            name = "maxOverhang",
                            label = "X 轴拖拽最大超界距离 (水滴拉出两端最大允许位移)",
                            value = currentPhysics.maxOverhang.value,
                            valueRange = 0f..120f,
                            format = { "${it.roundToInt()} dp" },
                            recommendation = "20.dp ~ 60.dp (默认 38.dp)",
                            isActive = activeParamId == "maxOverhang",
                            onHover = { activeParamId = "maxOverhang" },
                            onValueChange = {
                                onPhysicsChange(currentPhysics.copy(maxOverhang = it.dp))
                            }
                        )

                        ParamSliderItem(
                            id = "platterActiveScaleX",
                            name = "platterActiveScaleX",
                            label = "底座按下横向缩放倍率 (X 轴膨胀挤压)",
                            value = currentPhysics.platterActiveScaleX,
                            valueRange = 0.95f..1.15f,
                            format = { "${((it * 1000).roundToInt() / 1000f)}x" },
                            recommendation = "1.01f ~ 1.06f (默认 1.03f)",
                            isActive = activeParamId == "platterActiveScaleX",
                            onHover = { activeParamId = "platterActiveScaleX" },
                            onValueChange = {
                                onPhysicsChange(currentPhysics.copy(platterActiveScaleX = it))
                            }
                        )

                        ParamSliderItem(
                            id = "platterActiveScaleY",
                            name = "platterActiveScaleY",
                            label = "底座按下纵向缩放倍率 (Y 轴膨胀挤压)",
                            value = currentPhysics.platterActiveScaleY,
                            valueRange = 0.95f..1.15f,
                            format = { "${((it * 1000).roundToInt() / 1000f)}x" },
                            recommendation = "1.01f ~ 1.06f (默认 1.03f)",
                            isActive = activeParamId == "platterActiveScaleY",
                            onHover = { activeParamId = "platterActiveScaleY" },
                            onValueChange = {
                                onPhysicsChange(currentPhysics.copy(platterActiveScaleY = it))
                            }
                        )
                    } else if (selectedTab == TuningTab.Dimensions) {
                        CategoryHeader("1. 水滴与底座几何倍率 (Scales & Geometry)")

                        ParamSliderItem(
                            id = "indicatorActiveHeightScale",
                            name = "indicatorActiveHeightScale",
                            label = "激活态水滴高度倍率 (相对普通态 50.dp 的高度倍数)",
                            value = currentDimensions.indicatorActiveHeightScale,
                            valueRange = 1.0f..2.0f,
                            format = { "${((it * 100).roundToInt() / 100f)}x (${(currentDimensions.indicatorIdleHeight * it).value.roundToInt()}dp)" },
                            recommendation = "1.25x ~ 1.60x (默认 1.44x，对应 72.dp)",
                            isActive = activeParamId == "indicatorActiveHeightScale",
                            onHover = { activeParamId = "indicatorActiveHeightScale" },
                            onValueChange = {
                                onDimensionsChange(currentDimensions.copy(indicatorActiveHeightScale = it))
                            }
                        )

                        ParamSliderItem(
                            id = "indicatorActiveWidthScale",
                            name = "indicatorActiveWidthScale",
                            label = "激活态水滴宽度倍率 (相对单 Tab 宽度的倍数)",
                            value = currentDimensions.indicatorActiveWidthScale,
                            valueRange = 0.60f..1.80f,
                            format = { "${((it * 100).roundToInt() / 100f)}x" },
                            recommendation = "0.85x ~ 1.25x (默认 1.00x)",
                            isActive = activeParamId == "indicatorActiveWidthScale",
                            onHover = { activeParamId = "indicatorActiveWidthScale" },
                            onValueChange = {
                                onDimensionsChange(currentDimensions.copy(indicatorActiveWidthScale = it))
                            }
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        CategoryHeader("2. 容器与底座基础尺寸 (Base Dimensions)")

                        ParamSliderItem(
                            id = "indicatorIdleHeight",
                            name = "indicatorIdleHeight",
                            label = "普通态水滴槽位高度 (静止基准高度)",
                            value = currentDimensions.indicatorIdleHeight.value,
                            valueRange = 36f..64f,
                            format = { "${it.roundToInt()} dp" },
                            recommendation = "44.dp ~ 56.dp (默认 50.dp)",
                            isActive = activeParamId == "indicatorIdleHeight",
                            onHover = { activeParamId = "indicatorIdleHeight" },
                            onValueChange = {
                                onDimensionsChange(currentDimensions.copy(indicatorIdleHeight = it.dp))
                            }
                        )

                        ParamSliderItem(
                            id = "platterHeight",
                            name = "platterHeight",
                            label = "底盘静态高度 (Navbar 实体背景高度)",
                            value = currentDimensions.platterHeight.value,
                            valueRange = 44f..80f,
                            format = { "${it.roundToInt()} dp" },
                            recommendation = "52.dp ~ 64.dp (默认 58.dp)",
                            isActive = activeParamId == "platterHeight",
                            onHover = { activeParamId = "platterHeight" },
                            onValueChange = {
                                onDimensionsChange(currentDimensions.copy(platterHeight = it.dp))
                            }
                        )

                        ParamSliderItem(
                            id = "containerHeight",
                            name = "containerHeight",
                            label = "外部容器总高度 (承载超界水滴的安全区域)",
                            value = currentDimensions.containerHeight.value,
                            valueRange = 64f..120f,
                            format = { "${it.roundToInt()} dp" },
                            recommendation = "78.dp ~ 96.dp (默认 84.dp)",
                            isActive = activeParamId == "containerHeight",
                            onHover = { activeParamId = "containerHeight" },
                            onValueChange = {
                                onDimensionsChange(currentDimensions.copy(containerHeight = it.dp))
                            }
                        )
                    } else {
                        // Optics Tab: 1. Platter 实体底盘透镜光学
                        CategoryHeader("1. Platter 实体底盘透镜光学")

                        ParamSliderItem(
                            id = "platterBlur",
                            name = "platterBlur",
                            label = "底盘微磨砂模糊半径 (虚化背后图层厚度)",
                            value = currentOptics.platterBlur.value,
                            valueRange = 0f..16f,
                            format = { "${((it * 10).roundToInt() / 10f)} dp" },
                            recommendation = "0.dp ~ 3.dp (默认 1.dp，超轻量通透)",
                            isActive = activeParamId == "platterBlur",
                            onHover = { activeParamId = "platterBlur" },
                            onValueChange = {
                                onOpticsChange(currentOptics.copy(platterBlur = it.dp))
                            }
                        )

                        ParamSliderItem(
                            id = "platterRefractionHeight",
                            name = "platterRefractionHeight",
                            label = "底盘边缘透镜折射高度 (外周曲面陡峭度)",
                            value = currentOptics.platterRefractionHeight.value,
                            valueRange = 2f..28f,
                            format = { "${((it * 10).roundToInt() / 10f)} dp" },
                            recommendation = "10.dp ~ 18.dp (默认 14.dp)",
                            isActive = activeParamId == "platterRefractionHeight",
                            onHover = { activeParamId = "platterRefractionHeight" },
                            onValueChange = {
                                onOpticsChange(currentOptics.copy(platterRefractionHeight = it.dp))
                            }
                        )

                        ParamSliderItem(
                            id = "platterRefractionAmount",
                            name = "platterRefractionAmount",
                            label = "底盘边缘折射偏移强度 (光线偏折像素距离)",
                            value = currentOptics.platterRefractionAmount.value,
                            valueRange = 0f..26f,
                            format = { "${((it * 10).roundToInt() / 10f)} dp" },
                            recommendation = "6.dp ~ 14.dp (默认 10.dp)",
                            isActive = activeParamId == "platterRefractionAmount",
                            onHover = { activeParamId = "platterRefractionAmount" },
                            onValueChange = {
                                onOpticsChange(currentOptics.copy(platterRefractionAmount = it.dp))
                            }
                        )

                        ParamSliderItem(
                            id = "platterHighlightAlpha",
                            name = "platterHighlightAlpha",
                            label = "底盘环形高光透明度 (外轮廓晶莹切光强度)",
                            value = currentOptics.platterHighlightAlpha,
                            valueRange = 0.0f..1.0f,
                            format = { "${((it * 100).roundToInt() / 100f)}f" },
                            recommendation = "0.18f ~ 0.35f (浅色 0.24f / 深色 0.40f)",
                            isActive = activeParamId == "platterHighlightAlpha",
                            onHover = { activeParamId = "platterHighlightAlpha" },
                            onValueChange = {
                                onOpticsChange(currentOptics.copy(platterHighlightAlpha = it))
                            }
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Optics Tab: 2. SelectionIndicator 水滴光学透镜与色散
                        CategoryHeader("2. SelectionIndicator 水滴光学透镜与色散")

                        ParamSliderItem(
                            id = "indicatorRefractionHeightMin",
                            name = "indicatorRefractionHeightMin",
                            label = "水滴静止透镜凸面高度 (静止槽位透镜厚度)",
                            value = currentOptics.indicatorRefractionHeightMin.value,
                            valueRange = 1f..16f,
                            format = { "${((it * 10).roundToInt() / 10f)} dp" },
                            recommendation = "4.dp ~ 8.dp (默认 6.dp)",
                            isActive = activeParamId == "indicatorRefractionHeightMin",
                            onHover = { activeParamId = "indicatorRefractionHeightMin" },
                            onValueChange = {
                                onOpticsChange(currentOptics.copy(indicatorRefractionHeightMin = it.dp))
                            }
                        )

                        ParamSliderItem(
                            id = "indicatorRefractionHeightMax",
                            name = "indicatorRefractionHeightMax",
                            label = "水滴激活透镜最大凸面高度 (按压拖拽鼓胀高度)",
                            value = currentOptics.indicatorRefractionHeightMax.value,
                            valueRange = 4f..26f,
                            format = { "${((it * 10).roundToInt() / 10f)} dp" },
                            recommendation = "11.dp ~ 18.dp (默认 14.dp)",
                            isActive = activeParamId == "indicatorRefractionHeightMax",
                            onHover = { activeParamId = "indicatorRefractionHeightMax" },
                            onValueChange = {
                                onOpticsChange(currentOptics.copy(indicatorRefractionHeightMax = it.dp))
                            }
                        )

                        ParamSliderItem(
                            id = "indicatorRefractionAmount",
                            name = "indicatorRefractionAmount",
                            label = "水滴透镜折射偏折强度 (鱼眼扭曲放大倍率)",
                            value = currentOptics.indicatorRefractionAmount.value,
                            valueRange = 0f..20f,
                            format = { "${((it * 10).roundToInt() / 10f)} dp" },
                            recommendation = "5.dp ~ 11.dp (默认 8.dp)",
                            isActive = activeParamId == "indicatorRefractionAmount",
                            onHover = { activeParamId = "indicatorRefractionAmount" },
                            onValueChange = {
                                onOpticsChange(currentOptics.copy(indicatorRefractionAmount = it.dp))
                            }
                        )

                        ParamSliderItem(
                            id = "indicatorHighlightAlpha",
                            name = "indicatorHighlightAlpha",
                            label = "水滴表面流光高光透明度 (顶部微晕反光强度)",
                            value = currentOptics.indicatorHighlightAlpha,
                            valueRange = 0.0f..0.6f,
                            format = { "${((it * 100).roundToInt() / 100f)}f" },
                            recommendation = "0.12f ~ 0.25f (默认 0.16f ~ 0.20f)",
                            isActive = activeParamId == "indicatorHighlightAlpha",
                            onHover = { activeParamId = "indicatorHighlightAlpha" },
                            onValueChange = {
                                onOpticsChange(currentOptics.copy(indicatorHighlightAlpha = it))
                            }
                        )

                        ParamSwitchItem(
                            id = "chromaticAberration",
                            name = "chromaticAberration",
                            label = "色散/棱镜彩虹效果开关 (RGB 波长微分离)",
                            checked = currentOptics.chromaticAberration,
                            recommendation = "建议开启 (true)，呈现真实棱镜彩虹色散",
                            isActive = activeParamId == "chromaticAberration",
                            onHover = { activeParamId = "chromaticAberration" },
                            onCheckedChange = {
                                onOpticsChange(currentOptics.copy(chromaticAberration = it))
                            }
                        )

                        Spacer(modifier = Modifier.height(4.dp))

                        // Optics Tab: 3. 水滴空间悬浮与立体阴影
                        CategoryHeader("3. 水滴空间悬浮与立体阴影")

                        ParamSliderItem(
                            id = "indicatorShadowRadius",
                            name = "indicatorShadowRadius",
                            label = "水滴悬浮外投影模糊半径 (立体离地弥散度)",
                            value = currentOptics.indicatorShadowRadius.value,
                            valueRange = 0f..22f,
                            format = { "${((it * 10).roundToInt() / 10f)} dp" },
                            recommendation = "6.dp ~ 12.dp (默认 8.dp)",
                            isActive = activeParamId == "indicatorShadowRadius",
                            onHover = { activeParamId = "indicatorShadowRadius" },
                            onValueChange = {
                                onOpticsChange(currentOptics.copy(indicatorShadowRadius = it.dp))
                            }
                        )

                        ParamSliderItem(
                            id = "indicatorShadowOffsetY",
                            name = "indicatorShadowOffsetY",
                            label = "水滴悬浮外投影垂直下移 (入射光落差)",
                            value = currentOptics.indicatorShadowOffsetY.value,
                            valueRange = 0f..8f,
                            format = { "${((it * 10).roundToInt() / 10f)} dp" },
                            recommendation = "1.5.dp ~ 4.dp (默认 2.5.dp)",
                            isActive = activeParamId == "indicatorShadowOffsetY",
                            onHover = { activeParamId = "indicatorShadowOffsetY" },
                            onValueChange = {
                                onOpticsChange(currentOptics.copy(indicatorShadowOffsetY = it.dp))
                            }
                        )

                        ParamSliderItem(
                            id = "indicatorShadowAlpha",
                            name = "indicatorShadowAlpha",
                            label = "水滴悬浮外投影浓度 (悬浮对比度)",
                            value = currentOptics.indicatorShadowAlpha,
                            valueRange = 0.0f..1.0f,
                            format = { "${((it * 100).roundToInt() / 100f)}f" },
                            recommendation = "0.35f ~ 0.55f (默认 0.45f)",
                            isActive = activeParamId == "indicatorShadowAlpha",
                            onHover = { activeParamId = "indicatorShadowAlpha" },
                            onValueChange = {
                                onOpticsChange(currentOptics.copy(indicatorShadowAlpha = it))
                            }
                        )

                        ParamSliderItem(
                            id = "indicatorInnerShadowRadius",
                            name = "indicatorInnerShadowRadius",
                            label = "水滴边缘内阴影深度 (内部球体弯月暗圈)",
                            value = currentOptics.indicatorInnerShadowRadius.value,
                            valueRange = 0f..12f,
                            format = { "${((it * 10).roundToInt() / 10f)} dp" },
                            recommendation = "2.5.dp ~ 6.dp (默认 4.dp)",
                            isActive = activeParamId == "indicatorInnerShadowRadius",
                            onHover = { activeParamId = "indicatorInnerShadowRadius" },
                            onValueChange = {
                                onOpticsChange(currentOptics.copy(indicatorInnerShadowRadius = it.dp))
                            }
                        )

                        ParamSliderItem(
                            id = "indicatorInnerShadowAlpha",
                            name = "indicatorInnerShadowAlpha",
                            label = "水滴边缘内阴影浓度 (弯月暗圈黑度)",
                            value = currentOptics.indicatorInnerShadowAlpha,
                            valueRange = 0.0f..0.8f,
                            format = { "${((it * 100).roundToInt() / 100f)}f" },
                            recommendation = "0.20f ~ 0.38f (默认 0.28f)",
                            isActive = activeParamId == "indicatorInnerShadowAlpha",
                            onHover = { activeParamId = "indicatorInnerShadowAlpha" },
                            onValueChange = {
                                onOpticsChange(currentOptics.copy(indicatorInnerShadowAlpha = it))
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 6. Bottom Footer Note
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "调整的物理与光学参数会实时同步作用于上方测试条与页面展示的所有 Navigation Tab Bar。",
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Button(
                        onClick = onDismissRequest,
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text("应用并返回", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                    }
                }
            }
        }
    }
}

@Composable
private fun CategoryHeader(title: String) {
    Text(
        text = title,
        fontSize = 12.sp,
        fontWeight = FontWeight.Bold,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(top = 4.dp, bottom = 2.dp)
    )
}

@Composable
private fun ParamSliderItem(
    id: String,
    name: String,
    label: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    format: (Float) -> String,
    recommendation: String = "",
    isActive: Boolean,
    onHover: () -> Unit,
    onValueChange: (Float) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    LaunchedEffect(isHovered) {
        if (isHovered) onHover()
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isActive) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.28f)
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.22f)
            )
            .border(
                width = if (isActive) 1.5.dp else 1.dp,
                color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                shape = RoundedCornerShape(12.dp)
            )
            .hoverable(interactionSource)
            .clickable { onHover() }
            .padding(horizontal = 14.dp, vertical = 8.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = name,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = label,
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    if (recommendation.isNotBlank()) {
                        Text(
                            text = "建议区间: $recommendation",
                            fontSize = 10.sp,
                            color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                        )
                    }
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(MaterialTheme.colorScheme.surface)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = format(value),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }

            Slider(
                value = value,
                onValueChange = onValueChange,
                valueRange = valueRange,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun ParamSwitchItem(
    id: String,
    name: String,
    label: String,
    checked: Boolean,
    recommendation: String = "",
    isActive: Boolean,
    onHover: () -> Unit,
    onCheckedChange: (Boolean) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isHovered by interactionSource.collectIsHoveredAsState()

    LaunchedEffect(isHovered) {
        if (isHovered) onHover()
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isActive) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.28f)
                else MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.22f)
            )
            .border(
                width = if (isActive) 1.5.dp else 1.dp,
                color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                shape = RoundedCornerShape(12.dp)
            )
            .hoverable(interactionSource)
            .clickable { onHover() }
            .padding(horizontal = 14.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = name,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isActive) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = label,
                    fontSize = 11.sp,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                if (recommendation.isNotBlank()) {
                    Text(
                        text = "建议区间: $recommendation",
                        fontSize = 10.sp,
                        color = MaterialTheme.colorScheme.primary.copy(alpha = 0.8f)
                    )
                }
            }

            Switch(
                checked = checked,
                onCheckedChange = onCheckedChange
            )
        }
    }
}
