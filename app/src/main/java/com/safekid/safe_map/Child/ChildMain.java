package com.safekid.safe_map.Child;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.safekid.safe_map.http.CommonMethod;
import com.safekid.safe_map.FHome.errandHome;
import com.safekid.safe_map.Login.LoginActivity;
import com.safekid.safe_map.R;
import com.safekid.safe_map.common.ChildData;
import com.safekid.safe_map.common.ProfileData;
import com.safekid.safe_map.http.RequestHttpURLConnection;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class ChildMain extends AppCompatActivity {
    ImageButton parentButton, errandStart;
    TextView errandContent, target_name;
    String UUID, parent_Id;

    //심부름 목록
    private RecyclerView mRecyclerView;
    private CErrandRecyclerAdapter mRecyclerAdapter;
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
        mRecyclerAdapter = new CErrandRecyclerAdapter(this);

        /* adapt data */
        mErrandHome = new ArrayList<>();
        /*for(int i=1;i<=5;i++){
            mErrandHome.add(new errandHome(Integer.toString(i), Integer.toString(i), Integer.toString(i),Integer.toString(i)));
        }*/

        //fetchChild(UUID);
        String childInfo = fetchChild(UUID);
        try {
            JSONObject Alldata = new JSONObject(childInfo);
            String childName = Alldata.getString("childName");
            parent_Id = Alldata.getString("parent_id");
            ProfileData.setUserid(parent_Id);

            System.out.println("* errand *");
            JSONArray errandData = (JSONArray) Alldata.getJSONArray("errand");
            for(int j=0; j < errandData.length(); j++){
                //Log.i("정보정보 ", errandData.getString(j));
                JSONObject key = (JSONObject) errandData.getJSONObject(j);
                //Log.i("하나 ", key.getString("target_name"));

                String e_date = key.getString("e_date");
                String e_content = key.getString("e_content");
                String target_name = key.getString("target_name");
                String target_latitude = key.getString("target_latitude");
                String target_longitude = key.getString("target_longitude");
                String start_name = key.getString("start_name");
                String start_latitude = key.getString("start_latitude");
                String start_longitude = key.getString("start_longitude");
                String quest = key.getString("quest");


                //SimpleDateFormat format = new SimpleDateFormat("yyyy년 MM월 dd일");
                //Date tempDate = format.parse(e_date);

                //String date = format.format(tempDate);

                if (quest != "") {
                    //JSONArray questData = (JSONArray) key.getJSONArray("quest");

                    String[] date = e_date.split("T");
                    mErrandHome.add(new errandHome(childName, date[0], e_content,target_name,start_name, quest));

                } else {
                    String[] date = e_date.split("T");
                    mErrandHome.add(new errandHome(childName, date[0], e_content,target_name,start_name));

                }

            }

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        //mErrandHome.add(new errandHome("첫째아이", "2022-02-22", "빵 사오기","뚜레쥴"));
        //mErrandHome.add(new errandHome("첫째아이", "2022-02-23", "빵 사오기","뚜레쥴"));

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
                String result = fetchErrandChecking(parent_Id);
                if (result.equals("true")) {
                    // 심부름이 없다면 알림창을 띄움.
                    Toast.makeText(ChildMain.this, "설정된 심부름이 없습니다!", Toast.LENGTH_LONG).show();
                } else if (result.equals("false") && (ChildData.getcheckmapFlag()==false)){
                    // 처음 지도를 켜서 들어간 것
                    Intent intent = new Intent(getApplicationContext(), beforeCheck.class);
                    intent.putExtra("childId", UUID);
                    intent.putExtra("parentId", parent_Id);
                    startActivity(intent);
                } else {
                    Intent intent = new Intent(getApplicationContext(), ChildMap.class);
                    intent.putExtra("childId", UUID);
                    intent.putExtra("parentId", parent_Id);
                    startActivity(intent);
                }
            }
        });
    }

    public String fetchErrandChecking(String parent_id){
        String url = CommonMethod.ipConfig + "/api/fetchErrandChecking";
        String rtnStr= "";

        try{
            String jsonString = new JSONObject()
                    .put("userId", parent_id)
                    .toString();

            //REST API
            RequestHttpURLConnection.NetworkAsyncTask networkTask = new RequestHttpURLConnection.NetworkAsyncTask(url, jsonString);
            rtnStr = networkTask.execute().get();
        }catch(Exception e){
            e.printStackTrace();
        }
        return rtnStr;
    }

    public String fetchChild(String UUID){
        String url = CommonMethod.ipConfig + "/api/fetchChild";
        String rtnStr= "";
        String[] result = new String[0];

        try{
            String jsonString = new JSONObject()
                    .put("UUID", UUID)
                    .toString();

            //REST API
            RequestHttpURLConnection.NetworkAsyncTask networkTask = new RequestHttpURLConnection.NetworkAsyncTask(url, jsonString);
            rtnStr = networkTask.execute().get();
            Log.i("wkwkkwk" , rtnStr);
            //String result2 = rtnStr.substring(1, rtnStr.length() - 1);
            //Log.i("data22 " , result2);

            /*result = result2.split(",");
            for (int i=0; i<result.length; i++){
                result[i] = result[i].substring(1, result[i].length() - 1);
            }*/
        }catch(Exception e){
            e.printStackTrace();
        }
        return rtnStr;

    }

}