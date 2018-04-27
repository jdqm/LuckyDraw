package com.jdqm.luckydraw;

import android.os.Bundle;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {
    private TabLayout tabLayout;
    private ViewPager pager;
    private int[] titlesRes = new int[]{
            R.string.scroll_draw,
            R.string.circle_draw,
            R.string.focus_draw};
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        pager = findViewById(R.id.pager);
        pager.setAdapter(new FragmentPagerAdapter(getSupportFragmentManager()) {

            @Override
            public Fragment getItem(int position) {
                switch (position) {
                    case 0:
                        return new ScrollDrawFragment();
                    case 1:
                        return new circleDrawFragment();
                    case 2:
                        return new FocusDrawFragment();
                }
               return null;
            }

            @Override
            public int getCount() {
                return titlesRes.length;
            }

            @Override
            public CharSequence getPageTitle(int position) {
                return getString(titlesRes[position]);
            }

        });

        tabLayout = findViewById(R.id.tabLayout);
        tabLayout.setupWithViewPager(pager);
    }
}