package com.wdb.ihaustReportUtils.ui.main.tab.adapter;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.wdb.ihaustReportUtils.ui.main.tab.fragment.JsonFragment;
import com.wdb.ihaustReportUtils.ui.main.tab.fragment.TokenFragment;

import java.util.ArrayList;
import java.util.List;

public class TabFragmentAdapter extends FragmentStatePagerAdapter {
    private List<String> tabs;
    private List<Fragment> fragments;

    public TabFragmentAdapter(@NonNull FragmentManager fm) {
        super(fm);
        tabs = new ArrayList<>();
        fragments = new ArrayList<>();
        tabs.add("token");
        tabs.add("json");
        fragments.add(new TokenFragment());
        fragments.add( new JsonFragment());

    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        return fragments.get(position);
    }

    @Override
    public int getCount() {
        return tabs.size();
    }

    @Nullable
    @Override
    public CharSequence getPageTitle(int position) {
        return tabs.get(position);
    }
}
