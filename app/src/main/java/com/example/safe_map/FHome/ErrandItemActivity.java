package com.example.safe_map.FHome;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import com.example.safe_map.R;

public class ErrandItemActivity extends AppCompatActivity {

    TextView childName, date, content, target, start, quest;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_errand_item);

        childName = (TextView) findViewById(R.id.childname);
        date = (TextView) findViewById(R.id.idate);
        content = (TextView) findViewById(R.id.ierrandcontent);
        target = (TextView) findViewById(R.id.ierrandtarget);
        start = (TextView) findViewById(R.id.ierrandstart);
        quest = (TextView) findViewById(R.id.questlist);

        Intent intent = getIntent();

        String child = intent.getExtras().getString("childname");
        String da = intent.getExtras().getString("date");
        String co = intent.getExtras().getString("content");
        String tar = intent.getExtras().getString("target");
        String sta = intent.getExtras().getString("start");
        String questlist = intent.getExtras().getString("quest");

        String[] questString = new String[0];
        String questall = "";
        if (questlist != null){
            questlist = questlist.substring(1, questlist.length() - 1);
            questString = questlist.split(",");

            for(int k=0; k < questString.length; k++) {
                questString[k] = questString[k].substring(1, questString[k].length() -1);
                Log.i("questitem ", questString[k]);
                questall = questall + "\n"+ questString[k];
            }
        }


        childName.setText(child);
        date.setText(da);
        content.setText(co);
        target.setText(tar);
        start.setText(sta);
        quest.setText(questall);

    }
}