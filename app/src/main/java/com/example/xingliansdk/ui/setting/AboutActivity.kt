package com.example.xingliansdk.ui.setting

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.example.xingliansdk.R
import com.example.xingliansdk.XingLianApplication
import com.example.xingliansdk.base.BaseActivity
import com.example.xingliansdk.base.viewmodel.BaseViewModel
import com.example.xingliansdk.utils.AppUtils
import com.example.xingliansdk.utils.HelpUtil
import com.example.xingliansdk.utils.JumpUtil
import com.example.xingliansdk.utils.ShowToast
import com.example.xingliansdk.viewmodel.AppStartViewModel
import com.gyf.barlibrary.ImmersionBar
import com.ly.genjidialog.extensions.convertListenerFun
import com.ly.genjidialog.extensions.newGenjiDialog
import com.shon.connector.utils.TLog
import kotlinx.android.synthetic.main.activity_about.*

class AboutActivity : BaseActivity<AppStartViewModel>(),View.OnClickListener {
    override fun layoutId()=R.layout.activity_about

    override fun initView(savedInstanceState: Bundle?) {
        ImmersionBar.with(this)
            .titleBar(titleBar)
            .init()
        tvUser.setOnClickListener(this)
        tvPrivacy.setOnClickListener(this)
        settUpdate.setOnClickListener(this)
        tvVersion.text= AppUtils.getVersionName(this)
    }
    var upDataUrl: String? = null
    var forceUpdate=false
    override fun createObserver() {
        super.createObserver()
        mViewModel.result.observe(this) { it ->
            if (it.ota.isNullOrEmpty()) {
            ShowToast.showToastLong("已是最新版本")
            }
            else
            {
                upDataUrl=it.ota
                forceUpdate=it.isForceUpdate
                updateDialog()
            }
        }
        mViewModel.msg.observe(this){ code->
        }
    }
    override fun onClick(v: View) {
        when(v.id){
            R.id.settUpdate->
            {
                mViewModel.appUpdate("aiHealth", HelpUtil.getVersionCode(this@AboutActivity))
            }
            R.id.tvUser->{
                JumpUtil.startWeb(this, XingLianApplication.baseUrl+"/agreement/user")
            }
            R.id.tvPrivacy->{
                JumpUtil.startWeb(this, XingLianApplication.baseUrl+"/agreement/privacy")
            }
        }

    }
    private fun updateDialog() {
        newGenjiDialog {
            layoutId = R.layout.dialog_delete
            dimAmount = 0.3f
            isFullHorizontal = true
            animStyle = R.style.AlphaEnterExitAnimation
            convertListenerFun { holder, dialog ->
                var dialogCancel = holder.getView<TextView>(R.id.dialog_cancel)
                var dialogSet = holder.getView<TextView>(R.id.dialog_confirm)
                var dialogContent = holder.getView<TextView>(R.id.dialog_content)
                if(forceUpdate)
                    dialogCancel?.visibility=View.GONE
                else
                    dialogCancel?.visibility=View.VISIBLE
                dialogContent?.text = "APP有更新是否下载最新app进行升级?"
                dialogSet?.setOnClickListener {
                    if (upDataUrl.isNullOrEmpty()) {
                        ShowToast.showToastLong("地址链接失效")
                        return@setOnClickListener
                    } else {
                        val intent = Intent()
                        intent.action = "android.intent.action.VIEW"
                        val contentUrl = Uri.parse(upDataUrl)
                        intent.data = contentUrl
                        startActivity(intent)
                    }
                    dialog.dismiss()
                }
                dialogCancel?.setOnClickListener {
                    dialog.dismiss()
                }
            }
        }.showOnWindow(supportFragmentManager)
    }
}