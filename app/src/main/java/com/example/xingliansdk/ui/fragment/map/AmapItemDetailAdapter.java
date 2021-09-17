package com.example.xingliansdk.ui.fragment.map;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.db.AmapSportBean;
import com.example.xingliansdk.R;
import com.example.xingliansdk.ui.fragment.AmapHistorySportActivity;
import com.example.xingliansdk.utils.Utils;

import java.text.DecimalFormat;
import java.util.List;

/**
 * Created by Admin
 * Date 2021/9/17
 */
public class AmapItemDetailAdapter extends RecyclerView.Adapter<AmapItemDetailAdapter.ItemDetailViewHolder> {

    private List<AmapSportBean>  list;
    private Context mContext;

    private final DecimalFormat decimalFormat = new DecimalFormat("#.##");

    public AmapItemDetailAdapter(List<AmapSportBean> list, Context mContext) {
        this.list = list;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public ItemDetailViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_amap_sport_record_layout,parent,false);
        return new ItemDetailViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemDetailViewHolder holder, int position) {
        AmapSportBean amapSportBean = list.get(position);
        try {
            int sportType = amapSportBean.getSportType();
            holder.typeImg.setImageResource(mapSportTypeImg(sportType));
            holder.distanceTv.setText(decimalFormat.format(Float.valueOf(amapSportBean.getDistance())/1000)+"公里");
            holder.durationTv.setText(amapSportBean.getCurrentSportTime());
            holder.caloriesTv.setText(amapSportBean.getCalories()+"千卡");
            holder.currTimeTv.setText(Utils.formatCusTime(amapSportBean.getEndSportTime()));


            holder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    AmapSportBean amapSportBean = list.get(position);
                if(amapSportBean == null)
                    return;
                Intent intent = new Intent(mContext, AmapHistorySportActivity.class);
                intent.putExtra("sport_position",amapSportBean);
                mContext.startActivity(intent);
                }
            });
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ItemDetailViewHolder extends RecyclerView.ViewHolder{

        private ImageView typeImg;  //每天的item类型
        private TextView distanceTv;  //距离
        private TextView caloriesTv;  //卡路里
        private TextView durationTv;  //持续时间长
        private TextView currTimeTv;  //天

        public ItemDetailViewHolder(@NonNull View itemView) {
            super(itemView);

            typeImg = itemView.findViewById(R.id.itemAmapSportTypeImg);
            distanceTv = itemView.findViewById(R.id.itemAmapSportDistanceTv);
            caloriesTv = itemView.findViewById(R.id.itemAmapSportCaloriesTv);
            durationTv = itemView.findViewById(R.id.itemAmapSportDurationTv);
            currTimeTv = itemView.findViewById(R.id.itemAmapSportCurrDayTimeTv);
        }
    }

    private int mapSportTypeImg(int type){
        if(type == 1)  //步行
            return R.mipmap.icon_walk;
        if(type == 2)
            return R.mipmap.icon_run;
        if(type == 3)
            return R.mipmap.icon_ride;
        return R.mipmap.icon_walk;
    }
}
