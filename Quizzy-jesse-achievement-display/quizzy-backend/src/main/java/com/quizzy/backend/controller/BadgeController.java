package com.quizzy.backend.controller;

import com.quizzy.backend.model.BadgeResponse;
import com.quizzy.backend.service.BadgeService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/users")
@CrossOrigin(origins = "*")
public class BadgeController {

    private final BadgeService badgeService;

    public BadgeController(BadgeService badgeService) {
        this.badgeService = badgeService;
    }

    @GetMapping("/{userId}/badges")
    public List<BadgeResponse> getUserBadges(@PathVariable Long userId) {
        return badgeService.getBadgesForUser(userId);
    }
}