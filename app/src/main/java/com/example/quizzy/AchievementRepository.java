package com.example.quizzy;

import java.util.ArrayList;
import java.util.List;

public class AchievementRepository {
    public static AchievementSummary latestSummary;
    public static final List<Badge> unlockedBadges = new ArrayList<>();

    public static void saveSummary(AchievementSummary summary) {
        latestSummary = summary;

        for (Badge badge : summary.getEarnedBadges()) {
            if (!containsBadge(badge.getId())) {
                unlockedBadges.add(badge);
            }
        }
    }

    private static boolean containsBadge(String badgeId) {
        for (Badge badge : unlockedBadges) {
            if (badge.getId().equals(badgeId)) {
                return true;
            }
        }
        return false;
    }
}
