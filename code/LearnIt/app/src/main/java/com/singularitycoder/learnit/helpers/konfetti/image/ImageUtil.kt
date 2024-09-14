package com.singularitycoder.learnit.helpers.konfetti.image

import android.graphics.drawable.Drawable
import com.singularitycoder.learnit.helpers.konfetti.core.models.Shape

object ImageUtil {
    @JvmStatic
    fun loadDrawable(
        drawable: Drawable,
        tint: Boolean = true,
        applyAlpha: Boolean = true,
    ): Shape.DrawableShape {
        val width = drawable.intrinsicWidth
        val height = drawable.intrinsicHeight
        val drawableImage = DrawableImage(drawable, width, height)
        return Shape.DrawableShape(drawableImage, tint, applyAlpha)
    }
}
