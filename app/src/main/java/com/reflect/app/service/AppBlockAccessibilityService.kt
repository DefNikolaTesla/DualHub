package com.reflect.app.service

import android.accessibilityservice.AccessibilityService
import android.content.Intent
import android.view.accessibility.AccessibilityEvent
import com.reflect.app.data.RestrictedAppsRepository
import com.reflect.app.decision.DecisionActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AppBlockAccessibilityService : AccessibilityService() {

    private lateinit var repository: RestrictedAppsRepository
    private val serviceJob = Job()
    private val serviceScope = CoroutineScope(Dispatchers.Default + serviceJob)

    private var lastHandledPackage: String? = null
    private var allowedOnceUntil: Long = 0L

    override fun onServiceConnected() {
        super.onServiceConnected()
        repository = RestrictedAppsRepository.getInstance(applicationContext)
        instance = this
    }

    override fun onAccessibilityEvent(event: AccessibilityEvent?) {
        if (event == null) return
        if (event.eventType != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return

        val pkg = event.packageName?.toString() ?: return

        if (pkg == applicationContext.packageName) {
            lastHandledPackage = null
            return
        }

        if (pkg == lastHandledPackage && System.currentTimeMillis() < allowedOnceUntil) {
            return
        }

        serviceScope.launch {
            val restricted = repository.restrictedPackages.first()
            if (pkg in restricted) {
                launchDecisionScreen(pkg)
            }
        }
    }

    private fun launchDecisionScreen(packageName: String) {
        val intent = Intent(this, DecisionActivity::class.java).apply {
            putExtra(DecisionActivity.EXTRA_PACKAGE_NAME, packageName)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or
                    Intent.FLAG_ACTIVITY_CLEAR_TOP or
                    Intent.FLAG_ACTIVITY_SINGLE_TOP
        }
        startActivity(intent)
    }

    fun markApproved(packageName: String) {
        lastHandledPackage = packageName
        allowedOnceUntil = System.currentTimeMillis() + 4000L
    }

    override fun onInterrupt() {}

    override fun onDestroy() {
        super.onDestroy()
        serviceJob.cancel()
        if (instance == this) instance = null
    }

    companion object {
        var instance: AppBlockAccessibilityService? = null
    }
}
