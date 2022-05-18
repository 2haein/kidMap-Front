package com.example.safe_map.Child;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;

import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.safe_map.R;

import net.daum.mf.map.api.MapView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ChildMapView#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ChildMapView extends Fragment {

    MapView mapView;
    private LocationManager locationManager;
    private static final int REQUEST_CODE_LOCATION = 2;

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

        // 아이의 위치 수신을 위한 세팅
        locationManager = (LocationManager)getContext().getSystemService(Context.LOCATION_SERVICE);
        // 아이의 현재 위치
        Location userLocation = getMyLocation();
        if( userLocation != null ) {
            double latitude = userLocation.getLatitude();
            double longitude = userLocation.getLongitude();
            System.out.println("아이 현재 위치값 : "+latitude+","+longitude);
        }

        return rootView;
    }

    /**
     * 사용자의 위치를 수신
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

}