package fr.tomgimat.cooksmart.data.firebase.firestore;

import java.util.List;

public class UserIngredients {
    private String userId;
    private List<String> ingredientIds;

    public UserIngredients() {
        // Constructeur vide requis pour Firestore
    }

    public UserIngredients(String userId, List<String> ingredientIds) {
        this.userId = userId;
        this.ingredientIds = ingredientIds;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<String> getIngredientIds() {
        return ingredientIds;
    }

    public void setIngredientIds(List<String> ingredientIds) {
        this.ingredientIds = ingredientIds;
    }
} 