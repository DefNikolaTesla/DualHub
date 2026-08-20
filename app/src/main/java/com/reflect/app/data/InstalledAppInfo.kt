package com.reflect.app.data

import android.graphics.drawable.Drawable

data class InstalledAppInfo(
    val packageName: String,
    val label: String,
    val icon: Drawable?
)
