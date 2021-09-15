package com.example.xingliansdk.adapter;

import android.content.Context;

import androidx.annotation.NonNull;

import com.example.xingliansdk.base.adapter.BasePagerAdapter;
import com.example.xingliansdk.widget.SportChartViewSwitcher;

/**
 * 功能:Home 头PagerAdapter
 */
public class HomeHeadPagerAdapter extends BasePagerAdapter<SportChartViewSwitcher> implements SportChartViewSwitcher.OnSwitchTypeChangeListener {

    public HomeHeadPagerAdapter(Context context) {
        lists.add(initView(context));
        lists.add(initView(context));
        lists.add(initView(context));
        lists.add(initView(context));
    }
    @NonNull
    private SportChartViewSwitcher initView(Context context) {
        SportChartViewSwitcher switcher = new SportChartViewSwitcher(context);
        switcher.setSwitchTypeChangeListener(this);
        switcher.setSwitchType(SportChartViewSwitcher.TYPE_CIRCLE_PROGRESS);

        return switcher;
    }

    @Override
    public void onSwitchTypeChange(int type) {
        if (lists != null) {
            for (SportChartViewSwitcher switcher : lists) {
                switcher.setSwitchType(type);
            }
        }
    }
}
