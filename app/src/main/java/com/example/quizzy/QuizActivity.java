package com.example.quizzy;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.google.android.material.card.MaterialCardView;
import com.google.android.material.snackbar.Snackbar;

import java.util.List;
import java.util.Locale;

/**
 * Activity that manages the execution of a quiz session.
 * It displays questions one by one, validates user answers, tracks the score, 
 * and provides visual feedback for each attempt.
 */
public class QuizActivity extends AppCompatActivity {

    // UI Components
    private TextView tvQuestionNumber, tvQuestion, tvScore, tvFeedback;
    private RadioGroup radioGroup;
    private RadioButton option1, option2, option3, option4;
    private Button btnSubmit, btnNext;
    private ProgressBar progressBar;
    private MaterialCardView feedbackCard, questionCard;
    private View quizRoot;

    // Session State
    private List<Question> questionList;
    private int currentQuestionIndex = 0;
    private int score = 0;
    private boolean answered = false;
    private int gradeLevel;
    private SessionManager sessionManager;

    /**
     * Color constants for dynamic theme application.
     */
    private static final String DARK_MODE_BACKGROUND = "#121212";
    private static final String DARK_MODE_CARD = "#1E1E1E";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quiz);

        initializeComponents();
        
        sessionManager = new SessionManager(this);
        gradeLevel = getIntent().getIntExtra("GRADE_LEVEL", 3);

        applyTheme();

        // Retrieve questions pre-loaded by InstructionsActivity
        questionList = QuizRepository.currentQuizQuestions;

        if (questionList == null || questionList.isEmpty()) {
            Toast.makeText(this, "No questions available", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        progressBar.setMax(questionList.size());
        showQuestion();

        btnSubmit.setOnClickListener(v -> validateAnswer());
        btnNext.setOnClickListener(v -> navigateToNextQuestion());
    }

    /**
     * Finds and assigns all UI components from the layout.
     */
    private void initializeComponents() {
        quizRoot = findViewById(R.id.quizRoot);
        tvQuestionNumber = findViewById(R.id.tvQuestionNumber);
        tvQuestion = findViewById(R.id.tvQuestion);
        tvScore = findViewById(R.id.tvScore);
        tvFeedback = findViewById(R.id.tvFeedback);
        radioGroup = findViewById(R.id.radioGroup);
        option1 = findViewById(R.id.option1);
        option2 = findViewById(R.id.option2);
        option3 = findViewById(R.id.option3);
        option4 = findViewById(R.id.option4);
        btnSubmit = findViewById(R.id.btnSubmit);
        btnNext = findViewById(R.id.btnNext);
        progressBar = findViewById(R.id.progressBar);
        feedbackCard = findViewById(R.id.feedbackCard);
        questionCard = findViewById(R.id.questionCard);
    }

    /**
     * Adjusts the UI appearance based on the user's dark mode preference.
     */
    private void applyTheme() {
        if (sessionManager.isDarkMode()) {
            if (quizRoot != null) {
                quizRoot.setBackgroundColor(Color.parseColor(DARK_MODE_BACKGROUND));
            }

            int textColor = Color.WHITE;
            tvQuestionNumber.setTextColor(textColor);
            
            if (questionCard != null) {
                questionCard.setCardBackgroundColor(Color.parseColor(DARK_MODE_CARD));
                questionCard.setStrokeWidth(0);
            }
            tvQuestion.setTextColor(textColor);

            option1.setTextColor(textColor);
            option2.setTextColor(textColor);
            option3.setTextColor(textColor);
            option4.setTextColor(textColor);
        }
    }

    /**
     * Updates the UI to display the current question and its options.
     */
    private void showQuestion() {
        Question currentQuestion = questionList.get(currentQuestionIndex);

        tvQuestionNumber.setText(String.format(Locale.getDefault(),
                "Question %d of %d",
                currentQuestionIndex + 1,
                questionList.size()));

        tvQuestion.setText(currentQuestion.getQuestionText());
        tvScore.setText(String.format(Locale.getDefault(), "Score: %d", score));

        option1.setText(currentQuestion.getOption1());
        option2.setText(currentQuestion.getOption2());
        option3.setText(currentQuestion.getOption3());
        option4.setText(currentQuestion.getOption4());

        radioGroup.clearCheck();
        enableOptions(true);

        progressBar.setProgress(currentQuestionIndex + 1);
        feedbackCard.setVisibility(View.GONE);

        btnSubmit.setVisibility(View.VISIBLE);
        btnNext.setVisibility(View.GONE);
    }

    /**
     * Algorithm: Validates the user's selection against the correct answer.
     * 1. Checks if an option is selected.
     * 2. Plays a UI feedback sound.
     * 3. Compares the selected text with the 'correctAnswer' field.
     * 4. Updates the score and displays a success/failure card.
     */
    private void validateAnswer() {
        if (answered) return;
        
        MusicManager.INSTANCE.playClickSound(this);

        int selectedId = radioGroup.getCheckedRadioButtonId();

        if (selectedId == -1) {
            View contextView = findViewById(android.R.id.content);
            Snackbar.make(contextView, "Please select an answer", Snackbar.LENGTH_SHORT).show();
            return;
        }

        RadioButton selectedButton = findViewById(selectedId);
        String selectedAnswer = selectedButton.getText().toString();
        Question currentQuestion = questionList.get(currentQuestionIndex);

        answered = true;
        enableOptions(false);
        feedbackCard.setVisibility(View.VISIBLE);

        if (selectedAnswer.equals(currentQuestion.getCorrectAnswer())) {
            score++;
            tvFeedback.setText("Correct! 🎉");
            feedbackCard.setCardBackgroundColor(
                    ContextCompat.getColor(this, android.R.color.holo_green_dark)
            );
        } else {
            String feedbackMsg = "Wrong. The correct answer was: " + currentQuestion.getCorrectAnswer();
            tvFeedback.setText(feedbackMsg);
            feedbackCard.setCardBackgroundColor(
                    ContextCompat.getColor(this, android.R.color.holo_red_dark)
            );
        }

        tvScore.setText(String.format(Locale.getDefault(), "Score: %d", score));
        btnSubmit.setVisibility(View.GONE);
        btnNext.setVisibility(View.VISIBLE);
    }

    /**
     * Moves to the next question in the list or finishes the quiz if at the end.
     */
    private void navigateToNextQuestion() {
        currentQuestionIndex++;
        answered = false;

        if (currentQuestionIndex < questionList.size()) {
            showQuestion();
        } else {
            openResultsScreen();
        }
    }

    /**
     * Helper to enable or disable interaction with the radio group options.
     */
    private void enableOptions(boolean enable) {
        for (int i = 0; i < radioGroup.getChildCount(); i++) {
            radioGroup.getChildAt(i).setEnabled(enable);
        }
    }

    /**
     * Calculates achievements and transitions to the results activity.
     */
    private void openResultsScreen() {
        BadgeManager.checkAndUnlockBadges(this, score, questionList.size(), gradeLevel);

        Intent intent = new Intent(QuizActivity.this, ResultActivity.class);
        intent.putExtra("score", score);
        intent.putExtra("totalQuestions", questionList.size());
        intent.putExtra("sessionId", QuizRepository.currentSessionId);
        startActivity(intent);
        finish();
    }
}
