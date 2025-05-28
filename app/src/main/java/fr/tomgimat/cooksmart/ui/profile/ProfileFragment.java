package fr.tomgimat.cooksmart.ui.profile;

import static android.content.Context.MODE_PRIVATE;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.Toast;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import fr.tomgimat.cooksmart.MainActivity;
import fr.tomgimat.cooksmart.R;
import fr.tomgimat.cooksmart.data.DietaryPreference;
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
        savedRecipeIds = new ArrayList<>();
        savedRecipesAdapter = new SavedRecipesAdapter(savedRecipeIds);
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

    private void loadSavedRecipes() {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        
        db.collection("saved_recipes").document(uid)
            .get()
            .addOnSuccessListener(documentSnapshot -> {
                if (documentSnapshot.exists()) {
                    List<String> recipeIds = (List<String>) documentSnapshot.get("recipe_ids");
                    if (recipeIds != null) {
                        savedRecipeIds.clear();
                        savedRecipeIds.addAll(recipeIds);
                        savedRecipesAdapter.notifyDataSetChanged();
                    }
                }
            })
            .addOnFailureListener(e -> {
                Toast.makeText(getContext(), "Erreur lors du chargement des recettes sauvegardées", Toast.LENGTH_SHORT).show();
            });
    }

    // Classe interne pour l'adaptateur des recettes sauvegardées
    private class SavedRecipesAdapter extends RecyclerView.Adapter<SavedRecipesAdapter.ViewHolder> {
        private List<String> recipeIds;

        public SavedRecipesAdapter(List<String> recipeIds) {
            this.recipeIds = recipeIds;
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
            String recipeId = recipeIds.get(position);
            // TODO: Charger les détails de la recette depuis Firestore
            holder.recipeTitle.setText("Recette " + recipeId);
        }

        @Override
        public int getItemCount() {
            return recipeIds.size();
        }

        class ViewHolder extends RecyclerView.ViewHolder {
            TextView recipeTitle;

            ViewHolder(View view) {
                super(view);
                recipeTitle = view.findViewById(R.id.recipe_title);
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

    /**
     * Ajoute une recette à la liste des recettes sauvegardées
     * @param recipeId ID de la recette à sauvegarder
     */
    public static void saveRecipe(String recipeId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DocumentReference savedRecipesRef = db.collection("saved_recipes").document(uid);

        savedRecipesRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                // Le document existe, on ajoute la recette à la liste existante
                List<String> recipeIds = (List<String>) documentSnapshot.get("recipe_ids");
                if (recipeIds == null) {
                    recipeIds = new ArrayList<>();
                }
                if (!recipeIds.contains(recipeId)) {
                    recipeIds.add(recipeId);
                    savedRecipesRef.update("recipe_ids", recipeIds);
                }
            } else {
                // Le document n'existe pas, on le crée avec la première recette
                List<String> recipeIds = new ArrayList<>();
                recipeIds.add(recipeId);
                Map<String, Object> data = new HashMap<>();
                data.put("recipe_ids", recipeIds);
                savedRecipesRef.set(data);
            }
        });
    }

    /**
     * Supprime une recette de la liste des recettes sauvegardées
     * @param recipeId ID de la recette à supprimer
     */
    public static void removeRecipe(String recipeId) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DocumentReference savedRecipesRef = db.collection("saved_recipes").document(uid);

        savedRecipesRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                List<String> recipeIds = (List<String>) documentSnapshot.get("recipe_ids");
                if (recipeIds != null) {
                    recipeIds.remove(recipeId);
                    if (recipeIds.isEmpty()) {
                        // Si la liste est vide, on supprime le document
                        savedRecipesRef.delete();
                    } else {
                        savedRecipesRef.update("recipe_ids", recipeIds);
                    }
                }
            }
        });
    }

    /**
     * Vérifie si une recette est sauvegardée
     * @param recipeId ID de la recette à vérifier
     * @param callback Callback pour retourner le résultat
     */
    public static void isRecipeSaved(String recipeId, OnRecipeSavedCallback callback) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        DocumentReference savedRecipesRef = db.collection("saved_recipes").document(uid);

        savedRecipesRef.get().addOnSuccessListener(documentSnapshot -> {
            if (documentSnapshot.exists()) {
                List<String> recipeIds = (List<String>) documentSnapshot.get("recipe_ids");
                callback.onResult(recipeIds != null && recipeIds.contains(recipeId));
            } else {
                callback.onResult(false);
            }
        }).addOnFailureListener(e -> callback.onResult(false));
    }

    public interface OnRecipeSavedCallback {
        void onResult(boolean isSaved);
    }

}