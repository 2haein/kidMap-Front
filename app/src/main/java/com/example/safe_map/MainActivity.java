package com.example.safe_map;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.Signature;
import android.os.Bundle;
import android.util.Base64;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Button;

import com.example.safe_map.FCheckMap.CheckMapFragment;
import com.example.safe_map.FHome.HomeFragment;
import com.example.safe_map.FMypage.MypageFragment;
import com.example.safe_map.FNotifyMap.NotifyFragment;
import com.example.safe_map.Login.LoginActivity;
import com.example.safe_map.callback.SessionCallback;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.kakao.auth.AuthType;
import com.kakao.auth.Session;
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.LoginButton;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.LogoutResponseCallback;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class MainActivity extends AppCompatActivity {

    // menu item initialization




    BottomNavigationView BottomNavigationView;
    HomeFragment fragment1;
    CheckMapFragment fragment2;
    NotifyFragment fragment3;
    MypageFragment fragment4;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        BottomNavigationView = findViewById(R.id.bottomNavigationView);

        //프래그먼트 생성
        fragment1 = new HomeFragment();
        fragment2 = new CheckMapFragment();
        fragment3 = new NotifyFragment();
        fragment4 = new MypageFragment();

        //제일 처음 띄워줄 뷰를 세팅해줍니다. commit();까지 해줘야 합니다.
        getSupportFragmentManager().beginTransaction().replace(R.id.home_ly,fragment1).commitAllowingStateLoss();

        //bottomnavigationview의 아이콘을 선택 했을때 원하는 프래그먼트가 띄워질 수 있도록 리스너를 추가합니다.
        BottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()){
                    //menu_bottom.xml에서 지정해줬던 아이디 값을 받아와서 각 아이디값마다 다른 이벤트를 발생시킵니다.
                    case R.id.home:{
                        getSupportFragmentManager().beginTransaction().replace(R.id.home_ly,fragment1).commitAllowingStateLoss();
                        return true; }
                    case R.id.i_map:{
                        getSupportFragmentManager().beginTransaction().replace(R.id.home_ly,fragment2).commitAllowingStateLoss();
                        return true; }
                    case R.id.notify_map:{
                        getSupportFragmentManager().beginTransaction().replace(R.id.home_ly,fragment3).commitAllowingStateLoss();
                        return true; }
                    case R.id.settings:{
                        getSupportFragmentManager().beginTransaction().replace(R.id.home_ly,fragment4).commitAllowingStateLoss();
                        return true; }
                    default: return false;
                }
            }
        });



    }

}