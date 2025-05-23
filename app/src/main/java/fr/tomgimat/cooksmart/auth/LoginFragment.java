package fr.tomgimat.cooksmart.auth;

import static android.content.Context.MODE_PRIVATE;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;

import fr.tomgimat.cooksmart.MainActivity;
import fr.tomgimat.cooksmart.R;
import fr.tomgimat.cooksmart.databinding.FragmentLoginBinding;

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
                    getContext().getSharedPreferences("cooksmart", MODE_PRIVATE)
                            .edit()
                            .putBoolean("profile_exists", true)
                            .apply();
                    requireActivity().setResult(Activity.RESULT_OK);
                    requireActivity().finish();
                })
                .addOnFailureListener(e ->
                        Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_LONG).show()
                );
    }
}

