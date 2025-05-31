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
import androidx.recyclerview.widget.LinearLayoutManager;

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

    private void setupValidationButton() {
        binding.buttonValidateIngredientSelection.setOnClickListener(v -> {
            // Désactiver le bouton et afficher le chargement
            binding.buttonValidateIngredientSelection.setEnabled(false);
            binding.progressBar.setVisibility(View.VISIBLE);
            
            // Récupérer la liste actuelle des ingrédients
            List<Ingredient> currentIngredients = adapter.getCurrentList();
            if (currentIngredients != null) {
                // Sauvegarder les ingrédients sélectionnés
                viewModel.saveUserIngredients(currentIngredients);
                
                // Afficher un message de confirmation
                Toast.makeText(getContext(), R.string.ingredients_saved, Toast.LENGTH_SHORT).show();
                
                // Retourner à l'écran précédent
                Navigation.findNavController(v).navigateUp();
            }
        });
    }

    private void observeIngredients() {
        viewModel.getFilteredIngredients().observe(getViewLifecycleOwner(), ingredients -> {
            adapter.submitList(ingredients);
        });

        viewModel.getOwnedIngredients().observe(getViewLifecycleOwner(), ownedIngredients -> {
            if (ownedIngredients != null) {
                // Mettre à jour les sélections pour les ingrédients possédés
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