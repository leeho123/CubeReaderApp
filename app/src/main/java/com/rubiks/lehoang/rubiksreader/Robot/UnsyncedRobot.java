package com.rubiks.lehoang.rubiksreader.Robot;

import android.util.Log;

import com.rubiks.lehoang.rubiksreader.Solver.CubeSolver;
import com.rubiks.lehoang.rubiksreader.Helper;

import java.lang.Exception;
import java.util.Arrays;
import java.util.List;

/**
 * Created by LeHoang on 14/05/2015.
 */
public class UnsyncedRobot {
    public static enum Move{
        F,R,U,B,L,D,F2,R2,U2,B2,L2,D2,F3,R3,U3,B3,L3,D3,RL,RL3;
    }
    public static final int RIGHTLEFT = 0;
    public static final int FRONTBACK = 1;

    UnsyncedArms[] arms;

    boolean flipped = false;


    public UnsyncedRobot() throws Exception {
        arms = new UnsyncedArms[2];
        arms[RIGHTLEFT] = new UnsyncedArms(BluetoothConnector.slaveFAddress, BluetoothConnector.slaveBAddress);
        arms[FRONTBACK] = new UnsyncedArms(BluetoothConnector.slaveRAddress, BluetoothConnector.slaveLAddress);
    }

    public void finish(){
        flipped = false;
        arms[RIGHTLEFT].reset();
        arms[FRONTBACK].reset();
    }

    public long[][] profile(){
        clampAll();
        Move[] movesToProfileNorm = new Move[]{Move.F, Move.R, Move.B, Move.L,
                                            Move.F2, Move.R2, Move.B2, Move.L2,
                                              Move.F3, Move.R3, Move.B3, Move.L3};
        Move[] movesToProfileFlip = new Move[]{Move.D, Move.U,Move.D2, Move.U2,Move.D3, Move.U3};

        long[][] costs = new long[2][movesToProfileNorm.length + movesToProfileFlip.length];

        long before;
        long after;

        for(Move move : movesToProfileNorm){
            before = System.currentTimeMillis();
            move(move);
            after = System.currentTimeMillis();

            costs[0][move.ordinal()] = after - before;
        }

        for(Move move : movesToProfileNorm){
            if(!flipped) {
                move(Move.RL);
            }
            before = System.currentTimeMillis();
            move(move);
            after = System.currentTimeMillis();

            costs[1][move.ordinal()] = after - before;
        }

        if(flipped){
           move(Move.RL3);
        }

        for(Move move: movesToProfileFlip){
            before = System.currentTimeMillis();
            move(move);
            after = System.currentTimeMillis();

            costs[0][move.ordinal()] = after - before;
            move(Move.RL3);
        }

        move(Move.RL);
        for(Move move: movesToProfileFlip){
            before = System.currentTimeMillis();
            move(move);
            after = System.currentTimeMillis();

            costs[1][move.ordinal()] = after - before;
        }

        finish();


        normalise(costs);

        return costs;
    }

    public static void normalise(long[][] costs){
        long min0 = Helper.min(costs[0]);
        long min1 = Helper.min(costs[1]);

        double min = min0 < min1 ? min0 : min1;

        System.out.println(min);

        for(int i = 0; i< costs[0].length; i++){
            costs[0][i] = Math.round(((double) costs[0][i])/min);
            costs[1][i] = Math.round(((double) costs[1][i])/min);
        }
    }
    public void clampAll(){

        BluetoothConnector conn1 = arms[RIGHTLEFT].clampBoth();
        BluetoothConnector conn2 = arms[FRONTBACK].clampBoth();

        conn1.readMessage();
        conn2.readMessage();
    }

    public void unclampAll(){
        BluetoothConnector conn1 = arms[RIGHTLEFT].unclampBoth();
        BluetoothConnector conn2 = arms[FRONTBACK].unclampBoth();

        conn1.readMessage();
        conn2.readMessage();
    }

