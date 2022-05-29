package com.safekid.safe_map.FHome;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.safekid.safe_map.http.CommonMethod;
import com.safekid.safe_map.Login.ChildnumItem;
import com.safekid.safe_map.Login.StdRecyclerAdapter;
import com.safekid.safe_map.NetworkStatus;
import com.safekid.safe_map.R;
import com.safekid.safe_map.common.ProfileData;
import com.safekid.safe_map.http.RequestHttpURLConnection;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

public class AddMissionActivity extends AppCompatActivity implements CompoundButton.OnCheckedChangeListener {

    // 초기변수설정
    public Integer childnum1 = 0, selectChild = null;
    public String[] UUIDArray;
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

    // 길 찾기 관련 변수들
    ArrayList<jPoint> safe_path = new ArrayList<>(); // 안전 경로

    // 퀘스트 관련 변수들
    int trafficLight_num = 0;// 신호등
    int crossWalk_num = 0; // 횡단보도
    int onFoot_num = 0;  // 도보
    int alley_num = 0;  // 골목
    int driveWay_num = 0;


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

    // 주소 좌표 변환
    String x_a, y_a, x_b, y_b;
    String addr1, addr2;

    @SuppressLint("WrongViewCast")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_mission);
        int SDK_INT = android.os.Build.VERSION.SDK_INT;
        if (SDK_INT > 8)
        {
            StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder()
                    .permitAll().build();
            StrictMode.setThreadPolicy(policy);

        }

        UUIDArray = fetchUUID(ProfileData.getUserId());

        mRecyclerView = (RecyclerView) findViewById(R.id.recyclerView2);
        /* initiate adapter */
        mRecyclerAdapter = new StdRecyclerAdapter(this);

        final String childnum = fetchChildNum(ProfileData.getUserId());
        final int childNum = Integer.parseInt(childnum);
        /* adapt data */
        mChildnum = new ArrayList<>();
        for(int i = 1; i<= childNum; i++){
            if (i==1){
                mChildnum.add(new ChildnumItem("첫째아이",UUIDArray[0]) );
            } else if (i==2){
                mChildnum.add(new ChildnumItem("둘째아이", UUIDArray[1]));
            } else if (i==3){
                mChildnum.add(new ChildnumItem("셋째아이", UUIDArray[2]));
            } else if (i==4){
                mChildnum.add(new ChildnumItem("넷째아이", UUIDArray[3]));
            } else if (i==5) {
                mChildnum.add(new ChildnumItem("다섯째아이", UUIDArray[4]));
            } else {
                mChildnum.add(new ChildnumItem("자녀가 없습니다", "X"));
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
//                mRecyclerAdapter.clearSelectedItem();
                selectChild = a_position;
                Toast.makeText(AddMissionActivity.this, Integer.toString(selectChild+1), Toast.LENGTH_SHORT).show();
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
                }, h1, mi1, false);
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
        mArrayList.add(new QuestData("(필수) 중간 지점을 거쳐 경로 따라가기"));
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

                String childUUID = "";
                if(selectChild != null) {
                    childUUID = child[selectChild];
                }
                if (addr1 != null) {
                    requestGeocode(addr1);
                    Log.i("addr1 ", addr1);
                    target_latitude = Double.parseDouble(y_a);
                    target_longitude = Double.parseDouble(x_a);
                    Log.i("target lat ", String.valueOf(target_latitude));
                    Log.i("target lgt ", String.valueOf(target_longitude));
                }
                if (addr2 != null) {
                    requestGeocode2(addr2);
                    Log.i("addr1 ", addr2);
                    start_latitude = Double.parseDouble(y_b);
                    start_longitude = Double.parseDouble(x_b);
                    Log.i("target lat ", String.valueOf(start_latitude));
                    Log.i("target lgt ", String.valueOf(start_longitude));
                }
                String E_content = "";
                E_content = edit_content.getText().toString();
                Log.i("E_content ", E_content);
                String E_date = y+"-"+m+"-"+d+"T"+h+":"+mi;
                if (childUUID.equals("")){
                    Toast.makeText(AddMissionActivity.this, "자녀를 선택하세요", Toast.LENGTH_LONG).show();
                } else if (E_content == ""){
                    Toast.makeText(AddMissionActivity.this, "심부름 내용을 입력하세요", Toast.LENGTH_LONG).show();
                } else if (E_date == ""){
                } else if (target_longitude == 0 && target_latitude == 0){
                    Toast.makeText(AddMissionActivity.this, "목적지 주소의 위도, 경도값이 올바르지 않습니다. 목적지를 다시 입력해주세요.", Toast.LENGTH_LONG).show();
                } else if (start_latitude == 0 && start_longitude == 0){
                    Toast.makeText(AddMissionActivity.this, "출발지 주소의 위도, 경도값이 올바르지 않습니다. 목적지를 다시 입력해주세요.", Toast.LENGTH_LONG).show();
                } else if (childUUID != null && E_content != "" && E_date != "" && target_latitude != 0 && target_longitude != 0 && start_longitude != 0 && start_latitude != 0){
                    for (int i =0; i < mArrayList.size(); i++) {
                        Log.i("퀘스트 내용 ", String.valueOf(mArrayList.get(i).getQuest()));
                        quest.add("\""+mArrayList.get(i).getQuest().toString()+"\"");
                    }
                     try {
                        registerErrand(childUUID, E_date, E_content,
                                target_latitude,target_longitude,edit_addr.getText().toString(), edit_addr2.getText().toString(), quest, start_latitude,start_longitude,true);
                        updateErrandChecking();

                     } catch (JSONException e) {
                         Toast.makeText(AddMissionActivity.this, "error", Toast.LENGTH_LONG).show();

                         e.printStackTrace();
                    }
                    Toast.makeText(AddMissionActivity.this, "심부름을 시작합니다", Toast.LENGTH_LONG).show();
                    ProfileData.setErrandChildId(childUUID);
                    //ProfileData.setcheckmapFlag(true);
                    finish();

                }

  // 여기서 퀘스트 메이커가 이뤄주ㅝ얗 ㅏㄴ다.

                //registerErrand(ProfileData.getUserId(), childUUID, E_date, E_content,
                   //     target_latitude,target_longitude,0,0,true);
                //Activity MainActivity = new MainActivity;
                //((com.safekid.safe_map.MainActivity) MainActivity).CallCheckMap();
            }
        });

    }


    // 지오코딩 된 출발, 도착지를 이용하여 1)안전 경로를 찾고, 2) 경로 환경을 파악하고, 3)제이슨에 저장한다.
    private void Find_Safe_Path(double start_latitude, double start_longitude, double target_latitude, double target_longitude) {
        Astar astar = new Astar();
        jPoint jp_start = new jPoint(start_latitude, start_longitude);
        jPoint jp_end = new jPoint(target_latitude, target_longitude);

        // 1-1) 노드, 링크, 위험 지역 파싱
        astar.ParseNode(mContext);
        astar.ParseLinks(mContext);
        astar.ParseDanger(mContext);

        // 1-2) 출발, 도착지와 인접한 노드 번호를 찾는다.
        astar.FindDangerousNodeNum();
        int start = astar.findCloseNode(jp_start);
        int end = astar.findCloseNode(jp_end);

       // Log.d("test","1. start :"+ start);
       // Log.d("test","1. end: "+ end);

        // 1-3) 위에서 찾은 노드 번호를 이용하여 길 찾기 수행
        astar.AstarSearch(start, end);
        astar.FindPath(start, end);
        astar.GetCoordPath(start_latitude, start_longitude, target_latitude, target_longitude);
        //Log.d("test","2. path size: "+ astar.jp_path.size());

        // 1-4) 경로로부터 요소 정보 추출
        astar.GetPathInfo();
        trafficLight_num = astar.traffic;
        crossWalk_num = astar.crosswalk;
        onFoot_num = astar.onfoot;
        alley_num = astar.alley;
      //  Log.d("test","3. t: "+ trafficLight_num);
      //  Log.d("test","3. c: "+ crossWalk_num);
       // Log.d("test","3. o: "+ onFoot_num);

        // 1-5) Json에 경로 저장.
        astar.Save_SafePath_To_Json(getFilesDir());

    }


    // 출발지와 목적지가 정해지면 신호등, 육교, 횡단보도, 골목길 등을 찾는다.
    // 찾은 결과에 따라 맞는 필수 퀘스트를 지정해준다.
    private void Quest_Maker(){

        String TrafficLight1 = "초록불일 때 만 길을 건너세요. 불이 깜빡거리면 멈춰서 다음 신호를 기다려요 (신호등 : ";
        String TrafficLight2 = "개 존재)";
        String CrossWalk1 = "건너기 전 항상 양 옆을 확인하여 차가 오는지 확인하고 건너세요 (횡단보도 : ";
        String CrossWalk2 = "개 존재)";
        String Alley = "갑자기 차가 튀어나올 수 있습니다. 주변을 살피며 가세요.";
        String DriveWay = "차가 지나다닙니다. 끝에 붙어서 다니세요";

        if(trafficLight_num >= 1){
            mArrayList.add(new QuestData(TrafficLight1+ trafficLight_num +TrafficLight2));
        }
        if(crossWalk_num >= 1){
            mArrayList.add(new QuestData(CrossWalk1 + crossWalk_num + CrossWalk2));
        }
        if(alley_num >= 1){
            mArrayList.add(new QuestData(Alley));
        }
        if(driveWay_num >= 1){
            mArrayList.add(new QuestData(DriveWay));
        }

        mQuestAdapter = new QuestAdapter(mContext, mArrayList);

        Log.d("test","QuestMaker 1");
        mQuestRecyclerView.setAdapter(mQuestAdapter);
        Log.d("test","QuestMaker 2");
    }


    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        Log.i("test", "onActivityResult");
        switch (requestCode) {
            case SEARCH_ADDRESS_ACTIVITY:
                if (resultCode == RESULT_OK) {
                    addr1 = intent.getExtras().getString("data");
                    Log.i("test", "addr1:" + addr1);
                    if (addr1 != null) {
                        edit_addr.setText(addr1);
                    }
                }
                break;
            case SEARCH_ADDRESS_ACTIVITY2:
                if (resultCode == RESULT_OK) {
                    addr2 = intent.getExtras().getString("data");
                    if (addr2 != null) {
                        Log.i("test", "addr2:" + addr2);
                        edit_addr2.setText(addr2);
                    }
                }
                break;
        }
    }

    /*public void onActivityResult(int requestCode, int resultCode, Intent intent) {
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
                                Log.i("test123","GEO1  lat : "+target_latitude + "lon : "+target_longitude );
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
                                Log.i("test123","GEO2  lat : "+start_latitude + "lon : "+start_longitude );
                                Find_Safe_Path(start_latitude,start_longitude,target_latitude, target_longitude);
                                Quest_Maker();
                            }
                        }
                    }
                }
                break;
        }
    }*/

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

