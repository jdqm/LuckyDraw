package com.jdqm.luckydraw;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.LinearLayout;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    private LinearLayout container1;
    private LinearLayout container2;

    private ScrollDrawView scrollDrawView;

    private List<String> names0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        scrollDrawView = findViewById(R.id.scrollDrawView);
        int n = 10;
        int lineNum = n / 5;
        initData();
        scrollDrawView.setData(names0, "好");

    }

    private void initData() {
        names0 = new ArrayList<>();
        names0.add("张  三1");
        names0.add("李  四2");
        names0.add("王  五3");
        names0.add("赵  六4");
        names0.add("赵本本5");
        names0.add("李柳柳6");
        names0.add("通  钢7");
        names0.add("红  框8");
        names0.add("里皮说9");
        names0.add("里皮说10");
    }

    public void start(View view) {
        scrollDrawView.start();
    }

    public void stop(View view) {
        scrollDrawView.stop();
    }

    @Override
    protected void onDestroy() {
        scrollDrawView.release();
        super.onDestroy();
    }

    public void openLucky(View view) {
        Intent intent = new Intent(this, LuckyDrawActivity.class);
        intent.putExtra("count", 10);
        intent.putExtra("round", 1);
        startActivity(intent);
    }
}
