package com.example.xingliansdk.ui.problemsFeedback

import android.os.Bundle
import com.example.xingliansdk.R
import com.example.xingliansdk.base.BaseActivity
import com.example.xingliansdk.base.viewmodel.BaseViewModel
import com.gyf.barlibrary.ImmersionBar
import kotlinx.android.synthetic.main.activity_problems_feedback.*

class ProblemsFeedbackActivity : BaseActivity<BaseViewModel>() {
    override fun layoutId()=R.layout.activity_problems_feedback

    override fun initView(savedInstanceState: Bundle?) {
        ImmersionBar.with(this)
            .titleBar(titleBar)
            .init()
    }
}