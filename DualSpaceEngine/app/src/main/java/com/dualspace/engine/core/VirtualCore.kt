package com.dualspace.engine.core

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.util.Log
import com.dualspace.engine.model.VirtualAppInfo
import com.dualspace.engine.rewrite.ApkRewriter
import java.io.File
import java.util.concurrent.ConcurrentHashMap

/**
 * VirtualCore – central singleton of the dual-space engine.
 *
 * This is the heart of a VirtualApp-style multi-instance system.
 *
 * Responsibilities:
 *  - Own the virtual environment lifecycle
 *  - Keep a registry of all cloned (virtual) packages
 *  - Provide install / launch / uninstall entry points
 *  - Coordinate with ApkRewriter and future proxies
 *
 * IMPORTANT LIMITATIONS (read carefully):
 *  - Full PackageManager / ActivityManager proxying requires heavy reflection
 *    and is extremely version-sensitive (Android 14/15/16 break hooks often).
 *  - This foundation focuses on the structure, registry, APK rewrite path,
 *    and isolated data directories. The deepest process-level hooks still
 *    need device-specific hardening and testing.
 *  - On a real device you must still handle installation of the rewritten APK
 *    (usually via PackageInstaller session or a privileged path).
 */
class VirtualCore private constructor() {

    private lateinit var appContext: Context
    private lateinit var packageManager: PackageManager
    private lateinit var rewriter: ApkRewriter

    /** virtualPackage → VirtualAppInfo */
    private val virtualApps = ConcurrentHashMap<String, VirtualAppInfo>()

    /** originalPackage → list of virtual packages */
    private val originalToVirtual = ConcurrentHashMap<String, MutableList<String>>()

    private var started = false

    fun startup(context: Context) {
        if (started) return
        appContext = context.applicationContext
        packageManager = appContext.packageManager
        rewriter = ApkRewriter(appContext)

        // Ensure our private virtual data root exists
        val root = getVirtualRoot()
        if (!root.exists()) {
            root.mkdirs()
        }

        // Load any previously registered clones from simple persistence
        loadRegistry()

        started = true
        Log.i(TAG, "VirtualCore startup complete. Virtual root: ${root.absolutePath}")
    }

    fun isStarted(): Boolean = started

    /**
     * Returns the private directory that holds all virtual app data and rewritten APKs.
     * Example: /data/data/com.dualspace.engine/files/virtual/
     */
    fun getVirtualRoot(): File {
        return File(appContext.filesDir, "virtual")
    }

