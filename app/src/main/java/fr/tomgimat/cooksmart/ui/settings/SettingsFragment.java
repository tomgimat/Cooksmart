package fr.tomgimat.cooksmart.ui.settings;

import android.app.AlertDialog;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.util.Locale;

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
        setupLanguageButton();
        observeSettings();

        return binding.getRoot();
    }

    /**
     * Setup des boutons de préférences même si certains ne fonctionnent pas en l'état
     * TODO
     */
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


    private void setupLanguageButton() {
        binding.buttonChangeLanguage.setOnClickListener(v -> {
            String currentLanguage = Locale.getDefault().getLanguage();
            String newLanguage = currentLanguage.equals("en") ? "fr" : "en";

            Locale newLocale = new Locale(newLanguage);
            Locale.setDefault(newLocale);

            Resources resources = requireContext().getResources();
            Configuration config = resources.getConfiguration();
            config.setLocale(newLocale);
            resources.updateConfiguration(config, resources.getDisplayMetrics());

            // Redémarrer l'activité pour appliquer les changements
            requireActivity().recreate();
        });
    }

    /**
     * Observe les changements des préférences et met à jour les boutons en conséquence
     */
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
    }

    private void updateOfflineMode(boolean enabled) {
        // TODO: Implémenter la gestion du mode hors-ligne
    }

    private void clearSearchHistory() {
        // TODO: Implémenter la suppression de l'historique de recherche
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

    ////////////////////PARTIE RESET DES PARAMETRES/////////////////////

//    private void setupResetButton() {
//        binding.btnResetSettings.setOnClickListener(v -> showResetConfirmationDialog());
//    }

//    private void showResetConfirmationDialog() {
//        new AlertDialog.Builder(requireContext())
//                .setTitle(R.string.settings_reset)
//                .setMessage(R.string.settings_reset_confirm)
//                .setPositiveButton(android.R.string.yes, (dialog, which) -> {
//                    viewModel.resetSettings();
//                    Toast.makeText(requireContext(), R.string.settings_reset_success, Toast.LENGTH_SHORT).show();
//                })
//                .setNegativeButton(android.R.string.no, null)
//                .show();
//    }
} 