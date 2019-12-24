package com.example.waterseekbar.widget;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;

import androidx.annotation.ColorInt;

import com.example.waterseekbar.R;


public class WaterSeekBar extends View {

    //选中节点图标
    private Drawable mThumb;
    //当前选中节点图标的大小
    private int mThumbOffset ;
    //选中节点图标颜色
    private int mThumbColor;
    //刻度图标
    private Drawable mNode;
    //刻度图标的大小
    private int mNodeOffset;
    //刻度图标颜色
    private int mNodeColor;
    //选中节点上方的水滴图标
    private Drawable mWaterIcon;
    //选中节点上方的水滴图标颜色
    private int mWaterIconColor;
    //选中节点上方的水滴图标大小
    private int mWaterSize;
    private Paint mPaint;
    //进度条线的宽度
    int lineStoke = dp2px(3);
    //进度条线的颜色（分段颜色，）
    int[] lineColors = new int[]{Color.parseColor("#2AA703"), Color.parseColor("#F39A0F"), Color.parseColor("#D81B60")};
    //进度条最大值
    int max = 15;
    //当前进度值
    int progress = 0;

    //控件宽度
    int width;
    //控件高度
    int height ;
    //剩余宽度 = 控件宽度 % 进度条最大值
    int residueWidth  ;
    //剩余宽度平分到两端的宽度
    int diffX;
    //刻度的间隔宽度
    int spaceWidth;
    //两个刻度的中间点坐标X（用来确定选中的坐标属于哪个刻度）
    int[] coords ;

    private OnProgressChangeListener progressChangeListener;
    /*
        app:max="15"
        app:progress="6"
        app:water_icon_size="32dp"
        app:water_icon="@drawable/ic_water"
        app:thumb_icon="@drawable/ic_seekbar"
        app:thumb_icon_color="@color/colorPrimary"
        app:thumb_icon_size="16dp"
        app:node_icon="@drawable/ic_seekbar"
        app:node_icon_color="@color/colorAccent"
        app:node_icon_size="20dp"
    * */

    public WaterSeekBar(Context context) {
        super(context);
    }

