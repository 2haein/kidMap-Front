package com.example.safe_map.Login;

import java.util.UUID;

public class ChildnumItem {
    String childNum;
    String UUID;

    public ChildnumItem(String childnum){
        this.childNum = childnum;
    }
    public ChildnumItem(String childnum, String UUID){
        this.childNum = childnum;
        this.UUID = UUID;
    }

    public String getChildNum() { return childNum; }
    public String getUUID() { return UUID; }
    public void setChildNum(String Num) { this.childNum = Num; }
}
