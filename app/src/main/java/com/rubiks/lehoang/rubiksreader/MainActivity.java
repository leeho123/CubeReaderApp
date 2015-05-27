package com.rubiks.lehoang.rubiksreader;

import android.app.Activity;
import android.os.AsyncTask;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.ToggleButton;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.features2d.Features2d;
import org.opencv.highgui.VideoCapture;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutionException;


public class MainActivity extends Activity implements CameraBridgeViewBase.CvCameraViewListener2 {
    private CustomCameraView mOpenCvCameraView;
    Button nextBtn;
    Button doneBtn;
    Boolean isNext = Boolean.FALSE;
    Button solveBtn;
    CubeSolver solver;
    ToggleButton wbBtn;
    String solution;


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
                final String cube = cubeBuilder.buildCube();
                Log.e("com.rubiks.lehoang.rubiksreader", cube);
                try {
                    solver = new CubeSolver();
                    Thread newThread = new Thread(new Runnable(){

                        @Override
                        public void run() {
                            solution = solver.solve(cube);
                            Log.e("com.rubiks.lehoang.rubiksreader", "Solution: " + solution);
                            solveBtn.setVisibility(View.VISIBLE);
                        }
                    });
                    newThread.start();

                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });


        solveBtn  = (Button) findViewById(R.id.solveBtn);
        solveBtn.setVisibility(View.GONE);
        solveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                solver.robotSolve(solution);
            }
        });

        wbBtn = (ToggleButton) findViewById(R.id.togglebutton);
        wbBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });




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
                    mat= new Mat();
                    mat2=new Mat();
                    hierarchy = new Mat();
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

    public double norm(double[] vals, double p){
        double sum = 0;
        for(double val : vals){
            sum += Math.pow(val,p);
        }
        sum = Math.pow(sum, 1/p);
        return sum;
    }
    public Mat whiteBalance(Mat input){
        Scalar mean = Core.mean(input);

        double[] meanVal = Arrays.copyOfRange(mean.val, 0, 3);

        //Average
        double avg = norm(meanVal, 2);

        Scalar coeffs = new Scalar(meanVal[0]/avg, meanVal[1]/avg, meanVal[2]/avg, 1);
        Core.divide(input, coeffs, input);

        return input;
    }

    String ans;
    Mat mat;
    Mat mat2;
    CubeBuilder cubeBuilder = new CubeBuilder();
    Mat gray;
    //Assuming first picture we take is top face
    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        if (!isNext) {
            //White balance


            Mat element10 = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(20, 20));
            Mat kernel = Imgproc.getStructuringElement(Imgproc.MORPH_ELLIPSE, new Size(9, 9));

            inputFrame.gray().convertTo(mat2, -1, 1, -45);
            mat2.convertTo(mat2, -1, 25, 0);

            Imgproc.GaussianBlur(mat2, mat2, new Size(7,7), 0);

            Imgproc.Laplacian(mat2, mat, CvType.CV_8U,3,1,0,Imgproc.BORDER_DEFAULT);

            Core.convertScaleAbs(mat, mat, 10, 0);
            Imgproc.morphologyEx(mat2, mat2, Imgproc.MORPH_CLOSE, kernel);

            Imgproc.dilate(mat, mat, element10);
            Imgproc.adaptiveThreshold(mat, mat, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY_INV, 15, 4);

            Core.bitwise_not(mat, mat);


            Mat rgb;

            if(wbBtn.isChecked()){
                rgb = whiteBalance(inputFrame.rgba());
            }else{
                rgb = inputFrame.rgba();
            }

            //might need RGB
            List<Rect> squares = findSquares(mat);

            int innerBoxHeight;
            int innerBoxWidth;
            int innerBoxTop;
            int innerBoxLeft;

            for (int i = 0; i < squares.size(); i++) {
                Rect r = squares.get(i);
                Point centroid = Helper.getCentroid(r);
                innerBoxHeight = 2 * r.height/3;
                innerBoxWidth = 2 * r.width/3;
                innerBoxTop = (int) centroid.y - innerBoxHeight/2;
                innerBoxLeft = (int) centroid.x - innerBoxWidth/2;
                Rect rect = new Rect(innerBoxLeft, innerBoxTop, innerBoxWidth, innerBoxHeight);
                Core.rectangle(rgb, new Point(rect.x, rect.y), new Point(rect.x+rect.width, rect.y + rect.height), new Scalar(255,0,0,255), 3);
                //Draw rectangles on screens
                Core.rectangle(rgb, new Point(r.x, r.y), new Point(r.x + r.width, r.y + r.height), new Scalar(255, 0, 0, 255), 3);
                Core.putText(rgb, Integer.toString(i), Helper.getCentroid(r), 0 , 1, new Scalar(0,0,0), 5);
            }

            if (squares.size() == 9) {

                isNext = true;
                ProcessFace face = new ProcessFace(inputFrame.rgba());
                face.execute(squares);
                try {
                    ans = face.get();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                }
                //Start processing

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
                    }
                });
            }


            Mat mat3 = inputFrame.rgba();

            Core.putText(mat3, "turn cube and click next", new Point(100, 100), 0, 1, new Scalar(255, 0, 0), 4);
            Core.putText(mat3, ans, new Point(400, 400), 0, 1, new Scalar(255, 0, 0), 4);
            Log.d("com.rubiks.lehoang.rubiksreader", ans);

            /*
            for(int i = 0; i < contours.size(); i++){
                Imgproc.drawContours(mat3, contours,i, new Scalar(0,0,0), 3);

            }*/

            for(int i = 0; i < squares.size(); i++){
                Rect r = squares.get(i);
                Point centroid = Helper.getCentroid(r);
                int innerBoxHeight = 2 * r.height/3;
                int innerBoxWidth = 2 * r.width/3;
                int innerBoxTop = (int) centroid.y - innerBoxHeight/2;
                int innerBoxLeft = (int) centroid.x - innerBoxWidth/2;
                Rect rect = new Rect();
                rect.x = innerBoxLeft;
                rect.y = innerBoxTop;
                rect.height = innerBoxHeight;
                rect.width = innerBoxWidth;
                Core.rectangle(mat3, new Point(rect.x, rect.y), new Point(rect.x+rect.width, rect.y + rect.height), new Scalar(255,0,0,255), 3);
                //Draw rectangles on screens
                Core.rectangle(mat3, new Point(r.x, r.y), new Point(r.x + r.width, r.y + r.height), new Scalar(255, 0, 0, 255), 3);
                Core.putText(mat3, Integer.toString(i), Helper.getCentroid(r), 0 , 1, new Scalar(255,0,0), 5);

            }

            return mat3;
        }
    }



    class ProcessFace extends AsyncTask<List<Rect>,Void,String>{
        Mat origRGB;

        ProcessFace(Mat origRGB){
            this.origRGB = origRGB;
        }

        @Override
        protected String doInBackground(List<Rect>... params) {
            Imgproc.GaussianBlur(origRGB, origRGB, new Size(31,31), 0);
            List<Rect> squares = params[0];



            StringBuilder result = new StringBuilder();

            //Extract colour
            int innerBoxHeight;
            int innerBoxWidth;
            int innerBoxTop;
            int innerBoxLeft;

            for(Rect r : squares){
                Point centroid = Helper.getCentroid(r);

                //Look for inner
                innerBoxHeight = 2 * r.height/3 ;
                innerBoxWidth = 2 * r.width/3;
                innerBoxTop = (int) (centroid.y - innerBoxHeight/2);
                innerBoxLeft = (int) (centroid.x - innerBoxWidth/2);

                Mat subframe = origRGB.submat(innerBoxTop, innerBoxTop+innerBoxHeight, innerBoxLeft, innerBoxLeft+innerBoxWidth);
                Imgproc.cvtColor(subframe, subframe, Imgproc.COLOR_RGB2HSV);
                Scalar mean = Core.mean(subframe);
                Log.e("com.rubiks.lehoang.rubiksreader", mean.toString());
                result.append(scalarToColour(mean));
            }


            return result.toString();
        }



        @Override
        protected void onPostExecute(String result){
           cubeBuilder.add(result);

        }
    }

    private FaceColour scalarToColour(Scalar hsvSc){
        int hMax = 179;
        int sMax = 255;
        int vMax = 255;

        double[] hsv = hsvSc.val;

        if      (hsv[1] < 0.3 * sMax && hsv[2] > 0.3 * vMax) return FaceColour.W;
        else {
            double deg = hsv[0];
            int shift = 0;
            //use phase shift?


            if (deg >= 0 && deg < 5) return FaceColour.R;
            else if (deg >= 5 && deg < 20) return FaceColour.O;
            else if (deg >= 20 && deg < 45) return FaceColour.Y;
            else if (deg >= 45 && deg < 90) return FaceColour.G;
            else if (deg >= 90 && deg < 120) return FaceColour.B;
            else if (deg >= 120 && deg < 180) return FaceColour.R;
            else return null;
        }
    }



    List<Rect> squares = new ArrayList<>();
    List<MatOfPoint> contours= new ArrayList<>();
    Mat hierarchy;

    double maxAreaDiff = 0.2;

    private List<Rect> findSquares(Mat mIntermediateMat2) {
        squares.clear();
        contours.clear();

        //Find all contours of image
        Imgproc.findContours(mIntermediateMat2.clone(), contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

        for( int i = 0; i < contours.size(); i++ ) {
            Rect boundingRect = Imgproc.boundingRect(contours.get(i));

            //Test for a threshold
            if (Math.abs(boundingRect.height - boundingRect.width) < 20 &&
                    Math.abs(boundingRect.area() - Imgproc.contourArea(contours.get(i))) < 2500 &&
                   boundingRect.area() > 60*60 && boundingRect.area() < 400 * 400) {

                squares.add(boundingRect);
            }

        }


        if(squares.isEmpty()){
            return squares;
        }

        Collections.sort(squares, new Comparator<Rect>() {
            @Override
            public int compare(Rect lhs, Rect rhs) {
                return (int) (lhs.area() - rhs.area());
            }
        });

        int middle = squares.size()/2;

        double median = squares.get(middle).area();

        for (Iterator<Rect> iterator = squares.iterator(); iterator.hasNext();) {
            Rect rect = iterator.next();
            if (!isWithinRange(median, rect.area(), maxAreaDiff)) {
                // Remove the current element from the iterator and the list.
                iterator.remove();
            }
        }



        //Sort the params by centre coordinate row across
        Collections.sort(squares, new Vector.SortRowWise());

        Rect prev = null;
        //Get rid of duplicates
        for (Iterator<Rect> iterator = squares.iterator(); iterator.hasNext();) {
            Rect rect = iterator.next();
            if(prev != null) {

                if (Vector.isSimilarCoordinate(Helper.getCentroid(rect), Helper.getCentroid(prev))) {
                    // Remove the current element from the iterator and the list.
                    iterator.remove();
                }
            }
            prev = rect;
        }

        if(!squares.isEmpty() && squares.size() < 9){
            //attempt to recover other faces

            recoverEachRow(squares);

        }


        return squares;
    }

    public void recoverEachRow(List<Rect> squares){
        int minX = Integer.MAX_VALUE;
        int maxX = 0;


        int minY = Integer.MAX_VALUE;
        int maxY = 0;

        for(int i = 0; i < squares.size(); i++){
            Rect square = squares.get(i);

            if(square.x < minX){
                minX = square.x;
            }

            if(square.x > maxX){
                maxX = square.x;
            }

            if(square.y > maxY){
                maxY = square.y;
            }

            if(square.y < minY){
                minY = square.y;
            }
        }

        Log.e("com.rubiks.lehoang.rubiksreader", "MinX:" + minX + " MaxX:" + maxX + " MinY:" + minY + " MaxY:" + maxY);
        Log.e("com.rubiks.lehoang.rubiksreader", Arrays.toString(squares.toArray()));
        Log.e("com.rubiks.lehoang.rubiksreader", "Height is: " + squares.get(0).height*2);
        Log.e("com.rubiks.lehoang.rubiksreader", "Width is: " + squares.get(0).width*2);

        //Make sure that we have minmax X and minmaxY values
        if(isWithinRange(maxY - minY,squares.get(0).height*3, Vector.diffFrac) &&
            isWithinRange(maxX - minX, squares.get(0).width*3, Vector.diffFrac)) {

            Log.e("com.rubiks.lehoang.rubiksreader", "We have what we need");

            int[] rowIndexes = {Integer.MAX_VALUE, Integer.MAX_VALUE, Integer.MAX_VALUE};

            for(int i = squares.size() -1 ; i >= 0; i--){
                Rect square = squares.get(i);

                if(isWithinRange(square.y, minY, Vector.diffFrac)){
                    //first row
                    if(rowIndexes[0] > i){
                        Log.e("com.rubiks.lehoang.rubiksreader", "Is within range row 1");

                        rowIndexes[0] = i;
                    }
                }

                if(isWithinRange(square.y, minY + (maxY-minY)/2, Vector.diffFrac)){
                    //middle row
                    if(rowIndexes[1] > i){
                        Log.e("com.rubiks.lehoang.rubiksreader", "Is within range row 2");

                        rowIndexes[1] = i;
                        rowIndexes[0] = i;
                    }
                }

                if(isWithinRange(square.y, maxY, Vector.diffFrac)){
                    //bottom row
                    if(rowIndexes[2] > i){
                        Log.e("com.rubiks.lehoang.rubiksreader", "Is within range row 3");

                        rowIndexes[2] = i;
                        rowIndexes[1] = i;
                        rowIndexes[0] = i;
                    }
                }
            }

            Log.e("com.rubiks.lehoang.rubiksreader", Arrays.toString(rowIndexes));

            List<Rect> top = getMissingRect(new ArrayList<Rect>(squares.subList(rowIndexes[0], rowIndexes[1])), minX, maxX, minY);

            List<Rect> middle = getMissingRect(new ArrayList<Rect>(squares.subList(rowIndexes[1], rowIndexes[2])), minX, maxX, minY + (maxY - minY)/2);

            List<Rect> bottom = getMissingRect(new ArrayList<Rect>(squares.subList(rowIndexes[2], squares.size())), minX, maxX, maxY);

            for(Rect square: squares) {
                Log.e("com.rubiks.lehoang.rubiksreader","Before:");
                Log.e("com.rubiks.lehoang.rubiksreader", Helper.getCentroid(square).toString());
            }

            squares.clear();
            squares.addAll(top);
            squares.addAll(middle);
            squares.addAll(bottom);

            for(Rect square: squares) {
                Log.e("com.rubiks.lehoang.rubiksreader","After:");
                Log.e("com.rubiks.lehoang.rubiksreader", Helper.getCentroid(square).toString());
            }
        }
    }

    public List<Rect> getMissingRect(List<Rect> squares, int minX, int maxX, int yVal){
        if(squares.size() == 3){
            return squares;
        }else if(squares.size() == 2){
            //Need to find out if the square is missing from the middle or the left or right
            if(isWithinRange(squares.get(0).x, minX, Vector.diffFrac)){
                if(isWithinRange(squares.get(1).x, maxX, Vector.diffFrac)){
                    //Must be missing in middle
                    int newX = minX + ((maxX - minX)/2);
                    Rect newRect = new Rect(newX, yVal,squares.get(0).width,squares.get(0).height);
                    squares.add(1, newRect);
                }else{
                    //Must be missing on the right
                    Rect newRect = new Rect(maxX, yVal,squares.get(0).width,squares.get(0).height);
                    squares.add(newRect);
                }
            }else{
                Rect newRect = new Rect(minX,yVal,squares.get(0).width,squares.get(0).height);
                squares.add(0, newRect);
                //Must be missing on the left
            }
        }else if(squares.size() == 1){
            if(isWithinRange(squares.get(0).x, minX, Vector.diffFrac)){
                //Two missing on the right
                int newX1 = minX + ((maxX - minX)/2);
                Rect newRect1 = new Rect(newX1, yVal,squares.get(0).width,squares.get(0).height);
                squares.add(1, newRect1);

                int newX2 = maxX;
                Rect newRect2 = new Rect(newX2, yVal,squares.get(0).width,squares.get(0).height);
                squares.add(newRect2);

            }else if(isWithinRange(squares.get(0).x, maxX, Vector.diffFrac)){
                //Two missing on the left
                int newX1 = minX;
                Rect newRect1 = new Rect(newX1, yVal,squares.get(0).width,squares.get(0).height);
                squares.add(0, newRect1);

                int newX2 = minX + ((maxX - minX)/2);
                Rect newRect2 = new Rect(newX2, yVal,squares.get(0).width,squares.get(0).height);
                squares.add(1, newRect2);
            }else{
                //Two missing at either side
                int newX1 = minX;
                Rect newRect1 = new Rect(newX1, yVal,squares.get(0).width,squares.get(0).height);
                squares.add(0, newRect1);

                int newX2 = maxX;
                Rect newRect2 = new Rect(newX2, yVal,squares.get(0).width,squares.get(0).height);
                squares.add(newRect2);
            }
        }else if(squares.size() == 0){
            //whole row missing
            int newX1 = minX;
            int width = (maxX - minX)/2;
            Rect newRect1 = new Rect(newX1, yVal,width,width);
            squares.add(newRect1);

            int newX2 = minX + width;
            Rect newRect2 = new Rect(newX2, yVal,width,width);
            squares.add(newRect2);

            int newX3 = maxX;
            Rect newRect3 = new Rect(newX3, yVal,width,width);
            squares.add(newRect3);
        }

        return squares;
    }

    public boolean isWithinRange(double value, double other, double range){
        double diff = Math.abs(value-other);
        return diff < range*value;
    }
}
