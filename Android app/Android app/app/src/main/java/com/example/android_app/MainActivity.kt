package com.example.android_app

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels

class MainActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context?) {
        val prefs = newBase?.getSharedPreferences("settings", Context.MODE_PRIVATE)
        val savedLanguage = prefs?.getString("app_language", "English") ?: "English"
        val context = LocaleHelper.setLocale(newBase!!, savedLanguage)
        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val authViewModel: AuthViewModel by viewModels()
        setContent {
            Navigation(authViewModel)
        }
    }
}