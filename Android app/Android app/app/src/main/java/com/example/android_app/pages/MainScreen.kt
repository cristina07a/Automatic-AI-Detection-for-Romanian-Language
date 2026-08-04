    package com.example.android_app.pages

    import android.widget.Toast
    import androidx.compose.foundation.Image
    import androidx.compose.foundation.background
    import androidx.compose.foundation.layout.*
    import androidx.compose.foundation.shape.RoundedCornerShape
    import androidx.compose.material3.Button
    import androidx.compose.material3.ButtonDefaults
    import androidx.compose.material3.MaterialTheme
    import androidx.compose.material3.Text
    import androidx.compose.runtime.*
    import androidx.compose.runtime.livedata.observeAsState
    import androidx.compose.ui.Alignment
    import androidx.compose.ui.Modifier
    import androidx.compose.ui.graphics.Color
    import androidx.compose.ui.graphics.ColorFilter
    import androidx.compose.ui.platform.LocalContext
    import androidx.compose.ui.res.painterResource
    import androidx.compose.ui.res.stringResource
    import androidx.compose.ui.text.TextStyle
    import androidx.compose.ui.text.font.FontFamily
    import androidx.compose.ui.tooling.preview.Preview
    import androidx.compose.ui.unit.dp
    import androidx.navigation.NavController
    import androidx.navigation.compose.rememberNavController
    import com.example.android_app.AuthState
    import com.example.android_app.AuthViewModel
    import com.example.android_app.R
    import com.example.android_app.Screen


    @Composable
    fun MainScreen(navController: NavController, authViewModel: AuthViewModel) {

        val authState = authViewModel.authState.observeAsState()
        val context = LocalContext.current

        LaunchedEffect(authState.value) {
            when (authState.value) {
                is AuthState.Authenticated -> {
                    navController.navigate(Screen.PredictionScreen.route) {
                        popUpTo(Screen.MainScreen.route) { //se scot din stiva toate ecranele afisate poana acum,
                            inclusive = true //inclusiv main screen
                        }
                    }
                }

                is AuthState.Error ->
                    Toast.makeText(
                        context,
                        (authState.value as AuthState.Error).message,
                        Toast.LENGTH_SHORT
                    ).show()

                else -> Unit
            }
        }

        Box(
            modifier = Modifier
                .fillMaxSize()
        ) {

            Image(
                painter = painterResource(id = R.drawable.home_screen),
                contentDescription = null,
                modifier = Modifier.fillMaxSize(),
                alignment = Alignment.Center,
                contentScale = androidx.compose.ui.layout.ContentScale.Crop
            )

            Box(
                modifier = Modifier
                    .padding(top = 200.dp)
                    .fillMaxWidth()
                    .background(
                        color = Color.White.copy(alpha = 0.6f),
                        shape = RoundedCornerShape(16.dp)
                    )
                    .padding(30.dp)
            ) {
                Text(
                    text = "Hello World!",
                    style = TextStyle(
                        fontFamily = FontFamily.Serif,
                        color = Color.Black,
                    ),
                    modifier = Modifier.align(Alignment.Center)
                )
            }



            Column(
                verticalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier
                    .fillMaxWidth()
                    .fillMaxHeight()
                    .padding(horizontal = 40.dp)
            ) {


                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .fillMaxWidth()
                        .padding(top = 350.dp)
                        .padding(bottom = 50.dp)
                ) {
                    Button(
                        onClick = {
                            navController.navigate(Screen.LoginScreen.route)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2196F3)
                        ),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = stringResource(R.string.login), color = Color.White)
                    }

                    Button(
                        onClick = {
                            navController.navigate(Screen.RegisterScreen.route)
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = Color(0xFF2196F3)
                        ),
                        modifier = Modifier.fillMaxWidth()

                    ) {
                        Text(text = stringResource(R.string.register), color = Color.White)
                    }
                }

                Spacer(modifier = Modifier.height(30.dp))
            }
        }
    }
