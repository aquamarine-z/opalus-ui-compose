package io.github.opalusui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.DisableSelection
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.DialogProperties
import io.github.opalusui.opalus.ui.material3.dialog.ConfirmDialogOptions
import io.github.opalusui.opalus.ui.material3.dialog.ConfirmDialogType
import io.github.opalusui.opalus.ui.material3.dialog.DialogAction
import io.github.opalusui.opalus.ui.material3.dialog.DialogActionType
import io.github.opalusui.opalus.ui.material3.dialog.DialogContent
import io.github.opalusui.opalus.ui.material3.dialog.DialogHeader
import io.github.opalusui.opalus.ui.material3.dialog.alert
import io.github.opalusui.opalus.ui.material3.dialog.confirm
import io.github.opalusui.opalus.ui.material3.dialog.custom
import io.github.opalusui.opalus.ui.material3.dialog.dialogs
import io.github.opalusui.opalus.ui.surface.SurfaceController
import io.github.opalusui.opalus.ui.surface.useSurface
import kotlinx.coroutines.launch

private enum class DeploymentTier {
    DEVELOPMENT,
    STAGING,
    PRODUCTION
}

@Composable
fun DialogsDemoPage() {
    val controller = useSurface()
    val scope = rememberCoroutineScope()
    var latestAction by remember { mutableStateOf("Ready. Trigger any dialog below.") }
    val history = remember { mutableStateListOf<String>() }

    fun logResult(msg: String) {
        latestAction = msg
        history.add(0, msg)
        if (history.size > 5) history.removeLast()
    }

    BoxWithConstraints(modifier = Modifier.fillMaxSize()) {
        val isMobile = maxWidth < 768.dp
        val pagePadding = if (isMobile) 16.dp else 32.dp

        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(pagePadding),
            verticalArrangement = Arrangement.spacedBy(if (isMobile) 20.dp else 28.dp)
        ) {
        // Section Header
        M3SectionHeader(
            title = "Async Dialog & Surface System",
            subtitle = "Suspend-driven dialog architecture for Compose Multiplatform. Directly invoke alerts, confirms, and custom slots from any coroutine without manual state lifting.",
            badge = "opalus-ui-core"
        )

        // Live Coroutine Inspector
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "COROUTINE RETURN INSPECTOR",
                        fontSize = 11.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary,
                        letterSpacing = 1.sp
                    )
                    Text(
                        text = "LIVE STATE",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(10.dp))
                        .background(Color(0xFF0F172A))
                        .padding(14.dp)
                ) {
                    Text(
                        text = latestAction,
                        fontFamily = FontFamily.Monospace,
                        fontSize = 13.sp,
                        color = Color(0xFF38BDF8)
                    )
                }

                if (history.size > 1) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Recent executions:",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    history.drop(1).take(3).forEach { entry ->
                        Text(
                            text = "• $entry",
                            fontFamily = FontFamily.Monospace,
                            fontSize = 11.sp,
                            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f)
                        )
                    }
                }
            }
        }

        // Interactive Demos
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "Interactive Triggers",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "Each button suspends in a coroutine, opens the modal overlay, and directly receives the user's interaction result upon dismissal.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                Spacer(modifier = Modifier.height(20.dp))

                DisableSelection {
                    if (isMobile) {
                        Column(
                            modifier = Modifier.fillMaxWidth(),
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Button(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = {
                                    scope.launch {
                                        val res = controller.dialogs.alert(
                                            title = "System Notification",
                                            message = "Build artifact 'opalus-ui-liquid:0.2.8' compiled with Compose Multiplatform 1.12.1.",
                                            okText = "Acknowledge"
                                        )
                                        logResult("Alert finished: user acknowledged ($res)")
                                    }
                                }
                            ) {
                                Text("Alert Dialog")
                            }

                            Button(
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                                onClick = {
                                    scope.launch {
                                        val confirmed = controller.dialogs.confirm(
                                            title = "Dangerous Action: Delete Branch",
                                            message = "Are you sure you want to delete branch 'feature/fluid-glass'? This action cannot be reverted.",
                                            confirmText = "Delete Permanently",
                                            cancelText = "Keep Branch",
                                            options = ConfirmDialogOptions().apply {
                                                type = ConfirmDialogType.Dangerous
                                                properties = DialogProperties(
                                                    dismissOnClickOutside = true,
                                                    dismissOnBackPress = true
                                                )
                                            }
                                        )
                                        logResult("Dangerous confirm returned: ${if (confirmed == true) "Confirmed (true)" else "Cancelled (false)"}")
                                    }
                                }
                            ) {
                                Text("Dangerous Confirm")
                            }

                            Button(
                                modifier = Modifier.fillMaxWidth(),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                onClick = {
                                    scope.launch {
                                        val confirmed = controller.dialogs.confirm(
                                            title = "Publish Release",
                                            message = "Do you want to sign and push package 'io.github.opalusui:opalus-ui-liquid' to Maven Central?",
                                            confirmText = "Publish",
                                            cancelText = "Draft Later"
                                        )
                                        logResult("Safe confirm returned: ${if (confirmed == true) "Published (true)" else "Drafted (false)"}")
                                    }
                                }
                            ) {
                                Text("Safe Confirm")
                            }

                            OutlinedButton(
                                modifier = Modifier.fillMaxWidth(),
                                onClick = {
                                    scope.launch {
                                        val selection = controller.showDeploymentSelectorDialog()
                                        logResult("Custom slot dialog resolved: $selection")
                                    }
                                }
                            ) {
                                Text("Custom Slot Dialog")
                            }
                        }
                    } else {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(14.dp)
                        ) {
                            Button(
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    scope.launch {
                                        val res = controller.dialogs.alert(
                                            title = "System Notification",
                                            message = "Build artifact 'opalus-ui-liquid:0.2.8' compiled with Compose Multiplatform 1.12.1.",
                                            okText = "Acknowledge"
                                        )
                                        logResult("Alert finished: user acknowledged ($res)")
                                    }
                                }
                            ) {
                                Text("Alert Dialog")
                            }

                            Button(
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEF4444)),
                                onClick = {
                                    scope.launch {
                                        val confirmed = controller.dialogs.confirm(
                                            title = "Dangerous Action: Delete Branch",
                                            message = "Are you sure you want to delete branch 'feature/fluid-glass'? This action cannot be reverted.",
                                            confirmText = "Delete Permanently",
                                            cancelText = "Keep Branch",
                                            options = ConfirmDialogOptions().apply {
                                                type = ConfirmDialogType.Dangerous
                                                properties = DialogProperties(
                                                    dismissOnClickOutside = true,
                                                    dismissOnBackPress = true
                                                )
                                            }
                                        )
                                        logResult("Dangerous confirm returned: ${if (confirmed == true) "Confirmed (true)" else "Cancelled (false)"}")
                                    }
                                }
                            ) {
                                Text("Dangerous Confirm")
                            }

                            Button(
                                modifier = Modifier.weight(1f),
                                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF10B981)),
                                onClick = {
                                    scope.launch {
                                        val confirmed = controller.dialogs.confirm(
                                            title = "Publish Release",
                                            message = "Do you want to sign and push package 'io.github.opalusui:opalus-ui-liquid' to Maven Central?",
                                            confirmText = "Publish",
                                            cancelText = "Draft Later"
                                        )
                                        logResult("Safe confirm returned: ${if (confirmed == true) "Published (true)" else "Drafted (false)"}")
                                    }
                                }
                            ) {
                                Text("Safe Confirm")
                            }

                            OutlinedButton(
                                modifier = Modifier.weight(1f),
                                onClick = {
                                    scope.launch {
                                        val selection = controller.showDeploymentSelectorDialog()
                                        logResult("Custom slot dialog resolved: $selection")
                                    }
                                }
                            ) {
                                Text("Custom Slot Dialog")
                            }
                        }
                    }
                }
            }
        }

        // Section: Exhaustive Class-by-Class API Documentation
        M3SectionHeader(
            title = "Class & Component API Reference",
            subtitle = "Detailed documentation for every class, extension function, option class, and slot composable in opalus-ui-core.",
            badge = "Async Dialog System"
        )

        // 1. SurfaceHost
        M3ClassDocCard(
            className = "SurfaceHost",
            kind = "Composable Host",
            signature = """@Composable fun SurfaceHost(
    controller: SurfaceController = remember { SurfaceController() },
    content: @Composable () -> Unit
)""",
            description = "The foundational root container for the Opalus UI async dialog and surface system. Must wrap your root application hierarchy once. It provides `LocalSurfaceHost` to all descendant composables and renders `SurfaceRenderer` on a decoupled top layer. This completely eliminates the need to hoist boolean dialog state variables (`showDialog = true/false`) into parent composables, preventing recomposition pollution and UI boilerplate.",
            tips = "TIP: Place `SurfaceHost` directly inside your `MaterialTheme` wrapper at the top level of `App.kt` or `MainActivity`. Never create multiple nested `SurfaceHost` instances.",
            parameters = listOf(
                ParamItem("controller", "SurfaceController", "remember { ... }", "The surface controller instance managing the active surface stack and locks."),
                ParamItem("content", "@Composable () -> Unit", "Required", "Your application's main content tree (navigation graphs, screens, scaffolds).")
            ),
            codeExample = """@Composable
fun App() {
    MaterialTheme {
        // Wrap your application root once with SurfaceHost
        SurfaceHost {
            MainNavigation()
        }
    }
}"""
        )

        // 2. SurfaceController & useSurface
        M3ClassDocCard(
            className = "SurfaceController & useSurface()",
            kind = "Controller Hook & Engine",
            signature = """@Composable fun useSurface(): SurfaceController

class SurfaceController {
    suspend fun <T> show(surface: Surface<T>): T?
    fun getSurface(id: String): Surface<Any?>?
    suspend fun removeSurface(id: String)
    suspend fun clear()
}

val SurfaceController.dialogs: Dialogs""",
            description = "`SurfaceController` manages the registration, deferred coroutine suspension, and dismissal order of all modal overlays. `useSurface()` is a Composable hook that retrieves the current `SurfaceController` from `LocalSurfaceHost`. Through the `controller.dialogs` extension property, callers gain access to `alert`, `confirm`, and `custom<T>` methods.",
            tips = "TIP: Use `useSurface()` in your screens, and invoke `controller.dialogs.*` within any `rememberCoroutineScope().launch { ... }` or pass it into your asynchronous business logic.",
            parameters = listOf(
                ParamItem("useSurface()", "SurfaceController", "Contextual", "Hook returning the active SurfaceController instance from composition local."),
                ParamItem("show(surface)", "suspend fun <T>", "Suspends", "Shows a Surface overlay and suspends until resolved via `state.resolve(value)`."),
                ParamItem("clear()", "suspend fun", "Mutex locked", "Dismisses and wipes all currently open surfaces immediately.")
            ),
            codeExample = """@Composable
fun MyScreen() {
    val controller = useSurface()
    val scope = rememberCoroutineScope()

    Button(onClick = {
        scope.launch {
            controller.dialogs.alert(title = "Notice", message = "Operation complete.")
        }
    }) {
        Text("Notify")
    }
}"""
        )

        // 3. Dialogs.alert
        M3ClassDocCard(
            className = "Dialogs.alert",
            kind = "Suspend Function",
            signature = """suspend fun Dialogs.alert(
    title: String,
    message: String,
    okText: String = "OK",
    options: AlertDialogOptions = AlertDialogOptions()
)

suspend fun Dialogs.alert(
    header: (@Composable () -> Unit) = { DialogHeader(title = "Alert") },
    message: (@Composable () -> Unit) = { Text("") },
    ok: (@Composable (close: (Unit) -> Unit) -> Unit)? = null,
    options: AlertDialogOptions = AlertDialogOptions()
)""",
            description = "Suspends the invoking coroutine and displays an informative modal alert dialog with a single acknowledgment button. Execution automatically resumes once the user taps the OK button or dismisses the dialog.",
            parameters = listOf(
                ParamItem("title", "String", "Required", "Headline text displayed in the dialog header."),
                ParamItem("message", "String", "Required", "Informational body message explaining the alert."),
                ParamItem("okText", "String", "\"OK\"", "Button label for the acknowledgment action."),
                ParamItem("options", "AlertDialogOptions", "Defaults", "Configures transition animations and dialog dismissal properties.")
            ),
            codeExample = """scope.launch {
    // Execution pauses here until the user taps 'Acknowledge'
    controller.dialogs.alert(
        title = "Sync Complete",
        message = "Successfully synchronized 142 items with remote storage.",
        okText = "Acknowledge"
    )

    // Resumes immediately after dismissal:
    println("Alert acknowledged by user")
}"""
        )

        // 4. Dialogs.confirm
        M3ClassDocCard(
            className = "Dialogs.confirm",
            kind = "Suspend Function",
            signature = """suspend fun Dialogs.confirm(
    title: String,
    message: String,
    confirmText: String = "Confirm",
    cancelText: String = "Cancel",
    options: ConfirmDialogOptions = ConfirmDialogOptions()
): Boolean?

suspend fun Dialogs.confirm(
    header: (@Composable () -> Unit) = { DialogHeader() },
    message: (@Composable () -> Unit) = { Text("Are you sure?") },
    confirm: (@Composable (close: (Boolean?) -> Unit) -> Unit)? = null,
    cancel: (@Composable (close: (Boolean?) -> Unit) -> Unit)? = null,
    options: ConfirmDialogOptions = ConfirmDialogOptions()
): Boolean?""",
            description = "Prompts the user with confirm and cancel buttons, suspending the coroutine and returning a tri-state result:\n\n• `true`: User clicked the confirmation button.\n• `false`: User clicked the cancellation button.\n• `null`: User dismissed the dialog by tapping outside the modal boundary or pressing the ESC/Back key.",
            parameters = listOf(
                ParamItem("title", "String", "Required", "Headline text displayed in the dialog header."),
                ParamItem("message", "String", "Required", "Body description detailing the proposed action."),
                ParamItem("confirmText", "String", "\"Confirm\"", "Label for the confirmation button."),
                ParamItem("cancelText", "String", "\"Cancel\"", "Label for the cancel button."),
                ParamItem("options", "ConfirmDialogOptions", "Defaults", "Controls whether the action is Common or Dangerous (red) and sets DialogProperties.")
            ),
            codeExample = """scope.launch {
    val confirmed = controller.dialogs.confirm(
        title = "Delete Repository",
        message = "Are you sure you want to delete 'repo-alpha'? This cannot be undone.",
        confirmText = "Delete Permanently",
        cancelText = "Keep Repository",
        options = ConfirmDialogOptions().apply {
            type = ConfirmDialogType.Dangerous
        }
    )

    when (confirmed) {
        true -> executeDeletion()
        false -> log("User explicitly cancelled")
        null -> log("User dismissed dialog outside")
    }
}"""
        )

        // 5. ConfirmDialogOptions & ConfirmDialogType
        M3ClassDocCard(
            className = "ConfirmDialogOptions & ConfirmDialogType",
            kind = "Configuration & Enum",
            signature = """class ConfirmDialogOptions(
    var type: ConfirmDialogType = ConfirmDialogType.Common
) : CustomDialogOptions()

enum class ConfirmDialogType {
    Common,
    Dangerous
}""",
            description = "Configures the semantic styling of confirmation dialogs. When `type = ConfirmDialogType.Dangerous`, the confirm action button is highlighted in Material 3 `error` red color to visually alert the user to destructive, irreversible actions.",
            parameters = listOf(
                ParamItem("type", "ConfirmDialogType", "Common", "Semantic type: Common (primary theme color) or Dangerous (error red color)."),
                ParamItem("properties", "DialogProperties", "Defaults", "Dismiss on outside click, dismiss on back press, and default width flags."),
                ParamItem("enterTransition", "EnterTransition", "Fade + Slide", "Compose animation played when the dialog enters the screen."),
                ParamItem("exitTransition", "ExitTransition", "Fade + Slide", "Compose animation played when the dialog exits the screen.")
            ),
            codeExample = """val dangerousOptions = ConfirmDialogOptions().apply {
    type = ConfirmDialogType.Dangerous
    properties = DialogProperties(
        dismissOnClickOutside = false, // Force user to click an action button
        dismissOnBackPress = false
    )
}"""
        )

        // 6. Dialogs.custom<T>
        M3ClassDocCard(
            className = "Dialogs.custom<T>",
            kind = "Generic Suspend Function",
            signature = """suspend fun <T> Dialogs.custom(
    options: CustomDialogOptions = CustomDialogOptions(),
    content: @Composable (close: (value: T?) -> Unit) -> Unit
): T?""",
            description = "The universal generic foundation of the dialog system. Suspends the coroutine and renders arbitrary Composable content. When your custom UI calls `close(value)`, the dialog dismisses with exit transitions and resolves the caller's suspended coroutine with that strongly typed value `T` (or `null` if dismissed outside).",
            parameters = listOf(
                ParamItem("options", "CustomDialogOptions", "Defaults", "Configures entrance/exit transitions and platform DialogProperties."),
                ParamItem("content", "@Composable (close: (T?) -> Unit)", "Required", "Lambda receiving the resolver `close` callback to render custom forms, pickers, or cards.")
            ),
            codeExample = """enum class ExportFormat { PDF, CSV, JSON }

suspend fun pickFormat(controller: SurfaceController): ExportFormat? {
    return controller.dialogs.custom<ExportFormat> { close ->
        DialogContent(
            header = { DialogHeader(title = "Select Export Format") },
            actions = listOf({
                DialogAction(onClick = { close(null) }) { Text("Cancel") }
            })
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                ExportFormat.entries.forEach { format ->
                    Button(onClick = { close(format) }) {
                        Text(format.name)
                    }
                }
            }
        }
    }
}"""
        )

        // 7. Slot Composables
        M3ClassDocCard(
            className = "DialogContent, DialogHeader & DialogAction",
            kind = "Slot Layout Composables",
            signature = """@Composable fun DialogContent(
    header: @Composable () -> Unit = { DialogHeader() },
    actions: List<@Composable () -> Unit> = emptyList(),
    modifier: Modifier = Modifier,
    shape: Shape = MaterialTheme.shapes.extraLarge,
    color: Color = MaterialTheme.colorScheme.surfaceContainerHigh,
    content: @Composable () -> Unit = {}
)

@Composable fun DialogHeader(icon: (@Composable () -> Unit)? = null, title: String)

@Composable fun DialogAction(
    onClick: () -> Unit,
    type: DialogActionType = DialogActionType.Common,
    content: @Composable () -> Unit
)""",
            description = "A suite of pre-styled Material Design 3 slot composables designed to assemble dialogs adhering strictly to M3 elevation, surface container styling, typography scale, and button padding.",
            parameters = listOf(
                ParamItem("DialogContent.header", "@Composable () -> Unit", "Default header", "Slot rendering the dialog's top title and optional icon."),
                ParamItem("DialogContent.actions", "List<@Composable () -> Unit>", "Empty list", "List of trailing action button composables (Cancel, OK, etc.)."),
                ParamItem("DialogContent.content", "@Composable () -> Unit", "Empty body", "Main body slot rendering message text, form controls, or lists."),
                ParamItem("DialogHeader.icon", "@Composable () -> Unit", "null", "Optional leading or top icon slot."),
                ParamItem("DialogAction.type", "DialogActionType", "Common", "Common (primary color) or Dangerous (error color) button styling.")
            ),
            codeExample = """DialogContent(
    header = {
        DialogHeader(title = "Permissions Required")
    },
    actions = listOf(
        { DialogAction(onClick = { close(false) }) { Text("Deny") } },
        { DialogAction(onClick = { close(true) }, type = DialogActionType.Common) { Text("Allow") } }
    )
) {
    Text("This app needs access to storage to save export files.")
}"""
        )
    }
}
}

private suspend fun SurfaceController.showDeploymentSelectorDialog(): String {
    return this.dialogs.custom<String> { close ->
        DialogContent(
            header = {
                DialogHeader {
                    Text(
                        text = "Select Target Environment",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                }
            },
            actions = listOf(
                {
                    DialogAction({ close("Cancelled") }, type = DialogActionType.Common) {
                        Text("Cancel")
                    }
                },
                {
                    DialogAction({ close("Tier: STAGING") }, type = DialogActionType.Common) {
                        Text("Staging")
                    }
                },
                {
                    DialogAction({ close("Tier: PRODUCTION") }, type = DialogActionType.Dangerous) {
                        Text("Production")
                    }
                }
            )
        ) {
            Column {
                Text(
                    text = "dialogs.custom<T> allows passing arbitrary Composable content and returning strongly typed values directly to the caller coroutine.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "• Fully decoupled from screen state\n• Supports forms, selectors, or wizards\n• Return type can be String, Boolean, Enum, or Data Class",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }
        }
    } ?: "Dismissed outside"
}
