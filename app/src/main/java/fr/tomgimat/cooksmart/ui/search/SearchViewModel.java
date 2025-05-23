package fr.tomgimat.cooksmart.ui.search;

import androidx.lifecycle.LiveData;
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
    private final MutableLiveData<Set<String>> dietFilters = new MutableLiveData<>(new HashSet<>());
    private final MutableLiveData<Integer> maxPrepTime = new MutableLiveData<>(null); // null = pas de filtre
    private final MutableLiveData<Integer> maxIngredients = new MutableLiveData<>(null);
    private final MutableLiveData<List<FirestoreRecipe>> searchResults = new MutableLiveData<>(new ArrayList<>());

    public LiveData<List<FirestoreRecipe>> getSearchResults() { return searchResults; }

    // Appelé quand la query ou les filtres changent (dans le fragment)
    public void updateSearch(String query, Set<String> diet, Integer prepTime, Integer nbIngredients) {
        searchQuery.setValue(query);
        dietFilters.setValue(diet);
        maxPrepTime.setValue(prepTime);
        maxIngredients.setValue(nbIngredients);
        fetchRecipes();
    }

    // Exécuté dès qu’un filtre ou la query change
    private void fetchRecipes() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        CollectionReference recipesRef = db.collection("recipes");

        // Pour la démo, tu peux d’abord charger tout et filtrer côté client si la logique de requête combinée est trop complexe
        recipesRef.get().addOnSuccessListener(snapshot -> {
            List<FirestoreRecipe> filtered = new ArrayList<>();
            String recipeQuery = searchQuery.getValue() != null ? searchQuery.getValue().toLowerCase() : "";
            Set<String> diet = dietFilters.getValue() != null ? dietFilters.getValue() : new HashSet<>();
            Integer time = maxPrepTime.getValue();
            Integer nbIng = maxIngredients.getValue();

            for (DocumentSnapshot doc : snapshot.getDocuments()) {
                FirestoreRecipe recipe = FirestoreRecipe.fromFirestoreDoc(doc);

                // 1. Filtre par texte (nom ou ingrédient)
                boolean matches = recipeQuery.isEmpty()
                        || recipe.name.toLowerCase().contains(recipeQuery)
                        || recipe.ingredients.stream().anyMatch(i -> i.toLowerCase().contains(recipeQuery));

                // 2. Filtres diététiques (végétarien, vegan...)
                boolean dietOk = true;
                if (diet.contains("Vegetarian") && !recipe.isVegetarian) dietOk = false;
                if (diet.contains("Vegan") && !recipe.isVegan) dietOk = false;
                if (diet.contains("GlutenFree") && !recipe.isGlutenFree) dietOk = false;
                if (diet.contains("LactoseFree") && !recipe.isLactoseFree) dietOk = false;
                // ... rajoute les autres au besoin

                // 3. Durée max
                boolean timeOk = time == null || (recipe.duration != null && recipe.duration <= time);

                // 4. Nb ingrédients max
                boolean ingOk = nbIng == null || (recipe.ingredients != null && recipe.ingredients.size() <= nbIng);

                if (matches && dietOk && timeOk && ingOk) {
                    filtered.add(recipe);
                }
            }
            searchResults.setValue(filtered);
        });
    }
}
