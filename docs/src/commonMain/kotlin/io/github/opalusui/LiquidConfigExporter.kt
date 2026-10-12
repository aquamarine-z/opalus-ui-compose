package io.github.opalusui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.github.opalusui.opalus.ui.liquid.LiquidTabBarDefaults
import io.github.opalusui.opalus.ui.liquid.LiquidTabBarDimensions
import io.github.opalusui.opalus.ui.liquid.LiquidTabBarOptics
import io.github.opalusui.opalus.ui.liquid.LiquidTabBarPhysics
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

private fun Float.differsFrom(other: Float, eps: Float = 0.0001f): Boolean = abs(this - other) > eps
private fun Dp.differsFrom(other: Dp, eps: Float = 0.0001f): Boolean = abs(this.value - other.value) > eps

fun getModifiedParamsCount(
    physics: LiquidTabBarPhysics,
    dimensions: LiquidTabBarDimensions,
    optics: LiquidTabBarOptics,
    isDark: Boolean
): Int {
    val defPhys = LiquidTabBarDefaults.physics()
    val defDim = LiquidTabBarDefaults.dimensions()
    val defOpt = LiquidTabBarDefaults.optics(isDark = isDark)
    var count = 0

    if (physics.maxOverhang.differsFrom(defPhys.maxOverhang)) count++
    if (physics.overhangResistance.differsFrom(defPhys.overhangResistance)) count++
    if (physics.overhangBounceAmplitude.differsFrom(defPhys.overhangBounceAmplitude)) count++
    if (physics.overhangCushion.differsFrom(defPhys.overhangCushion)) count++
    if (physics.overhangSettlingStiffness.differsFrom(defPhys.overhangSettlingStiffness)) count++
    if (physics.overhangSettlingDampingRatio.differsFrom(defPhys.overhangSettlingDampingRatio)) count++
    if (physics.platterActiveScaleX.differsFrom(defPhys.platterActiveScaleX)) count++
    if (physics.platterActiveScaleY.differsFrom(defPhys.platterActiveScaleY)) count++
    if (physics.itemActiveScale.differsFrom(defPhys.itemActiveScale)) count++
    if (physics.activeItemScale.differsFrom(defPhys.activeItemScale)) count++
    if (physics.indicatorActiveScale.differsFrom(defPhys.indicatorActiveScale)) count++
    if (physics.velocityHorizontalFactor.differsFrom(defPhys.velocityHorizontalFactor)) count++
    if (physics.velocityVerticalFactor.differsFrom(defPhys.velocityVerticalFactor)) count++
    if (physics.maxVelocityCoerce.differsFrom(defPhys.maxVelocityCoerce)) count++
    if (physics.dragDampingRatio.differsFrom(defPhys.dragDampingRatio)) count++
    if (physics.dragStiffness.differsFrom(defPhys.dragStiffness)) count++
    if (physics.dockingDampingRatio.differsFrom(defPhys.dockingDampingRatio)) count++
    if (physics.dockingStiffness.differsFrom(defPhys.dockingStiffness)) count++
    if (physics.dockingInertiaFactor.differsFrom(defPhys.dockingInertiaFactor)) count++
    if (physics.growDampingRatio.differsFrom(defPhys.growDampingRatio)) count++
    if (physics.growStiffness.differsFrom(defPhys.growStiffness)) count++
    if (physics.shrinkDampingRatio.differsFrom(defPhys.shrinkDampingRatio)) count++
    if (physics.shrinkStiffness.differsFrom(defPhys.shrinkStiffness)) count++
    if (physics.dropletElasticity.differsFrom(defPhys.dropletElasticity)) count++
    if (physics.dockingShrinkThreshold.differsFrom(defPhys.dockingShrinkThreshold)) count++

    if (dimensions.indicatorActiveHeightScale.differsFrom(defDim.indicatorActiveHeightScale)) count++
    if (dimensions.indicatorActiveWidthScale.differsFrom(defDim.indicatorActiveWidthScale)) count++
    if (dimensions.indicatorIdleHeight.differsFrom(defDim.indicatorIdleHeight)) count++
    if (dimensions.platterHeight.differsFrom(defDim.platterHeight)) count++
    if (dimensions.containerHeight.differsFrom(defDim.containerHeight)) count++
    if (dimensions.horizontalPadding.differsFrom(defDim.horizontalPadding)) count++
    if (dimensions.borderWidth.differsFrom(defDim.borderWidth)) count++
    if (dimensions.indicatorBorderWidth.differsFrom(defDim.indicatorBorderWidth)) count++

    if (optics.platterBlur.differsFrom(defOpt.platterBlur)) count++
    if (optics.platterRefractionHeight.differsFrom(defOpt.platterRefractionHeight)) count++
    if (optics.platterRefractionAmount.differsFrom(defOpt.platterRefractionAmount)) count++
    if (optics.platterHighlightAlpha.differsFrom(defOpt.platterHighlightAlpha)) count++
    if (optics.indicatorRefractionHeightMin.differsFrom(defOpt.indicatorRefractionHeightMin)) count++
    if (optics.indicatorRefractionHeightMax.differsFrom(defOpt.indicatorRefractionHeightMax)) count++
    if (optics.indicatorRefractionAmount.differsFrom(defOpt.indicatorRefractionAmount)) count++
    if (optics.indicatorHighlightAlpha.differsFrom(defOpt.indicatorHighlightAlpha)) count++
    if (optics.indicatorShadowRadius.differsFrom(defOpt.indicatorShadowRadius)) count++
    if (optics.indicatorShadowOffsetY.differsFrom(defOpt.indicatorShadowOffsetY)) count++
    if (optics.indicatorShadowAlpha.differsFrom(defOpt.indicatorShadowAlpha)) count++
    if (optics.indicatorInnerShadowRadius.differsFrom(defOpt.indicatorInnerShadowRadius)) count++
    if (optics.indicatorInnerShadowAlpha.differsFrom(defOpt.indicatorInnerShadowAlpha)) count++
    if (optics.chromaticAberration != defOpt.chromaticAberration) count++

    return count
}

