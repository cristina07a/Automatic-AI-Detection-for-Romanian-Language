package com.example.android_app

import androidx.compose.runtime.*
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.android_app.pages.HistoryScreen
import com.example.android_app.pages.LoginScreen
import com.example.android_app.pages.MainScreen
import com.example.android_app.pages.PredictionScreen
import com.example.android_app.pages.RegisterScreen
import com.example.android_app.pages.SettingsScreen

@Composable
fun Navigation(authViewModel: AuthViewModel) {
    val navController = rememberNavController()

    NavHost(navController = navController, startDestination = Screen.MainScreen.route) {
        composable(route = Screen.MainScreen.route) {
            MainScreen(navController, authViewModel)
        }
        composable(route = Screen.LoginScreen.route) {
            LoginScreen(navController, authViewModel)
        }

        composable(route = Screen.PredictionScreen.route) {
            PredictionScreen(navController, authViewModel)
        }

        composable(route = Screen.RegisterScreen.route) {
            RegisterScreen(navController, authViewModel)
        }
        composable(route = Screen.HistoryScreen.route) {
            HistoryScreen(navController, authViewModel)
        }

        composable(route = Screen.SettingsScreen.route) {
            SettingsScreen(navController, authViewModel)
        }
    }
}