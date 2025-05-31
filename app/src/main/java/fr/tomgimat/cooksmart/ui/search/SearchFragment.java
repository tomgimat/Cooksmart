package fr.tomgimat.cooksmart.ui.search;

import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.GridLayoutManager;

import java.util.ArrayList;

import fr.tomgimat.cooksmart.R;
import fr.tomgimat.cooksmart.data.firebase.firestore.FirestoreRecipe;
import fr.tomgimat.cooksmart.databinding.FragmentSearchBinding;
import fr.tomgimat.cooksmart.ui.home.RecipeImageAdapter;

public class SearchFragment extends Fragment {

    private FragmentSearchBinding binding;
    private SearchViewModel viewModel;
    private RecipeImageAdapter<FirestoreRecipe> adapter;

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSearchBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        viewModel = new ViewModelProvider(this).get(SearchViewModel.class);

        adapter = new RecipeImageAdapter<>(new ArrayList<>(), recipe -> {
            // navigation vers détail recette
            Bundle bundle = new Bundle();
            bundle.putString("recipe_id", recipe.id);
            Navigation.findNavController(binding.getRoot()).navigate(R.id.fragment_meal_detail, bundle);
        }, R.layout.item_recipe_grid);

        // Ecoute search text
        binding.editSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                viewModel.setSearchQuery(s.toString());
            }

            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        // Filtres diététiques
        binding.chipVegetarian.setOnCheckedChangeListener((btn, checked) -> {
            Filters f = viewModel.getFilters();
            f.vegetarian = checked ? true : null; // null = désactivé
            viewModel.setFilters(f);
        });
        binding.chipVegan.setOnCheckedChangeListener((btn, checked) -> {
            Filters f = viewModel.getFilters();
            f.vegan = checked ? true : null;
            viewModel.setFilters(f);
        });
        binding.chipGlutenFree.setOnCheckedChangeListener((btn, checked) -> {
            Filters f = viewModel.getFilters();
            f.glutenFree = checked ? true : null;
            viewModel.setFilters(f);
        });
        binding.chipLactoseFree.setOnCheckedChangeListener((btn, checked) -> {
            Filters f = viewModel.getFilters();
            f.lactoseFree = checked ? true : null;
            viewModel.setFilters(f);
        });

        binding.chipLowSalt.setOnCheckedChangeListener((btn, checked) -> {
            Filters f = viewModel.getFilters();
            f.lowSalt = checked ? true : null;
            viewModel.setFilters(f);
        });

        binding.chipLowSugar.setOnCheckedChangeListener((btn, checked) -> {
            Filters f = viewModel.getFilters();
            f.lowSugar = checked ? true : null;
            viewModel.setFilters(f);
        });

        binding.chipPescetarian.setOnCheckedChangeListener((btn, checked) -> {
            Filters f = viewModel.getFilters();
            f.pescetarian = checked ? true : null;
            viewModel.setFilters(f);
        });

        binding.chipHalal.setOnCheckedChangeListener((btn, checked) -> {
            Filters f = viewModel.getFilters();
            f.halal = checked ? true : null;
            viewModel.setFilters(f);
        });

        // Slider nombre d'ingrédients
        binding.sliderIngredients.addOnChangeListener((slider, value, fromUser) -> {
            Filters f = viewModel.getFilters();
            f.maxIngredients = (int) value;
            viewModel.setFilters(f);
            binding.textNbIngredients.setText(getString(R.string.filter_ingredients, f.maxIngredients));
        });

        // Slider durée
        binding.sliderDuration.addOnChangeListener((slider, value, fromUser) -> {
            Filters f = viewModel.getFilters();
            f.maxDuration = (int) value;
            viewModel.setFilters(f);
            binding.textDurationFilter.setText(getString(R.string.filter_duration, f.maxDuration));
        });

        // Observe les résultats filtrés
        viewModel.getFilteredRecipes().observe(getViewLifecycleOwner(), recipes -> {
            adapter.setRecipes(recipes); // Ajoute une méthode dans ton adapter pour refresh la liste
        });

        binding.rvSearchResults.setAdapter(adapter);
        binding.rvSearchResults.setLayoutManager(new GridLayoutManager(getContext(), 3));

    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}