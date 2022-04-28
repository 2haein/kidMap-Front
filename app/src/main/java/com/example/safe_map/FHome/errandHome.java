package com.example.safe_map.FHome;

import java.util.Date;

public class errandHome {
    String childName;
    String date;
    String errandContent;
    String destination;


    public errandHome(String childName, String date, String errandContent, String destination){
        this.childName = childName;
        this.date = date;
        this.errandContent = errandContent;
        this.destination = destination;
    }

    public String getChildName() { return childName; }
    public String getDate() { return date; }
    public String geterrandContent() { return errandContent; }
    public String getDestination() { return destination; }


    public void setChildName(String Num) { this.childName = Num; }
    public void setDate(String Num) { this.date = Num; }
    public void setErrandContent(String Num) { this.errandContent = Num; }
    public void setdestination(String Num) { this.destination = Num; }
}
