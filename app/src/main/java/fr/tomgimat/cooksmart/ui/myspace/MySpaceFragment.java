package fr.tomgimat.cooksmart.ui.myspace;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import fr.tomgimat.cooksmart.R;
import fr.tomgimat.cooksmart.databinding.FragmentMySpaceBinding;
import fr.tomgimat.cooksmart.ui.ingredients.OwnedIngredientsAdapter;
import fr.tomgimat.cooksmart.ui.ingredients.IngredientsSelectionViewModel;
import fr.tomgimat.cooksmart.data.firebase.firestore.Ingredient;

public class MySpaceFragment extends Fragment implements OwnedIngredientsAdapter.OnIngredientRemovedListener {
    private FragmentMySpaceBinding binding;
    private IngredientsSelectionViewModel viewModel;
    private OwnedIngredientsAdapter ownedIngredientsAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentMySpaceBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(IngredientsSelectionViewModel.class);
        
        setupRecyclerView();
        observeOwnedIngredients();
        setupIngredientsCard();
        
        return binding.getRoot();
    }

    /**
     * Configure le RecyclerView pour afficher les ingrédients possédés
     */
    private void setupRecyclerView() {
        ownedIngredientsAdapter = new OwnedIngredientsAdapter(this);
        binding.recyclerViewOwnedIngredients.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewOwnedIngredients.setAdapter(ownedIngredientsAdapter);
    }

    /**
     * Configure la card Ingredients pour naviguer vers la sélection d'ingrédients
     */
    private void setupIngredientsCard() {
        binding.cardIngredients.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_navigation_myspace_to_ingredientsSelectionFragment);
        });

        binding.btnResetIngredients.setOnClickListener(v -> {
            viewModel.resetIngredientsList();
        });
    }

    /**
     * Observe les ingrédients possédés et met à jour la vue en conséquence
     */
    private void observeOwnedIngredients() {
        viewModel.getOwnedIngredients().observe(getViewLifecycleOwner(), ingredients -> {
            if (ingredients != null && !ingredients.isEmpty()) {
                binding.textViewNoOwnedIngredients.setVisibility(View.GONE);
                binding.recyclerViewOwnedIngredients.setVisibility(View.VISIBLE);
                binding.btnResetIngredients.setVisibility(View.VISIBLE);
                ownedIngredientsAdapter.submitList(ingredients);
            } else {
                binding.textViewNoOwnedIngredients.setVisibility(View.VISIBLE);
                binding.recyclerViewOwnedIngredients.setVisibility(View.GONE);
                binding.btnResetIngredients.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onIngredientRemoved(Ingredient ingredient) {
        viewModel.removeIngredient(ingredient);
    }

    @Override
    public void onResume() {
        super.onResume();
        viewModel.loadIngredients();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}