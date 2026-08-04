package com.example.android_app.pages

import android.content.ActivityNotFoundException
import android.content.Intent
import android.util.Log
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Divider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.android_app.AuthViewModel
import com.google.firebase.firestore.FirebaseFirestore
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.Scaffold
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.core.content.FileProvider
import com.example.android_app.FullPredictionRecord
import com.example.android_app.PdfHelper
//import com.example.android_app.PredictionRecord
import com.example.android_app.R
import com.example.android_app.Screen
import com.example.android_app.loadPredictionHistory
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HistoryScreen(navController: NavController, authViewModel: AuthViewModel) {
    val userId = authViewModel.currentUser?.uid
    var predictions by remember { mutableStateOf<List<FullPredictionRecord>>(emptyList()) }
    var loading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    var visibleCount by remember { mutableStateOf(5) }

    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    LaunchedEffect(userId) {
        if (userId != null) {
            loadPredictionHistory(userId) { loadedPredictions, error ->
                predictions = loadedPredictions
                errorMessage = error
                loading = false
            }
        } else {
            errorMessage = "Utilizatorul nu este autentificat"
            loading = false
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        Image(
            painter = painterResource(id = R.drawable.prediction_screen),
            contentDescription = null,
            modifier = Modifier.fillMaxSize(),
            contentScale = ContentScale.Crop,
            alpha = 1f
        )

        ModalNavigationDrawer(
            drawerState = drawerState,
            drawerContent = {
                ModalDrawerSheet(
                    modifier = Modifier.width(200.dp),
                    drawerContainerColor = Color(0xFF2196F3)
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
                containerColor = Color.Transparent,
                topBar = {
                    TopAppBar(
                        title = { Text(stringResource(R.string.history), color = Color.White) },
                        colors = TopAppBarDefaults.topAppBarColors(
                            containerColor = Color(0xFF2196F3)
                        ),
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
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(16.dp)
                ) {
                    when {
                        loading -> {
                            Text(stringResource(R.string.history_loading), color = Color.White)
                        }

                        errorMessage != null -> {
                            Text("Eroare: $errorMessage", color = Color.White)
                        }

                        else -> {
                            // --- Selectam doar primele visibleCount predictii ---
                            val visiblePredictions = predictions.take(visibleCount)

                            LazyColumn(
                                modifier = Modifier.padding(horizontal = 16.dp)
                            ) {
                                items(visiblePredictions) { prediction ->
                                    Box(
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(vertical = 6.dp)
                                            .clip(RoundedCornerShape(20.dp))
                                            .background(Color(0xFF2196F3))
                                    ) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 6.dp)
                                                .clip(RoundedCornerShape(20.dp))
                                                .background(Color(0xFF2196F3))
                                        ) {
                                            Box(modifier = Modifier.padding(16.dp)) {

                                                Row(
                                                    modifier = Modifier.fillMaxWidth(),
                                                    verticalAlignment = Alignment.Top,
                                                    horizontalArrangement = Arrangement.SpaceBetween
                                                ) {
                                                    Column(
                                                        modifier = Modifier.weight(1f)
                                                    ) {
                                                        Text(
                                                            text = stringResource(R.string.checked_text),
                                                            color = Color.White,
                                                            fontWeight = FontWeight.Bold
                                                        )
                                                        Text(
                                                            text = prediction.userInput.take(50),
                                                            color = Color.White
                                                        )
                                                    }

                                                    IconButton(
                                                        onClick = {
                                                            val pdfFile =
                                                                PdfHelper.generatePredictionPdf(
                                                                    context,
                                                                    prediction.batchPredictions
                                                                )

                                                            if (pdfFile != null) {
                                                                val uri = FileProvider.getUriForFile(
                                                                    context,
                                                                    "${context.packageName}.fileprovider",
                                                                    pdfFile
                                                                )

                                                                val intent =
                                                                    Intent(Intent.ACTION_VIEW).apply {
                                                                        setDataAndType(
                                                                            uri,
                                                                            "application/pdf"
                                                                        )
                                                                        flags =
                                                                            Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_GRANT_READ_URI_PERMISSION
                                                                    }

                                                                try {
                                                                    context.startActivity(intent)
                                                                } catch (e: ActivityNotFoundException) {
                                                                    Toast.makeText(
                                                                        context,
                                                                        context.getString(R.string.no_app_for_pdf),
                                                                        Toast.LENGTH_LONG
                                                                    ).show()
                                                                }
                                                            } else {
                                                                Toast.makeText(
                                                                    context,
                                                                    context.getString(R.string.pdf_generation_error),
                                                                    Toast.LENGTH_SHORT
                                                                ).show()
                                                            }
                                                        }
                                                    ) {
                                                        Icon(
                                                            painter = painterResource(id = R.drawable.file_pdf_box),
                                                            contentDescription = stringResource(R.string.open_pdf),
                                                            tint = Color.White,
                                                            modifier = Modifier.size(24.dp)
                                                        )
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }

                                if (visibleCount < predictions.size) {
                                    item {
                                        Button(
                                            onClick = { visibleCount += 5 },
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .padding(vertical = 16.dp),
                                            colors = ButtonDefaults.buttonColors(
                                                containerColor = Color(
                                                    0xFF2196F3
                                                )
                                            )
                                        ) {
                                            Text(
                                                stringResource(R.string.load_more_predictions),
                                                color = Color.White
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}