package com.example.safe_map.FNotifyMap;


//==

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import net.daum.android.map.MapViewEventListener;
import net.daum.mf.map.api.CalloutBalloonAdapter;
import net.daum.mf.map.api.MapReverseGeoCoder;
import net.daum.mf.map.api.MapView;

import com.example.safe_map.FHome.AddMissionActivity;
import com.example.safe_map.FHome.AddressApiActivity;
import com.example.safe_map.MainActivity;
import com.example.safe_map.NetworkStatus;
import com.example.safe_map.R;
import com.example.safe_map.common.ProfileData;
import com.example.safe_map.databinding.FragmentHomeBinding;
import com.example.safe_map.databinding.FragmentNotifyBinding;
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
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link NotifyFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class NotifyFragment extends Fragment implements NotifyFragment_finish, MapView.CurrentLocationEventListener, MapView.MapViewEventListener  {

    public static NotifyFragment newInstance(String param1, String param2) {
        NotifyFragment fragment = new NotifyFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    private FragmentNotifyBinding binding;
    private static final int GPS_ENABLE_REQUEST_CODE = 2001;
    private static final int PERMISSIONS_REQUEST_CODE = 100;
    String[] REQUIRED_PERMISSIONS  = {Manifest.permission.ACCESS_FINE_LOCATION};

    private static final String LOG_TAG = "NotifyFragment";

    /**
     * KAKAO MAP 선언
     * */
    private MapView mapView;
    MapPoint currentMapPoint;

//    View rootView;
    ViewGroup mapViewContainer;

    private Double mCurrentLng;
    private Double mCurrentLat;
    private Double getPickedLng=0.0;
    private Double getPickedLat=0.0;
    private List<String> markers= new ArrayList<>();
    private String strUserId;


    MainActivity activity;

    private MarkerEventListener eventListener = new MarkerEventListener();

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        activity = (MainActivity) getActivity();
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    private LocationManager locationManager;
    private static final int REQUEST_CODE_LOCATION = 2;

    //위험 지역 등록
    TextView addr_result;
    String notify_content;
    Button addr_search, notify_add;
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
        binding = FragmentNotifyBinding.inflate(inflater, container, false);
        View rootView = binding.getRoot();

        strUserId = ProfileData.getUserId();

        mapViewContainer = (ViewGroup) binding.notifyMap;
        mapView = new MapView(getActivity());
        mapViewContainer = mapViewContainer.findViewById(R.id.notify_map);
        mapViewContainer.addView(mapView);


        mapView.setCurrentLocationEventListener(this);
        mapView.setMapViewEventListener(this);

        mapView.setZoomLevel(3, true); //맵 확대
        mapView.zoomIn(true);
        mapView.zoomOut(true);
        mapView.setCalloutBalloonAdapter(new CustomCalloutBalloonAdapter());
        mapView.setPOIItemEventListener(eventListener);



        /**
         * KAKAO MAP 권한 설정
         * */

        if (!checkLocationServicesStatus()) {
            showDialogForLocationServiceSetting();
        }else {
            checkRunTimePermission();
        }

        Location loc_Current = getMyLocation();
        mCurrentLat = loc_Current.getLatitude();
        mCurrentLng = loc_Current.getLongitude();

        // 중심점
        mapView.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(mCurrentLat, mCurrentLng), true);

        // 줌 레벨 변경
//        mapView.setZoomLevel(4, true);

        //마커 찍기
        MapPoint MARKER_POINT = MapPoint.mapPointWithGeoCoord(mCurrentLat, mCurrentLng);


        MapPOIItem marker = new MapPOIItem();
        marker.setItemName("현재 위치");
        marker.setTag(0);
        marker.setMapPoint(MARKER_POINT);
        marker.setMarkerType(MapPOIItem.MarkerType.BluePin); // 기본으로 제공하는 BluePin 마커 모양.
        marker.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin); // 마커를 클릭했을때, 기본으로 제공하는 RedPin 마커 모양.
        mapView.addPOIItem(marker);


        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(), "현재위치 탐색",Toast.LENGTH_SHORT).show();
                Location loc_Current = getMyLocation();
                mCurrentLat = loc_Current.getLatitude();
                mCurrentLng = loc_Current.getLongitude();

                // 중심점
                mapView.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(mCurrentLat, mCurrentLng), true);
