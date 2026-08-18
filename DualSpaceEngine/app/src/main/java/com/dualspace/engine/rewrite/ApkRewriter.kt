package com.dualspace.engine.rewrite

import android.content.Context
import android.util.Log
import net.dongliu.apk.parser.ApkFile
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipFile
import java.util.zip.ZipOutputStream

/**
 * ApkRewriter
 *
 * Core responsibility: take an original APK and produce a new APK with a
 * different package name so it can be treated as a separate application
 * inside the virtual environment.
 *
 * Real commercial engines perform deep binary rewriting of:
 *  - AndroidManifest.xml (binary XML)
 *  - resources.arsc
 *  - dex files (sometimes)
 *  - native libraries (rare)
 *
 * This foundation implements a practical, understandable pipeline:
 *  1. Copy the original APK
 *  2. Extract and rewrite the binary AndroidManifest package attribute
 *     (simplified approach using string replacement on the binary for demo)
 *  3. Re-pack the APK
 *  4. (Optional) re-sign – left as a clear extension point
 *
 * WARNING: Full production-grade manifest rewriting requires a proper
 * binary XML encoder/decoder (AAPT2 or a dedicated library). The method
 * below is intentionally transparent so you can see every step and replace
 * the manifest rewrite with a stronger implementation later.
 */
class ApkRewriter(private val context: Context) {

    /**
     * Rewrites [sourceApk] so that its package name becomes [newPackageName].
     * Returns the File of the newly written APK inside [outputDir].
     */
    fun rewrite(sourceApk: File, newPackageName: String, outputDir: File): File {
        require(sourceApk.exists()) { "Source APK does not exist: ${sourceApk.absolutePath}" }
        if (!outputDir.exists()) outputDir.mkdirs()

        val outFile = File(outputDir, "$newPackageName.apk")
        if (outFile.exists()) outFile.delete()

        Log.i(TAG, "Rewriting ${sourceApk.name} → $newPackageName")

        // Step 1: Read original package name for logging / validation
        val originalPackage = try {
            ApkFile(sourceApk).use { it.apkMeta.packageName }
        } catch (t: Throwable) {
            Log.w(TAG, "Could not parse original package via apk-parser", t)
            "unknown"
        }
        Log.i(TAG, "Original package: $originalPackage → New: $newPackageName")

        // Step 2: Create a new ZIP (APK) while rewriting the manifest entry
        ZipFile(sourceApk).use { zipIn ->
            ZipOutputStream(FileOutputStream(outFile)).use { zipOut ->
                val entries = zipIn.entries()
                while (entries.hasMoreElements()) {
                    val entry = entries.nextElement()
                    val name = entry.name

                    // Skip existing signature files – we will need to re-sign later
                    if (name.startsWith("META-INF/") &&
                        (name.endsWith(".SF") || name.endsWith(".RSA") || name.endsWith(".DSA") || name.endsWith(".EC") || name == "META-INF/MANIFEST.MF")
                    ) {
                        continue
                    }

                    val newEntry = ZipEntry(name)
                    zipOut.putNextEntry(newEntry)

                    zipIn.getInputStream(entry).use { input ->
                        if (name == "AndroidManifest.xml") {
                            // Extremely simplified binary manifest patch.
                            // A real engine replaces the package name string
                            // inside the binary XML structure correctly.
                            val originalBytes = input.readBytes()
                            val patched = patchPackageNameInBinaryManifest(
                                originalBytes,
                                originalPackage,
                                newPackageName
                            )
                            zipOut.write(patched)
                        } else {
                            input.copyTo(zipOut)
                        }
                    }
                    zipOut.closeEntry()
                }
            }
        }

        Log.i(TAG, "Rewritten APK written to ${outFile.absolutePath} (${outFile.length()} bytes)")
        Log.w(TAG, "NOTE: The new APK is currently UNSIGNED. You must sign it before installation.")
        Log.w(TAG, "Use apksigner or jarsigner, or implement signing inside this class.")

        return outFile
    }

    /**
     * Very naive binary patch: searches for the old package name bytes and
     * replaces them with the new ones when lengths match.
     * This is ONLY a teaching / foundation implementation.
     * Production code must use a proper binary XML rewriter.
     */
    private fun patchPackageNameInBinaryManifest(
        data: ByteArray,
        oldPackage: String,
        newPackage: String
    ): ByteArray {
        if (oldPackage == "unknown" || oldPackage.length != newPackage.length) {
            // Length mismatch – cannot do simple in-place replace.
            // In a real engine you would rebuild the binary XML.
            Log.w(TAG, "Package name length differs or unknown – skipping naive patch. " +
                    "Old len=${oldPackage.length}, new len=${newPackage.length}")
            return data
        }

        val oldBytes = oldPackage.toByteArray(Charsets.UTF_8)
        val newBytes = newPackage.toByteArray(Charsets.UTF_8)

        // Search for the sequence
        val result = data.copyOf()
        var i = 0
        while (i <= result.size - oldBytes.size) {
            var match = true
            for (j in oldBytes.indices) {
                if (result[i + j] != oldBytes[j]) {
                    match = false
                    break
                }
            }
            if (match) {
                for (j in newBytes.indices) {
                    result[i + j] = newBytes[j]
                }
                Log.i(TAG, "Patched package name at offset $i")
                i += oldBytes.size
            } else {
                i++
            }
        }
        return result
    }

    companion object {
        private const val TAG = "ApkRewriter"
    }
}
