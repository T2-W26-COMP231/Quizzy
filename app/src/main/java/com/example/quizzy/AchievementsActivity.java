package com.example.quizzy;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.List;

public class AchievementsActivity extends AppCompatActivity {

    private LinearLayout achievementsContainer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_achievements);

        achievementsContainer = findViewById(R.id.achievementsContainer);

        int userId = 1;
        List<Badges> badges = QuizRepository.getUserBadges(userId);

        showBadges(badges);
    }

    private void showBadges(List<Badges> badges) {
        achievementsContainer.removeAllViews();

        if (badges == null || badges.isEmpty()) {
            TextView empty = new TextView(this);
            empty.setText("No badges available.");
            empty.setTextSize(18f);
            empty.setTextColor(Color.parseColor("#6E6257"));
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(0, 80, 0, 0);
            achievementsContainer.addView(empty);
            return;
        }

        int maxToShow = Math.min(badges.size(), 10);

        for (int i = 0; i < maxToShow; i++) {
            Badges badge = badges.get(i);
            boolean unlocked = badge.isUnlocked();

            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(0, 18, 0, 18);
            row.setAlpha(unlocked ? 1f : 0.45f);

            LinearLayout textLayout = new LinearLayout(this);
            textLayout.setOrientation(LinearLayout.VERTICAL);

            LinearLayout.LayoutParams textParams = new LinearLayout.LayoutParams(
                    0,
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    1f
            );
            textLayout.setLayoutParams(textParams);

            TextView title = new TextView(this);
            title.setText(badge.getName());
            title.setTextSize(18f);
            title.setTypeface(null, android.graphics.Typeface.BOLD);
            title.setTextColor(unlocked
                    ? Color.parseColor("#3E3126")
                    : Color.parseColor("#9E9E9E"));

            TextView description = new TextView(this);
            description.setText(badge.getDescription());
            description.setTextSize(14f);
            description.setTextColor(unlocked
                    ? Color.parseColor("#6E6257")
                    : Color.parseColor("#BDBDBD"));

            TextView status = new TextView(this);
            status.setText(unlocked ? "Unlocked" : "Locked");
            status.setTextSize(12f);
            status.setPadding(0, 6, 0, 0);
            status.setTextColor(unlocked
                    ? Color.parseColor("#2E7D32")
                    : Color.parseColor("#9E9E9E"));

            TextView icon = new TextView(this);
            icon.setText(unlocked ? "🏆" : "🔒");
            icon.setTextSize(22f);
            icon.setPadding(16, 0, 0, 0);

            textLayout.addView(title);
            textLayout.addView(description);
            textLayout.addView(status);

            row.addView(textLayout);
            row.addView(icon);

            achievementsContainer.addView(row);

            View divider = new View(this);
            divider.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    1
            ));
            divider.setBackgroundColor(Color.parseColor("#DDDDDD"));

            achievementsContainer.addView(divider);
        }
    }
}