package com.rubiks.lehoang.rubiksreader.Vision;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by LeHoang on 09/06/2015.
 */
public class WhiteBalancer {
    public static final int MEAN_NORMALISER = 0;
    public static final int MAX_NORMALISER = 1;

    double norm;
    int normaliser;

    public WhiteBalancer(double norm, int normaliser){
        assert(normaliser == MEAN_NORMALISER || normaliser == MAX_NORMALISER);
        this.norm = norm;
        this.normaliser = normaliser;
    }

    public double norm(double[] vals, double p){
        double sum = 0;
        for(double val : vals){
            sum += Math.pow(val,p);
        }
        sum /= vals.length;
        sum = Math.pow(sum, 1/p);
        return sum;
    }

    public double norm(Mat input, double p){
        Mat mat2 = new Mat();
        Core.pow(input, p, mat2);
        Scalar scalar = Core.sumElems(mat2);

        return Math.pow(scalar.val[0]/(double) input.total(),1/p);
    }
    public Mat whiteBalance(Mat input){

        //Scalar mean = Core.mean(input);
        Mat mat2 = new Mat();
        input.convertTo(mat2, CvType.CV_32FC3);
        List<Mat> channels = new ArrayList<Mat>();
        Core.split(mat2, channels);

        double[] meanVal = {0,0,0};

        for(int i = 0; i < 3; i++) {
            Mat mat = channels.get(i);
            meanVal[i] = norm(mat, norm);
        }

        double normalisingConst=0;
        //Average
        if(normaliser == MAX_NORMALISER){
            normalisingConst = Math.max(meanVal[2], Math.max(meanVal[0], meanVal[1]));
        }else if(normaliser == MEAN_NORMALISER){
            normalisingConst = norm(meanVal, 1);
        }

        Scalar coeffs = new Scalar(normalisingConst/meanVal[0], normalisingConst/meanVal[1], normalisingConst/meanVal[2], 1);
        Core.multiply(mat2, coeffs, mat2);

        mat2.convertTo(mat2, CvType.CV_8UC3);
        return mat2;
    }
}
