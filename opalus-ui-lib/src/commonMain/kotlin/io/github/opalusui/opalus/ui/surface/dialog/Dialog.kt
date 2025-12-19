package io.github.opalusui.opalus.ui.surface.dialog

import io.github.opalusui.opalus.ui.surface.SurfaceController


val SurfaceController.dialogs: Dialogs
    get() = Dialogs(this)

class Dialogs(internal val controller: SurfaceController)
