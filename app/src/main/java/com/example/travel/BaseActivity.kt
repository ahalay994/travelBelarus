package com.example.travel

import android.content.Context
import android.os.Bundle
import android.os.PersistableBundle
import androidx.appcompat.app.AppCompatActivity
import com.example.travel.utility.LocaleService

open class BaseActivity : AppCompatActivity() {
    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(
            LocaleService.updateBaseContextLocale(newBase)
        )
    }
}
