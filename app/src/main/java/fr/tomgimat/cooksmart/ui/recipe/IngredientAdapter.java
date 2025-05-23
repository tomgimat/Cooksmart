package fr.tomgimat.cooksmart.ui.recipe;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

import fr.tomgimat.cooksmart.R;

public class IngredientAdapter extends RecyclerView.Adapter<IngredientAdapter.IngredientViewHolder> {
    private List<String> ingredients;

    public IngredientAdapter(List<String> ingredients) {
        this.ingredients = ingredients;
    }

    public void setIngredients(List<String> newIngredients) {
        this.ingredients = newIngredients;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public IngredientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_ingredient, parent, false);
        return new IngredientViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull IngredientViewHolder holder, int position) {
        holder.bind(ingredients.get(position));
    }

    @Override
    public int getItemCount() {
        return ingredients != null ? ingredients.size() : 0;
    }

    static class IngredientViewHolder extends RecyclerView.ViewHolder {
        private TextView textIngredient;

        public IngredientViewHolder(@NonNull View itemView) {
            super(itemView);
            textIngredient = itemView.findViewById(R.id.textIngredient);
        }

        public void bind(String ingredient) {
            textIngredient.setText(ingredient);
        }
    }
}

