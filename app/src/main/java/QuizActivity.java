import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

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

        btnSubmit.setOnClickListener(v -> validateAnswer());  // Submit button wired here
    }

    private void showQuestion() {
        Question currentQuestion = questionList.get(currentQuestionIndex);
        tvQuestionNumber.setText("Question " + (currentQuestionIndex + 1) + " of " + questionList.size());
        tvQuestion.setText(currentQuestion.getQuestionText());
        tvScore.setText("Score: " + score);
        option1.setText(currentQuestion.getOption1());
        option2.setText(currentQuestion.getOption2());
        option3.setText(currentQuestion.getOption3());
        option4.setText(currentQuestion.getOption4());
        radioGroup.clearCheck();
        btnSubmit.setVisibility(View.VISIBLE);
        btnNext.setVisibility(View.GONE);
    }
    btnNext.setOnClickListener(v ->

    {
        currentQuestionIndex++;
        answered = false;
        if (currentQuestionIndex < questionList.size()) {
            showQuestion();
        } else {
            Intent intent = new Intent(QuizActivity.this, ResultActivity.class);
            intent.putExtra("score", score);
            intent.putExtra("total", questionList.size());
            startActivity(intent);
            finish();
        }
    });
  }
private void validateAnswer() {
    if (answered) return;

    int selectedId = radioGroup.getCheckedRadioButtonId();

    if (selectedId == -1) {
        View contextView = findViewById(android.R.id.content);
        com.google.android.material.snackbar.Snackbar.make(contextView, "Please select an answer", com.google.android.material.snackbar.Snackbar.LENGTH_SHORT).show();
        return;
    }

    RadioButton selectedButton = findViewById(selectedId);
    String selectedAnswer = selectedButton.getText().toString();
    Question currentQuestion = questionList.get(currentQuestionIndex);

    View layout = findViewById(android.R.id.content);

    // US #20 - As a Child, I want to submit my selected answer, so the system evaluates my response
    // Task #22 - Compare submitted answer with the correct answer
    if (selectedAnswer.equals(currentQuestion.getCorrectAnswer())) {
        // US #20 - As a Child, I want to submit my selected answer, so the system evaluates my response
        // Task #62 - Update player score after correct answer
        score++;
        // US #24 - As a Child, I want to see immediate feedback after submitting my answer
        // Task #25 - Display correct feedback message
        com.google.android.material.snackbar.Snackbar.make(layout, "¡Correct!", com.google.android.material.snackbar.Snackbar.LENGTH_LONG)
                .setBackgroundTint(getResources().getColor(android.R.color.holo_green_dark))
                .show();
    } else {
        // US #24 - As a Child, I want to see immediate feedback after submitting my answer
        // Task #25 - Display incorrect feedback message
        // Task #63 - Display the correct answer after wrong submission
        com.google.android.material.snackbar.Snackbar.make(layout, "Wrong. The answer was: " + currentQuestion.getCorrectAnswer(), com.google.android.material.snackbar.Snackbar.LENGTH_LONG)
                .setBackgroundTint(getResources().getColor(android.R.color.holo_red_dark))
                .show();
    }

    answered = true;
    // US #20 - As a Child, I want to submit my selected answer, so the system evaluates my response
    // Task #62 - Track and update score display locally after answer evaluation
    tvScore.setText("Score: " + score);
    btnSubmit.setVisibility(View.GONE);
    btnNext.setVisibility(View.VISIBLE);
   }
}

