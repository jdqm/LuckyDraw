package com.jdqm.luckydraw;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.GradientDrawable;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;

import java.util.List;


/**
 * 循环滚动抽奖
 * <p>
 * Created by yangsheng on 2018-1-24.
 */

public class ScrollDrawView extends View {
    private static final String TAG = "ScrollDrawView";
    public static final int UPDATE_OFFSET = 0;
    public static final int ADD_SPEED = 1;
    public static final int REDUCE_SPEED = 2;
    public static final int REVISE_STOP_POSITION = 3;

    private static final int STATUS_INIT = 0;
    private static final int STATUS_SCROLLING = 1;
    private static final int STATUS_FINISHED = 2;

    public static final int MAX_SPEED = -10;

    private Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    private int textColor = Color.parseColor("#000000");
    private int lineColor = Color.parseColor("#000000");

    //true，只显示姓
    private boolean isEncryption;

    //默认为高度的一半
    private int itemHeight;
    private int textHeight;
    private int offsetY;

    private int startIndex;
    private int secondIndex;
    private int thirdIndex;

    //备选人员数量
    private int n;
    private List<String> data;

    //当前速度
    private int currentV = -4;
    private int baseLineOffset;
    private int luckyPosition;
    private int luckyOffsetY;
    private boolean skip;
    private int status = STATUS_INIT;
    private boolean openLucky;
    private String initWord;

    private GradientDrawable topGd = new GradientDrawable(
            GradientDrawable.Orientation.TOP_BOTTOM,
            new int[]{Color.parseColor("#bb000000"), Color.parseColor("#00000000")});
    private GradientDrawable bottomGd = new GradientDrawable(
            GradientDrawable.Orientation.BOTTOM_TOP,
            new int[]{Color.parseColor("#bb000000"), Color.parseColor("#00000000")});

