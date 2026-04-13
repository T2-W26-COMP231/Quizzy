package com.example.quizzy;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.util.ArrayList;
import java.util.List;

public class AchievementsActivity extends AppCompatActivity {

    private LinearLayout achievementsContainer;
    private SessionManager sessionManager;

    private List<Badges> allBadges = new ArrayList<>();
    private String selectedFilter = "All";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_achievements);

        achievementsContainer = findViewById(R.id.achievementsContainer);
        sessionManager = new SessionManager(this);

        sessionManager.saveSelectedDisplay("Achievements");

        findViewById(R.id.navHome).setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("start_screen", "Home");
            startActivity(intent);
            finish();
        });

        findViewById(R.id.navAwards).setOnClickListener(v -> {
            // already here
        });

        findViewById(R.id.navGuardian).setOnClickListener(v -> {
            sessionManager.saveSelectedDisplay("Latest Activity");

            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("start_screen", "Guardian");
            startActivity(intent);
            finish();
        });

        findViewById(R.id.navSettings).setOnClickListener(v -> {
            Intent intent = new Intent(this, MainActivity.class);
            intent.putExtra("start_screen", "Settings");
            startActivity(intent);
            finish();
        });

        int userId = (int) sessionManager.getUserId();

        if (userId == -1) {
            showError("User not logged in.");
            return;
        }

        QuizRepository.getUserBadges(userId, new QuizRepository.BadgeCallback() {
            @Override
            public void onSuccess(List<Badges> badges) {
                runOnUiThread(() -> {
                    allBadges = badges != null ? badges : new ArrayList<>();
                    showBadges();
                });
            }

            @Override
            public void onError(String error) {
                runOnUiThread(() -> showError(error));
            }
        });
    }

    private void showError(String message) {
        achievementsContainer.removeAllViews();

        TextView errorView = new TextView(AchievementsActivity.this);
        errorView.setText("Error: " + message);
        errorView.setTextSize(18f);
        errorView.setTextColor(Color.RED);
        errorView.setPadding(20, 40, 20, 20);

        achievementsContainer.addView(errorView);
    }

    private void showBadges() {
        achievementsContainer.removeAllViews();

        addFilterDropdown();

        List<Badges> filteredBadges = getFilteredBadges();

        if (filteredBadges.isEmpty()) {
            TextView empty = new TextView(this);
            if ("Unlocked".equals(selectedFilter)) {
                empty.setText("No unlocked badges yet.");
            } else if ("Locked".equals(selectedFilter)) {
                empty.setText("No locked badges found.");
            } else {
                empty.setText("No badges available yet.");
            }
            empty.setTextSize(22f);
            empty.setTextColor(Color.parseColor("#6E6257"));
            empty.setGravity(Gravity.CENTER);
            empty.setPadding(0, 100, 0, 0);
            achievementsContainer.addView(empty);
            return;
        }

        for (Badges badge : filteredBadges) {
            boolean unlocked = badge.isUnlocked();

            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setGravity(Gravity.CENTER_VERTICAL);
            row.setPadding(0, 28, 0, 28);
            row.setAlpha(unlocked ? 1f : 0.55f);

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
            title.setTextSize(24f);
            title.setTypeface(null, Typeface.BOLD);
            title.setTextColor(unlocked
                    ? Color.parseColor("#2F241C")
                    : Color.parseColor("#BDBDBD"));

            TextView description = new TextView(this);
            description.setText(badge.getDescription());
            description.setTextSize(18f);
            description.setPadding(0, 8, 0, 0);
            description.setTextColor(unlocked
                    ? Color.parseColor("#6E6257")
                    : Color.parseColor("#D0D0D0"));

            TextView status = new TextView(this);
            status.setText(unlocked ? "Unlocked" : "Locked");
            status.setTextSize(15f);
            status.setPadding(0, 10, 0, 0);
            status.setTextColor(unlocked
                    ? Color.parseColor("#2E7D32")
                    : Color.parseColor("#9E9E9E"));

            textLayout.addView(title);
            textLayout.addView(description);
            textLayout.addView(status);

            row.addView(textLayout);

            TextView icon = new TextView(this);
            icon.setText(unlocked ? "🏆" : "🔒");
            icon.setTextSize(30f);
            icon.setPadding(20, 0, 0, 0);
            row.addView(icon);

            achievementsContainer.addView(row);

            View divider = new View(this);
            divider.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    2
            ));
            divider.setBackgroundColor(Color.parseColor("#D8D2C8"));

            achievementsContainer.addView(divider);
        }
    }

    private void addFilterDropdown() {
        LinearLayout filterRow = new LinearLayout(this);
        filterRow.setOrientation(LinearLayout.HORIZONTAL);
        filterRow.setGravity(Gravity.END | Gravity.CENTER_VERTICAL);
        filterRow.setPadding(0, 10, 0, 30);

        TextView filterButton = new TextView(this);
        filterButton.setText("Filter: " + selectedFilter + " ▼");
        filterButton.setTextSize(16f);
        filterButton.setTypeface(null, Typeface.BOLD);
        filterButton.setTextColor(Color.parseColor("#5A4A3B"));
        filterButton.setBackgroundColor(Color.parseColor("#F4EFE7"));
        filterButton.setPadding(36, 18, 36, 18);

        filterButton.setOnClickListener(v -> {
            PopupMenu popupMenu = new PopupMenu(AchievementsActivity.this, filterButton);
            popupMenu.getMenu().add("All");
            popupMenu.getMenu().add("Unlocked");
            popupMenu.getMenu().add("Locked");

            popupMenu.setOnMenuItemClickListener(item -> {
                selectedFilter = item.getTitle().toString();
                showBadges();
                return true;
            });

            popupMenu.show();
        });

        filterRow.addView(filterButton);
        achievementsContainer.addView(filterRow);
    }

    private List<Badges> getFilteredBadges() {
        List<Badges> filtered = new ArrayList<>();

        for (Badges badge : allBadges) {
            if ("Unlocked".equals(selectedFilter) && badge.isUnlocked()) {
                filtered.add(badge);
            } else if ("Locked".equals(selectedFilter) && !badge.isUnlocked()) {
                filtered.add(badge);
            } else if ("All".equals(selectedFilter)) {
                filtered.add(badge);
            }
        }

        return filtered;
    }
}