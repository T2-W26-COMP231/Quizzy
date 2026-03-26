package com.example.quizzy;

import java.util.List;

public class AchievementSummary {
    private final int score;
    private final int totalQuestions;
    private final int percentage;
    private final String performanceMessage;
    private final List<Badge> earnedBadges;

    public AchievementSummary(int score, int totalQuestions, int percentage, String performanceMessage, List<Badge> earnedBadges) {
        this.score = score;
        this.totalQuestions = totalQuestions;
        this.percentage = percentage;
        this.performanceMessage = performanceMessage;
        this.earnedBadges = earnedBadges;
    }

    public int getScore() {
        return score;
    }

    public int getTotalQuestions() {
        return totalQuestions;
    }

    public int getPercentage() {
        return percentage;
    }

    public String getPerformanceMessage() {
        return performanceMessage;
    }

    public List<Badge> getEarnedBadges() {
        return earnedBadges;
    }
}
