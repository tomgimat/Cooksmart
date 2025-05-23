package fr.tomgimat.cooksmart.ui.home;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import fr.tomgimat.cooksmart.R;
import fr.tomgimat.cooksmart.data.firebase.firestore.FirestoreRecipe;
import fr.tomgimat.cooksmart.data.mealdb.Meal;
import fr.tomgimat.cooksmart.databinding.FragmentHomeBinding;
import fr.tomgimat.cooksmart.ui.adapter.RecipeImageAdapter;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private RecipeImageAdapter<FirestoreRecipe> customRecipeAdapter;
    private RecipeImageAdapter<Meal> randomMealAdapter;
    private List<Meal> randomMeals = new ArrayList<>();
    private List<FirestoreRecipe> customMeals = new ArrayList<>();

    public View onCreateView(@NonNull LayoutInflater inflater,
                             ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Init ViewModel
        HomeViewModel homeViewModel =
                new ViewModelProvider(requireActivity()).get(HomeViewModel.class);

        //Custom Recipe Adapter
        customRecipeAdapter = new RecipeImageAdapter<>(customMeals, recipe -> {
            Bundle bundle = new Bundle();
            bundle.putString("recipe_id", recipe.id);
            Navigation.findNavController(binding.getRoot()).navigate(R.id.fragment_meal_detail, bundle);
        });
        binding.rvMeals.setAdapter(customRecipeAdapter);
        binding.rvMeals.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));

        homeViewModel.getSuggestedRecipes().observe(getViewLifecycleOwner(), recipes -> {
            customRecipeAdapter.setRecipes(recipes);
        });

        //Random Meal Adapter
        randomMealAdapter = new RecipeImageAdapter<>(randomMeals, randomMeal -> {
            Bundle bundle = new Bundle();
            bundle.putString("meal_id", randomMeal.id);
            Navigation.findNavController(binding.getRoot()).navigate(R.id.fragment_meal_detail, bundle);
        });
        binding.rvRandomMeals.setAdapter(randomMealAdapter);
        binding.rvRandomMeals.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));


        homeViewModel.getRandomMeals().observe(getViewLifecycleOwner(), meals -> {
            randomMealAdapter.setRecipes(meals);
        });


        homeViewModel.loadSuggestionsForCurrentUser();
        homeViewModel.fetchRandomMealsIfNeeded();


        /*
        Message d'accueil
         */
        final TextView textView = binding.textViewWelcomeHome;
        homeViewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        homeViewModel.updateWelcomeText();
    }

    @Override
    public void onResume() {
        super.onResume();
        HomeViewModel homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
        homeViewModel.updateWelcomeText();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


}