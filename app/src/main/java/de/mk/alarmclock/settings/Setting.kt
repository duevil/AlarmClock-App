package de.mk.alarmclock.settings

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import kotlinx.coroutines.flow.map

class Setting<T>(
    private val dataStore: DataStore<Preferences>,
    private val key: Preferences.Key<T>,
    val default: T,
) {
    val flow = dataStore.data.map { it[key] ?: default }

    suspend fun set(value: T) {
        dataStore.edit { it[key] = value }
    }

    suspend fun remove() {
        dataStore.edit { it.remove(key) }
    }

    override fun toString() = key.name
}