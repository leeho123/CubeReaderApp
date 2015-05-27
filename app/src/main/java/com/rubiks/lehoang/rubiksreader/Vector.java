package com.rubiks.lehoang.rubiksreader;

import org.opencv.core.Point;
import org.opencv.core.Rect;

import java.util.Comparator;

/**
 * Created by LeHoang on 18/05/2015.
 */
public class Vector {

    final static double diffFrac = 0.2;

    public static boolean isSimilarCoordinate(Point a, Point b){
        return isSimilar(a.x, b.x) && isSimilar(a.y, b.y);
    }

    public static boolean isSimilar(double x1, double x2){
        double xDiff = Math.abs(x1 - x2);

        return xDiff < x1 * diffFrac;
    }

    public static class SortRowWise implements Comparator<Rect>{

        @Override
        public int compare(Rect lhs, Rect rhs) {
            Point lhsCentre = Helper.getCentroid(lhs);
            Point rhsCentre = Helper.getCentroid(rhs);

            if (lhsCentre.y < rhsCentre.y) {
                if (rhsCentre.y - lhsCentre.y > diffFrac * lhsCentre.y) {
                    return -1;
                }
            } else if (lhsCentre.y > rhsCentre.y) {
                if (lhsCentre.y - rhsCentre.y > diffFrac * lhsCentre.y) {
                    return 1;
                }
            }

            if (lhsCentre.x < rhsCentre.x) {
                if (rhsCentre.x - lhsCentre.x > diffFrac * lhsCentre.x) {
                    return -1;
                }
            } else if (rhsCentre.x < lhsCentre.x) {
                if (lhsCentre.x - rhsCentre.x > diffFrac * lhsCentre.x) {
                    return 1;
                }
            }

            return 0;
        }
    }

    public static class SortColWise implements Comparator<Rect>{
        @Override
        public int compare(Rect lhs, Rect rhs) {
            Point lhsCentre = Helper.getCentroid(lhs);
            Point rhsCentre = Helper.getCentroid(rhs);

            if (lhsCentre.x < rhsCentre.x) {
                if (rhsCentre.x - lhsCentre.x > diffFrac * lhsCentre.x) {
                    return -1;
                }
            } else if (rhsCentre.x < lhsCentre.x) {
                if (lhsCentre.x - rhsCentre.x > diffFrac * lhsCentre.x) {
                    return 1;
                }
            }


            if (lhsCentre.y < rhsCentre.y) {
                if (rhsCentre.y - lhsCentre.y > diffFrac * lhsCentre.y) {
                    return -1;
                }
            } else if (lhsCentre.y > rhsCentre.y) {
                if (lhsCentre.y - rhsCentre.y > diffFrac * lhsCentre.y) {
                    return 1;
                }
            }

            return 0;
        }
    }

}
