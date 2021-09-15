package com.example.xingliansdk.ui.fragment.home

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.example.xingliansdk.R
import com.example.xingliansdk.base.BaseFragment
import com.example.xingliansdk.base.viewmodel.BaseViewModel
import com.google.gson.Gson

class MainMotionFragment : BaseFragment<BaseViewModel>() {


    override fun layoutId(): Int =R.layout.activity_main_motion_fragment

    override fun initView(savedInstanceState: Bundle?) {
    }

    fun getInstance( ): MainMotionFragment {
        val mMainMotionFragment =
            MainMotionFragment()
        val bundle = Bundle()
        //TLog.error("title==${Gson().toJson(mChildrenBean)}")
     //   bundle.putString("LookExploreChildrenFragment", mChildrenBean.type)
        mMainMotionFragment.arguments = bundle
          //    mMainMotionFragment.type = mChildrenBean.type

        return mMainMotionFragment
    }
}