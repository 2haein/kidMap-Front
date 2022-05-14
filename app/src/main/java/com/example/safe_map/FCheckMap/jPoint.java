package com.example.safe_map.FCheckMap;

public class jPoint {

    public int nodeNum; // 너를 넣는 작업이 필요하다.
    public double lat;
    public double lng;


    jPoint(){

    }

    jPoint(double lat, double lng){
        this.lat = lat;
        this.lng = lng;
    }

    // 뭐야 기껏 만들었는데 왜 안 쓰이지?
    public void SetNodeNum(int nodeNum){
        this.nodeNum  = nodeNum;
    }

    public void SetLat(double lat){
        this.lat = lat;
    }

    public void SetLng(double lng){
        this.lng = lng;
    }

    public double GetLat(){
        return lat;
    }

    public double GetLng(){
        return lng;
    }

    public int GetNodeNum(){ return nodeNum;}
}




