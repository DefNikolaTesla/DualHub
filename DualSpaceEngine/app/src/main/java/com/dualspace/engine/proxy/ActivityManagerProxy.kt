package com.dualspace.engine.proxy

import android.content.Context
import android.util.Log

/**
 * ActivityManagerProxy
 *
 * Parallel to PackageManagerProxy. In a complete engine this intercepts
 * IActivityManager so that:
 *  - startActivity requests for virtual packages are rewritten
 *  - process names and UIDs are mapped into the virtual space
 *  - services and content providers of clones are isolated
 *
 * Same reality as PackageManagerProxy: requires deep reflection hooks that
 * are version-sensitive. This class exists so the architecture is clear
 * and the extension points are obvious.
 */
class ActivityManagerProxy(private val context: Context) {

    fun installHooks() {
        Log.i(TAG, "ActivityManagerProxy.installHooks() called")
        Log.w(TAG, "Full IActivityManager proxy is not activated in this foundation build.")
        Log.w(TAG, "Commercial engines replace the ActivityManager binder via reflection.")
    }

    companion object {
        private const val TAG = "ActivityManagerProxy"
    }
}
