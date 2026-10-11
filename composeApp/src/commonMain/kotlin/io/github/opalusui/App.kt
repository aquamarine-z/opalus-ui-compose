package io.github.opalusui


import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.DialogProperties
import io.github.opalusui.opalus.ui.liquid.LiquidTabBar
import io.github.opalusui.opalus.ui.liquid.LiquidTabBarDefaults
import io.github.opalusui.opalus.ui.liquid.LiquidTabItem
import io.github.opalusui.opalus.ui.material3.dialog.*
import io.github.opalusui.opalus.ui.surface.SurfaceController
import io.github.opalusui.opalus.ui.surface.SurfaceHost
import io.github.opalusui.opalus.ui.surface.useSurface
import io.github.opalusui.ui.theme.AppTypography
import io.github.opalusui.ui.theme.lightScheme
import kotlinx.coroutines.launch

private enum class NavigationTab(val label: String) {
    HOME("Home"),
    DISCOVER("Discover"),
    NOTIFICATIONS("Alerts"),
    PROFILE("Profile")
}

@Composable
fun App() {
    MaterialTheme(
        colorScheme = lightScheme,
        typography = AppTypography
    ) {
        SurfaceHost {
            val controller = useSurface()
            val scope = rememberCoroutineScope()
            val tabs = remember { NavigationTab.entries.toList() }
            var selectedTab by remember { mutableStateOf(NavigationTab.HOME) }
            Column(
                modifier = Modifier.fillMaxSize().background(MaterialTheme.colorScheme.surfaceDim),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = "Active Tab: ${selectedTab.label}",
                    style = MaterialTheme.typography.titleMedium,
                    modifier = Modifier.padding(bottom = 24.dp)
                )

                LiquidTabBar(
                    items = tabs,
                    selectedTabIndex = { tabs.indexOf(selectedTab) },
                    onTabSelected = { _, tab -> selectedTab = tab },
                    colors = LiquidTabBarDefaults.colors(
                        activeItemColor = MaterialTheme.colorScheme.primary,
                        inactiveItemColor = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.65f)
                    ),
                    modifier = Modifier
                        .widthIn(max = 420.dp)
                        .padding(horizontal = 16.dp),
                    inactiveItem = { _, tab ->
                        LiquidTabItem(
                            label = { Text(tab.label, style = MaterialTheme.typography.labelMedium) }
                        )
                    },
                    activeItem = { _, tab ->
                        LiquidTabItem(
                            label = { Text(tab.label, style = MaterialTheme.typography.labelMedium) }
                        )
                    }
                )

                Spacer(modifier = Modifier.height(32.dp))

                Button(
                    onClick = {
                        scope.launch {
                            val result = controller.showCustomDialog()
                            println("Result: $result")
                        }
                    }) {
                    Text("Show Custom Dialog")
                }
                Button(
                    onClick = {
                        scope.launch {
                            val result = controller.dialogs.confirm(
                                "Delete File",
                                "Are you sure you want to delete this file? This action cannot be undone.",
                                options = ConfirmDialogOptions().apply {
                                    type = ConfirmDialogType.Dangerous
                                    properties = DialogProperties(
                                        dismissOnClickOutside = true,
                                        dismissOnBackPress = true,
                                        usePlatformDefaultWidth = true,
                                    )
                                })
                            controller.dialogs.alert(
                                title = "Result", message = "Selection result: $result", okText = "OK"
                            )

                        }
                    }) {
                    Text("Show Confirm Dialog")
                }
            }
        }

    }

}

private suspend fun SurfaceController.showCustomDialog(): Boolean {
    return this.dialogs.custom<Boolean> { close ->
        DialogContent(
            header = {
                DialogHeader {
                    Text("Confirmation")
                }
            }, actions = listOf({
                DialogAction({
                    close(false)
                }, type = DialogActionType.Common){
                    Text("Cancel")
                }
            }, {
                DialogAction({
                    close(true)
                }, type = DialogActionType.Dangerous){
                    Text("Confirm")
                }
            })
        ) {
            Text("Do you want to proceed with this custom action?")
        }
    } ?: false
}