fun buildDiffConfigJson(
    physics: LiquidTabBarPhysics,
    dimensions: LiquidTabBarDimensions,
    optics: LiquidTabBarOptics,
    isDark: Boolean
): String {
    val defPhys = LiquidTabBarDefaults.physics()
    val defDim = LiquidTabBarDefaults.dimensions()
    val defOpt = LiquidTabBarDefaults.optics(isDark = isDark)

    val physEntries = mutableListOf<String>()
    val dimEntries = mutableListOf<String>()
    val optEntries = mutableListOf<String>()

    fun addFloat(list: MutableList<String>, key: String, cur: Float, def: Float) {
        if (cur.differsFrom(def)) {
            val formatted = ((cur * 1000f).roundToInt() / 1000f).toString()
            list.add("    \"$key\": $formatted")
        }
    }
    fun addDp(list: MutableList<String>, key: String, cur: Dp, def: Dp) {
        if (cur.differsFrom(def)) {
            val formatted = ((cur.value * 100f).roundToInt() / 100f).toString()
            list.add("    \"$key\": \"${formatted}dp\"")
        }
    }
    fun addBool(list: MutableList<String>, key: String, cur: Boolean, def: Boolean) {
        if (cur != def) {
            list.add("    \"$key\": $cur")
        }
    }

    addDp(physEntries, "maxOverhang", physics.maxOverhang, defPhys.maxOverhang)
    addFloat(physEntries, "overhangResistance", physics.overhangResistance, defPhys.overhangResistance)
    addFloat(physEntries, "overhangBounceAmplitude", physics.overhangBounceAmplitude, defPhys.overhangBounceAmplitude)
    addDp(physEntries, "overhangCushion", physics.overhangCushion, defPhys.overhangCushion)
    addFloat(physEntries, "overhangSettlingStiffness", physics.overhangSettlingStiffness, defPhys.overhangSettlingStiffness)
    addFloat(physEntries, "overhangSettlingDampingRatio", physics.overhangSettlingDampingRatio, defPhys.overhangSettlingDampingRatio)
    addFloat(physEntries, "platterActiveScaleX", physics.platterActiveScaleX, defPhys.platterActiveScaleX)
    addFloat(physEntries, "platterActiveScaleY", physics.platterActiveScaleY, defPhys.platterActiveScaleY)
    addFloat(physEntries, "itemActiveScale", physics.itemActiveScale, defPhys.itemActiveScale)
    addFloat(physEntries, "activeItemScale", physics.activeItemScale, defPhys.activeItemScale)
    addFloat(physEntries, "indicatorActiveScale", physics.indicatorActiveScale, defPhys.indicatorActiveScale)
    addFloat(physEntries, "velocityHorizontalFactor", physics.velocityHorizontalFactor, defPhys.velocityHorizontalFactor)
    addFloat(physEntries, "velocityVerticalFactor", physics.velocityVerticalFactor, defPhys.velocityVerticalFactor)
    addFloat(physEntries, "maxVelocityCoerce", physics.maxVelocityCoerce, defPhys.maxVelocityCoerce)
    addFloat(physEntries, "dragDampingRatio", physics.dragDampingRatio, defPhys.dragDampingRatio)
    addFloat(physEntries, "dragStiffness", physics.dragStiffness, defPhys.dragStiffness)
    addFloat(physEntries, "dockingDampingRatio", physics.dockingDampingRatio, defPhys.dockingDampingRatio)
    addFloat(physEntries, "dockingStiffness", physics.dockingStiffness, defPhys.dockingStiffness)
    addFloat(physEntries, "dockingInertiaFactor", physics.dockingInertiaFactor, defPhys.dockingInertiaFactor)
    addFloat(physEntries, "growDampingRatio", physics.growDampingRatio, defPhys.growDampingRatio)
    addFloat(physEntries, "growStiffness", physics.growStiffness, defPhys.growStiffness)
    addFloat(physEntries, "shrinkDampingRatio", physics.shrinkDampingRatio, defPhys.shrinkDampingRatio)
    addFloat(physEntries, "shrinkStiffness", physics.shrinkStiffness, defPhys.shrinkStiffness)
    addFloat(physEntries, "dropletElasticity", physics.dropletElasticity, defPhys.dropletElasticity)
    addFloat(physEntries, "dockingShrinkThreshold", physics.dockingShrinkThreshold, defPhys.dockingShrinkThreshold)

    addFloat(dimEntries, "indicatorActiveHeightScale", dimensions.indicatorActiveHeightScale, defDim.indicatorActiveHeightScale)
    addFloat(dimEntries, "indicatorActiveWidthScale", dimensions.indicatorActiveWidthScale, defDim.indicatorActiveWidthScale)
    addDp(dimEntries, "indicatorIdleHeight", dimensions.indicatorIdleHeight, defDim.indicatorIdleHeight)
    addDp(dimEntries, "platterHeight", dimensions.platterHeight, defDim.platterHeight)
    addDp(dimEntries, "containerHeight", dimensions.containerHeight, defDim.containerHeight)
    addDp(dimEntries, "horizontalPadding", dimensions.horizontalPadding, defDim.horizontalPadding)
    addDp(dimEntries, "borderWidth", dimensions.borderWidth, defDim.borderWidth)
    addDp(dimEntries, "indicatorBorderWidth", dimensions.indicatorBorderWidth, defDim.indicatorBorderWidth)

    addDp(optEntries, "platterBlur", optics.platterBlur, defOpt.platterBlur)
    addDp(optEntries, "platterRefractionHeight", optics.platterRefractionHeight, defOpt.platterRefractionHeight)
    addDp(optEntries, "platterRefractionAmount", optics.platterRefractionAmount, defOpt.platterRefractionAmount)
    addFloat(optEntries, "platterHighlightAlpha", optics.platterHighlightAlpha, defOpt.platterHighlightAlpha)
    addDp(optEntries, "indicatorRefractionHeightMin", optics.indicatorRefractionHeightMin, defOpt.indicatorRefractionHeightMin)
    addDp(optEntries, "indicatorRefractionHeightMax", optics.indicatorRefractionHeightMax, defOpt.indicatorRefractionHeightMax)
    addDp(optEntries, "indicatorRefractionAmount", optics.indicatorRefractionAmount, defOpt.indicatorRefractionAmount)
    addFloat(optEntries, "indicatorHighlightAlpha", optics.indicatorHighlightAlpha, defOpt.indicatorHighlightAlpha)
    addDp(optEntries, "indicatorShadowRadius", optics.indicatorShadowRadius, defOpt.indicatorShadowRadius)
    addDp(optEntries, "indicatorShadowOffsetY", optics.indicatorShadowOffsetY, defOpt.indicatorShadowOffsetY)
    addFloat(optEntries, "indicatorShadowAlpha", optics.indicatorShadowAlpha, defOpt.indicatorShadowAlpha)
    addDp(optEntries, "indicatorInnerShadowRadius", optics.indicatorInnerShadowRadius, defOpt.indicatorInnerShadowRadius)
    addFloat(optEntries, "indicatorInnerShadowAlpha", optics.indicatorInnerShadowAlpha, defOpt.indicatorInnerShadowAlpha)
    addBool(optEntries, "chromaticAberration", optics.chromaticAberration, defOpt.chromaticAberration)

    if (physEntries.isEmpty() && dimEntries.isEmpty() && optEntries.isEmpty()) {
        return "{\n  \"message\": \"所有参数均与系统默认值一致 (All parameters match defaults)\"\n}"
    }

    val sb = StringBuilder("{\n")
    val sections = mutableListOf<String>()

    if (dimEntries.isNotEmpty()) {
        sections.add("  \"dimensions\": {\n" + dimEntries.joinToString(",\n") + "\n  }")
    }
    if (physEntries.isNotEmpty()) {
        sections.add("  \"physics\": {\n" + physEntries.joinToString(",\n") + "\n  }")
    }
    if (optEntries.isNotEmpty()) {
        sections.add("  \"optics\": {\n" + optEntries.joinToString(",\n") + "\n  }")
    }

    sb.append(sections.joinToString(",\n"))
    sb.append("\n}")
    return sb.toString()
}

