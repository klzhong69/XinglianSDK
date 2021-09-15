package com.example.xingliansdk.adapter

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.example.xingliansdk.R
import com.example.xingliansdk.bean.room.CustomizeDialBean
import com.example.xingliansdk.network.api.dialView.RecommendDialBean
import com.example.xingliansdk.network.api.meView.MeDialImgBean
import com.example.xingliansdk.utils.ImgUtil
import com.example.xingliansdk.view.DialProgressBar

class CustomDialImgAdapter(data: MutableList<CustomizeDialBean>) :
    BaseQuickAdapter<CustomizeDialBean, BaseViewHolder>(
        R.layout.item_dial_img,
        data
    ) {

    init {
        addChildClickViewIds(R.id.tvInstall)
    }

    override fun convert(helper: BaseViewHolder, item: CustomizeDialBean?) {
        if (item == null) {
            return
        }
        val img = helper.getView<ImageView>(R.id.imgDial)
        val tvInstall = helper.getView<TextView>(R.id.tvInstall)
        val tvName = helper.getView<TextView>(R.id.tvName)
        tvName.visibility = View.GONE
        tvInstall.visibility = View.VISIBLE
//        val dialProgressBar=helper.getView<DialProgressBar>(R.id.dialProgressBar)
//        if(item.progress.isNotEmpty())
//        dialProgressBar.setProgress(item.progress.toInt())
//        if(item.install.isNotEmpty())
//        tvInstall.text=item.install
        //  ImgUtil.loadMeImgDialCircle(img,R.mipmap.icon_my_qrc)
        ImgUtil.loadMeImgDialCircle(img, item.imgPath)
    }

}