package com.example.xingliansdk.ui.dial

import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.listener.OnItemChildClickListener
import com.example.xingliansdk.R
import com.example.xingliansdk.adapter.RecommendDialAdapter
import com.example.xingliansdk.base.BaseFragment
import com.example.xingliansdk.bean.DeviceFirmwareBean
import com.example.xingliansdk.network.api.dialView.RecommendDialBean
import com.example.xingliansdk.network.api.dialView.RecommendDialViewModel
import com.example.xingliansdk.utils.JumpUtil
import com.google.gson.Gson
import com.orhanobut.hawk.Hawk
import com.shon.connector.utils.TLog

import kotlinx.android.synthetic.main.fragment_recommend_dial.*
import kotlinx.android.synthetic.main.item_dial_img_text.view.*

class RecommendDialFragment : BaseFragment<RecommendDialViewModel>(),View.OnClickListener {
    override fun layoutId()=R.layout.fragment_recommend_dial
    private lateinit var mRecommendDialAdapter:RecommendDialAdapter
    private lateinit var mList: MutableList<RecommendDialBean.ListDTO>
    override fun initView(savedInstanceState: Bundle?) {
      var bean=  Hawk.get("DeviceFirmwareBean", DeviceFirmwareBean())
        TLog.error("bean+" + Gson().toJson(bean))
        tvEdt.setOnClickListener(this)
        imgDial.setOnClickListener(this)
       var hashMap= HashMap<String, String>()
        hashMap["productNumber"] = bean.productNumber
        mViewModel.findDialImg(hashMap)
        setAdapter()
    }

    override fun onResume() {
        super.onResume()
    }

    private fun setAdapter()
    {
        mList= ArrayList()
        ryRecommendDial.layoutManager = LinearLayoutManager(
            activity,
            LinearLayoutManager.VERTICAL,
            false)
        mRecommendDialAdapter = RecommendDialAdapter(mList)
        ryRecommendDial.adapter = mRecommendDialAdapter

        val headerView: View = layoutInflater.inflate(R.layout.item_dial_img_text, null)
        headerView.layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            ViewGroup.LayoutParams.WRAP_CONTENT)
        headerView.imgDialCustomize.setOnClickListener(this)
        headerView.tvCustomize.setOnClickListener(this)
        mRecommendDialAdapter.addFooterView(headerView)

        mRecommendDialAdapter.listener=
            OnItemChildClickListener { adapter, view, position ->

                when(view.id)
                {
                    R.id.tvInstall->
                    {
                        //  view.dialProgressBar.setProgress(50)
                        TLog.error("点击++"+Gson().toJson(adapter.data[position]))
                        var bean:RecommendDialBean.ListDTO.TypeListDTO=adapter.data[position] as RecommendDialBean.ListDTO.TypeListDTO
                        JumpUtil.startDialDetailsActivity(activity,Gson().toJson(adapter.data[position]))



                    }

                }
            }
        mRecommendDialAdapter.addChildClickViewIds(R.id.tvInstall)
            mRecommendDialAdapter.setOnItemChildClickListener { adapter, view, position ->
            when(view.id)
            {
                R.id.tvInstall->
                {
                    //  view.dialProgressBar.setProgress(50)
                     TLog.error("点击++"+position)

                }

            }

        }

    }
    override fun createObserver() {
        super.createObserver()
        mViewModel.msg.observe(this)
        {
            TLog.error("msg++$it")
        }
        mViewModel.result.observe(this)
        {
            TLog.error("数据++" + Gson().toJson(it))
            mList.addAll(it.list)
//            mList.add(it)
            mRecommendDialAdapter.notifyDataSetChanged()
        }
    }

    override fun onClick(v: View) {
        when(v.id)
        {
            R.id.tvCustomize,
            R.id.imgDialCustomize->
            {
                JumpUtil.startCustomizeDialActivity(activity)
            }
            R.id.imgDial,
            R.id.tvEdt->
            {
                JumpUtil.startCustomizeDialActivity(activity)
            }
        }
    }
}