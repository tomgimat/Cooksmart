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

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import fr.tomgimat.cooksmart.MainActivity;
import fr.tomgimat.cooksmart.R;
import fr.tomgimat.cooksmart.data.DietaryPreference;
import fr.tomgimat.cooksmart.databinding.FragmentProfileBinding;

public class ProfileFragment extends Fragment {

    private FragmentProfileBinding binding;
    private Map<String, CheckBox> prefCheckBoxes;
    private Button btnSavePrefs;


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