package com.example.safe_map.Child;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;

import com.example.safe_map.R;

public class beforeCheck extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener{
    String UUID;

    Button next;
    ImageView image1, image2, image3;
    CheckBox checkbox1, checkbox2, checkbox3;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_before_check);
        Intent intent = getIntent();
        Bundle data = getIntent().getExtras();
        //UUID = data.getString("childId");

        //imageview 색상 바꾸기
        image1 = (ImageView) findViewById(R.id.imageView6);
        image2 = (ImageView) findViewById(R.id.imageView7);
        image3 = (ImageView) findViewById(R.id.imageView8);
        image1.setColorFilter(Color.parseColor("#125B50"), PorterDuff.Mode.SRC_ATOP);
        image2.setColorFilter(Color.parseColor("#F8B400"), PorterDuff.Mode.SRC_ATOP);
        image3.setColorFilter(Color.parseColor("#FF6363"), PorterDuff.Mode.SRC_ATOP);

        //체크박스
        checkbox1 = (CheckBox) findViewById(R.id.checkBox1);
        checkbox2 = (CheckBox) findViewById(R.id.checkBox2);
        checkbox3 = (CheckBox) findViewById(R.id.checkBox3);

        checkbox1.setOnCheckedChangeListener(beforeCheck.this);
        checkbox2.setOnCheckedChangeListener(beforeCheck.this);
        checkbox3.setOnCheckedChangeListener(beforeCheck.this);


        next = (Button) findViewById(R.id.next);
        next.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                if(checkbox1.isChecked() && checkbox2.isChecked() && checkbox3.isChecked()){
                    Intent intent = new Intent(getApplicationContext(), QuestBeforeCheck.class);
                    //intent.putExtra("childId", UUID);
                    startActivity(intent);
                    finish();
                } else {
                    AlertDialog.Builder dlg = new AlertDialog.Builder(beforeCheck.this);
                    dlg.setTitle("안전한 심부름");
                    dlg.setMessage("주의사항을 다시 확인하고 체크하세요");
                    dlg.setPositiveButton("네",
                            new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) {
                                }
                            });
                    dlg.show();
                }
            }
        });
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

    }
}