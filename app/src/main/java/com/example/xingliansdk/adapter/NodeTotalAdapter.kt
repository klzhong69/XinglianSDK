package com.example.xingliansdk.adapter

import android.annotation.SuppressLint
import android.widget.TextView
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.entity.node.BaseNode
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.example.xingliansdk.Config
import com.example.xingliansdk.R
import com.example.xingliansdk.bean.node.ItemExerciseRecordNode
import com.example.xingliansdk.utils.HelpUtil
import com.shon.connector.utils.TLog
import com.google.gson.Gson

/**
 * 运动累计总数类
 */
class NodeTotalAdapter(data: MutableList<BaseNode>) :
    BaseQuickAdapter<BaseNode, BaseViewHolder>(
        R.layout.item_exrcise_record_total,
        data
    ) {

    @SuppressLint("MissingPermission")
    override fun convert(helper: BaseViewHolder, item: BaseNode?) {
        if (item == null) {
            return
        }
        val totalNode: ItemExerciseRecordNode = item as ItemExerciseRecordNode
        var tvTotalDistanceName=helper.getView<TextView>(R.id.tvTotalDistanceName)
        if (totalNode.type== Config.exercise.BICYCLE)
            tvTotalDistanceName.text="自行车(公里)"
        if (totalNode.type== Config.exercise.RUN)
            tvTotalDistanceName.text="跑步(公里)"
        if (totalNode.type== Config.exercise.WALK)
            tvTotalDistanceName.text="步行(公里)"
        if (totalNode.type== Config.exercise.MOUNTAIN_CLIMBING)
            tvTotalDistanceName.text="爬山(公里)"
        helper.setText(R.id.tvTotalDistance,HelpUtil.setNumber(totalNode.distance.toDouble(),2).toString())
    }
}