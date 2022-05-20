package com.example.safe_map.FHome;

public class DangerPoint {

    double type;
    double lat;
    double lng;


    public void SetType(double i){
        type = i;
    }
    public void SetLat(double lat){
        this.lat = lat;
    }
    public void SetLng(double lng){
        this.lng = lng;
    }

    public double GetType(){
        return type;
    }
    public double GetLat(){
        return lat;
    }
    public double GetLng(){
        return lng;
    }

}
