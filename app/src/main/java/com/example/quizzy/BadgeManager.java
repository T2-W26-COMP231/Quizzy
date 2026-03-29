package com.example.quizzy;

import android.content.Context;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class BadgeManager {

    public static List<Badges> getUnlockedBadges(List<Badges> allBadges) {
        List<Badges> unlockedBadges = new ArrayList<>();

        if (allBadges == null) {
            return unlockedBadges;
        }

        for (Badges badge : allBadges) {
            if (badge.isUnlocked()) {
                unlockedBadges.add(badge);
            }
        }

        return unlockedBadges;
    }

    public static List<Badges> mergeBadgeStates(List<Badges> allBadges, List<Badges> earnedBadges) {
        List<Badges> merged = new ArrayList<>();

        if (allBadges == null) {
            return merged;
        }

        Set<Long> earnedIds = new HashSet<>();
        if (earnedBadges != null) {
            for (Badges badge : earnedBadges) {
                earnedIds.add(badge.getId());
            }
        }

        for (Badges badge : allBadges) {
            boolean unlocked = earnedIds.contains(badge.getId());

            merged.add(new Badges(
                    badge.getBadgeId(),
                    badge.getName(),
                    badge.getDescription(),
                    unlocked
            ));
        }

        return merged;
    }

    public static boolean hasBadge(List<Badges> badges, long badgeId) {
        if (badges == null) {
            return false;
        }

        for (Badges badge : badges) {
            if (badge.getId() == badgeId) {
                return true;
            }
        }

        return false;
    }

    public static int countUnlockedBadges(List<Badges> badges) {
        if (badges == null) {
            return 0;
        }

        int count = 0;
        for (Badges badge : badges) {
            if (badge.isUnlocked()) {
                count++;
            }
        }

        return count;
    }

    public static void checkAndUnlockBadges(Context context, int score, int totalQuestions, int gradeLevel) {
        int userId = 1;

        if (totalQuestions > 0) {
            unlockBadge(context, userId, 1);
        }

        if (totalQuestions > 0 && score == totalQuestions) {
            unlockBadge(context, userId, 2);
        }

        if (totalQuestions > 0) {
            double percent = (score * 100.0) / totalQuestions;
            if (percent >= 80.0) {
                unlockBadge(context, userId, 3);
            }
        }

        if (gradeLevel >= 4) {
            unlockBadge(context, userId, 4);
        }

        if (score >= 3) {
            unlockBadge(context, userId, 6);
            unlockBadge(context, userId, 8);
        }

        if (score >= 1) {
            unlockBadge(context, userId, 9);
        }
    }

    public static List<Badges> getEarnedBadges(Context context) {
        List<Badges> allBadges = BadgeCatalog.getAllBadges();
        return getUnlockedBadges(allBadges);
    }

    private static void unlockBadge(Context context, int userId, int badgeId) {
        // Placeholder for backend/API unlock later
        // Kept empty for now so project compiles cleanly
    }
}