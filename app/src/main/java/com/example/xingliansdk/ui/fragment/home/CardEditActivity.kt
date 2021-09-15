package com.example.xingliansdk.ui.fragment.home

import android.animation.ValueAnimator
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.RecyclerView
import com.chad.library.adapter.base.listener.OnItemDragListener
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.example.xingliansdk.Config
import com.example.xingliansdk.R
import com.example.xingliansdk.adapter.CardDeleAdapter
import com.example.xingliansdk.adapter.CardEditAdapter
import com.example.xingliansdk.base.BaseActivity
import com.example.xingliansdk.bean.HomeCardBean
import com.example.xingliansdk.eventbus.SNEventBus
import com.shon.connector.utils.TLog
import com.example.xingliansdk.viewmodel.MainViewModel
import com.example.xingliansdk.widget.TitleBarLayout
import com.google.gson.Gson
import com.gyf.barlibrary.ImmersionBar
import com.orhanobut.hawk.Hawk
import kotlinx.android.synthetic.main.activity_card_edit.*

class CardEditActivity : BaseActivity<MainViewModel>() {
    lateinit var mCardEditAdapter: CardEditAdapter
    lateinit var mCardDeleAdapter: CardDeleAdapter
    private var mAddList: MutableList<HomeCardBean.AddCardDTO> = arrayListOf()
    private var mDeleList: MutableList<HomeCardBean.DeleCardDTO> = arrayListOf()
    override fun layoutId() = R.layout.activity_card_edit
    override fun initView(savedInstanceState: Bundle?) {
        ImmersionBar.with(this)
            .titleBar(titleBar)
            .init()
        if (mHomeCardBean.addCard != null)
            mAddList = mHomeCardBean.addCard
        if (mHomeCardBean.deleCard != null)
            mDeleList = mHomeCardBean.deleCard
        titleBar.setTitleBarListener(object : TitleBarLayout.TitleBarListener {
            override fun onBackClick() {
                TLog.error("没走吗")
                updateCard()
                finish()
            }

            override fun onActionImageClick() {
            }

            override fun onActionClick() {
                updateCard()
                finish()
            }

        })
        // 拖拽监听
        val listener: OnItemDragListener = object : OnItemDragListener {
            override fun onItemDragStart(viewHolder: RecyclerView.ViewHolder, pos: Int) {
                val holder = viewHolder as BaseViewHolder
                val startColor = Color.WHITE
                val endColor = Color.rgb(245, 245, 245)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    val v =
                        ValueAnimator.ofArgb(startColor, endColor)
                    v.addUpdateListener { animation -> holder.itemView.setBackgroundColor(animation.animatedValue as Int) }
                    v.duration = 300
                    v.start()
                }
            }

            override fun onItemDragMoving(
                source: RecyclerView.ViewHolder,
                from: Int,
                target: RecyclerView.ViewHolder,
                to: Int
            ) {
            }

            override fun onItemDragEnd(viewHolder: RecyclerView.ViewHolder, pos: Int) {

                val holder = viewHolder as BaseViewHolder
                val startColor = Color.rgb(245, 245, 245)
                val endColor = Color.WHITE
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    val v =
                        ValueAnimator.ofArgb(startColor, endColor)
                    v.addUpdateListener { animation -> holder.itemView.setBackgroundColor(animation.animatedValue as Int) }
                    v.duration = 300
                    v.start()
                }

            }
        }
        mCardEditAdapter = CardEditAdapter(mAddList)
        mCardDeleAdapter = CardDeleAdapter(mDeleList)
        mCardEditAdapter?.let {
            it.draggableModule?.isDragEnabled = true
            it.draggableModule?.setOnItemDragListener(listener)
            it.draggableModule?.itemTouchHelperCallback?.setDragMoveFlags(ItemTouchHelper.LEFT or ItemTouchHelper.RIGHT or ItemTouchHelper.UP or ItemTouchHelper.DOWN)
        }
        ryCardAdd.adapter = mCardEditAdapter
        ryCardMore.adapter = mCardDeleAdapter
        mCardEditAdapter.setOnItemClickListener { adapter, view, position ->
            mDeleList.add(
                HomeCardBean.DeleCardDTO(
                    mCardEditAdapter.data[position].name,
                    mCardEditAdapter.data[position].type,
                    mCardEditAdapter.data[position].img,
                    mCardEditAdapter.data[position].time,
                    mCardEditAdapter.data[position].dayContent,
                    mCardEditAdapter.data[position].dayContentString,
                    mCardEditAdapter.data[position].subTitle
                )
            )
            mAddList.removeAt(position)
            mCardEditAdapter.notifyItemRemoved(position)
            mCardDeleAdapter.notifyItemChanged(position)
            loadingVisibility()
            // updateCard()
        }
        mCardDeleAdapter.setOnItemClickListener { adapter, view, position ->
            //先加后删
            mAddList.add(
                HomeCardBean.AddCardDTO(
                    mCardDeleAdapter.data[position].name,
                    mCardDeleAdapter.data[position].type,
                    mCardDeleAdapter.data[position].img,
                    mCardDeleAdapter.data[position].time,
                    mCardDeleAdapter.data[position].dayContent,
                    mCardDeleAdapter.data[position].dayContentString,
                    mCardDeleAdapter.data[position].subTitle
                )
            )
            mDeleList.removeAt(position)
            mCardEditAdapter.notifyItemChanged(position)
            mCardDeleAdapter.notifyItemRemoved(position)
            loadingVisibility()
            //    updateCard()
        }
        loadingVisibility()
    }

    private fun updateCard() {
        mHomeCardBean.addCard = mAddList
        mHomeCardBean.deleCard = mDeleList
        TLog.error("直接改变结果++${Gson().toJson(mHomeCardBean)}")
        Hawk.put("HomeCardBean", mHomeCardBean)
        SNEventBus.sendEvent(Config.eventBus.HOME_CARD)
    }

    private fun loadingVisibility() {
       // TLog.error("mDeleList+=" + mDeleList.size)
     //   TLog.error("mAddList+=" + mAddList.size)
        if (mDeleList.size <= 0 || mDeleList == null) {
        //    TLog.error("??")
            tvMore.visibility = View.GONE
            cvMoreCard.visibility = View.GONE
        } else if (mAddList.size <= 0 || mAddList == null) {
        //    TLog.error("mAddList")
            tvAdd.visibility = View.GONE
            cvAddCard.visibility = View.GONE
        } else {
//            TLog.error("还能进else?")
            tvMore.visibility = View.VISIBLE
            cvMoreCard.visibility = View.VISIBLE
            tvAdd.visibility = View.VISIBLE
            cvAddCard.visibility = View.VISIBLE
        }
    }

    override fun onKeyDown(keyCode: Int, event: KeyEvent?): Boolean {
        if(keyCode==KeyEvent.KEYCODE_BACK){
            TLog.error("做一个事件管理")
            updateCard()
        }

        return super.onKeyDown(keyCode, event)
    }
}