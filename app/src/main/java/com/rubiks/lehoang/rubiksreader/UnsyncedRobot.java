package com.rubiks.lehoang.rubiksreader;

import android.util.Log;import java.lang.Exception;

/**
 * Created by LeHoang on 14/05/2015.
 */
public class UnsyncedRobot {
    enum Move{
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
        arms[RIGHTLEFT].reset();
        arms[FRONTBACK].reset();
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

                flipped = !flipped;


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
                flipped = !flipped;

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
}
