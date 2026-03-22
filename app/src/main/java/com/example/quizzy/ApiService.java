package com.example.quizzy;

import java.util.ArrayList;
import java.util.List;

public class ApiService {

    public static List<Question> generateQuestionsFromPrompt(String prompt, String level) {

        List<Question> questions = new ArrayList<>();

        if (level.equals("easy")) {
            questions.add(new Question("1 + 1 = ?", "2", "3", "4", "5", "2"));
            questions.add(new Question("2 + 3 = ?", "4", "5", "6", "7", "5"));
            questions.add(new Question("3 + 4 = ?", "5", "6", "7", "8", "7"));
            questions.add(new Question("10 - 4 = ?", "4", "5", "6", "7", "6"));
            questions.add(new Question("2 × 3 = ?", "4", "5", "6", "7", "6"));
        } else if (level.equals("medium")) {
            questions.add(new Question("5 + 7 = ?", "10", "11", "12", "13", "12"));
            questions.add(new Question("9 - 3 = ?", "5", "6", "7", "8", "6"));
            questions.add(new Question("4 × 4 = ?", "12", "14", "16", "18", "16"));
            questions.add(new Question("20 ÷ 4 = ?", "3", "4", "5", "6", "5"));
            questions.add(new Question("13 + 8 = ?", "19", "20", "21", "22", "21"));
        } else {
            questions.add(new Question("12 × 2 = ?", "20", "22", "24", "26", "24"));
            questions.add(new Question("15 ÷ 3 = ?", "3", "4", "5", "6", "5"));
            questions.add(new Question("7 × 8 = ?", "54", "56", "58", "60", "56"));
            questions.add(new Question("144 ÷ 12 = ?", "10", "11", "12", "13", "12"));
            questions.add(new Question("25 × 4 = ?", "90", "95", "100", "105", "100"));
        }

        return questions;
    }
}