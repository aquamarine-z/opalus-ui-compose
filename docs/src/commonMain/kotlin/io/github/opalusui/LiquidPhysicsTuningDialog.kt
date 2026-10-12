package io.github.opalusui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.hoverable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsHoveredAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
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
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import io.github.opalusui.opalus.ui.liquid.LiquidTabBar
import io.github.opalusui.opalus.ui.liquid.LiquidTabBarDefaults
import io.github.opalusui.opalus.ui.liquid.LiquidTabBarPhysics
import io.github.opalusui.opalus.ui.liquid.LiquidTabItem
import kotlin.math.roundToInt

data class PhysicsParamInfo(
    val id: String,
    val name: String,
    val label: String,
    val category: String,
    val explanation: String,
    val visualEffect: String,
    val recommendation: String
)

private val physicsParamDocs = mapOf(
    "dockingDampingRatio" to PhysicsParamInfo(
        id = "dockingDampingRatio",
        name = "dockingDampingRatio",
        label = "槽位吸附惯性阻尼比",
        category = "槽位吸附与惯性进入",
        explanation = "控制水滴飞入目标槽位时的振荡阻尼比。当阻尼比小于 1.0 时，水滴进入槽位会带自然惯性稍稍过冲再回弹。",
        visualEffect = "从左侧进入向右偏转过冲、从右侧进入向左偏转过冲后摇晃回弹。阻尼比越小偏转越明显，阻尼比接近 1.0 时几乎无偏转。",
        recommendation = "推荐 0.58f ~ 0.65f，呈现轻柔灵动的水流冲量感。"
    ),
    "dockingStiffness" to PhysicsParamInfo(
        id = "dockingStiffness",
        name = "dockingStiffness",
        label = "槽位吸附刚度",
        category = "槽位吸附与惯性进入",
        explanation = "水滴产生惯性偏转后回正到槽位中心的弹簧刚度。",
        visualEffect = "刚度越小，回正周期越悠长，摇晃更明显；刚度越大，偏转后归位越迅捷干脆。",
        recommendation = "推荐 500f ~ 700f，兼顾灵动惯性与高效归位。"
    ),
    "dockingInertiaFactor" to PhysicsParamInfo(
        id = "dockingInertiaFactor",
        name = "dockingInertiaFactor",
        label = "槽位惯性冲量倍率",
        category = "槽位吸附与惯性进入",
        explanation = "等比例缩放滑动脱手甩动或轻点进入 item 时的惯性动量大小。",
        visualEffect = "设为 0 时关闭动量偏转；值越大，飞入目标槽位时的左右偏转过冲幅度越深。",
        recommendation = "默认 1.0f；喜欢明显惯性动量可设为 1.2f ~ 1.5f。"
    ),
    "dockingShrinkThreshold" to PhysicsParamInfo(
        id = "dockingShrinkThreshold",
        name = "dockingShrinkThreshold",
        label = "吸附变小距离阈值",
        category = "槽位吸附与惯性进入",
        explanation = "归一化 Tab 槽位跨度距离。距目标槽位多远以内才开始触发变小收拢动画。",
        visualEffect = "在距离大于该阈值时，水滴在跨槽位飞行中维持展开与透镜折射状态；进入阈值后才开始平滑收敛吸附。",
        recommendation = "推荐 0.28f ~ 0.40f 槽位距离。"
    ),
    "shrinkDampingRatio" to PhysicsParamInfo(
        id = "shrinkDampingRatio",
        name = "shrinkDampingRatio",
        label = "水珠缩小收拢阻尼比",
        category = "水珠变小/变大与果冻弹性",
        explanation = "控制水珠吸附变小过程中的欠阻尼弹性。",
        visualEffect = "小于 1.0 时水珠缩小不是直接线性消失，而是伴随果冻微弹后平顺收拢，杜绝机械生硬感。",
        recommendation = "推荐 0.60f ~ 0.68f，呈现真实水滴表面张力回弹。"
    ),
    "shrinkStiffness" to PhysicsParamInfo(
        id = "shrinkStiffness",
        name = "shrinkStiffness",
        label = "水珠缩小收拢刚度",
        category = "水珠变小/变大与果冻弹性",
        explanation = "水珠变小的缓动收拢速度。",
        visualEffect = "刚度越低，水珠缩小越慢、越柔润悠闲；刚度越高缩小越迅疾。",
        recommendation = "推荐 380f ~ 480f，获得柔润平缓的停靠感。"
    ),
    "dropletElasticity" to PhysicsParamInfo(
        id = "dropletElasticity",
        name = "dropletElasticity",
        label = "水滴果冻弹性回弹强度",
        category = "水珠变小/变大与果冻弹性",
        explanation = "水滴缩小触底时体积守恒的果冻形变幅度。",
        visualEffect = "当水滴缩回到静止槽位时，横向产生微弹扩张、纵向微弹压扁，再弹回标准胶囊，带来水珠落地果冻质感。",
        recommendation = "默认 0.35f；设为 0 关闭形变；设为 0.5f ~ 0.7f 呈现高弹性果冻质感。"
    ),
    "growDampingRatio" to PhysicsParamInfo(
        id = "growDampingRatio",
        name = "growDampingRatio",
        label = "水珠变大展开阻尼比",
        category = "水珠变小/变大与果冻弹性",
        explanation = "手指或光标按下瞬间水珠向外膨胀的弹性阻尼。",
        visualEffect = "欠阻尼带来水滴破膜而出的饱满表面张力与微回弹，避免平淡展开。",
        recommendation = "推荐 0.65f ~ 0.72f。"
    ),
    "growStiffness" to PhysicsParamInfo(
        id = "growStiffness",
        name = "growStiffness",
        label = "水珠变大展开刚度",
        category = "水珠变小/变大与果冻弹性",
        explanation = "按下瞬间水珠成型的爆发速度。",
        visualEffect = "刚度越高，光标一按下水滴展开越快，极度跟手；刚度越低则像慢镜头膨胀。",
        recommendation = "推荐 1200f ~ 1600f，保证按下瞬时响应。"
    ),
    "dragDampingRatio" to PhysicsParamInfo(
        id = "dragDampingRatio",
        name = "dragDampingRatio",
        label = "手势拖拽位移阻尼比",
        category = "手势实时拖拽跟手",
        explanation = "手指在屏幕上连续拖拽时，水滴跟随光标的物理阻尼。",
        visualEffect = "微欠阻尼赋予水滴位移轻微的水体流动感与动态惯性。",
        recommendation = "推荐 0.80f ~ 0.88f。"
    ),
    "dragStiffness" to PhysicsParamInfo(
        id = "dragStiffness",
        name = "dragStiffness",
        label = "手势拖拽位移刚度",
        category = "手势实时拖拽跟手",
        explanation = "手指拖拽时的位移弹簧刚度。",
        visualEffect = "控制水滴跟随手指的贴合灵敏度。数值越高越跟手，数值越低滞后感越强。",
        recommendation = "推荐 900f ~ 1100f。"
    )
)

