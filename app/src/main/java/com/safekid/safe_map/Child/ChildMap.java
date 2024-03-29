package com.safekid.safe_map.Child;

import static android.app.PendingIntent.getActivity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Looper;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.safekid.safe_map.http.CommonMethod;
import com.safekid.safe_map.FHome.Astar;
import com.safekid.safe_map.FHome.jPoint;
import com.safekid.safe_map.R;
import com.safekid.safe_map.common.ChildData;
import com.safekid.safe_map.common.ProfileData;
import com.safekid.safe_map.http.RequestHttpURLConnection;

import org.json.JSONObject;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import com.skt.Tmap.TMapCircle;
import com.skt.Tmap.TMapPolyLine;
import com.skt.Tmap.TMapView;
import com.skt.Tmap.TMapGpsManager;
import com.skt.Tmap.TMapMarkerItem;
import com.skt.Tmap.TMapPoint;

public class ChildMap extends AppCompatActivity implements TMapGpsManager.onLocationChangedCallback {

    private static final int PERMISSION_RQST_SEND = 8282;
    String UUID = ChildData.getChildId();
    String parent_id = ProfileData.getUserId();
    Astar astar = new Astar();
    Astar astar_t = new Astar();

    Context mContext = ChildMap.this;


    // 경로 정보
    final int onfoot = 0;
    final int alley = 1;
    final int traffic = 2;
    final int crosswalk = 3;

    // 파싱 용도
    double src_lat, src_lon, dst_lat, dst_lon;
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

    private TimerTask task;
    NotificationManager Nmanager;
    NotificationCompat.Builder builder;

