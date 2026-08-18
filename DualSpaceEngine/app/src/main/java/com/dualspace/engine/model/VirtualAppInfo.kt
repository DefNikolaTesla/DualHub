package com.dualspace.engine.model

import android.graphics.drawable.Drawable

/**
 * Represents one virtual (cloned) instance of an app.
 *
 * @param originalPackage  the real package name on the device (e.g. com.whatsapp)
 * @param virtualPackage   the rewritten package name used inside the virtual env
 * @param label            display name
 * @param icon             launcher icon
 * @param cloneIndex       1, 2, 3... for multiple clones of the same original
 * @param dataDir          isolated data directory for this clone
 */
data class VirtualAppInfo(
    val originalPackage: String,
    val virtualPackage: String,
    val label: String,
    val icon: Drawable?,
    val cloneIndex: Int,
    val dataDir: String
)
