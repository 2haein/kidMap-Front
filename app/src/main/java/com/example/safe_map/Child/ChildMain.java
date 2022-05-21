package com.example.safe_map.Child;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.safe_map.FHome.ErrandRecyclerAdapter;
import com.example.safe_map.FHome.errandHome;
import com.example.safe_map.Login.ChildLoginActivity;
import com.example.safe_map.Login.LoginActivity;
import com.example.safe_map.Login.Signup;
import com.example.safe_map.R;
import com.example.safe_map.common.ChildData;
import com.example.safe_map.http.CommonMethod;
import com.example.safe_map.http.RequestHttpURLConnection;

import org.json.JSONObject;

import java.util.ArrayList;

public class ChildMain extends AppCompatActivity {
    ImageButton parentButton, errandStart;
    TextView errandContent, target_name;
    String UUID;

    //심부름 목록
    private RecyclerView mRecyclerView;
    private ErrandRecyclerAdapter mRecyclerAdapter;
    private ArrayList<errandHome> mErrandHome;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child);

        Intent intent = getIntent();
        UUID = intent.getExtras().getString("childId");
        ChildData.setChildId(UUID);
        Log.i("child ID 22222", UUID);

        // 부모 모드로 전환
        parentButton = (ImageButton) findViewById(R.id.parent);
        parentButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dlg = new AlertDialog.Builder(ChildMain.this);
                dlg.setTitle("부모 로그인");
                dlg.setMessage("부모로 로그인하실건가요?");
                dlg.setPositiveButton("네",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // 처리할 코드 작성
                                ChildData.setChildId("");
                                Intent intent2 = new Intent(ChildMain.this, LoginActivity.class);
                                startActivity(intent2);
                                finish();
                            }
                        });
                dlg.show();
            }
        });
        //심부름 목록 불러오기
        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView4);
        /* initiate adapter */
        mRecyclerAdapter = new ErrandRecyclerAdapter(this);

        /* adapt data */
        mErrandHome = new ArrayList<>();
        /*for(int i=1;i<=5;i++){
            mErrandHome.add(new errandHome(Integer.toString(i), Integer.toString(i), Integer.toString(i),Integer.toString(i)));
        }*/

        //fetchChild(UUID);
        mErrandHome.add(new errandHome("첫째아이", "2022-02-22", "빵 사오기","뚜레쥴"));
        mErrandHome.add(new errandHome("첫째아이", "2022-02-23", "빵 사오기","뚜레쥴"));

        mRecyclerAdapter.setErrandHome(mErrandHome);
        /* initiate recyclerview */
        mRecyclerView.setAdapter(mRecyclerAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        //mRecyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL,false));


        // 심부름 시작하기 버튼
        errandStart = (ImageButton) findViewById(R.id.start);
        errandStart.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), beforeCheck.class);
                intent.putExtra("childId", UUID);
                startActivity(intent);
            }
        });
    }



}