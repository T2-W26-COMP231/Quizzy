package com.example.quizzy;

import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResultActivity extends AppCompatActivity {

    private TextView tvFinalScore;
    private TextView tvResultMessage;
    private LinearLayout achievementsContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        tvFinalScore = findViewById(R.id.tvFinalScore);
        tvResultMessage = findViewById(R.id.tvResultMessage);
        achievementsContainer = findViewById(R.id.achievementsContainer);

        int score = getIntent().getIntExtra("score", 0);
        int totalQuestions = getIntent().getIntExtra("totalQuestions", 0);

        tvFinalScore.setText(String.format(Locale.getDefault(),
                "Final Score: %d / %d", score, totalQuestions));

        tvResultMessage.setText(AchievementProcessor.getResultMessage(score, totalQuestions));

        loadBadgesFromBackend();
    }

    private void loadBadgesFromBackend() {
        BadgeApiService api = RetrofitClient.getInstance().create(BadgeApiService.class);
        int userId = 1;

        api.getUserBadges(userId).enqueue(new Callback<List<Badges>>() {
            @Override
            public void onResponse(Call<List<Badges>> call, Response<List<Badges>> response) {
                if (response.isSuccessful() && response.body() != null) {
                    List<Badges> unlocked = new ArrayList<>();

                    for (Badges badge : response.body()) {
                        if (badge.isUnlocked()) {
                            unlocked.add(badge);
                        }
                    }

                    displayBadges(unlocked);
                }
            }

            @Override
            public void onFailure(Call<List<Badges>> call, Throwable t) {
                t.printStackTrace();
            }
        });
    }

    private void displayBadges(List<Badges> badges) {
        achievementsContainer.removeAllViews();

        for (Badges badge : badges) {
            TextView badgeView = new TextView(this);
            badgeView.setText("🏆 " + badge.getName());
            badgeView.setTextSize(18f);
            badgeView.setPadding(0, 12, 0, 12);
            achievementsContainer.addView(badgeView);
        }
    }
}