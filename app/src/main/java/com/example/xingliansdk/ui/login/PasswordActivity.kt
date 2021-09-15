package com.example.xingliansdk.ui.login

import android.os.Bundle
import android.text.TextUtils
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.view.View
import androidx.core.widget.addTextChangedListener
import com.example.xingliansdk.Config
import com.example.xingliansdk.R
import com.example.xingliansdk.base.BaseActivity
import com.example.xingliansdk.network.api.login.LoginBean
import com.example.xingliansdk.ui.login.viewMode.LoginViewModel
import com.example.xingliansdk.utils.HelpUtil
import com.example.xingliansdk.utils.JumpUtil
import com.example.xingliansdk.utils.ShowToast
import com.google.gson.Gson
import com.gyf.barlibrary.ImmersionBar
import com.orhanobut.hawk.Hawk
import com.shon.connector.utils.TLog
import kotlinx.android.synthetic.main.activity_password.*

class PasswordActivity : BaseActivity<LoginViewModel>(), View.OnClickListener {
    var imgPasswordSureStatus = false
    var imgPasswordStatus = false
    var type=1
    override fun layoutId() = R.layout.activity_password
    var phone = ""
    var code = ""
    var areaCode=""
    override fun initView(savedInstanceState: Bundle?) {
        ImmersionBar.with(this)
            .titleBar(imgBack)
            .init()
        imgPasswordSure.setOnClickListener(this)
        imgPassword.setOnClickListener(this)
        tvSure.setOnClickListener(this)
        imgBack.setOnClickListener(this)
        phone = intent.getStringExtra("phone").toString()
        code = intent.getStringExtra("code").toString()
        areaCode=intent.getStringExtra("areaCode").toString()
        type=intent.getIntExtra("type",1)
        edtPassword.addTextChangedListener {
            setSureBtnColor()
        }
        edtPasswordSure.addTextChangedListener {
            setSureBtnColor()
        }

    }

    override fun createObserver() {
        super.createObserver()
        mViewModel.result.observe(this){
            TLog.error("==${Gson().toJson(it)}")
            Hawk.put(Config.database.USER_INFO, it)
            JumpUtil.startDeviceInformationActivity(this, true)
        }
        mViewModel.resultPassword.observe(this){
            Hawk.put(Config.database.USER_INFO, it)
            if(it.user.sex.toDouble().toInt()==0)
            {
                JumpUtil.startDeviceInformationActivity(this, true)
            }
            else {
                JumpUtil.startMainHomeActivity(this)
                finish()
            }
        }
        mViewModel.msgUpdatePassword.observe(this){
            TLog.error("it=="+Gson().toJson(it))
            var loginBean=Gson().fromJson(Gson().toJson(it),LoginBean::class.java)
            TLog.error("loginBean=="+loginBean.user.sex)
            Hawk.put(Config.database.USER_INFO, loginBean)
            if(loginBean.user.sex.toDouble().toInt()==0)
            {
                JumpUtil.startDeviceInformationActivity(this, true)
            }
            else {
                JumpUtil.startMainHomeActivity(this)
            finish()
            }

        }
        mViewModel.msg.observe(this){
            ShowToast.showToastLong(it)
        }
    }
    private fun setSureBtnColor()
    {
        if(edtPassword.text.trim().length>=6&&
            edtPasswordSure.text.trim().length>=6)
        {
            tvSure.setTextColor(resources.getColor(R.color.white))
            tvSure.setBackgroundResource(R.drawable.bg_login_password)
        }
        else
        {
            tvSure.setTextColor(resources.getColor(R.color.color_login_code))
            tvSure.setBackgroundResource(R.drawable.bg_login_password_gray)
        }

    }
    override fun onClick(v: View) {
        when (v.id) {
            R.id.imgPassword -> {
                if (imgPasswordStatus) {
                    edtPassword.transformationMethod = PasswordTransformationMethod.getInstance()
                    imgPassword.setImageResource(R.mipmap.icon_non)
                } else {
                    edtPassword.transformationMethod = HideReturnsTransformationMethod.getInstance()
                    imgPassword.setImageResource(R.mipmap.icon_see)
                }
                imgPasswordStatus = !imgPasswordStatus
            }
            R.id.imgPasswordSure -> {
                if (imgPasswordSureStatus) {
                    edtPasswordSure.transformationMethod =
                        PasswordTransformationMethod.getInstance()
                    imgPasswordSure.setImageResource(R.mipmap.icon_non)
                } else {
                    edtPasswordSure.transformationMethod =
                        HideReturnsTransformationMethod.getInstance()
                    imgPasswordSure.setImageResource(R.mipmap.icon_see)
                }
                imgPasswordSureStatus = !imgPasswordSureStatus
            }
            R.id.tvSure -> {
                if (!HelpUtil.passwordStatus(edtPassword.text) || edtPassword.text.trim().length < 6) {
                    ShowToast.showToastLong(resources.getString(R.string.edt_password_number_text))
                    return
                } else if (!TextUtils.equals(edtPassword.text,edtPasswordSure.text)) {
                    ShowToast.showToastLong("两次密码不一致,请重新输入")
                    return
                }
//                TLog.error("=="+Gson().toJson(Hawk.get(Config.database.USER_INFO)))
                if(type==2) {
                   var value: HashMap<String,String> = HashMap()
                    value["verifyCode"]=code
                    value["phone"]=phone
                    value["areaCode"]=areaCode
                    value["password"]=edtPassword.text.toString()
                    mViewModel.updatePassword(value)
                }
                else
            mViewModel.setPassword(phone,edtPassword.text.toString(),areaCode,code)
            }
            R.id.imgBack -> {
                finish()
            }
        }
    }
}