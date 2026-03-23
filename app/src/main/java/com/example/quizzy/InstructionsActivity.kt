package com.example.quizzy

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder

private const val TAG = "QUIZZY_DEBUG"

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

data class DisplayQuestion(
    val questionText: String,
    val options: List<String>,
    val correctAnswer: String
)

@Composable
fun InstructionsScreen(
    gradeLevel: Int,
    gradeName: String,
    onBackClick: () -> Unit
) {
    var instructionText by remember { mutableStateOf("") }
    var questions by remember { mutableStateOf<List<DisplayQuestion>>(emptyList()) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }
    var currentQuestionIndex by remember { mutableIntStateOf(0) }

    LaunchedEffect(gradeLevel) {
        isLoading = true
        errorMessage = ""
        currentQuestionIndex = 0

        try {
            instructionText = fetchInstructionFromBackend(gradeLevel)
            questions = fetchGeneratedQuizFromBackend(buildPromptForGrade(gradeLevel))
        } catch (e: Exception) {
            errorMessage = e.message ?: "Something went wrong."
            Log.e(TAG, "Screen load failed", e)
        } finally {
            isLoading = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFFBF2))
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        when {
            isLoading -> {
                LoadingContent(onBackClick = onBackClick)
            }

            errorMessage.isNotEmpty() -> {
                ErrorContent(
                    errorMessage = errorMessage,
                    onBackClick = onBackClick
                )
            }

            else -> {
                MainContent(
                    gradeName = gradeName,
                    instructionText = instructionText,
                    questions = questions,
                    currentQuestionIndex = currentQuestionIndex,
                    onBackClick = onBackClick,
                    onPreviousClick = {
                        if (currentQuestionIndex > 0) {
                            currentQuestionIndex--
                        }
                    },
                    onNextClick = {
                        if (currentQuestionIndex < questions.lastIndex) {
                            currentQuestionIndex++
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun LoadingContent(onBackClick: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        BackText(onBackClick = onBackClick)

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                CircularProgressIndicator(color = Color(0xFFA874FF))
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Loading...",
                    fontSize = 18.sp,
                    color = Color(0xFF7B6A58)
                )
            }
        }
    }
}

@Composable
fun ErrorContent(
    errorMessage: String,
    onBackClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp),
        horizontalAlignment = Alignment.Start
    ) {
        BackText(onBackClick = onBackClick)

        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = errorMessage,
                color = Color.Red,
                fontSize = 16.sp,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun MainContent(
    gradeName: String,
    instructionText: String,
    questions: List<DisplayQuestion>,
    currentQuestionIndex: Int,
    onBackClick: () -> Unit,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit
) {
    val scrollState = rememberScrollState()
    val scope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .verticalScroll(scrollState)
    ) {
        BackText(onBackClick = onBackClick)

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = gradeName,
            fontSize = 34.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF4C4035)
        )

        Spacer(modifier = Modifier.height(18.dp))

        InstructionCard(instructionText = instructionText)

        Spacer(modifier = Modifier.height(20.dp))

        if (questions.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(18.dp),
                colors = CardDefaults.cardColors(containerColor = Color.White),
                elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
            ) {
                Text(
                    text = "No questions available.",
                    fontSize = 18.sp,
                    color = Color(0xFF7B6A58),
                    modifier = Modifier.padding(20.dp)
                )
            }
        } else {
            val currentQuestion = questions[currentQuestionIndex]

            Text(
                text = "Question ${currentQuestionIndex + 1} of ${questions.size}",
                fontSize = 16.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFFA874FF)
            )

            Spacer(modifier = Modifier.height(10.dp))

            QuestionCard(question = currentQuestion)

            Spacer(modifier = Modifier.height(18.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Button(
                    onClick = {
                        onPreviousClick()
                        scope.launch { scrollState.animateScrollTo(0) }
                    },
                    enabled = currentQuestionIndex > 0,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFA874FF),
                        disabledContainerColor = Color(0xFFE7D7FF)
                    )
                ) {
                    Text(
                        text = "Previous",
                        fontSize = 16.sp
                    )
                }

                Button(
                    onClick = {
                        onNextClick()
                        scope.launch { scrollState.animateScrollTo(0) }
                    },
                    enabled = currentQuestionIndex < questions.lastIndex,
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = Color(0xFFA874FF),
                        disabledContainerColor = Color(0xFFE7D7FF)
                    )
                ) {
                    Text(
                        text = if (currentQuestionIndex == questions.lastIndex) "Done" else "Next",
                        fontSize = 16.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
fun BackText(onBackClick: () -> Unit) {
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

@Composable
fun InstructionCard(instructionText: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = "Instructions",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF5A4A3B)
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = instructionText.ifBlank { "No instructions available." },
                fontSize = 18.sp,
                color = Color(0xFF4C4035),
                lineHeight = 27.sp
            )
        }
    }
}

