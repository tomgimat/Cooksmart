package fr.tomgimat.cooksmart.ui.home;

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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import fr.tomgimat.cooksmart.data.MealDbClient;
import fr.tomgimat.cooksmart.data.firebase.firestore.FirestoreRecipe;
import fr.tomgimat.cooksmart.data.firebase.firestore.FirestoreVideo;
import fr.tomgimat.cooksmart.data.mealdb.Meal;
import fr.tomgimat.cooksmart.data.mealdb.MealDbApiResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class HomeViewModel extends ViewModel {

    private MutableLiveData<String> welcomeText = new MutableLiveData<>();
    private MutableLiveData<List<Meal>> randomMeals = new MutableLiveData<>();
    // Suggestions list
    private MutableLiveData<List<FirestoreRecipe>> suggestedRecipes = new MutableLiveData<>();
    private final MutableLiveData<List<FirestoreVideo>> videos = new MutableLiveData<>(new ArrayList<>());

    public LiveData<List<Meal>> getRandomMeals() {
        return randomMeals;
    }

    public LiveData<List<FirestoreRecipe>> getSuggestedRecipes() {
        return suggestedRecipes;
    }

    public LiveData<List<FirestoreVideo>> getVideos() {
        return videos;
    }

    public HomeViewModel() {
    }

    public LiveData<String> getText() {
        return welcomeText;
    }

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

        query.limit(10)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<FirestoreRecipe> results = new ArrayList<>();
                    for (DocumentSnapshot doc : queryDocumentSnapshots) {
//                        FirestoreRecipe recipe = doc.toObject(FirestoreRecipe.class);
                        FirestoreRecipe recipe = FirestoreRecipe.fromFirestoreDoc(doc);
                        results.add(recipe);
                    }
                    //Utilise la liste pour update adapter RecyclerView
//                    updateRecyclerView(results);
                    suggestedRecipes.setValue(results);
                })
                .addOnFailureListener(e -> {
                    Log.d("Firestore", "Error getting documents: ", e);
                });
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

}