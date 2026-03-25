package com.example.quizzy;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.snackbar.Snackbar;

import java.util.List;
import java.util.Locale;

public class QuizActivity extends AppCompatActivity {

    private TextView tvQuestionNumber, tvQuestion, tvScore;
    private RadioGroup radioGroup;
    private RadioButton option1, option2, option3, option4;
    private Button btnSubmit, btnNext;

    private List<Question> questionList;
    private int currentQuestionIndex = 0;
    private int score = 0;
    private boolean answered = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        tvQuestionNumber = findViewById(R.id.tvQuestionNumber);
        tvQuestion = findViewById(R.id.tvQuestion);
        tvScore = findViewById(R.id.tvScore);
        radioGroup = findViewById(R.id.radioGroup);
        option1 = findViewById(R.id.option1);
        option2 = findViewById(R.id.option2);
        option3 = findViewById(R.id.option3);
        option4 = findViewById(R.id.option4);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnNext = findViewById(R.id.btnNext);

        questionList = QuizRepository.currentQuizQuestions;

        if (questionList == null || questionList.isEmpty()) {
            Toast.makeText(this, "No questions available", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        showQuestion();

        btnSubmit.setOnClickListener(v -> validateAnswer());

        btnNext.setOnClickListener(v -> {
            currentQuestionIndex++;
            answered = false;
            if (currentQuestionIndex < questionList.size()) {
                showQuestion();
            } else {
                Toast.makeText(this, String.format(Locale.getDefault(), "Quiz completed! Your score: %d", score), Toast.LENGTH_LONG).show();
                finish();
            }
        });
    }

    private void showQuestion() {
        Question currentQuestion = questionList.get(currentQuestionIndex);
        tvQuestionNumber.setText(String.format(Locale.getDefault(), "Question %d of %d", currentQuestionIndex + 1, questionList.size()));
        tvQuestion.setText(currentQuestion.getQuestionText());
        tvScore.setText(String.format(Locale.getDefault(), "Score: %d", score));
        option1.setText(currentQuestion.getOption1());
        option2.setText(currentQuestion.getOption2());
        option3.setText(currentQuestion.getOption3());
        option4.setText(currentQuestion.getOption4());
        radioGroup.clearCheck();
        btnSubmit.setVisibility(View.VISIBLE);
        btnNext.setVisibility(View.GONE);
    }

    private void validateAnswer() {
        if (answered) return;

        int selectedId = radioGroup.getCheckedRadioButtonId();

        if (selectedId == -1) {
            View contextView = findViewById(android.R.id.content);
            Snackbar.make(contextView, "Please select an answer", Snackbar.LENGTH_SHORT).show();
            return;
        }

        RadioButton selectedButton = findViewById(selectedId);
        String selectedAnswer = selectedButton.getText().toString();
        Question currentQuestion = questionList.get(currentQuestionIndex);

        View layout = findViewById(android.R.id.content);

        if (selectedAnswer.equals(currentQuestion.getCorrectAnswer())) {
            score++;
            Snackbar.make(layout, "Correct!", Snackbar.LENGTH_LONG)
                    .setBackgroundTint(ContextCompat.getColor(this, android.R.color.holo_green_dark))
                    .show();
        } else {
            Snackbar.make(layout, "Wrong. The answer was: " + currentQuestion.getCorrectAnswer(), Snackbar.LENGTH_LONG)
                    .setBackgroundTint(ContextCompat.getColor(this, android.R.color.holo_red_dark))
                    .show();
        }

        answered = true;
        tvScore.setText(String.format(Locale.getDefault(), "Score: %d", score));
        btnSubmit.setVisibility(View.GONE);
        btnNext.setVisibility(View.VISIBLE);
    }
}
