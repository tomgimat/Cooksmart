package fr.tomgimat.cooksmart.ui.recipe;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import fr.tomgimat.cooksmart.R;
import fr.tomgimat.cooksmart.data.model.RecipeStep;

public class RecipeStepAdapter extends RecyclerView.Adapter<RecipeStepAdapter.StepViewHolder> {
    private List<RecipeStep> steps = new ArrayList<>();
    private int currentStepIndex = 0;

    public void setSteps(List<RecipeStep> steps) {
        this.steps = steps;
        notifyDataSetChanged();
    }

    public void setCurrentStepIndex(int index) {
        int oldIndex = this.currentStepIndex;
        this.currentStepIndex = index;
        notifyItemChanged(oldIndex);
        notifyItemChanged(currentStepIndex);
    }

    @NonNull
    @Override
    public StepViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_recipe_step, parent, false);
        return new StepViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull StepViewHolder holder, int position) {
        RecipeStep step = steps.get(position);
        holder.textStepNumber.setText(String.format("Étape %d", position + 1));
        holder.textStepInstruction.setText(step.getInstruction());

        //On change l'affichage de l'étape courante
        if (position == currentStepIndex) {
            float elevationPx = 8 * holder.itemView.getContext().getResources().getDisplayMetrics().density;
            holder.cardView.setCardElevation(elevationPx);
            holder.cardView.setBackgroundColor(holder.itemView.getContext().getColor(R.color.colorTertiaryPurple));
            holder.textStepNumber.setTextColor(holder.itemView.getContext().getColor(android.R.color.white));
            holder.textStepInstruction.setTextColor(holder.itemView.getContext().getColor(android.R.color.white));
        } else {
            float elevationPx = 2 * holder.itemView.getContext().getResources().getDisplayMetrics().density;
            holder.cardView.setCardElevation(elevationPx);
            holder.cardView.setBackgroundColor(holder.itemView.getContext().getColor(android.R.color.white));
            holder.textStepNumber.setTextColor(holder.itemView.getContext().getColor(android.R.color.black));
            holder.textStepInstruction.setTextColor(holder.itemView.getContext().getColor(android.R.color.black));
        }
    }

    @Override
    public int getItemCount() {
        return steps.size();
    }

    static class StepViewHolder extends RecyclerView.ViewHolder {
        CardView cardView;
        TextView textStepNumber;
        TextView textStepInstruction;

        StepViewHolder(@NonNull View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardStepItem);
            textStepNumber = itemView.findViewById(R.id.textStepNumberItem);
            textStepInstruction = itemView.findViewById(R.id.textStepInstructionItem);
        }
    }
} 