package com.example.safe_map.Child;

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

import com.example.safe_map.R;
import com.example.safe_map.common.ChildData;
import com.example.safe_map.common.ProfileData;
import com.example.safe_map.http.CommonMethod;
import com.example.safe_map.http.RequestHttpURLConnection;

import org.json.JSONObject;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;

import com.skt.Tmap.TMapView;
import com.skt.Tmap.TMapGpsManager;
import com.skt.Tmap.TMapMarkerItem;
import com.skt.Tmap.TMapPoint;

public class ChildMap extends AppCompatActivity{
    Fragment ChildMapView;
    String UUID;
    private Context mContext;


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
        if (ActivityCompat.checkSelfPermission((Activity) getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            System.out.println("////////////사용자에게 권한을 요청해야함");
            ActivityCompat.requestPermissions((Activity) getContext(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, this.REQUEST_CODE_LOCATION);
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
}