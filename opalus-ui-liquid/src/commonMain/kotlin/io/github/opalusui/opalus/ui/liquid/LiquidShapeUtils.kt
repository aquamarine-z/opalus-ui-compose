package io.github.opalusui.opalus.ui.liquid

import androidx.compose.foundation.shape.CornerBasedShape
import androidx.compose.ui.graphics.Shape
import com.kyant.shapes.RoundedRectangularShape

/**
 * Kyant's lens effect has an SDF implementation for rounded rectangular
 * shapes. Generic Compose shapes are still valid for drawing and clipping, but
 * must skip the lens shader because the shader cannot derive their corner
 * radii.
 */
internal fun Shape.supportsLiquidLens(): Boolean =
    this is CornerBasedShape || this is RoundedRectangularShape
