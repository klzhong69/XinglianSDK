package com.example.db;


import android.util.Log;
import com.google.gson.Gson;
import org.litepal.LitePal;
import java.util.List;

/**
 * Created by Admin
 * Date 2021/9/12
 */
public class DbManager {

    private static final String TAG = "DbManager";

    private static DbManager dbManager = null;

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


}
