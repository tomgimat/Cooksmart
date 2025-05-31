package fr.tomgimat.cooksmart.ui.recipe;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import fr.tomgimat.cooksmart.R;
import fr.tomgimat.cooksmart.data.model.RecipeStep;
import fr.tomgimat.cooksmart.databinding.FragmentStepByStepBinding;

public class StepByStepFragment extends Fragment {
    private static final String ARG_RECIPE_ID = "recipe_id";
    private StepByStepViewModel viewModel;
    private FragmentStepByStepBinding binding;

    public static StepByStepFragment newInstance(String recipeId) {
        StepByStepFragment fragment = new StepByStepFragment();
        Bundle args = new Bundle();
        args.putString(ARG_RECIPE_ID, recipeId);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        viewModel = new ViewModelProvider(this).get(StepByStepViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = FragmentStepByStepBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        // Initialiser les vues et configurer la RecyclerView
        binding.rvSteps.setLayoutManager(new LinearLayoutManager(getContext()));
        RecipeStepAdapter stepAdapter = new RecipeStepAdapter();
        binding.rvSteps.setAdapter(stepAdapter);

        binding.layoutNavigation.setVisibility(View.VISIBLE);

        binding.buttonStartTimer.setOnClickListener(v -> viewModel.startTimer());
        binding.buttonStopTimer.setOnClickListener(v -> viewModel.stopTimer());
        binding.buttonPrevious.setOnClickListener(v -> viewModel.previousStep());
        binding.buttonNext.setOnClickListener(v -> viewModel.nextStep());
        binding.buttonSpeak.setOnClickListener(v -> viewModel.speakCurrentStep());

        //Observation des changements dans les étapes et changement dans l'affichage
        viewModel.getSteps().observe(getViewLifecycleOwner(), steps -> {
            stepAdapter.setSteps(steps);
            updateStepDisplay();
            updateNavigationButtons();
        });

        viewModel.getCurrentStepIndex().observe(getViewLifecycleOwner(), index -> {
            stepAdapter.setCurrentStepIndex(index);
            updateStepDisplay();
            updateNavigationButtons();
            //défiler la RecyclerView jusqu'à l'étape courante
            binding.rvSteps.smoothScrollToPosition(index);
        });

        viewModel.getIsTimerRunning().observe(getViewLifecycleOwner(), isRunning -> {
            binding.buttonStartTimer.setVisibility(isRunning ? View.GONE : View.VISIBLE);
            binding.buttonStopTimer.setVisibility(isRunning ? View.VISIBLE : View.GONE);
        });

        viewModel.getRemainingTime().observe(getViewLifecycleOwner(), seconds -> {
            if (seconds != null && seconds > 0) {
                long minutes = seconds / 60;
                long remainingSeconds = seconds % 60;
                binding.textTimer.setText(getString(R.string.timer_format, minutes, remainingSeconds));
                binding.layoutTimerContainer.setVisibility(View.VISIBLE);
            } else {
                binding.layoutTimerContainer.setVisibility(View.GONE);
            }
        });

        //visibilité du ProgressBar
        viewModel.getSteps().observe(getViewLifecycleOwner(), steps -> {
            binding.progressBar.setVisibility(steps == null || steps.isEmpty() ? View.VISIBLE : View.GONE);
        });

        // Chargement des étapes
        String recipeId = getArguments().getString(ARG_RECIPE_ID);
        if (recipeId != null) {
            viewModel.loadRecipeSteps(recipeId);
        }
    }

    /**
     * Met à jour l'affichage de l'étape courante selon le viewmodel
     */
    private void updateStepDisplay() {
        List<RecipeStep> steps = viewModel.getSteps().getValue();
        Integer currentIndex = viewModel.getCurrentStepIndex().getValue();

        //minuteur uniquement pour l'étape courante s'il y en a un
        if (steps != null && currentIndex != null && currentIndex < steps.size()) {
            RecipeStep currentStep = steps.get(currentIndex);
            //Le layoutTimer est géré par l'observation de getRemainingTime dans onViewCreated
            //on s'assure juste que le bon état du bouton start/stop est affiché
            if (currentStep.isTimerStep()) {
                 if (viewModel.getIsTimerRunning().getValue() != null) {
                    binding.buttonStartTimer.setVisibility(viewModel.getIsTimerRunning().getValue() ? View.GONE : View.VISIBLE);
                    binding.buttonStopTimer.setVisibility(viewModel.getIsTimerRunning().getValue() ? View.VISIBLE : View.GONE);
                 }
            } else {
                //Pas de bouton du minuteur si l'étape n'en a pas
                binding.buttonStartTimer.setVisibility(View.GONE);
                binding.buttonStopTimer.setVisibility(View.GONE);
            }
        } else {
            binding.layoutTimerContainer.setVisibility(View.GONE);
            binding.buttonStartTimer.setVisibility(View.GONE);
            binding.buttonStopTimer.setVisibility(View.GONE);
        }
    }

    /**
     * Met à jour les boutons de navigation selon le viewmodel
     */
    private void updateNavigationButtons() {
        List<RecipeStep> steps = viewModel.getSteps().getValue();
        Integer currentIndex = viewModel.getCurrentStepIndex().getValue();

        if (steps != null && currentIndex != null) {
            binding.buttonPrevious.setEnabled(currentIndex > 0);
            binding.buttonNext.setEnabled(currentIndex < steps.size() - 1);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 