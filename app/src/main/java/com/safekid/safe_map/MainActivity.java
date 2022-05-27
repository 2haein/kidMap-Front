package com.safekid.safe_map;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;

import com.safekid.safe_map.FCheckMap.CheckMapFragment;
import com.safekid.safe_map.FHome.AddressApiActivity;
import com.safekid.safe_map.FHome.HomeFragment;
import com.safekid.safe_map.FMypage.MypageFragment;
import com.safekid.safe_map.FNotifyMap.NotifyFragment;
import com.safekid.safe_map.common.ProfileData;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

    // menu item initialization
    BottomNavigationView BottomNavigationView;
    HomeFragment fragment1;
    CheckMapFragment fragment2;
    NotifyFragment fragment3;
    MypageFragment fragment4;

    private static final int SEARCH_ADDRESS_ACTIVITY = 30000;
    private Object MypageFragment;

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
                ProfileData.setMapFlag(true);
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

    public void moveToAddressApi(){
        Intent intent = new Intent(MainActivity.this, AddressApiActivity.class);
        startActivityForResult(intent, 30000);
    }


    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        Log.i("request Code : ", String.valueOf(requestCode));
        if (requestCode == 30000) { //mypage 부모 집주소
            for (Fragment fragment : getSupportFragmentManager().getFragments()) {
                if (null != fragment) {
                    fragment.onActivityResult(requestCode, resultCode, intent);
                } else {
                    new MypageFragment().onActivityResult(requestCode, resultCode, intent);
                }
            }
        } else if (requestCode == 40000) { //notify 위험지역 주소
            for (Fragment fragment : getSupportFragmentManager().getFragments()) {
                if (null != fragment) {
                    fragment.onActivityResult(requestCode, resultCode, intent);
                } else {
                    new NotifyFragment().onActivityResult(requestCode, resultCode, intent);
                }
            }
        }
    }
    public void CallCheckMap(){
        CheckMapFragment fr = new CheckMapFragment();
        FragmentManager fm = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fm.beginTransaction();
        fragmentTransaction.replace(R.id.home_ly, fr);
        fragmentTransaction.commit();
        //getSupportFragmentManager().beginTransaction().replace(R.id.home_ly, new CheckMapFragment()).commitAllowingStateLoss();
    }

}