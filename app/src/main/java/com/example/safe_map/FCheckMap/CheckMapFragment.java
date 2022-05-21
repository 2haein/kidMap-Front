package com.example.safe_map.FCheckMap;


//==

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.example.safe_map.FHome.AddMissionActivity;
import com.example.safe_map.FHome.DangerPoint;
import com.example.safe_map.FHome.jPoint;
import com.example.safe_map.Login.ChildLoginActivity;
import com.example.safe_map.R;

import net.daum.mf.map.api.MapCircle;
import net.daum.mf.map.api.MapPOIItem;
import net.daum.mf.map.api.MapPoint;
import net.daum.mf.map.api.MapPolyline;
import net.daum.mf.map.api.MapView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link CheckMapFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class CheckMapFragment extends Fragment {


    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";



   // json에서 받아온다.
    ArrayList<MapPoint> safe_path = new ArrayList<>();

    // 출발, 도착 지점 이름
    String src_name = "";
    String dst_name = "";

    // 디폴트 좌표 : 중앙대 310관 운동장
    boolean isDefault = true;
    MapPoint mp_default = MapPoint.mapPointWithGeoCoord(37.503619745977055,126.95668175768733);

    MapView  mapView;    //MapView  mapView = new MapView(getActivity());
    Context mContext;


    // 위험지역
    ArrayList<DangerPoint> DangerZone = new ArrayList<>();

    ViewGroup mapViewContainer;


    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.

     * @return A new instance of fragment CheckMapFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CheckMapFragment newInstance(double param1, double param2) {
        CheckMapFragment fragment = new CheckMapFragment();
        Bundle args = new Bundle();
        args.putDouble(ARG_PARAM1, param1);
        args.putDouble(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mContext = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setHasOptionsMenu(true);
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState
    ) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_check_map, container, false);




        // 지도 중심을 설정하기 위한 좌표
        double lat_d = 0.0;
        double lon_d = 0.0;
        MapPoint mid = MapPoint.mapPointWithGeoCoord(lat_d, lon_d);

        // 카카오 지도
        mapView = new MapView(getContext());
        mapViewContainer = (ViewGroup) v.findViewById(R.id.childMapView);
        mapViewContainer.addView(mapView);

        // 중심점 변경
        mapView.setMapCenterPoint(mp_default, true);

        // 줌 레벨 변경
        mapView.setZoomLevel(2, true);

        if (GetErrandDataFromJson()){
            // json으로부터 심부름 설정 정보 불러옴.
            GetErrandDataFromJson();

            ParseDangerZone();
            // 1. 위험 구역, 노드, 링크 파싱 후 위험 구역을 지도에 마커로 띄우기
            ShowDangerZoneOnMap();

            // 3. 지도에 안전 경로 띄우기
            ShowPathOnMap();
        } else {
            AlertDialog.Builder dlg = new AlertDialog.Builder(getActivity());
            dlg.setTitle("심부름 설정하기");
            dlg.setMessage("심부름을 설정해주세요");
            dlg.setPositiveButton("심부름 보내기",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // 처리할 코드 작성
                            Intent intent = new Intent(getActivity(), AddMissionActivity.class); //fragment라서 activity intent와는 다른 방식
                            intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                            startActivity(intent);
                        }
                    });
            dlg.show();
        }


        return v;
    }

    @Override
    public void onResume() {
        Log.d("test",""+"onResume Started");
        mapView.onResume();
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public void onPause(){
        Log.d("test",""+"onPause Started");
        mapViewContainer.removeView(mapView);
        //getActivity().finish();
        super.onPause();
    }

    public void finish() {
        mapViewContainer.removeView(mapView);
        getActivity().finish();
    }


    private boolean GetErrandDataFromJson() {
        String jsonString = null;
        try {
            String filename = "PathInfo.json";
            FileInputStream fos = new FileInputStream(getActivity().getFilesDir()+"/"+filename);
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
                MapPoint mp = MapPoint.mapPointWithGeoCoord(lat,lon);
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
            InputStream is = mContext.getAssets().open("test_danger.json");
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
            }

            // Log.d("test",""+"as.nodes size : " + nodes.size());
        }catch (JSONException e) {
            e.printStackTrace();
        }
    }


    // 위험 구역 지도에 띄우기
    private void ShowDangerZoneOnMap() {

        int RED = 0;
        int GREEN = 0;
        int BLUE = 0;
        String TAG = "";


        for (int o = 0; o < DangerZone.size(); o++) {

            // 만약 위험 지역이 성범죄자 거주 구역이라면
            if (DangerZone.get(o).GetType() == 1.0) {
               RED = 255;
               GREEN = 0;
               BLUE = 0;
               TAG = "성범죄자 거주 구역";
            }
            // 보행자 사고 다발 지역인 경우
            else if(DangerZone.get(o).GetType() == 2.0){
                RED = 0;
                GREEN = 255;
                BLUE = 0;
                TAG = "보행자 사고 다발 구역";
            }
            // 자전거 사고 다발 지역인 경우
            else if(DangerZone.get(o).GetType() == 3.0){
                RED = 0;
                GREEN = 0;
                BLUE = 255;
                TAG = "자전거 사고 다발 구역";
            }
            // 교통사고 주의 구간인 경우
            else{
                RED = 255;
                GREEN = 255;
                BLUE = 255;
                TAG = "교통사고 주의 구역";
            }

            MapCircle circle = new MapCircle(
                    MapPoint.mapPointWithGeoCoord(DangerZone.get(o).GetLat(), DangerZone.get(o).GetLng()),
                    5, // radius, meter
                    Color.argb(128, 0, 0, 0), // strokeColor
                    Color.argb(128, RED, GREEN, BLUE) // fillColor
            );

            MapPoint mark_point = MapPoint.mapPointWithGeoCoord(DangerZone.get(o).GetLat(), DangerZone.get(o).GetLng());
            MapPOIItem marker = new MapPOIItem();
            marker.setItemName(TAG);
            marker.setTag(0);
            marker.setMapPoint(mark_point);
            marker.setMarkerType(MapPOIItem.MarkerType.RedPin); // 기본으로 제공하는 BluePin 마커 모양.
            marker.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin); // 마커를 클릭했을때, 기본으로 제공하는 RedPin 마커 모양.

            mapView.addCircle(circle);
            mapView.addPOIItem(marker);
        }
    }





    // 3. 경로를 지도에 폴리라인으로 띄우기
    void ShowPathOnMap(){
        MapPolyline pathLine = new MapPolyline();

        int size =  safe_path.size();

        MapPoint mark_point1 = MapPoint.mapPointWithGeoCoord(safe_path.get(0).getMapPointGeoCoord().latitude,safe_path.get(0).getMapPointGeoCoord().longitude);
        MapPOIItem marker1 = new MapPOIItem();
        marker1.setItemName("출발");
        marker1.setTag(0);
        marker1.setMapPoint(mark_point1);
        marker1.setMarkerType(MapPOIItem.MarkerType.RedPin); // 기본으로 제공하는 BluePin 마커 모양.
        marker1.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin); // 마커를 클릭했을때, 기본으로 제공하는 RedPin 마커 모양.

        MapPoint mark_point2 = MapPoint.mapPointWithGeoCoord(safe_path.get(size-1).getMapPointGeoCoord().latitude,safe_path.get(size-1).getMapPointGeoCoord().longitude);
        MapPOIItem marker2 = new MapPOIItem();
        marker2.setItemName("도착");
        marker2.setTag(0);
        marker2.setMapPoint(mark_point2);
        marker2.setMarkerType(MapPOIItem.MarkerType.RedPin); // 기본으로 제공하는 BluePin 마커 모양.
        marker2.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin); // 마커를 클릭했을때, 기본으로 제공하는 RedPin 마커 모양.

        mapView.addPOIItem(marker1);
        mapView.addPOIItem(marker2);


        for(int i =1 ; i < safe_path.size()-1 ; i ++){
            MapPoint tmp = MapPoint.mapPointWithGeoCoord(safe_path.get(i).getMapPointGeoCoord().latitude,safe_path.get(i).getMapPointGeoCoord().longitude );
            pathLine.addPoint(tmp);
        }

        mapView.addPolyline(pathLine);

        MapPoint mid = MapPoint.mapPointWithGeoCoord(safe_path.get((int)size/2).getMapPointGeoCoord().latitude,safe_path.get((int)size/2).getMapPointGeoCoord().longitude );
        mapView.setMapCenterPoint(mid, true);


    }

}





