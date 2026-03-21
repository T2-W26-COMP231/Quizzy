package com.example.quizzy;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private Button btnEasy, btnMedium, btnHard;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        btnEasy = findViewById(R.id.btnEasy);
        btnMedium = findViewById(R.id.btnMedium);
        btnHard = findViewById(R.id.btnHard);

        // ============================================================
        // TEMPORARY TEST DATA - FOR TESTING PURPOSES ONLY
        // Uncomment the lines below to test QuizActivity directly
        // without needing the backend or difficulty buttons.
        // Remember to comment them back out before final release.
        // ============================================================
        // List<Question> testQuestions = new ArrayList<>();
        // testQuestions.add(new Question("What is 2 + 2?", "3", "4", "5", "6", "4"));
        // testQuestions.add(new Question("What is 10 - 3?", "5", "6", "7", "8", "7"));
        // QuizRepository.currentQuizQuestions = testQuestions;
        // Intent intent = new Intent(MainActivity.this, QuizActivity.class);
        // startActivity(intent);
        // ============================================================

        btnEasy.setOnClickListener(v -> startQuizWithBackendPrompt(1, "easy"));
        btnMedium.setOnClickListener(v -> startQuizWithBackendPrompt(2, "medium"));
        btnHard.setOnClickListener(v -> startQuizWithBackendPrompt(3, "hard"));
    }

    private void startQuizWithBackendPrompt(int gradeLevel, String level) {
        new Thread(() -> {
            try {
                URL url = new URL("http://10.0.2.2:3000/api/instructions/" + gradeLevel);
                HttpURLConnection connection = (HttpURLConnection) url.openConnection();
                connection.setRequestMethod("GET");
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);

                int responseCode = connection.getResponseCode();
                InputStream stream = (responseCode >= 200 && responseCode < 300)
                        ? connection.getInputStream()
                        : connection.getErrorStream();

                BufferedReader reader = new BufferedReader(new InputStreamReader(stream, StandardCharsets.UTF_8));
                StringBuilder responseBuilder = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    responseBuilder.append(line);
                }

                reader.close();
                connection.disconnect();

                if (responseCode < 200 || responseCode >= 300) {
                    runOnUiThread(() ->
                            Toast.makeText(MainActivity.this, "Failed to load instructions", Toast.LENGTH_SHORT).show()
                    );
                    return;
                }

                JSONObject json = new JSONObject(responseBuilder.toString());
                String promptTemplate = json.getString("promptTemplate");
                String instructionText = json.getString("instructionText");

                String finalPrompt = promptTemplate + " " + instructionText;

                // Note: ApiService and Question classes need to exist
                // List<Question> generatedQuestions = ApiService.generateQuestionsFromPrompt(finalPrompt, level);
                // QuizRepository.currentQuizQuestions = generatedQuestions;

                runOnUiThread(() -> {
                    Intent intent = new Intent(MainActivity.this, QuizActivity.class);
                    startActivity(intent);
                });

            } catch (Exception e) {
                e.printStackTrace();
                runOnUiThread(() ->
                        Toast.makeText(MainActivity.this, "Error connecting to backend", Toast.LENGTH_SHORT).show()
                );
            }
        }).start();
    }
}
