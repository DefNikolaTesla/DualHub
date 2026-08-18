package com.dualspace.engine

import android.app.Application
import android.util.Log
import com.dualspace.engine.core.VirtualCore

/**
 * Application entry point.
 * Boots the virtual environment as early as possible.
 */
class DualSpaceApp : Application() {

    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "DualSpaceApp starting – initializing VirtualCore")
        try {
            VirtualCore.get().startup(this)
            Log.i(TAG, "VirtualCore started successfully")
        } catch (t: Throwable) {
            Log.e(TAG, "Failed to start VirtualCore", t)
        }
    }

    companion object {
        private const val TAG = "DualSpaceApp"
    }
}
