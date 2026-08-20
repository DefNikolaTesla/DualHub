package com.reflect.app.data

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "reflect_prefs")

class RestrictedAppsRepository(private val context: Context) {

    private val keyRestricted = stringSetPreferencesKey("restricted_packages")

    val restrictedPackages: Flow<Set<String>> =
        context.dataStore.data.map { it[keyRestricted] ?: emptySet() }

    suspend fun setRestrictedPackages(packages: Set<String>) {
        context.dataStore.edit { prefs ->
            prefs[keyRestricted] = packages
        }
    }

    suspend fun addPackage(pkg: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[keyRestricted] ?: emptySet()
            prefs[keyRestricted] = current + pkg
        }
    }

    suspend fun removePackage(pkg: String) {
        context.dataStore.edit { prefs ->
            val current = prefs[keyRestricted] ?: emptySet()
            prefs[keyRestricted] = current - pkg
        }
    }

    companion object {
        @Volatile private var instance: RestrictedAppsRepository? = null
        fun getInstance(context: Context): RestrictedAppsRepository =
            instance ?: synchronized(this) {
                instance ?: RestrictedAppsRepository(context.applicationContext).also { instance = it }
            }
    }
}
