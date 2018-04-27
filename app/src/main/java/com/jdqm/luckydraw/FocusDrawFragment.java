package com.jdqm.luckydraw;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Jdqm on 2018-4-27.
 */
public class FocusDrawFragment extends Fragment {
    private Button btnAction;
    private ScrollDrawView scrollDrawView1;
    private ScrollDrawView scrollDrawView2;
    /**
     * 是否正在滚动抽奖
     */
    private boolean isScrolling;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.scroll_draw_layout, container, false);
        scrollDrawView1 = view.findViewById(R.id.scrollDrawView1);
        scrollDrawView2 = view.findViewById(R.id.scrollDrawView2);
        initData();
        btnAction = view.findViewById(R.id.btnAction);
        btnAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (isScrolling) {
                    isScrolling = false;
                    btnAction.setText("start");
                    scrollDrawView1.stop();
                    scrollDrawView2.stop();
                } else {
                    isScrolling = true;
                    btnAction.setText("stop");
                    scrollDrawView1.start();
                    scrollDrawView2.start();
                }
            }
        });
        return view;
    }

    private void initData() {
        List<String> data = new ArrayList<>();
        data.add("张三");
        data.add("李四");
        data.add("王五");
        data.add("刘德华");
        data.add("周杰伦");
        data.add("乔布斯");
        data.add("库克");
        data.add("马云");
        data.add("马化腾");
        data.add("李彦宏");
        scrollDrawView1.setData(data, "奖");
        scrollDrawView2.setData(data, "奖");
    }
}