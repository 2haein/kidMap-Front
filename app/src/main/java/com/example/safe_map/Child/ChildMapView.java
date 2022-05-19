package com.example.safe_map.Child;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.Toast;

import com.example.safe_map.MainActivity;
import com.example.safe_map.R;
import com.example.safe_map.common.ProfileData;
import com.example.safe_map.http.CommonMethod;
import com.example.safe_map.http.RequestHttpURLConnection;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.maps.android.SphericalUtil;

import net.daum.mf.map.api.MapView;

import org.json.JSONObject;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ChildMapView#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChildMapView extends Fragment {
    String UUID;

    MapView mapView;
    private LocationManager locationManager;
    private static final int REQUEST_CODE_LOCATION = 2;

    ImageButton home, camera, call, qr;
    String number = "0100000000";

    LatLng previousPosition = null;
    Marker addedMarker = null;
    int tracking = 0;

    public ChildMapView() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View rootView = inflater.inflate(R.layout.fragment_child_map_view, container, false);

        home = (ImageButton) rootView.findViewById(R.id.homeaddr);
        camera = (ImageButton) rootView.findViewById(R.id.camera);
        call = (ImageButton) rootView.findViewById(R.id.call);

        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        camera.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

            }
        });

        call.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                number = fetchPhone(ProfileData.getUserId());
                Intent tt = new Intent("android.intent.action.DIAL", Uri.parse("tel:" + number));
                startActivity(tt);
            }
        });


        // 아이의 위치 수신을 위한 세팅
        locationManager = (LocationManager)getContext().getSystemService(Context.LOCATION_SERVICE);
        // 아이의 현재 위치
        Location userLocation = getMyLocation();
        if( userLocation != null ) {
            double latitude = userLocation.getLatitude();
            double longitude = userLocation.getLongitude();
            System.out.println("아이 현재 위치값 : "+latitude+","+longitude);
            registerChildLocation(UUID, latitude, longitude);
        }

        // 아이의 현재 위치와 가까운 노드와의 거리 재기


        return rootView;
    }



    /**
     * 아이의 위치를 수신
     */
    private Location getMyLocation() {
        Location currentLocation = null;
        // Register the listener with the Location Manager to receive location updates
        if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            System.out.println("////////////사용자에게 권한을 요청해야함");
            ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, this.REQUEST_CODE_LOCATION);
            getMyLocation();
        }
        else {
            System.out.println("////////////권한요청 안해도됨");

            // 수동으로 위치 구하기
            String locationProvider = LocationManager.GPS_PROVIDER;
            currentLocation = locationManager.getLastKnownLocation(locationProvider);
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

    public void registerChildLocation(String UUID, double current_latitude, double current_longitude){
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
            Toast.makeText(getContext().getApplicationContext(), "전화번호가 저장되었습니다", Toast.LENGTH_LONG).show();
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