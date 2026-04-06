package com.quizzy.backend.controller;

import com.quizzy.backend.model.Guardian;
import com.quizzy.backend.model.QuizSession;
import com.quizzy.backend.model.Student;
import com.quizzy.backend.model.User;
import com.quizzy.backend.repository.GuardianRepository;
import com.quizzy.backend.repository.QuizSessionRepository;
import com.quizzy.backend.repository.StudentRepository;
import com.quizzy.backend.repository.UserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/guardian")
@CrossOrigin(origins = "*")
public class GuardianController {

    private final GuardianRepository guardianRepository;
    private final StudentRepository studentRepository;
    private final UserRepository userRepository;
    private final QuizSessionRepository quizSessionRepository;

    public GuardianController(GuardianRepository guardianRepository,
                              StudentRepository studentRepository,
                              UserRepository userRepository,
                              QuizSessionRepository quizSessionRepository) {
        this.guardianRepository = guardianRepository;
        this.studentRepository = studentRepository;
        this.userRepository = userRepository;
        this.quizSessionRepository = quizSessionRepository;
    }

    @GetMapping("/{userId}/student-score")
    public ResponseEntity<?> getStudentScore(@PathVariable Integer userId,
                                             @RequestParam(defaultValue = "all") String filter) {
        try {
            if (guardianRepository.findByUserId(userId).isEmpty()) {
                Guardian newGuardian = new Guardian();
                newGuardian.setUserId(userId);
                guardianRepository.save(newGuardian);
            }

            Optional<Student> studentOpt = studentRepository.findByUserId(userId);
            Optional<User> userOpt = userRepository.findById(Long.valueOf(userId));

            if (studentOpt.isEmpty() || userOpt.isEmpty()) {
                return ResponseEntity.status(404)
                        .body(Map.of("error", "Profile data for user " + userId + " not found"));
            }

            User user = userOpt.get();

            List<QuizSession> sessions =
                    quizSessionRepository.findTop15ByUserIdAndCompletionIsNotNullOrderByCompletionDesc(userId);

            LocalDateTime now = LocalDateTime.now();

            List<QuizSession> filteredSessions = sessions.stream().filter(session -> {
                if (session.getCompletion() == null) return false;

                LocalDateTime completionDate = session.getCompletion();

                switch (filter.toLowerCase()) {
                    case "7days":
                        return completionDate.isAfter(now.minusDays(7));
                    case "30days":
                        return completionDate.isAfter(now.minusDays(30));
                    case "3months":
                        return completionDate.isAfter(now.minusMonths(3));
                    case "all":
                    default:
                        return true;
                }
            }).toList();

            int filteredTotalScore = filteredSessions.stream()
                    .mapToInt(session -> session.getFinalScore() != null ? session.getFinalScore() : 0)
                    .sum();

            return ResponseEntity.ok(Map.of(
                    "studentName", user.getUsername(),
                    "totalScore", filteredTotalScore,
                    "filter", filter,
                    "sessionsCount", filteredSessions.size()
            ));

        } catch (Exception e) {
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}