@Composable
fun LiquidPhysicsTuningDialog(
    isDarkTheme: Boolean,
    currentPhysics: LiquidTabBarPhysics,
    onPhysicsChange: (LiquidTabBarPhysics) -> Unit,
    onDismissRequest: () -> Unit
) {
    var activeParamId by remember { mutableStateOf("dockingDampingRatio") }
    var previewTab by remember { mutableStateOf(0) }
    val previewTabs = remember { listOf("Tab 1", "Tab 2", "Tab 3", "Tab 4") }

    val activeInfo = physicsParamDocs[activeParamId] ?: physicsParamDocs["dockingDampingRatio"]!!

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Card(
            modifier = Modifier
                .widthIn(max = 840.dp)
                .fillMaxWidth(0.96f)
                .heightIn(max = 720.dp)
                .clip(RoundedCornerShape(24.dp)),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 12.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                // 1. Top Header Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Liquid Tab Bar 动力学参数调优",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = "实时调节物理弹簧与阻尼，即刻查看惯性进入、左右偏转与水珠弹性回弹效果。",
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
                                onPhysicsChange(LiquidTabBarDefaults.physics())
                            },
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("重置为默认值", fontSize = 12.sp)
                        }

                        FilledTonalButton(
                            onClick = onDismissRequest,
                            shape = RoundedCornerShape(10.dp)
                        ) {
                            Text("关闭", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

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
                                text = "实时测试条（点击槽位或拖拽，立即体验当前参数手感）",
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
                                optics = LiquidTabBarDefaults.optics(
                                    isDark = true,
                                    chromaticAberration = true
                                ),
                                physics = currentPhysics,
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

                Spacer(modifier = Modifier.height(14.dp))

                // 3. Hover / Active Parameter Real-Time Explanation Card
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

                        Spacer(modifier = Modifier.height(4.dp))

                        Text(
                            text = "物理效果: ${activeInfo.visualEffect}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.primary,
                            lineHeight = 16.sp
                        )

                        Spacer(modifier = Modifier.height(2.dp))

                        Text(
                            text = "调参建议: ${activeInfo.recommendation}",
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.outline,
                            lineHeight = 16.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 4. Scrollable Sliders List
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Group 1: 槽位吸附与惯性进入
                    CategoryHeader("1. 槽位吸附与惯性进入 (左入偏右、右入偏左)")

                    ParamSliderItem(
                        id = "dockingDampingRatio",
                        name = "dockingDampingRatio",
                        label = "槽位吸附惯性阻尼比 (越小偏转越明显)",
                        value = currentPhysics.dockingDampingRatio,
                        valueRange = 0.40f..0.95f,
                        format = { "${((it * 100).roundToInt() / 100f)}f" },
                        isActive = activeParamId == "dockingDampingRatio",
                        onHover = { activeParamId = "dockingDampingRatio" },
                        onValueChange = {
                            onPhysicsChange(currentPhysics.copy(dockingDampingRatio = it))
                        }
                    )

                    ParamSliderItem(
                        id = "dockingStiffness",
                        name = "dockingStiffness",
                        label = "槽位吸附刚度 (控制偏转回正频率)",
                        value = currentPhysics.dockingStiffness,
                        valueRange = 200f..1200f,
                        format = { "${it.roundToInt()}f" },
                        isActive = activeParamId == "dockingStiffness",
                        onHover = { activeParamId = "dockingStiffness" },
                        onValueChange = {
                            onPhysicsChange(currentPhysics.copy(dockingStiffness = it))
                        }
                    )

                    ParamSliderItem(
                        id = "dockingInertiaFactor",
                        name = "dockingInertiaFactor",
                        label = "惯性冲量倍率 (直接等比缩放左右偏移动量)",
                        value = currentPhysics.dockingInertiaFactor,
                        valueRange = 0.0f..2.5f,
                        format = { "${((it * 100).roundToInt() / 100f)}x" },
                        isActive = activeParamId == "dockingInertiaFactor",
                        onHover = { activeParamId = "dockingInertiaFactor" },
                        onValueChange = {
                            onPhysicsChange(currentPhysics.copy(dockingInertiaFactor = it))
                        }
                    )

                    ParamSliderItem(
                        id = "dockingShrinkThreshold",
                        name = "dockingShrinkThreshold",
                        label = "吸附变小距离阈值 (距目标多远开始收拢)",
                        value = currentPhysics.dockingShrinkThreshold,
                        valueRange = 0.10f..0.70f,
                        format = { "${((it * 100).roundToInt() / 100f)} tab" },
                        isActive = activeParamId == "dockingShrinkThreshold",
                        onHover = { activeParamId = "dockingShrinkThreshold" },
                        onValueChange = {
                            onPhysicsChange(currentPhysics.copy(dockingShrinkThreshold = it))
                        }
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Group 2: 水珠变小/变大与果冻弹性
                    CategoryHeader("2. 水珠展开与收拢弹性 (果冻回弹与表面张力)")

                    ParamSliderItem(
                        id = "shrinkDampingRatio",
                        name = "shrinkDampingRatio",
                        label = "水珠变小收拢阻尼比 (欠阻尼赋予弹性收缩)",
                        value = currentPhysics.shrinkDampingRatio,
                        valueRange = 0.40f..0.95f,
                        format = { "${((it * 100).roundToInt() / 100f)}f" },
                        isActive = activeParamId == "shrinkDampingRatio",
                        onHover = { activeParamId = "shrinkDampingRatio" },
                        onValueChange = {
                            onPhysicsChange(currentPhysics.copy(shrinkDampingRatio = it))
                        }
                    )

                    ParamSliderItem(
                        id = "shrinkStiffness",
                        name = "shrinkStiffness",
                        label = "水珠变小收拢刚度 (缓动减速时间)",
                        value = currentPhysics.shrinkStiffness,
                        valueRange = 150f..900f,
                        format = { "${it.roundToInt()}f" },
                        isActive = activeParamId == "shrinkStiffness",
                        onHover = { activeParamId = "shrinkStiffness" },
                        onValueChange = {
                            onPhysicsChange(currentPhysics.copy(shrinkStiffness = it))
                        }
                    )

                    ParamSliderItem(
                        id = "dropletElasticity",
                        name = "dropletElasticity",
                        label = "水滴果冻弹性回弹强度 (缩小触底压扁微张幅度)",
                        value = currentPhysics.dropletElasticity,
                        valueRange = 0.0f..1.0f,
                        format = { "${((it * 100).roundToInt() / 100f)}x" },
                        isActive = activeParamId == "dropletElasticity",
                        onHover = { activeParamId = "dropletElasticity" },
                        onValueChange = {
                            onPhysicsChange(currentPhysics.copy(dropletElasticity = it))
                        }
                    )

                    ParamSliderItem(
                        id = "growDampingRatio",
                        name = "growDampingRatio",
                        label = "水珠变大展开阻尼比 (表面张力弹动)",
                        value = currentPhysics.growDampingRatio,
                        valueRange = 0.45f..0.95f,
                        format = { "${((it * 100).roundToInt() / 100f)}f" },
                        isActive = activeParamId == "growDampingRatio",
                        onHover = { activeParamId = "growDampingRatio" },
                        onValueChange = {
                            onPhysicsChange(currentPhysics.copy(growDampingRatio = it))
                        }
                    )

                    ParamSliderItem(
                        id = "growStiffness",
                        name = "growStiffness",
                        label = "水珠变大展开刚度 (按下瞬时爆发成型速度)",
                        value = currentPhysics.growStiffness,
                        valueRange = 600f..2400f,
                        format = { "${it.roundToInt()}f" },
                        isActive = activeParamId == "growStiffness",
                        onHover = { activeParamId = "growStiffness" },
                        onValueChange = {
                            onPhysicsChange(currentPhysics.copy(growStiffness = it))
                        }
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    // Group 3: 手势拖拽位移实时跟手
                    CategoryHeader("3. 手势拖拽位移实时跟手")

                    ParamSliderItem(
                        id = "dragDampingRatio",
                        name = "dragDampingRatio",
                        label = "拖拽位移阻尼比 (手指拖动时的水体惯性)",
                        value = currentPhysics.dragDampingRatio,
                        valueRange = 0.60f..1.0f,
                        format = { "${((it * 100).roundToInt() / 100f)}f" },
                        isActive = activeParamId == "dragDampingRatio",
                        onHover = { activeParamId = "dragDampingRatio" },
                        onValueChange = {
                            onPhysicsChange(currentPhysics.copy(dragDampingRatio = it))
                        }
                    )

                    ParamSliderItem(
                        id = "dragStiffness",
                        name = "dragStiffness",
                        label = "拖拽位移刚度 (手势贴合灵敏度)",
                        value = currentPhysics.dragStiffness,
                        valueRange = 400f..1600f,
                        format = { "${it.roundToInt()}f" },
                        isActive = activeParamId == "dragStiffness",
                        onHover = { activeParamId = "dragStiffness" },
                        onValueChange = {
                            onPhysicsChange(currentPhysics.copy(dragStiffness = it))
                        }
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // 5. Bottom Footer Note
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "调整的物理参数会实时作用于展示页面的两个 Navigation Tab Bar。",
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
