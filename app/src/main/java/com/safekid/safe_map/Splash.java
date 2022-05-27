package com.safekid.safe_map;

import android.content.Intent;
import android.os.Bundle;

import com.safekid.safe_map.Login.LoginActivity;

import androidx.appcompat.app.AppCompatActivity;

import com.safekid.safe_map.databinding.ActivitySplashBinding;

public class Splash extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try{
            Thread.sleep(500);
        }catch(InterruptedException e){
            e.printStackTrace();
        }
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}