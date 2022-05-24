package com.example.safe_map.FHome;

import java.util.Date;
import java.util.List;

public class errandHome {
    String childName;
    String date;
    String errandContent;
    String destination;
    String start;
    String quest;


    public errandHome(String childName, String date, String errandContent, String destination, String start_name, String quest){
        this.childName = childName;
        this.date = date;
        this.errandContent = errandContent;
        this.destination = destination;
        this.start = start_name;
        this.quest = quest;
    }

    public errandHome(String childName, String date, String e_content, String target_name, String start_name) {
        this.childName = childName;
        this.date = date;
        this.errandContent = errandContent;
        this.destination = destination;
        this.start = start_name;
    }


    public String getChildName() { return childName; }
    public String getDate() { return date; }
    public String geterrandContent() { return errandContent; }
    public String getDestination() { return destination; }
    public String getStartName() { return start; }
    public String getQuest() { return quest; }


    public void setChildName(String Num) { this.childName = Num; }
    public void setDate(String Num) { this.date = Num; }
    public void setErrandContent(String Num) { this.errandContent = Num; }
    public void setdestination(String Num) { this.destination = Num; }
    public void setStart(String start) { this.start = start; }
    public void setQuest(String quest) {this.quest = quest; }
}
