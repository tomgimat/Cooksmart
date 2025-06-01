package fr.tomgimat.cooksmart;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

/**
 * Activité d'authentification (connexion/inscription).
 */
public class AuthActivity extends AppCompatActivity {

    @Override protected void onCreate(Bundle s){
        super.onCreate(s);
        setContentView(R.layout.activity_auth);
    }
}
