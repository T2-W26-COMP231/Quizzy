package com.example.quizzy

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.quizzy.ui.theme.QuizzyTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            QuizzyTheme {
                QuizzyApp()
            }
        }
    }
}

fun getInstructions(level: String): String {
    return when (level) {
        "Easy" -> "Solve simple addition questions."
        "Medium" -> "Solve mixed arithmetic questions."
        "Hard" -> "Solve advanced math problems."
        else -> "No instructions available."
    }
}

@Composable
fun QuizzyApp() {
    var currentScreen by remember { mutableStateOf("level_selection") }
    var selectedLevel by remember { mutableStateOf("") }
    var selectedInstructions by remember { mutableStateOf("") }

    when (currentScreen) {
        "level_selection" -> {
            LevelSelectionScreen(
                onLevelSelected = { level ->
                    selectedLevel = level
                    selectedInstructions = getInstructions(level)
                    currentScreen = "quiz_screen"
                }
            )
        }

        "quiz_screen" -> {
            QuizScreen(
                level = selectedLevel,
                instructions = selectedInstructions
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LevelSelectionScreen(onLevelSelected: (String) -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quiz Selection") }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Select Quiz Level",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(24.dp))

            Button(
                onClick = { onLevelSelected("Easy") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Easy")
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { onLevelSelected("Medium") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Medium")
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = { onLevelSelected("Hard") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Hard")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizScreen(level: String, instructions: String) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quiz Screen") }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Selected Level: $level",
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Instructions:",
                style = MaterialTheme.typography.titleMedium
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = instructions,
                style = MaterialTheme.typography.bodyLarge
            )
        }
    }
}