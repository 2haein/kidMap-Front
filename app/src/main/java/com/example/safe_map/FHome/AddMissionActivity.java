package com.example.safe_map.FHome;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.UserHandle;
import android.text.Editable;
import android.util.Log;
import android.view.Display;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.example.safe_map.Child.beforeCheck;
import com.example.safe_map.FCheckMap.CheckMapFragment;
import com.example.safe_map.Login.ChildnumItem;
import com.example.safe_map.Login.Signup;
import com.example.safe_map.Login.StdRecyclerAdapter;
import com.example.safe_map.MainActivity;
import com.example.safe_map.NetworkStatus;
import com.example.safe_map.R;
import com.example.safe_map.common.ProfileData;
import com.example.safe_map.http.CommonMethod;
import com.example.safe_map.http.RequestHttpURLConnection;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.text.BreakIterator;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

public class AddMissionActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {

    // 초기변수설정
    public Integer childnum1 = 0;
    int y1=0, m1=0, d1=0, h1=0, mi1=0;
    int y=0, m=0, d=0, h=0, mi=0;

    TextView edit_addr, edit_addr2, date_view, time_view;
    Button addDate, addTime, addAddrBtn1, addAddrBtn2, checkDanger, All;
    CheckBox risk_chk;
    List<Address> address = null;
    List<String> quest = null;

    String target_name = "출발";
    String start_name= "도착";

    double target_longitude, target_latitude;
    double start_longitude, start_latitude;

    // 아이 목록 리사이클러뷰
    private RecyclerView mRecyclerView;
    private StdRecyclerAdapter mRecyclerAdapter;
    private ArrayList<ChildnumItem> mChildnum;

    // 퀘스트 목록 리사이클러뷰
    private Context mContext;
    private ArrayList<QuestData> mArrayList;
    private QuestAdapter mQuestAdapter;
    private RecyclerView mQuestRecyclerView;
    private EditText quest_name;
    private Button quest_save;

