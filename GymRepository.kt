package com.homiesgym.app.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore("homies")

data class Workout(val id: String, val name: String, val muscle: String, val minutes: Int, val premium: Boolean = false)

data class UserStats(val workouts: Int, val streak: Int, val minutes: Int)

data class AppState(val premium: Boolean = false, val workouts: Int = 0, val streak: Int = 4, val minutes: Int = 0)

class GymRepository(private val context: Context) {
    private val premiumKey = booleanPreferencesKey("premium")
    private val workoutsKey = intPreferencesKey("workouts")
    private val minutesKey = intPreferencesKey("minutes")

    val state: Flow<AppState> = context.dataStore.data.map { p -> AppState(p[premiumKey] ?: false, p[workoutsKey] ?: 0, p[minutesKey] ?: 0) }
    suspend fun complete(minutes: Int) = context.dataStore.edit { p -> p[workoutsKey] = (p[workoutsKey] ?: 0) + 1; p[minutesKey] = (p[minutesKey] ?: 0) + minutes }
    suspend fun setPremium(value: Boolean) = context.dataStore.edit { it[premiumKey] = value }
}
