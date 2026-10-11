package io.github.opalusui.opalus.ui.liquid

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

val LocalLiquidTabBarItemScale = staticCompositionLocalOf { 1f }

/**
 * iOS HIG 规范：流体导航栏单独项声明数据模型 (LiquidTabBarItem)
 */
@Immutable
data class LiquidTabBarItem(
    val inactive: @Composable () -> Unit,
    val active: @Composable () -> Unit = inactive
)

/**
 * iOS HIG 规范：标准 Tab 项垂直布局组件 (LiquidTabItem)
 */
@Composable
fun LiquidTabItem(
    icon: @Composable () -> Unit = {},
    label: @Composable () -> Unit = {},
    modifier: Modifier = Modifier,
    spacing: Dp = 2.dp
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        icon()
        Spacer(Modifier.height(spacing))
        label()
    }
}
