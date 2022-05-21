package com.example.safe_map.common;

public class ProfileData {
    private static String userId;
    private static String nickName;
    private static String profile;
    private static String thumbnail;
    private static Integer childNum;
    private static Boolean mapFlag= false;

    public ProfileData(String userId, String nickName, String profile, String thumbnail) {
        this.userId = userId;
        this.nickName = nickName;
        this.profile = profile;
        this.thumbnail = thumbnail;
        this.childNum = null;
    }

    public static void setChildNum(Integer childNum) {
        ProfileData.childNum = childNum;
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


    public static Boolean getMapFlag() {
        return mapFlag;
    }

    public static void setMapFlag(Boolean flag) {
        mapFlag = flag;
    }

}
