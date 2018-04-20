package com.jdqm.luckydraw;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.GridLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class LuckyDrawActivity extends Activity {
    private static final String TAG = "LuckyDrawActivity";
    private static final int STATUS_START = 0;
    private static final int STATUS_STOP = 1;
    private static final int STATUS_OPEN = 2;
    private static final int STATUS_AGAIN = 3;

    public static final String ROUND_ONE = "round_one";
    public static final String ROUND_TWO = "round_two";
    public static final String ROUND_THREE = "round_three";

    private TextView tvTitle;
    private GridLayout gridLayout;
    private int count;
    private int rowCount = 1;
    private int columnCount = 5;
    private List<String> names0;
    private ScrollDrawView[] drawViews;
    private Button btnAction;
    private int status = STATUS_START;

    //所有View滚动停止才能点全部揭晓
    private int finishedCount;

    //当前轮次
    private int round;

    private String[] initWords = {"来", "看", "看", "你", "的", "运", "气", "好", "不", "好"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lucky_draw);
        count = getIntent().getIntExtra("count", -1);
        round = getIntent().getIntExtra("round", -1);
        Log.d(TAG, "count: " + count);
        if (count <= 0 || round <= 0) {
            finish();
        }
        drawViews = new ScrollDrawView[count];
        if (count > 5) {
            rowCount = count / 5;
            if (count % 5 != 0) {
                rowCount++;
            }
            columnCount = count / rowCount;
            if (count % rowCount != 0) {
                columnCount++;
            }
        }

        initData();
        initViews();
        checkThisRound();
    }

    private void initViews() {
        tvTitle = findViewById(R.id.tvTitle);
        gridLayout = findViewById(R.id.gridLayout);
        btnAction = findViewById(R.id.btnAction);

        gridLayout.setRowCount(rowCount);
        gridLayout.setColumnCount(columnCount);

        for (int i = 0; i < count; i++) {
            View view = View.inflate(this, R.layout.draw_item, null);
            drawViews[i] = view.findViewById(R.id.scrollDrawView);
            drawViews[i].setData(names0, initWords[i]);
            gridLayout.addView(view);
        }
        setTitle();
    }

    private void setTitle() {
        SharedPreferences sp = getSharedPreferences("config", MODE_PRIVATE);
        int times = 0;
        if (round == 1) {
            times = sp.getInt(ROUND_ONE, 0);
        } else if (round == 2) {
            times = sp.getInt(ROUND_TWO, 0);
        } else if (round == 3) {
            times = sp.getInt(ROUND_THREE, 0);
        } else {
            // TODO: 2018-1-28 特别奖标题如何显示
        }
        times++;
        tvTitle.setText("第" + round + "轮，第" + times + "次");
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
        if (status == STATUS_START) {
            status = STATUS_STOP;
            finishedCount = 0;
            btnAction.setBackgroundResource(R.drawable.stop_bg_selector);
            for (int i = 0; i < count; i++) {
                drawViews[i].start();
            }
            writeProgress();
        } else if (status == STATUS_STOP) {
            status = STATUS_OPEN;
            btnAction.setVisibility(View.INVISIBLE);
            btnAction.setBackgroundResource(R.drawable.open_all);
            for (int i = 0; i < count; i++) {
                drawViews[i].stop();
                drawViews[i].setDrawListener(new ScrollDrawView.DrawListener() {
                    @Override
                    public void onLucky(String name) {
                        finishedCount++;
                        // TODO: 2018-1-28 记录中奖人

                        if (finishedCount == count) {
                            btnAction.setVisibility(View.VISIBLE);
                        }
                    }
                });
            }

        } else if (status == STATUS_OPEN) {
            status = STATUS_AGAIN;
            btnAction.setBackgroundResource(R.drawable.again_bg_selector);
            for (int i = 0; i < count; i++) {
                drawViews[i].setOpenLucky(true);
            }
            if (checkThisRound()){
                tvTitle.setText("本轮已结束");
            }

        } else if (status == STATUS_AGAIN) {
            status = STATUS_START;
            btnAction.setBackgroundResource(R.drawable.start_bg_selector);
            setTitle();
            // TODO: 2018-1-28  重置数据
            for (int i = 0; i < drawViews.length; i++) {
                drawViews[i].reset();
            }

        }

    }

    private void writeProgress() {
        SharedPreferences sp = getSharedPreferences("config", MODE_PRIVATE);
        int count;
        if (round == 1) { //一次10个抽四次
            count = sp.getInt(ROUND_ONE, 0);
            count++;
            sp.edit().putInt(ROUND_ONE, count).apply();
            Log.d(TAG, "第一轮已经行: " + count + "次");
        } else if (round == 2) {
            count = sp.getInt(ROUND_TWO, 0);
            count++;
            sp.edit().putInt(ROUND_TWO, count).apply();
            Log.d(TAG, "第二轮已经行: " + count + "次");
        } else if (round == 3) {
            count = sp.getInt(ROUND_THREE, 0);
            count++;
            sp.edit().putInt(ROUND_THREE, count).apply();
            Log.d(TAG, "第三轮已经行: " + count + "次");
        }
    }

    /**
     * 全部开奖后，若本轮结束，则隐藏按钮（或者显示已结束）
     *
     * @return true, this round is end.
     */
    private boolean checkThisRound() {
        SharedPreferences sp = getSharedPreferences("config", MODE_PRIVATE);
        int count;
        int c;
        if (round == 1) {
            count = 100;
            c = sp.getInt(ROUND_ONE, 0);
        } else if (round == 2) {
            count = 2;
            c = sp.getInt(ROUND_TWO, 0);
        } else if (round == 3) {
            count = 1;
            c = sp.getInt(ROUND_THREE, 0);
        } else {
            return false;
        }
        if (c >= count) {
            btnAction.setVisibility(View.GONE);
            return true;
        }
        return false;
    }

    @Override
    protected void onDestroy() {
        for (int i = 0; i < count; i++) {
            drawViews[i].release();
        }
        super.onDestroy();
    }
}
