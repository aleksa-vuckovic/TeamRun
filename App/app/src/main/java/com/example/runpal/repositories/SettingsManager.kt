package com.example.runpal.repositories

import android.content.Context
import android.content.SharedPreferences
import com.example.runpal.Formatter
import com.example.runpal.ImperialDistanceFormatter
import com.example.runpal.ImperialPaceFormatter
import com.example.runpal.ImperialSpeedFormatter
import com.example.runpal.ImperialWeightFormatter
import com.example.runpal.MetricDistanceFormatter
import com.example.runpal.MetricPaceFormatter
import com.example.runpal.MetricSpeedFormatter
import com.example.runpal.MetricWeightFormatter
import com.example.runpal.Units
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class SettingsManager @Inject constructor(
    private val loginManager: LoginManager,
    @ApplicationContext private val context: Context
) {

    companion object {
        private const val SHARED_PREFS_SETTINGS = "SETTINGS_"
        private const val SHARED_PREFS_SETTINGS_UNITS = "UNITS"
    }



    private val prefs: SharedPreferences
        get() = context.getSharedPreferences(SHARED_PREFS_SETTINGS + loginManager.currentUserId()!!, Context.MODE_PRIVATE)


    /**
     * Returned the preferred units
     */
    var units: Units
        get() = Units.valueOf(prefs.getString(SHARED_PREFS_SETTINGS_UNITS, Units.METRIC.name)!!)
        set(value) {prefs.edit().putString(SHARED_PREFS_SETTINGS_UNITS, value.name).apply()}
}