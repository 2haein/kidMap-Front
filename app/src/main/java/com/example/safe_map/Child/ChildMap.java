package com.example.safe_map.Child;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;

import com.example.safe_map.R;

public class ChildMap extends AppCompatActivity {
    Fragment ChildMapView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_map);
        ChildMapView = new ChildMapView();

        getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainerView,ChildMapView).commitAllowingStateLoss();

    }
}