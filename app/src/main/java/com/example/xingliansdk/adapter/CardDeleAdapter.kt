package com.example.xingliansdk.adapter

import android.annotation.SuppressLint
import android.graphics.drawable.Drawable
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.module.DraggableModule
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.example.xingliansdk.R
import com.example.xingliansdk.bean.HomeCardBean
import com.example.xingliansdk.utils.ResUtil.getResources


class CardDeleAdapter(data: MutableList<HomeCardBean.DeleCardDTO>) :
    BaseQuickAdapter<HomeCardBean.DeleCardDTO, BaseViewHolder>(R.layout.item_card_edit, data),
    DraggableModule {
    var name = arrayOf(
        "", "",
        "", R.mipmap.icon_card_sleep, R.mipmap.icon_card_blood_oxygen,
        R.mipmap.icon_friends_bp_bo
    )

    override fun convert(helper: BaseViewHolder, item: HomeCardBean.DeleCardDTO?) {
        if (item == null) {
            return
        }

        var tvName = helper.getView<TextView>(R.id.tvName)
        val weather: Drawable = getResources().getDrawable(R.mipmap.icon_card_add)
        weather.setBounds(0, 0, weather.minimumWidth, weather.minimumWidth)
        tvName.setCompoundDrawables(weather, null, null, null)
        tvName.text = item.name
        var imgDrag = helper.getView<ImageView>(R.id.imgDrag)
        imgDrag.visibility = View.GONE
      //  val viewColor=helper.getView<View>(R.id.viewColor)
//        if(helper.adapterPosition>=data.size-1)
//            viewColor.visibility=View.GONE
//        else
//            viewColor.visibility=View.VISIBLE

    }

}