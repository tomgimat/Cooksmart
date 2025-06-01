package fr.tomgimat.cooksmart.ui.ingredients;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;

import java.util.List;

import fr.tomgimat.cooksmart.R;
import fr.tomgimat.cooksmart.data.firebase.firestore.Ingredient;
import fr.tomgimat.cooksmart.databinding.FragmentIngredientsSelectionBinding;

public class IngredientsSelectionFragment extends Fragment {
    private FragmentIngredientsSelectionBinding binding;
    private IngredientsSelectionViewModel viewModel;
    private IngredientsAdapter adapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentIngredientsSelectionBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(IngredientsSelectionViewModel.class);

        setupRecyclerView();
        setupSearchBar();
        setupValidationButton();
        observeIngredients();

        return binding.getRoot();
    }

    private void setupRecyclerView() {
        adapter = new IngredientsAdapter(viewModel);
        binding.recyclerViewIngredients.setLayoutManager(new GridLayoutManager(getContext(), 2));
        binding.recyclerViewIngredients.setAdapter(adapter);
    }

    private void setupSearchBar() {
        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                viewModel.filterIngredients(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                viewModel.filterIngredients(newText);
                return true;
            }
        });
    }

    /**
     * Configure le bouton de validation pour sauvegarder les ingrédients sélectionnés
     */
    private void setupValidationButton() {
        binding.buttonValidateIngredientSelection.setOnClickListener(v -> {
            binding.buttonValidateIngredientSelection.setEnabled(false);
            binding.progressBar.setVisibility(View.VISIBLE);
            List<Ingredient> currentIngredients = adapter.getCurrentList();
            if (currentIngredients != null) {
                viewModel.saveUserIngredients(currentIngredients);
                Toast.makeText(getContext(), R.string.ingredients_saved, Toast.LENGTH_SHORT).show();
                Navigation.findNavController(v).navigateUp();
            }
        });
    }

    /**
     * Observe les ingrédients de la liste filteredIngredients et les ingrédients possédés pour les mettre à jour
     */
    private void observeIngredients() {
        // Observe les ingrédients filtrés pour les mettre à jour
        viewModel.getFilteredIngredients().observe(getViewLifecycleOwner(), ingredients -> {
            adapter.submitList(ingredients);
        });

        // Observe les ingrédients possédés pour les mettre à jour
        viewModel.getOwnedIngredients().observe(getViewLifecycleOwner(), ownedIngredients -> {
            if (ownedIngredients != null) {

                for (Ingredient ownedIngredient : ownedIngredients) {
                    viewModel.toggleIngredientSelection(ownedIngredient);
                }
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 