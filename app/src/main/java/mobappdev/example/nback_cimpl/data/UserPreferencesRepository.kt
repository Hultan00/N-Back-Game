package mobappdev.example.nback_cimpl.data

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import mobappdev.example.nback_cimpl.ui.viewmodels.GridType
import java.io.IOException

/**
 * This repository provides a way to interact with the DataStore api,
 * with this API you can save key:value pairs
 *
 * Currently this file contains only one thing: getting the highscore as a flow
 * and writing to the highscore preference.
 * (a flow is like a waterpipe; if you put something different in the start,
 * the end automatically updates as long as the pipe is open)
 *
 * Date: 25-08-2023
 * Version: Skeleton code version 1.0
 * Author: Yeetivity
 *
 */

class UserPreferencesRepository(
    private val dataStore: DataStore<Preferences>
) {
    private companion object {
        val HIGHSCORE = intPreferencesKey("highscore")
        val N_BACK = intPreferencesKey("nBack")
        val GRID_TYPE = stringPreferencesKey("gridType")
        val BLINK_DURATION = longPreferencesKey("blinkDuration")
        val EVENT_INTERVAL = longPreferencesKey("eventInterval")
        val NUMBER_OF_EVENTS = intPreferencesKey("numberOfEvents")
        const val TAG = "UserPreferencesRepo"
    }

    val highscore: Flow<Int> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                Log.e(TAG, "Error reading preferences", exception)
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[HIGHSCORE] ?: 0
        }

    val nBack: Flow<Int> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[N_BACK] ?: 1
        }

    val gridType: Flow<String> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                Log.e(TAG, "Error reading preferences", exception)
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[GRID_TYPE] ?: GridType.Grid_3x3.name
        }

    val blinkDuration: Flow<Long> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                Log.e(TAG, "Error reading preferences", exception)
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[BLINK_DURATION] ?: 1000L
        }

    val eventInterval: Flow<Long> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                Log.e(TAG, "Error reading preferences", exception)
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[EVENT_INTERVAL] ?: 2000L
        }

    val numberOfEvents: Flow<Int> = dataStore.data
        .catch { exception ->
            if (exception is IOException) {
                emit(emptyPreferences())
            } else {
                throw exception
            }
        }
        .map { preferences ->
            preferences[NUMBER_OF_EVENTS] ?: 10
        }

    suspend fun saveHighScore(score: Int) {
        dataStore.edit { preferences ->
            preferences[HIGHSCORE] = score
        }
    }

    suspend fun saveNBack(nBack: Int) {
        dataStore.edit { preferences ->
            preferences[N_BACK] = nBack
            Log.d(TAG, "Saved nBack value: $nBack")
        }
    }

    suspend fun saveGridType(gridType: GridType) {
        dataStore.edit { preferences ->
            preferences[GRID_TYPE] = gridType.name
        }
    }

    suspend fun saveBlinkDuration(blinkDuration: Long) {
        dataStore.edit { preferences ->
            preferences[BLINK_DURATION] = blinkDuration
        }
    }

    suspend fun saveEventInterval(eventInterval: Long) {
        dataStore.edit { preferences ->
            preferences[EVENT_INTERVAL] = eventInterval
        }
    }

    suspend fun saveNumberOfEvents(numOfEvents: Int) {
        dataStore.edit { preferences ->
            preferences[NUMBER_OF_EVENTS] = numOfEvents
        }
    }
}
