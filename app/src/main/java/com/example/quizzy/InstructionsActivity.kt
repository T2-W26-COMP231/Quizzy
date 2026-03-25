package com.example.quizzy

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.Dispatchers
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
        val gradeName = intent.getStringExtra("GRADE_NAME") ?: "Grade $gradeLevel"
        
        val themeColor = when(gradeLevel) {
            3 -> Color(0xFFA874FF)
            4 -> Color(0xFFFFB26B)
            5 -> Color(0xFF6FE3C1)
            else -> Color(0xFFA874FF)
        }

        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color(0xFFFFFBF2)
                ) {
                    QuizFlow(
                        gradeLevel = gradeLevel,
                        gradeName = gradeName,
                        themeColor = themeColor,
                        onBackClick = { finish() }
                    )
                }
            }
        }
    }
}

@Composable
fun BigGradeButton(
    text: String,
    color: Color,
    enabled: Boolean = true,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier
            .fillMaxWidth()
            .height(88.dp),
        shape = RoundedCornerShape(24.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = color,
            disabledContainerColor = Color.LightGray
        ),
        elevation = ButtonDefaults.buttonElevation(defaultElevation = 6.dp)
    ) {
        Text(
            text = text,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White
        )
    }
}

@Composable
fun QuizOptionButton(
    text: String,
    isSelected: Boolean,
    enabled: Boolean,
    backgroundColor: Color,
    borderColor: Color,
    onClick: () -> Unit
) {
    // We use a Box with clickable instead of a Button to remove the default "square" ripple/border
    // and have full control over the rounded design.
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(min = 72.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(backgroundColor)
            .border(3.dp, borderColor, RoundedCornerShape(24.dp))
            .clickable(enabled = enabled) { onClick() }
            .padding(horizontal = 24.dp, vertical = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            fontSize = 20.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFF5A4A3B),
            textAlign = TextAlign.Center
        )
    }
}

data class DisplayQuestion(
    val questionText: String,
    val options: List<String>,
    val correctAnswer: String
)

@Composable
fun QuizFlow(
    gradeLevel: Int,
    gradeName: String,
    themeColor: Color,
    onBackClick: () -> Unit
) {
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }
    var questions by remember { mutableStateOf<List<DisplayQuestion>>(emptyList()) }

    LaunchedEffect(gradeLevel) {
        isLoading = true
        errorMessage = ""
        try {
            val prompt = buildPromptForGrade(gradeLevel)
            questions = fetchGeneratedQuizFromBackend(prompt)
            if (questions.isEmpty()) {
                errorMessage = "No questions generated. Please try again."
            }
        } catch (e: Exception) {
            errorMessage = "Failed to load questions: ${e.message}"
            Log.e(TAG, "Load failed", e)
        } finally {
            isLoading = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(Color(0xFFFFFBF2), Color(0xFFF8F5EC))
                )
            )
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        if (isLoading) {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Quizzy",
                    fontSize = 38.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = Color(0xFF5A4A3B)
                )
                Spacer(modifier = Modifier.height(32.dp))
                CircularProgressIndicator(color = themeColor)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Generating your $gradeName quiz...",
                    fontSize = 18.sp,
                    color = Color(0xFF7B6A58),
                    textAlign = TextAlign.Center
                )
            }
        } else if (errorMessage.isNotEmpty()) {
            Column(
                modifier = Modifier.fillMaxSize().padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(text = errorMessage, color = Color.Red, textAlign = TextAlign.Center, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(24.dp))
                BigGradeButton(text = "Go Back", color = themeColor, onClick = onBackClick)
            }
        } else {
            QuizScreen(
                gradeName = gradeName,
                questions = questions,
                themeColor = themeColor,
                onFinish = { onBackClick() }
            )
        }
    }
}

