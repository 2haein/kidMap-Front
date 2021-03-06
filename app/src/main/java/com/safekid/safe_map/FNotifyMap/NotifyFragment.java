package com.safekid.safe_map.FNotifyMap;


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

import net.daum.mf.map.api.CalloutBalloonAdapter;
import net.daum.mf.map.api.MapView;

import com.safekid.safe_map.http.CommonMethod;
import com.safekid.safe_map.FHome.AddressApiActivity;
import com.safekid.safe_map.MainActivity;
import com.safekid.safe_map.NetworkStatus;
import com.safekid.safe_map.R;
import com.safekid.safe_map.common.ProfileData;
import com.safekid.safe_map.databinding.FragmentHomeBinding;
import com.safekid.safe_map.databinding.FragmentNotifyBinding;
import com.safekid.safe_map.http.RequestHttpURLConnection;
//import com.google.android.gms.maps.MapView;

import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;

import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

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
     * KAKAO MAP ??????
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

    //?????? ?????? ??????
    TextView addr_result;
    EditText notify_content_editText;
    String notify_content_string;
    Button addr_search, notify_add;
    private static final int SEARCH_ADDRESS_ACTIVITY = 40000;
    public double notify_longitude, notify_latitude;
    List<Address> address = null;
    MapPOIItem tempMarker = new MapPOIItem();

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

        mapView.setZoomLevel(3, true); //??? ??????
        mapView.zoomIn(true);
        mapView.zoomOut(true);
        mapView.setCalloutBalloonAdapter(new CustomCalloutBalloonAdapter());
        mapView.setPOIItemEventListener(eventListener);



        /**
         * KAKAO MAP ?????? ??????
         * */

        if (!checkLocationServicesStatus()) {
            showDialogForLocationServiceSetting();
        }else {
            checkRunTimePermission();
        }

        Location loc_Current = getMyLocation();
        mCurrentLat = loc_Current.getLatitude();
        mCurrentLng = loc_Current.getLongitude();
        Log.i("current ", String.valueOf(mCurrentLng));
        // ?????????
        mapView.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(mCurrentLat, mCurrentLng), true);

        // ??? ?????? ??????
//        mapView.setZoomLevel(4, true);

        //?????? ??????
        MapPoint MARKER_POINT = MapPoint.mapPointWithGeoCoord(mCurrentLat, mCurrentLng);


        MapPOIItem marker = new MapPOIItem();
        marker.setItemName("?????? ??????");
        marker.setTag(0);
        marker.setMapPoint(MARKER_POINT);
        marker.setMarkerType(MapPOIItem.MarkerType.BluePin); // ???????????? ???????????? BluePin ?????? ??????.
        marker.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin); // ????????? ???????????????, ???????????? ???????????? RedPin ?????? ??????.
        mapView.addPOIItem(marker);


        binding.fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Toast.makeText(getActivity(), "???????????? ??????",Toast.LENGTH_SHORT).show();
                Location loc_Current = getMyLocation();
                mCurrentLat = loc_Current.getLatitude();
                mCurrentLng = loc_Current.getLongitude();

                // ?????????
                mapView.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(mCurrentLat, mCurrentLng), true);
