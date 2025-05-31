package fr.tomgimat.cooksmart.data.model;

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

    public static final DietaryPreference VEGETARIAN = new DietaryPreference("vegetarian", R.id.cbVegetarian);
    public static final DietaryPreference GLUTEN_FREE = new DietaryPreference("glutenFree", R.id.cbGlutenFree);
    public static final DietaryPreference LACTOSE_FREE = new DietaryPreference("lactoseFree", R.id.cbLactoseFree);
    public static final DietaryPreference LOW_SUGAR = new DietaryPreference("lowSugar", R.id.cbLowSugar);
    public static final DietaryPreference VEGAN = new DietaryPreference("vegan", R.id.cbVegan);
    public static final DietaryPreference PESCETARIAN = new DietaryPreference("pescetarian", R.id.cbPescetarian);
    public static final DietaryPreference LOW_SALT = new DietaryPreference("lowSalt", R.id.cbLowSalt);
    public static final DietaryPreference HALAL = new DietaryPreference("halal", R.id.cbHalal);

    /**
     * Liste des préférences alimentaires disponibles
     */
    public static final List<DietaryPreference> ALL_PREFERENCES = Arrays.asList(
            VEGETARIAN,
            GLUTEN_FREE,
            LACTOSE_FREE,
            LOW_SUGAR,
            VEGAN,
            PESCETARIAN,
            LOW_SALT,
            HALAL
    );

}


