package fr.tomgimat.cooksmart.ui.recipe;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.button.MaterialButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.tomgimat.cooksmart.R;
import fr.tomgimat.cooksmart.data.firebase.firestore.FirestoreRecipe;
import fr.tomgimat.cooksmart.data.model.IngredientDisplay;
import fr.tomgimat.cooksmart.databinding.FragmentRecipeDetailBinding;

/**
 * Gère les détails d'une recette
 */
public class RecipeDetailFragment extends Fragment {
    private FragmentRecipeDetailBinding binding;
    private IngredientAdapterForRecipeUI ingredientAdapterForRecipeUI;
    private boolean isRecipeSaved = false;
    private static final String TAG = "RecipeDetailFragment";
    private FirestoreRecipe recipe;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentRecipeDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Overlay de loading en attendant le chargement de la page
        showLoadingOverlay(true);

        ingredientAdapterForRecipeUI = new IngredientAdapterForRecipeUI();
        binding.rvIngredients.setAdapter(ingredientAdapterForRecipeUI);
        binding.rvIngredients.setLayoutManager(new LinearLayoutManager(getContext()));

        Bundle args = getArguments();
        String firestoreId = args != null ? args.getString("recipe_id") : null;
        String mealId = args != null ? args.getString("meal_id") : null;

        RecipeDetailViewModel viewModel = new ViewModelProvider(this).get(RecipeDetailViewModel.class);

        // Vérifier si la recette est sauvegardée
        if (firestoreId != null) {
            checkIfRecipeSaved(firestoreId);
        }

        // Configurer le bouton de sauvegarde
        binding.btnSaveRecipe.setOnClickListener(v -> {
            if (firestoreId != null) {
                toggleRecipeSave(firestoreId);
            } else if (mealId != null) {
                toggleRecipeSave(mealId);
            }
        });

        //Appelle le viewmodel pour charger la recette
        viewModel.loadRecipe(firestoreId, mealId);

