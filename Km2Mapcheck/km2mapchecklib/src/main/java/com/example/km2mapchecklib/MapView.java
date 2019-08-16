package com.example.km2mapchecklib;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.os.Handler;
import android.os.Message;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.View;
import android.widget.CursorTreeAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

public class MapView extends View implements Observer {
    private Paint paint;//画笔
    private Paint carPaint;
    private Bitmap cacheBitmap = null;//定义一个内存中的图片,该图片将作为缓冲区
    private Canvas cacheCanvas = null;//定义cacheBitmap上的canvas对象
    private final int VIEW_WIDTH = 800;
    private final int VIEW_HEIGHT = 500;

    private static double dmapX = 0;
    private static double dmapY = 0;
    private static int nMoveX;
    private static int nMoveY;
    private static int nCurrentZoomLevel = 0;
    private static int ZoomLevels[] = {1, 5, 10, 20, 40, 80, 160, 320};
    private static int MAP_LEVEL_COUNT = 8;
    private static LocationData mLocationData;
    private List<Integer> mfrontWheelIndexList = new ArrayList();
    private List<Integer> mBackWheelIndexList = new ArrayList();
    //static Boolean test = false;

    //MapView mapView;

    @Override
    public void update(Observable o, Object arg) {
        mLocationData = (LocationData)arg;

        dmapX = mLocationData.longitude;
        dmapY = mLocationData.latitude;

        /*mapView.*/invalidate();
    }

    public MapView(Context context, AttributeSet attrs)
    {
        super(context, attrs);

        //mapView = (MapView)findViewById(R.id.iv_canvas);
        //handler.post(runnable);

        //创建一个与该View相同大小的缓存区
        cacheBitmap = Bitmap.createBitmap(VIEW_WIDTH,VIEW_HEIGHT,Bitmap.Config.ARGB_8888);
        cacheCanvas = new Canvas();
        //设置cacheCanvas将会绘制到内存中的cacheBitmap上
        cacheCanvas.setBitmap(cacheBitmap);

        //设置画笔的颜色
        paint = new Paint(Paint.ANTI_ALIAS_FLAG |Paint.DITHER_FLAG);
        paint.setColor(Color.RED);
        //设置画笔的风格
        paint.setStyle(Paint.Style.STROKE);

        paint.setStrokeWidth((float) 0.5);
        //反锯齿
        paint.setAntiAlias(true);
        paint.setDither(true);

        carPaint = new Paint(Paint.ANTI_ALIAS_FLAG |Paint.DITHER_FLAG);
        carPaint.setColor(Color.BLUE);
        //设置画笔的风格
        carPaint.setStyle(Paint.Style.STROKE);

        carPaint.setStrokeWidth((float) 0.5);
        //反锯齿
        carPaint.setAntiAlias(true);
        carPaint.setDither(true);

        Init();
        ProcessCenter.getInstance().addObserver(this);
        //setBackgroundColor(Color.BLUE);
    }

    /*Handler handler=new Handler();
    Runnable runnable = new Runnable() {
        @Override
        public void run() {
            invalidate();
            handler.post(this);
        }
    };*/

    public void Init()
    {
        nMoveX = VIEW_WIDTH/2;
        nMoveY = VIEW_HEIGHT/2;
        nCurrentZoomLevel = 2;

        mfrontWheelIndexList.add(24);
        mfrontWheelIndexList.add(25);
        mfrontWheelIndexList.add(29);
        mfrontWheelIndexList.add(28);

        mBackWheelIndexList.add(26);
        mBackWheelIndexList.add(27);
        mBackWheelIndexList.add(31);
        mBackWheelIndexList.add(30);
    }

    @Override
    public void onDraw(Canvas canvas)
    {
        super.onDraw(canvas);

        List<VanField> fieldList = MapManager.getInstance().GetFields();
        for (int i = 0; i < fieldList.size();i++)
            DrawMap(fieldList.get(i), canvas);

        if(CarModel.getInstance().mDynamicPt.size() == 32)
            DrawCar(canvas);

        //canvas.drawBitmap(cacheBitmap, 0, 0, null);
    }

    public  void DrawMap(VanField vanField, Canvas cas)
    {
        int preX = 0,preY = 0;
        int currX,currY;
        int nCount = vanField.GetZoneCount();
        for (int i = 0; i < nCount; i++)
        {
            VanZone vanZone = vanField.GetZone(i);
            int ptCount = vanZone.nPointCount;

            LLRECT rc = new LLRECT(0x0ffffffffL, -2147483647L - 1, -2147483647L - 1, 0x0ffffffffL);
            for(int j = 0; j < ptCount; j++)
            {
                /*if(!test)
                {
                    dmapX = vanZone.listPoints.get(j).lon;
                    dmapY = vanZone.listPoints.get(j).lat;
                    test = true;
                }*/
                currX = MoveX(vanZone.listPoints.get(j).lon);
                currY = MoveY(vanZone.listPoints.get(j).lat);
                rc.Union(currX,currY);

                if (0 == j)
                {
                    preX = currX;
                    preY = currY;
                }
                else
                {
                    cas.drawLine(preX,preY,currX,currY,paint);
                    preX = currX;
                    preY = currY;
                }
            }
        }
    }

    private static int MoveX(double x)
    {
        double d = (x - dmapX)*ZoomLevels[nCurrentZoomLevel] + nMoveX;
        return (int)d;
    }

    private static int MoveY(double y)
    {
        double d = (dmapY - y)*ZoomLevels[nCurrentZoomLevel] + nMoveY;
        return (int)d;
    }

