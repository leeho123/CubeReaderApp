package com.rubiks.lehoang.rubiksreader.Vision;

import com.rubiks.lehoang.rubiksreader.Helper;

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by LeHoang on 09/06/2015.
 */
public class ColourExtractor {

    private static FaceColour scalarToColour(Scalar hsvSc){
        int hMax = 179;
        int sMax = 255;
        int vMax = 255;

        double[] hsv = hsvSc.val;

        if      (hsv[1] < 0.3 * sMax && hsv[2] > 0.3 * vMax) return FaceColour.W;
        else {
            double deg = hsv[0];
            if(deg >=0 && deg < 5) return FaceColour.R;
            else if (deg >= 5 && deg < 20) return FaceColour.O;
            else if (deg >= 20 && deg < 45) return FaceColour.Y;
            else if (deg >= 45 && deg < 90) return FaceColour.G;
            else if (deg >= 90 && deg < 120) return FaceColour.B;
            else if (deg >= 120 && deg < 180) return FaceColour.R;
            else return null;
        }
    }

    public static String extract(Mat origRGB, List<Rect> colLocations){
        Imgproc.GaussianBlur(origRGB, origRGB, new Size(11, 11), 0);



        StringBuilder result = new StringBuilder();

        //Extract colour
        int innerBoxHeight;
        int innerBoxWidth;
        int innerBoxTop;
        int innerBoxLeft;

        for(Rect r : colLocations){
            Point centroid = Helper.getCentroid(r);

            //Look for inner
            innerBoxHeight = 3 * r.height/4 ;
            innerBoxWidth = 3 * r.width/4;
            innerBoxTop = (int) (centroid.y - innerBoxHeight/2);
            innerBoxLeft = (int) (centroid.x - innerBoxWidth/2);

            Mat subframe = origRGB.submat(innerBoxTop, innerBoxTop+innerBoxHeight, innerBoxLeft, innerBoxLeft+innerBoxWidth);

            Imgproc.cvtColor(subframe, subframe, Imgproc.COLOR_RGB2HSV);
            Scalar mean = Core.mean(subframe);

            List<Mat> hsvSplit = new ArrayList<>();
            Core.split(subframe, hsvSplit);

            MatOfInt histSize = new MatOfInt(180);
            MatOfFloat histRange = new MatOfFloat(0f, 180f);

            Mat h_hist = new Mat();
            List<Mat> hOnly = new ArrayList<>();
            hOnly.add(hsvSplit.get(0));

            Imgproc.calcHist(hOnly, new MatOfInt(0), new Mat(),h_hist,histSize,histRange,false);


            double max = 0;
            int maxVal = 0;
            for(int i = 0; i < (int) histSize.get(0,0)[0]; i++){
                double val = h_hist.get(i,0)[0];
                if(val > max){
                    max = val;
                    maxVal = i;
                }
            }

            double[] meanArr = mean.val;
            meanArr[0] = maxVal;

            mean.set(meanArr);
            result.append(scalarToColour(mean));
        }


        return result.toString();
    }


}
