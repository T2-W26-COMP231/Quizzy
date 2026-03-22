# Quizzy — Feature Branch: camila

## Overview

This branch implements two user-facing tasks for the Quizzy Android application, along with the necessary backend infrastructure to support quiz generation and session storage.

---

## Tasks Completed

### Task 1: Store Generated Quiz Questions in Local Storage
Quiz questions generated from the AI API are now saved to the database under a unique session ID. The session ID is returned alongside the questions so the frontend can reference the current quiz session.

### Task 2: Retrieve Quiz Questions from Local Storage and Display Question Text
The `QuizActivity` reads questions from `QuizRepository.currentQuizQuestions` (the in-memory local storage for the current session) and displays the question text in the question container (`tvQuestion`).

### Task 3: Retrieve Answer Options and Populate Multiple-Choice Buttons
The four answer options for each question are retrieved from the question object and populated into the RadioButtons (`option1` through `option4`) in the quiz layout.

---

## Backend Changes

### New Files
- `model/QuizSession.java` — JPA entity that maps to the `quiz_sessions` table. Stores raw JSON of generated questions and a creation timestamp.
- `repository/QuizSessionRepository.java` — Spring Data JPA repository for saving and retrieving quiz sessions.
- `service/QuizService.java` — Orchestrates the quiz generation flow: calls `AIService`, parses the response, saves the session to the database, and returns the result with a `sessionId`.

### Modified Files
- `controller/QuizController.java` — Added `POST /api/quiz/generate?prompt={prompt}` endpoint.
- `model/QuizResponse.java` — Added `sessionId` field to the response.

### New Endpoint
```
POST /api/quiz/generate?prompt=algebra basics
```

**Response:**
```json
{
  "sessionId": 1,
  "questions": [
    {
      "questionText": "What is 5 + 3?",
      "option1": "6",
      "option2": "7",
      "option3": "8",
      "option4": "9",
      "correctAnswer": "8"
    }
  ]
}
```

---

## Android Changes

### Modified Files
- `MainActivity.java` — Updated to call `POST /api/quiz/generate` instead of using hardcoded questions. Parses the JSON response, stores questions in `QuizRepository`, and launches `QuizActivity`.
- `AndroidManifest.xml` — Added `INTERNET` permission, `android:usesCleartextTraffic="true"`, and declared `QuizActivity`.
- `app/build.gradle.kts` — Fixed `compileSdk`, added `appcompat` dependency, and set `kotlinOptions.jvmTarget = "11"`.
- `gradle/libs.versions.toml` — Updated AGP to `8.5.2`, Kotlin to `2.0.0`, added `kotlin-android` plugin.
- `gradle.properties` — Added `android.useAndroidX=true`.
- `res/values/themes.xml` — Changed theme parent to `Theme.AppCompat.Light.NoActionBar` to fix crash on launch.

### How the Flow Works
```
User taps Easy / Medium / Hard
        ↓
GET /api/instructions/{level}  →  fetch prompt from backend
        ↓
POST /api/quiz/generate?prompt=...  →  AI generates questions
        ↓
Parse JSON → List<Question>
        ↓
QuizRepository.currentQuizQuestions = questionList  ← local storage
        ↓
QuizActivity reads QuizRepository → displays question text + answer options
```

---

## Testing

To test the display tasks without the backend, uncomment the test data block in `MainActivity.java`:

```java
// TEMPORARY TEST DATA - FOR TESTING PURPOSES ONLY
// Uncomment the lines below to test QuizActivity directly
// without needing the backend or difficulty buttons.
// List<Question> testQuestions = new ArrayList<>();
// testQuestions.add(new Question("What is 2 + 2?", "3", "4", "5", "6", "4"));
// testQuestions.add(new Question("What is 10 - 3?", "5", "6", "7", "8", "7"));
// QuizRepository.currentQuizQuestions = testQuestions;
// Intent intent = new Intent(MainActivity.this, QuizActivity.class);
// startActivity(intent);
```

---

## Backend Setup

1. Create `quizzy-backend/src/main/resources/application.properties` (not committed — add to `.gitignore`):

```properties
server.port=3000

spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

openrouter.api.key=YOUR_OPENROUTER_API_KEY
```

2. Run the backend:
```bash
cd quizzy-backend
./mvnw spring-boot:run
```

3. The backend will start on `http://localhost:3000`.

> **Note:** `application.properties` is listed in `.gitignore` to protect API keys. Each developer must create their own local copy with their own OpenRouter API key.
