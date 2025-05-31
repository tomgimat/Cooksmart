package fr.tomgimat.cooksmart.ui.home;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import fr.tomgimat.cooksmart.R;
import fr.tomgimat.cooksmart.data.firebase.firestore.FirestoreRecipe;
import fr.tomgimat.cooksmart.data.firebase.firestore.FirestoreVideo;
import fr.tomgimat.cooksmart.data.mealdb.Meal;
import fr.tomgimat.cooksmart.databinding.FragmentHomeBinding;
import fr.tomgimat.cooksmart.AuthActivity;

public class HomeFragment extends Fragment {

    private static final String TAG = "HomeFragment";
    private FragmentHomeBinding binding;
    private RecipeImageAdapter<FirestoreRecipe> customRecipeAdapter;
    private RecipeImageAdapter<Meal> randomMealAdapter;
    private List<Meal> randomMeals = new ArrayList<>();
    private List<FirestoreRecipe> customMeals = new ArrayList<>();
    private VideoAdapter videoAdapter;
    private List<FirestoreVideo> videos = new ArrayList<>();
    private HomeViewModel viewModel;
    private ActivityResultLauncher<String> requestPermissionLauncher;
    private ActivityResultLauncher<Uri> takePictureLauncher;
    private Uri currentPhotoUri;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        requestPermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.RequestPermission(),
            isGranted -> {
                if (isGranted) {
                    startCamera();
                } else {
                    Snackbar.make(binding.getRoot(), R.string.camera_permission_required, Snackbar.LENGTH_LONG).show();
                }
            }
        );

        takePictureLauncher = registerForActivityResult(
            new ActivityResultContracts.TakePicture(),
            success -> {
                if (success && currentPhotoUri != null) {
                    processImage(currentPhotoUri);
                }
            }
        );
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHomeBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(requireActivity()).get(HomeViewModel.class);

        setupScanButton();
        observeViewModel();

        boolean isUserLoggedIn = FirebaseAuth.getInstance().getCurrentUser() != null;

        // Compatible Recipes Adapter
        RecipeImageAdapter<FirestoreRecipe> compatibleRecipeAdapter = new RecipeImageAdapter<>(new ArrayList<>(), recipe -> {
            Bundle bundle = new Bundle();
            bundle.putString("recipe_id", recipe.id);
            Navigation.findNavController(binding.getRoot()).navigate(R.id.fragment_meal_detail, bundle);
        }, R.layout.item_meal_image);
        binding.rvCompatibleRecipes.setAdapter(compatibleRecipeAdapter);
        binding.rvCompatibleRecipes.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));

        viewModel.getCompatibleRecipes().observe(getViewLifecycleOwner(), recipes -> {
            compatibleRecipeAdapter.setRecipes(recipes);
            binding.textCompatibleRecipes.setVisibility(recipes.isEmpty() ? View.GONE : View.VISIBLE);
            binding.rvCompatibleRecipes.setVisibility(recipes.isEmpty() ? View.GONE : View.VISIBLE);
        });

        //Custom Recipe Adapter
        customRecipeAdapter = new RecipeImageAdapter<>(customMeals, recipe -> {
            Bundle bundle = new Bundle();
            bundle.putString("recipe_id", recipe.id);
            Navigation.findNavController(binding.getRoot()).navigate(R.id.fragment_meal_detail, bundle);
        }, R.layout.item_meal_image);
        binding.rvMeals.setAdapter(customRecipeAdapter);
        binding.rvMeals.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));

        viewModel.getSuggestedRecipes().observe(getViewLifecycleOwner(), recipes -> {
            customRecipeAdapter.setRecipes(recipes);
        });

        //Random Meal Adapter
        randomMealAdapter = new RecipeImageAdapter<>(randomMeals, randomMeal -> {
            if (FirebaseAuth.getInstance().getCurrentUser() != null) {
                Bundle bundle = new Bundle();
                bundle.putString("meal_id", randomMeal.id);
                Navigation.findNavController(binding.getRoot()).navigate(R.id.fragment_meal_detail, bundle);
            }
        }, R.layout.item_meal_image);
        binding.rvRandomMeals.setAdapter(randomMealAdapter);
        binding.rvRandomMeals.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));

        viewModel.getRandomMeals().observe(getViewLifecycleOwner(), meals -> {
            randomMealAdapter.setRecipes(meals);
        });

        // Video Adapter
        videoAdapter = new VideoAdapter(requireContext(), videos);
        binding.rvVideos.setAdapter(videoAdapter);
        binding.rvVideos.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.HORIZONTAL, false));

        viewModel.getVideos().observe(getViewLifecycleOwner(), videoList -> {
            videoAdapter.setVideos(videoList);
            Log.d("VideoAdapter", "Videos received: " + videoList.size());
        });

        viewModel.loadSuggestionsForCurrentUser();
        viewModel.fetchRandomMealsIfNeeded();
        viewModel.loadVideos();
        viewModel.loadCompatibleRecipes();

        /*
        Message d'accueil
         */
        final TextView textView = binding.textViewWelcomeHome;
        viewModel.getText().observe(getViewLifecycleOwner(), textView::setText);
        viewModel.updateWelcomeText();

        //        viewModel.cleanDuplicateIngredients();
        // Gérer l'affichage du message de connexion et cacher les éléments restreints si l'utilisateur n'est pas connecté
        if (!isUserLoggedIn) {
            binding.textViewAuthPrompt.setVisibility(View.VISIBLE);
            binding.fabScanIngredient.setVisibility(View.GONE);
            // Ne pas charger les vidéos et les recettes compatibles si déconnecté
            binding.rvVideos.setVisibility(View.GONE);
            binding.rvCompatibleRecipes.setVisibility(View.GONE);
            binding.textCompatibleRecipes.setVisibility(View.GONE);
            binding.textVideosToDiscover.setVisibility(View.GONE);
        } else {
            binding.textViewAuthPrompt.setVisibility(View.GONE);
            binding.fabScanIngredient.setVisibility(View.VISIBLE);
            // Charger les vidéos et les recettes compatibles si connecté
             binding.rvVideos.setVisibility(View.VISIBLE);
             binding.rvCompatibleRecipes.setVisibility(View.VISIBLE);
             binding.textVideosToDiscover.setVisibility(View.VISIBLE);
            // La visibilité de textCompatibleRecipes est gérée par l'observation du LiveData
        }

        // Configurer le bouton de prompt de connexion
        binding.textViewAuthPrompt.setOnClickListener(v -> {
            startActivity(new Intent(requireActivity(), AuthActivity.class));
        });

        return binding.getRoot();
    }

    /**
     * Setup du bouton de scan d'ingrédient
     */
    private void setupScanButton() {
        binding.fabScanIngredient.setOnClickListener(v -> {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA)
                    == PackageManager.PERMISSION_GRANTED) {
                startCamera();
            } else {
                requestPermissionLauncher.launch(Manifest.permission.CAMERA);
            }
        });
    }

    /**
     * Lancement de la caméra
     */
    private void startCamera() {
        try {
            currentPhotoUri = requireContext().getContentResolver().insert(
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                new android.content.ContentValues()
            );
            if (currentPhotoUri != null) {
                takePictureLauncher.launch(currentPhotoUri);
            }
        } catch (Exception e) {
            Log.e(TAG, "Erreur lors du lancement de la caméra", e);
            Toast.makeText(requireContext(), R.string.error_processing_image, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Traitement de l'image
     * @param photoUri
     */
    private void processImage(Uri photoUri) {
        try {
            InputStream inputStream = requireContext().getContentResolver().openInputStream(photoUri);
            if (inputStream != null) {
                Bitmap bitmap = BitmapFactory.decodeStream(inputStream);
                inputStream.close();
                viewModel.scanIngredient(bitmap);
            }
        } catch (IOException e) {
            Log.e(TAG, "Erreur lors du traitement de l'image", e);
            Toast.makeText(requireContext(), R.string.error_processing_image, Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Observe les changements du ViewModel
     */
    private void observeViewModel() {
        viewModel.getScanResult().observe(getViewLifecycleOwner(), result -> {
            if (result != null) {
                Snackbar.make(binding.getRoot(), result, Snackbar.LENGTH_LONG).show();
            }
        });

        viewModel.getIsScanning().observe(getViewLifecycleOwner(), isScanning -> {
            binding.fabScanIngredient.setEnabled(!isScanning);
            // TODO: Ajouter un indicateur de chargement si nécessaire
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        // Mettre à jour l'affichage en fonction de l'état de connexion à chaque reprise du fragment
        boolean isUserLoggedIn = FirebaseAuth.getInstance().getCurrentUser() != null;
        if (!isUserLoggedIn) {
            binding.textViewAuthPrompt.setVisibility(View.VISIBLE);
            binding.fabScanIngredient.setVisibility(View.GONE);
            binding.rvVideos.setVisibility(View.GONE);
            binding.rvCompatibleRecipes.setVisibility(View.GONE);
            binding.textCompatibleRecipes.setVisibility(View.GONE);
            binding.textVideosToDiscover.setVisibility(View.GONE);
        } else {
            binding.textViewAuthPrompt.setVisibility(View.GONE);
            binding.fabScanIngredient.setVisibility(View.VISIBLE);
             binding.rvVideos.setVisibility(View.VISIBLE);
             binding.rvCompatibleRecipes.setVisibility(View.VISIBLE);
             binding.textVideosToDiscover.setVisibility(View.VISIBLE);
            // La visibilité de textCompatibleRecipes est gérée par l'observation du LiveData

            // Recharger les données restreintes si l'utilisateur vient de se connecter
            HomeViewModel homeViewModel = new ViewModelProvider(this).get(HomeViewModel.class);
            homeViewModel.updateWelcomeText(); // Ceci devrait déjà être géré par le ViewModel
            homeViewModel.fetchRandomMealsIfNeeded(); // Ceci devrait déjà être géré par le ViewModel
            homeViewModel.loadVideos(); // Recharger les vidéos
            homeViewModel.loadCompatibleRecipes(); // Recharger les recettes compatibles
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}