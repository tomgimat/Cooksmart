package fr.tomgimat.cooksmart;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.AppBarConfiguration;
import androidx.navigation.ui.NavigationUI;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreSettings;

import fr.tomgimat.cooksmart.databinding.ActivityMainBinding;

public class MainActivity extends AppCompatActivity {

    private ActivityMainBinding binding;
    //    private FirebaseAnalytics analytics;
    private ActivityResultLauncher<Intent> authLauncher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Toolbar toolbar = binding.toolbar;
        setSupportActionBar(toolbar);

        AppBarConfiguration appBarConfiguration = new AppBarConfiguration.Builder(
                R.id.fragment_home, R.id.fragment_search, R.id.fragment_my_space)
                .build();

        //        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        NavHostFragment navHostFragment = (NavHostFragment) getSupportFragmentManager().findFragmentById(R.id.nav_host_fragment_activity_main);
        NavController navController = navHostFragment.getNavController();

        NavigationUI.setupActionBarWithNavController(this, navController, appBarConfiguration);
        NavigationUI.setupWithNavController(binding.bottomNavView, navController);

        bottomNavItemChangeListener(binding.bottomNavView, navController);
//        analytics = FirebaseAnalytics.getInstance(this);

//        FirebaseFirestore.getInstance().terminate()
//                .addOnSuccessListener(unused ->
//                        FirebaseFirestore.getInstance().clearPersistence()
//                );

        FirebaseFirestore.getInstance().setFirestoreSettings(
                new FirebaseFirestoreSettings.Builder()
                        .setPersistenceEnabled(true)  // cache disque & mémoire
                        .build()
        );

        authLauncher = registerForActivityResult(
                new ActivityResultContracts.StartActivityForResult(),
                result -> {
                    if (result.getResultCode() == Activity.RESULT_OK) {
                        Toast.makeText(this, R.string.authentication_success, Toast.LENGTH_SHORT).show();
                    }
                }
        );

    }

    /**
     * Assure que la bottom navbar reste une destination top-level
     *
     * @param navigationView
     * @param navController
     */
    private void bottomNavItemChangeListener(BottomNavigationView navigationView, NavController navController) {
        navigationView.setOnItemSelectedListener(item -> {
            if (item.getItemId() != navigationView.getSelectedItemId()) {
                navController.popBackStack(item.getItemId(), true, false);
                navController.navigate(item.getItemId());
            }
            return true;
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.top_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.fragment_login) {
            Intent intent = new Intent(this, AuthActivity.class);

            // Vérifier si l'utilisateur est connecté ou non
            boolean userExists = getSharedPreferences("cooksmart", MODE_PRIVATE).getBoolean("profile_exists", false);
            FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
            if (userExists && user != null) {
                Navigation.findNavController(this, R.id.nav_host_fragment_activity_main)
                        .navigate(R.id.fragment_profile);
            } else {
                // Utilisateur non connecté, rediriger vers AuthActivity
                authLauncher.launch(intent);
            }
            return true;
        } else if (item.getItemId() == R.id.fragment_settings) {
            Navigation.findNavController(this, R.id.nav_host_fragment_activity_main)
                    .navigate(R.id.fragment_settings);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onSupportNavigateUp() {
        NavController navController = Navigation.findNavController(this, R.id.nav_host_fragment_activity_main);
        return navController.navigateUp() || super.onSupportNavigateUp();
    }

}