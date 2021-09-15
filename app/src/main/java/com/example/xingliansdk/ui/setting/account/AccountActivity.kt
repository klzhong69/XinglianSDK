package com.example.xingliansdk.ui.setting.account

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import com.example.xingliansdk.R
import com.example.xingliansdk.base.BaseActivity
import com.example.xingliansdk.base.viewmodel.BaseViewModel
import com.example.xingliansdk.ui.login.viewMode.UserViewModel
import com.example.xingliansdk.utils.AllGenJIDialog
import com.example.xingliansdk.utils.JumpUtil
import com.gyf.barlibrary.ImmersionBar
import kotlinx.android.synthetic.main.activity_account.*

class AccountActivity : BaseActivity<UserViewModel>(), View.OnClickListener {
    override fun layoutId() = R.layout.activity_account
    override fun initView(savedInstanceState: Bundle?) {
        ImmersionBar.with(this)
            .titleBar(titleBar)
            .init()
        settingUpPassword.setOnClickListener(this)
        settingChangePhone.setOnClickListener(this)
        tvSignOut.setOnClickListener(this)
        tvLogout.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.settingUpPassword -> {
                JumpUtil.startUpPasswordActivity(this)
            }
            R.id.settingChangePhone->{
                JumpUtil.startFindPhoneActivity(this)
            }
            R.id.tvSignOut -> {
                AllGenJIDialog.signOutDialog(supportFragmentManager, mViewModel, userInfo,this)
            }
            R.id.tvLogout->{
                JumpUtil.startLogOutActivity(this)
            }
        }
    }
}