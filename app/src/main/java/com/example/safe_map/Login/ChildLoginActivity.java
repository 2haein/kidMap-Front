package com.example.safe_map.Login;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.example.safe_map.Child.ChildMain;
import com.example.safe_map.common.ProfileData;
import com.example.safe_map.http.CommonMethod;
import com.example.safe_map.http.RequestHttpURLConnection;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


import com.example.safe_map.R;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ChildLoginActivity extends AppCompatActivity {
    TextView idlist;
    Button ok;
    String ID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login_uuid);

        final EditText uuid = (EditText) findViewById(R.id.child_uuid);

        ok = (Button) findViewById(R.id.button);
        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ID = uuid.getText().toString();
                Log.i("login " ,findUUID(ID));
                Log.i("login222222", ID);
                Intent intent;
                if (findUUID(ID).equals("true")){
                     intent = new Intent(ChildLoginActivity.this, ChildMain.class);
                     intent.putExtra("childId", ID);
                     startActivity(intent);
                     finish();
                }
                else {
                    AlertDialog.Builder dlg = new AlertDialog.Builder(ChildLoginActivity.this);
                    dlg.setTitle("자녀 ID");
                    dlg.setMessage("ID를 잘못 입력하였습니다");
                    dlg.setPositiveButton("다시 입력하기",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                    // 처리할 코드 작성
                                }
                            });
                    dlg.show();
                }
            }
        });


        String[] array = fetchUUID(ProfileData.getUserId());
        //Log.i("array " , String.valueOf(arrayList.size()));
        //String[] array = arrayList.toArray(new String[arrayList.size()]);
        Log.i("array " , String.valueOf(array.length));

        idlist = (TextView) findViewById(R.id.idlist);
        idlist.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder dlg = new AlertDialog.Builder(ChildLoginActivity.this);
                dlg.setTitle("자녀 ID 목록 (첫째부터)");
                dlg.setItems(array, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        uuid.setText(array[which]);
                    }
                });
                dlg.show();
            }
        });
    }

    //Boolean
    public String findUUID(String UUID){
        String url = CommonMethod.ipConfig + "/api/findUUID";
        String rtnStr = "";

        try{
            String jsonString = new JSONObject()
                    .put("UUID", UUID)
                    .toString();

            //REST API
            RequestHttpURLConnection.NetworkAsyncTask networkTask = new RequestHttpURLConnection.NetworkAsyncTask(url, jsonString);
            rtnStr = networkTask.execute().get();
            rtnStr = Boolean.toString(Boolean.parseBoolean(rtnStr));

        }catch(Exception e){
            e.printStackTrace();
        }
        return rtnStr;
    }

    //리스트로 반환
    public String[] fetchUUID(String userId){
        String url = CommonMethod.ipConfig + "/api/fetchUUID";
        List<String> arrayList = new ArrayList<String>();
        String data = "";
        String[] result = new String[0];
        try{
            String jsonString = new JSONObject()
                    .put("userId", userId)
                    .toString();

            //REST API
            RequestHttpURLConnection.NetworkAsyncTask networkTask = new RequestHttpURLConnection.NetworkAsyncTask(url, jsonString);
            data = networkTask.execute().get();
            Log.i("data " , data);
            String result2 = data.substring(1, data.length() - 1);
            Log.i("data22 " , result2);
            result = result2.split(",");
            for (int i=0; i<result.length; i++){
                result[i] = result[i].substring(1, result[i].length() - 1);
            }

        }catch(Exception e){
            e.printStackTrace();
        }
        return result;
    }

    //자녀 정보 가져오기 -> Child DB정보 가져옴
    public String fetchChild(String UUID){
        String url = CommonMethod.ipConfig + "/api/fetchChild";
        String rtnStr= "";

        try{
            String jsonString = new JSONObject()
                    .put("UUID", UUID)
                    .toString();

            //REST API
            RequestHttpURLConnection.NetworkAsyncTask networkTask = new RequestHttpURLConnection.NetworkAsyncTask(url, jsonString);
            rtnStr = networkTask.execute().get();

//          Toast.makeText(getActivity(), "자녀 등록을 완료하였습니다.", Toast.LENGTH_SHORT).show();
//           Log.i(TAG, String.format("가져온 Phonenum: (%s)", rtnStr));

        }catch(Exception e){
            e.printStackTrace();
        }

        return rtnStr;

    }
}