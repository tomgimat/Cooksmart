package fr.tomgimat.cooksmart.data.firebase.firestore;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.ArrayList;
import java.util.List;

import fr.tomgimat.cooksmart.R;

public class FirestoreRecipeAdapter extends RecyclerView.Adapter<FirestoreRecipeAdapter.ViewHolder> {
    private List<FirestoreRecipe> recipes = new ArrayList<>();

    public void setRecipes(List<FirestoreRecipe> recipes) {
        this.recipes = recipes;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_meal_image, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        FirestoreRecipe recipe = recipes.get(position);
        Glide.with(holder.itemView.getContext())
                .load(recipe.imageUrl)
                .centerCrop()
                .into(holder.imageMeal);

        holder.itemView.setOnClickListener(v -> {
            //Navigation vers le détail recette
        });
    }

    @Override
    public int getItemCount() {
        return recipes.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        ImageView imageMeal;

        ViewHolder(@NonNull View itemView) {
            super(itemView);
            imageMeal = itemView.findViewById(R.id.imageMeal);
        }
    }
}

