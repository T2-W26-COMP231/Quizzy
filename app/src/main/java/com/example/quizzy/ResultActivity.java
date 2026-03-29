package com.example.quizzy;

import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.content.ContextCompat;

import com.example.quizzy.network.NetworkClient;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ResultActivity extends AppCompatActivity {
//TODO: Check future results enhancements
    private LinearLayout achievementsContainer;
    private SessionManager sessionManager;
    private TextView tvResultTitle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        achievementsContainer = findViewById(R.id.achievementsContainer);
        tvResultTitle = findViewById(R.id.tvResultTitle);
        sessionManager = new SessionManager(this);

        int score = getIntent().getIntExtra("score", 0);
        int totalQuestions = getIntent().getIntExtra("totalQuestions", 0);

        tvFinalScore.setText(String.format(Locale.getDefault(),
                "Final Score: %d / %d", score, totalQuestions));

        tvResultMessage.setText(AchievementProcessor.getResultMessage(score, totalQuestions));

        // Sync score and badges with backend
        syncScoreAndBadges(score);

        // Display achievements in UI
        List<Badges> allBadges = BadgeCatalog.getAllBadges();
        List<Badges> earnedBadges = BadgeManager.getEarnedBadges(this);

        List<AchievementDisplayItem> displayItems =
                AchievementProcessor.prepareAchievementsForDisplay(
                        allBadges,
                        earnedBadges,
                        score,
                        totalQuestions
                );

        showAchievements(achievementsContainer, displayItems);

        btnBackToDashboard.setOnClickListener(v -> {
            Intent intent = new Intent(ResultActivity.this, MainActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
            startActivity(intent);
            finish();
        });
    }

    private void syncScoreAndFetchNewBadges(int correctAnswers) {
        long userId = sessionManager.getUserId();
        long sessionId = QuizRepository.currentSessionId;

        if (userId == -1L) {
            showError("User not logged in.");
            return;
        }

        try {
            JSONObject body = new JSONObject();
            body.put("user_id", userId);
            body.put("points", correctAnswers);

            if (sessionId != -1L) {
                body.put("session_id", sessionId);
            }

            new Thread(() -> {
                try {
                    NetworkClient.postSync("/score/update", body);
                    Log.d("SYNC", "Score synced successfully for userId: " + userId + " sessionId: " + sessionId);
                } catch (Exception e) {
                    Log.e("SYNC", "Error syncing score: " + e.getMessage());
                }
                
                runOnUiThread(() -> {
                    if (newlyEarned.isEmpty()) {
                        tvResultTitle.setText("Quiz Complete!");
                        showNoNewBadges();
                    } else {
                        tvResultTitle.setText("New Achievements! 🎉");
                        displayNewBadges(newlyEarned);
                    }
                });

            } catch (Exception e) {
                Log.e("SYNC", "Error: " + e.getMessage());
                runOnUiThread(() -> showError("Failed to sync: " + e.getMessage()));
            }
        }).start();
    }

    private void displayNewBadges(List<Badges> badges) {
        achievementsContainer.removeAllViews();

        for (int i = 0; i < badges.size(); i++) {
            Badges badge = badges.get(i);
            View badgeView = createBadgeCard(badge);
            
            // Basic entry animation
            badgeView.setAlpha(0f);
            badgeView.setTranslationY(50f);
            achievementsContainer.addView(badgeView);
            
            badgeView.animate()
                    .alpha(1f)
                    .translationY(0f)
                    .setDuration(500)
                    .setStartDelay(i * 200L)
                    .setInterpolator(new AccelerateDecelerateInterpolator())
                    .start();
        }
    }

    private View createBadgeCard(Badges badge) {
        CardView cardView = new CardView(this);
        LinearLayout.LayoutParams cardParams = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
        );
        cardParams.setMargins(0, 16, 0, 16);
        cardView.setLayoutParams(cardParams);
        cardView.setRadius(32f);
        cardView.setCardElevation(12f);
        cardView.setUseCompatPadding(true);
        
        // Gradient Background for the card
        GradientDrawable gradient = new GradientDrawable(
                GradientDrawable.Orientation.TL_BR,
                new int[] { Color.parseColor("#FFFFFF"), Color.parseColor("#F5F0FF") }
        );
        gradient.setCornerRadius(32f);
        cardView.setBackground(gradient);

        // FrameLayout to allow overlapping "NEW" tag
        FrameLayout rootLayout = new FrameLayout(this);
        
        // Content Layout
        LinearLayout contentLayout = new LinearLayout(this);
        contentLayout.setOrientation(LinearLayout.HORIZONTAL);
        contentLayout.setGravity(Gravity.CENTER_VERTICAL);
        contentLayout.setPadding(40, 48, 40, 48);

        // Badge Icon (Trophy)
        TextView iconView = new TextView(this);
        iconView.setText("🏆");
        iconView.setTextSize(40f);
        iconView.setPadding(0, 0, 32, 0);

        // Text Info
        LinearLayout textLayout = new LinearLayout(this);
        textLayout.setOrientation(LinearLayout.VERTICAL);
        
        TextView tvName = new TextView(this);
        tvName.setText(badge.getName());
        tvName.setTextSize(22f);
        tvName.setTextColor(Color.parseColor("#4A148C")); // Deep Purple
        tvName.setTypeface(null, Typeface.BOLD);

        TextView tvDesc = new TextView(this);
        tvDesc.setText(badge.getDescription());
        tvDesc.setTextSize(16f);
        tvDesc.setTextColor(Color.parseColor("#6A1B9A"));
        tvDesc.setPadding(0, 8, 0, 0);

        textLayout.addView(tvName);
        textLayout.addView(tvDesc);

        contentLayout.addView(iconView);
        contentLayout.addView(textLayout);
        
        rootLayout.addView(contentLayout);

        // "NEW" Mark Circle
        TextView newMark = new TextView(this);
        newMark.setText("NEW");
        newMark.setTextSize(10f);
        newMark.setTextColor(Color.WHITE);
        newMark.setTypeface(null, Typeface.BOLD);
        newMark.setGravity(Gravity.CENTER);
        
        int size = 80; // px
        FrameLayout.LayoutParams newParams = new FrameLayout.LayoutParams(size, size);
        newParams.gravity = Gravity.TOP | Gravity.END;
        newParams.setMargins(0, 16, 16, 0);
        newMark.setLayoutParams(newParams);
        
        GradientDrawable circle = new GradientDrawable();
        circle.setShape(GradientDrawable.OVAL);
        circle.setColor(Color.parseColor("#FF4081")); // Pink accent
        newMark.setBackground(circle);

        rootLayout.addView(newMark);
        
        cardView.addView(rootLayout);
        return cardView;
    }

    private void showNoNewBadges() {
        achievementsContainer.removeAllViews();
        TextView message = new TextView(this);
        message.setText("Great effort! You didn't unlock any new badges this time, but keep practicing to reach the next milestone.");
        message.setTextSize(18f);
        message.setTextColor(Color.parseColor("#7B1FA2"));
        message.setGravity(Gravity.CENTER);
        message.setPadding(40, 100, 40, 0);
        message.setLineSpacing(0, 1.2f);
        achievementsContainer.addView(message);
    }

    private void showError(String message) {
        achievementsContainer.removeAllViews();
        TextView errorView = new TextView(this);
        errorView.setText("Oops! " + message);
        errorView.setTextColor(Color.RED);
        errorView.setGravity(Gravity.CENTER);
        errorView.setPadding(0, 50, 0, 0);
        achievementsContainer.addView(errorView);
    }
}