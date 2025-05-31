package fr.tomgimat.cooksmart.data.firebase.firestore;

import java.util.ArrayList;
import java.util.List;

import fr.tomgimat.cooksmart.ui.RecipeDisplayable;

public class FirestoreRecipe implements RecipeDisplayable {
    public String id;
    public String name;
    public String category;
    public String area;
    public String instructions;
    public String imageUrl;
    public String youtubeVideoUrl;

    public Integer duration;

    public boolean isVegetarian;
    public boolean isVegan;
    public boolean isGlutenFree;
    public boolean isLactoseFree;
    public boolean isLowSalt;
    public boolean isLowSugar;
    public boolean isPescetarian;

    public boolean isHalal;

    public List<String> ingredients;
    public List<String> measures;

    public FirestoreRecipe() {}

    @Override
    public String getName() { return name; }
    @Override
    public String getImageUrl() { return imageUrl; }

    public List<String> getIngredients() {
        return ingredients;
    }

    public static FirestoreRecipe fromFirestoreDoc(com.google.firebase.firestore.DocumentSnapshot doc) {
        FirestoreRecipe recipe = new FirestoreRecipe();
        recipe.id = doc.getId();
        recipe.name = doc.getString("name");
        recipe.category = doc.getString("category");
        recipe.area = doc.getString("area");
        recipe.instructions = doc.getString("instructions");
        recipe.imageUrl = doc.getString("imageUrl");
        recipe.youtubeVideoUrl = doc.getString("youtubeVideoUrl");
        recipe.duration = doc.getLong("duration") != null ? doc.getLong("duration").intValue() : null;
        recipe.isVegetarian = doc.getBoolean("isVegetarian") != null && doc.getBoolean("isVegetarian");
        recipe.isVegan = doc.getBoolean("isVegan") != null && doc.getBoolean("isVegan");
        recipe.isGlutenFree = doc.getBoolean("isGlutenFree") != null && doc.getBoolean("isGlutenFree");
        recipe.isLactoseFree = doc.getBoolean("isLactoseFree") != null && doc.getBoolean("isLactoseFree");
        recipe.isLowSalt = doc.getBoolean("isLowSalt") != null && doc.getBoolean("isLowSalt");
        recipe.isLowSugar = doc.getBoolean("isLowSugar") != null && doc.getBoolean("isLowSugar");
        recipe.isPescetarian = doc.getBoolean("isPescetarian") != null && doc.getBoolean("isPescetarian");
        recipe.isHalal = doc.getBoolean("isHalal") != null && doc.getBoolean("isHalal");

        recipe.ingredients = new ArrayList<>();
        recipe.measures = new ArrayList<>();
        for (int i = 1; i <= 20; i++) {
            String ingredient = doc.getString("ingredient" + i);
            String measure = doc.getString("measure" + i);
            if (ingredient != null && !ingredient.trim().isEmpty()) {
                recipe.ingredients.add(ingredient);
                recipe.measures.add((measure != null && !measure.trim().isEmpty()) ? measure : "");
            }
        }
        return recipe;
    }

}
