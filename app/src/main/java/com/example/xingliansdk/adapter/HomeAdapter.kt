package com.example.xingliansdk.adapter

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.example.xingliansdk.R
import com.example.xingliansdk.bean.HomeCardBean
import com.example.xingliansdk.utils.HelpUtil
import com.example.xingliansdk.view.DateUtil
import com.shon.connector.utils.TLog

class HomeAdapter(data: MutableList<HomeCardBean.AddCardDTO>) :
    BaseQuickAdapter<HomeCardBean.AddCardDTO, BaseViewHolder>(R.layout.item_home, data) {
    var imgList = intArrayOf(
        R.mipmap.icon_home_sport,
        R.mipmap.icon_home_heart_rate,
        R.mipmap.icon_home_sleep,
        R.mipmap.icon_home_pressure,
        R.mipmap.icon_home_blodoxygen,
        R.mipmap.icon_home_bloodpressure
        , R.mipmap.icon_home_temperature
        , R.mipmap.icon_home_weight
    )

    override fun convert(helper: BaseViewHolder, item: HomeCardBean.AddCardDTO?) {
        if (item == null) {
            return
        }
        val img = helper.getView<ImageView>(R.id.imgIcon)
        img.setImageResource(imgList[item.type])
        //  helper.setImageResource(R.id.imgIcon, item.img)
        // helper.setText(R.id.tvItemStatusTitle,"记录")
        helper.setText(R.id.tvItemStatusTitle, item.name)
        val tvItemStatusData = helper.getView<TextView>(R.id.tvItemStatusData)
        val tvItemStatusSubTitle = helper.getView<TextView>(R.id.tvItemStatusSubTitle)
        tvItemStatusSubTitle.text = item.subTitle
//        TLog.error("item.dayContent=="+item.dayContent)
        if (!item.dayContent.isNullOrEmpty()) {
            tvItemStatusData.visibility = View.VISIBLE
            tvItemStatusSubTitle.text =DateUtil.getDate(DateUtil.MM_AND_DD,item.time*1000)
            if (item.type == 2) {
                val time = item.dayContent
                tvItemStatusData.text = HelpUtil.getSpan(
                    time.substring(0, 2),
                    time.substring(2, 4),
                    time.substring(4, 6),
                    time.substring(6, 8),
                    R.color.sub_text_color,
                    12
                )
                img.setImageResource(R.mipmap.icon_home_sleep_data)
            } else {
                if( item.dayContentString.isNullOrEmpty())
                    item.dayContentString=""
                tvItemStatusData.text =
                    HelpUtil.getSpan(item.dayContent, item.dayContentString)
                when (item.type) {
                    0 -> {
                        img.setImageResource(R.mipmap.icon_home_sport_data)
                    }
                    1 -> {
                        img.setImageResource(R.mipmap.icon_home_heart_rate_data)
                    }
                    3 -> {
                        img.setImageResource(R.mipmap.icon_home_pressure_data)
                    }
                    4 -> {
                        img.setImageResource(R.mipmap.icon_home_blodoxygen_data)
                    }
                    5 -> {
                        img.setImageResource(R.mipmap.icon_home_bloodpressure_data)
                    }
                    6 -> {
                        img.setImageResource(R.mipmap.icon_home_temperature_data)
                    }
                    7 -> {
                        img.setImageResource(R.mipmap.icon_home_weight_data)
                    }
                }
            }
        } else
            tvItemStatusData.visibility = View.GONE

    }

}