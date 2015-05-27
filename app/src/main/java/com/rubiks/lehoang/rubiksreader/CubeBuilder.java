package com.rubiks.lehoang.rubiksreader;

import org.opencv.core.MatOfPoint;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by LeHoang on 22/04/2015.
 */
public class CubeBuilder {
    public static final Map<FaceColour,Character> faceToColourMap = Collections.synchronizedMap(new HashMap<FaceColour, Character>());
    static{
        faceToColourMap.put(FaceColour.B, 'U');
        faceToColourMap.put(FaceColour.G, 'D');
        faceToColourMap.put(FaceColour.W, 'F');
        faceToColourMap.put(FaceColour.Y, 'B');
        faceToColourMap.put(FaceColour.O, 'L');
        faceToColourMap.put(FaceColour.R, 'R');
    }


    String[] cube = new String[6];

    // Given a list of colours,
    public void add(String colours){
        //get middle colour
        char middle = colours.charAt(4);

        FaceColour col = FaceColour.valueOf(Character.toString(middle));

        for(FaceColour faceCol : faceToColourMap.keySet()){
            char faceColCh = faceCol.name().charAt(0);
            colours = colours.replace(faceColCh, faceToColourMap.get(faceCol));
        }
        cube[col.ordinal()] = colours;
    }

    public boolean isBuildable(){
        for(String face : cube){
            if(face == null || face.isEmpty()){
                return false;
            }
        }
        return true;
    }

    public String buildCube(){
        StringBuilder builder = new StringBuilder();

        for(String face : cube){
            builder.append(face);
        }

        return builder.toString();
    }


}
