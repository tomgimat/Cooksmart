package fr.tomgimat.cooksmart.ui.home;

import android.graphics.Bitmap;
import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import fr.tomgimat.cooksmart.data.mealdb.MealDbClient;
import fr.tomgimat.cooksmart.data.firebase.firestore.FirestoreRecipe;
import fr.tomgimat.cooksmart.data.firebase.firestore.FirestoreVideo;
import fr.tomgimat.cooksmart.data.firebase.firestore.UserIngredients;
import fr.tomgimat.cooksmart.data.mealdb.Meal;
import fr.tomgimat.cooksmart.data.mealdb.MealDbApiResponse;
import fr.tomgimat.cooksmart.utils.GeminiUtils;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class HomeViewModel extends ViewModel {

    private static final String TAG = "HomeViewModel";
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private MutableLiveData<String> welcomeText = new MutableLiveData<>();
    private MutableLiveData<List<Meal>> randomMeals = new MutableLiveData<>();
    // Suggestions list
    private final MutableLiveData<List<FirestoreRecipe>> suggestedRecipes = new MutableLiveData<>();
    private final MutableLiveData<List<FirestoreRecipe>> compatibleRecipes = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<List<FirestoreVideo>> videos = new MutableLiveData<>(new ArrayList<>());
    private final MutableLiveData<String> scanResult = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isScanning = new MutableLiveData<>(false);
    private List<String> availableIngredients = new ArrayList<>();

    public LiveData<List<Meal>> getRandomMeals() {
        return randomMeals;
    }

    public LiveData<List<FirestoreRecipe>> getSuggestedRecipes() {
        return suggestedRecipes;
    }

    public LiveData<List<FirestoreVideo>> getVideos() {
        return videos;
    }

    public LiveData<List<FirestoreRecipe>> getCompatibleRecipes() {
        return compatibleRecipes;
    }

    public HomeViewModel() {
        loadAvailableIngredients();
    }

    public LiveData<String> getText() {
        return welcomeText;
    }

    /**
     * Met à jour le texte de bienvenue selon la connexion
     */
    public void updateWelcomeText() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user != null) {

            FirebaseFirestore db = FirebaseFirestore.getInstance();
            String uid = user.getUid();
            DocumentReference userProfile = db.collection("users").document(uid);
            userProfile.addSnapshotListener((snapshot, err) -> {
                if (snapshot != null && snapshot.exists()) {
                    String pseudo = snapshot.getString("pseudo");
                    welcomeText.setValue("Welcome back, " + pseudo + " !");
                }
            });
        } else {
            welcomeText.setValue("Welcome to CookSmart!");
        }
    }

    /**
     * Récupère des recettes aléatoires
     */
    public void fetchRandomMealsIfNeeded() {
        //Check si déjà fetch sinon fetch
        if (randomMeals.getValue() != null && !randomMeals.getValue().isEmpty()) {
            return;
        }

        List<Meal> result = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            MealDbClient.getApi().getRandomMeal().enqueue(new Callback<MealDbApiResponse>() {
                @Override
                public void onResponse(Call<MealDbApiResponse> call, Response<MealDbApiResponse> response) {
                    if (response.isSuccessful() && response.body() != null && response.body().meals != null) {
                        Meal meal = response.body().meals.get(0);
                        result.add(meal);
                        if (result.size() == 10) {
                            randomMeals.setValue(result);
                        }
                    }
                }

                @Override
                public void onFailure(Call<MealDbApiResponse> call, Throwable t) {
                }
            });
        }
    }

    /**
     * Charge des suggestions de plat en adéquation avec les préférences de l'utilisateur
     * notamment grâce à l'appel à la méthode fetchCompatibleRecipes()
     */
    public void loadSuggestionsForCurrentUser() {
        //Check si déjà fetch sinon fetch
        if (suggestedRecipes.getValue() != null && !suggestedRecipes.getValue().isEmpty()) {
            return;
        }

        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            suggestedRecipes.setValue(new ArrayList<>());
            return;
        }
        FirebaseFirestore.getInstance().collection("users").document(user.getUid())
                .get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Map<String, Boolean> prefs = (Map<String, Boolean>) doc.get("preferences");
                        //Appel à l'API Firestore pour récupérer les recettes compatibles
                        fetchCompatibleRecipes(prefs);
                    }
                });
    }

    /**
     * Récupère des recettes compatibles avec les préférences de l'utilisateur
     * @param prefs
     */
    private void fetchCompatibleRecipes(Map<String, Boolean> prefs) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        //Commence la requête sur la base
        Query query = db.collection("recipes");

        if (Boolean.TRUE.equals(prefs.getOrDefault("vegetarian", false))) {
            query = query.whereEqualTo("isVegetarian", true);
        }
        if (Boolean.TRUE.equals(prefs.getOrDefault("glutenFree", false))) {
            query = query.whereEqualTo("isGlutenFree", true);
        }
        if (Boolean.TRUE.equals(prefs.getOrDefault("lowSalt", false))) {
            query = query.whereEqualTo("isLowSalt", true);
        }
        if (Boolean.TRUE.equals(prefs.getOrDefault("lactoseFree", false))) {
            query = query.whereEqualTo("isLactoseFree", true);
        }
        if (Boolean.TRUE.equals(prefs.getOrDefault("lowSugar", false))) {
            query = query.whereEqualTo("isLowSugar", true);
        }
        if (Boolean.TRUE.equals(prefs.getOrDefault("vegan", false))) {
            query = query.whereEqualTo("isVegan", true);
        }
        if (Boolean.TRUE.equals(prefs.getOrDefault("pescetarian", false))) {
            query = query.whereEqualTo("isPescetarian", true);
        }
        if (Boolean.TRUE.equals(prefs.getOrDefault("halal", false))) {
            query = query.whereEqualTo("isHalal", true);
        }

        query.limit(40)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<FirestoreRecipe> results = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
                        FirestoreRecipe recipe = FirestoreRecipe.fromFirestoreDoc(doc);
                        results.add(recipe);
                    }
                    // Mélange la liste des résultats
                    Collections.shuffle(results);
                    // Ne garde que les 20 premiers éléments
                    if (results.size() > 20) {
                        results = results.subList(0, 20);
                    }
                    suggestedRecipes.setValue(results);
                })
                .addOnFailureListener(e -> {
                    Log.d("Firestore", "Error getting documents: ", e);
                });
    }

    public void updateSuggestions() {
        // Réinitialiser (ou vider) la liste des recettes suggérées (par exemple en postant une liste vide) :
        suggestedRecipes.postValue(new ArrayList<FirestoreRecipe>());
        // Puis relancer la requête (par exemple en appelant loadSuggestionsForCurrentUser) :
        loadSuggestionsForCurrentUser();
        }
        

    public void loadVideos() {
        FirebaseFirestore.getInstance()
            .collection("videos")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<FirestoreVideo> videoList = new ArrayList<>();
                for (DocumentSnapshot doc : queryDocumentSnapshots) {
                    videoList.add(FirestoreVideo.fromFirestoreDoc(doc));
                }
                videos.setValue(videoList);
            })
            .addOnFailureListener(e -> {
                // Gérer l'erreur
            });
    }

    private void loadAvailableIngredients() {
        db.collection("ingredients")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                availableIngredients.clear();
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    String ingredientName = document.getString("name");
                    if (ingredientName != null) {
                        availableIngredients.add(ingredientName.toLowerCase());
                    }
                }
                Log.d(TAG, "Ingrédients chargés : " + availableIngredients.size());
            })
            .addOnFailureListener(e -> Log.e(TAG, "Erreur lors du chargement des ingrédients", e));
    }

    //////////////////////////////////////////////////
    ////////////////////////////////////////////////// SCAN
    ////////////////////////////////////////////////


    /**
     * Effectue la reconnaissance d'ingrédient
     * @param image
     */
    public void scanIngredient(Bitmap image) {
        if (availableIngredients.isEmpty()) {
            scanResult.setValue("Erreur : Liste d'ingrédients non chargée");
            return;
        }

        isScanning.setValue(true);
        GeminiUtils.recognizeIngredient(image, availableIngredients, new okhttp3.Callback() {
            @Override
            public void onFailure(okhttp3.Call call, IOException e) {
                Log.e(TAG, "Erreur lors de la reconnaissance d'ingrédient", e);
                scanResult.postValue("Erreur de connexion");
                isScanning.postValue(false);
            }

            @Override
            public void onResponse(okhttp3.Call call, okhttp3.Response response) throws IOException {
                try {
                    String responseBody = response.body().string();
                    JSONObject jsonResponse = new JSONObject(responseBody);
                      JSONArray candidates = jsonResponse.getJSONArray("candidates");
                    if (candidates.length() > 0) {
                        JSONObject candidate = candidates.getJSONObject(0);
                        JSONObject content = candidate.getJSONObject("content");
                        JSONArray parts = content.getJSONArray("parts");
                        String recognizedIngredient = parts.getJSONObject(0).getString("text").trim().toLowerCase();

                        if ("non_trouve".equals(recognizedIngredient)) {
                            scanResult.postValue("Aucun ingrédient correspondant trouvé dans l'application");
                        } else {
                            // Vérifier si l'ingrédient est dans la liste
                            String matchedIngredient = findBestMatch(recognizedIngredient);
                            if (matchedIngredient != null) {
                                addIngredientToUserSelection(matchedIngredient);
                                scanResult.postValue("Ingrédient reconnu : " + matchedIngredient);
                            } else {
                                scanResult.postValue("Aucun ingrédient correspondant trouvé dans l'application");
                            }
                        }
                    }
                } catch (JSONException e) {
                    Log.e(TAG, "Erreur lors du parsing de la réponse", e);
                    scanResult.postValue("Erreur lors du traitement de l'image");
                } finally {
                    isScanning.postValue(false);
                }
            }
        });
    }

    /**
     * Recherche le meilleur ingrédient correspondant dans la liste
     * @param recognizedIngredient
     * @return
     */
    private String findBestMatch(String recognizedIngredient) {
        // Recherche exacte d'abord
        if (availableIngredients.contains(recognizedIngredient)) {
            return recognizedIngredient;
        }

        // Recherche de similarité (à améliorer selon les besoins)
        for (String ingredient : availableIngredients) {
            if (ingredient.contains(recognizedIngredient) || recognizedIngredient.contains(ingredient)) {
                return ingredient;
            }
        }
        return null;
    }

    /**
     * Ajoute un ingrédient à la liste des ingrédients de l'utilisateur
     * @param ingredient
     */
    private void addIngredientToUserSelection(String ingredient) {
        String userId = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();

        db.collection("ingredients")
            .whereEqualTo("name", ingredient)
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                if (!queryDocumentSnapshots.isEmpty()) {
                    String ingredientId = queryDocumentSnapshots.getDocuments().get(0).getId();
                    
                    //add l'id à la liste des ingrédients de l'utilisateur
                    db.collection("user_ingredients")
                        .document(userId)
                        .get()
                        .addOnSuccessListener(document -> {
                            List<String> ingredientIds = new ArrayList<>();
                            if (document.exists()) {
                                List<String> existingIds = (List<String>) document.get("ingredientIds");
                                if (existingIds != null) {
                                    ingredientIds.addAll(existingIds);
                                }
                            }

                            if (!ingredientIds.contains(ingredientId)) {
                                ingredientIds.add(ingredientId);
                                
                                //savela liste mise à jour
                                db.collection("user_ingredients")
                                    .document(userId)
                                    .set(new UserIngredients(userId, ingredientIds))
                                    .addOnSuccessListener(aVoid -> {
                                        Log.d(TAG, "Ingrédient ajouté avec succès");
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "Erreur lors de l'ajout de l'ingrédient", e);
                                    });
                            }
                        });
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Erreur lors de la recherche de l'ingrédient", e);
            });
    }

    public LiveData<String> getScanResult() {
        return scanResult;
    }

    public LiveData<Boolean> getIsScanning() {
        return isScanning;
    }

    //////////////////////////////////////////////////
    //////////////////////////////////////////////////
    ////////////////////////////////////////////////

    /**
     * Récupère les recettes compatibles avec les ingrédients de l'utilisateur
     */
    public void loadCompatibleRecipes() {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user == null) {
            compatibleRecipes.setValue(new ArrayList<>());
            return;
        }

        // On récupère d'abord les ingrédients de l'utilisateur
        db.collection("user_ingredients")
            .document(user.getUid())
            .get()
            .addOnSuccessListener(document -> {
                if (document.exists()) {
                    List<String> userIngredientIds = (List<String>) document.get("ingredientIds");
                    if (userIngredientIds != null && !userIngredientIds.isEmpty()) {
                        // On récupere les noms des ingrédients possédés
                        db.collection("ingredients")
                            .whereIn("__name__", userIngredientIds)
                            .get()
                            .addOnSuccessListener(ingredientDocs -> {
                                List<String> ownedIngredientNames = new ArrayList<>();
                                for (DocumentSnapshot doc : ingredientDocs) {
                                    String name = doc.getString("name");
                                    if (name != null) {
                                        ownedIngredientNames.add(name.toLowerCase());
                                    }
                                }

                                // Récupérer toutes les recettes
                                db.collection("recipes")
                                    .get()
                                    .addOnSuccessListener(queryDocumentSnapshots -> {
                                        List<FirestoreRecipe> compatibleList = new ArrayList<>();
                                        for (DocumentSnapshot doc : queryDocumentSnapshots) {
                                            FirestoreRecipe recipe = FirestoreRecipe.fromFirestoreDoc(doc);
                                            List<String> recipeIngredients = recipe.getIngredients();

                                            //Verification si l'user a tous les ingrédients de la recette
                                            boolean hasAllIngredients = true;
                                            if (recipeIngredients != null) {
                                                for (String ingredient : recipeIngredients) {
                                                    if (!ownedIngredientNames.contains(ingredient.toLowerCase())) {
                                                        hasAllIngredients = false;
                                                        break;
                                                    }
                                                }
                                            }
                                            
                                            if (hasAllIngredients) {
                                                compatibleList.add(recipe);
                                            }
                                        }
                                        compatibleRecipes.setValue(compatibleList);
                                    })
                                    .addOnFailureListener(e -> {
                                        Log.e(TAG, "Erreur lors de la récupération des recettes compatibles", e);
                                        compatibleRecipes.setValue(new ArrayList<>());
                                    });
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Erreur lors de la récupération des noms d'ingrédients", e);
                                compatibleRecipes.setValue(new ArrayList<>());
                            });
                    } else {
                        compatibleRecipes.setValue(new ArrayList<>());
                    }
                } else {
                    compatibleRecipes.setValue(new ArrayList<>());
                }
            })
            .addOnFailureListener(e -> {
                Log.e(TAG, "Erreur lors de la récupération des ingrédients de l'utilisateur", e);
                compatibleRecipes.setValue(new ArrayList<>());
            });
    }

    /**
     * Nettoie la base de données des ingrédients en supprimant les doublons
     * et en mettant à jour les références dans les recettes
     */
    public void cleanDuplicateIngredients() {
        db.collection("ingredients")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                // Map pour stocker les ingrédients uniques (nom en minuscules -> ID)
                Map<String, String> uniqueIngredients = new HashMap<>();
                // Liste des documents à supprimer (doublons)
                List<DocumentSnapshot> duplicatesToDelete = new ArrayList<>();
                // Map pour stocker les mises à jour à faire dans les recettes (ancien ID -> nouveau ID)
                Map<String, String> ingredientIdUpdates = new HashMap<>();

                // Première passe : identifier les doublons
                for (DocumentSnapshot doc : queryDocumentSnapshots) {
                    String name = doc.getString("name");
                    if (name != null) {
                        String normalizedName = name.toLowerCase().trim();
                        if (uniqueIngredients.containsKey(normalizedName)) {
                            // C'est un doublon
                            duplicatesToDelete.add(doc);
                            ingredientIdUpdates.put(doc.getId(), uniqueIngredients.get(normalizedName));
                        } else {
                            // Premier ingrédient avec ce nom
                            uniqueIngredients.put(normalizedName, doc.getId());
                        }
                    }
                }

                // Deuxième passe : mettre à jour les recettes
                if (!ingredientIdUpdates.isEmpty()) {
                    db.collection("recipes")
                        .get()
                        .addOnSuccessListener(recipeDocs -> {
                            for (DocumentSnapshot recipeDoc : recipeDocs) {
                                List<String> ingredients = (List<String>) recipeDoc.get("ingredients");
                                if (ingredients != null) {
                                    boolean needsUpdate = false;
                                    for (int i = 0; i < ingredients.size(); i++) {
                                        String oldId = ingredients.get(i);
                                        if (ingredientIdUpdates.containsKey(oldId)) {
                                            ingredients.set(i, ingredientIdUpdates.get(oldId));
                                            needsUpdate = true;
                                        }
                                    }
                                    if (needsUpdate) {
                                        recipeDoc.getReference().update("ingredients", ingredients);
                                    }
                                }
                            }
                        });
                }

                // Troisième passe : supprimer les doublons
                for (DocumentSnapshot duplicate : duplicatesToDelete) {
                    duplicate.getReference().delete()
                        .addOnSuccessListener(aVoid -> Log.d(TAG, "Doublon supprimé : " + duplicate.getId()))
                        .addOnFailureListener(e -> Log.e(TAG, "Erreur lors de la suppression du doublon", e));
                }
            })
            .addOnFailureListener(e -> Log.e(TAG, "Erreur lors du nettoyage des ingrédients", e));
    }
}