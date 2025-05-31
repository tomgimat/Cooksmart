package fr.tomgimat.cooksmart.data.mealdb;

import com.google.gson.annotations.SerializedName;
import java.util.ArrayList;
import java.util.List;

import fr.tomgimat.cooksmart.ui.RecipeDisplayable;

public class Meal implements RecipeDisplayable {
    @SerializedName("idMeal")
    public String id;

    @SerializedName("strMeal")
    public String name;

    @SerializedName("strCategory")
    public String category;

    @SerializedName("strArea")
    public String area;

    @SerializedName("strInstructions")
    public String instructions;

    @SerializedName("strMealThumb")
    public String imageUrl;

    @SerializedName("strTags")
    public String tags;

    @SerializedName("strYoutube")
    public String youtubeUrl;

    @SerializedName("strSource")
    public String sourceUrl;

    @SerializedName("strIngredient1")
    public String ingredient1;
    @SerializedName("strIngredient2")
    public String ingredient2;
    @SerializedName("strIngredient3")
    public String ingredient3;
    @SerializedName("strIngredient4")
    public String ingredient4;
    @SerializedName("strIngredient5")
    public String ingredient5;
    @SerializedName("strIngredient6")
    public String ingredient6;
    @SerializedName("strIngredient7")
    public String ingredient7;
    @SerializedName("strIngredient8")
    public String ingredient8;
    @SerializedName("strIngredient9")
    public String ingredient9;
    @SerializedName("strIngredient10")
    public String ingredient10;
    @SerializedName("strIngredient11")
    public String ingredient11;
    @SerializedName("strIngredient12")
    public String ingredient12;
    @SerializedName("strIngredient13")
    public String ingredient13;
    @SerializedName("strIngredient14")
    public String ingredient14;
    @SerializedName("strIngredient15")
    public String ingredient15;
    @SerializedName("strIngredient16")
    public String ingredient16;
    @SerializedName("strIngredient17")
    public String ingredient17;
    @SerializedName("strIngredient18")
    public String ingredient18;
    @SerializedName("strIngredient19")
    public String ingredient19;
    @SerializedName("strIngredient20")
    public String ingredient20;

    @SerializedName("strMeasure1")
    public String measure1;
    @SerializedName("strMeasure2")
    public String measure2;
    @SerializedName("strMeasure3")
    public String measure3;
    @SerializedName("strMeasure4")
    public String measure4;
    @SerializedName("strMeasure5")
    public String measure5;
    @SerializedName("strMeasure6")
    public String measure6;
    @SerializedName("strMeasure7")
    public String measure7;
    @SerializedName("strMeasure8")
    public String measure8;
    @SerializedName("strMeasure9")
    public String measure9;
    @SerializedName("strMeasure10")
    public String measure10;
    @SerializedName("strMeasure11")
    public String measure11;
    @SerializedName("strMeasure12")
    public String measure12;
    @SerializedName("strMeasure13")
    public String measure13;
    @SerializedName("strMeasure14")
    public String measure14;
    @SerializedName("strMeasure15")
    public String measure15;
    @SerializedName("strMeasure16")
    public String measure16;
    @SerializedName("strMeasure17")
    public String measure17;
    @SerializedName("strMeasure18")
    public String measure18;
    @SerializedName("strMeasure19")
    public String measure19;
    @SerializedName("strMeasure20")
    public String measure20;

    @SerializedName("strDrinkAlternate")
    public String drinkAlternate;

    @SerializedName("strCreativeCommonsConfirmed")
    public String creativeCommonsConfirmed;

    @SerializedName("dateModified")
    public String dateModified;

    // UTILITY

    /**
     * Récupérer la liste des ingrédients et de leurs quantités associées
     */
    public List<String> getIngredientsList() {
        List<String> list = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            try {
                String ingredient = (String) this.getClass().getField("ingredient" + i).get(this);
                String measure = (String) this.getClass().getField("measure" + i).get(this);
                if (ingredient != null && !ingredient.trim().isEmpty()) {
                    String desc = (measure != null && !measure.trim().isEmpty())
                            ? measure + " " + ingredient
                            : ingredient;
                    list.add(desc.trim());
                }
            } catch (Exception ignored) { }
        }
        return list;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getImageUrl() {
        return imageUrl;
    }
}
