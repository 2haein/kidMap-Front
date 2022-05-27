package com.safekid.safe_map.common;

import java.util.ArrayList;

public class ProfileData {
    private static String userId;
    private static String nickName;
    private static String profile;
    private static String thumbnail;
    private static Integer childNum;
    private static Boolean mapFlag= false;
    private static Boolean checkmapFlag = false;
    private static String errandChildId;
    private static ArrayList<Integer> safe_path_info = new ArrayList<>();

    public ProfileData(String userId, String nickName, String profile, String thumbnail) {
        this.userId = userId;
        this.nickName = nickName;
        this.profile = profile;
        this.thumbnail = thumbnail;
        this.childNum = null;
    }


    public static Integer getChildNum() {return childNum; }

    public static String getUserId() {
        return userId;
    }

    public static String getNickName() {
        return nickName;
    }

    public static String getProfile() {
        return profile;
    }

    public static String getThumbnail() {
        return thumbnail;
    }

    public static String getErrandChildId() { return errandChildId; }

    public static Boolean getcheckmapFlag() {
        return checkmapFlag;
    }

    public static Boolean getMapFlag() {
        return mapFlag;
    }

    public static ArrayList<Integer> getSafe_path_info(){ return safe_path_info;}





    public static void setUserid(String userid) {
        ProfileData.userId = userid;
    }

    public static void setChildNum(Integer childNum) {
        ProfileData.childNum = childNum;
    }

    public static void setMapFlag(Boolean flag) {
        mapFlag = flag;
    }

    public static void setcheckmapFlag(Boolean flag) {
        checkmapFlag = flag;
    }

    public static void setErrandChildId(String Id) { errandChildId = Id; }

    public static void setSafe_path_info(int i){ safe_path_info.add(i);}

}
