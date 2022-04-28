package com.example.safe_map.FHome;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.safe_map.Login.ChildnumItem;
import com.example.safe_map.Login.Signup;
import com.example.safe_map.Login.StdRecyclerAdapter;
import com.example.safe_map.NetworkStatus;
import com.example.safe_map.R;

import java.util.ArrayList;

public class AddMissionActivity extends AppCompatActivity {

    // 초기변수설정
    TextView edit_addr;
    Button addAddrBtn1, addAddrBtn2;

    private RecyclerView mRecyclerView;
    private StdRecyclerAdapter mRecyclerAdapter;
    private ArrayList<ChildnumItem> mChildnum;

    // 주소 요청코드 상수 requestCode
    private static final int SEARCH_ADDRESS_ACTIVITY = 10000;


    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_mission);

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView2);
        /* initiate adapter */
        mRecyclerAdapter = new StdRecyclerAdapter(this);

        /* adapt data */
        mChildnum = new ArrayList<>();
        /*for(int i=1;i<=5;i++){
            mChildnum.add(new ChildnumItem(i+"명"));
        }*/
        mChildnum.add(new ChildnumItem("첫째아이"));
        mChildnum.add(new ChildnumItem("둘째아이"));
        mChildnum.add(new ChildnumItem("셋째아이"));

        mRecyclerAdapter.setChildNum(mChildnum);
        /* initiate recyclerview */
        mRecyclerView.setAdapter(mRecyclerAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        //mRecyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL,false));

        mRecyclerAdapter.setOnItemClickListener(new StdRecyclerAdapter.OnItemClickEventListener() {
            @Override
            public void onItemClick(View a_view, int a_position) {
                Toast.makeText(AddMissionActivity.this, a_position, Toast.LENGTH_LONG).show();
            }
        });


        // UI 요소 연결
        edit_addr = findViewById(R.id.editaddr_target);
        addAddrBtn1 = findViewById(R.id.add_adr_button1);
        addAddrBtn2 = findViewById(R.id.add_addr_button2);

        // 터치 안되게 막기
        //edit_addr.setFocusable(false);
        // 주소입력창 클릭
        addAddrBtn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("주소설정페이지", "주소입력창 클릭");
                int status = NetworkStatus.getConnectivityStatus(getApplicationContext());
                if(status == NetworkStatus.TYPE_MOBILE || status == NetworkStatus.TYPE_WIFI) {

                    Log.i("주소설정페이지", "주소입력창 클릭");
                    Intent i = new Intent(AddMissionActivity.this, AddressApiActivity.class);
                    // 화면전환 애니메이션 없애기
                    overridePendingTransition(0, 0);
                    // 주소결과
                    startActivityForResult(i, SEARCH_ADDRESS_ACTIVITY);

                }else {
                    Toast.makeText(getApplicationContext(), "인터넷 연결을 확인해주세요.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        Log.i("test", "onActivityResult");

        switch (requestCode) {
            case SEARCH_ADDRESS_ACTIVITY:
                if (resultCode == RESULT_OK) {
                    String data = intent.getExtras().getString("data");
                    if (data != null) {
                        Log.i("test", "data:" + data);
                        edit_addr.setText(data);
                    }
                }
                break;
        }
    }
}