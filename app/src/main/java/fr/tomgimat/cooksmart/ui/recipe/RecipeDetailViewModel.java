package fr.tomgimat.cooksmart.ui.recipe;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import fr.tomgimat.cooksmart.data.MealDbClient;
import fr.tomgimat.cooksmart.data.firebase.firestore.FirestoreRecipe;
import fr.tomgimat.cooksmart.data.mealdb.Meal;
import fr.tomgimat.cooksmart.data.mealdb.MealDbApiResponse;
import fr.tomgimat.cooksmart.utils.GeminiUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class RecipeDetailViewModel extends ViewModel {
    private final MutableLiveData<FirestoreRecipe> recipeLiveData = new MutableLiveData<>();

    public LiveData<FirestoreRecipe> getRecipe() {
        return recipeLiveData;
    }

    // Charge depuis Firestore OU MealDB, puis sauvegarde si besoin
    public void loadRecipe(String firestoreId, String mealId) {
        if (firestoreId != null) {
            FirebaseFirestore.getInstance().collection("recipes").document(firestoreId)
                    .get().addOnSuccessListener(doc -> {
                        if (doc.exists()) {
                            //Si duration existe
                            if (doc.contains("duration") && doc.getLong("duration") != null && doc.getLong("duration") > 0) {
                                recipeLiveData.setValue(FirestoreRecipe.fromFirestoreDoc(doc));
                            } else {
                                try {
                                    updateDurationIfMissing(firestoreId, doc, recipeLiveData::setValue);
                                } catch (JSONException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        } else {
                            //todo log d'échec
                        }
                    });
        } else if (mealId != null) {
            MealDbClient.getApi().getMealById(mealId)
                    .enqueue(new Callback<MealDbApiResponse>() {
                        @Override
                        public void onResponse(Call<MealDbApiResponse> call, Response<MealDbApiResponse> response) {
                            if (response.isSuccessful() && response.body() != null && !response.body().meals.isEmpty()) {
                                Meal meal = response.body().meals.get(0);
                                //Convertisseur de recette mealdb vers la structure Firestore
                                FirestoreRecipe recipe = convertMealToFirestoreRecipe(meal);
                                //Sauvegarde dans Firestore
                                saveMealToFirestoreIfNeeded(meal.id, recipe, recipeLiveData::setValue);
                            }
                        }

                        @Override
                        public void onFailure(Call<MealDbApiResponse> call, Throwable t) {
                        }
                    });
        }
    }

    /**
     * Convertit un Meal (API themealdb) en FirestoreRecipe
     */
    private FirestoreRecipe convertMealToFirestoreRecipe(Meal meal) {
        FirestoreRecipe recipe = new FirestoreRecipe();
        recipe.id = meal.id;
        recipe.name = meal.name;
        recipe.category = meal.category;
        recipe.area = meal.area;
        recipe.instructions = meal.instructions;
        recipe.imageUrl = meal.imageUrl;
        recipe.youtubeVideoUrl = meal.youtubeUrl;
        recipe.ingredients = new ArrayList<>();
        recipe.measures = new ArrayList<>();
        // Ingrédients / mesures (1-20)
        for (int i = 1; i <= 20; i++) {
            try {
                String ingredient = (String) meal.getClass().getField("ingredient" + i).get(meal);
                String measure = (String) meal.getClass().getField("measure" + i).get(meal);
                if (ingredient != null && !ingredient.trim().isEmpty()) {
                    recipe.ingredients.add(ingredient);
                    recipe.measures.add(measure != null ? measure : "");
                }
            } catch (Exception ignored) {
            }
        }
        //On cherche a faire match les tags entre themealdb et les tags firestore
        recipe.isVegetarian = meal.category != null && meal.category.toLowerCase().contains("vegetarian");
        recipe.isVegan = meal.tags != null && meal.tags.toLowerCase().contains("vegan");
        recipe.isGlutenFree = meal.tags != null && meal.tags.toLowerCase().contains("gluten");
        recipe.isLactoseFree = false; //Impossible à savoir sans analyse ingrédients
        recipe.isLowSalt = false;
        recipe.isLowSugar = false;
        recipe.isPescetarian = false;
        return recipe;
    }

    /**
     * Enregistre la recette dans Firestore, puis callback
     */
    private void saveMealToFirestoreIfNeeded(String mealId, FirestoreRecipe recipe, RecipeDetailFragment.OnRecipeLoaded callback) {
        FirebaseFirestore.getInstance().collection("recipes").document(mealId)
                .get().addOnSuccessListener(doc -> {
                    if (doc.exists() && doc.getLong("duration") != null && doc.contains("duration")) {
                        //Déjà présente donc on utilise la version Firestore
                        FirestoreRecipe saved = FirestoreRecipe.fromFirestoreDoc(doc);
                        callback.onLoaded(saved);
                    } else {
                        //Partie duration
                        try {
                            GeminiUtils.extractDurationFromInstructions(recipe.instructions, new okhttp3.Callback() {
                                @Override
                                public void onResponse(@NonNull okhttp3.Call call, @NonNull okhttp3.Response response) throws IOException {
                                    int duration = 0;
                                    try {
                                        JSONObject res = new JSONObject(response.body().string());
                                        //contenu dans "candidates[0].content.parts[0].text"
                                        String answer = res.getJSONArray("candidates")
                                                .getJSONObject(0)
                                                .getJSONObject("content")
                                                .getJSONArray("parts")
                                                .getJSONObject(0)
                                                .getString("text")
                                                .replaceAll("[^0-9]", "");
                                        Log.d("GEMINI", answer);
                                        if (!answer.isEmpty()) {
                                            duration = Integer.parseInt(answer);
                                        }
                                    } catch (Exception ignored) {
                                    }

                                    recipe.duration = duration;

                                    //on construit la Map data
                                    Map<String, Object> data = new HashMap<>();
                                    data.put("name", recipe.name);
                                    data.put("category", recipe.category);
                                    data.put("area", recipe.area);
                                    data.put("instructions", recipe.instructions);
                                    data.put("imageUrl", recipe.imageUrl);
                                    data.put("youtubeVideoUrl", recipe.youtubeVideoUrl);
                                    data.put("duration", duration);

                                    for (int i = 1; i <= recipe.ingredients.size(); i++) {
                                        data.put("ingredient" + i, recipe.ingredients.get(i - 1));
                                        data.put("measure" + i, recipe.measures.size() > (i - 1) ? recipe.measures.get(i - 1) : "");
                                    }
                                    data.put("isVegetarian", recipe.isVegetarian);
                                    data.put("isVegan", recipe.isVegan);
                                    data.put("isGlutenFree", recipe.isGlutenFree);
                                    data.put("isLactoseFree", recipe.isLactoseFree);
                                    data.put("isLowSalt", recipe.isLowSalt);
                                    data.put("isLowSugar", recipe.isLowSugar);
                                    data.put("isPescetarian", recipe.isPescetarian);

                                    FirebaseFirestore.getInstance().collection("recipes").document(mealId)
                                            .set(data)
                                            .addOnSuccessListener(aVoid -> callback.onLoaded(recipe));
                                }

                                @Override
                                public void onFailure(@NonNull okhttp3.Call call, @NonNull IOException e) {
                                    //Save sans durée
                                    Map<String, Object> data = new HashMap<>();
                                    data.put("name", recipe.name);
                                    data.put("category", recipe.category);
                                    data.put("area", recipe.area);
                                    data.put("instructions", recipe.instructions);
                                    data.put("imageUrl", recipe.imageUrl);
                                    data.put("youtubeVideoUrl", recipe.youtubeVideoUrl);
                                    data.put("duration", 0);
                                    for (int i = 1; i <= recipe.ingredients.size(); i++) {
                                        data.put("ingredient" + i, recipe.ingredients.get(i - 1));
                                        data.put("measure" + i, recipe.measures.size() > (i - 1) ? recipe.measures.get(i - 1) : "");
                                    }
                                    data.put("isVegetarian", recipe.isVegetarian);
                                    data.put("isVegan", recipe.isVegan);
                                    data.put("isGlutenFree", recipe.isGlutenFree);
                                    data.put("isLactoseFree", recipe.isLactoseFree);
                                    data.put("isLowSalt", recipe.isLowSalt);
                                    data.put("isLowSugar", recipe.isLowSugar);
                                    data.put("isPescetarian", recipe.isPescetarian);

                                    FirebaseFirestore.getInstance().collection("recipes").document(mealId)
                                            .set(data)
                                            .addOnSuccessListener(aVoid -> callback.onLoaded(recipe));
                                }
                            });
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                    }
                });
    }

    private void updateDurationIfMissing(String docId, DocumentSnapshot doc, RecipeDetailFragment.OnRecipeLoaded callback) throws JSONException {
        String instructions = doc.getString("instructions");
        if (instructions == null || instructions.isEmpty()) {
            callback.onLoaded(FirestoreRecipe.fromFirestoreDoc(doc));
            return;
        }

        Log.d("RecipeDetailViewModel", "Instructions: " + instructions);

        GeminiUtils.extractDurationFromInstructions(instructions, new okhttp3.Callback() {
            @Override
            public void onResponse(@NonNull okhttp3.Call call, @NonNull okhttp3.Response response) throws IOException {
                int duration = 0;
                try {
                    JSONObject res = new JSONObject(response.body().string());
                    //contenu dans "candidates[0].content.parts[0].text"
                    Log.d("RecipeDetailViewModel", "Response: " + res.toString());
                    String answer = res.getJSONArray("candidates")
                            .getJSONObject(0)
                            .getJSONObject("content")
                            .getJSONArray("parts")
                            .getJSONObject(0)
                            .getString("text")
                            .replaceAll("[^0-9]", "");
                    if (!answer.isEmpty()) {
                        duration = Integer.parseInt(answer);
                    }
                } catch (Exception ignored) {
                }

                FirebaseFirestore.getInstance().collection("recipes").document(docId)
                        .update("duration", duration)
                        .addOnSuccessListener(aVoid -> {
                            // Recharge le doc mis à jour pour callback
                            FirebaseFirestore.getInstance().collection("recipes").document(docId)
                                    .get().addOnSuccessListener(updatedDoc -> {
                                        callback.onLoaded(FirestoreRecipe.fromFirestoreDoc(updatedDoc));
                                    });
                        });
            }

            @Override
            public void onFailure(@NonNull okhttp3.Call call, @NonNull IOException e) {
                // En cas d'échec, callback sur le doc original
                callback.onLoaded(FirestoreRecipe.fromFirestoreDoc(doc));
            }
        });
    }


}

