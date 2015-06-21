package com.rubiks.lehoang.rubiksreader.Vision;

import android.util.Log;

import com.rubiks.lehoang.rubiksreader.Helper;
import com.rubiks.lehoang.rubiksreader.Vision.Vector;

import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Rect;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

/**
 * Created by LeHoang on 08/06/2015.
 */
public class CubeFinder {



    private static final double maxAreaDiff = 0.2;

    private static List<Rect> squares = new ArrayList<>();
    private static List<MatOfPoint> contours= new ArrayList<>();
    private static Mat hierarchy = new Mat();

    public static List<Rect> findStickers(Mat mat){

        Mat mat2 = new Mat();

        Mat size20 = Imgproc.getStructuringElement(Imgproc.MORPH_RECT, new Size(20, 20));

        mat.convertTo(mat2, -1, 1, -45);
        mat2.convertTo(mat2, -1, 25, 0);

        Imgproc.GaussianBlur(mat2, mat2, new Size(7,7), 0);

        Imgproc.Laplacian(mat2, mat2, CvType.CV_8U,3,1,0,Imgproc.BORDER_DEFAULT);

        Core.convertScaleAbs(mat2, mat2, 10, 0);

        Imgproc.dilate(mat2, mat2, size20);

        Imgproc.adaptiveThreshold(mat2, mat2, 255, Imgproc.ADAPTIVE_THRESH_MEAN_C, Imgproc.THRESH_BINARY_INV, 15, 4);

        Core.bitwise_not(mat2, mat2);

        return findSquares(mat2);
    }

    private static List<Rect> findSquares(Mat intMat) {
        squares.clear();
        contours.clear();

        //Find all contours of image
        Imgproc.findContours(intMat.clone(), contours, hierarchy, Imgproc.RETR_LIST, Imgproc.CHAIN_APPROX_SIMPLE);

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

    private static void recoverEachRow(List<Rect> squares){
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

    public static boolean isWithinRange(double value, double other, double range){
        double diff = Math.abs(value-other);
        return diff < range*value;
    }

    private static List<Rect> getMissingRect(List<Rect> squares, int minX, int maxX, int yVal){
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
}


