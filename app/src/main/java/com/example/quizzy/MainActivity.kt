package com.example.quizzy

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import com.example.quizzy.network.NetworkClient
import com.example.quizzy.ui.theme.QuizzyTheme
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.components.Legend
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.TimeZone

data class GuardianQuizSession(
    val id: Int,
    val displaySessionNumber: Int = 0,
    val score: Int,
    val completedAt: String,
    val totalQuestions: Int
)

private const val NAV_PREFS = "quizzy_navigation_state"
private const val KEY_LAST_MAIN_SCREEN = "last_main_screen"
private const val KEY_GUARDIAN_FILTER = "guardian_filter"

private fun getNavPrefs(context: Context) =
    context.getSharedPreferences(NAV_PREFS, Context.MODE_PRIVATE)

private fun saveLastMainScreen(context: Context, screen: String) {
    getNavPrefs(context).edit().putString(KEY_LAST_MAIN_SCREEN, screen).apply()
}

private fun getLastMainScreen(context: Context): String {
    return getNavPrefs(context).getString(KEY_LAST_MAIN_SCREEN, "Home") ?: "Home"
}

private fun saveGuardianFilter(context: Context, filter: String) {
    getNavPrefs(context).edit().putString(KEY_GUARDIAN_FILTER, filter).apply()
}

private fun getGuardianFilter(context: Context): String {
    return getNavPrefs(context).getString(KEY_GUARDIAN_FILTER, "Last 15") ?: "Last 15"
}

class MainActivity : ComponentActivity() {

    private var pendingStartScreen by mutableStateOf<String?>(null)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        pendingStartScreen = intent.getStringExtra("start_screen")

        MusicManager.startMusic(this)

