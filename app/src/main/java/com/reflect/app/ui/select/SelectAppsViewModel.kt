package com.reflect.app.ui.select

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.reflect.app.data.InstalledAppInfo
import com.reflect.app.data.RestrictedAppsRepository
import com.reflect.app.util.AppIconLoader
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SelectAppsViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = RestrictedAppsRepository.getInstance(app)

    private val _allApps = MutableStateFlow<List<InstalledAppInfo>>(emptyList())
    private val _query = MutableStateFlow("")
    private val _selected = MutableStateFlow<Set<String>>(emptySet())
    private val _loading = MutableStateFlow(true)

    val loading: StateFlow<Boolean> = _loading.asStateFlow()
    val query: StateFlow<String> = _query.asStateFlow()
    val selected: StateFlow<Set<String>> = _selected.asStateFlow()

    val filteredApps: StateFlow<List<InstalledAppInfo>> =
        combine(_allApps, _query) { apps, q ->
            if (q.isBlank()) apps else apps.filter { it.label.contains(q, ignoreCase = true) }
        }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    init {
        viewModelScope.launch {
            _selected.value = repo.restrictedPackages.first()
            val apps = withContext(Dispatchers.IO) { AppIconLoader.getLaunchableApps(app) }
            _allApps.value = apps
            _loading.value = false
        }
    }

    fun onQueryChange(q: String) {
        _query.value = q
    }

    fun toggle(packageName: String) {
        _selected.value = if (packageName in _selected.value) {
            _selected.value - packageName
        } else {
            _selected.value + packageName
        }
    }

    fun save() {
        viewModelScope.launch {
            repo.setRestrictedPackages(_selected.value)
        }
    }
}
