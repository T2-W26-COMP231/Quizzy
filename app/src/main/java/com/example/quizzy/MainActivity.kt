package com.example.quizzy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
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
    var selectedLevel by remember { mutableStateOf("") }
    var instructions by remember { mutableStateOf("Choose a level to load quiz instructions.") }
    var isLoading by remember { mutableStateOf(false) }

    val scope = rememberCoroutineScope()

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFFF6F0FF),
                        Color(0xFFE8F0FF),
                        Color(0xFFFFFFFF)
                    )
                )
            )
            .padding(18.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            HeaderCard()

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Choose Difficulty",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF2D2A4A)
            )

            Spacer(modifier = Modifier.height(14.dp))

            LevelCard(
                title = "Easy",
                subtitle = "Grade 1",
                color = Color(0xFF6C63FF),
                onClick = {
                    selectedLevel = "Grade 1"
                    isLoading = true
                    scope.launch {
                        instructions = fetchInstructionsFromBackend(1)
                        isLoading = false
                    }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            LevelCard(
                title = "Medium",
                subtitle = "Grade 2",
                color = Color(0xFFFF8A65),
                onClick = {
                    selectedLevel = "Grade 2"
                    isLoading = true
                    scope.launch {
                        instructions = fetchInstructionsFromBackend(2)
                        isLoading = false
                    }
                }
            )

            Spacer(modifier = Modifier.height(12.dp))

            LevelCard(
                title = "Hard",
                subtitle = "Grade 3",
                color = Color(0xFF26A69A),
                onClick = {
                    selectedLevel = "Grade 3"
                    isLoading = true
                    scope.launch {
                        instructions = fetchInstructionsFromBackend(3)
                        isLoading = false
                    }
                }
            )

            Spacer(modifier = Modifier.height(22.dp))

            InstructionCard(
                selectedLevel = selectedLevel,
                instructions = instructions,
                isLoading = isLoading
            )
        }
    }
}

@Composable
fun HeaderCard() {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFF5B4BDB)
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(54.dp)
                    .background(Color.White.copy(alpha = 0.18f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "School",
                    tint = Color.White,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.size(14.dp))

            Column {
                Text(
                    text = "Quizzy",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Learn, choose, and get ready for your quiz.",
                    color = Color.White.copy(alpha = 0.9f),
                    fontSize = 14.sp
                )
            }
        }
    }
}

@Composable
fun LevelCard(
    title: String,
    subtitle: String,
    color: Color,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(22.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .background(color.copy(alpha = 0.15f), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = title.first().toString(),
                        color = color,
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp
                    )
                }

                Spacer(modifier = Modifier.size(12.dp))

                Column {
                    Text(
                        text = title,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color(0xFF2D2A4A)
                    )
                    Text(
                        text = subtitle,
                        fontSize = 14.sp,
                        color = Color.Gray
                    )
                }
            }

            Button(
                onClick = onClick,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = color
                )
            ) {
                Text("Select")
            }
        }
    }
}

@Composable
fun InstructionCard(
    selectedLevel: String,
    instructions: String,
    isLoading: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 6.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = "Selected",
                    tint = Color(0xFF5B4BDB),
                    modifier = Modifier.size(22.dp)
                )

                Spacer(modifier = Modifier.size(8.dp))

                Text(
                    text = "Current Selection",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF2D2A4A)
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = "Level",
                fontSize = 13.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = selectedLevel.ifBlank { "No level selected yet" },
                fontSize = 20.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF5B4BDB)
            )

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = "Instructions",
                fontSize = 13.sp,
                color = Color.Gray
            )

            Spacer(modifier = Modifier.height(8.dp))

            if (isLoading) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(22.dp),
                        strokeWidth = 3.dp,
                        color = Color(0xFF5B4BDB)
                    )
                    Spacer(modifier = Modifier.size(10.dp))
                    Text(
                        text = "Loading instructions...",
                        fontSize = 15.sp,
                        color = Color(0xFF2D2A4A)
                    )
                }
            } else {
                Text(
                    text = instructions,
                    fontSize = 16.sp,
                    lineHeight = 24.sp,
                    color = Color(0xFF2D2A4A)
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