package com.ablanco.parallax

import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.PointF
import android.support.annotation.Px
import android.support.v7.widget.CardView
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.DecelerateInterpolator

/**
 * Created by Álvaro Blanco Cabrero on 14/07/2017.
 * ParallaxLayout.
 */
class ParallaxView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null, defStyleAttr: Int = 0) :
        CardView(context, attrs, defStyleAttr) {

    private var pendingDownAnimation = false
    private val viewMovementDistance: Int

    private var parallaxMovementDistance: Int
    private var firstLayerAppliesParallax = false
    var touchMode: Int

    private val zAnimatorUp = ValueAnimator.ofFloat(dip(MIN_VIEW_ELEVATION), dip(MAX_VIEW_ELEVATION)).apply {
        addUpdateListener { super.setCardElevation(it.animatedValue as Float) }
    }

    private val zAnimatorDown = ValueAnimator.ofFloat(dip(MAX_VIEW_ELEVATION), dip(MIN_VIEW_ELEVATION)).apply {
        addUpdateListener { super.setCardElevation(it.animatedValue as Float) }
    }

    init {
        super.setMaxCardElevation(dip(MAX_VIEW_ELEVATION))
        super.setCardElevation(dip(MIN_VIEW_ELEVATION))
        super.setPreventCornerOverlap(false)
        super.setUseCompatPadding(true)
        radius = dip(4)

        viewMovementDistance = dip(VIEW_MOVEMENT_FACTOR).toInt()
        cameraDistance = (6000f * resources.displayMetrics.density)

        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.ParallaxView)
        parallaxMovementDistance = typedArray.getDimensionPixelSize(R.styleable.ParallaxView_parallaxMovementDistance,
                dip(PARALLAX_MOVEMENT_FACTOR).toInt())
        touchMode = typedArray.getInt(R.styleable.ParallaxView_touchMode, TOUCH_MODE_PRESSED)
        firstLayerAppliesParallax = typedArray.getBoolean(R.styleable.ParallaxView_firstLayerAppliesParallax, false)
        typedArray.recycle()

    }

    fun addLayer(layerView: LayerView) {
        addView(layerView)
        applyChildrenMargins()
    }

    override fun setCardElevation(elevation: Float) {
        //NO OP
    }

    override fun setMaxCardElevation(maxElevation: Float) {
        //NO OP
    }

    override fun setPreventCornerOverlap(preventCornerOverlap: Boolean) {
        //NO OP
    }

    override fun setUseCompatPadding(useCompatPadding: Boolean) {
        //NO OP
    }

    /**
     * Change parallax move distance for every layer.
     * Note: this will lead to a new margin computing for all the layers
     */
    fun setParallaxMovementDistance(@Px parallaxMovementDistance: Int) {
        this.parallaxMovementDistance = parallaxMovementDistance
        applyChildrenMargins()
    }

    fun getParallaxMovementDistance() = parallaxMovementDistance

    fun firstLayerAppliesParallax(applies: Boolean) {
        this.firstLayerAppliesParallax = applies
        applyChildrenMargins()
    }

    fun doesFirstLayerAppliesParallax() = firstLayerAppliesParallax

    /**
     * Do not let View to take padding in order to allow Layers to take
     * negative margin to make a correct parallax effect
     */
    override fun setPadding(left: Int, top: Int, right: Int, bottom: Int) {
        //NO OP
    }

    override fun onTouchEvent(event: MotionEvent?): Boolean {

        event?.let { motionEvent ->
            when (motionEvent.actionMasked) {
                MotionEvent.ACTION_DOWN -> {

                    zAnimatorUp.start()
                    pendingDownAnimation = true

                    val originalTouchPoint = getRotationPoint(motionEvent)
                    val touchModeFormattedPoint = mapPointForTouchMode(originalTouchPoint)

                    animate().rotationY(touchModeFormattedPoint.x * VIEW_ROTATION_ANGLE)
                            .rotationX(touchModeFormattedPoint.y * VIEW_ROTATION_ANGLE)
                            .translationX(originalTouchPoint.x * viewMovementDistance)
                            .translationY(originalTouchPoint.y * viewMovementDistance)
                            .setInterpolator(DecelerateInterpolator())
                            .withEndAction({ pendingDownAnimation = false })
                            .start()

                    forEachInstanceIndexed<LayerView> { index, v ->
                        v.animate().translationX(originalTouchPoint.x * getParallaxMovementFactor(index))
                                .translationY(originalTouchPoint.y * getParallaxMovementFactor(index))
                                .setInterpolator(DecelerateInterpolator())
                                .start()
                    }

                }
                MotionEvent.ACTION_UP,
                MotionEvent.ACTION_CANCEL -> {
                    zAnimatorDown.start()
                    animate().rotationY(0f)
                            .rotationX(0f)
                            .translationY(0f)
                            .translationX(0f)
                            .setInterpolator(DecelerateInterpolator())
                            .start()

                    forEachInstance<LayerView> {
                        it.animate().translationY(0f)
                                .translationX(0f)
                                .setInterpolator(DecelerateInterpolator())
                                .start()
                    }
                }
                MotionEvent.ACTION_MOVE -> {
                    if (pendingDownAnimation) {
                        pendingDownAnimation = false
                        animate().cancel()
                    }

                    val originalTouchPoint = getRotationPoint(motionEvent)
                    val touchModeFormattedPoint = mapPointForTouchMode(originalTouchPoint)

                    rotationY = touchModeFormattedPoint.x * VIEW_ROTATION_ANGLE
                    rotationX = touchModeFormattedPoint.y * VIEW_ROTATION_ANGLE
                    translationX = originalTouchPoint.x * viewMovementDistance
                    translationY = originalTouchPoint.y * viewMovementDistance

                    forEachInstanceIndexed<LayerView> { index, v ->
                        v.translationX = originalTouchPoint.x * getParallaxMovementFactor(index)
                        v.translationY = originalTouchPoint.y * getParallaxMovementFactor(index)
                    }

                }
            }
        }

        return true
    }

    override fun onFinishInflate() {
        super.onFinishInflate()
        applyChildrenMargins()
    }

    override fun onAttachedToWindow() {
        super.onAttachedToWindow()
        if (parent !is ParallaxContainer) throw IllegalStateException("ParallaxView can be only" +
                "child of ParallaxContainer")
    }

    override fun drawChild(canvas: Canvas?, child: View?, drawingTime: Long): Boolean {
        if (child is LayerView) return super.drawChild(canvas, child, drawingTime)
        else throw IllegalStateException("Only LayerViews can be added as children")
    }

    /**
     * invalidate every child to set correct margins again if needed
     *every view will apply a negative margin equals to the max translation possible
     *to ensure a correct parallax effect
     */
    private fun applyChildrenMargins() {

        forEachInstanceIndexed<LayerView> { index, v ->
            v.layoutParams = LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT).apply {
                val offsetMargin =
                        if (v.useParallaxPadding) -getParallaxMovementFactor(index).toInt()
                        else 0
                setMargins(offsetMargin, offsetMargin, offsetMargin, offsetMargin)
            }
        }
    }


    /**
     * Returns parallax movement to apply to layer based on its Z index
     *
     * Note: Use (1 - index / (childCount.toFloat() - 1) to apply 1.0 mov factor to first view
     * and 0.0 to last view.
     * Use (1 - index / (childCount.toFloat()) to apply 1.0 mov factor to first view
     * and a little factor to last view too
     */
    private fun getParallaxMovementFactor(viewIndex: Int) =
            parallaxMovementDistance * (1 - viewIndex / (childCount.toFloat() -
                    (if (firstLayerAppliesParallax) 0 else 1)))


    /**
     * Returns a PointF with normalized value between 0-1 in X and Y in order to apply rotation.
     * Values are normalized such as every view quadrant gets values between 0-1
     * ___________
     * |1  0|0  1|
     * |1__0|0__1|
     * |1  0|0  1|
     * |1__0|0__1|
     *
     */
    private fun getRotationPoint(motionEvent: MotionEvent): PointF {
        //first we should normalize values between 0 and 1
        val normalizedX = Math.max(MIN_VALUE, Math.min(TOP_VALUE, motionEvent.x / width))
        val normalizedY = Math.max(MIN_VALUE, Math.min(TOP_VALUE, motionEvent.y / height))

        //now we have to map [0.5 - 1] range to [0-1] as we have rotate the view from the center axis,
        //so 0.5 will ve our virtually center of reference, values > 0.5 will rotate towards and values
        // <0.5 will rotate backwards
        val formattedX = mapRange(normalizedX, TOP_VALUE / 2, TOP_VALUE,
                MIN_VALUE, TOP_VALUE)
        val formattedY = mapRange(normalizedY, TOP_VALUE / 2, TOP_VALUE,
                MIN_VALUE, TOP_VALUE)
        return PointF(formattedX, formattedY)
    }

    private fun mapPointForTouchMode(touchPoint: PointF) =
            when (touchMode) {
                TOUCH_MODE_PRESSED -> PointF(touchPoint.x, -touchPoint.y)
                TOUCH_MODE_LIFTED -> PointF(-touchPoint.x, touchPoint.y)
                else -> touchPoint
            }

    /**
     * Map the value contained in [minSrc-maxSrc] range to [minDst-maxDst] range
     */
    private fun mapRange(value: Float, minSrc: Float, maxSrc: Float, minDst: Float, maxDst: Float) =
            (value - minSrc) / (maxSrc - minSrc) * (maxDst - minDst) + minDst

    companion object {
        //max angle that view can rotate while being touched
        const val VIEW_ROTATION_ANGLE = 5f //degrees
        //max val that view will move following user's finger direction
        const val VIEW_MOVEMENT_FACTOR = 15 //dp

        //parallax movement distance
        const val PARALLAX_MOVEMENT_FACTOR = 5//dp

        const val MAX_VIEW_ELEVATION = 8
        const val MIN_VIEW_ELEVATION = 2

        const val TOP_VALUE = 1f
        const val MIN_VALUE = 0f

        const val TOUCH_MODE_PRESSED = 0
        const val TOUCH_MODE_LIFTED = 1

    }
}