package com.example.safe_map.FCheckMap;


//==

import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

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

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;



    //============================ GET ===================================

    // 경로 찾기용
    jPoint jp_src = null;
    jPoint jp_dst = null;

    // 지도에 마커로 띄우기용
    //MapPoint mp_src = MapPoint.mapPointWithGeoCoord(jp_src.GetLat(),jp_src.GetLng());
    // MapPoint mp_dst = MapPoint.mapPointWithGeoCoord(jp_dst.GetLat(),jp_dst.GetLng());


    //============================= GET END ==================================




    // 0509 테스트 좌표============================
    jPoint jp_src_test = new jPoint(37.503604177, 126.951403055); //  # 70
    jPoint jp_dst_test = new jPoint(37.504353194, 126.948863560); //  # 117

    MapPoint mp_src_test = MapPoint.mapPointWithGeoCoord(jp_src_test.GetLat(),jp_src_test.GetLng());
    MapPoint mp_dst_test = MapPoint.mapPointWithGeoCoord(jp_dst_test.GetLat(),jp_dst_test.GetLng());

    // 지도 중심 용도.
    double lat_d =  (jp_src_test.GetLat() + jp_dst_test.GetLat()) /2.0;
    double lon_d =  (jp_src_test.GetLng() + jp_dst_test.GetLng()) /2.0;
    MapPoint mid = MapPoint.mapPointWithGeoCoord(lat_d, lon_d);
    //=============================== 테스트 좌표 끝



    MapView  mapView;    //MapView  mapView = new MapView(getActivity());
    Context mContext;

    Astar as = new Astar();

    // 위험지역
    ArrayList<DangerPoint> DangerZone = new ArrayList<>();



    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment CheckMapFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static CheckMapFragment newInstance(String param1, String param2) {
        CheckMapFragment fragment = new CheckMapFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
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

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_check_map, container, false);


        //지도
        mapView = new MapView(getContext());
        ViewGroup mapViewContainer = (ViewGroup) v.findViewById(R.id.map_view);
        mapViewContainer.addView(mapView);

        // 중심점 변경
        mapView.setMapCenterPoint(mid, true);

        // 줌 레벨 변경
        mapView.setZoomLevel(2, true);


        // 0. 시작점, 도착점 지도에 마커로 띄우기
        MapPOIItem marker_src = new MapPOIItem();
        marker_src.setItemName("출발 지점");
        marker_src.setTag(0);
        marker_src.setMapPoint(mp_src_test);
        marker_src.setMarkerType(MapPOIItem.MarkerType.BluePin); // 기본으로 제공하는 BluePin 마커 모양.
        marker_src.setSelectedMarkerType(MapPOIItem.MarkerType.RedPin); // 마커를 클릭했을때, 기본으로 제공하는 RedPin 마커 모양.

        MapPOIItem marker_dst = new MapPOIItem();
        marker_dst.setItemName("도착 지점");
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

        SearchPath(jp_src_test, jp_dst_test);

        // 3. 지도에 안전 경로 띄우기
        ShowPathOnMap();

        return v;
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
        for(int o = 0 ; o < DangerZone.size() ; o++) {
            MapCircle circle = new MapCircle(
                    MapPoint.mapPointWithGeoCoord(DangerZone.get(o).GetLat(), DangerZone.get(o).GetLng()), // center
                    5, // radius, meter

                    // type에 따라 색 바꿀 예정
                    Color.argb(128, 0, 0, 0), // strokeColor
                    Color.argb(128, 255, 0, 0) // fillColor
            );
            mapView.addCircle(circle);
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





