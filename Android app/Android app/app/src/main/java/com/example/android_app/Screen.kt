package com.example.android_app

sealed class Screen(val route: String)
{
    object MainScreen : Screen(route = "main_screen") //first screen
    object RegisterScreen : Screen(route = "register_screen")
    object LoginScreen : Screen(route = "login_screen") //login
    object PredictionScreen : Screen(route = "prediction_screen") //add text for prediction
    object HistoryScreen : Screen(route = "history_screen")
    object SettingsScreen : Screen(route = "settings_screen") //settings screen

}