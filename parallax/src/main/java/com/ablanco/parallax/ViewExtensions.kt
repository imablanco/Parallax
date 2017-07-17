package com.ablanco.parallax

import android.content.Context
import android.view.View
import android.view.ViewGroup


/**
 * Created by Álvaro Blanco Cabrero on 13/07/2017.
 * ParallaxLayout.
 */

fun View.padding(padding: Int) = setPadding(padding, padding, padding, padding)

fun View.dip(size: Int) = size * context.resources.displayMetrics.density
fun Context.dip(size: Int) = size * resources.displayMetrics.density

val ViewGroup.children: List<View>
    get() = (0..childCount - 1).map { getChildAt(it) }

inline fun <reified T : View> ViewGroup.forEachInstance(block: (v: T) -> Unit) =
        children.filterIsInstance<T>().forEach { block(it) }

inline fun <reified T : View> ViewGroup.forEachInstanceIndexed(block: (index: Int, v: T) -> Unit) =
        children.filterIsInstance<T>().forEachIndexed { index, it -> block(index, it) }
