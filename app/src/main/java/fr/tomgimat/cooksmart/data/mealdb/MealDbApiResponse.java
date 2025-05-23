package fr.tomgimat.cooksmart.data.mealdb;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class MealDbApiResponse {
    @SerializedName("meals")
    public List<Meal> meals;
}

