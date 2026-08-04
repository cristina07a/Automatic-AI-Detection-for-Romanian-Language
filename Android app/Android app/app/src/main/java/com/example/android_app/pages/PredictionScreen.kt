package com.example.android_app.pages


import android.content.ActivityNotFoundException
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import androidx.navigation.NavController
import com.example.android_app.AuthState
import com.example.android_app.AuthViewModel
import com.example.android_app.BatchPrediction
import com.example.android_app.FullPredictionRecord
import com.example.android_app.PdfHelper
import com.example.android_app.PdfHelper.extractTextFromPdf
//import com.example.android_app.PdfHelper.extractTextFromPdf
import com.example.android_app.R
import com.example.android_app.Screen
import com.example.android_app.runModelInferenceBatched
import com.example.android_app.saveFullPredictionResult
import com.example.android_app.tokenizer.FullTokenizer
import com.example.android_app.tokenizer.UnknownToken
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FirebaseFirestore
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import kotlinx.coroutines.launch
import org.tensorflow.lite.Interpreter
import java.io.File
import java.io.FileInputStream
import java.io.IOException
import java.nio.ByteBuffer
import java.nio.channels.FileChannel
import androidx.compose.material3.IconButton as IconButton1

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PredictionScreen(navController: NavController, authViewModel: AuthViewModel)
{
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    val context = LocalContext.current

    var userInput by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var selectedLanguage by remember { mutableStateOf("en") }



    val authState = authViewModel.authState.observeAsState()

    val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

    val pdfPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument()
    ) { uri: Uri? ->
        uri?.let {
            userInput = extractTextFromPdf(context, it)
        }
    }

    //image uploading
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        uri?.let {
            try {
                val image = InputImage.fromFilePath(context, it)
                recognizer.process(image)
                    .addOnSuccessListener { visionText ->
                        userInput = visionText.text
                    }
                    .addOnFailureListener { e ->
                        Log.e("OCR", "Eroare la extragerea textului: ${e.message}")
                    }
            } catch (e: IOException) {
                Log.e("OCR", "Eroare la citirea imaginii: ${e.message}")
            }
        }
    }


    LaunchedEffect(authState.value) {
        when (authState.value){
            is AuthState.Unauthenticated -> navController.navigate(Screen.MainScreen.route)
            else -> Unit
        }
    }

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
        },
    ) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = {
                        Text(
                            stringResource(R.string.predictions),
                            color = Color.White
                        )
                    },
                    navigationIcon = {
                        IconButton1(onClick = {
                            scope.launch { drawerState.open() }
                        }) {
                            Icon(
                                Icons.Default.Menu, contentDescription = stringResource(R.string.open_menu),
                                tint = Color.White
                            )
                        }
                    },
                    colors = TopAppBarDefaults.topAppBarColors(
                        containerColor = Color(0xFF2196F3)
                    ),
                )
            }
        ) { innerPadding ->
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding) ,

                ) {
                Image(
                    painter = painterResource(id = R.drawable.prediction_screen),
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(
                    modifier = Modifier
                        .padding(24.dp)
                        .align(Alignment.Center)
                        .background(
                            color = Color.White.copy(alpha = 0.15f),
                            shape = RoundedCornerShape(20.dp)
                        )
                        .border(2.dp, Color.White, RoundedCornerShape(10.dp))
                        .padding(10.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .wrapContentWidth()
                            .fillMaxHeight()
                            .padding(bottom = 20.dp),
                        verticalArrangement = Arrangement.Center
                    ) {
                        Spacer(modifier = Modifier.height(16.dp))

                        if (message.isNotBlank()) {
                            Text(
                                text = message,
                                color = Color.White,
                                modifier = Modifier.align(Alignment.CenterHorizontally),
                            )
                            Spacer(modifier = Modifier.height(5.dp))
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(16.dp, Alignment.CenterHorizontally)
                        ) {

                            // Switch limbă RO/EN
                            IconButton1(
                                onClick = {
                                    selectedLanguage = if (selectedLanguage == "ro") "en" else "ro"
                                },
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(Color.White.copy(alpha = 0.2f), shape = CircleShape)
                            ) {
                                Text(
                                    text = selectedLanguage.take(2).uppercase(),
                                    color = Color.White,
                                    fontWeight = FontWeight.Bold
                                )
                            }

                            // Icon pentru încărcare galerie
                            IconButton1(
                                onClick = { launcher.launch("image/*") },
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(Color.White.copy(alpha = 0.2f), shape = CircleShape)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.image),
                                    contentDescription = stringResource(R.string.load_image),
                                    tint = Color.White
                                )
                            }
                            // Icon pentru încărcare PDF
                            IconButton1(
                                onClick = { pdfPickerLauncher.launch(arrayOf("application/pdf")) },
                                modifier = Modifier
                                    .size(48.dp)
                                    .background(Color.White.copy(alpha = 0.2f), shape = CircleShape)
                            ) {
                                Icon(
                                    painter = painterResource(id = R.drawable.file_pdf_box),
                                    contentDescription = stringResource(R.string.load_pdf),
                                    tint = Color.White
                                )
                            }
                            // Buton Verifică
                            Button(
                                onClick = {
                                    scope.launch {
                                        message = context.getString(R.string.analyzing_text)
                                        kotlinx.coroutines.delay(100)

                                        val inferenceResult = runModelInferenceBatched(
                                            context,
                                            userInput,
                                            selectedLanguage
                                        )
                                        // restul logicii pentru rezultate...
                                        val predictions = inferenceResult.predictions
                                        val texts = inferenceResult.textsPerBatch
                                        val finalPrediction = inferenceResult.finalPrediction
                                        val aiPercentages = inferenceResult.aiPercentages // ACEASTĂ LINIE LIPSEA!


                                        val batchPredictionsMap =
                                            predictions.mapIndexed { index, prediction ->
                                                index.toString() to BatchPrediction(
                                                    prediction = prediction,
                                                    tokensText = texts[index].joinToString(" "),
                                                    aiPercentage = aiPercentages.getOrElse(index) { 0.0f }
                                                )
                                            }.toMap()

                                        authViewModel.currentUser?.uid?.let { uid ->
                                            val fullRecord = FullPredictionRecord(
                                                userInput = userInput,
                                                batchPredictions = batchPredictionsMap,
                                                finalPrediction = finalPrediction,
                                                timestamp = Timestamp.now()
                                            )
                                            saveFullPredictionResult(uid, fullRecord)
                                        }

                                        val pdfFile = PdfHelper.generatePredictionPdf(
                                            context,
                                            batchPredictionsMap
                                        )
                                        if (pdfFile != null) {
                                            val uri = FileProvider.getUriForFile(
                                                context,
                                                "${context.packageName}.fileprovider",
                                                pdfFile
                                            )
                                            val intent = Intent(Intent.ACTION_VIEW).apply {
                                                setDataAndType(uri, "application/pdf")
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

                                        }
                                        message = " "
                                    }
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = Color(0xFF2196F3)
                                ),
                                modifier = Modifier
                                    .height(48.dp)
                                    .weight(10f) // pentru a ocupa restul spațiului
                            ) {
                                Text(stringResource(R.string.verify))
                            }
                        }


                        Spacer(modifier = Modifier.height(16.dp))

                        OutlinedTextField(
                            value = userInput,
                            onValueChange = { userInput = it },
                            label = { Text(stringResource(R.string.enter_text)) },
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(horizontal = 10.dp)
                                .fillMaxHeight(),
                            textStyle = TextStyle(color = Color.White),
                            colors = TextFieldDefaults.outlinedTextFieldColors(
                                focusedBorderColor = Color.White,
                                unfocusedBorderColor = Color.White,
                                cursorColor = Color.White,
                                focusedLabelColor = Color.White,
                                unfocusedLabelColor = Color.White
                            )
                        )
                    }
                }
            }
        }
    }
}


