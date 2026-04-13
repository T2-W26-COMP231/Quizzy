package com.quizzy.backend.controller;

import com.quizzy.backend.model.Badge;
import com.quizzy.backend.model.UserBadge;
import com.quizzy.backend.repository.BadgeRepository;
import com.quizzy.backend.repository.UserBadgeRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = "*")
public class BadgeController {

    private final BadgeRepository badgeRepository;
    private final UserBadgeRepository userBadgeRepository;

    public BadgeController(BadgeRepository badgeRepository,
                           UserBadgeRepository userBadgeRepository) {
        this.badgeRepository = badgeRepository;
        this.userBadgeRepository = userBadgeRepository;
    }

    @GetMapping("/{userId}/badges")
    public ResponseEntity<?> getUserBadges(@PathVariable Integer userId) {
        try {
            List<UserBadge> unlockedUserBadges =
                    userBadgeRepository.findByUserIdAndUnlockedTrue(userId);

            List<Integer> unlockedBadgeIds = new ArrayList<>();
            for (UserBadge userBadge : unlockedUserBadges) {
                unlockedBadgeIds.add(userBadge.getBadgeId());
            }

            List<Badge> allBadges = badgeRepository.findAll();
            List<Map<String, Object>> response = new ArrayList<>();

            for (Badge badge : allBadges) {
                Map<String, Object> badgeMap = new HashMap<>();
                badgeMap.put("badgeId", badge.getBadgeId());
                badgeMap.put("name", badge.getBadgeName());
                badgeMap.put("description", badge.getDescription());
                badgeMap.put("unlocked", unlockedBadgeIds.contains(badge.getBadgeId()));
                response.add(badgeMap);
            }

            return ResponseEntity.ok(response);

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}