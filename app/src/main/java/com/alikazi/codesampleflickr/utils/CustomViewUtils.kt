package com.alikazi.codesampleflickr.utils

import android.content.Context
import android.graphics.drawable.Drawable
import android.support.v4.content.ContextCompat
import com.alikazi.codesampleflickr.R

class CustomViewUtils {

    companion object {

        fun getPhotoPlaceholderIcon(context: Context) : Drawable? {
            val drawable: Drawable? = ContextCompat.getDrawable(context, R.drawable.ic_placeholder_photo)
            drawable?.setTint(ContextCompat.getColor(context, R.color.colorIconInactive))
            return drawable
        }

    }
}
