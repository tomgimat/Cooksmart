package fr.tomgimat.cooksmart.ui.ingredients;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.SearchView;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
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
        observeIngredients();
        
        return binding.getRoot();
    }

    private void setupRecyclerView() {
        adapter = new IngredientsAdapter();
        binding.recyclerViewIngredients.setLayoutManager(new LinearLayoutManager(getContext()));
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

    private void observeIngredients() {
        viewModel.getFilteredIngredients().observe(getViewLifecycleOwner(), ingredients -> {
            adapter.submitList(ingredients);
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 