        val sessionManager = SessionManager(this)
        if (!sessionManager.isLoggedIn()) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }

        enableEdgeToEdge()

        setContent {
            val systemInDarkTheme = isSystemInDarkTheme()
            var isDarkMode by remember {
                mutableStateOf(sessionManager.isDarkMode(systemInDarkTheme))
            }

            QuizzyTheme(darkTheme = isDarkMode) {
                var currentScreen by remember {
                    mutableStateOf(getLastMainScreen(this@MainActivity))
                }

                LaunchedEffect(pendingStartScreen) {
                    val startScreen = pendingStartScreen
                    if (!startScreen.isNullOrBlank()) {
                        currentScreen = startScreen
                        saveLastMainScreen(this@MainActivity, startScreen)
                        pendingStartScreen = null
                    }
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Scaffold(
                        bottomBar = {
                            FancyNavigationBar(
                                currentScreen = currentScreen,
                                onTabSelected = { screen ->
                                    if (screen == "Achievements") {
                                        saveLastMainScreen(this@MainActivity, currentScreen)

                                        val achievementsIntent = Intent(
                                            this@MainActivity,
                                            AchievementsActivity::class.java
                                        )
                                        achievementsIntent.putExtra("start_screen", currentScreen)
                                        startActivity(achievementsIntent)
                                    } else {
                                        currentScreen = screen
                                        saveLastMainScreen(this@MainActivity, screen)
                                    }
                                }
                            )
                        }
                    ) { innerPadding ->
                        Box(modifier = Modifier.padding(innerPadding)) {
                            when (currentScreen) {
                                "Home", "Dashboard" -> DashboardScreen(
                                    onStartQuiz = {
                                        currentScreen = "QuizSelection"
                                        saveLastMainScreen(this@MainActivity, "QuizSelection")
                                    }
                                )

                                "QuizSelection" -> QuizSelectionScreen(
                                    onGradeSelected = { gradeLevel, gradeName ->
                                        val instructionsIntent = Intent(
                                            this@MainActivity,
                                            InstructionsActivity::class.java
                                        )
                                        instructionsIntent.putExtra("GRADE_LEVEL", gradeLevel)
                                        instructionsIntent.putExtra("GRADE_NAME", gradeName)
                                        startActivity(instructionsIntent)
                                    }
                                )

                                "Guardian" -> GuardianDashboardScreen()
                                "Settings" -> SettingsScreen(
                                    isDarkMode = isDarkMode,
                                    onThemeChanged = { dark ->
                                        isDarkMode = dark
                                        sessionManager.setThemeMode(dark)
                                    }
                                )

                                else -> DashboardScreen(
                                    onStartQuiz = {
                                        currentScreen = "QuizSelection"
                                        saveLastMainScreen(this@MainActivity, "QuizSelection")
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    override fun onResume() {
        super.onResume()
        MusicManager.startMusic(this)
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        pendingStartScreen = intent.getStringExtra("start_screen")
    }
}

@Composable
fun DashboardScreen(onStartQuiz: () -> Unit) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }
    var totalScore by remember { mutableIntStateOf(0) }
    var isLoading by remember { mutableStateOf(true) }
    val userId = sessionManager.getUserId()

    LaunchedEffect(Unit) {
        try {
            val result = NetworkClient.get("/score/user/$userId")

            if (result.isSuccess) {
                val json = result.getOrNull()
                totalScore = json?.optInt("totalScore", 0) ?: 0
            } else {
                totalScore = 0
            }
        } catch (_: Exception) {
            totalScore = 0
        } finally {
            isLoading = false
        }
    }

    val infiniteTransition = rememberInfiniteTransition(label = "pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 1f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(
            animation = tween(1000, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "scale"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(MaterialTheme.colorScheme.background, MaterialTheme.colorScheme.surfaceVariant)
                )
            )
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "Q",
                fontSize = 80.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color(0xFFA874FF)
            )
            Text(
                text = "uizzy",
                fontSize = 64.sp,
                fontWeight = FontWeight.ExtraBold,
                color = MaterialTheme.colorScheme.onBackground,
                modifier = Modifier.padding(top = 12.dp)
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Ready to test your knowledge?",
            fontSize = 20.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(24.dp))

        if (isLoading) {
            CircularProgressIndicator(color = Color(0xFFA874FF))
        } else {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp)
                    .shadow(10.dp, RoundedCornerShape(28.dp))
                    .background(
                        brush = Brush.linearGradient(
                            colors = listOf(Color(0xFFA874FF), Color(0xFFFFB26B))
                        ),
                        shape = RoundedCornerShape(28.dp)
                    )
                    .padding(24.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Total Score",
                            fontSize = 16.sp,
                            color = Color.White.copy(alpha = 0.9f),
                            fontWeight = FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = totalScore.toString(),
                            fontSize = 36.sp,
                            fontWeight = FontWeight.ExtraBold,
                            color = Color.White
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Keep earning points!",
                            fontSize = 14.sp,
                            color = Color.White.copy(alpha = 0.85f)
                        )
                    }

                    Box(
                        modifier = Modifier
                            .size(64.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Star,
                            contentDescription = "Score",
                            tint = Color.White,
                            modifier = Modifier.size(34.dp)
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(48.dp))

        Box(
            modifier = Modifier
                .width(200.dp)
                .height(80.dp)
                .graphicsLayer {
                    scaleX = scale
                    scaleY = scale
                }
                .shadow(elevation = 12.dp, shape = RoundedCornerShape(28.dp))
                .background(
                    brush = Brush.linearGradient(
                        colors = listOf(Color(0xFFA874FF), Color(0xFFFFB26B))
                    ),
                    shape = RoundedCornerShape(28.dp)
                )
                .clickable { onStartQuiz() },
            contentAlignment = Alignment.Center
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.PlayArrow,
                    contentDescription = "Start Quiz",
                    modifier = Modifier.size(44.dp),
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "PLAY",
                    color = Color.White,
                    fontSize = 28.sp,
                    fontWeight = FontWeight.Black,
                    letterSpacing = 2.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(48.dp))
    }
}

@Composable
fun QuizSelectionScreen(
    onGradeSelected: (Int, String) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(MaterialTheme.colorScheme.background, MaterialTheme.colorScheme.surfaceVariant)
                )
            )
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = "Select Grade",
            fontSize = 38.sp,
            fontWeight = FontWeight.ExtraBold,
            color = MaterialTheme.colorScheme.onBackground
        )

        Spacer(modifier = Modifier.height(12.dp))

        Text(
            text = "Choose your level to begin the quiz",
            fontSize = 18.sp,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(36.dp))

        BigGradeButton(
            text = "Grade 3",
            color = Color(0xFFA874FF),
            onClick = { onGradeSelected(3, "Grade 3") }
        )

        Spacer(modifier = Modifier.height(18.dp))

        BigGradeButton(
            text = "Grade 4",
            color = Color(0xFFFFB26B),
            onClick = { onGradeSelected(4, "Grade 4") }
        )

        Spacer(modifier = Modifier.height(18.dp))

        BigGradeButton(
            text = "Grade 5",
            color = Color(0xFF6FE3C1),
            onClick = { onGradeSelected(5, "Grade 5") }
        )
    }
}

