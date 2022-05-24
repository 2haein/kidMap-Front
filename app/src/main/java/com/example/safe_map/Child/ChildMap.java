package com.example.safe_map.Child;

import static android.app.PendingIntent.getActivity;
import static java.security.AccessController.getContext;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

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
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.example.safe_map.FHome.DangerPoint;
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

public class ChildMap extends AppCompatActivity{
    Fragment ChildMapView;
    String UUID;

    // json에서 받아온다.
    ArrayList<TMapPoint> safe_path = new ArrayList<>();
    // 위험지역
    ArrayList<DangerPoint> DangerZone = new ArrayList<>();
    // 안전 경로 길이
    int path_size = 0;


    TMapView tMapView;
    RelativeLayout mapView;
    double latitude, longitude;

    ViewGroup mapViewContainer;
    private LocationManager manager;
    private static final int REQUEST_CODE_LOCATION = 2;

    ImageButton home, camera, call, qr;
    String number = "0100000000";
    File file;

    private boolean m_bTrackingMode = true;

    private TMapGpsManager tmapgps = null;
    //private static String mApiKey = "앱키입력하기"; // 발급받은 appKey
    private static int mMarkerID;

    //private ArrayList<TMapPoint> m_tmapPoint = new ArrayList<TMapPoint>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_map);
        //ChildMapView = new ChildMapView();

        //getSupportFragmentManager().beginTransaction().replace(R.id.fragmentContainerView,ChildMapView).commitAllowingStateLoss();
        home = (ImageButton) findViewById(R.id.homeaddr);
        camera = (ImageButton) findViewById(R.id.camera);
        call = (ImageButton) findViewById(R.id.call);

        // 아이의 위치 수신을 위한 세팅
        manager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        //gpsListener = new GPSListener();


        // 아이의 현재 위치
        getMyLocation();

        //mapview 세팅
        mapView = (RelativeLayout) findViewById(R.id.childMapView2);
        tMapView = new TMapView(this);


        tMapView.setSKTMapApiKey("l7xx94f3b9ca30ba4d16850a60f2c3ebfdd5");
        //tMapView.setLocationPoint(latitude,longitude);
        //tMapView.setCenterPoint(latitude,longitude);
        tMapView.setCompassMode(true);
        tMapView.setIconVisibility(true);
        tMapView.setZoomLevel(18); // 클수록 확대
        tMapView.setMapType(TMapView.MAPTYPE_STANDARD);  //일반지도
        tMapView.setLanguage(TMapView.LANGUAGE_KOREAN);

        tmapgps = new TMapGpsManager(this);
        tmapgps.setMinTime(1000);
        tmapgps.setMinDistance(5);
        tmapgps.setProvider(tmapgps.NETWORK_PROVIDER); //연결된 인터넷으로 현 위치를 받습니다.
        //실내일 때 유용합니다.
        tmapgps.setProvider(tmapgps.GPS_PROVIDER); //gps로 현 위치를 잡습니다.
        tmapgps.OpenGps();

        tMapView.setTrackingMode(true);
        tMapView.setSightVisible(true);
        mapView.addView(tMapView);


        // 1. 심부름 정보(안전 경로) 받아오기
        GetErrandDataFromJson();

        // 2. 위험 지역 파싱 : 나중에 db에서 사용사 신고도 받아와야됨
        ParseDangerZone();

        // 3. 위험 지역 띄우기
        ShowDangerZoneOnMap();

        // 4. 안전 경로 띄우기
        ShowPathOnMap();



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
     * @return
     */
    private Location getMyLocation() {
        final Location[] currentLocation = {null};
        // Register the listener with the Location Manager to receive location updates
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            System.out.println("////////////사용자에게 권한을 요청해야함");
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, this.REQUEST_CODE_LOCATION);
            getMyLocation();
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

