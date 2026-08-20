package com.reflect.app.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.reflect.app.data.RestrictedAppsRepository
import com.reflect.app.util.PermissionUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class HomeViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = RestrictedAppsRepository.getInstance(app)

    private val _restrictedCount = MutableStateFlow(0)
    val restrictedCount: StateFlow<Int> = _restrictedCount.asStateFlow()

    private val _permissionsGranted = MutableStateFlow(false)
    val permissionsGranted: StateFlow<Boolean> = _permissionsGranted.asStateFlow()

    init {
        viewModelScope.launch {
            repo.restrictedPackages.collect { _restrictedCount.value = it.size }
        }
    }

    fun refreshPermissions() {
        val context = getApplication<Application>()
        _permissionsGranted.value = PermissionUtils.hasUsageAccess(context) &&
                PermissionUtils.isAccessibilityServiceEnabled(context)
    }
}
