package com.safekid.safe_map.Child;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.example.safe_map.http.CommonMethod;
import com.safekid.safe_map.R;
import com.safekid.safe_map.common.ChildData;
import com.safekid.safe_map.http.RequestHttpURLConnection;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class QuestBeforeCheck extends AppCompatActivity {
    String UUID;
    Button quest;
    TextView questView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quest_before_check);

        Intent intent = getIntent();
        UUID = intent.getExtras().getString("childId");

        quest = (Button) findViewById(R.id.realstart);
        quest.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                ChildData.setcheckmapFlag(true);
                Intent intent = new Intent(getApplicationContext(), ChildMap.class);
                intent.putExtra("childId", UUID);
                startActivity(intent);
                finish();
            }
        });

        questView = (TextView) findViewById(R.id.textView32);

        String childInfo = fetchChild(UUID);
        try {
            JSONObject Alldata = new JSONObject(childInfo);
            String childName = Alldata.getString("childName");

            System.out.println("* errand *");
            JSONArray errandData = (JSONArray) Alldata.getJSONArray("errand");
            for (int j = 0; j < errandData.length(); j++) {
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
                String questlist = key.getString("quest");

                //List<String> questList = new ArrayList<String>();

                if (questlist != "") {
                    //JSONArray questData = (JSONArray) key.getJSONArray("quest");

                    String[] date = e_date.split("T");
                    //mErrandHome.add(new errandHome(childName, date[0], e_content, target_name, start_name, quest));
                    String[] questString = new String[0];
                    String questall = "";
                    if (questlist != null){
                        questlist = questlist.substring(1, questlist.length() - 1);
                        questString = questlist.split(",");

                        for(int k=0; k < questString.length; k++) {
                            questString[k] = questString[k].substring(1, questString[k].length() -1);
                            Log.i("questitem ", questString[k]);
                            questall = questall + "\n\n"+ questString[k];
                        }
                        questView.setText(questall);
                    }
                } else {
                    String[] date = e_date.split("T");
                    //mErrandHome.add(new errandHome(childName, date[0], e_content, target_name, start_name));
                }
                //SimpleDateFormat format = new SimpleDateFormat("yyyy년 MM월 dd일");
                //Date tempDate = format.parse(e_date);

                //String date = format.format(tempDate);
            }


        } catch (JSONException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

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