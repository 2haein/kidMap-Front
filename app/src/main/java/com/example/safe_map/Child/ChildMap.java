package com.example.safe_map.Child;

import static android.app.PendingIntent.getActivity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.safe_map.FHome.Astar;
import com.example.safe_map.FHome.DangerPoint;
import com.example.safe_map.FHome.jPoint;
import com.example.safe_map.R;
import com.example.safe_map.common.ChildData;
import com.example.safe_map.common.ProfileData;
import com.example.safe_map.http.CommonMethod;
import com.example.safe_map.http.RequestHttpURLConnection;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

import com.skt.Tmap.TMapCircle;
import com.skt.Tmap.TMapPolyLine;
import com.skt.Tmap.TMapView;
import com.skt.Tmap.TMapGpsManager;
import com.skt.Tmap.TMapMarkerItem;
import com.skt.Tmap.TMapPoint;

public class ChildMap extends AppCompatActivity implements TMapGpsManager.onLocationChangedCallback{

    String UUID = ChildData.getChildId();
    String parent_id = ProfileData.getUserId();
    Astar astar = new Astar();
    Context mContext = ChildMap.this;


    // 경로 정보
    final int onfoot = 0;
    final int alley = 1;
    final int traffic = 2;
    final int crosswalk= 3;

    // 파싱 용도
    double src_lat,src_lon, dst_lat, dst_lon;
    String src_name, dst_name;

    // 지도 마커 용도
    jPoint jp_src = new jPoint();
    jPoint jp_dst = new jPoint();


    TMapView tMapView = null;
    private TMapGpsManager tmapgps = null;
    RelativeLayout mapView;
    double latitude, longitude;

    private LocationManager manager;
    private static final int REQUEST_CODE_LOCATION = 2;

