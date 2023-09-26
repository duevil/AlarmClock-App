package de.mk.alarmclock.settings

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore("ac_settings")

class Settings(context: Context) {
    val host = Setting(
        context.dataStore,
        stringPreferencesKey("host"),
        "0.0.0.0"
    )
    val port = Setting(
        context.dataStore,
        intPreferencesKey("port"),
        8080
    )
    val setupDone = Setting(
        context.dataStore,
        booleanPreferencesKey("setup"),
        false
    )
}