    private Handler handler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what) {
                case UPDATE_OFFSET:
                    handler.sendEmptyMessageDelayed(0, 4);
                    setOffset(offsetY + currentV);
                    break;
                case ADD_SPEED:
                    if (currentV > MAX_SPEED) {
                        currentV--;
                        handler.sendEmptyMessageDelayed(1, 500);
                    }
                    break;

                case REDUCE_SPEED:
                    calculateNextSpeed();
                    if (currentV <= -1) {
                        handler.sendEmptyMessageDelayed(REDUCE_SPEED, 8);
                        setOffset(offsetY + currentV);
                    } else {
                        handler.removeCallbacksAndMessages(null);
                        handler.removeMessages(REDUCE_SPEED);
                        handler.sendEmptyMessage(REVISE_STOP_POSITION);
                    }
                    break;
                case REVISE_STOP_POSITION:
                    if (offsetY % itemHeight != 0) {
                        setOffset(offsetY - 1);
                        handler.sendEmptyMessageDelayed(3, 2);
                    } else {
                        currentV = -4;
                        if (drawListener != null) {
                            drawListener.onLucky(data.get(secondIndex));
                        }
                        status = STATUS_FINISHED;
                        Log.d(TAG, "中奖人: " + data.get(secondIndex));
                    }
                    break;
                default:
                    break;
            }
        }
    };

    private void calculateNextSpeed() {
        int currOffsetY = Math.abs(offsetY);

        //跳过当次，因为太近了
        if (skip) {
            if (currOffsetY > luckyOffsetY) {
                skip = false;
            }
            return;
        }
        if (currOffsetY > luckyOffsetY) {
            //保持当前速度，再跑一圈
            return;
        }
        currentV = -(int) (((1 - currOffsetY * 1.0f / luckyOffsetY) * 10));
        if (currentV == -1 || currentV == -2) {
            currentV = -3;
        }
    }

    public interface DrawListener {
        void onLucky(String name);
    }

    private DrawListener drawListener;

    public void setDrawListener(DrawListener drawListener) {
        this.drawListener = drawListener;
    }

    public ScrollDrawView(Context context) {
        this(context, null);
    }

    public ScrollDrawView(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public ScrollDrawView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);

        TypedArray ta = context.obtainStyledAttributes(attrs, R.styleable.ScrollDrawView);
        textColor = ta.getColor(R.styleable.ScrollDrawView_textColor, textColor);
        int textSize = (int) ta.getDimension(R.styleable.ScrollDrawView_textSize, DisplayUtil.spToPixel(30));
        lineColor = ta.getColor(R.styleable.ScrollDrawView_lineColor, lineColor);
        isEncryption = ta.getBoolean(R.styleable.ScrollDrawView_encryption, false);
        ta.recycle();

        paint.setStrokeWidth(2);
        paint.setTextSize(textSize);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        //处理wrap_content的默认宽高
        int wMode = MeasureSpec.getMode(widthMeasureSpec);
        int hMode = MeasureSpec.getMode(heightMeasureSpec);
        if (wMode == MeasureSpec.AT_MOST) {
            widthMeasureSpec = MeasureSpec.makeMeasureSpec(DisplayUtil.dpToPixel(80), MeasureSpec.EXACTLY);
        }
        if (hMode == MeasureSpec.AT_MOST) {
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(DisplayUtil.dpToPixel(100), MeasureSpec.EXACTLY);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
        super.onLayout(changed, left, top, right, bottom);
        itemHeight = getHeight() / 2;
        Paint.FontMetricsInt fm = paint.getFontMetricsInt();
        textHeight = fm.bottom - fm.top;
        baseLineOffset = textHeight / 4;

        luckyOffsetY = itemHeight * luckyPosition;
        Log.d(TAG, "luckyOffsetY: " + luckyOffsetY);
        topGd.setBounds(0, 0, getWidth(), itemHeight / 2);
        bottomGd.setBounds(0, itemHeight + itemHeight / 2, getWidth(), getBottom());

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        calculateIndex();
        Log.d(TAG, "startIndex: " + startIndex);
        if (status == STATUS_INIT) {
            drawInit(canvas);
        } else {
            drawScrollView(canvas);
        }
        // topGd.draw(canvas);
        // bottomGd.draw(canvas);
    }

    private void drawScrollView(Canvas canvas) {
        paint.setColor(lineColor);
//        canvas.drawLine(0, startIndex * itemHeight + itemHeight / 2 + offsetY, getWidth(), startIndex * itemHeight + itemHeight / 2 + offsetY, paint);
//        canvas.drawLine(0, secondIndex * itemHeight + itemHeight / 2 + offsetY, getWidth(), secondIndex * itemHeight + itemHeight / 2 + offsetY, paint);
//        canvas.drawLine(0, thirdIndex * itemHeight + itemHeight / 2 + offsetY, getWidth(), thirdIndex * itemHeight + itemHeight / 2 + offsetY, paint);

        paint.setColor(textColor);
        if (!isEncryption) {
            int textWidth = (int) paint.measureText(data.get(startIndex));
            canvas.drawText(data.get(startIndex), (getWidth() - textWidth) / 2, startIndex * itemHeight + baseLineOffset + offsetY, paint);
            textWidth = (int) paint.measureText(data.get(secondIndex));
            canvas.drawText(data.get(secondIndex), (getWidth() - textWidth) / 2, secondIndex * itemHeight + baseLineOffset + offsetY, paint);
            textWidth = (int) paint.measureText(data.get(thirdIndex));
            canvas.drawText(data.get(thirdIndex), (getWidth() - textWidth) / 2, thirdIndex * itemHeight + baseLineOffset + offsetY, paint);
        } else {
            if (openLucky) {
                String text = data.get(secondIndex);
                int textWidth = (int) paint.measureText(text);
                canvas.drawText(text, (getWidth() - textWidth) / 2, secondIndex * itemHeight + baseLineOffset + offsetY, paint);
            } else {
                String text = data.get(startIndex).substring(0, 1) + "**";
                int textWidth = (int) paint.measureText(text);
                canvas.drawText(text, (getWidth() - textWidth) / 2, startIndex * itemHeight + baseLineOffset + offsetY, paint);

                if (!openLucky) {
                    text = data.get(secondIndex).substring(0, 1) + "**";
                    textWidth = (int) paint.measureText(text);
                    canvas.drawText(text, (getWidth() - textWidth) / 2, secondIndex * itemHeight + baseLineOffset + offsetY, paint);
                } else {
                    text = data.get(secondIndex);
                    textWidth = (int) paint.measureText(text);
                    canvas.drawText(text, (getWidth() - textWidth) / 2, secondIndex * itemHeight + baseLineOffset + offsetY, paint);
                }

                text = data.get(thirdIndex).substring(0, 1) + "**";
                textWidth = (int) paint.measureText(text);
                canvas.drawText(text, (getWidth() - textWidth) / 2, thirdIndex * itemHeight + baseLineOffset + offsetY, paint);
            }
        }
    }

    private void drawInit(Canvas canvas) {
        paint.setColor(textColor);
        int textWidth = (int) paint.measureText(initWord);
        canvas.drawText(initWord, (getWidth() - textWidth) / 2, getHeight() / 2 + baseLineOffset, paint);
    }

    private void calculateIndex() {
        startIndex = Math.abs(offsetY / itemHeight);
        if (startIndex >= n) {
            offsetY = 0;
            startIndex = 0;
        }
        secondIndex = startIndex + 1;
        thirdIndex = startIndex + 2;
    }

    public int getOffset() {
        return offsetY;
    }

    public void setOffset(int offsetY) {
        this.offsetY = offsetY;
        invalidate();
    }

    /**
     * @param data 姓名集合
     */
    public void setData(List<String> data, String initWord) {
        status = STATUS_INIT;
        this.initWord = initWord;
        if (data == null || data.size() < 3) {
            throw new IllegalArgumentException("人数必须大于3个");
        }
        this.data = data;
        n = data.size();
        luckyPosition = n - 1;

        //在尾部追加前三个数据，方便计算offsetY做循环处理
        data.add(data.get(0));
        data.add(data.get(1));
        data.add(data.get(2));
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (status == STATUS_FINISHED) {
            setOpenLucky(true);
            return true;
        }
        return super.onTouchEvent(event);
    }

    public int getTextColor() {
        return textColor;
    }

    public void setTextColor(int textColor) {
        this.textColor = textColor;
    }

    public int getLineColor() {
        return lineColor;
    }

    public void setLineColor(int lineColor) {
        this.lineColor = lineColor;
    }

    public boolean isEncryption() {
        return isEncryption;
    }

    public void setEncryption(boolean encryption) {
        isEncryption = encryption;
    }

    public int getOffsetY() {
        return offsetY;
    }

    public void setOffsetY(int offsetY) {
        this.offsetY = offsetY;
    }

    public int getCurrentV() {
        return currentV;
    }

    public void setCurrentV(int currentV) {
        this.currentV = currentV;
    }

    public void start() {
        status = STATUS_SCROLLING;
        openLucky = false;
        handler.removeCallbacksAndMessages(null);
        handler.sendEmptyMessage(UPDATE_OFFSET);
        handler.sendEmptyMessageDelayed(ADD_SPEED, 500);
    }

    public void stop() {
        int cy = Math.abs(offsetY);
        //暂停位置距离目标位置太近，再跑一圈
        if (cy < luckyOffsetY && (luckyOffsetY - cy) < itemHeight * 4) {
            skip = true;
        }
        handler.removeMessages(UPDATE_OFFSET);
        handler.sendEmptyMessage(REDUCE_SPEED);
    }

    public void reset() {
        status = STATUS_INIT;
        invalidate();
    }

    public void release() {
        handler.removeCallbacksAndMessages(null);
    }

    public void setOpenLucky(boolean openLucky) {
        if (status == STATUS_FINISHED) {
            this.openLucky = openLucky;
            invalidate();
        }
    }

}
