package com.rsinukov.shutterstockclient.utils

import android.content.Context
import android.support.annotation.DrawableRes
import android.support.v4.content.ContextCompat

fun Context.getDrawableCompat(@DrawableRes id: Int) = ContextCompat.getDrawable(this, id)

fun Context.dpToPx(dp: Int): Int = (this.resources.displayMetrics.density * dp).toInt()
