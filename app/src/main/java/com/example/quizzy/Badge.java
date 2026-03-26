package com.example.quizzy;

public class Badge {
    private final String id;
    private final String title;
    private final String description;
    private final int minimumScore;

    public Badge(String id, String title, String description, int minimumScore) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.minimumScore = minimumScore;
    }

    public String getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public int getMinimumScore() {
        return minimumScore;
    }
}
