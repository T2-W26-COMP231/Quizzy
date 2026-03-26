package com.example.quizzy;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BadgeProcessor {

    private static final List<Badge> AVAILABLE_BADGES = Arrays.asList(
            new Badge("quiz_starter", "Quiz Starter", "Completed a quiz.", 0),
            new Badge("sharp_thinker", "Sharp Thinker", "Scored at least 3 out of 5.", 3),
            new Badge("math_master", "Math Master", "Scored 5 out of 5.", 5)
    );

    public static AchievementSummary buildAchievementSummary(int score, int totalQuestions) {
        int percentage = totalQuestions == 0 ? 0 : (score * 100) / totalQuestions;
        String performanceMessage = getPerformanceMessage(score, totalQuestions, percentage);
        List<Badge> earnedBadges = new ArrayList<>();

        for (Badge badge : AVAILABLE_BADGES) {
            if (score >= badge.getMinimumScore()) {
                earnedBadges.add(badge);
            }
        }

        return new AchievementSummary(score, totalQuestions, percentage, performanceMessage, earnedBadges);
    }

    private static String getPerformanceMessage(int score, int totalQuestions, int percentage) {
        if (totalQuestions == 0) {
            return "No quiz data available yet.";
        }
        if (score == totalQuestions) {
            return "Perfect score! Amazing job!";
        }
        if (percentage >= 80) {
            return "Great work! You are doing very well.";
        }
        if (percentage >= 60) {
            return "Nice effort! Keep practicing to earn more badges.";
        }
        return "Good try! Practice again to unlock more achievements.";
    }
}
