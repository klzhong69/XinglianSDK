package com.example.db;

import java.util.List;

public class AmapRecordBean {

    private String monthStr;

    private List<AmapSportBean> list;

    private String distanceCount;

    private String caloriesCount;

    private int sportCount;


    private boolean isShow;


    public String getMonthStr() {
        return monthStr;
    }

    public void setMonthStr(String monthStr) {
        this.monthStr = monthStr;
    }

    public List<AmapSportBean> getList() {
        return list;
    }

    public void setList(List<AmapSportBean> list) {
        this.list = list;
    }

    public String getDistanceCount() {
        return distanceCount;
    }

    public void setDistanceCount(String distanceCount) {
        this.distanceCount = distanceCount;
    }

    public String getCaloriesCount() {
        return caloriesCount;
    }

    public void setCaloriesCount(String caloriesCount) {
        this.caloriesCount = caloriesCount;
    }

    public int getSportCount() {
        return sportCount;
    }

    public void setSportCount(int sportCount) {
        this.sportCount = sportCount;
    }

    public boolean isShow() {
        return isShow;
    }

    public void setShow(boolean show) {
        isShow = show;
    }
}
