import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;


public class QuizActivity {

    private TextView tvQuestionNumber, tvQuestion, tvScore;
    private RadioGroup radioGroup;
    private RadioButton option1, option2, option3, option4;
    private Button btnSubmit, btnNext;

    private List<Question> questionList;
    private int currentQuestionIndex = 0;
    private int score = 0;
    private boolean answered = false;

    // US #16 - As a child, I want to see a quiz question with multiple possible answers, FILE: QuizActivity.Java
    // Task #17 - Retrieve quiz questions from local storage
    // US #27 - As a Child, I want to move to the next question so I can continue the quiz
    // Task #29 - Retrieve and prepare the next quiz question data from local storage
    questionList = QuizRepository.currentQuizQuestions;

        if (questionList == null || questionList.isEmpty()) {
        Toast.makeText(this, "No questions available", Toast.LENGTH_SHORT).show();
        finish();
        return;
    }
    // US #16 - As a child, I want to see a quiz question with multiple possible answers
    // Task #17 - Display the quiz question text in the question container
    // US #27 - As a Child, I want to move to the next question so I can continue the quiz
    // Task #30 - Update question number and question text for the next question
        tvQuestionNumber.setText("Question " + (currentQuestionIndex + 1) + " of " + questionList.size());
        tvQuestion.setText(currentQuestion.getQuestionText());
    // US #16 - As a child, I want to see a quiz question with multiple possible answers
    // Task #18 - Retrieve answer options and populate the multiple-choice buttons
    // US #27 - As a Child, I want to move to the next question so I can continue the quiz
    // Task #30 - Update the four answer options for the next question
        option1.setText(currentQuestion.getOption1());
        option2.setText(currentQuestion.getOption2());
        option3.setText(currentQuestion.getOption3());
        option4.setText(currentQuestion.getOption4());

    // Task #19 - Implement answer selection logic and store the selected answer
    private void validateAnswer() {

        if (answered) {
            return;
        }

        int selectedId = radioGroup.getCheckedRadioButtonId();

        if (selectedId == -1) {
            View contextView = findViewById(android.R.id.content);
            com.google.android.material.snackbar.Snackbar.make(contextView, "Please select an answer", com.google.android.material.snackbar.Snackbar.LENGTH_SHORT).show();
            return;
        }
    }
}


