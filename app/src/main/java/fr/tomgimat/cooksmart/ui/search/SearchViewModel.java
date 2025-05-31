package fr.tomgimat.cooksmart.ui.search;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import fr.tomgimat.cooksmart.data.firebase.firestore.FirestoreRecipe;

public class SearchViewModel extends ViewModel {

    private final MutableLiveData<String> searchQuery = new MutableLiveData<>("");
    private final MutableLiveData<Filters> filters = new MutableLiveData<>(new Filters());
    private final MutableLiveData<List<FirestoreRecipe>> allRecipes = new MutableLiveData<>(new ArrayList<>());
    private final MediatorLiveData<List<FirestoreRecipe>> filteredRecipes = new MediatorLiveData<>();

    public SearchViewModel() {
        filteredRecipes.addSource(allRecipes, r -> refreshResults());
        filteredRecipes.addSource(searchQuery, s -> refreshResults());
        filteredRecipes.addSource(filters, f -> refreshResults());
        //Charge toutes les recettes en cache pour alléger les requêtes ensuite
        fetchAllRecipes();
    }

    private void fetchAllRecipes() {
        FirebaseFirestore.getInstance().collection("recipes")
                .get()
                .addOnSuccessListener(snapshot -> {
                    List<FirestoreRecipe> list = new ArrayList<>();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        list.add(FirestoreRecipe.fromFirestoreDoc(doc));
                    }
                    allRecipes.setValue(list);
                });
    }

    private void refreshResults() {
        List<FirestoreRecipe> all = allRecipes.getValue() != null ? allRecipes.getValue() : new ArrayList<>();
        String query = searchQuery.getValue() != null ? searchQuery.getValue().toLowerCase() : "";
        Filters f = filters.getValue() != null ? filters.getValue() : new Filters();

        List<FirestoreRecipe> filtered = new ArrayList<>();
        for (FirestoreRecipe recipe : all) {
            boolean matches = recipe.name.toLowerCase().contains(query) ||
                    recipe.ingredients.stream().anyMatch(i -> i.toLowerCase().contains(query));

            boolean dietOk = (f.vegetarian == null || recipe.isVegetarian == f.vegetarian)
                    && (f.vegan == null || recipe.isVegan == f.vegan)
                    && (f.glutenFree == null || recipe.isGlutenFree == f.glutenFree)
                    && (f.lactoseFree == null || recipe.isLactoseFree == f.lactoseFree)
                    && (f.lowSalt == null || recipe.isLowSalt == f.lowSalt)
                    && (f.lowSugar == null || recipe.isLowSugar == f.lowSugar)
                    && (f.pescetarian == null || recipe.isPescetarian == f.pescetarian)
                    && (f.halal == null || recipe.isHalal == f.halal);

            boolean durationOk = f.maxDuration == null || (recipe.duration != null && recipe.duration <= f.maxDuration);
            boolean ingOk = f.maxIngredients == null || (recipe.ingredients != null && recipe.ingredients.size() <= f.maxIngredients);

            if (matches && dietOk && durationOk && ingOk) {
                filtered.add(recipe);
            }
        }
        filteredRecipes.setValue(filtered);
        Log.d("SearchViewModel", "Nb recettes trouvées : " + filtered.size());
    }

    public void setSearchQuery(String query) { searchQuery.setValue(query); }
    public void setFilters(Filters f) { filters.setValue(f); }
    public Filters getFilters() { return filters.getValue(); }

    public LiveData<List<FirestoreRecipe>> getFilteredRecipes() { return filteredRecipes; }

    public LiveData<List<FirestoreRecipe>> getAllRecipes() {
        return allRecipes;
    }
}


