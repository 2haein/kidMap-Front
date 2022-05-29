package com.safekid.safe_map.Login;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.safekid.safe_map.MainActivity;
import com.safekid.safe_map.R;
import com.safekid.safe_map.common.ProfileData;
import com.example.safe_map.http.CommonMethod;
import com.safekid.safe_map.http.RequestHttpURLConnection;

import org.json.JSONObject;

import java.util.ArrayList;

public class Signup extends AppCompatActivity{
    EditText edittext;
    public Integer childNum = 0;
    private final String TAG = "SignupActivity";
    private RecyclerView mRecyclerView;
    private StdRecyclerAdapter mRecyclerAdapter;
    private ArrayList<ChildnumItem> mChildnum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signuplayout);

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView3);
        /* initiate adapter */
        mRecyclerAdapter = new StdRecyclerAdapter(this);

        /* adapt data */
        mChildnum = new ArrayList<>();
        for(int i=1;i<=5;i++){
            mChildnum.add(new ChildnumItem(i+"명"));
        }

        mRecyclerAdapter.setChildNum(mChildnum);
        /* initiate recyclerview */
        mRecyclerView.setAdapter(mRecyclerAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        //mRecyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL,false));

        mRecyclerAdapter.setOnItemClickListener(new StdRecyclerAdapter.OnItemClickEventListener() {
            @Override
            public void onItemClick(View a_view, int a_position) {
                childNum = a_position + 1;
                Toast.makeText(Signup.this, Integer.toString(a_position+1) +"명을 선택하였습니다", Toast.LENGTH_LONG).show();
            }
        });

        findViewById(R.id.ok).setOnClickListener(buttonClickListener);
        /*if (!fetchChildNum(ProfileData.getUserId()).equals("")) {
            if(fetchChildNum(ProfileData.getUserId())!=null ){
                ProfileData.setChildNum(Integer.parseInt(fetchChildNum(ProfileData.getUserId())));
            }
        }*/

        /*if(ProfileData.getChildNum()!=null){
            Log.i(TAG, String.format("다음화면 넘길 때 등록한 childNum: (%d)", ProfileData.getChildNum()));
            Intent intent = new Intent(Signup.this, MainActivity.class);
            startActivity(intent);
        }*/

    }

    Button.OnClickListener buttonClickListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            switch(v.getId()){
                case R.id.ok:
                    //자녀 수 저장
                    try {
                        //childNum = Integer.parseInt(edittext.getText().toString());
                    } catch(NumberFormatException e) {
                        childNum = 1;
                    }
                    Log.d("자녀", "자녀수: "+ childNum);
                    Toast.makeText(Signup.this, "자녀 등록을 완료하였습니다.", Toast.LENGTH_SHORT).show();
                    ProfileData.setChildNum(childNum);
                    registerChild(childNum);
                    Intent intent = new Intent(Signup.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                    break;
            }
        }
    };

    public void registerChild(Integer childNum){
        String url = CommonMethod.ipConfig + "/api/registerChild";
        try{
            String jsonString = new JSONObject()
                    .put("userId", ProfileData.getUserId())
                    .put("childNum", childNum)
                    .toString();
            //REST API
            RequestHttpURLConnection.NetworkAsyncTask networkTask = new RequestHttpURLConnection.NetworkAsyncTask(url, jsonString);
            networkTask.execute().get();
            Log.i(TAG, String.format("등록한 childNum: (%d)", childNum));
        }catch(Exception e){
            e.printStackTrace();
        }
    }

}
