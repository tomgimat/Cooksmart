package fr.tomgimat.cooksmart.data;

import java.util.Arrays;
import java.util.List;

import fr.tomgimat.cooksmart.R;

public class DietaryPreference {
    public final String key;
    public final int checkBoxId;

    public DietaryPreference(String key, int checkBoxId) {
        this.key = key;
        this.checkBoxId = checkBoxId;
    }

    /**
     * Liste des préférences alimentaires disponibles
     */
    public static final List<DietaryPreference> ALL_PREFERENCES = Arrays.asList(
            new DietaryPreference("vegetarian", R.id.cbVegetarian),
            new DietaryPreference("vegan", R.id.cbVegan),
            new DietaryPreference("glutenFree", R.id.cbGlutenFree),
            new DietaryPreference("lactoseFree", R.id.cbLactoseFree),
            new DietaryPreference("lowSalt", R.id.cbLowSalt),
            new DietaryPreference("lowSugar", R.id.cbLowSugar),
            new DietaryPreference("pescetarian", R.id.cbPescetarian)
            // Ajoute ici facilement tes nouvelles prefs !
    );

}


