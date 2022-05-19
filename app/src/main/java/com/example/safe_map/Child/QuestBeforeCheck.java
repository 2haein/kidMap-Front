package com.example.safe_map.Child;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.example.safe_map.R;

public class QuestBeforeCheck extends AppCompatActivity {
    String UUID;
    Button quest;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_quest_before_check);

        Intent intent = getIntent();
        //UUID = intent.getExtras().getString("childId");

        quest = (Button) findViewById(R.id.realstart);
        quest.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), ChildMap.class);
                //intent.putExtra("childId", UUID);
                startActivity(intent);
                finish();
            }
        });
    }
}