@Composable
fun QuestionCard(question: DisplayQuestion) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Text(
                text = question.questionText,
                fontSize = 22.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color(0xFF4C4035),
                lineHeight = 30.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            question.options.forEachIndexed { index, option ->
                OptionRow(
                    label = ('A' + index).toString(),
                    text = option
                )
                if (index != question.options.lastIndex) {
                    Spacer(modifier = Modifier.height(10.dp))
                }
            }
        }
    }
}

@Composable
fun OptionRow(
    label: String,
    text: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                color = Color(0xFFF8F4FF),
                shape = RoundedCornerShape(14.dp)
            )
            .padding(horizontal = 14.dp, vertical = 14.dp),
        verticalAlignment = Alignment.Top
    ) {
        Text(
            text = "$label.",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFA874FF)
        )

        Text(
            text = " $text",
            fontSize = 18.sp,
            color = Color(0xFF4C4035),
            lineHeight = 24.sp
        )
    }
}

fun buildPromptForGrade(gradeLevel: Int): String {
    return when (gradeLevel) {
        3 -> "Generate exactly 5 multiple-choice math questions for Grade 3 students. Focus on addition, subtraction, simple multiplication, and basic word problems."
        4 -> "Generate exactly 5 multiple-choice math questions for Grade 4 students. Focus on multiplication, division, place value, fractions, and word problems."
        5 -> "Generate exactly 5 multiple-choice math questions for Grade 5 students. Focus on fractions, decimals, multiplication, division, and multi-step word problems."
        else -> "Generate exactly 5 multiple-choice math questions for Grade 3 students."
    }
}

suspend fun fetchInstructionFromBackend(gradeLevel: Int): String {
    return withContext(Dispatchers.IO) {
        val url = URL("http://10.0.2.2:3000/api/instructions/$gradeLevel")
        val connection = url.openConnection() as HttpURLConnection
        connection.requestMethod = "GET"
        connection.connectTimeout = 10000
        connection.readTimeout = 10000

        try {
            val responseCode = connection.responseCode
            Log.d(TAG, "GET instructions -> $responseCode")

            val responseText = readResponseText(connection, responseCode)
            Log.d(TAG, "Instructions raw: $responseText")

            if (responseCode != 200) {
                throw Exception("Instructions request failed: $responseCode")
            }

            val json = JSONObject(responseText)
            json.optString("instructionText", "No instructions found.")
        } finally {
            connection.disconnect()
        }
    }
}

suspend fun fetchGeneratedQuizFromBackend(prompt: String): List<DisplayQuestion> {
    return withContext(Dispatchers.IO) {
        val encodedPrompt = URLEncoder.encode(prompt, "UTF-8")
        val url = URL("http://10.0.2.2:3000/api/quiz/generate?prompt=$encodedPrompt")
        val connection = url.openConnection() as HttpURLConnection

        connection.requestMethod = "POST"
        connection.connectTimeout = 15000
        connection.readTimeout = 15000

        try {
            val responseCode = connection.responseCode
            Log.d(TAG, "POST quiz/generate -> $responseCode")

            val responseText = readResponseText(connection, responseCode)
            Log.d(TAG, "Quiz raw: $responseText")

            if (responseCode != 200) {
                throw Exception("Quiz generation failed: $responseCode")
            }

            val json = JSONObject(responseText)
            val questionsArray = json.optJSONArray("questions") ?: JSONArray()
            parseQuestions(questionsArray)
        } finally {
            connection.disconnect()
        }
    }
}

fun readResponseText(connection: HttpURLConnection, responseCode: Int): String {
    val stream = if (responseCode in 200..299) {
        connection.inputStream
    } else {
        connection.errorStream
    } ?: throw Exception("No response stream from server.")

    val reader = BufferedReader(InputStreamReader(stream))
    val result = StringBuilder()
    var line: String?

    while (true) {
        line = reader.readLine() ?: break
        result.append(line)
    }

    reader.close()
    return result.toString()
}

fun parseQuestions(questionsArray: JSONArray): List<DisplayQuestion> {
    val questions = mutableListOf<DisplayQuestion>()

    for (i in 0 until questionsArray.length()) {
        val questionObject = questionsArray.getJSONObject(i)

        val questionText = questionObject.optString("questionText", "No question text")
        val option1 = questionObject.optString("option1", "")
        val option2 = questionObject.optString("option2", "")
        val option3 = questionObject.optString("option3", "")
        val option4 = questionObject.optString("option4", "")
        val correctAnswer = questionObject.optString("correctAnswer", "N/A")

        val options = listOf(option1, option2, option3, option4)
            .filter { it.isNotBlank() }

        questions.add(
            DisplayQuestion(
                questionText = questionText,
                options = options,
                correctAnswer = correctAnswer
            )
        )
    }

    return questions
}