    public void DrawCar(Canvas cas)
    {
        if(mLocationData == null)
            return;

        int nCircleRadius = 6;
        int x,y,x2 = 0,y2 = 0;
        x = MoveX(mLocationData.longitude);
        y = MoveY(mLocationData.latitude);

        if (nCurrentZoomLevel>4) {
            cas.drawCircle((float) (x), (float) (y), (float) nCircleRadius, carPaint);
            cas.drawLine((float) (x - nCircleRadius), (float) (y), (float) (x + nCircleRadius), (float) y, carPaint);
            cas.drawLine((float) x, (float) (y - nCircleRadius), (float) x, (float) (y + nCircleRadius), carPaint);
        }

        int v = 0;
        for (int i = 0; i < 24; ++i)
        {
            double t1 = CarModel.getInstance().mDynamicPt.get(i).lon;
            double t2 = CarModel.getInstance().mDynamicPt.get(i).lat;
            x = MoveX(CarModel.getInstance().mDynamicPt.get(i).lon);
            y = MoveY(CarModel.getInstance().mDynamicPt.get(i).lat);
            if(i < 23) {
                x2 = MoveX(CarModel.getInstance().mDynamicPt.get(i + 1).lon);
                y2 = MoveY(CarModel.getInstance().mDynamicPt.get(i + 1).lat);
            }
            else
            {
                x2 = MoveX(CarModel.getInstance().mDynamicPt.get(0).lon);
                y2 = MoveY(CarModel.getInstance().mDynamicPt.get(0).lat);
            }
            cas.drawLine((float)x, (float)y, (float)x2, (float)y2, carPaint);

            if (nCurrentZoomLevel>4)
            {
                cas.drawCircle(x, y, nCircleRadius, carPaint);
                cas.drawLine((float)(x - nCircleRadius), (float)(y), (float)(x + nCircleRadius), (float)y, carPaint);
                cas.drawLine((float)x, (float)(y - nCircleRadius), (float)x, (float)(y + nCircleRadius), carPaint);
                cas.drawText(Integer.toString(i+1), x - nCircleRadius/2, y + nCircleRadius/2, carPaint);
            }
        }
        for(int i = 0; i < mfrontWheelIndexList.size() ;i++)
        {
            x = MoveX(CarModel.getInstance().mDynamicPt.get(mfrontWheelIndexList.get(i)).lon);
            y = MoveY(CarModel.getInstance().mDynamicPt.get(mfrontWheelIndexList.get(i)).lat);
            if(i < (mfrontWheelIndexList.size() - 1)) {
                x2 = MoveX(CarModel.getInstance().mDynamicPt.get(mfrontWheelIndexList.get(i + 1)).lon);
                y2 = MoveY(CarModel.getInstance().mDynamicPt.get(mfrontWheelIndexList.get(i + 1)).lat);

                cas.drawLine((float)x, (float)y, (float)x2, (float)y2, carPaint);
            }

            if (nCurrentZoomLevel>4)
            {
                cas.drawCircle(x, y, nCircleRadius, carPaint);
                cas.drawLine((float)(x - nCircleRadius), (float)(y), (float)(x + nCircleRadius), (float)y, carPaint);
                cas.drawLine((float)x, (float)(y - nCircleRadius), (float)x, (float)(y + nCircleRadius), carPaint);
                cas.drawText(Integer.toString(mfrontWheelIndexList.get(i)+1), x - nCircleRadius/2, y + nCircleRadius/2, carPaint);
            }
        }

        for(int i = 0; i < mBackWheelIndexList.size() ;i++) {
            x = MoveX(CarModel.getInstance().mDynamicPt.get(mBackWheelIndexList.get(i)).lon);
            y = MoveY(CarModel.getInstance().mDynamicPt.get(mBackWheelIndexList.get(i)).lat);
            if (i < (mBackWheelIndexList.size() - 1)) {
                x2 = MoveX(CarModel.getInstance().mDynamicPt.get(mBackWheelIndexList.get(i + 1)).lon);
                y2 = MoveY(CarModel.getInstance().mDynamicPt.get(mBackWheelIndexList.get(i + 1)).lat);

                cas.drawLine((float) x, (float) y, (float) x2, (float) y2, carPaint);
            }

            if (nCurrentZoomLevel>4)
            {
                cas.drawCircle(x, y, nCircleRadius, carPaint);
                cas.drawLine((float)(x - nCircleRadius), (float)(y), (float)(x + nCircleRadius), (float)y, carPaint);
                cas.drawLine((float)x, (float)(y - nCircleRadius), (float)x, (float)(y + nCircleRadius), carPaint);
                cas.drawText(Integer.toString(mBackWheelIndexList.get(i)+1), x - nCircleRadius/2, y + nCircleRadius/2, carPaint);
            }
        }

        x = MoveX(CarModel.getInstance().mDynamicPt.get(0).lon);
        y = MoveY(CarModel.getInstance().mDynamicPt.get(0).lat);
        x2 = MoveX(CarModel.getInstance().mDynamicPt.get(12).lon);
        y2 = MoveY(CarModel.getInstance().mDynamicPt.get(12).lat);
        cas.drawLine((float) x, (float) y, (float) x2, (float) y2, carPaint);
    }

    public static int ZoomIn()
    {
        if(nCurrentZoomLevel >= MAP_LEVEL_COUNT-1)
            return 0;

        ++nCurrentZoomLevel;

        return 1;
    }

    public static int ZoomOut()
    {
        if (nCurrentZoomLevel <= 0)
            return 0;

        --nCurrentZoomLevel;

        return 1;
    }
}
