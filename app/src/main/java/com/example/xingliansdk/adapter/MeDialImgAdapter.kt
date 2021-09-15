package com.example.xingliansdk.adapter

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.example.xingliansdk.R
import com.example.xingliansdk.network.api.dialView.RecommendDialBean
import com.example.xingliansdk.network.api.meView.MeDialImgBean
import com.example.xingliansdk.utils.ImgUtil
import com.example.xingliansdk.view.DialProgressBar

class MeDialImgAdapter(data: MutableList<RecommendDialBean.ListDTO.TypeListDTO>, type: Int) :
    BaseQuickAdapter<RecommendDialBean.ListDTO.TypeListDTO, BaseViewHolder>(
        R.layout.item_dial_img,
        data
    ) {
    var type = 0

    init {
        addChildClickViewIds(R.id.tvInstall)
        this.type = type
    }

    override fun convert(helper: BaseViewHolder, item: RecommendDialBean.ListDTO.TypeListDTO?) {
        if (item == null) {
            return
        }
        val img = helper.getView<ImageView>(R.id.imgDial)
        val tvInstall = helper.getView<TextView>(R.id.tvInstall)
        val tvName = helper.getView<TextView>(R.id.tvName)
        when (type) {
            1 -> {
                tvName.visibility = View.GONE
                tvInstall.visibility = View.GONE
            }
            2 -> {
                tvName.visibility = View.GONE
                tvInstall.visibility = View.VISIBLE
            }
            else -> {
                tvName.visibility = View.VISIBLE
                tvInstall.visibility = View.VISIBLE
            }
        }
//        val dialProgressBar=helper.getView<DialProgressBar>(R.id.dialProgressBar)
//        if(item.progress.isNotEmpty())
//        dialProgressBar.setProgress(item.progress.toInt())
//        if(item.install.isNotEmpty())
//        tvInstall.text=item.install
        //  ImgUtil.loadMeImgDialCircle(img,R.mipmap.icon_my_qrc)
        ImgUtil.loadMeImgDialCircle(img, item.image)
    }

}