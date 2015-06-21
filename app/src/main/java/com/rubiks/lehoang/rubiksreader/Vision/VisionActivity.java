package com.rubiks.lehoang.rubiksreader.Vision;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ToggleButton;

import com.rubiks.lehoang.rubiksreader.Solver.CubeBuilder;
import com.rubiks.lehoang.rubiksreader.Solver.CubeSolver;
import com.rubiks.lehoang.rubiksreader.Helper;
import com.rubiks.lehoang.rubiksreader.R;

import org.kociemba.twophase.Tools;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;

import java.util.List;


public class VisionActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2 {
    private CustomCameraView mOpenCvCameraView;
    Button nextBtn;
    Button doneBtn;
    Boolean isNext = Boolean.FALSE;
    Button tryAgainBtn;
    CubeSolver solver;
    ToggleButton wbBtn;
    String solution;
    ToggleButton flashToggle;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i("com.rubiks.reader", "called onCreate");
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);
        mOpenCvCameraView = (CustomCameraView) findViewById(R.id.HelloOpenCvView);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
        nextBtn = (Button) findViewById(R.id.nextBtn);
        nextBtn.setVisibility(View.GONE);
        nextBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                    nextBtn.setVisibility(View.GONE);
                    isNext = false;

            }
        });

        doneBtn = (Button) findViewById(R.id.doneBtn);
        doneBtn.setVisibility(View.GONE);


        doneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doneBtn.setVisibility(View.GONE);
                finish();
            }
        });


        tryAgainBtn  = (Button) findViewById(R.id.tryAgain);
        tryAgainBtn.setVisibility(View.GONE);
        tryAgainBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doneBtn.setVisibility(View.GONE);
                nextBtn.setVisibility(View.GONE);
                isNext = false;
            }
        });
        tryAgainBtn.setVisibility(View.GONE);

        wbBtn = (ToggleButton) findViewById(R.id.togglebutton);
        wbBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

    }

    /**
     * Thanks to Kociemba for this method. verify()
     *          0: Cube is solvable<br>
     *         -1: There is not exactly one facelet of each colour<br>
     *         -2: Not all 12 edges exist exactly once<br>
     *         -3: Flip error: One edge has to be flipped<br>
     *         -4: Not all 8 corners exist exactly once<br>
     *         -5: Twist error: One corner has to be twisted<br>
     *         -6: Parity error: Two corners or two edges have to be exchanged
     */

    @Override
    public void finish(){

        String state = cubeBuilder.buildCube();
        int isValid = Tools.verify(state);
        if(isValid == 0) {
            Intent result = new Intent();
            result.putExtra("cubestate", state);

            setResult(RESULT_OK, result);
        }else{
            Intent error = new Intent();
            error.putExtra("cubestate",getErrorMessage(isValid) + " " +state);
            setResult(RESULT_CANCELED, error);
        }

        super.finish();
    }

    public String getErrorMessage(int state){
        switch(state){
            case -1: return "There is not exactly one facelet of each colour";
            case -2: return "Not all 12 edges exists exactly once";
            case -3: return "One edge has to be flipped";
            case -4: return "Not all 8 corners exist exactly once";
            case -5: return "One corner has to be twisted";
            case -6: return "Two corners or two edges have to be exchanged";
            default: return "Unknown Error";
        }
    }
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i("com.rubiks.reader", "OpenCV loaded successfully");
                    mOpenCvCameraView.enableView();
                    gray = new Mat();

                }
                break;
                default: {
                    super.onManagerConnected(status);
                }
                break;
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_6, this, mLoaderCallback);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onCameraViewStarted(int width, int height) {

    }

    @Override
    public void onCameraViewStopped() {

    }



    List<Rect> squares;
    String ans;
    CubeBuilder cubeBuilder = new CubeBuilder();
    Mat gray;
    WhiteBalancer balancer = new WhiteBalancer(6, WhiteBalancer.MAX_NORMALISER);
    //Assuming first picture we take is top face
    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        if (!isNext) {
            //might need RGB
            squares = CubeFinder.findStickers(inputFrame.gray());

            Mat rgb;

            if(wbBtn.isChecked()){
                rgb = balancer.whiteBalance(inputFrame.rgba());
            }else{
                rgb = inputFrame.rgba();
            }

            if (squares.size() == 9) {

                isNext = true;
                ans = ColourExtractor.extract(rgb.clone(), squares);
                cubeBuilder.add(ans);
            }

            for (int i = 0; i < squares.size(); i++) {
                Rect r = squares.get(i);
                //Draw rectangles on screens
                Core.rectangle(rgb, new Point(r.x, r.y), new Point(r.x + r.width, r.y + r.height), new Scalar(255, 0, 0, 255), 3);
                Core.putText(rgb, Integer.toString(i), Helper.getCentroid(r), 0 , 1, new Scalar(0,0,0), 5);
            }
            return rgb;
        } else {

            if(!cubeBuilder.isBuildable()) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        nextBtn.setVisibility(View.VISIBLE);
                    }
                });
            }else{
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        doneBtn.setVisibility(View.VISIBLE);
                        tryAgainBtn.setVisibility(View.VISIBLE);
                    }
                });
            }

            Mat mat3 = inputFrame.rgba();

            Log.d("com.rubiks.lehoang.rubiksreader", ans);

            for(int i = 0; i < squares.size(); i++){
                Rect r = squares.get(i);
                //Draw rectangles on screens
                Core.rectangle(mat3, new Point(r.x, r.y), new Point(r.x + r.width, r.y + r.height), FaceColour.getScalarFromCol(ans.charAt(i)), Core.FILLED);
            }

            return mat3;
        }
    }













}
