package fr.tomgimat.cooksmart.ui.recipe;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.List;

import fr.tomgimat.cooksmart.R;
import fr.tomgimat.cooksmart.data.firebase.firestore.FirestoreRecipe;
import fr.tomgimat.cooksmart.databinding.FragmentRecipeDetailBinding;
import fr.tomgimat.cooksmart.ui.profile.ProfileFragment;

/**
 * Gère les détails d'une recette
 */
public class RecipeDetailFragment extends Fragment {
    private FragmentRecipeDetailBinding binding;
    private IngredientAdapter ingredientAdapter;
    private String recipeId;
    private ImageButton bookmarkButton;
    private boolean isSaved = false;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            recipeId = getArguments().getString("recipe_id");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentRecipeDetailBinding.inflate(inflater, container, false);
        
        // Initialisation du bouton d'enregistrement
        bookmarkButton = binding.bookmarkButton;
        setupBookmarkButton();
        
        ingredientAdapter = new IngredientAdapter(new ArrayList<>());
        binding.rvIngredients.setAdapter(ingredientAdapter);
        binding.rvIngredients.setLayoutManager(new LinearLayoutManager(getContext()));

        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Overlay de loading en attendant le chargement de la page
        showLoadingOverlay(true);

        Bundle args = getArguments();
        String mealId = args != null ? args.getString("meal_id") : null;

        RecipeDetailViewModel viewModel = new ViewModelProvider(this).get(RecipeDetailViewModel.class);

        //Appelle le viewmodel pour charger la recette
        viewModel.loadRecipe(recipeId, mealId);

        //Observe la recette en mettant à jour les données affichées
        viewModel.getRecipe().observe(getViewLifecycleOwner(), recipe -> {
            if (recipe == null){
                return;
            }
            displayRecipe(recipe);
            //On affiche la page et l'overlay disparait
            showLoadingOverlay(false);
        });
    }

    /**
     * Affichage des données de la recette
     */
    private void displayRecipe(FirestoreRecipe recipe) {
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

        //ingrédients
        List<String> displayIngredients = new ArrayList<>();
        for (int i = 0; i < recipe.ingredients.size(); i++) {
            String mesure = recipe.measures.size() > i ? recipe.measures.get(i) : "";
            String item = (mesure + " " + recipe.ingredients.get(i)).trim();
            displayIngredients.add(item);
        }
        ingredientAdapter.setIngredients(displayIngredients);
    }

    private void setupBookmarkButton() {
        // Vérifier si la recette est déjà sauvegardée
        ProfileFragment.isRecipeSaved(recipeId, isSaved -> {
            this.isSaved = isSaved;
            updateBookmarkButtonState();
        });

        // Gérer le clic sur le bouton
        bookmarkButton.setOnClickListener(v -> {
            if (isSaved) {
                ProfileFragment.removeRecipe(recipeId);
                Toast.makeText(getContext(), "Recette retirée des favoris", Toast.LENGTH_SHORT).show();
            } else {
                ProfileFragment.saveRecipe(recipeId);
                Toast.makeText(getContext(), "Recette ajoutée aux favoris", Toast.LENGTH_SHORT).show();
            }
            isSaved = !isSaved;
            updateBookmarkButtonState();
        });
    }

    private void updateBookmarkButtonState() {
        bookmarkButton.setImageResource(isSaved ? R.drawable.bookmark_fill_24px : R.drawable.bookmark_24px);
    }

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
    }

    /** Simple callback pour asynchro
     * Ce callback prend une FirestoreRecipe */
    interface OnRecipeLoaded {
        void onLoaded(FirestoreRecipe recipe);
    }
}
