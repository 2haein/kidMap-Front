package com.safekid.safe_map.FHome;

public class DangerPoint {

    double type;
    double lat;
    double lng;
    String tag;

    public DangerPoint() {

    }

    public DangerPoint(double v, double lat, double lon) {
        type = v;
        this.lat = lat;
        this.lng = lon;
    }




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
