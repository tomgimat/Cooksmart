package fr.tomgimat.cooksmart.ui.adapter;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import fr.tomgimat.cooksmart.R;

public class RecipeImageAdapter<T extends RecipeDisplayable>
        extends RecyclerView.Adapter<RecipeImageAdapter.ViewHolder> {

    public interface OnRecipeClickListener<T> {
        void onRecipeClick(T recipe);
    }

    ////////////////////////////////////////////////////////////////

    private List<T> recipes = new ArrayList<>();
    private OnRecipeClickListener<T> listener;

    public RecipeImageAdapter(List<T> recipes, OnRecipeClickListener<T> listener) {
        this.recipes = recipes;
        this.listener = listener;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setRecipes(List<T> recipes) {
        this.recipes = recipes;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_meal_image, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        T recipe = recipes.get(position);
        holder.bind(recipe, listener);
    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }

    ////////////////////////////////////////////////////////////

    static class ViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageMeal;
        private TextView textName;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageMeal = itemView.findViewById(R.id.imageMeal);
            textName = itemView.findViewById(R.id.textName);
        }

        public <T extends RecipeDisplayable> void bind(T recipe, OnRecipeClickListener<T> listener) {
            Glide.with(itemView.getContext())
                    .load(recipe.getImageUrl())
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .centerCrop()
                    .into(imageMeal);

            textName.setText(recipe.getName());

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onRecipeClick(recipe);
            });
        }
    }
}

