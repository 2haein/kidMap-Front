package com.example.safe_map.common;

public class ChildData {
    private static String childId;

    public ChildData(String childId) {
        this.childId = childId;
    }

    public static void setChildId(String childId) {
        ChildData.childId = childId;
    }

    public static String getChildId() {return childId; }

}