//                mapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading); //TrackingModeOnWithoutHeading

            }
        });

        addr_result = (TextView) rootView.findViewById(R.id.addr_result);
        addr_search = (Button) rootView.findViewById(R.id.search_addr);
        notify_add = (Button) rootView.findViewById(R.id.notify_add);
        notify_content_editText = (EditText) rootView.findViewById(R.id.notify_content);

        addr_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(markers.size() == 0) {

                    try {
                        Log.i("?????????????????????", "??????????????? ??????");
                        int status = NetworkStatus.getConnectivityStatus(getContext());
                        if (status == NetworkStatus.TYPE_MOBILE || status == NetworkStatus.TYPE_WIFI) {
                            Log.i("?????????????????????", "??????????????? ??????");
                            Intent i = new Intent(getContext(), AddressApiActivity.class);
                            // ???????????? ??????????????? ?????????
                            // overridePendingTransition(0, 0);
                            // ????????????
                            getActivity().startActivityForResult(i, SEARCH_ADDRESS_ACTIVITY);
                            //MainActivity.moveToAddressApi();
                        } else {
                            Toast.makeText(getContext(), "????????? ????????? ??????????????????.", Toast.LENGTH_SHORT).show();
                        }
                    } catch (NumberFormatException e) {
                        notify_latitude = 0;
                        notify_longitude = 0;
                        Toast.makeText(getContext().getApplicationContext(), "??? ????????? ?????? ??????????????????", Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(getContext().getApplicationContext(), "?????? ?????? ??? ?????? ??????????????????", Toast.LENGTH_LONG).show();
                }
            }
        });

        notify_add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                notify_content_string = notify_content_editText.getText().toString();
                    if(markers.size() == 0) {
                        Toast.makeText(getContext(), "?????? ?????? ????????? ??????????????????", Toast.LENGTH_SHORT).show();
                    }else if(notify_content_string.length() == 0 ) {
                        Toast.makeText(getContext(), "?????? ????????? ??????????????????", Toast.LENGTH_SHORT).show();
                    } else {
                        registerNotify (getPickedLng, getPickedLat, notify_content_string);
                        markers.clear();
                        mapView.removePOIItem(tempMarker);
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


    //MapView.MapViewEventListener ??????
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
//        Log.i(LOG_TAG, String.format("isPost????????????.: %d", isPost));
        if(markers.size() == 0){
            setMapMarker(mapView, mapPoint);
            markers.add("1");
        } else{
            Toast.makeText(getActivity(), "????????? ?????? ?????? ??????????????? ???????????? ???????????? ????????? ??????????????????!",
                    Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    public void onMapViewLongPressed(MapView mapView, MapPoint mapPoint) {
        getPickedLat = mapPoint.getMapPointGeoCoord().latitude;
        getPickedLng = mapPoint.getMapPointGeoCoord().longitude;

//        isPost = checkPostHistory();
//        Log.i(LOG_TAG, String.format("isPost????????????.: %d", isPost));
        if(markers.size() == 0){
            setMapMarker(mapView, mapPoint);
            markers.add("1");
        } else{
            Toast.makeText(getActivity(), "????????? ?????? ?????? ??????????????? ???????????? ???????????? ????????? ??????????????????!",
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
        //????????? ????????? ??????
        // 1. ?????? ???????????? ????????? ????????? ???????????????.
        int hasFineLocationPermission = ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_FINE_LOCATION);

        if (hasFineLocationPermission == PackageManager.PERMISSION_GRANTED ) {
            // 2. ?????? ???????????? ????????? ?????????
            // 3.  ?????? ?????? ????????? ??? ??????
//            mapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading); //TrackingModeOnWithoutHeading


        } else {  //2. ????????? ????????? ????????? ?????? ????????? ????????? ????????? ???????????????. 2?????? ??????(3-1, 4-1)??? ????????????.

            // 3-1. ???????????? ????????? ????????? ??? ?????? ?????? ????????????
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), REQUIRED_PERMISSIONS[0])) {

                // 3-2. ????????? ???????????? ?????? ?????????????????? ???????????? ????????? ????????? ???????????? ????????? ????????????.
                Toast.makeText(getActivity(), "??? ?????? ??????????????? ?????? ?????? ????????? ???????????????.", Toast.LENGTH_LONG).show();
                // 3-3. ??????????????? ????????? ????????? ?????????. ?????? ????????? onRequestPermissionResult?????? ???????????????.
                ActivityCompat.requestPermissions(getActivity(), REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);

            } else {
                // 4-1. ???????????? ????????? ????????? ??? ?????? ?????? ???????????? ????????? ????????? ?????? ?????????.
                // ?????? ????????? onRequestPermissionResult?????? ???????????????.
                ActivityCompat.requestPermissions(getActivity(), REQUIRED_PERMISSIONS,
                        PERMISSIONS_REQUEST_CODE);
            }

        }

    }

    // ????????? ????????? ?????????
    class CustomCalloutBalloonAdapter implements CalloutBalloonAdapter {
        private final View mCalloutBalloon;

        public CustomCalloutBalloonAdapter() {
            mCalloutBalloon = getLayoutInflater().inflate(R.layout.custom_callout_balloon, null);
        }

        @Override
        public View getCalloutBalloon(MapPOIItem poiItem) {

                ((TextView) mCalloutBalloon.findViewById(R.id.title)).setText(poiItem.getItemName());
            if(poiItem.getTag()==1) {
                ((TextView) mCalloutBalloon.findViewById(R.id.desc)).setText("??????!");
            } else{
                ((TextView) mCalloutBalloon.findViewById(R.id.desc)).setText("????????????");
            }
            return mCalloutBalloon;
        }

        @Override
        public View getPressedCalloutBalloon(MapPOIItem poiItem) {
            return null;
        }

    }

    //?????? ?????????
    class MarkerEventListener implements MapView.POIItemEventListener {
        @Override
        public void onPOIItemSelected(MapView mapView, MapPOIItem mapPOIItem) {

        }

        @Override
        public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem) {

        }

        // ????????? ?????? ?????? ???
        @Override
        public void onCalloutBalloonOfPOIItemTouched(MapView mapView, MapPOIItem mapPOIItem, MapPOIItem.CalloutBalloonButtonType calloutBalloonButtonType) {

            // ???????????? ????????? ?????? ?????? ???
            if(mapPOIItem.getTag()==1){
                android.app.AlertDialog.Builder builder = new android.app.AlertDialog.Builder(getActivity());
                builder.setTitle("???????????????");
                // ?????? ??????
                    builder.setItems(R.array.LAN2, new DialogInterface.OnClickListener(){
                        @Override
                        public void onClick(DialogInterface dialog, int pos)
                        {
                            String[] items = getResources().getStringArray(R.array.LAN2);
                            FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();


                            Toast.makeText(getActivity(),items[pos],Toast.LENGTH_SHORT).show();
                            // ??? ???????????? ????????? ???
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
        builder.setTitle("?????? ????????? ????????????");
        builder.setMessage("?????? ???????????? ???????????? ?????? ???????????? ???????????????.\n"
                + "?????? ????????? ???????????????????");
        builder.setCancelable(true);
        builder.setPositiveButton("??????", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                Intent callGPSSettingIntent
                        = new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivityForResult(callGPSSettingIntent, GPS_ENABLE_REQUEST_CODE);
            }
        });
        builder.setNegativeButton("??????", new DialogInterface.OnClickListener() {
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
        //??? ????????? ?????? ?????? ??????
        mapView.setMapCenterPoint(currentMapPoint, true);
        //?????? 1?????? ???????????? ?????? ??????
//        mapView.setCurrentLocationTrackingMode(MapView.CurrentLocationTrackingMode.TrackingModeOff); //TrackingModeOnWithoutHeading
        //??????????????? ?????? ?????? ??????
        mCurrentLat = mapPointGeo.latitude;
        mCurrentLng = mapPointGeo.longitude;
        Log.d(LOG_TAG, "???????????? => " + mCurrentLat + "  " + mCurrentLng);

    }

    public void setMapMarker(MapView mapView, MapPoint mapPoint) {
        MapPOIItem marker = new MapPOIItem();
        int tagNum=1;
        marker.setItemName("?????? ?????? ??????");
//        if(strUserId.equals(user_list)){
            //??? ????????? ???????????? ????????? ????????? ??????
//            marker.setTag(0);
//        }
//        else{
//            tagNum = Integer.parseInt(user_list);
        marker.setTag(tagNum);

        marker.setMapPoint(mapPoint);
        //Log.i(LOG_TAG, String.format("???????????????" + marker.getTag()));
        marker.setMarkerType(MapPOIItem.MarkerType.CustomImage);

        marker.setCustomImageResourceId(R.drawable.sirenback);

        getPickedLat = marker.getMapPoint().getMapPointGeoCoord().latitude;
        getPickedLng = marker.getMapPoint().getMapPointGeoCoord().longitude;


        marker.setCustomImageAutoscale(true);
        marker.setCustomImageAnchor(0.5f, 1.0f);
        marker.setDraggable(true);
        mapView.addPOIItem(marker);

        tempMarker = marker;

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

    public void registerNotify (double notify_longitude, double notify_latitude, String notify_content){
        String url = CommonMethod.ipConfig + "/api/registerNotify";

        try{
            String jsonString = new JSONObject()
                    .put("userId", ProfileData.getUserId())
                    .put("notify_longitude", notify_longitude)
                    .put("notify_latitude", notify_latitude)
                    .put("notify_name", "?????? ??????")
                    .put("notify_content", notify_content)
                    .toString();

            //REST API
            RequestHttpURLConnection.NetworkAsyncTask networkTask = new RequestHttpURLConnection.NetworkAsyncTask(url, jsonString);
            networkTask.execute().get();
            Toast.makeText(getContext().getApplicationContext(), "???????????? ????????? ?????????????????????", Toast.LENGTH_SHORT).show();
            //Log.i("???????????? ??????", String.format("????????? ?????? ?????? : lat " + current_latitude + ", long " + current_longitude));
            notify_content_editText.setText("");
        }catch(Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        switch (requestCode) {
            case GPS_ENABLE_REQUEST_CODE:
                //???????????? GPS ?????? ???????????? ??????
                if (checkLocationServicesStatus()) {
                    if (checkLocationServicesStatus()) {
                        Log.d("@@@", "onActivityResult : GPS ????????? ?????????");
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
                    Toast.makeText(getContext(), "???????????? ????????? ??????, ?????? ?????? ?????? ??? ????????????", Toast.LENGTH_LONG);
                } else {
                    Address addr = address.get(0);
                    Log.i("address ?????? ok", String.valueOf(addr.getLatitude()));
                    notify_latitude = addr.getLatitude();
                    notify_longitude = addr.getLongitude();
                    mapView.setMapCenterPoint(MapPoint.mapPointWithGeoCoord(notify_latitude, notify_longitude), true);
                    Toast.makeText(getContext(), "???????????? ????????? ??????, ?????? ?????? ?????????????????????", Toast.LENGTH_LONG);
                }
            }
        }
    }

    private Location getMyLocation() {
        Location currentLocation = null;
        // Register the listener with the Location Manager to receive location updates
        if (ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            System.out.println("////////////??????????????? ????????? ???????????????");
            ActivityCompat.requestPermissions(getActivity(), new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION}, this.REQUEST_CODE_LOCATION);
            getMyLocation();
        }
        else {
            System.out.println("////////////???????????? ????????????");

            // ???????????? ?????? ?????????
            locationManager = (LocationManager) getActivity().getSystemService(getActivity().LOCATION_SERVICE);
            currentLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (currentLocation != null) {
                double lng = currentLocation.getLongitude();
                double lat = currentLocation.getLatitude();
            }
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000, 1, gpsLocationListener);
            //locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 1000, 1, gpsLocationListener);

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
        markers.clear();
        mapView.removePOIItem(tempMarker);
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
        markers.clear();
        mapView.removePOIItem(tempMarker);
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
//                .title("?????? ?????? ??????" ));
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
            //Toast.makeText(this.getActivity(), "?????? ??????", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this.getActivity(), "?????? ??????", Toast.LENGTH_LONG).show();

            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), permissions[0])) {
                Toast.makeText(this.getActivity(), "?????? ?????? ?????????.", Toast.LENGTH_LONG).show();
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
                    //Toast.makeText(getActivity(), permissions[i] + " ????????? ?????????.", Toast.LENGTH_LONG).show();
                } else {
                    Toast.makeText(getActivity(), permissions[i] + " ????????? ???????????? ??????.", Toast.LENGTH_LONG).show();
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






