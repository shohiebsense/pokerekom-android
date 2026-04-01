package com.shohiebsense.pokerekom.data.local

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

private val Context.dataStore by preferencesDataStore(name = "pokerekom_prefs")

@Singleton
class PrefsDataStore @Inject constructor(
    private val context: Context
) {
    companion object {
        val LAST_TEAM_JSON = stringPreferencesKey("last_team_json")
    }

    suspend fun saveLastTeamJson(json: String) {
        context.dataStore.edit { prefs ->
            prefs[LAST_TEAM_JSON] = json
        }
    }

    suspend fun getLastTeamJson(): String? {
        val prefs = context.dataStore.data.first()
        return prefs[LAST_TEAM_JSON]
    }
}
