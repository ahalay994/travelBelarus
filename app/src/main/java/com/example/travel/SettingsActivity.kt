package com.example.travel

import android.content.Context
import android.os.Bundle
import android.view.MenuItem
import androidx.preference.ListPreference
import androidx.preference.PreferenceFragmentCompat
import com.example.travel.utility.*

class SettingsActivity : BaseActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.settings_activity)
        if (savedInstanceState == null) {
            supportFragmentManager
                .beginTransaction()
                .replace(R.id.settings, SettingsFragment())
                .commit()
        }
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = getString(R.string.action_settings)
    }

    class SettingsFragment : PreferenceFragmentCompat() {
        companion object {
            const val LANGUAGE_RU = "ru"
            const val LANGUAGE_EN = "en"
            const val THEME_NIGHT = "night"
        }

        override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey)

            val sharedPreferences = requireContext().getSharedPreferences(PREF_DB_NAME, Context.MODE_PRIVATE)
            val language = sharedPreferences.getString(PREF_TITLE_LANG, LANGUAGE_DEFAULT)
            val theme = sharedPreferences.getString(PREF_TITLE_THEME, THEME_DEFAULT)

            val languagePreferences: ListPreference? = findPreference("appLanguage")
            languagePreferences?.let {
                initLangPrefVal(language!!, it)
                it.setOnPreferenceChangeListener { _, newValue ->
                    handleChangeLanguage(newValue.toString())
                    true
                }
            }
            val themePreferences: ListPreference? = findPreference(PREF_TITLE_THEME)
            themePreferences?.let {
                initThemePrefVal(it, theme!!)
                it.setOnPreferenceChangeListener { _, newValue ->
                    handleThemeSwitch(newValue.toString())
                    true
                }
            }

        }

        private fun initThemePrefVal(it: ListPreference, theme: String) {
            val array = requireContext().resources.getStringArray(R.array.theme_array)
            it.value = when (theme) {
                THEME_DEFAULT -> array[0]
                else -> array[1]
            }
        }

        private fun initLangPrefVal(language: String, it: ListPreference) {
            val array = requireContext().resources.getStringArray(R.array.language_array)
            val langCode = when (language) {
                LANGUAGE_RU -> array[0]
                LANGUAGE_EN -> array[1]
                else -> array[0]
            }
            it.value = langCode.toString()
        }

        private fun handleThemeSwitch(newTheme: String) {
            val array = requireContext().resources.getStringArray(R.array.theme_array)
            val theme = when (newTheme) {
                array[0] -> THEME_DEFAULT
                else -> THEME_NIGHT
            }

            val sharedPref = requireContext().getSharedPreferences(PREF_DB_NAME, Context.MODE_PRIVATE) ?: return
            with (sharedPref.edit()) {
                putString(PREF_TITLE_THEME, theme)
                apply()
            }
            requireActivity().recreate()
        }

        private fun handleChangeLanguage(newLang: String) {
            val array = requireContext().resources.getStringArray(R.array.language_array)
            val langCode = when (newLang) {
                array[0] -> LANGUAGE_RU
                array[1] -> LANGUAGE_EN
                else -> LANGUAGE_RU
            }

            val sharedPref = requireContext().getSharedPreferences(PREF_DB_NAME, Context.MODE_PRIVATE) ?: return
            with (sharedPref.edit()) {
                putString(PREF_TITLE_LANG, langCode)
                apply()
            }
            requireActivity().recreate()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }
}