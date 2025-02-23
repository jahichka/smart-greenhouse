package com.example.greenhouse

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import java.util.Locale

class LanguageManager(private val context: Context) {

    fun setLocale(languageCode: String) {
        saveLanguage(languageCode)

        val locale = Locale(languageCode)
        Locale.setDefault(locale)

        val config = Configuration(context.resources.configuration)
        config.setLocale(locale)

        context.createConfigurationContext(config)
        context.resources.updateConfiguration(config, context.resources.displayMetrics)

        context.applicationContext.resources.updateConfiguration(
            config,
            context.applicationContext.resources.displayMetrics
        )
    }

    fun getLanguage(): String {
        val sharedPreferences = context.getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        return sharedPreferences.getString("Language", "en") ?: "en"
    }

    private fun saveLanguage(languageCode: String) {
        val sharedPreferences = context.getSharedPreferences("AppSettings", Context.MODE_PRIVATE)
        sharedPreferences.edit().putString("Language", languageCode).apply()
    }
}
