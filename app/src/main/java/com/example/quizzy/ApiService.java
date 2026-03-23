package com.example.quizzy;

import java.util.ArrayList;
import java.util.List;

public class ApiService {

    public static List<Question> generateQuestionsFromPrompt(String prompt, String level) {

        List<Question> questions = new ArrayList<>();

        if (level.equals("easy")) {
            questions.add(new Question("1 + 1 = ?", "2", "3", "4", "5", "2"));
            questions.add(new Question("2 + 3 = ?", "4", "5", "6", "7", "5"));
        } else if (level.equals("medium")) {
            questions.add(new Question("5 + 7 = ?", "10", "11", "12", "13", "12"));
            questions.add(new Question("9 - 3 = ?", "5", "6", "7", "8", "6"));
        } else {
            questions.add(new Question("12 × 2 = ?", "20", "22", "24", "26", "24"));
            questions.add(new Question("15 ÷ 3 = ?", "3", "4", "5", "6", "5"));
        }

        return questions;
    }
}