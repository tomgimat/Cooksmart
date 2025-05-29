package fr.tomgimat.cooksmart.data.firebase.firestore;

public class Ingredient {
    private String id;
    private String name;
    private String normalizedName;
    private boolean isSelected;

    public Ingredient() {
        // Required empty constructor for Firestore
    }

    public Ingredient(String id, String name) {
        this.id = id;
        this.name = name;
        this.normalizedName = normalizeString(name);
        this.isSelected = false;
    }

    /**
     * Enleve tout ce qui peut nuire à la recherche d'ingrédients
     * @param input
     * @return
     */
    private String normalizeString(String input) {
        return input.toLowerCase()
            .replaceAll("[éèêë]", "e")
            .replaceAll("[àâä]", "a")
            .replaceAll("[ùûü]", "u")
            .replaceAll("[îï]", "i")
            .replaceAll("[ôö]", "o")
            .replaceAll("[ç]", "c");
    }

    // Getters and Setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { 
        this.name = name;
        this.normalizedName = normalizeString(name);
    }

    public String getNormalizedName() { return normalizedName; }
    public void setNormalizedName(String normalizedName) { this.normalizedName = normalizedName; }

    public boolean isSelected() { return isSelected; }
    public void setSelected(boolean selected) { isSelected = selected; }
} 