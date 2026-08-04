package com.example.android_app.pages

import android.content.Context
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.android_app.AuthViewModel
import com.example.android_app.R
import com.example.android_app.Screen
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SimpleLanguageSelector(
    label: String,
    selectedLang: String,
    onLangSelected: (String) -> Unit,
    languages: List<String>,
    textColor: Color = Color.White  // implicit alb
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(text = label, style = MaterialTheme.typography.bodyLarge, color = textColor)

        Box(
            modifier = Modifier
                .border(
                    width = 1.dp,
                    color = Color.White,
                    shape = MaterialTheme.shapes.small
                )
                .clickable { expanded = true }
                .width(130.dp)
                .padding(horizontal = 12.dp, vertical = 8.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(text = selectedLang, color = textColor)
                Icon(
                    imageVector = Icons.Filled.ArrowDropDown,
                    contentDescription = "Dropdown arrow",
                    tint = textColor
                )
            }

            DropdownMenu(
                expanded = expanded,
                onDismissRequest = { expanded = false },
                modifier = Modifier.fillMaxWidth()
            ) {
                languages.forEach { lang ->
                    DropdownMenuItem(
                        text = { Text(lang, color = Color.Black) }, // dropdown text - dacă vrei alb, schimbă aici
                        onClick = {
                            onLangSelected(lang)
                            expanded = false
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController, authViewModel: AuthViewModel) {
    val context = LocalContext.current
    val prefs = remember { context.getSharedPreferences("settings", Context.MODE_PRIVATE) }
    val languages = listOf("Română", "English")

    var selectedAppLang by remember {
        mutableStateOf(
            prefs.getString("app_language", "English") ?: "English"
        )
    }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            ModalDrawerSheet(
                modifier = Modifier.width(200.dp),
                drawerContainerColor = Color(0xFF2196F3)  // albastru ca in HistoryScreen
            ) {
                Spacer(modifier = Modifier.height(40.dp))
                Text(
                    stringResource(R.string.history).uppercase(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clickable {
                            navController.navigate(Screen.HistoryScreen.route)
                            scope.launch { drawerState.close() }
                        },
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    stringResource(R.string.predictions).uppercase(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clickable {
                            navController.navigate(Screen.PredictionScreen.route)
                            scope.launch { drawerState.close() }
                        },
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    stringResource(R.string.settings).uppercase(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clickable {
                            navController.navigate(Screen.SettingsScreen.route)
                            scope.launch { drawerState.close() }
                        },
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    stringResource(R.string.logout).uppercase(),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp)
                        .clickable {
                            authViewModel.signout()
                            navController.navigate(Screen.LoginScreen.route) {
                                popUpTo(Screen.PredictionScreen.route) { inclusive = true }
                            }
                            scope.launch { drawerState.close() }
                        },
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.settings), color = Color.White) },
                    colors = TopAppBarDefaults.topAppBarColors(containerColor = Color(0xFF2196F3)),
                    navigationIcon = {
                        IconButton(onClick = { scope.launch { drawerState.open() } }) {
                            Icon(
                                imageVector = Icons.Default.Menu,
                                contentDescription = "Menu",
                                tint = Color.White
                            )
                        }
                    }
                )
            }
        ) { paddingValues ->
            Box(modifier = Modifier.fillMaxSize()) {
                Image(
                    painter = painterResource(id = R.drawable.prediction_screen),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )

                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(24.dp),
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(
                        verticalArrangement = Arrangement.spacedBy(24.dp)
                    ) {
                        SimpleLanguageSelector(
                            label = stringResource(R.string.app_language),
                            selectedLang = selectedAppLang,
                            onLangSelected = { selectedAppLang = it },
                            languages = languages,
                            textColor = Color.White
                        )
                    }

                    Button(
                        onClick = {
                            prefs.edit()
                                .putString("app_language", selectedAppLang)
                                .apply()

                            Toast.makeText(
                                context,
                                context.getString(R.string.settings_saved),
                                Toast.LENGTH_LONG
                            ).show()
                        },
                        modifier = Modifier.fillMaxWidth(),
                        colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2196F3))
                    ) {
                        Text(text = stringResource(R.string.save))
                    }
                }
            }
        }
    }
}