    private static String CHANNEL_ID = "channel1";
    private static String CHANEL_NAME = "Channel1";


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_child_map);

        //home = (ImageButton) findViewById(R.id.homeaddr);
        //camera = (ImageButton) findViewById(R.id.camera);
        call = (ImageButton) findViewById(R.id.call);

        // 아이의 위치 수신을 위한 세팅
        manager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        getMyLocation();
        //gpsListener = new GPSListener();

        //mapview 세팅
        tMapView = new TMapView(this);

        tMapView.setSKTMapApiKey("l7xx94f3b9ca30ba4d16850a60f2c3ebfdd5");
        //tMapView.setLocationPoint(latitude,longitude);
        //tMapView.setCenterPoint(latitude,longitude);
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
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        }

        tmapgps = new TMapGpsManager(this);
        tmapgps.setMinTime(1000);
        tmapgps.setMinDistance(10);
        tmapgps.setProvider(tmapgps.GPS_PROVIDER); //연결된 인터넷으로 현 위치를 받습니다.

        //실내일 때 유용합니다.
        //tmapgps.setProvider(tmapgps.GPS_PROVIDER); //gps로 현 위치를 잡습니다.
        tmapgps.OpenGps();

        // 1. 심부름 정보(안전 경로) 받아오기
        GetErrandData();

        // 2. 노드, 링크, 위험 지역 파싱
        ParseInformations();

        // 3. 안전 경로 찾기
        FindSafePath();

        // 4. 위험 지역 띄우기
        ShowDangerZoneOnMap();

        // 5. 시작, 중간, 도착 지점 띄우기
        ShowSrcMidDstOnMap();

        // 6. 경로 정보 마커로 띄우기 ( 신호등, 횡단보도 )
        ShowPathInfoOnMap();

        // 아이의 현재 위치 5초 간격 서버에 전송
        sendLocation();

        /*home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        File sdcard = Environment.getExternalStorageDirectory();
        file = new File(sdcard, "capture.jpg");
        camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                *//*Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                intent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(getContext(), "com.safekid.safe_map.fileProvider",file));
                startActivityForResult(intent, 101);*//*
            }
        });*/

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


    private void ParseInformations() {
        astar.ParseNode(mContext);
        astar.ParseLinks(mContext);
        astar.ParseDanger(mContext);

        astar_t.ParseNode(mContext);
        astar_t.ParseLinks(mContext);
        astar_t.ParseDanger(mContext);
    }

    private void FindSafePath() {
        // 1. 위험 지역을 찾는다.
        astar.FindDangerousNodeNum();

        // 2. 출발/도착지와 가장 가까운 노드번호를 찾는다.
        int start = astar.findCloseNode(jp_src);
        int end = astar.findCloseNode(jp_dst);


        // 3. 두 노드 번호를 이용하여 A* 알고리즘 실행.
        astar.AstarSearch(start, end);
        astar_t.AstarSearch(start, end);

        // 4. closeList를 탐색하여 경로 찾기
        astar.FindPath(start, end);
        astar_t.FindPath(start, end);


        // 5. 찾은 노드번호 경로를 이용하여 ( 출발 + 경로 + 도착 ) 좌표 리스트 추출.
        astar.GetCoordPath(jp_src.GetLat(), jp_src.GetLng(), jp_dst.GetLat(), jp_dst.GetLng());
        astar_t.GetCoordPath(jp_src.GetLat(), jp_src.GetLng(), jp_dst.GetLat(), jp_dst.GetLng());

        // 6. 경로 정보 파악.
        astar.GetPathInfo();

    }

    private void GetErrandData() {
        // 1. 부모 아이디를 이용하여 심부름 좌표를 받아온다.
        String url = CommonMethod.ipConfig + "/api/fetchRecentErrand";
        String rtnStr = "";

        try {
            String jsonString = new JSONObject()
                    .put("userId", parent_id)
                    .toString();

            //REST API
            RequestHttpURLConnection.NetworkAsyncTask networkTask = new RequestHttpURLConnection.NetworkAsyncTask(url, jsonString);
            rtnStr = networkTask.execute().get();

            Log.d("ChildMap123", "/api/fetchRecentErrand : " + rtnStr);

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

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onLocationChange(Location location) {
        tMapView.setLocationPoint(location.getLongitude(), location.getLatitude());
        tMapView.setCenterPoint(location.getLongitude(), location.getLatitude());
    }

    /*public void getLocation() {
        // Get the location manager
        LocationManager locationManager = (LocationManager)
                getSystemService(LOCATION_SERVICE);
        Criteria criteria = new Criteria();
        String bestProvider = locationManager.getBestProvider(criteria, false);
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        Location location = locationManager.getLastKnownLocation(bestProvider);
        LocationListener loc_listener = new LocationListener() {

            public void onLocationChanged(Location l) {}

            public void onProviderEnabled(String p) {}

            public void onProviderDisabled(String p) {}

            public void onStatusChanged(String p, int status, Bundle extras) {}
        };
        locationManager
                .requestLocationUpdates(bestProvider, 5000, 0, loc_listener);
        location = locationManager.getLastKnownLocation(bestProvider);
        try {
            latitude = location.getLatitude();
            longitude = location.getLongitude();

        } catch (NullPointerException e) {
            latitude = -1.0;
            longitude = -1.0;
        }

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
            task = new TimerTask() {
                private static final int REQUEST_CODE_LOCATION = 2;

                @Override
                public void run() {
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                        return;
                    }
                    currentLocation[0] = manager.getLastKnownLocation(LocationManager.GPS_PROVIDER); // 야외 GPS_PROVIDER, 실내 NETWOR_PROVIDER
                    latitude = currentLocation[0].getLatitude();
                    longitude = currentLocation[0].getLongitude();
                    System.out.println("아이 현재 위치값 : " + latitude + "," + longitude);
                    registerChildLocation(UUID, latitude, longitude);

                    /*Location loc = getMyLocation();
                    latitude = loc.getLatitude();
                    longitude = loc.getLongitude();
                    System.out.println("아이 현재 위치값 : " + latitude + "," + longitude);
                    */
                    //registerChildLocation(ChildData.getChildId(), latitude, longitude);

                    // 중간 지점이면 부모님께 메세지 전송
                    double middle_lat = getMiddleLat();
                    double middle_lon = getMiddleLon();

                    float distance = getDistance(latitude, longitude, middle_lat, middle_lon);
                    Log.i("중간지점까지의 거리", String.valueOf(distance));
                    if (distance < 7.0) { //3m

                        if (ChildData.getcheckSMS() == false){
                            Log.i("ㄱㄷㅁ", "False");
                            ChildData.setCheckSMS(true);
//                            sendSMS();
                            showNoti();
                        } else {
                            Log.i("ㄱㄷㅁ", "TRUE");
                        }
                    }
                }
            };

            scheduler.scheduleAtFixedRate(task, 0, 1000); // 1초마다 반복실행*/
        }


    }

    public void onBackPressed() {
        if (task != null) {
            task.cancel();
            Log.i("timer", "종료");
        }
        finish();
    }

    private Location getMyLocation() {
        Location currentLocation = null;
        // Register the listener with the Location Manager to receive location updates
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getApplicationContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            System.out.println("////////////사용자에게 권한을 요청해야함");
            ActivityCompat.requestPermissions((Activity) getApplicationContext(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, this.REQUEST_CODE_LOCATION);
            getMyLocation();
        } else {
            System.out.println("////////////권한요청 안해도됨");
            // 수동으로 위치 구하기
            String locationProvider = LocationManager.GPS_PROVIDER;
            currentLocation = manager.getLastKnownLocation(locationProvider);
            /*if (currentLocation != null) {
                longitude = currentLocation.getLongitude();
                latitude = currentLocation.getLatitude();
            }*/
        }
        return currentLocation;
    }


    private float getDistance(double lat1, double lon1, double lat2, double lon2) {
        float[] distance = new float[2];
        Location.distanceBetween(lat1, lon1, lat2, lon2, distance);
        return distance[0];
    }


    // 현재 아이 위치 전송
    public static void registerChildLocation(String UUID, double current_latitude, double current_longitude) {
        String url = CommonMethod.ipConfig + "/api/savePositionChild";

        try {
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

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

//    public void sendSMS () {
//        try {
//            if (ContextCompat.checkSelfPermission(this,Manifest.permission.SEND_SMS) != PackageManager.PERMISSION_GRANTED) {
//                if (ActivityCompat.shouldShowRequestPermissionRationale(this,Manifest.permission.SEND_SMS)) {
//                    Log.i("kkk", "no");
//                }
//                else { ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, PERMISSION_RQST_SEND);
//                    Log.i("ddd", "n");
//                }
//            } else {
//                //ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS}, PERMISSION_RQST_SEND);
//
//            }
//            //Toast.makeText(getApplicationContext(), "부모님께 메세지 전송 완료!", Toast.LENGTH_LONG).show();
//
//        } catch (Exception e) {
//            //Toast.makeText(getApplicationContext(), "메세지 전송 실패", Toast.LENGTH_LONG).show();
//            e.printStackTrace();
//        }
//
//    }

    //Now once the permission is there or not would be checked
    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case PERMISSION_RQST_SEND: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //전송
                    SmsManager smsManager = SmsManager.getDefault();
                    String phoneNo = fetchPhone(ProfileData.getUserId());
                    smsManager.sendTextMessage(phoneNo, null, "우리 아이가 심부름 경로의 중간 지점을 잘 지나갔어요", null, null);
                    Log.i("sms", "성공");
//                    Toast.makeText(getApplicationContext(), "SMS sent.",Toast.LENGTH_LONG).show();
                } else {
                    Log.i("sms", "실패");
//                    Toast.makeText(getApplicationContext(), "SMS failed, you may try again later.", Toast.LENGTH_LONG).show();
                    return;
                }
            }
        }
    }

    public void showNoti(){
        builder = null;
        Nmanager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        //버전 오레오 이상일 경우
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
            Nmanager.createNotificationChannel(
                    new NotificationChannel(CHANNEL_ID, CHANEL_NAME, NotificationManager.IMPORTANCE_DEFAULT)
            );

            builder = new NotificationCompat.Builder(this,CHANNEL_ID);

            //하위 버전일 경우
        }else{
            builder = new NotificationCompat.Builder(this);
        }

        //알림창 제목
        builder.setContentTitle("우리 아이 심부름을 부탁해");

        //알림창 메시지
        builder.setContentText("심부름 경로의 중간 위치를 지났어요");

        //알림창 아이콘
        builder.setSmallIcon(R.drawable.simbu_logo);


        Notification notification = builder.build();

        //알림창 실행
        Nmanager.notify(1,notification);
    }

    public String fetchPhone(String userId) {
        String url = CommonMethod.ipConfig + "/api/fetchTelNum";
        String rtnStr = "";

        try {
            String jsonString = new JSONObject()
                    .put("userId", userId)
                    .toString();

            //REST API
            RequestHttpURLConnection.NetworkAsyncTask networkTask = new RequestHttpURLConnection.NetworkAsyncTask(url, jsonString);
            rtnStr = networkTask.execute().get();

//          Toast.makeText(getActivity(), "자녀 등록을 완료하였습니다.", Toast.LENGTH_SHORT).show();
            //Log.i(TAG, String.format("가져온 Phonenum: (%s)", rtnStr));

        } catch (Exception e) {
            e.printStackTrace();
        }
        return rtnStr;
    }


    //
    // 출발, 중간, 도착 지점 마커로 띄우기
    void ShowSrcMidDstOnMap() {

        int size = astar.jp_path.size();


        // safe_path.get(0) = 출발 지점     >> 출발지점, 도착지점 둘 다 선으로 연결하지는 않는다.
        TMapMarkerItem markerItem1 = new TMapMarkerItem();
        TMapPoint mark_point1 = new TMapPoint(astar.jp_path.get(0).GetLat(), astar.jp_path.get(0).GetLng());
        Bitmap bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.jhouse);
        markerItem1.setIcon(bitmap); // 마커 아이콘 지정
        markerItem1.setPosition(0.5f, 1.0f); // 마커의 중심점을 중앙, 하단으로 설정
        markerItem1.setTMapPoint(mark_point1); // 마커의 좌표 지정
        markerItem1.setCanShowCallout(true);
        markerItem1.setCalloutTitle("출발!");
        markerItem1.setCalloutSubTitle("출발 지점이에요!");
        tMapView.addMarkerItem("markerItem1", markerItem1); // 지도에 마커 추가


        TMapMarkerItem markerItem3 = new TMapMarkerItem();

       // getMiddleLat
       // TMapPoint mark_point3 = new TMapPoint(astar.jp_path.get(size/2 -1).GetLat(), astar.jp_path.get(size/2 -1).GetLng());
       TMapPoint mark_point3 = new TMapPoint(getMiddleLat(), getMiddleLon());

       Log.d("childmap123"," lat : "+getMiddleLat() + " lon : "+ getMiddleLon());

        Bitmap bitmap3 = BitmapFactory.decodeResource(getResources(), R.drawable.jmiddle);
        markerItem3.setIcon(bitmap3); // 마커 아이콘 지정
        markerItem3.setPosition(0.5f, 1.0f); // 마커의 중심점을 중앙, 하단으로 설정
        markerItem3.setTMapPoint(mark_point3); // 마커의 좌표 지정
        markerItem3.setName("중간"); // 마커의 타이틀 지정

        // 풍션뷰
        markerItem3.setCanShowCallout(true);
        markerItem3.setCalloutTitle("중간 지점");
        markerItem3.setCalloutSubTitle("퀘스트를 잘 수행하고 있나요?");
        //markerItem3.setCalloutRightButtonImage(bitmap3);

        tMapView.addMarkerItem("markerItem3", markerItem3); // 지도에 마커 추가


        // safe_path.get(size-1) = 도착 지점  >> 출발지점, 도착지점 둘 다 선으로 연결하지는 않는다.
        TMapMarkerItem markerItem2 = new TMapMarkerItem();
        TMapPoint mark_point2 = new TMapPoint(astar.jp_path.get(size - 1).GetLat(), astar.jp_path.get(size - 1).GetLng());
        Bitmap bitmap2 = BitmapFactory.decodeResource(getResources(), R.drawable.jend);
        markerItem2.setIcon(bitmap2); // 마커 아이콘 지정
        markerItem2.setPosition(0.5f, 1.0f); // 마커의 중심점을 중앙, 하단으로 설정
        markerItem2.setTMapPoint(mark_point2); // 마커의 좌표 지정
        markerItem2.setName("도착"); // 마커의 타이틀 지정
        markerItem2.setCanShowCallout(true);
        markerItem2.setCalloutTitle("도착!");
        markerItem2.setCalloutSubTitle("도착 지점이에요!");
        tMapView.addMarkerItem("markerItem2", markerItem2); // 지도에 마커 추가

        // 지도 중심 변경 - gps 위치 때문에 안 바뀜
        //tMapView.setCenterPoint(astar.jp_path.get(size / 2).GetLat(), astar.jp_path.get(size / 2).GetLng());


    }

    // 위험 지역을 지도에 마커를 붙여 띄웁니다.
    private void ShowDangerZoneOnMap() {

        int RED = 0;
        int GREEN = 0;
        int BLUE = 0;
        String TAG = "";
        Bitmap bitmap;


        for (int o = 0; o < astar.DangerZone.size(); o++) {

            TMapPoint cmid = new TMapPoint(astar.DangerZone.get(o).GetLat(), astar.DangerZone.get(o).GetLng());
            TMapCircle tMapCircle = new TMapCircle();
            tMapCircle.setCenterPoint( cmid );
            tMapCircle.setRadius(20);
            tMapCircle.setCircleWidth(2);
            tMapCircle.setLineColor(Color.argb(0,255,255,255));
            tMapCircle.setAreaColor(Color.RED);
            tMapCircle.setAreaAlpha(20);
            tMapView.addTMapCircle("circle1"+o, tMapCircle);

            TMapCircle tMapCircle2 = new TMapCircle();
            tMapCircle2.setCenterPoint( cmid );
            tMapCircle2.setRadius(15);
            tMapCircle2.setCircleWidth(2);
            tMapCircle2.setLineColor(Color.argb(0,255,255,255));
            tMapCircle2.setAreaColor(Color.RED);
            tMapCircle2.setAreaAlpha(75);
            tMapView.addTMapCircle("circle2"+o, tMapCircle2);

            TMapCircle tMapCircle3 = new TMapCircle();
            tMapCircle3.setCenterPoint( cmid );
            tMapCircle3.setRadius(10);
            tMapCircle3.setCircleWidth(2);
            tMapCircle3.setLineColor(Color.argb(0,255,255,255));
            tMapCircle3.setAreaColor(Color.RED);
            tMapCircle3.setAreaAlpha(125);
            tMapView.addTMapCircle("circle3"+o, tMapCircle3);

            TMapCircle tMapCircle4 = new TMapCircle();
            tMapCircle4.setCenterPoint( cmid );
            tMapCircle4.setRadius(5);
            tMapCircle4.setCircleWidth(2);
            tMapCircle4.setLineColor(Color.argb(0,255,255,255));
            tMapCircle4.setAreaColor(Color.RED);
            tMapCircle4.setAreaAlpha(255);
            tMapView.addTMapCircle("circle4"+o, tMapCircle4);



            // 만약 위험 지역이 성범죄자 거주 구역이라면
            if (astar.DangerZone.get(o).GetType() == 1.0) {

                bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.jdevil);

                TMapMarkerItem markerItem2 = new TMapMarkerItem();
                TMapPoint mark_point2 = new TMapPoint(astar.DangerZone.get(o).GetLat(), astar.DangerZone.get(o).GetLng());
                markerItem2.setIcon(bitmap); // 마커 아이콘 지정
                markerItem2.setPosition(0.5f, 1.0f); // 마커의 중심점을 중앙, 하단으로 설정
                markerItem2.setTMapPoint(mark_point2); // 마커의 좌표 지정
                markerItem2.setCanShowCallout(true);
                markerItem2.setCalloutTitle("위험 지역");
                markerItem2.setCalloutSubTitle("성범죄자 거주 구역");

                tMapView.addMarkerItem("danger" + o, markerItem2); // 지도에 마커 추가
            }
            // 보행자 사고 다발 지역인 경우
            else if (astar.DangerZone.get(o).GetType() == 2.0) {

                bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.jaccident);

                TMapMarkerItem markerItem2 = new TMapMarkerItem();
                TMapPoint mark_point2 = new TMapPoint(astar.DangerZone.get(o).GetLat(), astar.DangerZone.get(o).GetLng());
                markerItem2.setIcon(bitmap); // 마커 아이콘 지정
                markerItem2.setPosition(0.5f, 1.0f); // 마커의 중심점을 중앙, 하단으로 설정
                markerItem2.setTMapPoint(mark_point2); // 마커의 좌표 지정
                markerItem2.setCanShowCallout(true);
                markerItem2.setCalloutTitle("사고 다발 지점");
                markerItem2.setCalloutSubTitle("보행자 사고 구역");

                tMapView.addMarkerItem("danger" + o, markerItem2); // 지도에 마커 추가

            }
            // 자전거 사고 다발 지역인 경우
            else if (astar.DangerZone.get(o).GetType() == 3.0) {

                TAG = "자전거 사고 다발 구역";

                // 나중에 맞는 마커로 바꿀 것
                bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.jcyclist);

                TMapMarkerItem markerItem2 = new TMapMarkerItem();
                TMapPoint mark_point2 = new TMapPoint(astar.DangerZone.get(o).GetLat(), astar.DangerZone.get(o).GetLng());
                markerItem2.setIcon(bitmap); // 마커 아이콘 지정
                markerItem2.setPosition(0.5f, 1.0f); // 마커의 중심점을 중앙, 하단으로 설정
                markerItem2.setTMapPoint(mark_point2); // 마커의 좌표 지정
                markerItem2.setCanShowCallout(true);
                markerItem2.setCalloutTitle("사고 다발 지점");
                markerItem2.setCalloutSubTitle("자전거 사고 구역");

               tMapView.addMarkerItem("danger" + o, markerItem2); // 지도에 마커 추가
            }
            // 교통사고 주의 구간인 경우
            else if (astar.DangerZone.get(o).GetType() == 4.0) {

                TAG = "교통사고 주의 구역";
                bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.jaccident_car);

                TMapMarkerItem markerItem2 = new TMapMarkerItem();
                TMapPoint mark_point2 = new TMapPoint(astar.DangerZone.get(o).GetLat(), astar.DangerZone.get(o).GetLng());
                markerItem2.setIcon(bitmap); // 마커 아이콘 지정
                markerItem2.setPosition(0.5f, 1.0f); // 마커의 중심점을 중앙, 하단으로 설정
                markerItem2.setTMapPoint(mark_point2); // 마커의 좌표 지정
                markerItem2.setCanShowCallout(true);
                markerItem2.setCalloutTitle("사고 다발 지점");
                markerItem2.setCalloutSubTitle("교통사고 구역");

                tMapView.addMarkerItem("danger" + o, markerItem2); // 지도에 마커 추가
            } else {

                TAG = "시민 신고 지역";
                bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.jsirenback);

                TMapMarkerItem markerItem2 = new TMapMarkerItem();
                TMapPoint mark_point2 = new TMapPoint(astar.DangerZone.get(o).GetLat(), astar.DangerZone.get(o).GetLng());
                markerItem2.setIcon(bitmap); // 마커 아이콘 지정
                markerItem2.setPosition(0.5f, 1.0f); // 마커의 중심점을 중앙, 하단으로 설정
                markerItem2.setTMapPoint(mark_point2); // 마커의 좌표 지정
                markerItem2.setCanShowCallout(true);
                markerItem2.setCalloutTitle("시민 신고 지역");
               // markerItem2.setCalloutSubTitle("교통사고 구역");

                tMapView.addMarkerItem("danger" + o, markerItem2); // 지도에 마커 추가

            }

        }
    }

    // 안전 경로의 정보(신호등, 골목, 횡단보도 등)을 지도에 마커를 붙여 띄웁니다.
    private void ShowPathInfoOnMap() {
        int start = 0;
        int size = 1;
        int tmp = astar.link_info.get(0);
        int i;


        for (i = 1; i < astar.link_info.size(); i++) {
            if (tmp == astar.link_info.get(i)) {
                // 같으면 사이즈 늘리고 continue
                size += 1;
                continue;
            }
            else {
                // 다르면 마커로 띄운다.
                if (size > 1) {
                    addmarker(start + size / 2, tmp);
                } else {
                    addmarker(start, tmp); // 마커를 넣는 함수
                }
                addpath(start, i, tmp);

                tmp = astar.link_info.get(i);
                start = i;
                size = 1;
            }
        }
        addmarker(start + size / 2, tmp);
        addpath(start,i, tmp);

    }


    // 해당 지점 마커로 타입에 맞게 띄우기
    private void addmarker(int start, int type) {


        if(type == onfoot){

        }
        else if(type == alley){
            Bitmap bitmap2 = null;
            TMapMarkerItem markerItem4 = new TMapMarkerItem();
            markerItem4.setCanShowCallout(true);
            bitmap2 = BitmapFactory.decodeResource(getResources(), R.drawable.jalley);
            markerItem4.setCalloutTitle("골목길");
            markerItem4.setCalloutSubTitle("주변을 살피며 걸어요!");

            double mid_lat = (astar.jp_path.get(start+1).GetLat() + astar.jp_path.get(start + 2).GetLat())/2.0;
            double mid_lon = (astar.jp_path.get(start+1).GetLng() + astar.jp_path.get(start + 2).GetLng())/2.0;

            TMapPoint mark_point2 = new TMapPoint(mid_lat, mid_lon);
            markerItem4.setIcon(bitmap2); // 마커 아이콘 지정
            markerItem4.setPosition(0.5f, 1.0f); // 마커의 중심점을 중앙, 하단으로 설정
            markerItem4.setTMapPoint(mark_point2); // 마커의 좌표 지정
            tMapView.addMarkerItem("markerItem4" + start, markerItem4); // 지도에 마커 추가
        }
        else if(type == traffic){
            Bitmap bitmap2 = null;
            TMapMarkerItem markerItem4 = new TMapMarkerItem();
            markerItem4.setCanShowCallout(true);
            bitmap2 = BitmapFactory.decodeResource(getResources(), R.drawable.jtraffic_lights);
            markerItem4.setCalloutTitle("신호등 : 녹색불이 깜빡거리면");
            markerItem4.setCalloutSubTitle("다음 신호를 기다려요!");

            double mid_lat = (astar.jp_path.get(start+1).GetLat() + astar.jp_path.get(start + 2).GetLat())/2.0;
            double mid_lon = (astar.jp_path.get(start+1).GetLng() + astar.jp_path.get(start + 2).GetLng())/2.0;

            TMapPoint mark_point2 = new TMapPoint(mid_lat, mid_lon);
            markerItem4.setIcon(bitmap2); // 마커 아이콘 지정
            markerItem4.setPosition(0.5f, 1.0f); // 마커의 중심점을 중앙, 하단으로 설정
            markerItem4.setTMapPoint(mark_point2); // 마커의 좌표 지정
            tMapView.addMarkerItem("markerItem4" + start, markerItem4); // 지도에 마커 추가
        }
        else if(type == crosswalk){
            Bitmap bitmap2 = null;
            TMapMarkerItem markerItem4 = new TMapMarkerItem();
            markerItem4.setCanShowCallout(true);
            bitmap2 = BitmapFactory.decodeResource(getResources(), R.drawable.jcross);
            markerItem4.setCalloutTitle("횡단보도");
            markerItem4.setCalloutSubTitle("어른들과 같이 건너요!");
            markerItem4.setCalloutLeftImage(bitmap2);

            double mid_lat = (astar.jp_path.get(start+1).GetLat() + astar.jp_path.get(start + 2).GetLat())/2.0;
            double mid_lon = (astar.jp_path.get(start+1).GetLng() + astar.jp_path.get(start + 2).GetLng())/2.0;

            TMapPoint mark_point2 = new TMapPoint(mid_lat, mid_lon);
            markerItem4.setIcon(bitmap2); // 마커 아이콘 지정
            markerItem4.setPosition(0.5f, 1.0f); // 마커의 중심점을 중앙, 하단으로 설정
            markerItem4.setTMapPoint(mark_point2); // 마커의 좌표 지정
            tMapView.addMarkerItem("markerItem4" + start, markerItem4); // 지도에 마커 추가
        }
        else{

        }


       // Log.d("ChildMap123","addTmarker--");
    }

    // 경로를 지도에 띄우기
    void addpath(int start,int end, int type){


        Log.d("cm123","길 추가  start : "+ start + " end : "+ end + " type : "+ type);

        TMapPolyLine tpolyline = new TMapPolyLine();
        tpolyline.setLineWidth(10);

    //    /*
        if(type == onfoot){
            tpolyline.setLineColor(Color.argb(255, 0, 0, 0));
        }
        else if(type == alley){
            tpolyline.setLineColor(Color.argb(255, 204, 92, 37));
        }
        else if(type == traffic){
            tpolyline.setLineColor(Color.argb(255, 0,255,0));
        }
        else if(type == crosswalk){
            tpolyline.setLineColor(Color.argb(255, 0, 255, 255));
        }
        else{

        }
//*/
        //tpolyline.setLineColor(Color.argb(255, 0, 0, 0));

        for(int y = start ; y <= end ; y++) {
            TMapPoint tp = new TMapPoint(astar.jp_path.get(y+1).GetLat(),astar.jp_path.get(y+1).GetLng());
            tpolyline.addLinePoint(tp);
        }

        tMapView.addTMapPolyLine("path"+start,tpolyline);


}


    double getMiddleLat(){

        double total = 0.0;
        double middle = 0.0;
        double lat = 0.0;

        int j = 0;

        for(int i = 1 ; i < astar.jp_path.size() - 2 ; i++){
            total += GetDistance(astar.jp_path.get(i),astar.jp_path.get(i+1));
        }

        middle = total/2.0;

        for( ; j < astar.jp_path.size() -2 ; j++){
            total -= GetDistance(astar.jp_path.get(j),astar.jp_path.get(j+1));

            if( middle - total > 0){
                break;
            }

        }

        lat = (astar.jp_path.get(j).GetLat())/3.0 * 2.0 + (astar.jp_path.get(j+1).GetLat() )/3.0 * 1.0;

        return lat;
    }

    double getMiddleLon(){

        double total = 0.0;
        double middle = 0.0;
        double lon = 0.0;

        int j = 0;

        for(int i = 1 ; i < astar.jp_path.size() -2 ; i++){
            total += GetDistance(astar.jp_path.get(i),astar.jp_path.get(i+1));
        }

        middle = total/2.0;

        for( ; j < astar.jp_path.size() -2 ; j++){
            total -= GetDistance(astar.jp_path.get(j),astar.jp_path.get(j+1));

            if( middle - total > 0){
                break;
            }

        }

        lon =  (astar.jp_path.get(j).GetLng())/3.0 * 2.0 + (astar.jp_path.get(j+1).GetLng() )/3.0 * 1.0;

        return lon;
    }


    public double GetDistance(jPoint src, jPoint dst) {
        Location start = new Location("src");
        start.setLatitude(src.GetLat());
        start.setLongitude(src.GetLng());

        Location end = new Location("dst");
        end.setLatitude(dst.GetLat());
        end.setLongitude(dst.GetLng());

        double distance = start.distanceTo(end);

        return distance;
    }
}