package fr.tomgimat.cooksmart.ui.recipe;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import fr.tomgimat.cooksmart.R;
import fr.tomgimat.cooksmart.data.model.IngredientDisplay;

public class IngredientAdapterForRecipeUI extends RecyclerView.Adapter<IngredientAdapterForRecipeUI.IngredientViewHolder> {
    private List<IngredientDisplay> ingredients;

    public IngredientAdapterForRecipeUI() {
        this.ingredients = new ArrayList<>();
    }

    public IngredientAdapterForRecipeUI(List<IngredientDisplay> initialIngredients) {
        this.ingredients = initialIngredients != null ? initialIngredients : new ArrayList<>();
    }

    public void setIngredientsWithOwnership(List<IngredientDisplay> ingredients) {
        this.ingredients = ingredients;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public IngredientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ingredient, parent, false);
        return new IngredientViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull IngredientViewHolder holder, int position) {
        IngredientDisplay ingredient = ingredients.get(position);
        holder.bind(ingredient);
    }

    @Override
    public int getItemCount() {
        return ingredients.size();
    }

    static class IngredientViewHolder extends RecyclerView.ViewHolder {
        private final TextView textIngredient;
        private final TextView textOwned;

        public IngredientViewHolder(@NonNull View itemView) {
            super(itemView);
            textIngredient = itemView.findViewById(R.id.textIngredient);
            textOwned = itemView.findViewById(R.id.textOwned);
        }

        public void bind(IngredientDisplay ingredient) {
            textIngredient.setText(ingredient.getText());
            textOwned.setVisibility(ingredient.isOwned() ? View.VISIBLE : View.GONE);
        }
    }
}