        return currentLocation[0];
    }

    /*private Location getMyLocation() {
        Location currentLocation = null;
        // Register the listener with the Location Manager to receive location updates
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
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
    }*/



    private float getDistance(double lat1, double lon1, double lat2, double lon2) {
        float[] distance = new float[2];
        Location.distanceBetween(lat1, lon1, lat2, lon2, distance);
        return distance[0];
    }

    /*private static double distance_in_meter(final double lat1, final double lon1, final double lat2, final double lon2) {
        double R = 6371000f; // Radius of the earth in m
        double dLat = (lat1 - lat2) * Math.PI / 180f;
        double dLon = (lon1 - lon2) * Math.PI / 180f;
        double a = Math.sin(dLat/2) * Math.sin(dLat/2) +
                Math.cos(latlong1.latitude * Math.PI / 180f) * Math.cos(latlong2.latitude * Math.PI / 180f) *
                        Math.sin(dLon/2) * Math.sin(dLon/2);
        double c = 2f * Math.atan2(Math.sqrt(a), Math.sqrt(1-a));
        double d = R * c;
        return d;
    }*/

    // 자녀의 정보 불러오기
    public String fetchChild(String UUID){
        String url = CommonMethod.ipConfig + "/api/fetchChild";
        String rtnStr= "";

        try{
            String jsonString = new JSONObject()
                    .put("UUID", UUID)
                    .toString();

            //REST API
            RequestHttpURLConnection.NetworkAsyncTask networkTask = new RequestHttpURLConnection.NetworkAsyncTask(url, jsonString);
            rtnStr = networkTask.execute().get();
//          Toast.makeText(getActivity(), "자녀 등록을 완료하였습니다.", Toast.LENGTH_SHORT).show();
//           Log.i(TAG, String.format("가져온 Phonenum: (%s)", rtnStr));
        }catch(Exception e){
            e.printStackTrace();
        }
        return rtnStr;

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




    private boolean GetErrandDataFromJson() {
        String jsonString = null;
        try {
            String filename = "PathInfo.json";
            FileInputStream fos = new FileInputStream(this.getFilesDir()+"/"+filename);
            // InputStream is = mContext.getAssets().open(getFilesDir()+ErrandInfo.json");
            //Log.d("resttt",""+fos.available());
            int size = fos.available();
            byte[] buffer = new byte[size];
            fos .read(buffer);
            fos .close();
            jsonString = new String(buffer, "UTF-8");
            Log.d("test","Parse jsonString : "+ jsonString);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        try{
            safe_path.clear();
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray jsonarray2 = (JSONArray) jsonObject.get("coords");
            Log.d("test","Parse 2: "+ jsonarray2);

            for (int i = 0; i < jsonarray2.length(); i++) {
                JSONObject jsonobject = jsonarray2.getJSONObject(i);

                double lat = Double.parseDouble(String.valueOf(jsonobject.getString("lat")));
                double lon = Double.parseDouble(String.valueOf(jsonobject.getString("lng")));
                TMapPoint mp = new TMapPoint(lat, lon);
                safe_path.add(mp);
            }
            return true;

        }catch (JSONException e) {
            e.printStackTrace();
            return false;
        }
    }

    void ParseDangerZone(){
        String jsonString = null;
        try {
            InputStream is = getAssets().open("test_danger.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            jsonString = new String(buffer, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        try{
            JSONObject jsonObject = new JSONObject(jsonString);
            JSONArray nodeArray = jsonObject.getJSONArray("coords");

            for(int i=0; i< nodeArray.length(); i++)
            {
                JSONArray Object =  (JSONArray) nodeArray.get(i);
                DangerPoint dp = new DangerPoint();

                dp.SetType((double) Object.get(0));
                dp.SetLat((double) Object.get(1));
                dp.SetLng((double) Object.get(2));
                DangerZone.add(dp);
                Log.d("test","danger lat: "+(double) Object.get(1) +" lon: "+ (double) Object.get(2));
            }

            // Log.d("test",""+"as.nodes size : " + nodes.size());
        }catch (JSONException e) {
            e.printStackTrace();
        }
    }

    void ShowPathOnMap(){
        TMapPolyLine tMapPolyLine = new TMapPolyLine();

        // 경로의 길이
        path_size =  safe_path.size();

        // safe_path.get(0) = 출발 지점     >> 출발지점, 도착지점 둘 다 선으로 연결하지는 않는다.
        TMapMarkerItem markerItem1 = new TMapMarkerItem();
        TMapPoint mark_point1 = new TMapPoint(safe_path.get(0).getLatitude(),safe_path.get(0).getLongitude());
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.jhome);
        markerItem1.setIcon(bitmap); // 마커 아이콘 지정
        markerItem1.setPosition(0.5f, 1.0f); // 마커의 중심점을 중앙, 하단으로 설정
        markerItem1.setTMapPoint( mark_point1 ); // 마커의 좌표 지정
        markerItem1.setName("출발"); // 마커의 타이틀 지정
        tMapView.addMarkerItem("markerItem1", markerItem1); // 지도에 마커 추가


        // safe_path.get(size-1) = 도착 지점  >> 출발지점, 도착지점 둘 다 선으로 연결하지는 않는다.
        TMapMarkerItem markerItem2 = new TMapMarkerItem();
        TMapPoint mark_point2 = new TMapPoint(safe_path.get(path_size -1).getLatitude(),safe_path.get(path_size -1).getLongitude());
        Bitmap bitmap2 = BitmapFactory.decodeResource(getResources(), R.drawable.jmarker);
        markerItem2.setIcon(bitmap2); // 마커 아이콘 지정
        markerItem2.setPosition(0.5f, 1.0f); // 마커의 중심점을 중앙, 하단으로 설정
        markerItem2.setTMapPoint( mark_point2 ); // 마커의 좌표 지정
        markerItem2.setName("도착"); // 마커의 타이틀 지정
        tMapView.addMarkerItem("markerItem2", markerItem2); // 지도에 마커 추가

        // 지도 중심 변경
        tMapView.setCenterPoint(safe_path.get(path_size/2).getLongitude(),safe_path.get(path_size/2).getLatitude());


        // index 1 ~ size-2 : A* 알고리즘으로 구한 좌표들. 선으로 이어준다.
        for(int i =1 ; i < safe_path.size()-1 ; i ++){
            tMapPolyLine.addLinePoint(new TMapPoint(safe_path.get(i).getLatitude(),safe_path.get(i).getLongitude()));
        }
        tMapView.addTMapPolyLine("Safe_Path", tMapPolyLine);

    }

    private void ShowDangerZoneOnMap() {

        int RED = 0;
        int GREEN = 0;
        int BLUE = 0;
        String TAG = "";
        Bitmap bitmap;

        for (int o = 0; o < DangerZone.size(); o++) {
            // 만약 위험 지역이 성범죄자 거주 구역이라면
            if (DangerZone.get(o).GetType() == 1.0) {
                RED = 255;
                GREEN = 0;
                BLUE = 0;
                TAG = "성범죄자 거주 구역";

                // 나중에 맞는 마커로 바꿀 것
                bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.jmarker);
            }
            // 보행자 사고 다발 지역인 경우
            else if(DangerZone.get(o).GetType() == 2.0){
                RED = 0;
                GREEN = 255;
                BLUE = 0;
                TAG = "보행자 사고 다발 구역";

                // 나중에 맞는 마커로 바꿀 것
                bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.jmarker);
            }
            // 자전거 사고 다발 지역인 경우
            else if(DangerZone.get(o).GetType() == 3.0){
                RED = 0;
                GREEN = 0;
                BLUE = 255;
                TAG = "자전거 사고 다발 구역";

                // 나중에 맞는 마커로 바꿀 것
                bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.jmarker);
            }
            // 교통사고 주의 구간인 경우
            else{
                RED = 255;
                GREEN = 255;
                BLUE = 255;
                TAG = "교통사고 주의 구역";
                bitmap = BitmapFactory.decodeResource(getResources(),R.drawable.jmarker);
            }

            // 원 그리기
            TMapPoint tp = new TMapPoint(DangerZone.get(o).GetLat(), DangerZone.get(o).GetLng());
            TMapCircle tMapCircle = new TMapCircle();
            tMapCircle.setCenterPoint( tp );
            tMapCircle.setRadius(10);    // 단위 : 미터
            tMapCircle.setCircleWidth(2);
            tMapCircle.setLineColor(Color.argb(128, 0, 0, 0));
            tMapCircle.setAreaColor(Color.argb(128, RED, GREEN, BLUE));
            tMapCircle.setAreaAlpha(100);
            tMapView.addTMapCircle("circle", tMapCircle);

            // 마커 찍기
            TMapMarkerItem markerItem2 = new TMapMarkerItem();
            TMapPoint mark_point2 = new TMapPoint(DangerZone.get(o).GetLat(), DangerZone.get(o).GetLng());
            markerItem2.setIcon(bitmap); // 마커 아이콘 지정
            markerItem2.setPosition(0.5f, 1.0f); // 마커의 중심점을 중앙, 하단으로 설정
            markerItem2.setTMapPoint( mark_point2 ); // 마커의 좌표 지정
            markerItem2.setName("위험지역"); // 마커의 타이틀 지정
            tMapView.addMarkerItem("danger", markerItem2); // 지도에 마커 추가

        }
    }

}