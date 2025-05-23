package fr.tomgimat.cooksmart.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

import java.util.List;

import fr.tomgimat.cooksmart.R;
import fr.tomgimat.cooksmart.data.mealdb.Meal;

public class MealImageAdapter extends RecyclerView.Adapter<MealImageAdapter.MealViewHolder> {

    public interface OnMealClickListener {
        void onMealClick(Meal meal);
    }

    /////////////////////////////////////////////////////////////////////////

    private List<Meal> meals;
    private OnMealClickListener listener;

    public MealImageAdapter(List<Meal> meals, OnMealClickListener listener) {
        this.meals = meals;
        this.listener = listener;
    }

    @NonNull
    @Override
    public MealViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_meal_image, parent, false);
        return new MealViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull MealViewHolder holder, int position) {
        Meal meal = meals.get(position);
        holder.bind(meal, listener);
    }

    @Override
    public int getItemCount() {
        return meals != null ? meals.size() : 0;
    }

    /////////////////////////////////////////////////////////////

    static class MealViewHolder extends RecyclerView.ViewHolder {
        private ImageView imageMeal;

        public MealViewHolder(@NonNull View itemView) {
            super(itemView);
            imageMeal = itemView.findViewById(R.id.imageMeal);
        }

        public void bind(Meal meal, OnMealClickListener listener) {
            Glide.with(itemView.getContext())
                    .load(meal.imageUrl)
                    .placeholder(R.drawable.ic_launcher_foreground)
                    .into(imageMeal);

            itemView.setOnClickListener(v -> {
                if (listener != null) listener.onMealClick(meal);
            });
        }
    }
}

