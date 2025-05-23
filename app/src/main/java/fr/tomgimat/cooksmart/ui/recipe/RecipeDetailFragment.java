package fr.tomgimat.cooksmart.ui.recipe;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.List;

import fr.tomgimat.cooksmart.data.firebase.firestore.FirestoreRecipe;
import fr.tomgimat.cooksmart.databinding.FragmentRecipeDetailBinding;

/**
 * Gère les détails d'une recette
 */
public class RecipeDetailFragment extends Fragment {
    private FragmentRecipeDetailBinding binding;
    private IngredientAdapter ingredientAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentRecipeDetailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ingredientAdapter = new IngredientAdapter(new ArrayList<>());
        binding.rvIngredients.setAdapter(ingredientAdapter);
        binding.rvIngredients.setLayoutManager(new LinearLayoutManager(getContext()));


        Bundle args = getArguments();
        String firestoreId = args != null ? args.getString("recipe_id") : null;
        String mealId = args != null ? args.getString("meal_id") : null;


        RecipeDetailViewModel viewModel = new ViewModelProvider(this).get(RecipeDetailViewModel.class);

        //Observe la recette en mettant à jour les données affichées
        viewModel.getRecipe().observe(getViewLifecycleOwner(), recipe -> {
            if (recipe == null){
                return;
            }
            binding.textRecipeTitle.setText(recipe.name);
            binding.textRecipeDescription.setText(recipe.instructions);
            displayRecipe(recipe);
        });

        //Appelle le viewmodel pour charger la recette
        viewModel.loadRecipe(firestoreId, mealId);

    }

    /**
     * Affichage des données de la recette
     */
    private void displayRecipe(FirestoreRecipe recipe) {
        binding.textRecipeTitle.setText(recipe.name);
        binding.textRecipeDescription.setText(recipe.instructions);
        //ingrédients
        List<String> displayIngredients = new ArrayList<>();
        for (int i = 0; i < recipe.ingredients.size(); i++) {
            String mesure = recipe.measures.size() > i ? recipe.measures.get(i) : "";
            String item = (mesure + " " + recipe.ingredients.get(i)).trim();
            displayIngredients.add(item);
        }
        ingredientAdapter.setIngredients(displayIngredients);
    }





    /** Simple callback pour asynchro
     * Ce callback prend une FirestoreRecipe */
    interface OnRecipeLoaded {
        void onLoaded(FirestoreRecipe recipe);
    }
}
