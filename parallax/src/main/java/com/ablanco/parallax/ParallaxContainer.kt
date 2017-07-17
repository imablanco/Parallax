package com.ablanco.parallax

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout

/**
 * Created by Álvaro Blanco Cabrero on 14/07/2017.
 * ParallaxLayout.
 */
class ParallaxContainer @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
        FrameLayout(context, attrs, defStyleAttr) {
    init {
        super.setClipToPadding(false)
        super.setClipChildren(false)
    }

    override fun setClipChildren(clipChildren: Boolean) {}
    override fun setClipToPadding(clipToPadding: Boolean) {}
    override fun setClipToOutline(clipToOutline: Boolean) {}
}