package fr.tomgimat.cooksmart.ui.settings;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;

/**
 * Factory permettant de créer une instance de SettingsViewModel avec un contexte.
 * Cette Factory est nécessaire car SettingsViewModel nécessite un Context dans son constructeur
 * pour accéder aux SharedPreferences. Le ViewModelProvider standard ne peut pas créer directement
 * un ViewModel avec des paramètres dans son constructeur.
 */
public class SettingsViewModelFactory implements ViewModelProvider.Factory {
    private final Context context;

    /**
     * Constructeur de la Factory
     * @param context Le contexte de l'application. Sera converti en ApplicationContext
     *               pour éviter les fuites de mémoire.
     */
    public SettingsViewModelFactory(Context context) {
        // Utilisation du contexte de l'application plutôt que le contexte de l'activité
        this.context = context.getApplicationContext();
    }

    /**
     * Méthode appelée par ViewModelProvider pour créer un nouveau ViewModel.
     * @param modelClass La classe du ViewModel à créer
     * @throws IllegalArgumentException si la classe demandée n'est pas SettingsViewModel
     */
    @NonNull
    @Override
    public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {
        if (modelClass.isAssignableFrom(SettingsViewModel.class)) {
            return (T) new SettingsViewModel(context);
        }
        throw new IllegalArgumentException("Unknown ViewModel class");
    }
} 