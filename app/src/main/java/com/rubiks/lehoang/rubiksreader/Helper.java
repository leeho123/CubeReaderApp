package com.rubiks.lehoang.rubiksreader;

import org.opencv.core.Point;
import org.opencv.core.Rect;

/**
 * Created by LeHoang on 25/04/2015.
 */
public class Helper {
    public static Point getCentroid(Rect rect){
        return new Point(rect.x + rect.width/2, rect.y + rect.height/2);
    }
}