    // 주소 요청코드 상수 requestCode
    private static final int SEARCH_ADDRESS_ACTIVITY = 10000;
    private static final int SEARCH_ADDRESS_ACTIVITY2 = 20000;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_mission);

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView2);
        /* initiate adapter */
        mRecyclerAdapter = new StdRecyclerAdapter(this);

        final String childnum = fetchChildNum(ProfileData.getUserId());
        final int childNum = Integer.parseInt(childnum);
        /* adapt data */
        mChildnum = new ArrayList<>();
        for(int i = 1; i<= childNum; i++){
            if (i==1){
                mChildnum.add(new ChildnumItem("첫째아이"));
            } else if (i==2){
                mChildnum.add(new ChildnumItem("둘째아이"));
            } else if (i==3){
                mChildnum.add(new ChildnumItem("셋째아이"));
            } else if (i==4){
                mChildnum.add(new ChildnumItem("넷째아이"));
            } else if (i==5) {
                mChildnum.add(new ChildnumItem("다섯째아이"));
            } else {
                mChildnum.add(new ChildnumItem("자녀가 없습니다"));
            }
        }
        //mChildnum.add(new ChildnumItem("첫째아이"));
        //mChildnum.add(new ChildnumItem("둘째아이"));
        //mChildnum.add(new ChildnumItem("셋째아이"));

        mRecyclerAdapter.setChildNum(mChildnum);
        /* initiate recyclerview */
        mRecyclerView.setAdapter(mRecyclerAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        //mRecyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL,false));


        mRecyclerAdapter.setOnItemClickListener(new StdRecyclerAdapter.OnItemClickEventListener() {
            @Override
            public void onItemClick(View a_view, int a_position) {
                childnum1 = a_position;
                Toast.makeText(AddMissionActivity.this, Integer.toString(childnum1+1), Toast.LENGTH_LONG).show();
            }
        });

        //자녀UUID 검색
        String[] child = fetchUUID(ProfileData.getUserId());

        //심부름 내용
        EditText edit_content = findViewById(R.id.errandContent);

        //심부름 날짜 선택
        Calendar cal = Calendar.getInstance();
        y1 = cal.get(Calendar.YEAR);
        m1 = cal.get(Calendar.MONTH);
        d1 = cal.get(Calendar.DAY_OF_MONTH);
        h1 = cal.get(Calendar.HOUR);
        mi1 = cal.get(Calendar.MINUTE);
        addDate = findViewById(R.id.date_btn);
        date_view = findViewById(R.id.date_view);
        addDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog datePickerDialog = new DatePickerDialog(AddMissionActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        y = year;
                        Log.i("year", Integer.toString(year));
                        m = month+1;
                        Log.i("year", Integer.toString(m));
                        d = dayOfMonth;
                        Log.i("year", Integer.toString(d));
                        date_view.setText(y+"년  "+m+"월  "+d + "일");
                    }
                },y1, m1, d1);
                datePickerDialog.setMessage("심부름 날짜");
                datePickerDialog.show();
            }
        });

        //심부름 시간 선택
        addTime = findViewById(R.id.time_btn);
        time_view = findViewById(R.id.timeView);
        addTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog timePickerDialog = new TimePickerDialog(AddMissionActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        h = hourOfDay;
                        mi = minute;
                        time_view.setText(h+"시  "+mi+"분");
                    }
                }, h1, mi1, true);
                timePickerDialog.setMessage("출발 시각");
                timePickerDialog.show();
            }
        });

        // UI 요소 연결
        edit_addr = findViewById(R.id.editaddr_target);
        edit_addr2 = findViewById(R.id.home_addr);
        addAddrBtn1 = findViewById(R.id.add_adr_button1);
        addAddrBtn2 = findViewById(R.id.add_addr_button2);

        // 터치 안되게 막기
        //edit_addr.setFocusable(false);
        // 주소입력창 클릭
        addAddrBtn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("주소설정페이지", "주소입력창 클릭");
                int status = NetworkStatus.getConnectivityStatus(getApplicationContext());
                if(status == NetworkStatus.TYPE_MOBILE || status == NetworkStatus.TYPE_WIFI) {

                    Log.i("주소설정페이지", "주소입력창 클릭");
                    Intent i = new Intent(AddMissionActivity.this, AddressApiActivity.class);
                    // 화면전환 애니메이션 없애기
                    overridePendingTransition(0, 0);
                    // 주소결과
                    startActivityForResult(i, SEARCH_ADDRESS_ACTIVITY);

                }else {
                    Toast.makeText(getApplicationContext(), "인터넷 연결을 확인해주세요.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        addAddrBtn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Log.i("주소설정페이지", "주소입력창 클릭");
                int status = NetworkStatus.getConnectivityStatus(getApplicationContext());
                if(status == NetworkStatus.TYPE_MOBILE || status == NetworkStatus.TYPE_WIFI) {
                    Log.i("주소설정페이지", "주소입력창 클릭");
                    Intent i = new Intent(AddMissionActivity.this, AddressApiActivity.class);
                    // 화면전환 애니메이션 없애기
                    overridePendingTransition(0, 0);
                    // 주소결과
                    startActivityForResult(i, SEARCH_ADDRESS_ACTIVITY2);
                }else {
                    Toast.makeText(getApplicationContext(), "인터넷 연결을 확인해주세요.", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // 퀘스트 목록 리사이클러뷰
        /*private Context mContext;
        private ArrayList<QuestData> mArrayList;
        private QuestAdapter mQuestAdapter;
        private RecyclerView mQuestRecyclerView;
        private EditText quest_name;
        private Button quest_save;
        */
        mContext = getApplicationContext();
        quest_name = findViewById(R.id.addQuest);
        quest_save = findViewById(R.id.addQuestButton);
        mQuestRecyclerView = findViewById(R.id.addQuestRecyclerView);

        LinearLayoutManager layoutManager = new LinearLayoutManager(mContext, LinearLayoutManager.VERTICAL, false);
        mQuestRecyclerView.setLayoutManager(layoutManager);

        mArrayList = new ArrayList<>();
        mArrayList.add(new QuestData("(필수) 중간 지점에서 사진 찍어 보내기"));
        mArrayList.add(new QuestData("(필수) 신호등 있으면 안전하게 건너기"));
        mArrayList.add(new QuestData("(필수) 심부름 내용 잘 수행하기"));

        mQuestAdapter = new QuestAdapter(mContext, mArrayList);
        mQuestRecyclerView.setAdapter(mQuestAdapter);

        quest_save.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (quest_name.getText().length()==0){
                    Toast.makeText(mContext, "퀘스트 내용을 입력하세요", Toast.LENGTH_LONG).show();
                } else {
                    String quest = quest_name.getText().toString();
                    quest_name.setText("");
                    QuestData data = new QuestData(quest);

                    mArrayList.add(data);
                    mQuestAdapter.notifyItemInserted(mArrayList.size()-1);
                }
            }
        });
        quest = new ArrayList<String>();

        // 위험 지역 확인 체크
        //risk_chk = (CheckBox) findViewById(R.id.risk_chk);
        //risk_chk.setOnCheckedChangeListener(AddMissionActivity.this);

        All = findViewById(R.id.button4);
        All.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*String user_Id, String UUID, String E_date, String E_content,
                double target_latitude, double target_longitude,
                double start_latitude, double start_longitude,
                boolean checking*/

                String childUUID = child[childnum1];
                String E_content = edit_content.getText().toString();
                String E_date = y+"-"+m+"-"+d+"T"+h+":"+mi;
                if (childUUID.equals(null)){
                    Toast.makeText(AddMissionActivity.this, "자녀를 선택하세요", Toast.LENGTH_LONG).show();
                } else if (E_content.equals(null)){
                    Toast.makeText(AddMissionActivity.this, "심부름 내용을 입력하세요", Toast.LENGTH_LONG).show();
                } else if (E_date.equals(null)){
                } else if (target_longitude == 0 && target_latitude == 0){
                    Toast.makeText(AddMissionActivity.this, "목적지 주소의 위도, 경도값이 올바르지 않습니다. 목적지를 다시 입력해주세요.", Toast.LENGTH_LONG).show();
                } else if (start_latitude == 0 && start_longitude == 0){
                    Toast.makeText(AddMissionActivity.this, "출발지 주소의 위도, 경도값이 올바르지 않습니다. 목적지를 다시 입력해주세요.", Toast.LENGTH_LONG).show();
                } else if (childUUID != null && E_content != null && E_date != null && target_latitude != 0 && target_longitude != 0 && start_longitude != 0 && start_latitude != 0){
                    for (int i =0; i < mArrayList.size(); i++) {
                        Log.i("퀘스트 내용 ", String.valueOf(mArrayList.get(i).getQuest()));
                        quest.add(mArrayList.get(i).getQuest().toString());
                    }
                     try {
                        registerErrand(childUUID, E_date, E_content,
                                target_latitude,target_longitude,edit_addr.getText().toString(), edit_addr2.getText().toString(), quest, start_latitude,start_longitude,true);
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    Toast.makeText(AddMissionActivity.this, "심부름을 시작합니다", Toast.LENGTH_LONG).show();
                    // add to json
                    AddErrandDataToJson();
                    finish();
                }
                //registerErrand(ProfileData.getUserId(), childUUID, E_date, E_content,
                   //     target_latitude,target_longitude,0,0,true);
                //Activity MainActivity = new MainActivity;
                //((com.example.safe_map.MainActivity) MainActivity).CallCheckMap();
            }
        });

    }


    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        Log.i("test", "onActivityResult");
        switch (requestCode) {
            case SEARCH_ADDRESS_ACTIVITY:
                if (resultCode == RESULT_OK) {
                    String data = intent.getExtras().getString("data");
                    if (data != null) {
                        Log.i("test", "data:" + data);
                        edit_addr.setText(data);
                        Geocoder geocoder = new Geocoder(AddMissionActivity.this);
                        try {
                            address = geocoder.getFromLocationName(data, 10);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if (address != null) {
                            if (address.size() == 0){
                                Log.i("nonono","0");
                                Toast.makeText(AddMissionActivity.this,"해당되는 주소의 위도, 경도 값을 찾을 수 없습니다",Toast.LENGTH_LONG).show();
                            } else {
                                Address addr = address.get(0);
                                Log.i("address 변환 ok" , String.valueOf(addr.getLatitude()));
                                target_latitude = addr.getLatitude();
                                target_longitude = addr.getLongitude();
                                //Toast.makeText(AddMissionActivity.this,"해당되는 주소의 위도, 경도 값을 설정하였습니다",Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                }
                break;
            case SEARCH_ADDRESS_ACTIVITY2:
                if (resultCode == RESULT_OK) {
                    String data = intent.getExtras().getString("data");
                    if (data != null) {
                        Log.i("test", "data:" + data);
                        edit_addr2.setText(data);
                        Geocoder geocoder = new Geocoder(AddMissionActivity.this);
                        try {
                            address = geocoder.getFromLocationName(data, 10);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if (address != null) {
                            if (address.size() == 0){
                                Log.i("nonono","0");
                                Toast.makeText(AddMissionActivity.this,"해당되는 주소의 위도, 경도 값을 찾을 수 없습니다",Toast.LENGTH_LONG).show();
                            } else {
                                Address addr = address.get(0);
                                Log.i("address 변환 ok" , String.valueOf(addr.getLatitude()));
                                start_latitude = addr.getLatitude();
                                start_longitude = addr.getLongitude();
                                //Toast.makeText(AddMissionActivity.this,"출발지 주소의 위도, 경도 값을 설정하였습니다",Toast.LENGTH_LONG).show();
                            }
                        }
                    }
                }
                break;
        }
    }

    public String[] fetchUUID(String userId){
        String url = CommonMethod.ipConfig + "/api/fetchUUID";
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

    public void registerErrand(String UUID, String E_date, String E_content,
                               double target_latitude, double target_longitude, String target_name,
                               String start_name, List<String> quest,double start_latitude, double start_longitude,
                               boolean checking) throws JSONException {
        String url = CommonMethod.ipConfig + "/api/registerErrand";

//        System.out.println(UUID+ E_date+ E_content+ target_latitude+ target_longitude+ target_name+ start_name+ start_latitude+ start_longitude+ checking +"확인하기11");

        try{
            String jsonString = new JSONObject()
                    .put("UUID", UUID)
                    .put("E_date", E_date)
                    .put("E_content", E_content)
                    .put("target_latitude", target_latitude)
                    .put("target_longitude", target_longitude)
                    .put("target_name", target_name)
                    .put("start_name", start_name)
                    .put("quest", quest)
                    .put("start_latitude", start_latitude)
                    .put("start_longitude", start_longitude)
                    .put("checking", checking)
                    .toString();
            //REST API
            RequestHttpURLConnection.NetworkAsyncTask networkTask = new RequestHttpURLConnection.NetworkAsyncTask(url, jsonString);
            networkTask.execute().get();
            Toast.makeText(AddMissionActivity.this, "심부름이 설정되었습니다", Toast.LENGTH_LONG).show();

            Log.i("심부름 설정하기 - target name 추가", target_name);
            for (int i =0; i < quest.size(); i++) {
                Log.i("퀘스트 내용 ", String.valueOf(quest.get(i)));
            }
            Log.i("심부름 설정하기-target name 추가", target_name);

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public String fetchChildNum(String userId){
        String url = CommonMethod.ipConfig + "/api/fetchChildNum";
        String rtnStr= "";

        try{
            String jsonString = new JSONObject()
                    .put("userId", ProfileData.getUserId())
                    .toString();
            //REST API
            RequestHttpURLConnection.NetworkAsyncTask networkTask = new RequestHttpURLConnection.NetworkAsyncTask(url, jsonString);
            rtnStr = networkTask.execute().get();
        }catch(Exception e){
            e.printStackTrace();
        }
        return rtnStr;
    }

    public void AddErrandDataToJson(){
        String filename = "ErrandInfo.json";

        JSONObject sObject = new JSONObject();//배열 내에 들어갈 json

        // 1. json에 넣을 데이터 만들기
        try {

            sObject.put("src_lat", start_latitude);
            sObject.put("src_lon", start_longitude);
            sObject.put("src_name", start_name);
            sObject.put("dst_lat", target_latitude);
            sObject.put("dst_lon", target_longitude);
            sObject.put("dst_name", target_name);

        } catch (JSONException e) {
            e.printStackTrace();
        }
        // 2. json파일을 열어 데이터 저장.
        try {
            String jsonStr = sObject.toString();

            Log.d("resttt",""+jsonStr);
            FileOutputStream fos = new FileOutputStream(getFilesDir()+"/"+filename);
            fos.write(jsonStr.getBytes());
            fos.close();
        } catch (FileNotFoundException fileNotFound) {
            fileNotFound.printStackTrace();
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

    }
}