    public WaterSeekBar(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public WaterSeekBar(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initPaint();
        final TypedArray a = context.obtainStyledAttributes(attrs, R.styleable.WaterSeekBar,  defStyleAttr, 0);
        max = a.getInteger(R.styleable.WaterSeekBar_max, max);
        progress = a.getInteger(R.styleable.WaterSeekBar_progress, progress);
        if(progress>max) progress = max;
        if(a.hasValue(R.styleable.WaterSeekBar_thumb_icon)){
            mThumb = a.getDrawable(R.styleable.WaterSeekBar_thumb_icon);
        }else{
            mThumb = context.getResources().getDrawable(R.drawable.ic_seekbar);
        }
        mThumbOffset = a.getDimensionPixelSize(R.styleable.WaterSeekBar_thumb_icon_size, dp2px(18));
        if(a.hasValue(R.styleable.WaterSeekBar_thumb_icon_color)){
            mThumbColor = a.getColor(R.styleable.WaterSeekBar_thumb_icon_color, getResources().getColor(R.color.colorAccent));
            mThumb.setColorFilter(mThumbColor, PorterDuff.Mode.SRC_ATOP);
        }
        mThumb.getBounds().set(0,0, mThumbOffset, mThumbOffset);

        if(a.hasValue(R.styleable.WaterSeekBar_node_icon)){
            mNode = a.getDrawable(R.styleable.WaterSeekBar_node_icon);
            mNodeOffset = a.getDimensionPixelSize(R.styleable.WaterSeekBar_node_icon_size, dp2px(12));
            mNode.getBounds().set(0,0, mNodeOffset, mNodeOffset);
            if(a.hasValue(R.styleable.WaterSeekBar_node_icon_color)){
                mNodeColor = a.getColor(R.styleable.WaterSeekBar_node_icon_color,0);
                mNode.setColorFilter(mNodeColor, PorterDuff.Mode.SRC_ATOP);
            }
        }else{
            mNode = null;
            mNodeOffset = 0;
        }
        if(a.hasValue(R.styleable.WaterSeekBar_water_icon)){
            mWaterIcon = a.getDrawable(R.styleable.WaterSeekBar_water_icon);
        }else{
            mWaterIcon = context.getResources().getDrawable(R.drawable.ic_water);
        }
        mWaterSize = a.getDimensionPixelSize(R.styleable.WaterSeekBar_water_icon_size, dp2px(26));
        mWaterIcon.getBounds().set(0,0, mWaterSize, mWaterSize);
        a.recycle();
    }

    private void initPaint() {
        mPaint = new Paint();
        mPaint.setColor(Color.GREEN);
        //指定是否使用抗锯齿功能，如果使用，会使绘图速度变慢
        mPaint.setAntiAlias(true);
    }

    public interface OnProgressChangeListener{
        void progressChange(int progress);
    }

    public void setLineColors(@ColorInt int... lineColors) {
        this.lineColors = lineColors;
        invalidate();
    }

    public void setMax(int max){
        this.max = max;
        invalidate();
    }

    public void setProgress(int progress){
        progress = progress>max ? max:progress;
        if(progress!=this.progress){
            this.progress = progress;
            if(progressChangeListener!=null){
                progressChangeListener.progressChange(this.progress);
            }
            invalidate();
        }
    }

    public void setProgressChangeListener(OnProgressChangeListener progressChangeListener) {
        this.progressChangeListener = progressChangeListener;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int width = MeasureSpec.getSize(widthMeasureSpec);
        int height = measureHeight( getSuggestedMinimumHeight(), heightMeasureSpec);
        setMeasuredDimension(width, height);
    }

    private int measureHeight(int defaultHeight, int measureSpec) {
        int specMode = MeasureSpec.getMode(measureSpec);
        int specSize = MeasureSpec.getSize(measureSpec);
        switch (specMode) {
            case MeasureSpec.AT_MOST:
                defaultHeight = mThumbOffset+mWaterSize + getPaddingTop() + getPaddingBottom();
                break;
            case MeasureSpec.EXACTLY:
                defaultHeight = specSize;
                break;
            case MeasureSpec.UNSPECIFIED:
                defaultHeight = Math.max(defaultHeight, specSize);
                break;
        }
        return defaultHeight;
    }

    @Override
    protected synchronized void onDraw(Canvas canvas) {
        width =  getMeasuredWidth();
        height = getMeasuredHeight();
        //剩余宽度 = 控件宽度 % 进度条最大值
        residueWidth = (width - getPaddingLeft() - getPaddingRight() - mThumbOffset)%max ;
        //剩余宽度平分到两端的宽度
        diffX = residueWidth==0 ? 0 : residueWidth/2;

        int startX = getPaddingLeft() + mThumbOffset/2 + diffX;
        int stopX = width - mThumbOffset/2 - getPaddingRight()- diffX;
        int startY = height-lineStoke/2-mThumbOffset/2-getPaddingBottom();
        int stopY = height-mThumbOffset/2+lineStoke/2-getPaddingBottom();
        //startX、stopX、startY、stopY 是画进度条横线的坐标
        draw(canvas, startX, startY,stopX ,stopY , mPaint );
    }

    /**
     *
     * @param canvas
     * @param startX 画进度条的坐标
     * @param startY 画进度条的坐标
     * @param stopX  画进度条的坐标
     * @param stopY  画进度条的坐标
     * @param mPaint
     */
    private void draw(Canvas canvas, int startX, int startY, int stopX, int stopY, Paint mPaint) {
        //进度条的分段颜色数量
        int count = lineColors.length;
        int dx = startX;
        //刻度的间隔宽度
        spaceWidth = (stopX - startX) / count;
        //绘制进度条的横线
        for (int i = 0; i < count; i++) {
            mPaint.setColor(lineColors[i]);
            canvas.drawRect(dx,startY, dx+spaceWidth, stopY, mPaint);
            dx += spaceWidth;
        }
        int saveflag = canvas.save();
        int cx = startX;
        //进度条横线的水平中心线
        int cy = startY + (stopY - startY)/2;
        //刻度图标的间隔宽度
        spaceWidth = (stopX - startX) / max;
        //两个刻度的中间点坐标X（用来确定选中的坐标属于哪个刻度）
        coords = new int[max+1];
        coords[0] = getLeft();
        coords[max] = getRight();
        //平移坐标系到第一个刻度位置
        canvas.translate(cx-mNodeOffset/2,cy-mNodeOffset/2);
        for (int i = 0; i <= max; i++) {
            //绘制刻度图标
            if(mNode!=null){
                mNode.draw(canvas);
                canvas.translate(spaceWidth, 0);
            }
            //记录刻度的坐标范围
            if(i<max-1){
                coords[i+1] = cx+spaceWidth/2;
            }
            cx += spaceWidth;
        }
        canvas.restoreToCount(saveflag);
        //计算进度所在刻度的坐标
        cx = startX + spaceWidth * progress - mThumb.getBounds().right/2;
        cy = startY + (stopY - startY)/2 - mThumb.getBounds().bottom/2;
        //平移坐标系到当前进度点
        canvas.translate(cx,cy);
        //绘制选中进度图标
        mThumb.draw(canvas);
        //根据进度计算进度上方水滴图标的颜色
        int colorIndex = progress/(max/count);
        int wColor = lineColors[colorIndex>=lineColors.length ? lineColors.length-1 : colorIndex];
        if(mWaterIconColor != wColor){
            mWaterIconColor = wColor;
            mWaterIcon.setColorFilter(wColor, PorterDuff.Mode.SRC_ATOP);
        }
        canvas.translate(mThumb.getBounds().right/2 - mWaterIcon.getBounds().right/2, - mWaterIcon.getBounds().bottom+dp2px(2));
        //绘制进度上方水滴图标
        mWaterIcon.draw(canvas);
        canvas.restoreToCount(saveflag);
        canvas.translate(mWaterIcon.getBounds().right/2,mWaterIcon.getBounds().bottom/2-dp2px(2));
        mPaint.setColor(Color.WHITE);
        //设置画笔宽度
        mPaint.setStrokeWidth(5);
        //绘图样式，对于设文字和几何图形都有效
        mPaint.setStyle(Paint.Style.FILL);
        //设置文字对齐方式，取值：align.CENTER、align.LEFT或align.RIGHT
        mPaint.setTextAlign(Paint.Align.CENTER);
        mPaint.setTextSize(dp2px(12));
        Paint.FontMetrics fontMetrics = mPaint.getFontMetrics();
        //绘制进度上方水滴图标中的进度文字
        canvas.drawText(String.valueOf(progress), 0, (fontMetrics.descent-fontMetrics.leading)/2, mPaint);
        canvas.restoreToCount(saveflag);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float x = event.getX();
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_MOVE:
            case MotionEvent.ACTION_UP:
                int currProgress = progress;
                if(x <= getLeft() + getPaddingLeft() + mThumbOffset ){
                    currProgress = 0;
                }else if(x >= getRight() - getPaddingRight() - mThumbOffset ){
                    currProgress = max;
                }else{
                    int index = Math.round(x/spaceWidth-1);
                    for (int i = index; i < coords.length; i++) {
                        if(i<0){
                            currProgress = 0;
                            break;
                        } else if(i>=coords.length){
                            currProgress = max;
                            break;
                        }else if(coords[i]<=x && i+1<coords.length && coords[i+1]>=x){
                            currProgress = i;
                            break;
                        }else if(coords[i]>x){
                            i-=2;
                        }
                    }
                }
                setProgress(currProgress);
                return true;
        }
        if(x>= getLeft() && x<=getRight()){
            return true;
        }
        return super.onTouchEvent(event);
    }

    private int dp2px(int dpValue){
        float density = Resources.getSystem().getDisplayMetrics().density;
        return (int) (dpValue * density + 0.5f);
    }
}
