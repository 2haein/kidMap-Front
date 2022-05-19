package com.example.safe_map.FCheckMap;


//==

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

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



    // 경로 찾기용
    jPoint jp_src = new jPoint();
    jPoint jp_dst = new jPoint();

    // 출발, 도착 지점 이름
    String src_name = "";
    String dst_name = "";

    // 디폴트 : 중앙대 310관 운동장
    boolean isDefault = true;
    jPoint jp_src_default = new jPoint(37.503619745977055, 126.95668175768733);
    jPoint jp_dst_default = new jPoint(37.503619745977055, 126.95668175768733);

    MapView  mapView;    //MapView  mapView = new MapView(getActivity());
    Context mContext;

    Astar as = new Astar();

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

        // json으로부터 심부름 설정 정보 불러옴.
        GetErrandDataFromJson();


        // 지도에 띄우기 용도의 좌표 객체
        MapPoint mp_src_test = MapPoint.mapPointWithGeoCoord(jp_src.GetLat(),jp_src.GetLng());
        MapPoint mp_dst_test = MapPoint.mapPointWithGeoCoord(jp_dst.GetLat(),jp_dst.GetLng());

        // 지도 중심을 설정하기 위한 좌표
        double lat_d =  (jp_src.GetLat() + jp_dst.GetLat()) /2.0;
        double lon_d =  (jp_src.GetLng() + jp_dst.GetLng()) /2.0;
        MapPoint mid = MapPoint.mapPointWithGeoCoord(lat_d, lon_d);

        // 카카오 지도
        mapView = new MapView(getContext());
        mapViewContainer = (ViewGroup) v.findViewById(R.id.childMapView);
        mapViewContainer.addView(mapView);

        // 중심점 변경
        mapView.setMapCenterPoint(mid, true);

        // 줌 레벨 변경
        mapView.setZoomLevel(2, true);


        // 0. 시작점, 도착점 지도에 마커로 띄우기
        MapPOIItem marker_src = new MapPOIItem();
        marker_src.setItemName(src_name);
        marker_src.setTag(0);
        marker_src.setMapPoint(mp_src_test);
        marker_src.setMarkerType(MapPOIItem.MarkerType.BluePin); // 기본으로 제공하는 BluePin 마커 모양.
        marker_src.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin); // 마커를 클릭했을때, 기본으로 제공하는 RedPin 마커 모양.

        MapPOIItem marker_dst = new MapPOIItem();
        marker_dst.setItemName(dst_name);
        marker_dst.setTag(0);
        marker_dst.setMapPoint(mp_dst_test);
        marker_dst.setMarkerType(MapPOIItem.MarkerType.BluePin);
        marker_dst.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin);

        mapView.addPOIItem(marker_src);
        mapView.addPOIItem(marker_dst);




        // 1. 위험 구역, 노드, 링크 파싱 후 위험 구역을 지도에 마커로 띄우기
        ParseInfos();
        as.FindDangerousNodeNum(DangerZone);
        ShowDangerZoneOnMap();

        // 2. 노드, 링크 파싱 후 안전 경로 찾기
        SearchPath(jp_src, jp_dst);

        // 3. 지도에 안전 경로 띄우기
        ShowPathOnMap();

        return v;
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

    public void finish() {
        mapViewContainer.removeView(mapView);
        getActivity().finish();
    }


    private void GetErrandDataFromJson() {
        String jsonString = null;
        try {
            String filename = "ErrandInfo.json";
            FileInputStream fos = new FileInputStream(getActivity().getFilesDir()+"/"+filename);
           // InputStream is = mContext.getAssets().open(getFilesDir()+ErrandInfo.json");
            Log.d("resttt",""+fos.available());
            int size = fos.available();
            byte[] buffer = new byte[size];
            fos .read(buffer);
            fos .close();
            jsonString = new String(buffer, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
        try{
            JSONObject jsonObject = new JSONObject(jsonString);

            jp_src.SetLat(Double.parseDouble(String.valueOf(jsonObject.get("src_lat"))));
            jp_src.SetLng(Double.parseDouble(String.valueOf(jsonObject.get("src_lon"))));
            src_name = String.valueOf(jsonObject.get("src_name"));

            jp_dst.SetLat(Double.parseDouble(String.valueOf(jsonObject.get("dst_lat"))));
            jp_dst.SetLng(Double.parseDouble(String.valueOf(jsonObject.get("dst_lon"))));
            dst_name = String.valueOf(jsonObject.get("dst_name"));


        }catch (JSONException e) {
            e.printStackTrace();
        }
    }


    // 1. 위험구역, 노드, 링크 파싱
    void ParseInfos(){
        ParseDangerZone();
        as.ParseNode(mContext);
        as.ParseLinks(mContext);
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



    // 2. 경로 찾기
    void SearchPath(jPoint jp_src, jPoint jp_dst){


        // 2-1. 출발지, 도착지와 가장 가까운 "노드 번호" 찾기
        int start = as.findCloseNode(jp_src);
        int end = as.findCloseNode(jp_dst);

        //Log.d("test111",""+"startNum:"+start+" endNum:"+ end);

        //as.TEST_print_parse();

        // 2-2. 노드 번호를 기반으로 길 찾기
        as.AstarSearch(start, end);
        as.FindPath(start,end);

    }


    // 3. 경로를 지도에 폴리라인으로 띄우기
    void ShowPathOnMap(){
        MapPolyline pathLine = new MapPolyline();

        //Log.d("test",""+"path size : " + as.path.size());

        for(int i =0 ; i < as.path.size() ; i ++){
            MapPoint tmp = MapPoint.mapPointWithGeoCoord(as.nodes.get(as.path.get(i)).lat, as.nodes.get(as.path.get(i)).lng);
            pathLine.addPoint(tmp);

        }

        mapView.addPolyline(pathLine);

    }

}





