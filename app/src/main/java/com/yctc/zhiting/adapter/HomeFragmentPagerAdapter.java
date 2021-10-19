package com.yctc.zhiting.adapter;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

import com.app.main.framework.baseview.BaseFragment;
import com.yctc.zhiting.entity.mine.LocationBean;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * author : JIM
 * date : 2021/4/2814:26
 * desc :
 */
public class HomeFragmentPagerAdapter extends FragmentPagerAdapter {

    private List<BaseFragment> mFragments = new ArrayList<>();
    private List<LocationBean> mTitles = new ArrayList<>();

//    public CommonFragmentPagerAdapter(FragmentManager fm, BaseFragment... fragments) {
//        super(fm);
//        mFragments.addAll(Arrays.asList(fragments));
//    }
//
//    public CommonFragmentPagerAdapter(FragmentManager fm, Collection<BaseFragment> fragments) {
//        super(fm);
//        mFragments.addAll(fragments);
//    }

    public HomeFragmentPagerAdapter(FragmentManager fm, Collection<BaseFragment> fragments, List<LocationBean> titles) {
        super(fm);
        mTitles.clear();
        mTitles.addAll(titles);
        mFragments.addAll(fragments);
    }

    @Override
    public Fragment getItem(int position) {
        return mFragments.get(position);
    }

    @Override
    public int getCount() {
        return mFragments.size();
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        if (mTitles != null && mTitles.size() > 0) {
            return mTitles.get(position).getName();
        }
        return "";
    }
}

