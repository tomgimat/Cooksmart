package fr.tomgimat.cooksmart.ui.ingredients;

import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import fr.tomgimat.cooksmart.data.firebase.firestore.Ingredient;
import fr.tomgimat.cooksmart.data.firebase.firestore.UserIngredients;

public class IngredientsSelectionViewModel extends ViewModel {
    private final MutableLiveData<List<Ingredient>> filteredIngredients = new MutableLiveData<>();
    private final MutableLiveData<List<Ingredient>> ownedIngredients = new MutableLiveData<>(new ArrayList<>());
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private final Set<String> selectedIngredientIds = new HashSet<>();
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

    public void loadUserIngredients() {
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
        List<String> newIngredientIds = selectedIngredients.stream()
                .filter(Ingredient::isSelected)
                .map(Ingredient::getId)
                .collect(Collectors.toList());

        // On récupère d'abord les ingrédients existants
        db.collection("user_ingredients")
                .document(userId)
                .get()
                .addOnSuccessListener(document -> {
                    List<String> existingIngredientIds = new ArrayList<>();
                    if (document.exists()) {
                        List<String> ids = (List<String>) document.get("ingredientIds");
                        if (ids != null) {
                            existingIngredientIds.addAll(ids);
                        }
                    }

                    existingIngredientIds.addAll(newIngredientIds);

                    List<String> mergedIngredientIds = existingIngredientIds.stream()
                            .distinct()
                            .collect(Collectors.toList());

                    db.collection("user_ingredients")
                            .document(userId)
                            .set(new UserIngredients(userId, mergedIngredientIds))
                            .addOnSuccessListener(aVoid -> {
                                loadIngredients();

                                List<Ingredient> owned = allIngredients.stream()
                                        .filter(ingredient -> mergedIngredientIds.contains(ingredient.getId()))
                                        .collect(Collectors.toList());
                                ownedIngredients.setValue(owned);
                            });
                });
    }

    /**
     * Charge les ingrédients depuis Firestore
     */
    public void loadIngredients() {
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
                    // On restaure les sélections
                    for (Ingredient ingredient : allIngredients) {
                        ingredient.setSelected(selectedIngredientIds.contains(ingredient.getId()));
                    }
                    filteredIngredients.setValue(allIngredients);
                    loadUserIngredients(); // On recharge les ingrédients possédés après avoir chargé tous les ingrédients
                });
    }

    /**
     * Filtre les ingrédients en fonction de la recherche
     *
     * @param query
     */
    public void filterIngredients(String query) {
        if (query == null || query.isEmpty()) {
            filteredIngredients.setValue(allIngredients);
            return;
        }

        String lowerCaseQuery = query.toLowerCase();
        List<Ingredient> filtered = allIngredients.stream()
                .filter(ingredient -> ingredient.getName().toLowerCase().contains(lowerCaseQuery))
                .collect(Collectors.toList());
        filteredIngredients.setValue(filtered);
    }

    public void toggleIngredientSelection(Ingredient ingredient) {
        if (selectedIngredientIds.contains(ingredient.getId())) {
            selectedIngredientIds.remove(ingredient.getId());
        } else {
            selectedIngredientIds.add(ingredient.getId());
        }
        // On met à jour la liste filtrée pour refléter les changements de sélection
        updateFilteredList();
    }

    private void updateFilteredList() {
        List<Ingredient> currentList = filteredIngredients.getValue();
        if (currentList != null) {
            for (Ingredient ingredient : currentList) {
                ingredient.setSelected(selectedIngredientIds.contains(ingredient.getId()));
            }
            filteredIngredients.setValue(currentList);
        }
    }

    public LiveData<List<Ingredient>> getFilteredIngredients() {
        return filteredIngredients;
    }

    public LiveData<List<Ingredient>> getOwnedIngredients() {
        return ownedIngredients;
    }

    public void removeIngredient(Ingredient ingredient) {
        String userId = auth.getCurrentUser().getUid();
        db.collection("user_ingredients")
                .document(userId)
                .get()
                .addOnSuccessListener(document -> {
                    if (document.exists()) {
                        List<String> ownedIngredientIds = (List<String>) document.get("ingredientIds");
                        if (ownedIngredientIds != null) {
                            ownedIngredientIds.remove(ingredient.getId());
                            db.collection("user_ingredients")
                                    .document(userId)
                                    .set(new UserIngredients(userId, ownedIngredientIds))
                                    .addOnSuccessListener(aVoid -> {
                                        selectedIngredientIds.remove(ingredient.getId());
                                        loadUserIngredients();
                                        updateFilteredList();
                                    });
                        }
                    }
                });
    }

    /**
     * Réinitialise la liste des ingrédients de l'utilisateur en supprimant tous les ingrédients
     * de la collection user_ingredients dans Firestore.
     */
    public void resetIngredientsList() {
        String userId = auth.getCurrentUser().getUid();
        if (userId == null) return;

        db.collection("user_ingredients")
            .document(userId)
            .set(Collections.singletonMap("ingredientIds", new ArrayList<String>()))
            .addOnSuccessListener(aVoid -> {
                ownedIngredients.setValue(new ArrayList<>());
                selectedIngredientIds.clear();
            })
            .addOnFailureListener(e -> {
                Log.e("IngredientsViewModel", "Erreur lors de la réinitialisation des ingrédients", e);
            });
    }
} 