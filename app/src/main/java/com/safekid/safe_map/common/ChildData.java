package com.safekid.safe_map.common;

public class ChildData {
    private static String childId;
    private static Boolean checkmapFlag = false;

    public ChildData(String childId) {
        this.childId = childId;
    }

    public static void setChildId(String childId) {
        ChildData.childId = childId;
    }
    public static void setcheckmapFlag(Boolean flag) {
        checkmapFlag = flag;
    }

    public static String getChildId() {return childId; }
    public static Boolean getcheckmapFlag() {
        return checkmapFlag;
    }

}
