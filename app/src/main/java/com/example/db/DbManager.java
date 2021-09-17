package com.example.db;


import android.util.Log;

import com.example.xingliansdk.Config;
import com.example.xingliansdk.utils.Utils;
import com.google.gson.Gson;
import com.orhanobut.hawk.Hawk;

import org.litepal.LitePal;

import java.text.DecimalFormat;
import java.util.List;
import java.util.Locale;

/**
 * Created by Admin
 * Date 2021/9/12
 */
public class DbManager {

    private static final String TAG = "DbManager";

    private static DbManager dbManager = null;

    private final DecimalFormat decimalFormat = new DecimalFormat("#.##");

    public static DbManager getDbManager(){
        synchronized (DbManager.class){
            if(dbManager == null)
                dbManager = new DbManager();
        }
        return dbManager;
    }



    //保存地图GPS运动数据
    public boolean saveAmapSportData(AmapSportBean amapSportBean){
        try {
            Log.e(TAG,"--去保存="+new Gson().toJson(amapSportBean));
            return amapSportBean.save();
        }catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }



    //查询地图GPS运动数据 根据用户id和日期查询
    public List<AmapSportBean> queryAmapSportData(String userId,String dayStr){
        try {
            String whereStr = "userId = ? and dayDate = ?";
            List<AmapSportBean> sportBeanList = LitePal.where(whereStr,userId,dayStr).find(AmapSportBean.class);
            return sportBeanList == null || sportBeanList.isEmpty() ? null : sportBeanList;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    //根据月份查询
    public List<AmapSportBean> queryAmapSportDataByMonth(String userId,String monthDay){
        try {
            String whereStr = "userId = ? and yearMonth = ?";
            List<AmapSportBean> sportBeanList = LitePal.where(whereStr,userId,monthDay).find(AmapSportBean.class);
            return sportBeanList == null || sportBeanList.isEmpty() ? null : sportBeanList;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }


    //根据用户ID查询，用于查询全部，后续根据后台接口拿数据
    public List<AmapSportBean> queryAmapSportDataByUserId(String userId){
        try {
            String whereStr = "userId = ?";
            List<AmapSportBean> sportBeanList = LitePal.where(whereStr,userId).find(AmapSportBean.class);
            return sportBeanList == null || sportBeanList.isEmpty() ? null : sportBeanList;
        }catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }

    /**
     * 保存总的距离
     * @param userId 用户ID
     */
    public void queryALlTotalDistance(String userId){
        try {
            List<AmapSportBean> sportBeanList =queryAmapSportDataByUserId(userId);
            if(sportBeanList == null ){
                Hawk.put(Config.database.WALK_DISTANCE_KEY,"0.0");
                Hawk.put(Config.database.RUN_DISTANCE_KEY,"0.0");
                Hawk.put(Config.database.BIKE_DISTANCE_KEY,"0.0");
                return;
            }
            double walkDistance = 0.0d;
            double runDistance = 0.0;
            double bikeDistance = 0.0;

            for(AmapSportBean amapSportBean : sportBeanList){
                int type = amapSportBean.getSportType();
                String currDistance = amapSportBean.getDistance();
                if(type == 0){
                    walkDistance = Utils.add(walkDistance,Double.parseDouble(currDistance.trim()));
                }
                if(type == 1){
                    runDistance = Utils.add(runDistance,Double.parseDouble(currDistance.trim()));
                }
                if(type == 2){
                    bikeDistance = Utils.add(bikeDistance,Double.parseDouble(currDistance.trim()));
                }
            }
            //直接保存公里
            Hawk.put(Config.database.WALK_DISTANCE_KEY,decimalFormat.format(Utils.divi(walkDistance,1000,2)));
            Hawk.put(Config.database.RUN_DISTANCE_KEY,decimalFormat.format(Utils.divi(runDistance,1000,2)));
            Hawk.put(Config.database.BIKE_DISTANCE_KEY,decimalFormat.format(Utils.divi(bikeDistance,1000,2)));
        }catch (Exception e){
            e.printStackTrace();
        }

    }


    //查询当天的累计里程
    public String queryCurrDayDistance(String userId,String day){
        double totalDistance = 0.0d;
        try {

            List<AmapSportBean> list = queryAmapSportData(userId,day);
            if(list == null) return totalDistance+"";
            for(AmapSportBean amapSportBean : list){
                String currDistance = amapSportBean.getDistance();
                totalDistance = Utils.add(totalDistance,Double.parseDouble(currDistance.trim()));
            }
            return decimalFormat.format(Utils.divi(totalDistance,1000,2));
        }catch (Exception e){
            e.printStackTrace();
            return totalDistance+"";
        }

    }

    /**
     * 根据类型查询数据
     * @param userId
     * @param sportType
     * @return
     */
    public List<AmapSportBean> queryByType(String userId,int sportType){
        if(sportType == -1){
            return queryAmapSportDataByUserId(userId);
        }else{
            String whereStr = "userId = ? and sportType = ?";
            List<AmapSportBean> sportBeanList = LitePal.where(whereStr,userId,sportType+"").find(AmapSportBean.class);
            return sportBeanList == null || sportBeanList.isEmpty() ? null : sportBeanList;
        }
    }


    //根据类型查询数据用于计算累加值
    public String queryTotalDistanceByType(String userId,int sportType){
        double totalDistance=0.0d;
        String whereStr = "userId = ? and sportType = ?";
        List<AmapSportBean> sportBeanList = LitePal.where(whereStr,userId,sportType+"").find(AmapSportBean.class);

        if(sportBeanList == null || sportBeanList.isEmpty()){
            return "0.0";
        }


        for(AmapSportBean amapSportBean : sportBeanList){
            String currDistance = amapSportBean.getDistance();
            totalDistance = Utils.add(totalDistance,Double.valueOf(currDistance));
        }
        return totalDistance+"";
    }


}