fun buildConfigHtml(jsonString: String): String {
    return """<!DOCTYPE html>
<html lang="zh-CN">
<head>
  <meta charset="UTF-8">
  <meta name="viewport" content="width=device-width, initial-scale=1.0">
  <title>LiquidTabBar Custom Configuration</title>
  <style>
    body {
      margin: 0;
      padding: 32px 16px;
      background: #0B0F17;
      color: #F8FAFC;
      font-family: -apple-system, BlinkMacSystemFont, "Segoe UI", Roboto, "Helvetica Neue", monospace;
    }
    .card {
      max-width: 800px;
      margin: 0 auto;
      background: #1E293B;
      border-radius: 16px;
      border: 1px solid rgba(255, 255, 255, 0.1);
      padding: 24px;
      box-shadow: 0 16px 36px rgba(0, 0, 0, 0.4);
    }
    .badge {
      display: inline-block;
      padding: 4px 10px;
      border-radius: 6px;
      background: rgba(56, 189, 248, 0.15);
      color: #38BDF8;
      font-size: 12px;
      font-weight: 600;
      margin-bottom: 12px;
    }
    h1 {
      margin: 0 0 8px 0;
      font-size: 20px;
      color: #F8FAFC;
    }
    p {
      margin: 0 0 20px 0;
      font-size: 13px;
      color: #94A3B8;
    }
    pre {
      background: #0F172A;
      border-radius: 12px;
      border: 1px solid rgba(56, 189, 248, 0.25);
      padding: 18px;
      overflow-x: auto;
      font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
      font-size: 13px;
      line-height: 1.5;
      color: #38BDF8;
    }
  </style>
</head>
<body>
  <div class="card">
    <div class="badge">Opalus UI Liquid</div>
    <h1>LiquidTabBar 自定义差异配置 (Custom Diff Config)</h1>
    <p>仅包含与默认值（Default）存在差异的参数项，以 JSON 格式输出。</p>
    <pre id="json-config"><code>$jsonString</code></pre>
  </div>
</body>
</html>"""
}

