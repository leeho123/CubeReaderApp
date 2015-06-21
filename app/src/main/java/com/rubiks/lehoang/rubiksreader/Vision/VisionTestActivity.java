package com.rubiks.lehoang.rubiksreader.Vision;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.widget.TextView;

import com.rubiks.lehoang.rubiksreader.Vision.CubeFinder;
import com.rubiks.lehoang.rubiksreader.R;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.util.List;

/**
 * Created by LeHoang on 09/06/2015.
 */
public class VisionTestActivity extends Activity {
    TextView progressText;
    TextView succeedTxt;
    TextView failedTxt;

    String[] modAnswers = new String[]{"WOOWBYOWG","RYGROGWBW","ORYGYBGOO","BWWYRRYBB","BYBOWRRBR","YGROGWGGY",
                                    "WWGWBGYRY","OBROYBWOB","OGGRRGWRO","WOBGWWYYR","OBROYBWOB","GBRYGBBOB",
                                    "GYOYBRWGY","GYOYBRWGY","WBOYOBYBR","BWRRGOROO","YBBYWWBOB","YBBBGBOOO",
                                    "GWWYRGGYB","YBBYWWBOB"};

    String[] wellAnswer = new String[]{"WYYWWRYRB","RRROBOORR", "GWYOYGWWW", "OBYOGBOBB", "BRYRWWYYW",
                                       "RROOBORRR","RYBRYROYR", "RYROWRGGY", "GWWYWGBOG", "YWWBRYWBB",
                                       "YORWGORWG","BBOGBOBBG","BGWBBWGOW", "OYYYRROGY", "GWWOYRRYR", "YRYYRGOBW",
                                       "WWOBGOBOG","GWYGOWBGY", "OOBBGBOOG"};

    String[] dimAnswer = new String[]{"BBOROGBBG","YORWGOROG","YORWGOROG","YORWGOROG","BBOROGBBG","BYYOYRBRW","RBOWBOOBG"
                                     ,"GRWGYYYRO","OGBYRBBYR","RBWGOWROB","RYORYRYRO","GOBGWYWWG","GOBGWYWWG","YWWWGOGGY"
                                     ,"YRWYWRBOB","OORGBYWGY","YWWBRYWBB"};

    String[] yellowAnswer = new String[]{"GYOWYYGGB","GRRYBGYOR","GBOOBWOBR","WWGBOORGR","YRWYWRBOB","RYOOWYORW"
                                        ,"ROYWRWBGY","GRGOYBBWR","WBOGOBBYY","RYOOWYORW","WYWGGRGBB","RWBBYOGRG"
                                        ,"YYBBOGOBW","YRGGBWOOR","ROYWRWBGY","RWOBYYOOR","GORGRWGGO","OORRWYBRB","WGYGGOWYW","GGGGROOWG"};

    String[] blueAnswer = new String[]{"ROOYYBOWB","YOYGGWWGY","GOGGRWGGO","WRWYBBYRW","BRORWOBYR","YWOGYYOOB",
                                       "GGBWGROOW","GBBROYWRR","BWYWBGYBG","GRWBRGROO","RORYWYYBW","RRWYORBBG",
                                       "OYBWYOYGO","BRYGGOGWO","WBWRBRWYY","YYRBWOWYR","BYRBORGRW","YGGWBBBWY",
                                       "WGORROGBR","BOOYYGOWY"};
    int failed = 0;
    int succeed = 0;

    @Override
    protected void onCreate(Bundle saved){
        super.onCreate(saved);
        setContentView(R.layout.activity_vision);
        progressText = (TextView) findViewById(R.id.progressTxt);
        succeedTxt = (TextView) findViewById(R.id.succeedTxt);
        failedTxt = (TextView) findViewById(R.id.failedTxt);

        progressText.setText("Loading...");
        update();

    }

    private void update(){
        failedTxt.setText("Failed: "+ failed);
        succeedTxt.setText("Success: "+ succeed);
    }
    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            if (status == LoaderCallbackInterface.SUCCESS ) {
                // now we can call opencv code !
                progressText.setText("Loaded OpenCV");
                Thread thread = new Thread(new Runnable(){

                    @Override
                    public void run() {
                        //testYellowLight();
                        //testLowLight();
                        //testWellLit();

                        //testModerateWhiteImages();
                        testBlueLight();
                    }
                });

                thread.start();

            } else {
                super.onManagerConnected(status);
            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_2_4_6,this, mLoaderCallback);
    }


    public void testLowLight(){
        testImages("VisionTest/dim", 17, dimAnswer);
    }

    public void testYellowLight(){
        testImages("VisionTest/dimyellow", 20, yellowAnswer);
    }

    public void testBlueLight(){
        testImages("VisionTest/dimblue", 20, blueAnswer);
    }

    public void testImages(final String folderPath, int numTests, String[] answers){
        File root = Environment.getExternalStorageDirectory();
        WhiteBalancer balancer = new WhiteBalancer(6, WhiteBalancer.MAX_NORMALISER);
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                update();
                progressText.setText("Testing " + folderPath);
            }
        });
        for(int i = 1; i <= numTests; i++) {
            final File file = new File(root, folderPath+"/test"+i+".jpg");


            if(file.exists()) {
                Mat image = Highgui.imread(file.getAbsolutePath());
                Imgproc.cvtColor(image, image, Imgproc.COLOR_BGR2RGBA);
                Mat imageGray = new Mat();
                Imgproc.cvtColor(image, imageGray, Imgproc.COLOR_RGBA2GRAY);
                imageGray.convertTo(imageGray, CvType.CV_8UC1);

                List<Rect> squares = CubeFinder.findStickers(imageGray);

                Mat rgb = balancer.whiteBalance(image);

                if (squares.size() == 9) {
                    final String ans = ColourExtractor.extract(rgb, squares);

                    for(int j = 0; j < ans.length(); j++){
                        if(ans.charAt(j) != answers[i-1].charAt(j)){
                            failed++;
                        }else{
                            succeed++;
                        }
                    }
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            update();
                            progressText.append("\n" + ans);
                        }
                    });

                } else {
                    failed+=9;
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            update();
                            progressText.append("\nCouldn't find cube " + file.getAbsolutePath());
                        }
                    });

                }
            }

        }
    }

    public void testWellLit(){
        testImages("VisionTest/whitelight", 19, wellAnswer);

    }
    public void testModerateWhiteImages(){
        testImages("VisionTest/moderatewhite", 20, modAnswers);

    }
}
