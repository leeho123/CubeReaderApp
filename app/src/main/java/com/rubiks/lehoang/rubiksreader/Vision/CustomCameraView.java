package com.rubiks.lehoang.rubiksreader.Vision;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.SurfaceView;
import android.widget.Button;

import com.rubiks.lehoang.rubiksreader.R;

import org.opencv.android.JavaCameraView;

import java.text.AttributedCharacterIterator;

/**
 * Created by LeHoang on 20/04/2015.
 */
public class CustomCameraView extends JavaCameraView {

    public CustomCameraView(Context context, AttributeSet attSet){
        super(context, attSet);
        setWillNotDraw(false);
        Resources r = getResources();
        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setAlpha(200);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setColor(r.getColor(R.color.primary_material_light));



    }
    public CustomCameraView(Context context, int cameraId) {
        super(context, cameraId);
        setWillNotDraw(false);
        Resources r = getResources();
        linePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        linePaint.setAlpha(200);
        linePaint.setStyle(Paint.Style.STROKE);
        linePaint.setColor(r.getColor(R.color.primary_material_light));
    }


    private Paint linePaint;
/*
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        linePaint.setStrokeWidth(5);

        canvas.drawRect(new Rect(10, 10, 200, 200), linePaint);
        canvas.drawRect(new Rect(200,10, 400 ,200), linePaint);
        canvas.drawRect(new Rect(400,10, 600,200),  linePaint);

        canvas.drawRect(new Rect(10, 200, 200, 400), linePaint);
        canvas.drawRect(new Rect(200,200, 400,400) , linePaint);
        canvas.drawRect(new Rect(400,200, 600, 400), linePaint);

        canvas.drawRect(new Rect(10, 400, 200, 600), linePaint);
        canvas.drawRect(new Rect(200,400, 400, 600), linePaint);
        canvas.drawRect(new Rect(400,400, 600, 600), linePaint);
    }*/

}
