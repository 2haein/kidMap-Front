package com.example.safe_map.Child;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;

import com.example.safe_map.Login.ChildLoginActivity;
import com.example.safe_map.Login.Signup;
import com.example.safe_map.R;

public class ChildMain extends AppCompatActivity {
    ImageButton parentButton, errandStart;
    TextView errandContent, target_name;
    String UUID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child);

        Intent intent = getIntent();
        UUID = intent.getExtras().getString("childId");
        Log.i("child ID 22222", UUID);

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
                                Intent intent2 = new Intent(ChildMain.this, beforeCheck.class);
                                intent2.putExtra("childId", UUID);
                                startActivity(intent2);
                                finish();
                            }
                        });
                dlg.show();
            }
        });

        errandStart = (ImageButton) findViewById(R.id.start);
        errandStart.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getApplicationContext(), beforeCheck.class);
                startActivity(intent);
            }
        });
    }


}