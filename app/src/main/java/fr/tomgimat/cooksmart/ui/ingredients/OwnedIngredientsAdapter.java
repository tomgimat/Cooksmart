package fr.tomgimat.cooksmart.ui.ingredients;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import fr.tomgimat.cooksmart.R;
import fr.tomgimat.cooksmart.data.firebase.firestore.Ingredient;
import fr.tomgimat.cooksmart.databinding.ItemOwnedIngredientBinding;

public class OwnedIngredientsAdapter extends ListAdapter<Ingredient, OwnedIngredientsAdapter.OwnedIngredientViewHolder> {
    private final OnIngredientRemovedListener listener;

    public interface OnIngredientRemovedListener {
        void onIngredientRemoved(Ingredient ingredient);
    }

    public OwnedIngredientsAdapter(OnIngredientRemovedListener listener) {
        super(new DiffUtil.ItemCallback<Ingredient>() {
            @Override
            public boolean areItemsTheSame(@NonNull Ingredient oldItem, @NonNull Ingredient newItem) {
                return oldItem.getId().equals(newItem.getId());
            }

            @Override
            public boolean areContentsTheSame(@NonNull Ingredient oldItem, @NonNull Ingredient newItem) {
                return oldItem.getName().equals(newItem.getName());
            }
        });
        this.listener = listener;
    }

    @NonNull
    @Override
    public OwnedIngredientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemOwnedIngredientBinding binding = ItemOwnedIngredientBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false);
        return new OwnedIngredientViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull OwnedIngredientViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class OwnedIngredientViewHolder extends RecyclerView.ViewHolder {
        private final ItemOwnedIngredientBinding binding;

        OwnedIngredientViewHolder(ItemOwnedIngredientBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        /**
         * Associe un ingrédient à la vue
         * @param ingredient
         */
        void bind(Ingredient ingredient) {
            binding.textViewIngredientName.setText(ingredient.getName());
            binding.buttonRemove.setOnClickListener(v -> {
                if (listener != null) {
                    listener.onIngredientRemoved(ingredient);
                }
            });
        }
    }
} 