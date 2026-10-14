import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.kyant.shapes.Capsule
import io.github.opalusui.opalus.ui.liquid.LiquidTabBar
import io.github.opalusui.opalus.ui.liquid.LiquidTabBarDefaults
import io.github.opalusui.opalus.ui.liquid.LiquidTabItem
import io.github.opalusui.LiquidDemoPage
import org.jetbrains.compose.storytale.story

val `LiquidTabBar Interactive Physics Playground` by story {
    MaterialTheme {
        LiquidDemoPage(isDarkTheme = true)
    }
}

val `LiquidTabBar Rounded Rectangle Shape` by story {
    MaterialTheme {
        var selectedTabIndex by remember { mutableStateOf(0) }
        val tabs = remember { listOf("Home", "Search", "Profile") }
        val barShape = remember { RoundedCornerShape(22.dp) }
        val inactiveIndicatorShape = remember { RoundedCornerShape(16.dp) }
        val activeIndicatorShape = remember { Capsule() }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Color(0xFF101318))
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            LiquidTabBar(
                items = tabs,
                selectedTabIndex = { selectedTabIndex },
                onTabSelected = { index, _ -> selectedTabIndex = index },
                colors = LiquidTabBarDefaults.colors(isDark = true),
                physics = LiquidTabBarDefaults.physics(
                    platterActivationDurationMillis = 160
                ),
                barShape = barShape,
                inactiveIndicatorShape = inactiveIndicatorShape,
                activeIndicatorShape = activeIndicatorShape,
                modifier = Modifier
                    .widthIn(max = 420.dp)
                    .fillMaxWidth(),
                inactiveItem = { _, item ->
                    LiquidTabItem(
                        label = {
                            Text(
                                text = item,
                                color = Color(0xFFCBD5E1)
                            )
                        }
                    )
                },
                activeItem = { _, item ->
                    LiquidTabItem(
                        label = {
                            Text(
                                text = item,
                                color = MaterialTheme.colorScheme.primary
                            )
                        }
                    )
                }
            )
        }
    }
}
