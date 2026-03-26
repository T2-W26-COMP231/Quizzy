package com.example.quizzy;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class ResultsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_results);

        TextView tvFinalScore = findViewById(R.id.tvFinalScore);
        TextView tvPercentage = findViewById(R.id.tvPercentage);
        TextView tvPerformanceMessage = findViewById(R.id.tvPerformanceMessage);
        LinearLayout badgeContainer = findViewById(R.id.badgeContainer);
        Button btnBackToDashboard = findViewById(R.id.btnBackToDashboard);

        int score = getIntent().getIntExtra("score", 0);
        int totalQuestions = getIntent().getIntExtra("totalQuestions", 0);

        AchievementSummary summary = BadgeProcessor.buildAchievementSummary(score, totalQuestions);
        AchievementRepository.saveSummary(summary);

        tvFinalScore.setText("Final Score: " + summary.getScore() + " / " + summary.getTotalQuestions());
        tvPercentage.setText("Percentage: " + summary.getPercentage() + "%");
        tvPerformanceMessage.setText(summary.getPerformanceMessage());

        for (Badge badge : summary.getEarnedBadges()) {
            TextView badgeView = new TextView(this);
            badgeView.setText("🏅 " + badge.getTitle() + "\n" + badge.getDescription());
            badgeView.setTextSize(18f);
            badgeView.setPadding(24, 24, 24, 24);
            badgeView.setBackgroundResource(R.drawable.rounded_score_bg);
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            );
            params.setMargins(0, 0, 0, 24);
            badgeView.setLayoutParams(params);
            badgeContainer.addView(badgeView);
        }

        btnBackToDashboard.setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        });
    }
}
