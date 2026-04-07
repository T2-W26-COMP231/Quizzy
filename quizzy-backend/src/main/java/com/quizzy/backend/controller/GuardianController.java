package com.quizzy.backend.controller;

import com.quizzy.backend.model.QuizSession;
import com.quizzy.backend.repository.QuizSessionRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/guardian")
@CrossOrigin(origins = "*")
public class GuardianController {

    private final QuizSessionRepository quizSessionRepository;

    public GuardianController(QuizSessionRepository quizSessionRepository) {
        this.quizSessionRepository = quizSessionRepository;
    }

    // 🔥 THIS is the endpoint your Android will call
    @GetMapping("/{userId}/latest-sessions")
    public List<QuizSession> getLatestSessions(@PathVariable Integer userId) {
        return quizSessionRepository
                .findTop15ByUserIdAndCompletionIsNotNullOrderByCompletionDesc(userId);
    }
}