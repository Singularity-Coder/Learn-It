package com.singularitycoder.learnit.helpers.konfetti.image

import android.graphics.drawable.Drawable
import com.singularitycoder.learnit.helpers.konfetti.core.models.CoreImage

data class DrawableImage(
    val drawable: Drawable,
    override val width: Int,
    override val height: Int,
) : CoreImage
