package com.example.xingliansdk.ui.fragment.map;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.db.AmapSportBean;
import com.example.xingliansdk.R;
import com.example.xingliansdk.utils.Utils;

import java.text.DecimalFormat;
import java.util.List;

/**
 * Created by Admin
 * Date 2021/9/12
 */
public class AmapRecordAdapter extends RecyclerView.Adapter<AmapRecordAdapter.AmapRecordViewHolder> {


    private List<AmapSportBean> sportBeanList;
    private Context mContext;

    private final DecimalFormat decimalFormat = new DecimalFormat("#.##");

    private AmapOnItemClickListener amapOnItemClickListener;

    public void setAmapOnItemClickListener(AmapOnItemClickListener amapOnItemClickListener) {
        this.amapOnItemClickListener = amapOnItemClickListener;
    }

    public AmapRecordAdapter(List<AmapSportBean> sportBeanList, Context mContext) {
        this.sportBeanList = sportBeanList;
        this.mContext = mContext;
    }

    @NonNull
    @Override
    public AmapRecordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.item_amap_sport_record_layout,parent,false);
        return new AmapRecordViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AmapRecordViewHolder holder, int position) {
        AmapSportBean amapSportBean = sportBeanList.get(position);
        try {
            int sportType = amapSportBean.getSportType();
            holder.typeImg.setImageResource(mapSportTypeImg(sportType));
            holder.distanceTv.setText(decimalFormat.format(Float.valueOf(amapSportBean.getDistance())/1000)+"公里");
            holder.durationTv.setText(amapSportBean.getCurrentSportTime());
            holder.caloriesTv.setText(amapSportBean.getCalories()+"千卡");
            holder.currTimeTv.setText(Utils.formatCusTime(amapSportBean.getEndSportTime()));
        }catch (Exception e){
            e.printStackTrace();
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int postion = holder.getLayoutPosition();
                if(amapOnItemClickListener != null)
                    amapOnItemClickListener.onAmapItemClick(postion);
            }
        });
    }



    @Override
    public int getItemCount() {
        return sportBeanList.size();
    }


    class AmapRecordViewHolder extends RecyclerView.ViewHolder{

        private ImageView typeImg;
        private TextView distanceTv;
        private TextView caloriesTv;
        private TextView durationTv;
        private TextView currTimeTv;

        public AmapRecordViewHolder(@NonNull View itemView) {
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


    public interface AmapOnItemClickListener{
        void onAmapItemClick(int position);
    }

}