private fun parseSessionDate(dateString: String): Date? {
    val patterns = listOf(
        "yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
        "yyyy-MM-dd'T'HH:mm:ssXXX",
        "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
        "yyyy-MM-dd'T'HH:mm:ss'Z'",
        "yyyy-MM-dd'T'HH:mm:ss.SSS",
        "yyyy-MM-dd'T'HH:mm:ss",
        "MMM d, yyyy - h:mm a"
    )

    for (pattern in patterns) {
        try {
            val formatter = SimpleDateFormat(pattern, Locale.getDefault())
            if (pattern.contains("'Z'") || pattern.contains("XXX")) {
                formatter.timeZone = TimeZone.getTimeZone("UTC")
            }
            return formatter.parse(dateString)
        } catch (_: Exception) {
        }
    }

    return null
}

private fun formatSessionDate(dateString: String): String {
    val date = parseSessionDate(dateString) ?: return dateString
    val formatter = SimpleDateFormat("d - MMM - yyyy", Locale.getDefault())
    return formatter.format(date)
}

private fun formatShortChartDate(dateString: String): String {
    val date = parseSessionDate(dateString) ?: return dateString
    val formatter = SimpleDateFormat("d MMM", Locale.getDefault())
    return formatter.format(date)
}

private fun isInCurrentDay(date: Date): Boolean {
    val now = Calendar.getInstance()
    val sessionCal = Calendar.getInstance().apply { time = date }

    return now.get(Calendar.YEAR) == sessionCal.get(Calendar.YEAR) &&
            now.get(Calendar.DAY_OF_YEAR) == sessionCal.get(Calendar.DAY_OF_YEAR)
}

private fun isInCurrentWeek(date: Date): Boolean {
    val now = Calendar.getInstance()
    val sessionCal = Calendar.getInstance().apply { time = date }

    return now.get(Calendar.YEAR) == sessionCal.get(Calendar.YEAR) &&
            now.get(Calendar.WEEK_OF_YEAR) == sessionCal.get(Calendar.WEEK_OF_YEAR)
}

private fun isInCurrentMonth(date: Date): Boolean {
    val now = Calendar.getInstance()
    val sessionCal = Calendar.getInstance().apply { time = date }

    return now.get(Calendar.YEAR) == sessionCal.get(Calendar.YEAR) &&
            now.get(Calendar.MONTH) == sessionCal.get(Calendar.MONTH)
}

private fun applySessionFilter(
    sessions: List<GuardianQuizSession>,
    filter: String
): List<GuardianQuizSession> {
    val sortedSessions = sessions.sortedBy { parseSessionDate(it.completedAt)?.time ?: 0L }

    return when (filter) {
        "Last 15" -> sortedSessions.takeLast(15)

        "Current Day" -> sortedSessions.filter { session ->
            val parsedDate = parseSessionDate(session.completedAt)
            parsedDate != null && isInCurrentDay(parsedDate)
        }

        "Current Week" -> sortedSessions.filter { session ->
            val parsedDate = parseSessionDate(session.completedAt)
            parsedDate != null && isInCurrentWeek(parsedDate)
        }

        "Current Month" -> sortedSessions.filter { session ->
            val parsedDate = parseSessionDate(session.completedAt)
            parsedDate != null && isInCurrentMonth(parsedDate)
        }

        else -> sortedSessions.takeLast(15)
    }
}

