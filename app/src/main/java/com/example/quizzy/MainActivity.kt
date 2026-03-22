package com.example.quizzy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    QuizzyApp()
                }
            }
        }
    }
}

@Composable
fun QuizzyApp() {
    var currentScreen by remember { mutableStateOf("selection") }
    var selectedGrade by remember { mutableStateOf("") }
    var instructions by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    when (currentScreen) {
        "selection" -> {
            GradeSelectionScreen(
                isLoading = isLoading,
                onGradeSelected = { gradeNumber, gradeLabel ->
                    selectedGrade = gradeLabel
                    isLoading = true

                    scope.launch {
                        instructions = fetchInstructionsFromBackend(gradeNumber)
                        isLoading = false
                        currentScreen = "instructions"
                    }
                }
            )
        }

        "instructions" -> {
            InstructionsScreen(
                selectedGrade = selectedGrade,
                instructions = instructions,
                onBackClick = {
                    currentScreen = "selection"
                }
            )
        }
    }
}

@Composable
fun GradeSelectionScreen(
    isLoading: Boolean,
    onGradeSelected: (Int, String) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF6F0FF),
                        Color(0xFFE8F0FF),
                        Color.White
                    )
                )
            )
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "Quizzy",
                fontSize = 34.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4B2E83)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Select Grade",
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF2D2A4A)
            )

            Spacer(modifier = Modifier.height(28.dp))

            BigGradeButton(
                text = "Grade 1",
                color = Color(0xFF6C63FF),
                enabled = !isLoading,
                onClick = { onGradeSelected(1, "Grade 1") }
            )

            Spacer(modifier = Modifier.height(16.dp))

            BigGradeButton(
                text = "Grade 2",
                color = Color(0xFFFF8A65),
                enabled = !isLoading,
                onClick = { onGradeSelected(2, "Grade 2") }
            )

            Spacer(modifier = Modifier.height(16.dp))

            BigGradeButton(
                text = "Grade 3",
                color = Color(0xFF26A69A),
                enabled = !isLoading,
                onClick = { onGradeSelected(3, "Grade 3") }
            )

            Spacer(modifier = Modifier.height(28.dp))

            if (isLoading) {
                CircularProgressIndicator(
                    color = Color(0xFF4B2E83),
                    strokeWidth = 4.dp
                )

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = "Loading instructions...",
                    fontSize = 16.sp,
                    color = Color(0xFF2D2A4A)
                )
            }
        }
    }
}

@Composable
fun BigGradeButton(
    text: String,
    color: Color,
    enabled: Boolean,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(72.dp),
        shape = RoundedCornerShape(18.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color)
    ) {
        Text(
            text = text,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

@Composable
fun InstructionsScreen(
    selectedGrade: String,
    instructions: String,
    onBackClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF6F0FF),
                        Color(0xFFE8F0FF),
                        Color.White
                    )
                )
            )
            .padding(20.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Top
        ) {
            Text(
                text = "Instructions",
                fontSize = 30.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF4B2E83)
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = selectedGrade,
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF2D2A4A)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Text(
                        text = instructions,
                        fontSize = 18.sp,
                        lineHeight = 28.sp,
                        color = Color(0xFF2D2A4A)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = onBackClick,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF4B2E83)
                )
            ) {
                Text(
                    text = "Back",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

suspend fun fetchInstructionsFromBackend(level: Int): String {
    return withContext(Dispatchers.IO) {
        try {
            val url = URL("http://10.0.2.2:3000/api/instructions/$level")
            val connection = url.openConnection() as HttpURLConnection
            connection.requestMethod = "GET"
            connection.connectTimeout = 10000
            connection.readTimeout = 10000

            val responseCode = connection.responseCode
            val stream = if (responseCode in 200..299) {
                connection.inputStream
            } else {
                connection.errorStream
            }

            val response = stream.bufferedReader().use { it.readText() }
            connection.disconnect()

            val json = JSONObject(response)
            json.optString(
                "instructionText",
                json.optString(
                    "instruction_text",
                    "No instructions found."
                )
            )
        } catch (e: Exception) {
            e.printStackTrace()
            "Error loading instructions."
        }
    }
}