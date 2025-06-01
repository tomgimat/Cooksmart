package fr.tomgimat.cooksmart.ui.settings;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

public class SettingsViewModel extends ViewModel {
    private static final String PREFS_NAME = "cooksmart_settings";
    private final SharedPreferences preferences;
    private final MutableLiveData<Boolean> notifications = new MutableLiveData<>();
    private final MutableLiveData<Boolean> offlineMode = new MutableLiveData<>();
    private final MutableLiveData<Boolean> cookingTimer = new MutableLiveData<>();
    private final MutableLiveData<Boolean> searchHistory = new MutableLiveData<>();
    private final MutableLiveData<Boolean> smartSuggestions = new MutableLiveData<>();

    public SettingsViewModel(Context context) {
        preferences = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        loadSettings();
    }

    /**
     * Charge les préférences de l'application depuis les SharedPreferences.
     */
    private void loadSettings() {
        notifications.setValue(preferences.getBoolean("notifications", true));
        offlineMode.setValue(preferences.getBoolean("offline_mode", false));
        cookingTimer.setValue(preferences.getBoolean("cooking_timer", true));
        searchHistory.setValue(preferences.getBoolean("search_history", true));
        smartSuggestions.setValue(preferences.getBoolean("smart_suggestions", true));
    }

    /**
     * Enregistre les préférences de l'application dans les SharedPreferences.
     * TODO les modes ne marchent pas
     */
    public void saveSettings() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.putBoolean("notifications", notifications.getValue() != null && notifications.getValue());
        editor.putBoolean("offline_mode", offlineMode.getValue() != null && offlineMode.getValue());
        editor.putBoolean("cooking_timer", cookingTimer.getValue() != null && cookingTimer.getValue());
        editor.putBoolean("search_history", searchHistory.getValue() != null && searchHistory.getValue());
        editor.putBoolean("smart_suggestions", smartSuggestions.getValue() != null && smartSuggestions.getValue());
        editor.apply();
    }

    /**
     * Réinitialise les préférences de l'application. Non fonctionnel
     */
    public void resetSettings() {
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear();
        editor.apply();
        loadSettings();
    }

    // Getters et Setters pour chaque paramètre
    public LiveData<Boolean> getNotifications() { return notifications; }
    public void setNotifications(boolean value) { notifications.setValue(value); }

    public LiveData<Boolean> getOfflineMode() { return offlineMode; }
    public void setOfflineMode(boolean value) { offlineMode.setValue(value); }

    public LiveData<Boolean> getCookingTimer() { return cookingTimer; }
    public void setCookingTimer(boolean value) { cookingTimer.setValue(value); }

    public LiveData<Boolean> getSearchHistory() { return searchHistory; }
    public void setSearchHistory(boolean value) { searchHistory.setValue(value); }

    public LiveData<Boolean> getSmartSuggestions() { return smartSuggestions; }
    public void setSmartSuggestions(boolean value) { smartSuggestions.setValue(value); }
} 