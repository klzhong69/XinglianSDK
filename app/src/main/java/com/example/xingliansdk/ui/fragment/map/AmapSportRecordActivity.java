package com.example.xingliansdk.ui.fragment.map;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.db.AmapRecordBean;
import com.example.db.AmapSportBean;
import com.example.db.DbManager;
import com.example.db.TestDB;
import com.example.xingliansdk.Config;
import com.example.xingliansdk.R;
import com.example.xingliansdk.base.BaseActivity;
import com.example.xingliansdk.network.api.login.LoginBean;
import com.example.xingliansdk.ui.fragment.AmapHistorySportActivity;
import com.example.xingliansdk.utils.Utils;
import com.example.xingliansdk.widget.TitleBarLayout;
import com.google.gson.Gson;
import com.orhanobut.hawk.Hawk;

import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 地图运动记录
 * Created by Admin
 * Date 2021/9/12
 */
public class AmapSportRecordActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "AmapSportRecordActivity";

    private RecyclerView recordRecyclerView;
    private List<AmapSportBean> list ;
    private AmapRecordAdapter amapRecordAdapter;

    private TitleBarLayout titleBarLayout;

    private TextView emptyTv;

    private TextView dateTv;
    private ImageView leftImg,rightImg;

    private String currDay = Utils.getCurrentDate();


    private List<AmapRecordBean> resultList = new ArrayList<>();

    @Override
    protected void onCreate(@androidx.annotation.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_amap_sport_record_layout);

        initViews();

        querySaveSport(currDay);


    }

    private void initViews() {
        emptyTv = findViewById(R.id.emptyTv);

        titleBarLayout = findViewById(R.id.amapRecordTb);
        recordRecyclerView = findViewById(R.id.amapRecordRecyclerView);
        dateTv = findViewById(R.id.itemDateTitleTv);
        leftImg = findViewById(R.id.itemDateLeftImg);
        rightImg = findViewById(R.id.itemDateRightImg);

        leftImg.setOnClickListener(this);
        rightImg.setOnClickListener(this);


        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        linearLayoutManager.setOrientation(LinearLayoutManager.VERTICAL);
        recordRecyclerView.setLayoutManager(linearLayoutManager);

        list = new ArrayList<>();
        amapRecordAdapter = new AmapRecordAdapter(resultList,this);
        recordRecyclerView.setAdapter(amapRecordAdapter);

        titleBarLayout.setTitleBarListener(new TitleBarLayout.TitleBarListener() {
            @Override
            public void onBackClick() {
                finish();
            }

            @Override
            public void onActionImageClick() {

            }

            @Override
            public void onActionClick() {

            }
        });


//        amapRecordAdapter.setAmapOnItemClickListener(new AmapRecordAdapter.AmapOnItemClickListener() {
//            @Override
//            public void onAmapItemClick(int position) {
//                AmapSportBean amapSportBean = list.get(position);
//                if(amapSportBean == null)
//                    return;
//                Intent intent = new Intent(AmapSportRecordActivity.this, AmapHistorySportActivity.class);
//                intent.putExtra("sport_position",amapSportBean);
//                startActivity(intent);
//            }
//        });

    }




    private void querySaveSport(String dayStr){
        dateTv.setText(dayStr);
        list.clear();
        try {
            LoginBean loginBean = Hawk.get(Config.database.USER_INFO);
            if(loginBean == null){
                showEmpty();
                return;
            }

            String userId = loginBean.getUser().getUserId();
            if(userId == null){
                showEmpty();
                return;
            }
            List<AmapSportBean> sportBeanList = DbManager.getDbManager().queryAmapSportDataByMonth(userId, "2021-09");
            Log.e(TAG,"-------记录查询="+new Gson().toJson(sportBeanList));
            if(sportBeanList == null){
                showEmpty();
                return;
            }

            analyseData(sportBeanList);

//            emptyTv.setVisibility(View.GONE);
//            list.clear();
//            list.addAll(sportBeanList);
//            amapRecordAdapter.notifyDataSetChanged();
        }catch (Exception e){
            e.printStackTrace();
        }

    }




    private void analyseData(List<AmapSportBean> sportLt){
        HashMap<String,Object> monthMap = new HashMap<>();
        resultList.clear();

        double countDistance = 0;
        double countCalories = 0;

        List<AmapSportBean> itemList = new ArrayList<>();
        String tmpMonth;

        Map<String,List<AmapSportBean>> rM = new HashMap<>();
        for(AmapSportBean amapSportBean : sportLt){

            String currMonth = amapSportBean.getYearMonth();

            itemList.add(amapSportBean);

            rM.put(currMonth,itemList);

//            AmapRecordBean amapRecordBean = new AmapRecordBean();

            //月份
            String monthStr = amapSportBean.getYearMonth();
//            String currDistance = amapSportBean.getDistance();
//            String currCalories = amapSportBean.getCalories();
//
//            amapRecordBean.setMonthStr(monthStr);

            monthMap.put(monthStr,"1");

//
//            if(monthMap.containsKey(monthStr)){
//                countCalories += Utils.add(countCalories,Double.parseDouble(currCalories));
//                countDistance += Utils.add(countDistance,Double.parseDouble(currDistance));
//                amapRecordBean.setDistanceCount(countDistance+"");
//                amapRecordBean.setCaloriesCount(countCalories+"");
//                itemList.add(amapSportBean);
//            }else{
//                itemList.clear();
//                monthMap.put(monthStr,amapSportBean);
//                countDistance += Utils.add(countDistance,Double.parseDouble(currCalories));
//                countCalories += Utils.add(countCalories,Double.parseDouble(currCalories));
//                amapRecordBean.setDistanceCount(countDistance+"");
//                amapRecordBean.setCaloriesCount(countCalories+"");
//                itemList.add(amapSportBean);
//
//            }
//
//            amapRecordBean.setList(itemList);
//            resultList.add(amapRecordBean);

        }

//        amapRecordAdapter.notifyDataSetChanged();

        List<String> dbList = new ArrayList<>();

        for(Map.Entry<String,Object> m : monthMap.entrySet()){
            dbList.add(m.getKey());
        }


        for(Map.Entry<String,List<AmapSportBean>> mm : rM.entrySet()){
            String keyMonth = mm.getKey();
            List<AmapSportBean> tmAL = mm.getValue();


            for(AmapSportBean amapSportBean : tmAL){
                String currDistance = amapSportBean.getDistance();
                String currCalories = amapSportBean.getCalories();
                countCalories  = Utils.add(countCalories,Double.parseDouble(currCalories));
                countDistance = Utils.add(countDistance,Double.parseDouble(currDistance));
            }

            AmapRecordBean amapRecordBean = new AmapRecordBean();
            amapRecordBean.setMonthStr(keyMonth);
            amapRecordBean.setShow(false);
            amapRecordBean.setDistanceCount(Utils.divi(countDistance,1000d,2)+"");
            amapRecordBean.setCaloriesCount(countCalories+"");
            amapRecordBean.setList(tmAL);
            resultList.add(amapRecordBean);
        }


        amapRecordAdapter.notifyDataSetChanged();
        Log.e(TAG,"------转换="+new Gson().toJson(dbList));



    }




    private void showEmpty(){
        emptyTv.setVisibility(View.VISIBLE);
        amapRecordAdapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.itemDateLeftImg){
            changeDayData(true);
        }

        if(view.getId() == R.id.itemDateRightImg){
            changeDayData(false);
        }
    }

    /**
     * 根据日期切换数据
     */
    private void changeDayData(boolean left) {

        String date = Utils.obtainAroundDate(currDay, left);
        if (date.equals(currDay) || date.isEmpty()) {
            return;// 空数据,或者大于今天的数据就别切了
        }
        currDay = date;
        querySaveSport(currDay);
    }
}
