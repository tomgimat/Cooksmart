package fr.tomgimat.cooksmart.data.mealdb;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MealDbClient {
    private static final String BASE_URL = "https://www.themealdb.com/api/json/v1/1/";
    private static Retrofit retrofit = null;

    public static MealDbApi getApi() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit.create(MealDbApi.class);
    }
}
