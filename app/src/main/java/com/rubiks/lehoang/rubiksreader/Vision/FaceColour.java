package com.rubiks.lehoang.rubiksreader.Vision;

import org.opencv.core.Scalar;

/**
 * Created by LeHoang on 22/04/2015.
 */
public enum FaceColour {
    B,R,W,G,O,Y;
    public static final Scalar red = new Scalar(255,0,0,255);
    public static final Scalar orange = new Scalar(255,100,0,255);
    public static final Scalar green = new Scalar(21,171,0,255);
    public static final Scalar blue = new Scalar(0,59,174,255);
    public static final Scalar white = new Scalar(255,255,255,255);
    public static final Scalar yellow = new Scalar(255,242,0,255);

    public static Scalar getScalarFromCol(char col){
        switch(col){
            case 'B': return blue;
            case 'R': return red;
            case 'W': return white;
            case 'G': return green;
            case 'O': return orange;
            case 'Y': return yellow;
        }
        return null;
    }
}
