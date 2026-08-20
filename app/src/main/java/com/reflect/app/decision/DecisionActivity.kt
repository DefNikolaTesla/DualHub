package com.reflect.app.decision

import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.drawable.Drawable
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.reflect.app.service.AppBlockAccessibilityService
import com.reflect.app.ui.theme.ReflectTheme

class DecisionActivity : ComponentActivity() {

    private var targetPackageName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val packageName = intent.getStringExtra(EXTRA_PACKAGE_NAME)
        if (packageName == null) {
            finish()
            return
        }
        targetPackageName = packageName

        val pm = packageManager
        val appLabel = try {
            val appInfo = pm.getApplicationInfo(packageName, 0)
            pm.getApplicationLabel(appInfo).toString()
        } catch (e: PackageManager.NameNotFoundException) {
            packageName
        }
        val appIcon: Drawable? = try {
            pm.getApplicationIcon(packageName)
        } catch (e: PackageManager.NameNotFoundException) {
            null
        }

        setContent {
            ReflectTheme {
                DecisionScreen(
                    appName = appLabel,
                    appIcon = appIcon,
                    onOpenAnyway = {
                        AppBlockAccessibilityService.instance?.markApproved(packageName)
                        openTargetApp(packageName)
                        finish()
                    },
                    onGoBack = {
                        goHome()
                        finish()
                    }
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        // singleInstance activity may be re-triggered for a different restricted app
        setIntent(intent)
        recreate()
    }

    private fun openTargetApp(packageName: String) {
        val launchIntent = packageManager.getLaunchIntentForPackage(packageName)
        launchIntent?.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        launchIntent?.let { startActivity(it) }
    }

    private fun goHome() {
        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(homeIntent)
    }

    override fun onBackPressed() {
        goHome()
        finish()
    }

    companion object {
        const val EXTRA_PACKAGE_NAME = "extra_package_name"
    }
}