@Composable
fun GuardianDashboardScreen() {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }

    var totalScore by remember { mutableIntStateOf(0) }
    var allSessions by remember { mutableStateOf<List<GuardianQuizSession>>(emptyList()) }
    var displayedSessions by remember { mutableStateOf<List<GuardianQuizSession>>(emptyList()) }
    var selectedFilter by remember { mutableStateOf(getGuardianFilter(context)) }
    var selectedDisplay by remember {
        mutableStateOf(sessionManager.getSelectedDisplay() ?: "Latest Activity")
    }
    var allBadges by remember { mutableStateOf<List<Badges>>(emptyList()) }
    var isFilterDropdownExpanded by remember { mutableStateOf(false) }
    var isDisplayDropdownExpanded by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(true) }
    var errorMessage by remember { mutableStateOf("") }

    val userId = sessionManager.getUserId()
    val filterOptions = listOf("Last 15", "Current Day", "Current Week", "Current Month")
    val displayOptions = listOf("Latest Activity", "Charts", "Achievements")

    LaunchedEffect(Unit) {
        try {
            val scoreResult = NetworkClient.get("/score/user/$userId")

            if (scoreResult.isSuccess) {
                val scoreJson = scoreResult.getOrNull()
                totalScore = scoreJson?.optInt("totalScore", 0) ?: 0
            }

            val sessionsResult = NetworkClient.get("/guardian/$userId/latest-sessions")

            if (sessionsResult.isSuccess) {
                val json = sessionsResult.getOrNull()
                Log.d("GUARDIAN_RAW", json.toString())

                val loadedSessions = mutableListOf<GuardianQuizSession>()

                val sessionsArray = if (json != null && json.has("data")) {
                    json.getJSONArray("data")
                } else {
                    null
                }

                if (sessionsArray != null) {
                    for (i in 0 until sessionsArray.length()) {
                        val item = sessionsArray.getJSONObject(i)

                        loadedSessions.add(
                            GuardianQuizSession(
                                id = item.optInt("id", 0),
                                displaySessionNumber = item.optInt("displaySessionNumber", i + 1),
                                score = item.optInt("finalscore", 0),
                                completedAt = item.optString("completion", "Unknown"),
                                totalQuestions = 5
                            )
                        )
                    }
                }

                allSessions = loadedSessions
                displayedSessions = applySessionFilter(loadedSessions, selectedFilter)
            } else {
                val error = sessionsResult.exceptionOrNull()
                Log.e("GUARDIAN", "Sessions fetch failed: ${error?.message}", error)
                errorMessage = "Failed to load latest sessions"
            }

            QuizRepository.getUserBadges(userId.toInt(), object : QuizRepository.BadgeCallback {
                override fun onSuccess(earnedBadges: List<Badges>) {
                    Handler(Looper.getMainLooper()).post {
                        val catalogBadges = BadgeCatalog.getAllBadges()
                        allBadges = BadgeManager.mergeBadgeStates(catalogBadges, earnedBadges)
                    }
                }

                override fun onError(error: String) {
                    Handler(Looper.getMainLooper()).post {
                        if (allBadges.isEmpty()) {
                            allBadges = BadgeCatalog.getAllBadges()
                        }
                    }
                }
            })
        } catch (e: Exception) {
            Log.e("GUARDIAN", "Error: ${e.message}", e)
            errorMessage = "Something went wrong"
        } finally {
            isLoading = false
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Column(modifier = Modifier.padding(start = 24.dp, end = 24.dp, top = 24.dp)) {
                Text(
                    text = "Guardian Dashboard",
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.onBackground
                )

                Spacer(modifier = Modifier.height(24.dp))

                if (isLoading) {
                    Box(
                        modifier = Modifier.fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(color = Color(0xFFA874FF))
                    }
                } else if (errorMessage.isNotBlank()) {
                    Text(
                        text = errorMessage,
                        color = Color.Red,
                        fontSize = 16.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                } else {
                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(24.dp),
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 8.dp
                    ) {
                        Column(modifier = Modifier.padding(24.dp)) {
                            Text(
                                text = "Student Progress:",
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "Total Score: $totalScore",
                                fontSize = 28.sp,
                                fontWeight = FontWeight.ExtraBold,
                                color = Color(0xFFA874FF)
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(20.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Box(modifier = Modifier.weight(1f)) {
                            Button(
                                onClick = { isFilterDropdownExpanded = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    contentColor = MaterialTheme.colorScheme.onSurface
                                ),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                            ) {
                                Text(
                                    text = "Filter: $selectedFilter",
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Filter dropdown"
                                )
                            }

                            DropdownMenu(
                                expanded = isFilterDropdownExpanded,
                                onDismissRequest = { isFilterDropdownExpanded = false }
                            ) {
                                filterOptions.forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option) },
                                        onClick = {
                                            selectedFilter = option
                                            saveGuardianFilter(context, option)
                                            displayedSessions = applySessionFilter(allSessions, option)
                                            isFilterDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }

                        Box(modifier = Modifier.weight(1f)) {
                            Button(
                                onClick = { isDisplayDropdownExpanded = true },
                                modifier = Modifier.fillMaxWidth(),
                                shape = RoundedCornerShape(16.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = MaterialTheme.colorScheme.surface,
                                    contentColor = MaterialTheme.colorScheme.onSurface
                                ),
                                elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp)
                            ) {
                                Text(
                                    text = "Display: $selectedDisplay",
                                    fontWeight = FontWeight.SemiBold
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Icon(
                                    imageVector = Icons.Default.ArrowDropDown,
                                    contentDescription = "Display dropdown"
                                )
                            }

                            DropdownMenu(
                                expanded = isDisplayDropdownExpanded,
                                onDismissRequest = { isDisplayDropdownExpanded = false }
                            ) {
                                displayOptions.forEach { option ->
                                    DropdownMenuItem(
                                        text = { Text(option) },
                                        onClick = {
                                            selectedDisplay = option
                                            sessionManager.saveSelectedDisplay(option)
                                            isDisplayDropdownExpanded = false
                                        }
                                    )
                                }
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    when (selectedDisplay) {
                        "Latest Activity" -> {
                            Text(
                                text = "Latest Activity",
                                fontSize = 18.sp,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                fontWeight = FontWeight.Medium
                            )

                            Spacer(modifier = Modifier.height(16.dp))

                            if (displayedSessions.isEmpty()) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(top = 16.dp),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = if (allSessions.isEmpty()) {
                                            "No quiz sessions found."
                                        } else {
                                            "No quiz sessions found for $selectedFilter."
                                        },
                                        fontSize = 16.sp,
                                        color = Color.Gray
                                    )
                                }
                            } else {
                                LazyColumn(
                                    modifier = Modifier.fillMaxSize(),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    itemsIndexed(displayedSessions) { index, session ->
                                        Surface(
                                            modifier = Modifier.fillMaxWidth(),
                                            shape = RoundedCornerShape(20.dp),
                                            color = MaterialTheme.colorScheme.surface,
                                            shadowElevation = 4.dp
                                        ) {
                                            Column(modifier = Modifier.padding(16.dp)) {
                                                Text(
                                                    text = "Session ${index + 1}",
                                                    fontSize = 18.sp,
                                                    fontWeight = FontWeight.Bold,
                                                    color = MaterialTheme.colorScheme.onSurface
                                                )

                                                Spacer(modifier = Modifier.height(8.dp))

                                                Text(
                                                    text = "Score: ${session.score}",
                                                    fontSize = 16.sp,
                                                    color = Color(0xFFA874FF),
                                                    fontWeight = FontWeight.SemiBold
                                                )

                                                Text(
                                                    text = "Total Questions: ${session.totalQuestions}",
                                                    fontSize = 14.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )

                                                Text(
                                                    text = "Completed: ${formatSessionDate(session.completedAt)}",
                                                    fontSize = 14.sp,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        "Charts" -> {
                            GuardianChartsView(allSessions = displayedSessions)
                        }

                        "Achievements" -> {
                            GuardianAchievementsView(allBadges = allBadges)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GuardianAchievementsView(allBadges: List<Badges>) {
    Column(modifier = Modifier.fillMaxSize()) {
        Text(
            text = "Achievements",
            fontSize = 18.sp,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            fontWeight = FontWeight.Medium
        )

        Spacer(modifier = Modifier.height(16.dp))

        if (allBadges.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 24.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "No badges available yet.",
                    fontSize = 16.sp,
                    color = Color.Gray
                )
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(bottom = 16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                itemsIndexed(allBadges) { _, badge ->
                    val unlocked = badge.isUnlocked

                    Surface(
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(20.dp),
                        color = MaterialTheme.colorScheme.surface,
                        shadowElevation = 4.dp
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = if (unlocked) "🏆" else "🔒",
                                fontSize = 28.sp
                            )

                            Spacer(modifier = Modifier.width(14.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = badge.name,
                                    fontSize = 18.sp,
                                    fontWeight = FontWeight.Bold,
                                    color = if (unlocked) MaterialTheme.colorScheme.onSurface else Color(0xFF9E9E9E)
                                )

                                Spacer(modifier = Modifier.height(4.dp))

                                Text(
                                    text = badge.description,
                                    fontSize = 14.sp,
                                    color = if (unlocked) MaterialTheme.colorScheme.onSurfaceVariant else Color(0xFFBDBDBD)
                                )

                                Spacer(modifier = Modifier.height(6.dp))

                                Text(
                                    text = if (unlocked) "Unlocked" else "Locked",
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.SemiBold,
                                    color = if (unlocked) Color(0xFF2E7D32) else Color(0xFF9E9E9E)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun GuardianChartsView(allSessions: List<GuardianQuizSession>) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }

    var selectedChart by remember {
        mutableStateOf(sessionManager.getSelectedChart() ?: "Pie Chart")
    }

    val chartTypes = listOf("Pie Chart", "Bar Chart", "Line Chart")

    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(bottom = 80.dp),
            contentAlignment = Alignment.Center
        ) {
            when {
                allSessions.isEmpty() -> {
                    Text(
                        text = "No chart data available.",
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                }

                selectedChart == "Line Chart" -> {
                    GuardianLineChartView(sessions = allSessions)
                }

                selectedChart == "Bar Chart" -> {
                    GuardianBarChartView(sessions = allSessions)
                }

                selectedChart == "Pie Chart" -> {
                    GuardianPieChartView(sessions = allSessions)
                }

                else -> {
                    Text(
                        text = "$selectedChart screen coming soon!",
                        fontSize = 18.sp,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        fontWeight = FontWeight.Medium,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }

        Surface(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(start = 24.dp, end = 24.dp, bottom = 12.dp),
            shape = RoundedCornerShape(24.dp),
            color = MaterialTheme.colorScheme.surface,
            shadowElevation = 12.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                chartTypes.forEach { type ->
                    val isSelected = selectedChart == type
                    val label = type.split(" ")[0]

                    Column(
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .background(
                                if (isSelected) Color(0xFFA874FF).copy(alpha = 0.22f)
                                else Color.Transparent
                            )
                            .clickable {
                                selectedChart = type
                                sessionManager.saveSelectedChart(type)
                            }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = label,
                            color = if (isSelected) Color(0xFFA874FF) else MaterialTheme.colorScheme.onSurfaceVariant,
                            fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.Medium,
                            fontSize = 14.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun GuardianBarChartView(sessions: List<GuardianQuizSession>) {
    val sortedSessions = remember(sessions) {
        sessions.sortedBy { parseSessionDate(it.completedAt)?.time ?: 0L }
    }

    val entries = remember(sortedSessions) {
        sortedSessions.mapIndexed { index, session ->
            BarEntry(index.toFloat(), session.score.toFloat())
        }
    }

    val xLabels = remember(sortedSessions) {
        sortedSessions.map { formatShortChartDate(it.completedAt) }
    }

    val chartBarColor = Color(0xFFA874FF).toArgb()
    val chartTextColor = MaterialTheme.colorScheme.onSurface.toArgb()
    val chartGridColor = MaterialTheme.colorScheme.outlineVariant.toArgb()

    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(300.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Score",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .graphicsLayer { rotationZ = -90f }
                    .padding(bottom = 8.dp)
            )

            AndroidView(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                factory = { context ->
                    BarChart(context).apply {
                        layoutParams = android.view.ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                        description.isEnabled = false
                        setTouchEnabled(true)
                        setPinchZoom(false)
                        setScaleEnabled(false)
                        legend.isEnabled = false
                        axisRight.isEnabled = false
                        setNoDataText("No chart data available")
                        setNoDataTextColor(chartTextColor)

                        setExtraOffsets(12f, 12f, 12f, 12f)

                        xAxis.apply {
                            position = XAxis.XAxisPosition.BOTTOM
                            granularity = 1f
                            isGranularityEnabled = true
                            textColor = chartTextColor
                            textSize = 11f
                            setDrawGridLines(false)
                            labelRotationAngle = -20f
                            valueFormatter =
                                object : com.github.mikephil.charting.formatter.ValueFormatter() {
                                    override fun getFormattedValue(value: Float): String {
                                        val index = value.toInt()
                                        return if (index in xLabels.indices) xLabels[index] else ""
                                    }
                                }
                        }

                        axisLeft.apply {
                            axisMinimum = 0f
                            granularity = 1f
                            textColor = chartTextColor
                            textSize = 11f
                            gridColor = chartGridColor
                            axisLineColor = chartGridColor
                        }
                    }
                },
                update = { chart ->
                    val dataSet = BarDataSet(entries, "Quiz Scores").apply {
                        color = chartBarColor
                        valueTextColor = chartTextColor
                        valueTextSize = 11f
                        setDrawValues(true)
                        valueFormatter =
                            object : com.github.mikephil.charting.formatter.ValueFormatter() {
                                override fun getFormattedValue(value: Float): String {
                                    return value.toInt().toString()
                                }
                            }
                    }

                    chart.data = BarData(dataSet).apply {
                        barWidth = 0.6f
                    }
                    chart.invalidate()
                }
            )
        }

        Text(
            text = "Date",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
fun GuardianPieChartView(sessions: List<GuardianQuizSession>) {
    val totalCorrect = sessions.sumOf { it.score }
    val totalQuestions = sessions.sumOf { it.totalQuestions }
    val totalIncorrect = totalQuestions - totalCorrect

    val entries = listOf(
        PieEntry(totalCorrect.toFloat(), "Correct"),
        PieEntry(totalIncorrect.toFloat(), "Incorrect")
    )

    val colors = listOf(
        Color.Green.toArgb(),
        Color.Red.toArgb()
    )

    val chartTextColor = MaterialTheme.colorScheme.onSurface.toArgb()

    AndroidView(
        modifier = Modifier
            .fillMaxWidth()
            .height(320.dp),
        factory = { context ->
            PieChart(context).apply {
                layoutParams = android.view.ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                description.isEnabled = false
                isDrawHoleEnabled = true
                setHoleColor(android.graphics.Color.TRANSPARENT)
                setTransparentCircleAlpha(0)
                setDrawEntryLabels(true)
                setEntryLabelColor(chartTextColor)
                setEntryLabelTextSize(12f)

                legend.isEnabled = true
                legend.textColor = chartTextColor
                legend.horizontalAlignment = Legend.LegendHorizontalAlignment.CENTER
            }
        },
        update = { chart ->
            val dataSet = PieDataSet(entries, "").apply {
                this.colors = colors
                valueTextColor = android.graphics.Color.WHITE
                valueTextSize = 14f
                valueTypeface = Typeface.DEFAULT_BOLD
                sliceSpace = 3f
            }
            chart.data = PieData(dataSet).apply {
                setValueFormatter(
                    object : com.github.mikephil.charting.formatter.ValueFormatter() {
                        override fun getFormattedValue(value: Float): String =
                            value.toInt().toString()
                    }
                )
            }
            chart.invalidate()
        }
    )
}

@Composable
fun GuardianLineChartView(sessions: List<GuardianQuizSession>) {
    val sortedSessions = remember(sessions) {
        sessions.sortedBy { parseSessionDate(it.completedAt)?.time ?: 0L }
    }

    val entries = remember(sortedSessions) {
        sortedSessions.mapIndexed { index, session ->
            Entry(index.toFloat(), session.score.toFloat())
        }
    }

    val xLabels = remember(sortedSessions) {
        sortedSessions.map { formatShortChartDate(it.completedAt) }
    }

    val chartLineColor = Color(0xFFA874FF).toArgb()
    val chartTextColor = MaterialTheme.colorScheme.onSurface.toArgb()
    val chartGridColor = MaterialTheme.colorScheme.outlineVariant.toArgb()

    Column(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth().height(300.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Score",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier
                    .graphicsLayer { rotationZ = -90f }
                    .padding(bottom = 8.dp)
            )

            AndroidView(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                factory = { context ->
                    LineChart(context).apply {
                        layoutParams = android.view.ViewGroup.LayoutParams(MATCH_PARENT, MATCH_PARENT)
                        description.isEnabled = false
                        setTouchEnabled(true)
                        setPinchZoom(false)
                        setScaleEnabled(false)
                        legend.isEnabled = false
                        axisRight.isEnabled = false
                        setNoDataText("No chart data available")
                        setNoDataTextColor(chartTextColor)

                        setExtraOffsets(12f, 12f, 12f, 12f)

                        xAxis.apply {
                            position = XAxis.XAxisPosition.BOTTOM
                            granularity = 1f
                            isGranularityEnabled = true
                            textColor = chartTextColor
                            textSize = 11f
                            setDrawGridLines(false)
                            labelRotationAngle = -20f
                            valueFormatter =
                                object : com.github.mikephil.charting.formatter.ValueFormatter() {
                                    override fun getFormattedValue(value: Float): String {
                                        val index = value.toInt()
                                        return if (index in xLabels.indices) xLabels[index] else ""
                                    }
                                }
                        }

                        axisLeft.apply {
                            axisMinimum = 0f
                            granularity = 1f
                            textColor = chartTextColor
                            textSize = 11f
                            gridColor = chartGridColor
                            axisLineColor = chartGridColor
                        }
                    }
                },
                update = { chart ->
                    val dataSet = LineDataSet(entries, "Quiz Scores").apply {
                        color = chartLineColor
                        setCircleColor(chartLineColor)
                        circleRadius = 5f
                        lineWidth = 2.5f
                        valueTextColor = chartTextColor
                        valueTextSize = 11f
                        setDrawFilled(false)
                        mode = LineDataSet.Mode.LINEAR
                        setDrawValues(true)
                        valueTypeface = Typeface.DEFAULT_BOLD
                    }

                    chart.data = LineData(dataSet)
                    chart.xAxis.axisMaximum =
                        (if (entries.isEmpty()) 0f else (entries.size - 1).toFloat()) + 0.2f
                    chart.xAxis.axisMinimum = -0.2f
                    chart.invalidate()
                }
            )
        }

        Text(
            text = "Date",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}

@Composable
fun SettingsScreen(
    isDarkMode: Boolean,
    onThemeChanged: (Boolean) -> Unit
) {
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }

    val prefs = context.getSharedPreferences("quizzy_settings", Context.MODE_PRIVATE)

    var volume by remember {
        mutableStateOf(prefs.getFloat("music_volume", 0.2f))
    }

    var sfxVolume by remember {
        mutableStateOf(prefs.getFloat("sfx_volume", 0.7f))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFFFFBF2))
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = "Settings",
            fontSize = 32.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Color(0xFF5A4A3B)
        )

        Spacer(modifier = Modifier.height(32.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            shadowElevation = 4.dp
        ) {
            Column(modifier = Modifier.padding(24.dp)) {
                Text(
                    text = "Logged in as:",
                    fontSize = 16.sp,
                    color = Color(0xFF7B6A58)
                )
                Text(
                    text = sessionManager.getUsername() ?: "Guest",
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF5A4A3B)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            shadowElevation = 4.dp
        ) {
            Column(modifier = Modifier.padding(24.dp)) {

                Text(
                    text = "Music Volume",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF5A4A3B)
                )

                Spacer(modifier = Modifier.height(12.dp))

                androidx.compose.material3.Slider(
                    value = volume,
                    onValueChange = { newVolume ->
                        volume = newVolume
                        MusicManager.setVolume(context, newVolume)
                        prefs.edit().putFloat("music_volume", newVolume).apply()
                    },
                    valueRange = 0f..1f
                )

                Text(
                    text = "${(volume * 100).toInt()}%",
                    fontSize = 14.sp,
                    color = Color(0xFF7B6A58)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            shadowElevation = 4.dp
        ) {
            Column(modifier = Modifier.padding(24.dp)) {

                Text(
                    text = "Sound Effects Volume",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF5A4A3B)
                )

                Spacer(modifier = Modifier.height(12.dp))

                androidx.compose.material3.Slider(
                    value = sfxVolume,
                    onValueChange = { newVolume ->
                        sfxVolume = newVolume
                        MusicManager.setSFXVolume(context, newVolume)
                        prefs.edit().putFloat("sfx_volume", newVolume).apply()
                    },
                    valueRange = 0f..1f
                )

                Text(
                    text = "${(sfxVolume * 100).toInt()}%",
                    fontSize = 14.sp,
                    color = Color(0xFF7B6A58)
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(24.dp),
            color = Color.White,
            shadowElevation = 4.dp
        ) {
            Row(
                modifier = Modifier
                    .padding(24.dp)
                    .fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = "Dark Mode",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color(0xFF5A4A3B)
                )

                Switch(
                    checked = isDarkMode,
                    onCheckedChange = onThemeChanged
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        Button(
            onClick = {
                sessionManager.logout()
                val intent = Intent(context, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                context.startActivity(intent)
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp),
            shape = RoundedCornerShape(20.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFF6B6B))
        ) {
            Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = null)
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = "LOGOUT",
                fontSize = 18.sp,
                fontWeight = FontWeight.Bold
            )
        }

        Spacer(modifier = Modifier.height(32.dp))
    }
}

@Composable
fun BigGradeButton(
    text: String,
    color: Color,
    onClick: () -> Unit
) {
    Button(
        onClick = onClick,
        modifier = Modifier
            .fillMaxWidth()
            .height(88.dp),
        shape = RoundedCornerShape(24.dp),
        colors = ButtonDefaults.buttonColors(containerColor = color),
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
fun FancyNavigationBar(
    currentScreen: String,
    onTabSelected: (String) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        shape = RoundedCornerShape(32.dp),
        color = MaterialTheme.colorScheme.surface,
        shadowElevation = 12.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 12.dp, horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            NavBarItem(
                icon = Icons.Default.Home,
                label = "Home",
                isSelected = currentScreen == "Home" ||
                        currentScreen == "Dashboard" ||
                        currentScreen == "QuizSelection",
                onClick = { onTabSelected("Home") }
            )

            NavBarItem(
                icon = Icons.Default.Star,
                label = "Awards",
                isSelected = false,
                onClick = { onTabSelected("Achievements") }
            )

            NavBarItem(
                icon = Icons.Default.Person,
                label = "Guardian",
                isSelected = currentScreen == "Guardian",
                onClick = { onTabSelected("Guardian") }
            )

            NavBarItem(
                icon = Icons.Default.Settings,
                label = "Settings",
                isSelected = currentScreen == "Settings",
                onClick = { onTabSelected("Settings") }
            )
        }
    }
}

@Composable
fun NavBarItem(
    icon: ImageVector,
    label: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val tint = if (isSelected) Color(0xFFA874FF) else MaterialTheme.colorScheme.onSurfaceVariant
    val background =
        if (isSelected) Color(0xFFA874FF).copy(alpha = 0.1f) else Color.Transparent

    Column(
        modifier = Modifier
            .clip(RoundedCornerShape(16.dp))
            .background(background)
            .clickable { onClick() }
            .padding(horizontal = 12.dp, vertical = 8.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = label,
            tint = tint,
            modifier = Modifier.size(28.dp)
        )

        if (isSelected) {
            Text(
                text = label,
                color = tint,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold
            )
        }
    }
}