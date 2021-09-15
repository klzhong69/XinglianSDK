package com.example.xingliansdk.utils

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Environment
import android.widget.ImageView
import androidx.core.app.ActivityCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.DecodeFormat
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.Target
import com.example.xingliansdk.Config
import com.example.xingliansdk.R
import com.example.xingliansdk.XingLianApplication
import com.orhanobut.hawk.Hawk
import com.shon.connector.utils.TLog
import okhttp3.TlsVersion
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

object ImgUtil {
    fun loadImage(
        context: Context?,
        url: String?
    ) {

        val isPermissRead = ActivityCompat.checkSelfPermission(
            XingLianApplication.mXingLianApplication,
            Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        if(!isPermissRead)
            return
        ThreadUtils.submit {
            var bitmap: Bitmap? = null
            val file =
                XingLianApplication.getXingLianApplication()
                    .getExternalFilesDir(null)?.absolutePath //public绝对路径
            val appDir = File(file, "iChat")
            if (!appDir.exists()) {
                appDir.mkdirs()
            }
            val pathName = "avatar.jpg"
            val currentFile = File(appDir, pathName)
            try {
                bitmap = Glide.with(context!!)
                    .asBitmap()
                    .load(url)
                    .diskCacheStrategy(DiskCacheStrategy.NONE)
                    .into(
                        Target.SIZE_ORIGINAL,
                        Target.SIZE_ORIGINAL
                    ).get()
                if (bitmap != null) {
                    saveImageToGallery(bitmap, currentFile)
                }
            } catch (e: Exception) {
                TLog.error("e==" + e.toString())
            }
        }
        object : Thread() {
            override fun run() {

            }
        }.start()
    }

    /**
     * 保存图片到相册
     *
     * @param bmp
     */
    fun saveImageToGallery(bmp: Bitmap, file: File?) {
        var fos: FileOutputStream? = null
        try {
            fos = FileOutputStream(file)
            bmp.compress(Bitmap.CompressFormat.JPEG, 100, fos)
            fos.flush()
//            TLog.error("保存完成" + file.toString())
            Hawk.put(Config.database.IMG_HEAD, file.toString())
        } catch (e: Exception) {
            e.printStackTrace()
        } finally {
            try {
                fos?.close()
            } catch (e: IOException) {
                TLog.error("e.printStackTrace()+=" + e.printStackTrace())
                e.printStackTrace()
            }
        }
    }

    fun loadCircle(ivHead: ImageView, url: Any) {
        Glide.with(ivHead.context).load(url).circleCrop().skipMemoryCache(true)
//            .override(  Target.SIZE_ORIGINAL, Target.SIZE_ORIGINAL)//加载原始图大小
          //  .format(DecodeFormat.PREFER_RGB_565)//设置通道减少内存
            .diskCacheStrategy(DiskCacheStrategy.NONE).into(ivHead)
    }
    fun loadHead(ivHead: ImageView, url: Any) {
        Glide.with(ivHead.context).load(url).skipMemoryCache(true)
            .diskCacheStrategy(DiskCacheStrategy.NONE)
            .circleCrop()
            .into(ivHead)
    }

    fun loadMeImgDialCircle(ivHead: ImageView, url: Any) {
        Glide.with(ivHead.context).load(url).circleCrop()
            .into(ivHead)
    }
    fun loadRound(ivHead: ImageView, url: Any, round: Int = 10) {
        Glide.with(ivHead.context).load(url).apply(roundOptions(round))
            .placeholder(R.mipmap.ic_launcher).dontAnimate().into(ivHead)
    }

    private fun roundOptions(size: Int = 10) = RequestOptions.bitmapTransform(RoundedCorners(size))
}