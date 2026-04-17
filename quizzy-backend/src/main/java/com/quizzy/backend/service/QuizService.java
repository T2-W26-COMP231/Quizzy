package com.quizzy.backend.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.quizzy.backend.model.QuestionDto;
import com.quizzy.backend.model.QuizResponse;
import com.quizzy.backend.model.QuizSession;
import com.quizzy.backend.repository.QuizSessionRepository;
import org.springframework.stereotype.Service;

@Service
public class QuizService {

    private final AIService aiService;
    private final QuizSessionRepository sessionRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public QuizService(AIService aiService, QuizSessionRepository sessionRepository) {
        this.aiService = aiService;
        this.sessionRepository = sessionRepository;
    }

    public QuizResponse generateAndStore(String prompt, Integer userId) throws Exception {
        // 1. Llamar a la AI
        String rawJson = aiService.generateQuestionsJson(prompt);

        // 2. Parsear el JSON a QuizResponse
        QuizResponse quizResponse = objectMapper.readValue(rawJson, QuizResponse.class);

        // --- Log generated questions and answers to console ---
        System.out.println("\n=== GENERATED QUIZ QUESTIONS ===");
        if (quizResponse.getQuestions() != null) {
            for (int i = 0; i < quizResponse.getQuestions().size(); i++) {
                QuestionDto q = quizResponse.getQuestions().get(i);
                System.out.println("Question " + (i + 1) + ": " + q.getQuestionText());
                System.out.println("  1) " + q.getOption1());
                System.out.println("  2) " + q.getOption2());
                System.out.println("  3) " + q.getOption3());
                System.out.println("  4) " + q.getOption4());
                System.out.println("  [CORRECT]: " + q.getCorrectAnswer());
                System.out.println();
            }
        }
        System.out.println("================================\n");

        // 3. Guardar en DB (la sesión actual)
        QuizSession session = new QuizSession();
        session.setQuestionsJson(rawJson);
        session.setUserId(userId);
        QuizSession saved = sessionRepository.save(session);

        // 4. Incluir el sessionId en la respuesta
        quizResponse.setSessionId(saved.getId());

        return quizResponse;
    }
}
