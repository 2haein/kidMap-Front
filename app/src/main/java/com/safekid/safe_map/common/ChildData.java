package com.safekid.safe_map.common;

public class ChildData {
    private static String childId;
    private static Boolean checkmapFlag = false;
    private static Boolean checkSMS = false;

    public ChildData(String childId) {
        this.childId = childId;
    }

    public static void setChildId(String childId) {
        ChildData.childId = childId;
    }
    public static void setcheckmapFlag(Boolean flag) {
        checkmapFlag = flag;
    }
    public static void setCheckSMS(Boolean flag) { checkSMS = true; }

    public static String getChildId() {return childId; }
    public static Boolean getcheckmapFlag() {
        return checkmapFlag;
    }
    public static Boolean getcheckSMS() {return checkSMS; }

}
