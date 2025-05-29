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
import fr.tomgimat.cooksmart.ui.ingredients.IngredientsAdapter;
import fr.tomgimat.cooksmart.ui.ingredients.IngredientsSelectionViewModel;

public class MySpaceFragment extends Fragment {
    private FragmentMySpaceBinding binding;
    private IngredientsSelectionViewModel viewModel;
    private IngredientsAdapter ownedIngredientsAdapter;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentMySpaceBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this).get(IngredientsSelectionViewModel.class);
        
        setupRecyclerView();
        observeOwnedIngredients();
        setupIngredientsCard();
        
        return binding.getRoot();
    }

    private void setupRecyclerView() {
        ownedIngredientsAdapter = new IngredientsAdapter();
        binding.recyclerViewOwnedIngredients.setLayoutManager(new LinearLayoutManager(getContext()));
        binding.recyclerViewOwnedIngredients.setAdapter(ownedIngredientsAdapter);
    }

    private void setupIngredientsCard() {
        binding.cardIngredients.setOnClickListener(v -> {
            Navigation.findNavController(v).navigate(R.id.action_navigation_myspace_to_ingredientsSelectionFragment);
        });
    }

    private void observeOwnedIngredients() {
        viewModel.getOwnedIngredients().observe(getViewLifecycleOwner(), ingredients -> {
            if (ingredients != null && !ingredients.isEmpty()) {
                binding.textViewNoOwnedIngredients.setVisibility(View.GONE);
                binding.recyclerViewOwnedIngredients.setVisibility(View.VISIBLE);
                ownedIngredientsAdapter.submitList(ingredients);
            } else {
                binding.textViewNoOwnedIngredients.setVisibility(View.VISIBLE);
                binding.recyclerViewOwnedIngredients.setVisibility(View.GONE);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}