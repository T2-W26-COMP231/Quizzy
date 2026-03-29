package com.quizzy.backend.service;

import com.quizzy.backend.model.Badge;
import com.quizzy.backend.model.BadgeResponse;
import com.quizzy.backend.model.UserBadge;
import com.quizzy.backend.repository.BadgeRepository;
import com.quizzy.backend.repository.UserBadgeRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class BadgeService {

    private final BadgeRepository badgeRepository;
    private final UserBadgeRepository userBadgeRepository;

    public BadgeService(BadgeRepository badgeRepository, UserBadgeRepository userBadgeRepository) {
        this.badgeRepository = badgeRepository;
        this.userBadgeRepository = userBadgeRepository;
    }

    public List<BadgeResponse> getBadgesForUser(Long userId) {
        List<Badge> allBadges = badgeRepository.findAll();
        List<UserBadge> userBadges = userBadgeRepository.findByUserId(userId);

        List<BadgeResponse> response = new ArrayList<>();

        for (Badge badge : allBadges) {
            boolean unlocked = false;

            for (UserBadge userBadge : userBadges) {
                if (userBadge.getBadge().getBadgeId().equals(badge.getBadgeId())) {
                    unlocked = userBadge.isUnlocked();
                    break;
                }
            }

            response.add(new BadgeResponse(
                    badge.getBadgeId(),
                    badge.getName(),
                    badge.getDescription(),
                    unlocked
            ));
        }

        return response;
    }
}
