package com.example.quizzy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

class InstructionsActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val gradeLevel = intent.getIntExtra("GRADE_LEVEL", 3)
        val gradeName = intent.getStringExtra("GRADE_NAME") ?: "Grade 3"

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    InstructionsScreen(
                        gradeLevel = gradeLevel,
                        gradeName = gradeName,
                        onBackClick = { finish() }
                    )
                }
            }
        }
    }
}

@Composable
fun InstructionsScreen(
    gradeLevel: Int,
    gradeName: String,
    onBackClick: () -> Unit
) {
    var instructionText by remember { mutableStateOf("") }
    var promptTemplate by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }

    LaunchedEffect(gradeLevel) {
        isLoading = true
        errorMessage = ""

        try {
            val result = fetchInstructionFromBackend(gradeLevel)
            instructionText = result.first
            promptTemplate = result.second
        } catch (e: Exception) {
            errorMessage = e.message ?: "Failed to load instructions."
        } finally {
            isLoading = false
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFFBF2))
            .statusBarsPadding()
            .padding(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "← Back",
                fontSize = 18.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF5A4A3B),
                modifier = Modifier
                    .clickable { onBackClick() }
                    .padding(vertical = 8.dp, horizontal = 4.dp)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = gradeName,
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF5A4A3B)
            )

            Spacer(modifier = Modifier.height(24.dp))

            when {
                isLoading -> {
                    CircularProgressIndicator(color = Color(0xFFA874FF))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "Loading instructions...",
                        fontSize = 18.sp,
                        color = Color(0xFF7B6A58)
                    )
                }

                errorMessage.isNotEmpty() -> {
                    Text(
                        text = errorMessage,
                        color = Color.Red,
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.fillMaxWidth()
                    )
                }

                else -> {
                    Text(
                        text = "Instructions",
                        fontSize = 22.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF5A4A3B)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = instructionText,
                        fontSize = 20.sp,
                        textAlign = TextAlign.Center,
                        color = Color(0xFF4C4035),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(20.dp))

                    Text(
                        text = "Prompt Template",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(0xFF5A4A3B)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Text(
                        text = promptTemplate,
                        fontSize = 18.sp,
                        textAlign = TextAlign.Center,
                        color = Color(0xFF4C4035),
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

suspend fun fetchInstructionFromBackend(gradeLevel: Int): Pair<String, String> {
    return withContext(Dispatchers.IO) {
        val url = URL("http://10.0.2.2:3000/api/instructions/$gradeLevel")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = 5000
        connection.readTimeout = 5000

        try {
            val responseCode = connection.responseCode

            if (responseCode == 200) {
                val response = connection.inputStream.bufferedReader().use { it.readText() }
                val json = JSONObject(response)

                val instructionText = json.getString("instructionText")
                val promptTemplate = json.getString("promptTemplate")

                Pair(instructionText, promptTemplate)
            } else {
                throw Exception("Server returned error code: $responseCode")
            }
        } finally {
            connection.disconnect()
        }
    }
}