package com.example.xingliansdk.network.api.meView

import androidx.lifecycle.MutableLiveData
import com.example.xingliansdk.base.viewmodel.BaseViewModel
import com.example.xingliansdk.network.api.dialView.RecommendDialBean
import com.example.xingliansdk.network.api.dialView.RecommendDialViewApi
import com.example.xingliansdk.network.api.meView.MeViewApi.Companion.mMeViewApi
import com.example.xingliansdk.network.requestCustom
import com.example.xingliansdk.utils.ShowToast
import com.shon.connector.utils.TLog


class MeViewModel : BaseViewModel() {
    val result: MutableLiveData<Any> = MutableLiveData()
    val msg: MutableLiveData<String> = MutableLiveData()
    fun getDialImg( ) {
        requestCustom({
            mMeViewApi.getDialImg( )
        }, {
            result.postValue(it)
        }, { code, message ->
            message?.let {
                TLog.error("it=" + it)
                ShowToast.showToastLong(it)
            }
        }
        )
    }

    val resultDialImg: MutableLiveData<RecommendDialBean> = MutableLiveData()
    val msgDialImg: MutableLiveData<String> = MutableLiveData()
    fun findDialImg(hashMap: HashMap<String,String>) {
        requestCustom({
            RecommendDialViewApi.mRecommendDialViewApi.findDial(hashMap)
        }, {
            resultDialImg.postValue(it)
        }, { code, message ->
            message?.let {
                msgDialImg.postValue(it)
                TLog.error("it=" + it)
                ShowToast.showToastLong(it)
            }
        }
        )
    }
}