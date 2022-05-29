package com.safekid.safe_map.FCheckMap;


//==

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.safekid.safe_map.http.CommonMethod;
import com.safekid.safe_map.FHome.AddMissionActivity;
import com.safekid.safe_map.FHome.Astar;
import com.safekid.safe_map.FHome.DangerPoint;
import com.safekid.safe_map.FHome.jPoint;
import com.safekid.safe_map.R;
import com.safekid.safe_map.common.ProfileData;

import com.safekid.safe_map.http.RequestHttpURLConnection;
import com.skt.Tmap.TMapMarkerItem;
import com.skt.Tmap.TMapPoint;
import com.skt.Tmap.TMapPolyLine;

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

    Astar astar = new Astar();


    // 경로 정보
    final int onfoot = 0;
    final int alley = 1;
    final int traffic = 2;
    final int crosswalk = 3;


    // 출발, 도착 좌표
    double src_lat;
    double src_lon;
    double dst_lat;
    double dst_lon;

    jPoint jp_src = new jPoint();
    jPoint jp_dst = new jPoint();

    // 출발, 도착 지점 이름
    String src_name = "";
    String dst_name = "";

    // 디폴트 좌표 : 중앙대 310관 운동장
    MapPoint mp_default = MapPoint.mapPointWithGeoCoord(37.503619745977055,126.95668175768733);

    MapView  mapView;    //MapView  mapView = new MapView(getActivity());
    Context mContext;

    ViewGroup mapViewContainer;

    Button child_location, finish;

    String current_child;

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

        // 심부름을 설정 했다면
        String result = fetchErrandChecking();

        Log.i("result ", result);

        if (result.equals("false")){

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


        } else {
            AlertDialog.Builder dlg = new AlertDialog.Builder(getActivity());
            dlg.setTitle("심부름 설정하기");
            dlg.setMessage("심부름을 설정해주세요");
            dlg.setPositiveButton("확인",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int which) {
                            // 처리할 코드 작성
                            // 여기에 확인(심부름 보내기) 버튼 누르면 그냥 dlg이 없어지는 동작을 넣어주세요.
                            // 현재 심부름 보내기를 누르면
                            // Unable to start activity ComponentInfo{com.safekid.safe_map/com.safekid.safe_map.FHome.AddMissionActivity}: java.lang.NumberFormatException: For input string: ""
                            // 에러가 뜨면서 앱이 종료됩니다.
                            //Intent intent = new Intent(getActivity(), AddMissionActivity.class); //fragment라서 activity intent와는 다른 방식
                            //intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION);
                            //startActivity(intent);
                        }
                    });
            dlg.show();
        }

        // 버튼으로 아이 위치 불러오기
        child_location = (Button) v.findViewById(R.id.location_upd);
        child_location.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (current_child != null){
                    String childInfo = fetchChild(current_child);
                    try {
                        JSONObject Alldata = new JSONObject(childInfo);
                        String childName = Alldata.getString("childName");
                        Double child_lat = Alldata.getDouble("current_latitude");
                        Double child_long = Alldata.getDouble("current_longitude");
                        Log.i("Child_lat ", child_lat.toString());

                        MapPoint mark_point_child = MapPoint.mapPointWithGeoCoord(child_lat,child_long);
                        MapPOIItem markerchild = new MapPOIItem();
                        markerchild.setItemName("우리 아이");
                        markerchild.setTag(0);
                        markerchild.setMapPoint(mark_point_child);
                        markerchild.setMarkerType(MapPOIItem.MarkerType.CustomImage);
                        markerchild.setCustomImageResourceId(R.drawable.student);
                        mapView.addPOIItem(markerchild);
                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }

            }
        });

        // 5초마다 아이 위치 불러오기
        /*
        Timer scheduler = new Timer();
        TimerTask task = new TimerTask() {
            private static final int REQUEST_CODE_LOCATION = 2;

            @Override
            public void run() {
                String locationProvider = LocationManager.GPS_PROVIDER;
                if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.
                    return;
                }
                // 현재 자녀 위치 불러오기
                if (ProfileData.getErrandChildId() != ""){
                    String childInfo = fetchChild(ProfileData.getErrandChildId());
                    try {
                        JSONObject Alldata = new JSONObject(childInfo);
                        String childName = Alldata.getString("childName");
                        Double child_lat = Alldata.getDouble("current_latitude");
                        Double child_long = Alldata.getDouble("current_longitude");
                        Log.i("Child_lat ", child_lat.toString());

                    } catch (JSONException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }
                }
            }
        };

        scheduler.scheduleAtFixedRate(task, 0, 5000); // 5초 뒤 1초마다 반복실행//
        */


        finish = (Button) v.findViewById(R.id.finish_errand);
        finish.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getContext(), CheckingQuestActivity.class);
                startActivity(intent);
            }
        });


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


    private void GetErrandData() {
        String url = CommonMethod.ipConfig + "/api/fetchRecentErrand";
        String rtnStr = "";

        try {
            String jsonString = new JSONObject()
                    .put("userId", ProfileData.getUserId())
                    .toString();

            //REST API
            RequestHttpURLConnection.NetworkAsyncTask networkTask = new RequestHttpURLConnection.NetworkAsyncTask(url, jsonString);
            rtnStr = networkTask.execute().get();

          //  Log.d("ChildMap123", "/api/fetchRecentErrand : " + rtnStr);

            JSONObject Alldata = new JSONObject(rtnStr);

            // 2. 좌표 추출
            src_lat = Double.parseDouble(Alldata.getString("start_latitude"));
            src_lon = Double.parseDouble(Alldata.getString("start_longitude"));
            dst_lat = Double.parseDouble(Alldata.getString("target_latitude"));
            dst_lon = Double.parseDouble(Alldata.getString("target_longitude"));
            current_child = Alldata.getString("uuid");

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

    private void ParseInformations() {
        astar.ParseNode(mContext);
        astar.ParseLinks(mContext);
        astar.ParseDanger(mContext);
    }


    private void FindSafePath(){
        // 1. 위험 지역을 찾는다.
        astar.FindDangerousNodeNum();

        // 2. 출발/도착지와 가장 가까운 노드번호를 찾는다.
        int start = astar.findCloseNode(jp_src);
        int end = astar.findCloseNode(jp_dst);

        // 3. 두 노드 번호를 이용하여 A* 알고리즘 실행.
        astar.AstarSearch(start, end);

        // 4. closeList를 탐색하여 경로 찾기
        astar.FindPath(start, end);

        // 5. 찾은 노드번호 경로를 이용하여 ( 출발 + 경로 + 도착 ) 좌표 리스트 추출.
        astar.GetCoordPath(jp_src.GetLat(), jp_src.GetLng(), jp_dst.GetLat(), jp_dst.GetLng());

        // 6. 경로 정보 파악.
        astar.GetPathInfo();
    }


    private void ShowSrcMidDstOnMap(){
        int size =  astar.jp_path.size();

        // src marker
        MapPoint mark_point = MapPoint.mapPointWithGeoCoord(astar.jp_path.get(0).GetLat(), astar.jp_path.get(0).GetLng());
        MapPOIItem marker = new MapPOIItem();
        marker.setItemName(src_name);
        marker.setTag(0);
        marker.setMapPoint(mark_point);
        marker.setMarkerType(MapPOIItem.MarkerType.CustomImage);
        marker.setCustomImageResourceId(R.drawable.jhouse);
        mapView.addPOIItem(marker);

        // middle marker
        MapPoint mark_point2 = MapPoint.mapPointWithGeoCoord(astar.jp_path.get(size / 2-1).GetLat(), astar.jp_path.get(size / 2-1).GetLng());
        MapPOIItem marker2 = new MapPOIItem();
        marker2.setItemName("middle");
        marker2.setTag(0);
        marker2.setMapPoint(mark_point2);
        marker2.setMarkerType(MapPOIItem.MarkerType.CustomImage);
        marker2.setCustomImageResourceId(R.drawable.jmiddle);
        mapView.addPOIItem(marker2);

        // dst marker
        MapPoint mark_point3= MapPoint.mapPointWithGeoCoord(astar.jp_path.get(astar.jp_path.size()-1).GetLat(), astar.jp_path.get(astar.jp_path.size()-1).GetLng());
        MapPOIItem marker3 = new MapPOIItem();
        marker3.setItemName(dst_name);
        marker3.setTag(0);
        marker3.setMapPoint(mark_point3);
        marker3.setMarkerType(MapPOIItem.MarkerType.CustomImage);
        marker3.setCustomImageResourceId(R.drawable.jend);
        mapView.addPOIItem(marker3);

        mapView.setMapCenterPoint(mark_point2, true);
    }


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


    void addpath(int start,int end, int type){
        // 경로를 지도에 띄우기

        MapPolyline polyline = new MapPolyline();


        if(type == onfoot){
            polyline.setLineColor(Color.BLACK);
        }
        else if(type == alley){
            polyline.setLineColor(Color.GRAY);
        }
        else if(type == traffic){
            polyline.setLineColor(Color.GREEN);
        }
        else if(type == crosswalk) {
            polyline.setLineColor(Color.BLUE);
        }
        else{

        }

        for(int y = start ; y <= end ; y++) {
            MapPoint tp = MapPoint.mapPointWithGeoCoord(astar.jp_path.get(y+1).GetLat(),astar.jp_path.get(y+1).GetLng());
            polyline.addPoint(tp);
        }

        mapView.addPolyline(polyline);


    }


    // 위험 구역 지도에 띄우기
    private void ShowDangerZoneOnMap() {

        int RED = 0;
        int GREEN = 0;
        int BLUE = 0;
        String TAG = "";


        for (int o = 0; o < astar.DangerZone.size(); o++) {

            if (astar.DangerZone.get(o).GetType() == 1.0) {

                TAG = "성범죄자 거주 구역";

                MapPoint mark_point = MapPoint.mapPointWithGeoCoord(astar.DangerZone.get(o).GetLat(), astar.DangerZone.get(o).GetLng());
                MapPOIItem marker = new MapPOIItem();
                marker.setItemName(TAG);
                marker.setTag(0);
                marker.setMapPoint(mark_point);
                marker.setMarkerType(MapPOIItem.MarkerType.CustomImage);
                marker.setCustomImageResourceId(R.drawable.jdevil);
                mapView.addPOIItem(marker);
            }
            // 보행자 사고 다발 지역인 경우
            else if (astar.DangerZone.get(o).GetType() == 2.0) {

                TAG = "보행자 사고 다발 구역";

                MapPoint mark_point = MapPoint.mapPointWithGeoCoord(astar.DangerZone.get(o).GetLat(), astar.DangerZone.get(o).GetLng());
                MapPOIItem marker = new MapPOIItem();
                marker.setItemName(TAG);
                marker.setTag(0);
                marker.setMapPoint(mark_point);
                marker.setMarkerType(MapPOIItem.MarkerType.CustomImage);
                marker.setCustomImageResourceId(R.drawable.jaccident);
                mapView.addPOIItem(marker);
            }
            // 자전거 사고 다발 지역인 경우
            else if (astar.DangerZone.get(o).GetType() == 3.0) {

                TAG = "자전거 사고 다발 구역";
                MapPoint mark_point = MapPoint.mapPointWithGeoCoord(astar.DangerZone.get(o).GetLat(), astar.DangerZone.get(o).GetLng());
                MapPOIItem marker = new MapPOIItem();
                marker.setItemName(TAG);
                marker.setTag(0);
                marker.setMapPoint(mark_point);
                marker.setMarkerType(MapPOIItem.MarkerType.CustomImage);
                marker.setCustomImageResourceId(R.drawable.jcyclist);
                mapView.addPOIItem(marker);
            }
            // 교통사고 주의 구간인 경우
            else if (astar.DangerZone.get(o).GetType() == 4.0) {

                TAG = "교통사고 주의 구역";
                MapPoint mark_point = MapPoint.mapPointWithGeoCoord(astar.DangerZone.get(o).GetLat(), astar.DangerZone.get(o).GetLng());
                MapPOIItem marker = new MapPOIItem();
                marker.setItemName(TAG);
                marker.setTag(0);
                marker.setMapPoint(mark_point);
                marker.setMarkerType(MapPOIItem.MarkerType.CustomImage);
                marker.setCustomImageResourceId(R.drawable.jaccident_car);
                mapView.addPOIItem(marker);
            }
            else {

                TAG = "시민 신고 지역";

                MapPoint mark_point = MapPoint.mapPointWithGeoCoord(astar.DangerZone.get(o).GetLat(), astar.DangerZone.get(o).GetLng());
                MapPOIItem marker = new MapPOIItem();
                marker.setItemName(TAG);
                marker.setTag(0);
                marker.setMapPoint(mark_point);
                marker.setMarkerType(MapPOIItem.MarkerType.CustomImage);
                marker.setCustomImageResourceId(R.drawable.jsirenback);
                mapView.addPOIItem(marker);
            }
        }
    }

    // 해당 지점 마커로 타입에 맞게 띄우기
    private void addmarker(int start, int type) {

        Log.d("애드마커","start : "+start+" type : "+type);

        double mid_lat = (astar.jp_path.get(start+1).GetLat() + astar.jp_path.get(start + 2).GetLat())/2.0;
        double mid_lon = (astar.jp_path.get(start+1).GetLng() + astar.jp_path.get(start + 2).GetLng())/2.0;

        MapPoint mark_point = MapPoint.mapPointWithGeoCoord(mid_lat, mid_lon);
        MapPOIItem marker = new MapPOIItem();


        if(type == onfoot){

        }
        else if(type == alley){
            marker.setItemName("골목길");
            marker.setMarkerType(MapPOIItem.MarkerType.CustomImage);
            marker.setCustomImageResourceId(R.drawable.jmarker);
        }
        else if(type == traffic){
            marker.setItemName("신호등");
            marker.setMarkerType(MapPOIItem.MarkerType.CustomImage);
            marker.setCustomImageResourceId(R.drawable.jtraffic_lights);
        }
        else if(type == crosswalk){
            marker.setItemName("횡단보도");
            marker.setMarkerType(MapPOIItem.MarkerType.CustomImage);
            marker.setCustomImageResourceId(R.drawable.jcross);
        }
        else{

        }

        marker.setMapPoint(mark_point);
        mapView.addPOIItem(marker);

    }


    public String fetchChild(String UUID){
        String url = CommonMethod.ipConfig + "/api/fetchChild";
        String rtnStr= "";
        String[] result = new String[0];

        try{
            String jsonString = new JSONObject()
                    .put("UUID", UUID)
                    .toString();

            //REST API
            RequestHttpURLConnection.NetworkAsyncTask networkTask = new RequestHttpURLConnection.NetworkAsyncTask(url, jsonString);
            rtnStr = networkTask.execute().get();
            Log.i("wkwkkwk" , rtnStr);
            //String result2 = rtnStr.substring(1, rtnStr.length() - 1);
            //Log.i("data22 " , result2);

            /*result = result2.split(",");
            for (int i=0; i<result.length; i++){
                result[i] = result[i].substring(1, result[i].length() - 1);
            }*/
        }catch(Exception e){
            e.printStackTrace();
        }
        return rtnStr;

    }

    public String fetchErrandChecking(){
        String url = CommonMethod.ipConfig + "/api/fetchErrandChecking";
        String rtnStr= "";

        try{
            String jsonString = new JSONObject()
                    .put("userId", ProfileData.getUserId())
                    .toString();

            //REST API
            RequestHttpURLConnection.NetworkAsyncTask networkTask = new RequestHttpURLConnection.NetworkAsyncTask(url, jsonString);
            rtnStr = networkTask.execute().get();
        }catch(Exception e){
            e.printStackTrace();
        }
        return rtnStr;
    }
}





