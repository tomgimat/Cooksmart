package fr.tomgimat.cooksmart.data.mealdb;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface MealDbApi {
    @GET("search.php")
    Call<MealDbApiResponse> searchMeals(@Query("s") String query);

    @GET("lookup.php")
    Call<MealDbApiResponse> getMealById(@Query("i") String id);

    @GET("random.php")
    Call<MealDbApiResponse> getRandomMeal();
}