    public void robotSolve(String solution){
        String trans = CubeSolver.transform(solution);
        List<Move> moves = CubeSolver.parseSolution(trans);
        for(UnsyncedRobot.Move move: moves){
            move(move);
        }
    }
    public void move(Move move){
        Log.d("com.rubiks.lehoang.phonesender", "Performing move: " + move.toString());
        BluetoothConnector sync1 = null;
        switch(move){
            case F:
                if(flipped){
                    move(Move.RL3);
                }
                arms[FRONTBACK].rotateFace90AndRecover(true, UnsyncedArms.FRONT);
                break;
            case R:
                arms[RIGHTLEFT].rotateFace90AndRecover(true, UnsyncedArms.RIGHT);
                break;
            case U:
                if(!flipped){
                    move(Move.RL);
                }
                arms[FRONTBACK].rotateFace90AndRecover(true, UnsyncedArms.BACK);
                break;
            case B:
                if(flipped){
                    move(Move.RL3);
                }
                arms[FRONTBACK].rotateFace90AndRecover(true, UnsyncedArms.BACK);
                break;
            case L:
                arms[RIGHTLEFT].rotateFace90AndRecover(true, UnsyncedArms.LEFT);
                break;
            case D:
                if(!flipped){
                    move(Move.RL);
                }
                arms[FRONTBACK].rotateFace90AndRecover(true, UnsyncedArms.FRONT);
                break;
            case F3:
                if(flipped){
                    move(Move.RL3);
                }
                arms[FRONTBACK].rotateFace90AndRecover(false, UnsyncedArms.FRONT);
                break;
            case R3:
                arms[RIGHTLEFT].rotateFace90AndRecover(false, UnsyncedArms.RIGHT);
                break;
            case U3:
                if(!flipped){
                    move(Move.RL);
                }
                arms[FRONTBACK].rotateFace90AndRecover(false, UnsyncedArms.BACK);
                break;
            case L3:
                arms[RIGHTLEFT].rotateFace90AndRecover(false, UnsyncedArms.LEFT);
                break;
            case B3:
                if(flipped){
                    move(Move.RL3);
                }
                arms[FRONTBACK].rotateFace90AndRecover(false, UnsyncedArms.BACK);
                break;
            case D3:
                if(!flipped){
                    move(Move.RL);
                }
                arms[FRONTBACK].rotateFace90AndRecover(false, UnsyncedArms.FRONT);
                break;
            case RL:
                sync1 = arms[FRONTBACK].unclampBoth();
                sync1.readMessage();

                sync1 = arms[RIGHTLEFT].clockBoth();
                sync1.readMessage();

                sync1 = arms[FRONTBACK].clampBoth();
                sync1.readMessage();

                sync1 = arms[RIGHTLEFT].unclampBoth();
                sync1.readMessage();

                sync1 = arms[RIGHTLEFT].antiBoth();
                sync1.readMessage();

                sync1 = arms[RIGHTLEFT].clampBoth();
                sync1.readMessage();

                flipped = true;


                break;
            case RL3:
                sync1 = arms[FRONTBACK].unclampBoth();
                sync1.readMessage();

                sync1 = arms[RIGHTLEFT].antiBoth();
                sync1.readMessage();

                sync1 = arms[FRONTBACK].clampBoth();
                sync1.readMessage();

                sync1 = arms[RIGHTLEFT].unclampBoth();
                sync1.readMessage();

                sync1 = arms[RIGHTLEFT].clockBoth();
                sync1.readMessage();

                sync1 = arms[RIGHTLEFT].clampBoth();
                sync1.readMessage();
                flipped = false;

                break;
            case R2:
                arms[RIGHTLEFT].rotateFace180AndRecover(UnsyncedArms.RIGHT);
                break;
            case F2:
                if(flipped){
                    move(Move.RL3);
                }
                arms[FRONTBACK].rotateFace180AndRecover(UnsyncedArms.FRONT);
                break;
            case U2:
                if(!flipped){
                    move(Move.RL);
                }
                arms[FRONTBACK].rotateFace180AndRecover(UnsyncedArms.BACK);
                break;
            case L2:
                arms[RIGHTLEFT].rotateFace180AndRecover(UnsyncedArms.LEFT);
                break;
            case B2:
                if(flipped){
                    move(Move.RL3);
                }
                arms[FRONTBACK].rotateFace180AndRecover(UnsyncedArms.BACK);
                break;
            case D2:
                if(!flipped){
                    move(Move.RL);
                }
                arms[FRONTBACK].rotateFace180AndRecover(UnsyncedArms.FRONT);
                break;
            default: break;
        }

    }

    public static void main(String[] args){
        long[][] costs = {{1779, 1814, 4491, 1881, 1683, 1805, 2039, 2279, 5106, 2153, 1965, 4831, 2015, 1919, 4506, 1994, 1819, 4587},
                {5415, 1802, 1806, 4498, 1959, 1959, 4819, 2049, 2028, 4778, 2133, 2062, 4796, 1807, 1771, 4673, 1810, 1825}
                };

        normalise(costs);

        System.out.println(Arrays.toString(costs[0]));
        System.out.println(Arrays.toString(costs[1]));

    }
}
