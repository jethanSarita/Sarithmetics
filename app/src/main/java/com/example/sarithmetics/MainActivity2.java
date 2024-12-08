package com.example.sarithmetics;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.example.sarithmetics.databinding.ActivityMain2Binding;

public class MainActivity2 extends AppCompatActivity {

    ActivityMain2Binding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMain2Binding.inflate(getLayoutInflater());
        EdgeToEdge.enable(this);
        setContentView(binding.getRoot());
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        replaceFragment(new HomeFragment());
        binding.bottomNavView.setSelectedItemId(R.id.nav_home2);
        binding.bottomNavView.setOnItemSelectedListener(item -> {
            int selected = item.getItemId();
            int home = R.id.nav_home2;
            int items = R.id.nav_items2;
            int restock = R.id.nav_restock2;
            int history = R.id.nav_history2;
            int insights = R.id.nav_insights2;

            if (selected == home) {
                replaceFragment(new HomeFragment());
            } else if (selected == items) {
                replaceFragment(new ItemsFragment());
            } else if (selected == restock) {
                replaceFragment(new RestockFragment());
            } else if (selected == history) {
                replaceFragment(new HistoryFragment());
            } else if (selected == insights) {
                replaceFragment(new InsightFragment());
            }

            return true;
        });
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.frame_layout, fragment);
        fragmentTransaction.commit();
    }
}