package com.safekid.safe_map.Login;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;

import com.safekid.safe_map.Child.ChildMain;
import com.safekid.safe_map.callback.SessionCallback;
import com.safekid.safe_map.common.ProfileData;
import com.safekid.safe_map.http.CommonMethod;
import com.safekid.safe_map.http.RequestHttpURLConnection;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;


import com.safekid.safe_map.R;
import com.kakao.auth.Session;
import com.kakao.network.ErrorResult;
import com.kakao.usermgmt.UserManagement;
import com.kakao.usermgmt.callback.LogoutResponseCallback;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class ChildLoginActivity extends AppCompatActivity {
    TextView idlist;
    Button ok;
    String ID;
    private SessionCallback sessionCallback = new SessionCallback();

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
                     UserManagement.getInstance()
                            .requestLogout(new LogoutResponseCallback() {
                                @Override
                                public void onSessionClosed(ErrorResult errorResult) {
                                    super.onSessionClosed(errorResult);
                                }
                                @Override
                                public void onCompleteLogout() {
                                    if (sessionCallback != null) {
                                        Session.getCurrentSession().removeCallback(sessionCallback);
                                    }
                                }
                            });
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


}