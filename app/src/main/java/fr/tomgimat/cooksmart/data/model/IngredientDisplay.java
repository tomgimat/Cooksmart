package fr.tomgimat.cooksmart.data.model;

public class IngredientDisplay {
    private final String text;
    private final boolean isOwned;

    public IngredientDisplay(String text, boolean isOwned) {
        this.text = text;
        this.isOwned = isOwned;
    }

    public String getText() {
        return text;
    }

    public boolean isOwned() {
        return isOwned;
    }
} 