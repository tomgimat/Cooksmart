package fr.tomgimat.cooksmart.auth;

import static android.content.Context.MODE_PRIVATE;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import fr.tomgimat.cooksmart.R;
import fr.tomgimat.cooksmart.databinding.FragmentRegisterBinding;
import fr.tomgimat.cooksmart.MainActivity;


public class RegisterFragment extends Fragment {
    private FragmentRegisterBinding binding;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentRegisterBinding.inflate(inflater, container, false);
        mAuth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        binding.registerButton.setOnClickListener(v -> attemptRegister());
        binding.backToLoginLink.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_register_to_login)
        );

        binding.buttonCancelRegister.setOnClickListener(v -> {
            requireActivity().finish();
        });

        return binding.getRoot();
    }

    private void attemptRegister() {
        String email = binding.emailField.getText().toString().trim();
        String password = binding.passwordField.getText().toString().trim();
        String pseudo = binding.pseudoField.getText().toString().trim();
        String dateOfBirth = binding.dobField.getText().toString().trim();
        if (email.isEmpty() || password.isEmpty() || pseudo.isEmpty() || dateOfBirth.isEmpty()) {
            Toast.makeText(getContext(), "@string/please_fill_all_fields", Toast.LENGTH_LONG).show();
            return;
        }
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(auth -> {
                    String uid = auth.getUser().getUid();
                    Map<String, Object> profile = new HashMap<>();
                    profile.put("pseudo", pseudo);
                    profile.put("email", email);
                    profile.put("dateNaissance", dateOfBirth);
                    profile.put("password", password);
                    db.collection("users").document(uid)
                            .set(profile, SetOptions.merge())
                            .addOnSuccessListener(a -> {
                                // Créer le document de préférences alimentaires par défaut pour le nouvel utilisateur
                                Map<String, Boolean> defaultPreferences = new HashMap<>();
                                defaultPreferences.put("vegetarian", false);
                                defaultPreferences.put("glutenFree", false);
                                defaultPreferences.put("lowSalt", false);
                                defaultPreferences.put("lactoseFree", false);
                                defaultPreferences.put("lowSugar", false);
                                defaultPreferences.put("vegan", false);
                                defaultPreferences.put("pescitarian", false);
                                defaultPreferences.put("halal", false);

                                db.collection("users").document(uid).set(Collections.singletonMap("preferences", defaultPreferences), SetOptions.merge())
                                        .addOnSuccessListener(b -> {
                                            handleAuthenticationSuccess();
                                        })
                                        .addOnFailureListener(e -> {
                                            // Loguer l'erreur mais continuer
                                            Log.e("RegisterFragment", "Erreur lors de l'initialisation des préférences alimentaires", e);
                                            handleAuthenticationSuccess();
                                        });
                            });
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }

    private void handleAuthenticationSuccess() {
        getContext().getSharedPreferences("cooksmart", MODE_PRIVATE)
                .edit()
                .putBoolean("profile_exists", true)
                .apply();

        // Redémarrer MainActivity et terminer AuthActivity
        android.content.Intent intent = new android.content.Intent(requireActivity(), MainActivity.class);
        intent.addFlags(android.content.Intent.FLAG_ACTIVITY_NEW_TASK | android.content.Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        requireActivity().finish();
    }
}

