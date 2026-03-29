package com.quizzy.backend.controller;

import com.quizzy.backend.model.Badge;
import com.quizzy.backend.model.Student;
import com.quizzy.backend.repository.BadgeRepository;
import com.quizzy.backend.repository.StudentRepository;
import org.json.JSONArray;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/score")
@CrossOrigin(origins = "*")
public class ScoreController {

    private final StudentRepository studentRepository;
    private final BadgeRepository badgeRepository;

    public ScoreController(StudentRepository studentRepository, 
                           BadgeRepository badgeRepository) {
        this.studentRepository = studentRepository;
        this.badgeRepository = badgeRepository;
    }

    @PostMapping("/update")
    @Transactional
    public ResponseEntity<?> updateScore(@RequestBody Map<String, Object> data) {
        try {
            Object userIdObj = data.get("user_id") != null ? data.get("user_id") : data.get("userId");
            if (userIdObj == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "user_id is required"));
            }
            
            Integer userId = Integer.valueOf(userIdObj.toString());
            
            Object pointsObj = data.get("points");
            if (pointsObj == null) {
                return ResponseEntity.badRequest().body(Map.of("error", "points is required"));
            }
            Integer pointsEarned = Integer.valueOf(pointsObj.toString());

            Student student = studentRepository.findByUserId(userId)
                    .orElseThrow(() -> new Exception("Student not found for user_id: " + userId));

            // Actualizar puntos
            int currentScore = (student.getTotalScore() != null ? student.getTotalScore() : 0);
            student.setTotalScore(currentScore + pointsEarned);
            student.setLastLogin(LocalDateTime.now());

            // Leer badges actuales
            String currentBadgesJson = student.getEarnedBadges();
            if (currentBadgesJson == null || currentBadgesJson.trim().isEmpty() || currentBadgesJson.equals("null")) {
                currentBadgesJson = "[]";
            }
            
            JSONArray earnedBadgesArray = new JSONArray(currentBadgesJson);
            List<Integer> earnedIds = new ArrayList<>();
            for (int i = 0; i < earnedBadgesArray.length(); i++) {
                earnedIds.add(earnedBadgesArray.getInt(i));
            }

            // Buscar TODOS los badges que el usuario ya debería tener por puntos
            // No solo los de tipo "POINTS", sino cualquiera cuya meta se haya alcanzado.
            List<Badge> allBadges = badgeRepository.findAll();
            List<Badge> newlyUnlocked = new ArrayList<>();

            for (Badge badge : allBadges) {
                // Lógica simplificada: si el requirement_type es POINTS y tiene suficientes puntos
                if ("POINTS".equalsIgnoreCase(badge.getRequirementType())) {
                    if (student.getTotalScore() >= badge.getRequirementValue()) {
                        if (!earnedIds.contains(badge.getBadgeId())) {
                            earnedBadgesArray.put(badge.getBadgeId());
                            newlyUnlocked.add(badge);
                            student.setBadgeId(badge.getBadgeId());
                        }
                    }
                }
                // Si es QUIZ_COUNT y enviamos puntos > 0, asumimos que completó un quiz
                else if ("QUIZ_COUNT".equalsIgnoreCase(badge.getRequirementType()) && pointsEarned > 0) {
                    // Aquí podrías llevar un contador real en la tabla students, 
                    // por ahora usamos una lógica básica para el test
                    if (!earnedIds.contains(badge.getBadgeId())) {
                        earnedBadgesArray.put(badge.getBadgeId());
                        newlyUnlocked.add(badge);
                    }
                }
            }

            // IMPORTANTE: Siempre actualizar el String del JSON si hubo cambios
            student.setEarnedBadges(earnedBadgesArray.toString());

            // Guardar cambios
            Student savedStudent = studentRepository.saveAndFlush(student);

            return ResponseEntity.ok(Map.of(
                "status", "success",
                "totalScore", savedStudent.getTotalScore(),
                "newBadges", newlyUnlocked,
                "databaseEarnedBadges", savedStudent.getEarnedBadges()
            ));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(500).body(Map.of("error", e.getMessage()));
        }
    }
}
