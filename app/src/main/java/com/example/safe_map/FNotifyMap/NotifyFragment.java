package com.example.safe_map.FNotifyMap;


//==

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import net.daum.android.map.MapViewEventListener;
import net.daum.mf.map.api.MapReverseGeoCoder;
import net.daum.mf.map.api.MapView;

import com.example.safe_map.FHome.AddMissionActivity;
import com.example.safe_map.FHome.AddressApiActivity;
import com.example.safe_map.MainActivity;
import com.example.safe_map.NetworkStatus;
import com.example.safe_map.R;
import com.example.safe_map.common.ProfileData;
import com.example.safe_map.http.CommonMethod;
import com.example.safe_map.http.RequestHttpURLConnection;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
//import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;

import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link NotifyFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NotifyFragment extends Fragment implements OnMapReadyCallback, NotifyFragment_finish {

    public static NotifyFragment newInstance(String param1, String param2) {
        NotifyFragment fragment = new NotifyFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    private static final String LOG_TAG = "NotifyFragment";

    View rootView;
    MapView mapView;
    ViewGroup mapViewContainer;

    private LocationManager locationManager;
    private static final int REQUEST_CODE_LOCATION = 2;
    double cur_lon, cur_lat;

    //위험 지역 등록
    TextView addr_search, addr_result;
    private static final int SEARCH_ADDRESS_ACTIVITY = 40000;
    public double notify_longitude, notify_latitude;
    List<Address> address = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        checkDangerousPermissions();
        rootView = inflater.inflate(R.layout.fragment_notify, container, false);
        mapView = new MapView(getActivity());
        mapViewContainer = (ViewGroup) rootView.findViewById(R.id.notify_map);
        mapViewContainer.addView(mapView);

        locationManager = (LocationManager)getContext().getSystemService(Context.LOCATION_SERVICE);
        Location loc_Current = getMyLocation();
        cur_lat = loc_Current.getLatitude();
        cur_lon = loc_Current.getLongitude();

        // 중심점
        mapView.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(cur_lat, cur_lon), true);

        // 줌 레벨 변경
        mapView.setZoomLevel(4, true);

        //마커 찍기
        MapPoint MARKER_POINT = MapPoint.mapPointWithGeoCoord(cur_lat, cur_lon);
        MapPOIItem marker = new MapPOIItem();
        marker.setItemName("Default Marker");
        marker.setTag(0);
        marker.setMapPoint(MARKER_POINT);
        marker.setMarkerType(MapPOIItem.MarkerType.BluePin); // 기본으로 제공하는 BluePin 마커 모양.
        marker.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin); // 마커를 클릭했을때, 기본으로 제공하는 RedPin 마커 모양.

        mapView.addPOIItem(marker);

        addr_result = (TextView) rootView.findViewById(R.id.addr_result);
        addr_search = (TextView) rootView.findViewById(R.id.search_addr);
        addr_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    Log.i("주소설정페이지", "주소입력창 클릭");
                    int status = NetworkStatus.getConnectivityStatus(getContext());
                    if(status == NetworkStatus.TYPE_MOBILE || status == NetworkStatus.TYPE_WIFI) {
                        Log.i("주소설정페이지", "주소입력창 클릭");
                        Intent i = new Intent(getContext(), AddressApiActivity.class);
                        // 화면전환 애니메이션 없애기
                        // overridePendingTransition(0, 0);
                        // 주소결과
                        getActivity().startActivityForResult(i, SEARCH_ADDRESS_ACTIVITY);
                        //MainActivity.moveToAddressApi();
                    }else {
                        Toast.makeText(getContext(), "인터넷 연결을 확인해주세요.", Toast.LENGTH_SHORT).show();
                    }} catch(NumberFormatException e) {
                    notify_latitude = 0;
                    notify_longitude = 0;
                    Toast.makeText(getContext().getApplicationContext(), "집 주소를 다시 입력해주세요", Toast.LENGTH_LONG).show();
                }
            }
        });

        return rootView;
    }

    public void registerNotify (double notify_longitude, double notify_latitude, String notify_name, String notify_content){
        String url = CommonMethod.ipConfig + "/api/savePositionChild";

        try{
            String jsonString = new JSONObject()
                    .put("userId", ProfileData.getUserId())
                    .put("notify_longitude", notify_longitude)
                    .put("notify_latitude", notify_latitude)
                    .put("notify_name", notify_name)
                    .put("notify_content", notify_content)
                    .toString();

            //REST API
            RequestHttpURLConnection.NetworkAsyncTask networkTask = new RequestHttpURLConnection.NetworkAsyncTask(url, jsonString);
            networkTask.execute().get();
            Toast.makeText(getContext().getApplicationContext(), "전화번호가 저장되었습니다", Toast.LENGTH_LONG).show();
            //Log.i("현재위치 전송", String.format("등록한 현재 위치 : lat " + current_latitude + ", long " + current_longitude));

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        Log.i("test", "onActivityResult");
        String data = intent.getExtras().getString("data");
        if (data != null) {
            Log.i("test", "data:" + data);
            addr_result.setText(data);
            Geocoder geocoder = new Geocoder(getContext());
            try {
                address = geocoder.getFromLocationName(data, 10);
            } catch (IOException e) {
                e.printStackTrace();
            }
            if (address != null) {
                if (address.size() == 0) {
                    Log.i("nonono", "0");
                    Toast.makeText(getContext(), "해당되는 주소의 위도, 경도 값을 찾을 수 없습니다", Toast.LENGTH_LONG);
                } else {
                    Address addr = address.get(0);
                    Log.i("address 변환 ok", String.valueOf(addr.getLatitude()));
                    notify_latitude = addr.getLatitude();
                    notify_longitude = addr.getLongitude();
                    Toast.makeText(getContext(), "해당되는 주소의 위도, 경도 값을 설정하였습니다", Toast.LENGTH_LONG);
                }
            }
        }
    }

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


    @Override
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
        //getActivity().finish();
        super.onPause();
    }

    @Override
    public void onLowMemory() {
        super.onLowMemory();
        //mapView.onLowMemory();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {

        MapsInitializer.initialize(this.getActivity());

        // Updates the location and zoom of the MapView
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(37.5047735, 126.953764999), 14);

        googleMap.animateCamera(cameraUpdate);

        googleMap.addMarker(new MarkerOptions()
                .position(new LatLng(37.5047735, 126.953764999))
                .title("중대 후문 임시" ));

    }

    private void checkDangerousPermissions() {
        String[] permissions = {
                android.Manifest.permission.ACCESS_COARSE_LOCATION,
                android.Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_WIFI_STATE
        };

        int permissionCheck = PackageManager.PERMISSION_GRANTED;
        for (int i = 0; i < permissions.length; i++) {
            permissionCheck = ContextCompat.checkSelfPermission(this.getActivity(), permissions[i]);
            if (permissionCheck == PackageManager.PERMISSION_DENIED) {
                break;
            }
        }

        if (permissionCheck == PackageManager.PERMISSION_GRANTED) {
            //Toast.makeText(this.getActivity(), "권한 있음", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this.getActivity(), "권한 없음", Toast.LENGTH_LONG).show();

            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), permissions[0])) {
                Toast.makeText(this.getActivity(), "권한 설명 필요함.", Toast.LENGTH_LONG).show();
            } else {
                ActivityCompat.requestPermissions(this.getActivity(), permissions, 1);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            for (int i = 0; i < permissions.length; i++) {
                if (grantResults[i] == PackageManager.PERMISSION_GRANTED) {
                    //Toast.makeText(getActivity(), permissions[i] + " 권한이 승인됨.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getActivity(), permissions[i] + " 권한이 승인되지 않음.", Toast.LENGTH_LONG).show();
                }
            }
        }
    }
    @Override
    public void finish() {
        mapViewContainer.removeView(mapView);
        getActivity().finish();
    }
}