@Composable
fun QuizScreen(
    gradeName: String,
    questions: List<DisplayQuestion>,
    themeColor: Color,
    onFinish: () -> Unit
) {
    var currentIndex by remember { mutableStateOf(0) }
    var score by remember { mutableStateOf(0) }
    var selectedOption by remember { mutableStateOf<String?>(null) }
    var showFeedback by remember { mutableStateOf(false) }
    var isCorrect by remember { mutableStateOf(false) }

    val currentQuestion = questions[currentIndex]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Quizzy",
            fontSize = 38.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF5A4A3B)
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = gradeName,
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = Color(0xFF7B6A58)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Question ${currentIndex + 1} of ${questions.size}",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = themeColor
            )
            Text(
                text = "Score: $score",
                fontSize = 16.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF5A4A3B)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        LinearProgressIndicator(
            progress = { (currentIndex + 1).toFloat() / questions.size },
            modifier = Modifier.fillMaxWidth().height(12.dp),
            color = themeColor,
            trackColor = themeColor.copy(alpha = 0.2f),
            strokeCap = androidx.compose.ui.graphics.StrokeCap.Round
        )

        Spacer(modifier = Modifier.height(32.dp))

        Column(
            modifier = Modifier.weight(1f).verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = currentQuestion.questionText,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF5A4A3B),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            currentQuestion.options.forEach { option ->
                val isSelected = selectedOption == option
                
                // Better Design Logic:
                // Normal: White background, Light Gray border
                // Selected: Subtle theme-colored background, Theme-colored border
                // Feedback Correct: Light Green background, Green border
                // Feedback Wrong: Light Red background, Red border
                
                val backgroundColor = when {
                    showFeedback && option == currentQuestion.correctAnswer -> Color(0xFFE8F5E9) // Success Green
                    showFeedback && isSelected && option != currentQuestion.correctAnswer -> Color(0xFFFFEBEE) // Error Red
                    isSelected -> themeColor.copy(alpha = 0.15f)
                    else -> Color.White
                }

                val borderColor = when {
                    showFeedback && option == currentQuestion.correctAnswer -> Color(0xFF4CAF50)
                    showFeedback && isSelected && option != currentQuestion.correctAnswer -> Color(0xFFF44336)
                    isSelected -> themeColor
                    else -> Color(0xFFE0E0E0)
                }

                QuizOptionButton(
                    text = option,
                    isSelected = isSelected,
                    enabled = !showFeedback,
                    backgroundColor = backgroundColor,
                    borderColor = borderColor,
                    onClick = { selectedOption = option }
                )
                Spacer(modifier = Modifier.height(16.dp))
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        if (!showFeedback) {
            BigGradeButton(
                text = "Check Answer",
                color = themeColor,
                enabled = selectedOption != null,
                onClick = {
                    if (selectedOption != null) {
                        isCorrect = selectedOption == currentQuestion.correctAnswer
                        if (isCorrect) score++
                        showFeedback = true
                    }
                }
            )
        } else {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = if (isCorrect) "Great Job! 🎉" else "Almost! Correct: ${currentQuestion.correctAnswer}",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = if (isCorrect) Color(0xFF4CAF50) else Color(0xFFF44336)
                )
                Spacer(modifier = Modifier.height(16.dp))
                BigGradeButton(
                    text = if (currentIndex < questions.size - 1) "Next Question" else "Finish",
                    color = themeColor,
                    onClick = {
                        if (currentIndex < questions.size - 1) {
                            currentIndex++
                            selectedOption = null
                            showFeedback = false
                        } else {
                            onFinish()
                        }
                    }
                )
            }
        }
    }
}

fun buildPromptForGrade(gradeLevel: Int): String {
    return when (gradeLevel) {
        3 -> "Generate exactly 5 multiple-choice math questions for Grade 3 students. Focus on addition, subtraction, simple multiplication, and basic word problems. Return as JSON with field 'questions' containing objects with 'questionText', 'option1', 'option2', 'option3', 'option4', 'correctAnswer' (string value matching one of the options)."
        4 -> "Generate exactly 5 multiple-choice math questions for Grade 4 students. Focus on multiplication, division, place value, fractions, and word problems. Return as JSON with field 'questions' containing objects with 'questionText', 'option1', 'option2', 'option3', 'option4', 'correctAnswer' (string value matching one of the options)."
        5 -> "Generate exactly 5 multiple-choice math questions for Grade 5 students. Focus on fractions, decimals, multiplication, division, and multi-step word problems. Return as JSON with field 'questions' containing objects with 'questionText', 'option1', 'option2', 'option3', 'option4', 'correctAnswer' (string value matching one of the options)."
        else -> "Generate exactly 5 multiple-choice math questions."
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
            val responseText = readResponseText(connection, responseCode)
            if (responseCode == 200) {
                val json = JSONObject(responseText)
                val questionsArray = json.optJSONArray("questions") ?: JSONArray()
                parseQuestions(questionsArray)
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            Log.e(TAG, "Fetch quiz failed", e)
            emptyList()
        } finally {
            connection.disconnect()
        }
    }
}

fun readResponseText(connection: HttpURLConnection, responseCode: Int): String {
    val stream = if (responseCode in 200..299) connection.inputStream else connection.errorStream
    val reader = BufferedReader(InputStreamReader(stream))
    val result = StringBuilder()
    var line: String?
    while (reader.readLine().also { line = it } != null) result.append(line)
    reader.close()
    return result.toString()
}

fun parseQuestions(questionsArray: JSONArray): List<DisplayQuestion> {
    val questions = mutableListOf<DisplayQuestion>()
    for (i in 0 until questionsArray.length()) {
        val obj = questionsArray.getJSONObject(i)
        questions.add(DisplayQuestion(
            questionText = obj.optString("questionText"),
            options = listOf(obj.optString("option1"), obj.optString("option2"), obj.optString("option3"), obj.optString("option4")).filter { it.isNotBlank() },
            correctAnswer = obj.optString("correctAnswer")
        ))
    }
    return questions
}
