package fr.tomgimat.cooksmart.ui.ingredients;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import fr.tomgimat.cooksmart.data.firebase.firestore.Ingredient;
import fr.tomgimat.cooksmart.data.firebase.firestore.FirestoreRecipe;
import fr.tomgimat.cooksmart.data.firebase.firestore.UserIngredients;

public class IngredientsSelectionViewModel extends ViewModel {
    private final MutableLiveData<List<Ingredient>> filteredIngredients = new MutableLiveData<>();
    private final MutableLiveData<List<Ingredient>> ownedIngredients = new MutableLiveData<>();
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private List<Ingredient> allIngredients = new ArrayList<>();

    public IngredientsSelectionViewModel() {
        loadIngredients();
        loadUserIngredients();
    }


//    private void extractAndSaveIngredients() {
//        db.collection("recipes")
//            .get()
//            .addOnSuccessListener(queryDocumentSnapshots -> {
//                Set<String> uniqueIngredients = new HashSet<>();
//
//                // Extraire tous les ingrédients uniques
//                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
//                    FirestoreRecipe recipe = FirestoreRecipe.fromFirestoreDoc(document);
//                    if (recipe.getIngredients() != null) {
//                        uniqueIngredients.addAll(recipe.getIngredients());
//                    }
//                }
//
//                // Créer et sauvegarder les ingrédients
//                for (String ingredientName : uniqueIngredients) {
//                    if (ingredientName != null && !ingredientName.trim().isEmpty()) {
//                        Ingredient ingredient = new Ingredient(null, ingredientName.trim());
//                        db.collection("ingredients")
//                            .add(ingredient)
//                            .addOnSuccessListener(documentReference -> {
//                                ingredient.setId(documentReference.getId());
//                                db.collection("ingredients")
//                                    .document(documentReference.getId())
//                                    .set(ingredient);
//                            });
//                    }
//                }
//            });
//    }

    private void loadUserIngredients() {
        String userId = auth.getCurrentUser().getUid();
        db.collection("user_ingredients")
            .document(userId)
            .get()
            .addOnSuccessListener(document -> {
                if (document.exists()) {
                    List<String> ownedIngredientIds = (List<String>) document.get("ingredientIds");
                    if (ownedIngredientIds != null) {
                        List<Ingredient> owned = allIngredients.stream()
                            .filter(ingredient -> ownedIngredientIds.contains(ingredient.getId()))
                            .collect(Collectors.toList());
                        ownedIngredients.setValue(owned);
                    }
                }
            });
    }

    public void saveUserIngredients(List<Ingredient> selectedIngredients) {
        String userId = auth.getCurrentUser().getUid();
        List<String> ingredientIds = selectedIngredients.stream()
            .filter(Ingredient::isSelected)
            .map(Ingredient::getId)
            .collect(Collectors.toList());

        db.collection("user_ingredients")
            .document(userId)
            .set(new UserIngredients(userId, ingredientIds))
            .addOnSuccessListener(aVoid -> {
                loadUserIngredients();
            });
    }

    /**
     * Charge les ingrédients depuis Firestore
     */
    private void loadIngredients() {
        db.collection("ingredients")
            .orderBy("normalizedName")
            .get()
            .addOnSuccessListener(queryDocumentSnapshots -> {
                List<Ingredient> ingredients = new ArrayList<>();
                for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                    Ingredient ingredient = document.toObject(Ingredient.class);
                    ingredient.setId(document.getId());
                    ingredients.add(ingredient);
                }
                allIngredients = ingredients;
                filteredIngredients.setValue(allIngredients);
                loadUserIngredients(); // Recharger les ingrédients possédés après avoir chargé tous les ingrédients
            });
    }

    /**
     * Filtre les ingrédients en fonction de la recherche
     * @param query
     */
    public void filterIngredients(String query) {
        if (query == null || query.isEmpty()) {
            filteredIngredients.setValue(allIngredients);
            return;
        }

        String normalizedQuery = normalizeString(query);
        List<Ingredient> filtered = allIngredients.stream()
            .filter(ingredient -> ingredient.getNormalizedName().contains(normalizedQuery))
            .collect(Collectors.toList());
        filteredIngredients.setValue(filtered);
    }

    /**
     * Enleve tout ce qui peut nuire à la recherche d'ingrédients
     * @param input
     * @return
     */
    private String normalizeString(String input) {
        return input.toLowerCase()
            .replaceAll("[éèêë]", "e")
            .replaceAll("[àâä]", "a")
            .replaceAll("[ùûü]", "u")
            .replaceAll("[îï]", "i")
            .replaceAll("[ôö]", "o")
            .replaceAll("[ç]", "c");
    }

    public LiveData<List<Ingredient>> getFilteredIngredients() {
        return filteredIngredients;
    }

    public LiveData<List<Ingredient>> getOwnedIngredients() {
        return ownedIngredients;
    }
} 