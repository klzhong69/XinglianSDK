package com.example.xingliansdk.adapter

import android.annotation.SuppressLint
import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemChildClickListener
import com.chad.library.adapter.base.listener.OnItemClickListener
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.example.xingliansdk.R
import com.example.xingliansdk.network.api.dialView.RecommendDialBean
import com.example.xingliansdk.utils.ExcelUtil
import com.google.gson.Gson
import com.shon.connector.utils.TLog

class RecommendDialAdapter(data: MutableList<RecommendDialBean.ListDTO>) :
    BaseQuickAdapter<RecommendDialBean.ListDTO, BaseViewHolder>(
        R.layout.item_dial_classify,
        data
    )  {
     var listener:OnItemChildClickListener ?= null
    @SuppressLint("MissingPermission")
    override fun convert(helper: BaseViewHolder, item: RecommendDialBean.ListDTO?) {
        if (item == null) {
            return
        }
        val ryImg = helper.getView<RecyclerView>(R.id.ryImg)
        var tvName=helper.getView<TextView>(R.id.tvName)
        tvName.text = item.typeName
        ryImg.setHasFixedSize(true)
        if (ryImg.adapter == null) {
            val nestAdapter = MeDialImgAdapter(item.typeList,0)
            nestAdapter.setOnItemChildClickListener(listener)
            ryImg.layoutManager
            ryImg.adapter = nestAdapter
        }
    }


}