@Composable
fun LiquidConfigExportDialog(
    physics: LiquidTabBarPhysics,
    dimensions: LiquidTabBarDimensions,
    optics: LiquidTabBarOptics,
    isDarkTheme: Boolean,
    onDismissRequest: () -> Unit
) {
    val clipboardManager = LocalClipboardManager.current
    val scope = rememberCoroutineScope()
    var copyNotice by remember { mutableStateOf(false) }

    val diffCount = remember(physics, dimensions, optics, isDarkTheme) {
        getModifiedParamsCount(physics, dimensions, optics, isDarkTheme)
    }

    val jsonString = remember(physics, dimensions, optics, isDarkTheme) {
        buildDiffConfigJson(physics, dimensions, optics, isDarkTheme)
    }

    val htmlContent = remember(jsonString) {
        buildConfigHtml(jsonString)
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
        val isMobile = maxWidth < 680.dp
        val dialogPad = if (isMobile) 12.dp else 24.dp

        Card(
            modifier = Modifier
                .widthIn(max = 760.dp)
                .fillMaxWidth(if (isMobile) 0.98f else 0.92f)
                .fillMaxHeight(if (isMobile) 0.94f else 0.85f)
                .clip(RoundedCornerShape(24.dp))
                .clickable(
                    interactionSource = remember { MutableInteractionSource() },
                    indication = null,
                    onClick = {}
                ),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            elevation = CardDefaults.cardElevation(defaultElevation = 14.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(dialogPad)
            ) {
                // Header
                if (isMobile) {
                    Column(
                        modifier = Modifier.fillMaxWidth(),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "导出自定义配置",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(6.dp))
                                    .background(
                                        if (diffCount > 0) MaterialTheme.colorScheme.primaryContainer
                                        else MaterialTheme.colorScheme.surfaceVariant
                                    )
                                    .padding(horizontal = 8.dp, vertical = 3.dp)
                            ) {
                                Text(
                                    text = if (diffCount > 0) "已修改 $diffCount 项" else "均为默认值",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (diffCount > 0) MaterialTheme.colorScheme.primary
                                    else MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Text(
                          text = "与系统 Default 相比的差异项（JSON 格式）",
                          style = MaterialTheme.typography.bodySmall,
                          color = MaterialTheme.colorScheme.onSurfaceVariant,
                          fontSize = 11.sp
                        )

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedButton(
                                onClick = {
                                    clipboardManager.setText(AnnotatedString(jsonString))
                                    copyNotice = true
                                    scope.launch {
                                        delay(2000)
                                        copyNotice = false
                                    }
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text(
                                    if (copyNotice) "已复制!" else "复制 JSON",
                                    fontSize = 11.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            FilledTonalButton(
                                onClick = {
                                    downloadFile("liquid-tabbar-config.html", htmlContent, "text/html")
                                },
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.weight(1f)
                            ) {
                                Text("导出 HTML", fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                            }

                            FilledTonalButton(
                                onClick = onDismissRequest,
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                ),
                                modifier = Modifier.weight(0.8f)
                            ) {
                                Text("关闭", fontSize = 11.sp)
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
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                horizontalArrangement = Arrangement.spacedBy(8.dp)
                            ) {
                                Text(
                                    text = "导出自定义配置 (JSON / HTML)",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(6.dp))
                                        .background(
                                            if (diffCount > 0) MaterialTheme.colorScheme.primaryContainer
                                            else MaterialTheme.colorScheme.surfaceVariant
                                        )
                                        .padding(horizontal = 8.dp, vertical = 3.dp)
                                ) {
                                    Text(
                                        text = if (diffCount > 0) "已修改 $diffCount 项参数" else "全部与默认值一致",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.SemiBold,
                                        color = if (diffCount > 0) MaterialTheme.colorScheme.primary
                                        else MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }
                            Text(
                                text = "系统已自动计算当前调优值与 Default 的差异项，并封装为干净的 JSON 与 HTML 文件。",
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
                                    clipboardManager.setText(AnnotatedString(jsonString))
                                    copyNotice = true
                                    scope.launch {
                                        delay(2000)
                                        copyNotice = false
                                    }
                                },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    if (copyNotice) "已复制到剪贴板!" else "复制 JSON",
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }

                            FilledTonalButton(
                                onClick = {
                                    downloadFile("liquid-tabbar-config.html", htmlContent, "text/html")
                                },
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text("下载 HTML 文件", fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
                            }

                            FilledTonalButton(
                                onClick = onDismissRequest,
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.filledTonalButtonColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                                )
                            ) {
                                Text("关闭", fontSize = 12.sp)
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Scrollable JSON Code Preview
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF0F172A))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp)
                    ) {
                        SelectionContainer {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .verticalScroll(rememberScrollState())
                            ) {
                                Text(
                                    text = jsonString,
                                    fontFamily = FontFamily.Monospace,
                                    fontSize = if (isMobile) 11.sp else 12.sp,
                                    lineHeight = if (isMobile) 16.sp else 18.sp,
                                    color = Color(0xFF38BDF8)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
