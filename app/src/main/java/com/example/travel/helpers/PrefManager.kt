package com.example.travel.helpers

import android.content.Context
import android.content.SharedPreferences
import android.util.Log

class PrefManager(private val mContext: Context) {
    private var editor: SharedPreferences.Editor? = null
    private var prefs: SharedPreferences? = null

    private val LANGUAGE = "language"
    private val PREF = "user_info"

    var language: String?
        get() {
            return this.prefs!!.getString(LANGUAGE, "en")
        }
        set(language) {
            this.editor = this.mContext.getSharedPreferences(PREF, Context.MODE_PRIVATE).edit()
            this.editor!!.putString(LANGUAGE, language)
            this.editor!!.apply()
            Log.d("TAG", "Should be saved")
        }

    fun regListener(listener: SharedPreferences.OnSharedPreferenceChangeListener) {
        this.prefs = this.mContext.getSharedPreferences(PREF, Context.MODE_PRIVATE)
        /*this.prefs!!.registerOnSharedPreferenceChangeListener { sharedPreferences: SharedPreferences, s: String ->
            Log.d("TAG", "Listener Fired: $s")
        }*/

        this.prefs!!.registerOnSharedPreferenceChangeListener(listener)

    }

}