package fr.tomgimat.cooksmart.ui.ingredients;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import fr.tomgimat.cooksmart.databinding.ItemIngredientCheckboxBinding;
import fr.tomgimat.cooksmart.data.firebase.firestore.Ingredient;

public class IngredientsAdapter extends ListAdapter<Ingredient, IngredientsAdapter.IngredientViewHolder> {
    private final IngredientsSelectionViewModel viewModel;

    public IngredientsAdapter(IngredientsSelectionViewModel viewModel) {
        super(new DiffUtil.ItemCallback<Ingredient>() {
            @Override
            public boolean areItemsTheSame(@NonNull Ingredient oldItem, @NonNull Ingredient newItem) {
                return oldItem.getId().equals(newItem.getId());
            }

            @Override
            public boolean areContentsTheSame(@NonNull Ingredient oldItem, @NonNull Ingredient newItem) {
                return oldItem.getName().equals(newItem.getName()) 
                    && oldItem.isSelected() == newItem.isSelected();
            }
        });
        this.viewModel = viewModel;
    }

    @NonNull
    @Override
    public IngredientViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemIngredientCheckboxBinding binding = ItemIngredientCheckboxBinding.inflate(
            LayoutInflater.from(parent.getContext()), parent, false);
        return new IngredientViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull IngredientViewHolder holder, int position) {
        holder.bind(getItem(position));
    }

    class IngredientViewHolder extends RecyclerView.ViewHolder {
        private final ItemIngredientCheckboxBinding binding;

        public IngredientViewHolder(ItemIngredientCheckboxBinding binding) {
            super(binding.getRoot());
            this.binding = binding;

            binding.checkBoxIngredient.setOnCheckedChangeListener((buttonView, isChecked) -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Ingredient ingredient = getItem(position);
                    viewModel.toggleIngredientSelection(ingredient);
                }
            });
        }

        public void bind(Ingredient ingredient) {
            binding.checkBoxIngredient.setText(ingredient.getName());

            // Temporairement retirer le listener pour éviter le déclenchement involontaire
            binding.checkBoxIngredient.setOnCheckedChangeListener(null);

            // Mettre à jour l'état de la CheckBox
            binding.checkBoxIngredient.setChecked(ingredient.isSelected());

            // Rattacher le listener
            binding.checkBoxIngredient.setOnCheckedChangeListener((buttonView, isChecked) -> {
                int position = getAdapterPosition();
                if (position != RecyclerView.NO_POSITION) {
                    Ingredient updatedIngredient = getItem(position);
                    viewModel.toggleIngredientSelection(updatedIngredient);
                }
            });
        }
    }
} 