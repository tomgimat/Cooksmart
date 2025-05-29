package fr.tomgimat.cooksmart.ui.settings;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import fr.tomgimat.cooksmart.R;
import fr.tomgimat.cooksmart.databinding.FragmentSettingsBinding;

public class SettingsFragment extends Fragment {
    private FragmentSettingsBinding binding;
    private SettingsViewModel viewModel;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentSettingsBinding.inflate(inflater, container, false);
        viewModel = new ViewModelProvider(this, new SettingsViewModelFactory(requireContext()))
                .get(SettingsViewModel.class);

        setupSwitches();
        setupResetButton();
        observeSettings();

        return binding.getRoot();
    }

    private void setupSwitches() {
        // Préférences Générales
        binding.switchNotifications.setOnCheckedChangeListener((buttonView, isChecked) -> {
            viewModel.setNotifications(isChecked);
            viewModel.saveSettings();
            updateNotifications(isChecked);
        });

        binding.switchOfflineMode.setOnCheckedChangeListener((buttonView, isChecked) -> {
            viewModel.setOfflineMode(isChecked);
            viewModel.saveSettings();
            updateOfflineMode(isChecked);
        });

        // Préférences de Cuisine
        binding.switchCookingTimer.setOnCheckedChangeListener((buttonView, isChecked) -> {
            viewModel.setCookingTimer(isChecked);
            viewModel.saveSettings();
        });

        // Préférences de Recherche
        binding.switchSearchHistory.setOnCheckedChangeListener((buttonView, isChecked) -> {
            viewModel.setSearchHistory(isChecked);
            viewModel.saveSettings();
            if (!isChecked) {
                clearSearchHistory();
            }
        });

        binding.switchSmartSuggestions.setOnCheckedChangeListener((buttonView, isChecked) -> {
            viewModel.setSmartSuggestions(isChecked);
            viewModel.saveSettings();
        });
    }

    private void setupResetButton() {
        binding.btnResetSettings.setOnClickListener(v -> showResetConfirmationDialog());
    }

    private void showResetConfirmationDialog() {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.settings_reset)
                .setMessage(R.string.settings_reset_confirm)
                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
                    viewModel.resetSettings();
                    Toast.makeText(requireContext(), R.string.settings_reset_success, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton(android.R.string.no, null)
                .show();
    }

    private void observeSettings() {
        viewModel.getNotifications().observe(getViewLifecycleOwner(), isChecked -> 
            binding.switchNotifications.setChecked(isChecked != null && isChecked));
        
        viewModel.getOfflineMode().observe(getViewLifecycleOwner(), isChecked -> 
            binding.switchOfflineMode.setChecked(isChecked != null && isChecked));
        
        viewModel.getCookingTimer().observe(getViewLifecycleOwner(), isChecked -> 
            binding.switchCookingTimer.setChecked(isChecked != null && isChecked));
        
        viewModel.getSearchHistory().observe(getViewLifecycleOwner(), isChecked -> 
            binding.switchSearchHistory.setChecked(isChecked != null && isChecked));
        
        viewModel.getSmartSuggestions().observe(getViewLifecycleOwner(), isChecked -> 
            binding.switchSmartSuggestions.setChecked(isChecked != null && isChecked));
    }

    private void updateNotifications(boolean enabled) {
        // TODO: Implémenter la gestion des notifications
        // Cela nécessitera probablement de configurer les canaux de notification
    }

    private void updateOfflineMode(boolean enabled) {
        // TODO: Implémenter la gestion du mode hors-ligne
        // Cela nécessitera probablement de gérer le cache des données
    }

    private void clearSearchHistory() {
        // TODO: Implémenter la suppression de l'historique de recherche
        // Cela nécessitera probablement de nettoyer la base de données locale
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
} 