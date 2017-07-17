package com.ablanco.parallax

import android.content.Context
import android.util.AttributeSet
import android.widget.FrameLayout

/**
 * Created by Álvaro Blanco Cabrero on 15/07/2017.
 * ParallaxLayout.
 */
class LayerView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
        FrameLayout(context, attrs, defStyleAttr) {


    var useParallaxPadding = true

    init {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.LayerView)
        useParallaxPadding = typedArray.getBoolean(R.styleable.LayerView_useParallaxPadding, true)
        typedArray.recycle()
    }
}