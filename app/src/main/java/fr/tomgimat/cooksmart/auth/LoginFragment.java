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

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import fr.tomgimat.cooksmart.MainActivity;
import fr.tomgimat.cooksmart.R;
import fr.tomgimat.cooksmart.databinding.FragmentLoginBinding;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class LoginFragment extends Fragment {
    private FragmentLoginBinding binding;
    private FirebaseAuth mAuth;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentLoginBinding.inflate(inflater, container, false);
        mAuth = FirebaseAuth.getInstance();

        binding.loginButton.setOnClickListener(v -> attemptLogin());
        binding.signUpLink.setOnClickListener(v ->
                NavHostFragment.findNavController(this)
                        .navigate(R.id.action_login_to_register)
        );

        binding.buttonCancelLogin.setOnClickListener(v ->{
            requireActivity().finish();
        });

        return binding.getRoot();
    }

    private void attemptLogin() {
        String email = binding.emailField.getText().toString().trim();
        String password   = binding.passwordField.getText().toString().trim();
        if (email.isEmpty() || password.isEmpty()){
            Toast.makeText(getContext(), "@string/please_enter_email_and_password", Toast.LENGTH_LONG).show();
            return;
        } else if (password.length() < 6){
            Toast.makeText(getContext(), "@string/password_must_be_at_least_6_characters", Toast.LENGTH_LONG).show();
            return;
        }

        mAuth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    String uid = authResult.getUser().getUid();
                    DocumentReference userDocRef = FirebaseFirestore.getInstance().collection("users").document(uid);

                    // Vérifier et initialiser les préférences alimentaires si elles n'existent pas
                    userDocRef.get()
                            .addOnCompleteListener(task -> {
                                if (task.isSuccessful()) {
                                    DocumentSnapshot document = task.getResult();
                                    Map<String, Object> currentProfile = document.getData();
                                    if (currentProfile == null || !currentProfile.containsKey("preferences") || currentProfile.get("preferences") == null) {
                                        // Les préférences n'existent pas ou sont nulles, les créer avec les valeurs par défaut
                                        Map<String, Boolean> defaultPreferences = new HashMap<>();
                                        defaultPreferences.put("vegetarian", false);
                                        defaultPreferences.put("glutenFree", false);
                                        defaultPreferences.put("lowSalt", false);
                                        defaultPreferences.put("lactoseFree", false);
                                        defaultPreferences.put("lowSugar", false);
                                        defaultPreferences.put("vegan", false);
                                        defaultPreferences.put("pescitarian", false);
                                        defaultPreferences.put("halal", false);


                                        userDocRef.set(Collections.singletonMap("preferences", defaultPreferences), SetOptions.merge())
                                                .addOnSuccessListener(aVoid -> {
                                                    handleAuthenticationSuccess();
                                                })
                                                .addOnFailureListener(e -> {
                                                    // Loguer l'erreur mais continuer
                                                    Log.e("LoginFragment", "Erreur lors de l'initialisation des préférences alimentaires", e);
                                                    handleAuthenticationSuccess();
                                                });
                                    } else {
                                        // Les préférences existent déjà, continuer
                                        handleAuthenticationSuccess();
                                    }
                                } else {
                                    // Erreur lors de la récupération du document utilisateur, continuer
                                    Log.e("LoginFragment", "Erreur lors de la récupération du document utilisateur", task.getException());
                                    handleAuthenticationSuccess();
                                }
                            });
                })
                .addOnFailureListener(e -> {
                    // Afficher le message d'erreur de connexion
                    Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show();
                });
    }

    private void handleAuthenticationSuccess() {
        // Mettre à jour le SharedPreferences (si toujours pertinent)
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

