package com.example.xingliansdk.ui.fragment.map;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
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
 * 地图运动记录,现在只查询本地数据库，后续上传后台后根据接口拿数据
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

//    private ImageView leftImg,rightImg;

    private String currDay = Utils.getCurrentDate();

    //返回
    private ImageView recordTitleBackImg;
    //标题
    private TextView recordSportTitleTv;


    private List<AmapRecordBean> resultList = new ArrayList<>();

    String[] typeStr = new String[]{"所有运动","步行","跑步","骑行"};
    //运动类型
    private int sportType;

    //类型弹窗
    private AlertDialog.Builder alert;

    @Override
    protected void onCreate(@androidx.annotation.Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_amap_sport_record_layout);

        initViews();

        sportType = getIntent().getIntExtra("sportType",-1);


        recordSportTitleTv.setText(typeStr[sportType+1]);

        querySaveSport(sportType);


    }

    private void initViews() {

        recordTitleBackImg = findViewById(R.id.recordTitleBackImg);
        recordSportTitleTv = findViewById(R.id.recordSportTitleTv);

        emptyTv = findViewById(R.id.emptyTv);

        titleBarLayout = findViewById(R.id.amapRecordTb);
        recordRecyclerView = findViewById(R.id.amapRecordRecyclerView);

        recordTitleBackImg.setOnClickListener(this);
        recordSportTitleTv.setOnClickListener(this);

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




    private void querySaveSport(int  type){
        resultList.clear();
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
            List<AmapSportBean> sportBeanList = DbManager.getDbManager().queryByType(userId,type);
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
        try {
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
                amapRecordBean.setSportCount(tmAL.size());
                resultList.add(amapRecordBean);
            }


            amapRecordAdapter.notifyDataSetChanged();
            Log.e(TAG,"------转换="+new Gson().toJson(dbList));
        }catch (Exception e){
            e.printStackTrace();
        }

    }




    private void showEmpty(){
        emptyTv.setVisibility(View.VISIBLE);
        amapRecordAdapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.recordTitleBackImg){
            finish();
        }
        if(view.getId() == R.id.recordSportTitleTv){    //标题点击
            alertDialogTitle();
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
       // querySaveSport(currDay);
    }



    private void alertDialogTitle(){

        alert = new AlertDialog.Builder(this)
                .setItems(typeStr, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                        recordSportTitleTv.setText(typeStr[i]);
                        sportType = i-1;
                        querySaveSport(sportType);
                    }
                }).setNegativeButton("取消", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });
        alert.create().show();
    }
}