//                mapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading); //TrackingModeOnWithoutHeading

            }
        });

        addr_result = (TextView) rootView.findViewById(R.id.addr_result);
        addr_search = (Button) rootView.findViewById(R.id.search_addr);
        notify_add = (Button) rootView.findViewById(R.id.notify_add);
        EditText notify_content_editText = (EditText) rootView.findViewById(R.id.notify_content);
        notify_content = notify_content_editText.getText().toString();
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

        notify_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                System.out.println("1111"+notify_content);
                    if(markers.size() == 0) {
                        Toast.makeText(getContext(), "위험 지역 표시를 등록해주세요", Toast.LENGTH_SHORT).show();
                    }else if(notify_content.equals("")) {
                        Toast.makeText(getContext(), "위험 내용을 입력해주세요", Toast.LENGTH_SHORT).show();
                    } else {
                        
                    }

            }
        });

        return rootView;
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }


    //MapView.MapViewEventListener 구현
    @Override
    public void onMapViewInitialized(MapView mapView) {

    }

    @Override
    public void onMapViewCenterPointMoved(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewZoomLevelChanged(MapView mapView, int i) {

    }

    @Override
    public void onMapViewSingleTapped(MapView mapView, MapPoint mapPoint) {

        getPickedLat = mapPoint.getMapPointGeoCoord().latitude;
        getPickedLng = mapPoint.getMapPointGeoCoord().longitude;

//        isPost = checkPostHistory();
//        Log.i(LOG_TAG, String.format("isPost값입니다.: %d", isPost));
        if(markers.size() == 0){
            setMapMarker(mapView, mapPoint);
            markers.add("1");
        } else{
            Toast.makeText(getActivity(), "마커를 다른 곳에 표시하려면 지도상의 존재하는 마커를 제거해주세요!",
                    Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onMapViewLongPressed(MapView mapView, MapPoint mapPoint) {
        getPickedLat = mapPoint.getMapPointGeoCoord().latitude;
        getPickedLng = mapPoint.getMapPointGeoCoord().longitude;

//        isPost = checkPostHistory();
//        Log.i(LOG_TAG, String.format("isPost값입니다.: %d", isPost));
        if(markers.size() == 0){
            setMapMarker(mapView, mapPoint);
            markers.add("1");
        } else{
            Toast.makeText(getActivity(), "마커를 다른 곳에 표시하려면 지도상의 존재하는 마커를 제거해주세요!",
                    Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onMapViewDragStarted(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewDragEnded(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewMoveFinished(MapView mapView, MapPoint mapPoint) {

    }

    @Override
    public void onMapViewDoubleTapped(MapView mapView, MapPoint mapPoint) {
    }

    public boolean checkLocationServicesStatus() {
        LocationManager locationManager = (LocationManager) getActivity().getSystemService(getActivity().LOCATION_SERVICE);

        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                || locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
    }

    void checkRunTimePermission(){
        //런타임 퍼미션 처리
        // 1. 위치 퍼미션을 가지고 있는지 체크합니다.
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION);

        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED ) {
            // 2. 이미 퍼미션을 가지고 있다면
            // 3.  위치 값을 가져올 수 있음
//            mapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading); //TrackingModeOnWithoutHeading


        } else {  //2. 퍼미션 요청을 허용한 적이 없다면 퍼미션 요청이 필요합니다. 2가지 경우(3-1, 4-1)가 있습니다.

            // 3-1. 사용자가 퍼미션 거부를 한 적이 있는 경우에는
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), REQUIRED_PERMISSIONS[0])) {

                // 3-2. 요청을 진행하기 전에 사용자가에게 퍼미션이 필요한 이유를 설명해줄 필요가 있습니다.
                Toast.makeText(getActivity(), "이 앱을 실행하려면 위치 접근 권한이 필요합니다.", Toast.LENGTH_LONG).show();
                // 3-3. 사용자게에 퍼미션 요청을 합니다. 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(getActivity(), REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);

            } else {
                // 4-1. 사용자가 퍼미션 거부를 한 적이 없는 경우에는 퍼미션 요청을 바로 합니다.
                // 요청 결과는 onRequestPermissionResult에서 수신됩니다.
                ActivityCompat.requestPermissions(getActivity(), REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);
            }

        }

    }

    // 커스텀 말풍선 클래스
    class CustomCalloutBalloonAdapter implements CalloutBalloonAdapter {
        private final View mCalloutBalloon;

        public CustomCalloutBalloonAdapter() {
            mCalloutBalloon = getLayoutInflater().inflate(R.layout.custom_callout_balloon, null);
        }

        @Override
        public View getCalloutBalloon(MapPOIItem poiItem) {

                ((TextView) mCalloutBalloon.findViewById(R.id.title)).setText(poiItem.getItemName());
            if(poiItem.getTag()==1) {
                ((TextView) mCalloutBalloon.findViewById(R.id.desc)).setText("클릭!");
            } else{
                ((TextView) mCalloutBalloon.findViewById(R.id.desc)).setText("현재위치");
            }
            return mCalloutBalloon;
        }

        @Override
        public View getPressedCalloutBalloon(MapPOIItem poiItem) {
            return null;
        }

    }

    //마커 클릭시
    class MarkerEventListener implements MapView.POIItemEventListener {
        @Override
        public void onPOIItemSelected(MapView mapView, MapPOIItem mapPOIItem) {

        }

        @Override
        public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem) {

        }

        // 마커의 풍선 클릭 시
        @Override
        public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem, MapPOIItem.CalloutBalloonButtonType calloutBalloonButtonType) {

            // 사용자가 신고할 마커 클릭 시
            if(mapPOIItem.getTag()==1){
                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getActivity());
                builder.setTitle("선택하세요");
                // 마커 삭제
                    builder.setItems(R.array.LAN2, new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialog, int pos)
                        {
                            String[] items = getResources().getStringArray(R.array.LAN2);
                            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();


                            Toast.makeText(getActivity(),items[pos],Toast.LENGTH_SHORT).show();
                            // 각 버튼별로 수행할 일
                            if(pos == 0){
                                if(markers.size()!=0) {
                                    markers.clear();
                                }
                                mapView.removePOIItem(mapPOIItem);
                            }
                        }
                    });

                AlertDialog alertDialog = builder.create();
                alertDialog.show();
            }
    }

        @Override
        public void onDraggablePOIItemMoved(MapView mapView, MapPOIItem mapPOIItem, MapPoint mapPoint) {

        }

    }
    private void showDialogForLocationServiceSetting() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("위치 서비스 비활성화");
        builder.setMessage("앱을 사용하기 위해서는 위치 서비스가 필요합니다.\n"
                + "위치 설정을 수정하실래요?");
        builder.setCancelable(true);
        builder.setPositiveButton("설정", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Intent callGPSSettingIntent
                        = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("취소", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }
    @Override
    public void onCurrentLocationUpdate(MapView mapView, MapPoint mapPoint, float accuracyInMeters) {
        MapPoint.GeoCoordinate mapPointGeo = mapPoint.getMapPointGeoCoord();
        Log.i(LOG_TAG, String.format("MapView onCurrentLocationUpdate (%f,%f) accuracy (%f)", mapPointGeo.latitude, mapPointGeo.longitude, accuracyInMeters));
        currentMapPoint = MapPoint.mapPointWithGeoCoord(mapPointGeo.latitude, mapPointGeo.longitude);
        //이 좌표로 지도 중심 이동
        mapView.setMapCenterPoint(currentMapPoint, true);
        //최초 1회만 현재위치 받기 위함
//        mapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOff); //TrackingModeOnWithoutHeading
        //전역변수로 현재 좌표 저장
        mCurrentLat = mapPointGeo.latitude;
        mCurrentLng = mapPointGeo.longitude;
        Log.d(LOG_TAG, "현재위치 => " + mCurrentLat + "  " + mCurrentLng);

    }

    public void setMapMarker(MapView mapView, MapPoint mapPoint) {
        MapPOIItem marker = new MapPOIItem();
        int tagNum=1;
        marker.setItemName("위험 지역 신고");
//        if(strUserId.equals(user_list)){
            //이 마커가 사용자가 생성한 마커일 경우
//            marker.setTag(0);
//        }
//        else{
//            tagNum = Integer.parseInt(user_list);
        marker.setTag(tagNum);

        marker.setMapPoint(mapPoint);
        //Log.i(LOG_TAG, String.format("마커태그값" + marker.getTag()));
        marker.setMarkerType(MapPOIItem.MarkerType.CustomImage);

        marker.setCustomImageResourceId(R.drawable.sirenback);


        marker.setCustomImageAutoscale(true);
        marker.setCustomImageAnchor(0.5f, 1.0f);
        marker.setDraggable(true);
        mapView.addPOIItem(marker);

    }

    @Override
    public void onCurrentLocationDeviceHeadingUpdate(MapView mapView, float v) {
    }

    @Override
    public void onCurrentLocationUpdateFailed(MapView mapView) {
    }

    @Override
    public void onCurrentLocationUpdateCancelled(MapView mapView) {
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
            Toast.makeText(getContext().getApplicationContext(), "위험지역 신고가 완료되었습니다", Toast.LENGTH_LONG).show();
            //Log.i("현재위치 전송", String.format("등록한 현재 위치 : lat " + current_latitude + ", long " + current_longitude));

        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        switch (requestCode) {
            case GPS_ENABLE_REQUEST_CODE:
                //사용자가 GPS 활성 시켰는지 검사
                if (checkLocationServicesStatus()) {
                    if (checkLocationServicesStatus()) {
                        Log.d("@@@", "onActivityResult : GPS 활성화 되있음");
                        checkRunTimePermission();
                        return;
                    }
                }
                break;
        }
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
                    mapView.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(notify_latitude, notify_longitude), true);
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
            locationManager = (LocationManager) getActivity().getSystemService(getActivity().LOCATION_SERVICE);
            currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (currentLocation != null) {
                double lng = currentLocation.getLongitude();
                double lat = currentLocation.getLatitude();
            }
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, gpsLocationListener);
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1, gpsLocationListener);

        }
        return currentLocation;
    }
    final LocationListener gpsLocationListener = new LocationListener()
    {
        public void onLocationChanged(Location location) { String provider = location.getProvider(); }
        public void onStatusChanged(String provider, int status, Bundle extras) { }
        public void onProviderEnabled(String provider) { }
        public void onProviderDisabled(String provider) { }
    };


    @Override
    public void onResume() {
        super.onResume();
        ProfileData.setMapFlag(false);
    }

    @Override
    public void onPause() {
        super.onPause();
        if(ProfileData.getMapFlag()) {
            mapViewContainer.removeAllViews();
        }
    }

//    @Override
//    public void onResume() {
//        mapView.onResume();
//        super.onResume();
//    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

//    @Override
//    public void onPause(){
//        mapViewContainer.removeView(mapView);
//        //getActivity().finish();
//        super.onPause();
//    }

//    @Override
//    public void onLowMemory() {
//        super.onLowMemory();
//        //mapView.onLowMemory();
//    }

//    @Override
//    public void onMapReady(GoogleMap googleMap) {
//
//        MapsInitializer.initialize(this.getActivity());
//
//        // Updates the location and zoom of the MapView
//        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(new LatLng(37.5047735, 126.953764999), 14);
//
//        googleMap.animateCamera(cameraUpdate);
//
//        googleMap.addMarker(new MarkerOptions()
//                .position(new LatLng(37.5047735, 126.953764999))
//                .title("중대 후문 임시" ));
//
//    }

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