//        System.out.println(quest);

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
                    .put("userId", ProfileData.getUserId())
                    .toString();
            //REST API
            RequestHttpURLConnection.NetworkAsyncTask networkTask = new RequestHttpURLConnection.NetworkAsyncTask(url, jsonString);
            networkTask.execute().get();
            Toast.makeText(AddMissionActivity.this, "심부름이 설정되었습니다", Toast.LENGTH_LONG).show();

            Log.i("심부름 설정하기-target name 추가", target_name);
            for (int i =0; i < quest.size(); i++) {
                Log.i("퀘스트 내용 ", String.valueOf(quest.get(i)));
            }
            Log.i("심부름 설정하기-target name 추가", target_name);

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public void updateErrandChecking(){
        String url = CommonMethod.ipConfig + "/api/updateErrandChecking";

        try{
            String jsonString = new JSONObject()
                    .put("userId", ProfileData.getUserId())
                    .put("isErrandComplete", false)
                    .toString();

            //REST API
            RequestHttpURLConnection.NetworkAsyncTask networkTask = new RequestHttpURLConnection.NetworkAsyncTask(url, jsonString);
            networkTask.execute().get();

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public String fetchErrandChecking(String userId){
        String url = CommonMethod.ipConfig + "/api/fetchErrandChecking";
        String rtnStr= "";

        try{
            String jsonString = new JSONObject()
                    .put("userId", userId)
                    .toString();

            //REST API
            RequestHttpURLConnection.NetworkAsyncTask networkTask = new RequestHttpURLConnection.NetworkAsyncTask(url, jsonString);
            rtnStr = networkTask.execute().get();
        }catch(Exception e){
            e.printStackTrace();
        }
        return rtnStr;
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


    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {

    }

    // naver map api로부터 위도 경도 받아오기
    public void requestGeocode(String addr){
        try{
            BufferedReader bufferedReader;
            StringBuilder stringBuilder = new StringBuilder();
            String query = "https://naveropenapi.apigw.ntruss.com/map-geocode/v2/geocode?query=" + URLEncoder.encode(addr,"UTF-8");
            URL url = new URL(query);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            if (conn != null) {
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                conn.setRequestMethod("GET");
                conn.setRequestProperty("X-NCP-APIGW-API-KEY-ID", "mvjxkqyyom");
                conn.setRequestProperty("X-NCP-APIGW-API-KEY", "vWS7ifOREQfxmxT4VZfy5VKcGUNGaWhFrTRJ3jTI");
                conn.setDoInput(true);

                int responseCode = conn.getResponseCode();

                if (responseCode == 200) {
                    bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                } else {
                    bufferedReader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                }

                String line  = null;
                while((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line + "\n");
                }
                int lat = stringBuilder.indexOf("\"x\":\"");
                int lgt = stringBuilder.indexOf("\",\"y\":");
                x_a = stringBuilder.substring(lat + 5, lgt);

                lat = stringBuilder.indexOf("\"y\":\"");
                lgt = stringBuilder.indexOf("\",\"distance\":");
                y_a = stringBuilder.substring(lat + 5, lgt);

                bufferedReader.close();
                conn.disconnect();
            }


        } catch (UnsupportedEncodingException | MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void requestGeocode2(String addr){
        try{
            BufferedReader bufferedReader;
            StringBuilder stringBuilder = new StringBuilder();
            String query = "https://naveropenapi.apigw.ntruss.com/map-geocode/v2/geocode?query=" + URLEncoder.encode(addr,"UTF-8");
            URL url = new URL(query);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            if (conn != null) {
                conn.setConnectTimeout(5000);
                conn.setReadTimeout(5000);
                conn.setRequestMethod("GET");
                conn.setRequestProperty("X-NCP-APIGW-API-KEY-ID", "mvjxkqyyom");
                conn.setRequestProperty("X-NCP-APIGW-API-KEY", "vWS7ifOREQfxmxT4VZfy5VKcGUNGaWhFrTRJ3jTI");
                conn.setDoInput(true);

                int responseCode = conn.getResponseCode();

                if (responseCode == 200) {
                    bufferedReader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                } else {
                    bufferedReader = new BufferedReader(new InputStreamReader(conn.getErrorStream()));
                }

                String line  = null;
                while((line = bufferedReader.readLine()) != null) {
                    stringBuilder.append(line + "\n");
                }
                int lat = stringBuilder.indexOf("\"x\":\"");
                int lgt = stringBuilder.indexOf("\",\"y\":");
                x_b = stringBuilder.substring(lat + 5, lgt);

                lat = stringBuilder.indexOf("\"y\":\"");
                lgt = stringBuilder.indexOf("\",\"distance\":");
                y_b = stringBuilder.substring(lat + 5, lgt);

                bufferedReader.close();
                conn.disconnect();
            }


        } catch (UnsupportedEncodingException | MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}