package com.safekid.safe_map.FCheckMap;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import com.safekid.safe_map.FHome.QuestData;
import com.safekid.safe_map.MainActivity;
import com.safekid.safe_map.QCheckRecyclerAdapter;
import com.safekid.safe_map.R;
import com.safekid.safe_map.RecyclerDecoration;
import com.safekid.safe_map.common.ProfileData;
import com.safekid.safe_map.http.CommonMethod;
import com.safekid.safe_map.http.RequestHttpURLConnection;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

public class CheckingQuestActivity extends AppCompatActivity {

    private ArrayList<QuestData> mArrayList;
    private QCheckRecyclerAdapter mQuestAdapter;
    private RecyclerView mQuestRecyclerView;
    private Context mContext;

    Button finish;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_checking_quest);

        mContext = getApplicationContext();
        mQuestRecyclerView = findViewById(R.id.recyclerView5);

        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false);
        mQuestRecyclerView.setLayoutManager(layoutManager);

        mArrayList = new ArrayList<>();
        //mArrayList.add(new QuestData("(필수) 중간 지점을 거쳐 경로 따라가기"));
        //mArrayList.add(new QuestData("(필수) 심부름 내용 잘 수행하기"));

        mQuestAdapter = new QCheckRecyclerAdapter(mContext, mArrayList);

        RecyclerDecoration spaceDecoration = new RecyclerDecoration(30);
        mQuestRecyclerView.addItemDecoration(spaceDecoration);

        String childInfo = fetchChild(ProfileData.getErrandChildId());
        try {
            JSONObject Alldata = new JSONObject(childInfo);
            String childName = Alldata.getString("childName");

            System.out.println("* errand *");
            JSONArray errandData = (JSONArray) Alldata.getJSONArray("errand");

            JSONObject key = (JSONObject) errandData.getJSONObject(errandData.length() - 1);
            //Log.i("하나 ", key.getString("target_name"));

            String e_date = key.getString("e_date");
            String questlist = key.getString("quest");

            if (questlist != "") {
                //JSONArray questData = (JSONArray) key.getJSONArray("quest");

                String[] date = e_date.split("T");
                //mErrandHome.add(new errandHome(childName, date[0], e_content, target_name, start_name, quest));
                String[] questString = new String[0];

                if (questlist != null){
                    questlist = questlist.substring(1, questlist.length() - 1);
                    questString = questlist.split(",");

                    for(int k=0; k < questString.length; k++) {
                        questString[k] = questString[k].substring(1, questString[k].length() -1);
                        Log.i("questitem ", questString[k]);
                        mArrayList.add(new QuestData(questString[k]));
                        //mQuestAdapter.notifyItemInserted(mArrayList.size()-1);
                    }
                }
            } else {
                //String[] date = e_date.split("T");
                //mErrandHome.add(new errandHome(childName, date[0], e_content, target_name, start_name));
            }

        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        mQuestRecyclerView.setAdapter(mQuestAdapter);

        finish = (Button) findViewById(R.id.button2);
        finish.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                updateErrandChecking();
                ProfileData.setErrandChildId("");
                Intent intent = new Intent(getApplicationContext(), MainActivity.class);
                startActivity(intent);
                finish();
            }
        });

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

        }catch(Exception e){
            e.printStackTrace();
        }
        return rtnStr;

    }

    public void updateErrandChecking(){
        String url = CommonMethod.ipConfig + "/api/updateErrandChecking";

        try{
            String jsonString = new JSONObject()
                    .put("userId", ProfileData.getUserId())
                    .put("isErrandComplete", true)
                    .toString();

            //REST API
            RequestHttpURLConnection.NetworkAsyncTask networkTask = new RequestHttpURLConnection.NetworkAsyncTask(url, jsonString);
            networkTask.execute().get();

        }catch(Exception e){
            e.printStackTrace();
        }
    }
}