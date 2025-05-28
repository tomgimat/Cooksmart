package fr.tomgimat.cooksmart.ui.profile;

import static android.content.Context.MODE_PRIVATE;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;


import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import fr.tomgimat.cooksmart.MainActivity;
import fr.tomgimat.cooksmart.R;
import fr.tomgimat.cooksmart.data.DietaryPreference;
import fr.tomgimat.cooksmart.data.firebase.firestore.FirestoreRecipe;
import fr.tomgimat.cooksmart.databinding.FragmentProfileBinding;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private Map<String, CheckBox> prefCheckBoxes;
    private Button btnSavePrefs;
    private RecyclerView savedRecipesRecyclerView;
    private List<String> savedRecipeIds;
    private SavedRecipesAdapter savedRecipesAdapter;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentProfileBinding.inflate(inflater, container, false);
        View root = binding.getRoot();
        return root;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        btnSavePrefs = binding.btnSavePrefs;

        // Initialisation du RecyclerView pour les recettes sauvegardées
        savedRecipesRecyclerView = binding.savedRecipesRecyclerView;
        savedRecipesRecyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        savedRecipesRecyclerView.setHasFixedSize(true);
        savedRecipesAdapter = new SavedRecipesAdapter(new ArrayList<>());
        savedRecipesRecyclerView.setAdapter(savedRecipesAdapter);

        // Charger les recettes sauvegardées
        loadSavedRecipes();

        /*
        Map toutes les préférences dans le layout
         */
        prefCheckBoxes = new HashMap<>();
        for (DietaryPreference pref : DietaryPreference.ALL_PREFERENCES) {
            CheckBox checkBox = view.findViewById(pref.checkBoxId);
            if (checkBox != null) {
                prefCheckBoxes.put(pref.key, checkBox);
            }
        }

        setPrefsEnabled(false);

        /**
         * Charger les préférences de l'utilisateur
         */
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DocumentReference userProfile = db.collection("users").document(uid);

        userProfile.addSnapshotListener((snapshot, err) -> {
            if (snapshot != null && snapshot.exists()) {
                Map<String, Boolean> prefs = (Map) snapshot.get("preferences");
                if (prefs != null) {
                    applyPreferences(prefs);
                }

                String pseudo = snapshot.getString("pseudo");
                String email = snapshot.getString("email");
                String dateNaissance = snapshot.getString("dateNaissance");
                Map<String, String> userData = new HashMap<>();
                userData.put("pseudo", pseudo);
                userData.put("email", email);
                userData.put("dateNaissance", dateNaissance);
                loadUsersInfos(userData);
            }
        });

        userProfile.get()
                .addOnSuccessListener(doc -> {
                    if (doc.exists()) {
                        Map<String, Boolean> prefs = (Map<String, Boolean>) doc.get("preferences");
                        if (prefs != null) {
                            applyPreferences(prefs);

                        }
                        setPrefsEnabled(true);
                        String pseudo = doc.getString("pseudo");
                        String email = doc.getString("email");
                        String dateNaissance = doc.getString("dateNaissance");
                        binding.usernameView.setText(pseudo);
                        binding.emailView.setText(email);
                        binding.dobView.setText(dateNaissance);
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                });


        /*
        Sauvegarder les préférences utilisateurs (régime alimentaire)
         */
        binding.btnSavePrefs.setOnClickListener(v -> {
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (user == null) return; // sécurité

            Map<String, Object> prefsToSave = new HashMap<>();
            for (Map.Entry<String, CheckBox> entry : prefCheckBoxes.entrySet()) {
                prefsToSave.put(entry.getKey(), entry.getValue().isChecked());
            }

            userProfile
                    .set(Collections.singletonMap("preferences", prefsToSave), SetOptions.merge())
                    .addOnSuccessListener(a ->
                            Toast.makeText(getContext(), R.string.prefs_saved, Toast.LENGTH_SHORT).show())
                    .addOnFailureListener(e ->
                            Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show());
        });


        binding.btnLogout.setOnClickListener(v -> signOut());

    }

    /**
     * Appliquer les préférences de l'utilisateur
     *
     * @param prefs
     */
    private void applyPreferences(Map<String, Boolean> prefs) {
        for (Map.Entry<String, CheckBox> entry : prefCheckBoxes.entrySet()) {
            Boolean value = prefs.get(entry.getKey());
            entry.getValue().setChecked(value != null && value);
        }
    }

    /**
     * Activer ou désactiver les champs de préférences alimentaire
     *
     * @param enabled
     */
    private void setPrefsEnabled(boolean enabled) {
        for (CheckBox cb : prefCheckBoxes.values()) {
            cb.setEnabled(enabled);
        }
        btnSavePrefs.setEnabled(enabled);
    }

    /**
     * Charger les informations de l'utilisateur (pseudo, email, date de naissance)
     *
     * @param userData
     */
    private void loadUsersInfos(Map<String, String> userData) {
        binding.usernameView.setText(userData.get("pseudo"));
        binding.emailView.setText(userData.get("email"));
        binding.dobView.setText(userData.get("dateNaissance"));

    }

    /**
     * Charger les recettes sauvegardées de l'utilisateur
     */
    private void loadSavedRecipes() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();

        db.collection("saved_recipes").document(uid)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    if (documentSnapshot.exists()) {
                        List<String> recipeIds = (List<String>) documentSnapshot.get("recipe_ids");
                        if (recipeIds != null && !recipeIds.isEmpty()) {
                            loadRecipeDetails(recipeIds);
                        } else {
                            Log.d("ProfileFragment", "Aucune recette sauvegardée");
                            savedRecipesAdapter.setRecipes(new ArrayList<>());
                        }
                    } else {
                        savedRecipesAdapter.setRecipes(new ArrayList<>());
                    }
                })
                .addOnFailureListener(e -> {
                    Toast.makeText(getContext(), "Erreur lors du chargement des recettes sauvegardées", Toast.LENGTH_SHORT).show();
                    savedRecipesAdapter.setRecipes(new ArrayList<>());
                });
    }

    /**
     * Charger les détails de chaque recette sauvegardée
     *
     * @param recipeIds
     */
    private void loadRecipeDetails(List<String> recipeIds) {
        if (recipeIds.isEmpty()) {
            Log.d("ProfileFragment", "Liste de recettes vide");
            savedRecipesAdapter.setRecipes(new ArrayList<>());
            return;
        }

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        List<Task<DocumentSnapshot>> tasks = new ArrayList<>();

        for (String recipeId : recipeIds) {
            tasks.add(db.collection("recipes").document(recipeId).get());
        }

        Tasks.whenAll(tasks)
            .addOnSuccessListener(aVoid -> {
                List<FirestoreRecipe> recipes = new ArrayList<>();
                
                for (Task<DocumentSnapshot> task : tasks) {
                    DocumentSnapshot doc = task.getResult();
                    if (doc != null && doc.exists()) {
                        FirestoreRecipe recipe = FirestoreRecipe.fromFirestoreDoc(doc);
                        recipes.add(recipe);
                    } else {
                        Log.w("ProfileFragment", "Document non trouvé ou null");
                    }
                }
                
                Log.d("ProfileFragment", "Nombre total de recettes chargées: " + recipes.size());
                savedRecipesAdapter.setRecipes(recipes);
            })
            .addOnFailureListener(e -> {
                Log.e("ProfileFragment", "Erreur lors du chargement des recettes", e);
                Toast.makeText(getContext(), "Erreur lors du chargement des recettes", Toast.LENGTH_SHORT).show();
                savedRecipesAdapter.setRecipes(new ArrayList<>());
            });
    }

    // Classe interne pour l'adaptateur des recettes sauvegardées
    private class SavedRecipesAdapter extends RecyclerView.Adapter<SavedRecipesAdapter.ViewHolder> {
        private List<FirestoreRecipe> recipes;

        public SavedRecipesAdapter(List<FirestoreRecipe> recipes) {
            this.recipes = recipes;
        }

        @SuppressLint("NotifyDataSetChanged")
        public void setRecipes(List<FirestoreRecipe> recipes) {
            this.recipes = recipes;
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.item_saved_recipe, parent, false);
            return new ViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
            FirestoreRecipe recipe = recipes.get(position);
            
            holder.recipeTitle.setText(recipe.name);
            holder.recipeDescription.setText(recipe.area + " • " + recipe.category + " • " +
                    (recipe.duration != 0 ? recipe.duration + " min" : getString(R.string.unknown_duration)));

            // Charger l'image de la recette
            if (recipe.imageUrl != null && !recipe.imageUrl.isEmpty()) {
                Glide.with(holder.itemView.getContext())
                    .load(recipe.imageUrl)
                    .centerCrop()
                    .placeholder(R.drawable.placeholder_plate)
                    .error(R.drawable.placeholder_plate)
                    .into(holder.recipeImage);
            } else {
                holder.recipeImage.setImageResource(R.drawable.placeholder_plate);
            }

            holder.itemView.setOnClickListener(v -> {
                Bundle args = new Bundle();
                args.putString("recipe_id", recipe.id);
                Navigation.findNavController(v)
                    .navigate(R.id.action_profileFragment_to_recipeDetailFragment, args);
            });
        }

        @Override
        public int getItemCount() {
            return recipes.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView recipeTitle;
            TextView recipeDescription;
            ImageView recipeImage;

            ViewHolder(View view) {
                super(view);
                recipeTitle = view.findViewById(R.id.recipe_title);
                recipeDescription = view.findViewById(R.id.recipe_description);
                recipeImage = view.findViewById(R.id.recipe_image);
            }
        }
    }

    /**
     * Déconnexion de l'utilisateur
     */
    private void signOut() {
        FirebaseAuth.getInstance().signOut();

        requireActivity().getSharedPreferences("cooksmart", MODE_PRIVATE)
                .edit()
                .remove("profile_exists")
                .apply();

        Intent intent = new Intent(getActivity(), MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }


}