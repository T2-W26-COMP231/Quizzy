package com.quizzy.backend.controller;

import com.quizzy.backend.model.LevelInstruction;
import com.quizzy.backend.repository.LevelInstructionRepository;
import com.quizzy.backend.service.AIService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = "*")
public class LevelInstructionController {

    private final LevelInstructionRepository levelInstructionRepository;
    private final AIService aiService;

    public LevelInstructionController(LevelInstructionRepository levelInstructionRepository,
                                      AIService aiService) {
        this.levelInstructionRepository = levelInstructionRepository;
        this.aiService = aiService;
    }

    @GetMapping("/instructions/{gradeLevel}")
    public LevelInstruction getInstructionByGradeLevel(@PathVariable Integer gradeLevel) {
        return levelInstructionRepository.findByGradeLevel(gradeLevel)
                .orElseThrow(() -> new RuntimeException("No instruction found for grade level: " + gradeLevel));
    }

    @GetMapping("/generate-quiz/{gradeLevel}")
    public String generateQuiz(@PathVariable Integer gradeLevel) throws Exception {
        LevelInstruction instruction = levelInstructionRepository.findByGradeLevel(gradeLevel)
                .orElseThrow(() -> new RuntimeException("No instruction found for grade level: " + gradeLevel));

        String prompt = instruction.getPromptTemplate() + " " + instruction.getInstructionText();
        return aiService.generateQuestionsJson(prompt);
    }
}