        //Observe la recette en mettant à jour les données affichées
        viewModel.getRecipe().observe(getViewLifecycleOwner(), recipe -> {
            if (recipe == null) {
                return;
            }
            this.recipe = recipe;
            displayRecipe(recipe);
            //On affiche la page et l'overlay disparait
            showLoadingOverlay(false);
            setupStepByStepButton();
        });
    }

    /**
     * Vérifie si la recette est déjà sauvegardée
     *
     * @param recipeId
     */
    private void checkIfRecipeSaved(String recipeId) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore.getInstance().collection("saved_recipes").document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> savedRecipes = (List<String>) documentSnapshot.get("recipe_ids");
                        isRecipeSaved = savedRecipes != null && savedRecipes.contains(recipeId);
                        updateSaveButtonState();
                    }
                });
    }

    /**
     * Sauvegarde ou supprime la recette des favoris/enregistrés
     *
     * @param recipeId
     */
    private void toggleRecipeSave(String recipeId) {
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        FirebaseFirestore.getInstance().collection("saved_recipes").document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    List<String> savedRecipes = new ArrayList<>();
                    if (documentSnapshot.exists()) {
                        List<String> existingRecipes = (List<String>) documentSnapshot.get("recipe_ids");
                        if (existingRecipes != null) {
                            savedRecipes.addAll(existingRecipes);
                        }
                    }

                    if (isRecipeSaved) {
                        savedRecipes.remove(recipeId);
                        Toast.makeText(getContext(), R.string.recipe_removed, Toast.LENGTH_SHORT).show();
                    } else {
                        savedRecipes.add(recipeId);
                        Toast.makeText(getContext(), R.string.recipe_saved, Toast.LENGTH_SHORT).show();
                    }

                    FirebaseFirestore.getInstance().collection("saved_recipes").document(uid)
                            .set(Collections.singletonMap("recipe_ids", savedRecipes))
                            .addOnSuccessListener(aVoid -> {
                                isRecipeSaved = !isRecipeSaved;
                                updateSaveButtonState();
                            });
                });
    }

    /**
     * Met à jour l'état du bouton de sauvegarde en fonction de la recette
     */
    private void updateSaveButtonState() {
        binding.btnSaveRecipe.setImageResource(isRecipeSaved ? R.drawable.bookmark_fill_24px : R.drawable.bookmark_24px);
    }

    /**
     * Affichage des données de la recette
     */
    private void displayRecipe(FirestoreRecipe recipe) {
        Log.d(TAG, "Début de l'affichage de la recette : " + recipe.name);

        //Animation fadeIn en chargeant les éléments de la page
        View contentView = binding.getRoot();
        contentView.setAlpha(0f);
        contentView.setVisibility(View.VISIBLE);
        contentView.animate().alpha(1f).setDuration(250).start();

        binding.textRecipeTitle.setText(recipe.name);
        binding.textRecipeDescription.setText(recipe.instructions);

        if (recipe.duration != null && recipe.duration > 0) {
            binding.textRecipeDuration.setText(getString(R.string.estimated_time, recipe.duration));
        } else {
            binding.textRecipeDuration.setText(getString(R.string.unknown_duration));
        }

        // Récupère les ingrédients possédés par l'utilisateur
        String userId = FirebaseAuth.getInstance().getCurrentUser().getUid();
        Log.d(TAG, "Récupération des ingrédients de l'utilisateur : " + userId);

        FirebaseFirestore.getInstance().collection("user_ingredients")
                .document(userId)
                .get()
                .addOnSuccessListener(document -> {
                    List<String> ownedIngredientIds = new ArrayList<>();
                    if (document.exists()) {
                        List<String> ids = (List<String>) document.get("ingredientIds");
                        if (ids != null) {
                            ownedIngredientIds.addAll(ids);
                            Log.d(TAG, "Ingrédients possédés par l'utilisateur : " + ownedIngredientIds.size());
                        }
                    }

                    // Récupérer les IDs des ingrédients de la recette
                    Log.d(TAG, "Récupération des IDs des ingrédients de la recette");
                    FirebaseFirestore.getInstance().collection("ingredients")
                            .get()
                            .addOnSuccessListener(ingredientDocs -> {
                                Map<String, String> ingredientNameToId = new HashMap<>();
                                for (DocumentSnapshot doc : ingredientDocs) {
                                    String name = doc.getString("name");
                                    if (name != null) {
                                        ingredientNameToId.put(name.toLowerCase(), doc.getId());
                                    }
                                }
                                Log.d(TAG, "Map des ingrédients créée : " + ingredientNameToId.size() + " ingrédients");

                                // Prépare la liste des ingrédients avec leur statut
                                List<IngredientDisplay> displayIngredients = new ArrayList<>();
                                List<String> missingIngredients = new ArrayList<>();

                                for (int i = 0; i < recipe.ingredients.size(); i++) {
                                    String ingredientName = recipe.ingredients.get(i);
                                    String mesure = recipe.measures.size() > i ? recipe.measures.get(i) : "";
                                    String displayText = (mesure + " " + ingredientName).trim();

                                    // Trouve l'ID de l'ingrédient
                                    String ingredientId = ingredientNameToId.get(ingredientName.toLowerCase());
                                    boolean isOwned = ingredientId != null && ownedIngredientIds.contains(ingredientId);

                                    Log.d(TAG, String.format("Ingrédient : %s (ID: %s, Possédé: %b)",
                                            ingredientName, ingredientId, isOwned));

                                    displayIngredients.add(new IngredientDisplay(displayText, isOwned));
                                    if (!isOwned) {
                                        missingIngredients.add(displayText);
                                    }
                                }

                                // Mets à jour l'adaptateur avec les ingrédients
                                ingredientAdapterForRecipeUI.setIngredientsWithOwnership(displayIngredients);
                                Log.d(TAG, "Adaptateur mis à jour avec " + displayIngredients.size() + " ingrédients");

                                // Affiche les ingrédients manquants si nécessaire
                                if (!missingIngredients.isEmpty()) {
                                    Log.d(TAG, "Ingrédients manquants trouvés : " + missingIngredients.size());
                                    binding.textMissingIngredients.setVisibility(View.VISIBLE);
                                    binding.textMissingIngredients.setText(getString(R.string.missing_ingredients));
                                    StringBuilder missingText = new StringBuilder();
                                    for (String ingredient : missingIngredients) {
                                        missingText.append("• ").append(ingredient).append("\n");
                                    }
                                    binding.textMissingIngredientsList.setText(missingText.toString());
                                    binding.textMissingIngredientsList.setVisibility(View.VISIBLE);
                                } else {
                                    Log.d(TAG, "Aucun ingrédient manquant");
                                    binding.textMissingIngredients.setVisibility(View.GONE);
                                    binding.textMissingIngredientsList.setVisibility(View.GONE);
                                }
                            })
                            .addOnFailureListener(e -> {
                                Log.e(TAG, "Erreur lors de la récupération des ingrédients", e);
                            });
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Erreur lors de la récupération des ingrédients de l'utilisateur", e);
                });
    }

    /**
     * Affiche ou cache l'overlay de chargement
     *
     * @param show
     */
    private void showLoadingOverlay(boolean show) {
        binding.loadingOverlay.setVisibility(show ? View.VISIBLE : View.GONE);

        int contentVisibility = show ? View.GONE : View.VISIBLE;
        binding.textRecipeTitle.setVisibility(contentVisibility);
        binding.ratingBarRecipe.setVisibility(contentVisibility);
        binding.textNbRatings.setVisibility(contentVisibility);
        binding.textRecipeDuration.setVisibility(contentVisibility);
        binding.btnStepByStep.setVisibility(contentVisibility);
        binding.textIngredientsLabel.setVisibility(contentVisibility);
        binding.rvIngredients.setVisibility(contentVisibility);
        binding.textRecipeInfoLabel.setVisibility(contentVisibility);
        binding.textRecipeDescription.setVisibility(contentVisibility);
        binding.btnSaveRecipe.setVisibility(contentVisibility);
    }

    /**
     * Configure le bouton pour afficher la page des étapes de la recette
     */
    private void setupStepByStepButton() {
        MaterialButton buttonStartStepByStep = binding.getRoot().findViewById(R.id.btnStepByStep);
        buttonStartStepByStep.setOnClickListener(v -> {
            if (recipe != null) {
                Bundle args = new Bundle();
                args.putString("recipe_id", recipe.id);
                Navigation.findNavController(requireView())
                        .navigate(R.id.action_recipeDetailFragment_to_stepByStepFragment, args);
            }
        });
    }

    /**
     * Simple callback pour asynchro
     * Ce callback prend une FirestoreRecipe
     */
    interface OnRecipeLoaded {
        void onLoaded(FirestoreRecipe recipe);
    }
}