    /**
     * Data directory for one specific virtual package.
     * This is the isolation boundary.
     */
    fun getDataDir(virtualPackage: String): File {
        val dir = File(getVirtualRoot(), "data/$virtualPackage")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    /**
     * Directory where rewritten APKs are stored.
     */
    fun getApkDir(): File {
        val dir = File(getVirtualRoot(), "apk")
        if (!dir.exists()) dir.mkdirs()
        return dir
    }

    /**
     * High-level clone entry point.
     *
     * 1. Locate the original APK of [originalPackage]
     * 2. Ask ApkRewriter to produce a new APK with a unique virtual package name
     * 3. Register the clone in memory + simple persistence
     * 4. Return the new VirtualAppInfo
     *
     * NOTE: Actual installation of the rewritten APK into a runnable state
     * still requires either a PackageInstaller flow or deeper engine hooks.
     * This method prepares everything the engine needs.
     */
    fun cloneApp(originalPackage: String): Result<VirtualAppInfo> {
        if (!started) return Result.failure(IllegalStateException("VirtualCore not started"))

        return try {
            val appInfo: ApplicationInfo = packageManager.getApplicationInfo(originalPackage, 0)
            val sourceApk = File(appInfo.sourceDir)
            if (!sourceApk.exists()) {
                return Result.failure(IllegalStateException("Source APK not found: ${appInfo.sourceDir}"))
            }

            val existing = originalToVirtual[originalPackage] ?: emptyList()
            val nextIndex = existing.size + 1
            val virtualPackage = "$originalPackage.ds$nextIndex"   // e.g. com.whatsapp.ds1

            Log.i(TAG, "Cloning $originalPackage → $virtualPackage (index $nextIndex)")

            val rewrittenApk = rewriter.rewrite(
                sourceApk = sourceApk,
                newPackageName = virtualPackage,
                outputDir = getApkDir()
            )

            val label = packageManager.getApplicationLabel(appInfo).toString() + " #$nextIndex"
            val icon = packageManager.getApplicationIcon(appInfo)
            val dataDir = getDataDir(virtualPackage).absolutePath

            val info = VirtualAppInfo(
                originalPackage = originalPackage,
                virtualPackage = virtualPackage,
                label = label,
                icon = icon,
                cloneIndex = nextIndex,
                dataDir = dataDir
            )

            virtualApps[virtualPackage] = info
            originalToVirtual.getOrPut(originalPackage) { mutableListOf() }.add(virtualPackage)
            persistRegistry()

            Log.i(TAG, "Clone registered: $virtualPackage → data: $dataDir")
            Log.i(TAG, "Rewritten APK at: ${rewrittenApk.absolutePath}")

            Result.success(info)
        } catch (t: Throwable) {
            Log.e(TAG, "cloneApp failed for $originalPackage", t)
            Result.failure(t)
        }
    }

    fun getAllVirtualApps(): List<VirtualAppInfo> {
        return virtualApps.values.sortedBy { it.label.lowercase() }
    }

    fun getVirtualAppsFor(originalPackage: String): List<VirtualAppInfo> {
        return (originalToVirtual[originalPackage] ?: emptyList())
            .mapNotNull { virtualApps[it] }
    }

    fun removeClone(virtualPackage: String): Boolean {
        val info = virtualApps.remove(virtualPackage) ?: return false
        originalToVirtual[info.originalPackage]?.remove(virtualPackage)
        // Delete data + rewritten APK
        getDataDir(virtualPackage).deleteRecursively()
        File(getApkDir(), "$virtualPackage.apk").delete()
        persistRegistry()
        Log.i(TAG, "Removed clone $virtualPackage")
        return true
    }

    /**
     * Placeholder launch entry.
     * In a full engine this would:
     *  - Ensure the rewritten APK is loaded into the virtual ClassLoader / process
     *  - Start the main activity under the virtual package identity
     *  - Apply all PackageManager / ActivityManager hooks
     *
     * For this foundation we only log and return the intended target.
     * Real launch requires the proxy layer to be fully hooked.
     */
    fun launchVirtualApp(virtualPackage: String): Boolean {
        val info = virtualApps[virtualPackage] ?: return false
        Log.i(TAG, "Launch requested for $virtualPackage (original=${info.originalPackage})")
        // TODO: full process start + activity launch under virtual identity
        // This is the part that needs the deep proxy + ClassLoader work.
        return true
    }

    // ---------- simple persistence (SharedPreferences style) ----------

    private fun persistRegistry() {
        val prefs = appContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putStringSet(KEY_VIRTUAL_PACKAGES, virtualApps.keys)
        virtualApps.forEach { (vp, info) ->
            editor.putString("$KEY_PREFIX$vp", "${info.originalPackage}|${info.cloneIndex}|${info.label}")
        }
        editor.apply()
    }

    private fun loadRegistry() {
        val prefs = appContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val packages = prefs.getStringSet(KEY_VIRTUAL_PACKAGES, emptySet()) ?: return
        packages.forEach { vp ->
            val raw = prefs.getString("$KEY_PREFIX$vp", null) ?: return@forEach
            val parts = raw.split("|")
            if (parts.size >= 3) {
                val original = parts[0]
                val index = parts[1].toIntOrNull() ?: 1
                val label = parts[2]
                val info = VirtualAppInfo(
                    originalPackage = original,
                    virtualPackage = vp,
                    label = label,
                    icon = null, // icons reloaded on demand later
                    cloneIndex = index,
                    dataDir = getDataDir(vp).absolutePath
                )
                virtualApps[vp] = info
                originalToVirtual.getOrPut(original) { mutableListOf() }.add(vp)
            }
        }
        Log.i(TAG, "Loaded ${virtualApps.size} virtual apps from registry")
    }

    companion object {
        private const val TAG = "VirtualCore"
        private const val PREFS = "dualspace_registry"
        private const val KEY_VIRTUAL_PACKAGES = "virtual_packages"
        private const val KEY_PREFIX = "info_"

        @Volatile
        private var INSTANCE: VirtualCore? = null

        fun get(): VirtualCore {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: VirtualCore().also { INSTANCE = it }
            }
        }
    }
}
