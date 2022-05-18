package com.example.safe_map.FCheckMap;

public class DangerPoint {

    double type;
    double lat;
    double lng;


    void SetType(double i ){
        type = i;
    }
    void SetLat(double lat){
        this.lat = lat;
    }
    void SetLng(double lng){
        this.lng = lng;
    }

    double GetType(){
        return type;
    }
    double GetLat(){
        return lat;
    }
    double GetLng(){
        return lng;
    }

}
