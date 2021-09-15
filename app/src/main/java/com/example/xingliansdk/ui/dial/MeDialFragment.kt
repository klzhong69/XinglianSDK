package com.example.xingliansdk.ui.dial

import android.os.Bundle
import android.view.View
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.xingliansdk.R
import com.example.xingliansdk.adapter.CustomDialImgAdapter
import com.example.xingliansdk.adapter.MeDialImgAdapter
import com.example.xingliansdk.adapter.RecommendDialAdapter
import com.example.xingliansdk.base.BaseFragment
import com.example.xingliansdk.base.viewmodel.BaseViewModel
import com.example.xingliansdk.bean.DeviceFirmwareBean
import com.example.xingliansdk.bean.room.AppDataBase
import com.example.xingliansdk.bean.room.CustomizeDialBean
import com.example.xingliansdk.bean.room.CustomizeDialDao
import com.example.xingliansdk.network.api.dialView.RecommendDialBean
import com.example.xingliansdk.network.api.dialView.RecommendDialViewModel
import com.google.gson.Gson
import com.orhanobut.hawk.Hawk
import com.shon.connector.utils.TLog
import kotlinx.android.synthetic.main.fragment_me_dial.*
import kotlinx.android.synthetic.main.fragment_recommend_dial.*

class MeDialFragment : BaseFragment<RecommendDialViewModel>(), View.OnClickListener {
    override fun layoutId() = R.layout.fragment_me_dial
    private lateinit var mList: MutableList<RecommendDialBean.ListDTO.TypeListDTO>
    private lateinit var customDialList: MutableList<CustomizeDialBean>
    private lateinit var meDialImgAdapter: MeDialImgAdapter
    lateinit var customDialImgAdapter: CustomDialImgAdapter
    lateinit var sDao: CustomizeDialDao
    override fun initView(savedInstanceState: Bundle?) {
        imgDownload.setOnClickListener(this)
        imgCustomize.setOnClickListener(this)
        imgLocal.setOnClickListener(this)
        var bean = Hawk.get("DeviceFirmwareBean", DeviceFirmwareBean())
        sDao = AppDataBase.instance.getCustomizeDialDao()
        var hashMap = HashMap<String, String>()
        hashMap["type"] = "0"
        hashMap["productNumber"] = bean.productNumber
        mViewModel.findDialImg(hashMap)
        dialInit()
    }

    private fun dialInit() {
        mList = ArrayList()
        ryLocal.layoutManager = GridLayoutManager(activity, 3)
        meDialImgAdapter = MeDialImgAdapter(mList, 2)
        ryLocal.adapter = meDialImgAdapter

        customDialList = if(sDao.getAllCustomizeDialList().isNullOrEmpty()||sDao.getAllCustomizeDialList().size<=0)
            ArrayList()
        else
            sDao.getAllCustomizeDialList()
        ryCustomize.layoutManager = GridLayoutManager(activity, 3)
        customDialImgAdapter = CustomDialImgAdapter(customDialList)
        ryCustomize.adapter = customDialImgAdapter
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.imgDownload -> {
                if(ryDownload.visibility==View.VISIBLE)
                {
                    ryDownload.visibility=View.GONE
                    imgDownload.rotation=90f
                }
                else
                {
                    ryDownload.visibility=View.VISIBLE
                    imgDownload.rotation=270f
                }

            }
            R.id.imgCustomize -> {
                if(ryCustomize.visibility==View.VISIBLE)
                {
                    ryCustomize.visibility=View.GONE
                    imgCustomize.rotation=90f
                }
                else
                {
                    ryCustomize.visibility=View.VISIBLE
                    imgCustomize.rotation=270f
                }
            }
            R.id.imgLocal -> {
                if(ryLocal.visibility==View.VISIBLE)
                {
                    ryLocal.visibility=View.GONE
                    imgLocal.rotation=90f
                }
                else
                {
                    ryLocal.visibility=View.VISIBLE
                    imgLocal.rotation=270f
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
            if(it.list[0].type==0) {
                mList.addAll(it.list[0].typeList)
                meDialImgAdapter.notifyDataSetChanged()
            }
            else if(it.list[0].type==1001)
            {
                mList.addAll(it.list[0].typeList)
                meDialImgAdapter.notifyDataSetChanged()
            }
        }
    }
}