package com.example.safe_map.FCheckMap;

import android.content.Context;
import android.util.Log;

import java.io.InputStream;
import java.util.ArrayList;

// 안드로이드 스튜디오와 인텔리제이에서 제공하는 라이브러리가 약간 다르다.
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.IOException;


public class Astar {

    final int nodeNum = 0;
    final int parent = 1;
    final int F = 2;    // 휴리스틱 값
    final int type = 2;

    ArrayList<Integer> path = new ArrayList<>();  // paths
    ArrayList<jPoint> nodes = new ArrayList<>();    // nodes
    ArrayList<int[]>[] links = new ArrayList[129];  // links
    ArrayList<Integer> dangerNodeNum = new ArrayList<>(); // 위험 지역에 속하는 노드 번호들

    int ClosePointer = 0; // 닫힌 리스트 pointer
    int OpenPointer = 0;  // 열린 리스트 pointer

    int[][] OpenTEST = new int[80][3];  // 열린 리스트 : 들어오고, 삭제되는 것이 있다.
    int[][] CloseTEST =  new int[80][2]; // 닫힌 리스트 : 들어오는 것 만 있고, 삭제는 이뤄지지 않는다.


    //=================== 메소드 ===========================

    // json 파싱
    void ParseNode(Context context) {
        String jsonString = null;
        try {
            InputStream is = context.getAssets().open("nodes_1.json");
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
                jPoint jp = new jPoint();
                // 소숫점이 14자리를 넘어가자 캐스팅이 안된다고 뜬다.
                jp.SetLat(Double.parseDouble(String.valueOf(Object.get(0))));
                jp.SetLng(Double.parseDouble(String.valueOf(Object.get(1))));
                nodes.add(jp);
            }

           // Log.d("test",""+"as.nodes size : " + nodes.size());
        }catch (JSONException e) {
            e.printStackTrace();
        }
    }
    void ParseLinks(Context context) {
        String jsonString = null;
        try {
            InputStream is = context.getAssets().open("links_1.json");
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
            JSONArray LinkArray = jsonObject.getJSONArray("links");
            JSONArray jsonObj3;
            JSONArray jsonObj2;

            for(int i=0 ; i< LinkArray.length(); i++){

                // jsonObj2 = [[1,0],[2,0]]
                jsonObj2 = (JSONArray)LinkArray.get(i);

                // 이걸 위해 for 조건 문을 C식으로 바꿈.
                links[i] = new ArrayList<>();

                for (int j = 0 ; j < jsonObj2.length() ; j++) {
                    // jsonObj3 = [1,0]
                    jsonObj3 = (JSONArray) jsonObj2.get(j);
                    int[] tmp = new int[2];
                    tmp[0] = Integer.parseInt(String.valueOf(jsonObj3.get(0)));
                    tmp[1] = Integer.parseInt(String.valueOf(jsonObj3.get(1)));
                    links[i].add(tmp);
                }
            }
        }catch (JSONException e) {
            e.printStackTrace();
        }
        
        
    }


    // 위험 지역에 속하는 노드 번호 찾기 : 5m 이내에 속하면 위험 지역
    void FindDangerousNodeNum(ArrayList<DangerPoint> dp){
        //Log.d("test","size : "+ dp.size());

        for(int i = 0 ; i < nodes.size() ; i++){
            for(int j = 0 ; j < dp.size() ; j++){
                jPoint tmp = new jPoint(dp.get(j).GetLat(),dp.get(j).GetLng());
                double res = GetDistanceWithCoords(nodes.get(i),tmp);
               // Log.d("test","넘버 : "+ i+ "거리 :"+res);
                if(res <= 5.0){
                    dangerNodeNum.add(i);
                }
            }

        }
    }

    // 노드 번호가 위험 지역에 속하는지 체크
    private int IsInDanger(int dstNum) {
        for(int i = 0 ; i < dangerNodeNum.size() ; i ++){
            if(dangerNodeNum.get(i) == dstNum){
                return 1;
            }
        }
        return 0;
    }

    // 받은 좌표와 거리가 가장 가까운 노드의 번호를 반환
    int findCloseNode(jPoint jp_src){
        int nodeNum = -1;
        double dist = 999999999.0;
        double cur_dist = 0;

        for(int i =0 ; i < nodes.size() ; i ++){
            cur_dist = GetDistanceWithCoords(jp_src, nodes.get(i));

            if( cur_dist < dist){
                dist = cur_dist;
                nodeNum = i;
            }
        }

        return nodeNum;
    }

    // 휴리스틱 계산 용 : 노드 번호로 거리 계산 - 위험 지역에 속할 시 가중치 적용
    public int GetDistanceWithNum(int srcNum, int dstNum){
        double weight = 1.0;

        if( IsInDanger(dstNum) == 1){
            weight *= 50.0;
        }

        double a1 = (nodes.get(srcNum).GetLat() - nodes.get(dstNum).GetLat())*100000.0;
        double a2 = (nodes.get(srcNum).GetLng() - nodes.get(dstNum).GetLng())*100000.0;

        return (int) (Math.sqrt(a1*a1 + a2*a2) * weight);
    }

    // 위험 지역에 속하는지 계산 용 : 좌표로 거리 계산
    double GetDistanceWithCoords(jPoint src, jPoint dst){

        final int EARTH_RADIUS = 6371; // Approx Earth radius in KM

        double dLat  = Math.toRadians((dst.GetLat() - src.GetLat()));
        double dLong = Math.toRadians((dst.GetLng() - src.GetLng()));

        double startLat = Math.toRadians(src.GetLat());
        double endLat   = Math.toRadians(dst.GetLat());

        double a = Math.pow(Math.sin(dLat / 2), 2) + Math.cos(startLat) * Math.cos(endLat) * Math.pow(Math.sin(dLong / 2), 2) ;
        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));


        return EARTH_RADIUS * c * 1000 ; // <-- d
    }


    // 그냥 두 점 사이의 거리 계산
    int checkdist2(jPoint src, jPoint dst){

        double a1 = (src.GetLat() - dst.GetLat())*1000000.0;
        double a2 = (src.GetLng() - dst.GetLng())*1000000.0;

        return (int) Math.sqrt(a1*a1 + a2*a2);
    }


    //================== A* Algorithm =======================
    public void AstarSearch(int srcNum, int dstNum) {

        // 첫 번째 노드를 닫힌 리스트에 넣는다.
        CloseTEST[ClosePointer][nodeNum] = srcNum;
        CloseTEST[ClosePointer][parent] = srcNum;

        int num = 0;

        while (true) {
            int curNum = CloseTEST[ClosePointer][nodeNum];

            // 만약 닫힌 리스트에 마지막으로 들어온 노드가 도착지라면 탈출
            if (curNum == dstNum) {
                break;
            }

            // 현재 노드에 대한 인접 노드들의 휴리스틱 값 계산
            for (int[] info : links[curNum]) {
                // 이미 인접 노드가 닫힌 리스트에 존재 할 경우 : 무시
                if (IsInClose(info[0]) == 1) {
                    continue;
                }

                // 인접 노드의 휴리스틱 값 계산
                int heuristic = GetDistanceWithNum(curNum, info[0]) + GetDistanceWithNum(info[0], dstNum);

                // 이 인접 노드가 열린 리스트에 이미 존재하는지 체크
                int IndexOpen = IsInOpen(info[0]);


                // 1. 인접 노드가 열린 리스트에 존재하지 않는다면
                if (IndexOpen == -1) {
                    // 열린 리스트에 삽입.
                    OpenTEST[OpenPointer][parent] = curNum;
                    OpenTEST[OpenPointer][nodeNum] = info[0];
                    OpenTEST[OpenPointer][F] = heuristic;
                    OpenPointer++;

                }
                // 2. 이미 인접 노드가 열린 리스트에 존재 할 경우
                else {
                    // 해당 노드와의 휴리스틱 비교하여 업데이트
                    if (heuristic < OpenTEST[IndexOpen][F]) {
                        OpenTEST[IndexOpen][F] = heuristic;
                        OpenTEST[IndexOpen][parent] = curNum;
                    }
                }
            }
            // 열린 리스트 중 F값이 가장 작은 노드의 인덱스 찾기.
            int indexMin = GetIndexMinFromOpen();
            // 찾은 노드를 닫힌 리스트에 삽입 후 열린 리스트에서 제거.
            CloseTEST[++ClosePointer][nodeNum] = OpenTEST[indexMin][nodeNum];

            // 0514 문제 해결 : CloseTEST[ClosePointer][parent] = curNum으로 해서 계속 뛰어넘기가 나왓다.
            CloseTEST[ClosePointer][parent] = OpenTEST[indexMin][parent];
            RemoveNodeFromOpen(indexMin);
        }
    }

    // 휴리스틱 최솟값을 가지는 노드의 인덱스를 받아 그 위치 삭제
    private void RemoveNodeFromOpen(int indexMin) {
        // 1. 맨 뒤의 인덱스의 정보를 그 인덱스에 저장한다.
        // 2. 포인터를 맨 뒤로 옮긴다.
        OpenTEST[indexMin][nodeNum] = OpenTEST[OpenPointer-1][nodeNum];
        OpenTEST[indexMin][parent] = OpenTEST[OpenPointer-1][parent];
        OpenTEST[indexMin][F] = OpenTEST[OpenPointer-1][F];

        OpenPointer--;

    }

    // OpenList에서 Path를 찾아서 "노드번호"로 저장.
    public void FindPath(int srcNum, int dstNum){

        int node = dstNum;
        path.add(node); // 우선 도착 노드를 넣는다.
       //Log.d("test","path start: "+ node);
        int parent = -1;

        while(true){
            // 그 다음 현재 노드의 부모 노드 번호를 찾는다.
            parent = getParentFromClose(node);
            jPoint pa_node = new jPoint();

            // 만약 부모 노드가 출발 노드라면 넣고 탈출
            if(parent == srcNum){
                path.add(parent);
                //Log.d("test","path end: "+parent);
                break;
            }

            // 출발 노드가 아니면 부모 노드를 넣고, 이 노드가 현재 노드가 됨.
            path.add(parent);
           // Log.d("test","path: "+parent);
            node = parent;

        }
    }

    // 열린 리스트에서 휴리스틱 최솟값을 가지는 노드의 인덱스 반환
    public int GetIndexMinFromOpen() {

        int min = 987654321;
        int idx = 0;

        for(int j = 0 ; j < OpenPointer ; j ++){
            // null에러 날거같은데? >> 포인터 선언시 그 초기화? 클래스 내에서 하는거 하면 됨.
            if( OpenTEST[j][F] < min){
                min = OpenTEST[j][F];
                idx = j;
            }
        }
        return idx;
    }

    // 인풋 노드의 부모 노드 번호를 반환
    public int getParentFromClose(int node){
        for(int i = 0 ; i <= ClosePointer ; i ++){
            if(CloseTEST[i][nodeNum] == node){
                return CloseTEST[i][parent];
            }
        }
        // 이게 반환 될 리는 없다.
        return -2;
    }

    // 해당 노드가 닫힌 공간에 있는지 파악
    int IsInClose(int node){
        // 만약 노드가 닫힌 리스트에 있다면 1, 없다면 0 반환
        for(int t = 0 ; t <= ClosePointer ; t ++){
            if(CloseTEST[t][nodeNum] == node)
                return 1;
        }
        return 0;
    }

    // 해당 노드가 열린 공간에 있는지 파악
    int IsInOpen(int node){
        // 만약 노드가 닫힌 없다면 -1 반환, 있으면 인덱스 위치 반환
        for(int t = 0 ; t < OpenPointer ; t ++){
            if(OpenTEST[t][nodeNum] == node)
                return t;
        }
        return -1;
    }
    //=================  A* Algorithm END ====================


    // 퀘스트 용도 : 경로의 정보 반환
    void GetPathInfo(){
        int traffic = 0;
        int crosswalk = 0;


        for( int i = 1 ; i < path.size() ; i ++){
            for(int[] info : links[path.get(i-1)]) {
                // 이미 인접 노드가 닫힌 리스트에 존재 할 경우 : 무시
                if (info[0] == path.get(i)) {
                    int tp = info[1];

                    switch (tp) {
                        case 1:
                            traffic++;
                            break;
                        case 2:
                            crosswalk++;
                            break;
                        default:
                            break;

                    }
                }
                }
            }

        System.out.println("주어진 경로에 신호등 :" + traffic +"개, 횡단보도 :"+crosswalk+"개가 있습니다.");

    }


    // 파싱 테스트 용도, 파싱 문제 없이 완료
    public void TEST_print_parse(){
        for(int i=0;i<nodes.size();i++){

            Log.d("test node",""+"Lat : " + nodes.get(i).GetLat() + " ");
            Log.d("test node",""+"Lng : " + nodes.get(i).GetLng());
        }


        for(int i = 0 ; i < 57 ; i++){
            for(int j =0 ; j < links[i].size() ; j ++){
               Log.d("test link",""+"nodeNum :" + links[i].get(j)[0] + " type :" +  links[i].get(j)[1]);
            }
        }
    }

    public void TEST_print_path(int srcNum){
        System.out.print("start : " + srcNum);
        for(int i = path.size() - 1 ; i > 0 ; i--){
            System.out.print( " >> " + path.get(i-1) );
        }
        System.out.println(": end");
    }

}
