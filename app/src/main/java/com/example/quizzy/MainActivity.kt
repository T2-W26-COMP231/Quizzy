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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.graphics.Color
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.quizzy.ui.theme.QuizzyTheme
import androidx.compose.runtime.*

data class Badge(
    val name: String,
    val unlocked: Boolean
)
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

@Composable
fun QuizzyApp() {
    val navController = rememberNavController()

    NavHost(
        navController = navController,
        startDestination = "dashboard"
    ) {
        composable("dashboard") {
            MainDashboardScreen(navController)
        }

        composable("quiz_selection") {
            QuizSelectionScreen(navController)
        }

        composable("guardian_dashboard") {
            GuardianDashboardScreen(navController)
        }
        composable("quiz_screen") {
            QuizScreen()
        }
        composable("badges") {
            BadgeScreen(navController)
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainDashboardScreen(navController: NavHostController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = "Quizzy Dashboard",
                        style = MaterialTheme.typography.headlineSmall
                    )
                },
                actions = {
                    IconButton(onClick = {
                        navController.navigate("guardian_dashboard")
                    }) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Guardian Dashboard"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = "Welcome to Quizzy",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Choose an option to continue",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    navController.navigate("quiz_selection")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Start Quiz")
            }

            Spacer(modifier = Modifier.height(16.dp))

            OutlinedButton(
                onClick = {
                    navController.navigate("badges")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("View Badges")
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun QuizSelectionScreen(navController: NavHostController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Quiz Selection") },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.navigate("dashboard")
                    }) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Back to Dashboard"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {

            Text(
                text = "Select Quiz Level",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Pick a level to begin",
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))

            Button(
                onClick = {
                    navController.navigate("quiz_screen")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Easy")
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    navController.navigate("quiz_screen")
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text("Medium")
            }

            Spacer(modifier = Modifier.height(12.dp))

            Button(
                onClick = {
                    navController.navigate("quiz_screen")
                },
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
fun GuardianDashboardScreen(navController: NavHostController) {
    val totalScore = 10

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Guardian Dashboard") },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.navigate("dashboard")
                    }) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Back to Dashboard"
                        )
                    }
                }
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
                text = "Student Progress Overview",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(24.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(20.dp),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.primaryContainer
                )
            ) {
                Column(
                    modifier = Modifier.padding(24.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Total Score",
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = "$totalScore",
                        style = MaterialTheme.typography.displaySmall
                    )
                }
            }
        }
    }
}
@Composable
fun QuizScreen() {
    val question = "What is 2 + 2?"
    val options = listOf("2", "3", "4", "5")
    val correctAnswer = "4"

    var selectedAnswer by remember { mutableStateOf<String?>(null) }
    var submitted by remember { mutableStateOf(false) }
    var score by remember {     mutableIntStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = "Quiz Question",
            style = MaterialTheme.typography.headlineMedium
        )

        Spacer(modifier = Modifier.height(20.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant
            )
        ) {
            Text(
                text = question,
                modifier = Modifier.padding(20.dp),
                style = MaterialTheme.typography.titleLarge
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        options.forEach { answer ->
            val containerColor = when {
                !submitted && selectedAnswer == answer -> MaterialTheme.colorScheme.secondaryContainer
                !submitted -> MaterialTheme.colorScheme.primary
                answer == correctAnswer -> Color(0xFF4CAF50)
                answer == selectedAnswer -> Color(0xFFE53935)
                else -> MaterialTheme.colorScheme.primary
            }

            Button(
                onClick = {
                    if (!submitted) selectedAnswer = answer
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.buttonColors(containerColor = containerColor)
            ) {
                Text(answer)
            }

            Spacer(modifier = Modifier.height(10.dp))
        }

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                submitted = true
                if (selectedAnswer == correctAnswer) {
                    score++
                }
            },
            enabled = selectedAnswer != null,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(16.dp)
        ) {
            Text("Submit Answer")
        }

        if (submitted) {
            Spacer(modifier = Modifier.height(20.dp))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (selectedAnswer == correctAnswer)
                        Color(0xFFE8F5E9)
                    else
                        Color(0xFFFFEBEE)
                )
            ) {
                Text(
                    text = if (selectedAnswer == correctAnswer) "Correct!" else "Incorrect",
                    modifier = Modifier.padding(16.dp),
                    style = MaterialTheme.typography.titleMedium,
                    color = if (selectedAnswer == correctAnswer)
                        Color(0xFF2E7D32)
                    else
                        Color(0xFFC62828)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = "Score: $score",
                style = MaterialTheme.typography.bodyLarge
            )

        }
    }
}

fun getBadgesForScore(score: Int): List<Badge> {
    return listOf(
        Badge("Starter", score >= 1),
        Badge("Quick Thinker", score >= 2),
        Badge("Math Star", score >= 3)
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BadgeScreen(navController: NavHostController) {
    val totalScore = 2
    val badges = getBadgesForScore(totalScore)

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("My Badges") },
                navigationIcon = {
                    IconButton(onClick = {
                        navController.navigate("dashboard")
                    }) {
                        Icon(
                            imageVector = Icons.Default.Home,
                            contentDescription = "Back to Dashboard"
                        )
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(24.dp)
        ) {
            Text(
                text = "Badges Earned",
                style = MaterialTheme.typography.headlineMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            badges.forEach { badge ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (badge.unlocked)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = badge.name,
                                style = MaterialTheme.typography.titleMedium
                            )
                            Text(
                                text = if (badge.unlocked) "Unlocked" else "Locked",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }

                        Text(
                            text = if (badge.unlocked) "🏆" else "🔒",
                            style = MaterialTheme.typography.headlineSmall
                        )
                    }
                }
            }
        }
    }
}