    ImageButton home, camera, call, qr;
    String number = "0100000000";
    File file;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_map);

        home = (ImageButton) findViewById(R.id.homeaddr);
        camera = (ImageButton) findViewById(R.id.camera);
        call = (ImageButton) findViewById(R.id.call);

        // 아이의 위치 수신을 위한 세팅
        manager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        //gpsListener = new GPSListener();

        //mapview 세팅
        tMapView = new TMapView(this);

        tMapView.setSKTMapApiKey("l7xx94f3b9ca30ba4d16850a60f2c3ebfdd5");
      //  tMapView.setLocationPoint(latitude,longitude);
      //  tMapView.setCenterPoint(latitude,longitude);
        tMapView.setCompassMode(true);
        tMapView.setIconVisibility(true);
        tMapView.setZoomLevel(18); // 클수록 확대
        tMapView.setMapType(TMapView.MAPTYPE_STANDARD);  //일반지도
        tMapView.setLanguage(TMapView.LANGUAGE_KOREAN);
        tMapView.setTrackingMode(true);
        tMapView.setSightVisible(true);

        mapView = (RelativeLayout) findViewById(R.id.childMapView2);
        mapView.addView(tMapView);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[] {Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        tmapgps = new TMapGpsManager(this);
        tmapgps.setMinTime(1000);
        tmapgps.setMinDistance(10);
        tmapgps.setProvider(tmapgps.NETWORK_PROVIDER); //연결된 인터넷으로 현 위치를 받습니다.

        //실내일 때 유용합니다.
        //tmapgps.setProvider(tmapgps.GPS_PROVIDER); //gps로 현 위치를 잡습니다.
      //  tmapgps.OpenGps();

        // 1. 심부름 정보(안전 경로) 받아오기
        GetErrandData();

        // 2. 노드, 링크, 위험 지역 파싱
        ParseInformations();

        // 3. 안전 경로 찾기
        FindSafePath();

        // 4. 위험 지역 띄우기
        ShowDangerZoneOnMap();

        // 5. 안전 경로 띄우기
        ShowPathOnMap();

        // 아이의 현재 위치 5초 간격 서버에 전송
      //  sendLocation();

        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        File sdcard = Environment.getExternalStorageDirectory();
        file = new File(sdcard, "capture.jpg");
        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(getContext(), "com.example.safe_map.fileProvider",file));
                startActivityForResult(intent, 101);*/
            }
        });

        call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                number = fetchPhone(ProfileData.getUserId());
                if (number.equals("")) {
                    Toast.makeText(ChildMap.this, "부모님 전화번호가 저장되지 않았습니다.", Toast.LENGTH_LONG).show();
                } else {
                    Intent tt = new Intent("android.intent.action.DIAL", Uri.parse("tel:" + number));
                    startActivity(tt);
                }

            }
        });
    }


    private void ParseInformations(){
        astar.ParseNode(mContext);
        astar.ParseLinks(mContext);
        astar.ParseDanger(mContext);

    }

    private void FindSafePath() {
        // 1. 위험 지역을 찾는다.
        astar.FindDangerousNodeNum();

        // 2. 출발/도착지와 가장 가까운 노드번호를 찾는다.
        int start = astar.findCloseNode(jp_src);
        int end = astar.findCloseNode(jp_dst);

        // 3. 두 노드 번호를 이용하여 A* 알고리즘 실행.
        astar.AstarSearch(start, end);

        // 4. closeList를 탐색하여 경로 찾기
        astar.FindPath(start, end);

        // 5. 찾은 노드번호 경로를 이용하여 ( 출발 + 경로 + 도착 ) 좌표 리스트 추출.
        astar.GetCoordPath(jp_src.GetLat(), jp_src.GetLng(), jp_dst.GetLat(), jp_dst.GetLng());


    }

    private void GetErrandData() {
        // 1. 부모 아이디를 이용하여 심부름 좌표를 받아온다.
        String url = CommonMethod.ipConfig + "/api/fetchRecentErrand";
        String rtnStr= "";

        try{
            String jsonString = new JSONObject()
                    .put("userId", parent_id)
                    .toString();

            //REST API
            RequestHttpURLConnection.NetworkAsyncTask networkTask = new RequestHttpURLConnection.NetworkAsyncTask(url, jsonString);
            rtnStr = networkTask.execute().get();

            Log.d("test123","/api/fetchRecentErrand : "+ rtnStr);

            JSONObject Alldata = new JSONObject(rtnStr);

            // 2. 좌표 추출
            src_lat = Double.parseDouble(Alldata.getString("start_latitude"));
            src_lon = Double.parseDouble(Alldata.getString("start_longitude"));
            dst_lat = Double.parseDouble(Alldata.getString("target_latitude"));
            dst_lon = Double.parseDouble(Alldata.getString("target_longitude"));

            src_name = Alldata.getString("start_name");
            dst_name = Alldata.getString("target_name");

            // 3. 출발점, 도착점에 맞는 좌표 넣어줌
            jp_src.SetLat(src_lat);
            jp_src.SetLng(src_lon);
            jp_dst.SetLat(dst_lat);
            jp_dst.SetLng(dst_lon);

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onLocationChange(Location location) {
        tMapView.setLocationPoint(location.getLongitude(), location.getLatitude());
        tMapView.setCenterPoint(location.getLongitude(), location.getLatitude());
    }






    /*@Override
    public void onResume() {
        mapView.onResume();
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onPause(){
        mapViewContainer.removeView(mapView);
        getActivity().finish();
        super.onPause();
    }

    public void finish() {
        mapViewContainer.removeView(mapView);
        getActivity().finish();
    }*/

    /**
     * 아이의 위치를 수신
     */
    private void sendLocation() {
        final Location[] currentLocation = new Location[1];

        // Register the listener with the Location Manager to receive location updates
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            System.out.println("////////////사용자에게 권한을 요청해야함");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, this.REQUEST_CODE_LOCATION);
            sendLocation();
        } else {
            System.out.println("////////////권한요청 안해도됨");

            // 수동으로 위치 구하기
            Timer scheduler = new Timer();
            TimerTask task = new TimerTask() {
                private static final int REQUEST_CODE_LOCATION = 2;

                @Override
                public void run() {

                    String locationProvider = LocationManager.GPS_PROVIDER;
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling
                        //    ActivityCompat#requestPermissions
                        // here to request the missing permissions, and then overriding
                        //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                        //                                          int[] grantResults)
                        // to handle the case where the user grants the permission. See the documentation
                        // for ActivityCompat#requestPermissions for more details.
                        return;
                    }
                    currentLocation[0] = manager.getLastKnownLocation(locationProvider);
                    latitude = currentLocation[0].getLatitude();
                    longitude = currentLocation[0].getLongitude();
                    System.out.println("아이 현재 위치값 : " + latitude + "," + longitude);
                    registerChildLocation(UUID, latitude, longitude);

                    Log.i("아이 현재 위치 ", Double.valueOf(latitude).toString());

                    registerChildLocation(ChildData.getChildId(), latitude, longitude);

                }
            };

            scheduler.scheduleAtFixedRate(task, 0, 5000); // 5초 뒤 1초마다 반복실행*/
        }


    }

    private Location getMyLocation() {
        Location currentLocation = null;
        // Register the listener with the Location Manager to receive location updates
        if (ActivityCompat.checkSelfPermission((Activity) this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission((Activity) getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            System.out.println("////////////사용자에게 권한을 요청해야함");
            ActivityCompat.requestPermissions((Activity) getApplicationContext(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, this.REQUEST_CODE_LOCATION);
            getMyLocation();
        }
        else {
            System.out.println("////////////권한요청 안해도됨");

            // 수동으로 위치 구하기

            String locationProvider = LocationManager.GPS_PROVIDER;
            currentLocation = manager.getLastKnownLocation(locationProvider);
            if (currentLocation != null) {
                double lng = currentLocation.getLongitude();
                double lat = currentLocation.getLatitude();
            }
        }
        return currentLocation;
    }



    private float getDistance(double lat1, double lon1, double lat2, double lon2) {
        float[] distance = new float[2];
        Location.distanceBetween(lat1, lon1, lat2, lon2, distance);
        return distance[0];
    }


    // 현재 아이 위치 전송
    public static void registerChildLocation(String UUID, double current_latitude, double current_longitude){
        String url = CommonMethod.ipConfig + "/api/savePositionChild";

        try{
            String jsonString = new JSONObject()
                    .put("UUID", UUID)
                    .put("current_latitude", current_latitude)
                    .put("current_longitude", current_longitude)
                    .toString();

            //REST API
            RequestHttpURLConnection.NetworkAsyncTask networkTask = new RequestHttpURLConnection.NetworkAsyncTask(url, jsonString);
            networkTask.execute().get();
            //Toast.makeText(getContext().getApplicationContext(), "전화번호가 저장되었습니다", Toast.LENGTH_LONG).show();
            Log.i("현재위치 전송", String.format("등록한 현재 위치 : lat " + current_latitude + ", long " + current_longitude));

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    public String fetchPhone(String userId){
        String url = CommonMethod.ipConfig + "/api/fetchTelNum";
        String rtnStr= "";

        try{
            String jsonString = new JSONObject()
                    .put("userId", userId)
                    .toString();

            //REST API
            RequestHttpURLConnection.NetworkAsyncTask networkTask = new RequestHttpURLConnection.NetworkAsyncTask(url, jsonString);
            rtnStr = networkTask.execute().get();

//          Toast.makeText(getActivity(), "자녀 등록을 완료하였습니다.", Toast.LENGTH_SHORT).show();
            //Log.i(TAG, String.format("가져온 Phonenum: (%s)", rtnStr));

        }catch(Exception e){
            e.printStackTrace();
        }
        return rtnStr;
    }





    // astar.jp_path를 이용.
    // 출발(마커) + 경로 (선) + 도착(마커) 로 표시
    void ShowPathOnMap(){
        TMapPolyLine tMapPolyLine = new TMapPolyLine();
        tMapPolyLine.setLineColor(Color.argb(100,0,0,0));

        int size = astar.jp_path.size();

        // safe_path.get(0) = 출발 지점     >> 출발지점, 도착지점 둘 다 선으로 연결하지는 않는다.
        TMapMarkerItem markerItem1 = new TMapMarkerItem();
        TMapPoint mark_point1 = new TMapPoint(astar.jp_path.get(0).GetLat(),astar.jp_path.get(0).GetLng());
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.jhouse);
        markerItem1.setIcon(bitmap); // 마커 아이콘 지정
        markerItem1.setPosition(0.5f, 1.0f); // 마커의 중심점을 중앙, 하단으로 설정
        markerItem1.setTMapPoint( mark_point1 ); // 마커의 좌표 지정
        markerItem1.setName("출발"); // 마커의 타이틀 지정
        tMapView.addMarkerItem("markerItem1", markerItem1); // 지도에 마커 추가


        // safe_path.get(size-1) = 도착 지점  >> 출발지점, 도착지점 둘 다 선으로 연결하지는 않는다.
        TMapMarkerItem markerItem2 = new TMapMarkerItem();
        TMapPoint mark_point2 = new TMapPoint(astar.jp_path.get(size-1).GetLat(),astar.jp_path.get(size-1).GetLng());
        Bitmap bitmap2 = BitmapFactory.decodeResource(getResources(), R.drawable.jfinish_line);
        markerItem2.setIcon(bitmap2); // 마커 아이콘 지정
        markerItem2.setPosition(0.5f, 1.0f); // 마커의 중심점을 중앙, 하단으로 설정
        markerItem2.setTMapPoint( mark_point2 ); // 마커의 좌표 지정
        markerItem2.setName("도착"); // 마커의 타이틀 지정
        tMapView.addMarkerItem("markerItem2", markerItem2); // 지도에 마커 추가

        // 지도 중심 변경
        tMapView.setCenterPoint(astar.jp_path.get(size/2).GetLat(), astar.jp_path.get(size/2).GetLng());


        // index 1 ~ size-2 : A* 알고리즘으로 구한 좌표들. 선으로 이어준다.
        for(int i =1 ; i < astar.jp_path.size() -1 ; i ++){
            tMapPolyLine.addLinePoint(new TMapPoint( astar.jp_path.get(i).GetLat(),astar.jp_path.get(i).GetLng()));
        }
        tMapView.addTMapPolyLine("Safe_Path", tMapPolyLine);

    }

    private void ShowDangerZoneOnMap() {

        int RED = 0;
        int GREEN = 0;
        int BLUE = 0;
        String TAG = "";
        Bitmap bitmap;

        for (int o = 0; o < astar.DangerZone.size(); o++) {
            // 만약 위험 지역이 성범죄자 거주 구역이라면
            if (astar.DangerZone.get(o).GetType() == 1.0) {
                RED = 255;
                GREEN = 0;
                BLUE = 0;
                TAG = "성범죄자 거주 구역";

                // 나중에 맞는 마커로 바꿀 것
                bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.jdevil);
            }
            // 보행자 사고 다발 지역인 경우
            else if(astar.DangerZone.get(o).GetType() == 2.0){
                RED = 0;
                GREEN = 255;
                BLUE = 0;
                TAG = "보행자 사고 다발 구역";

                // 나중에 맞는 마커로 바꿀 것
                bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.jaccident);
            }
            // 자전거 사고 다발 지역인 경우
            else if(astar.DangerZone.get(o).GetType() == 3.0){
                RED = 0;
                GREEN = 0;
                BLUE = 255;
                TAG = "자전거 사고 다발 구역";

                // 나중에 맞는 마커로 바꿀 것
                bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.jcyclist);
            }
            // 교통사고 주의 구간인 경우
            else if(astar.DangerZone.get(o).GetType() == 4.0){
                RED = 255;
                GREEN = 255;
                BLUE = 255;
                TAG = "교통사고 주의 구역";
                bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.jaccident);
            }
            else{
                RED = 255;
                GREEN = 255;
                BLUE = 255;
                TAG = "시민 신고 지역";
                bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.sirenback);

            }


            // 마커 찍기
            TMapMarkerItem markerItem2 = new TMapMarkerItem();
            TMapPoint mark_point2 = new TMapPoint(astar.DangerZone.get(o).GetLat(), astar.DangerZone.get(o).GetLng());
            markerItem2.setIcon(bitmap); // 마커 아이콘 지정
            markerItem2.setPosition(0.5f, 1.0f); // 마커의 중심점을 중앙, 하단으로 설정
            markerItem2.setTMapPoint( mark_point2 ); // 마커의 좌표 지정
            markerItem2.setName("위험지역"); // 마커의 타이틀 지정
            tMapView.addMarkerItem("danger"+o, markerItem2); // 지도에 마커 추가

        }
    }

}