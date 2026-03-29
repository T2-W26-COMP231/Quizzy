package com.example.quizzy;

import java.util.List;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Path;

public interface BadgeApiService {
    @GET("api/users/{userId}/badges")
    Call<List<Badges>> getUserBadges(@Path("userId") int userId);
}
