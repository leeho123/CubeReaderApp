package com.rubiks.lehoang.rubiksreader.Robot;

import android.util.Log;

import java.lang.Exception;import java.lang.String;

/**
 * Created by LeHoang on 14/05/2015. Controls 2 arms opposite each other
 */
public class UnsyncedArms {
    BluetoothConnector clamps;
    BluetoothConnector rotates;

    public static final int CLAMP = -1;
    public static final int ROT  = -2;

    public static final int RIGHT = 0;
    public static final int LEFT = 1;
    public static final int FRONT = 0;
    public static final int BACK = 1;


    public static final int CLAMPONE = 0;
    public static final int CLAMPTWO = 1;
    public static final int UNCLAMPONE = 2;
    public static final int UNCLAMPTWO = 3;
    public static final int CLAMPBOTH = 4;
    public static final int UNCLAMPBOTH = 5;

    public static final int CLOCKONE = 6;
    public static final int CLOCKTWO = 7;
    public static final int ANTIONE = 8;
    public static final int ANTITWO = 9;
    public static final int CLOCKBOTH = 10;
    public static final int ANTIBOTH = 11;

    public static final int CLOCK180ONE = 12;
    public static final int CLOCK180TWO = 13;
    public static final int ANTI180ONE = 14;
    public static final int ANTI180TWO  = 15;

    public static final int UNCLAMPHALFONE = 16;
    public static final int UNCLAMPHALFTWO = 17;
    public static final int UNCLAMPHALFBOTH = 18;

    public static final int END_SEQUENCE = -777;

    public void reset(){
        clamps.sendMessage(END_SEQUENCE);
        rotates.sendMessage(END_SEQUENCE);
        clamps.readMessage();
        rotates.readMessage();
    }

    public UnsyncedArms(String clamp, String rotate) throws Exception {
        clamps = new BluetoothConnector(clamp);
        rotates = new BluetoothConnector(rotate);
        clamps.setState(true);
        rotates.setState(true);
        if(!clamps.connect()){
            throw new Exception("Cannot connect to clamp NXT");
        }
        if(!rotates.connect()){
            throw new Exception("Cannot connect to rotation NXT");
        }
        clamps.sendMessage(CLAMP);
        rotates.sendMessage(ROT);
    }

    public BluetoothConnector clamp(int arm){
        clamps.sendMessage(CLAMPONE + arm);
        return clamps;
    }

    public BluetoothConnector unclamp(int arm){
        clamps.sendMessage(UNCLAMPONE + arm);
        return clamps;
    }

    public BluetoothConnector clampBoth(){
        clamps.sendMessage(CLAMPBOTH);
        return clamps;
    }

    public BluetoothConnector unclampBoth(){
        clamps.sendMessage(UNCLAMPBOTH);
        return clamps;
    }

    public BluetoothConnector rotateClock(int arm){
        Log.d("com.rubiks.lehoang.phonesender","Rotating clockwise");
        rotates.sendMessage(CLOCKONE + arm);
        return rotates;
    }

    public BluetoothConnector rotateAnti(int arm){
        rotates.sendMessage(ANTIONE + arm);
        return rotates;
    }

    public BluetoothConnector clockBoth(){
        rotates.sendMessage(CLOCKBOTH);
        return rotates;
    }

    public BluetoothConnector antiBoth(){
        rotates.sendMessage(ANTIBOTH);
        return rotates;
    }

    public void rotateFace180AndRecover(int arm){

        BluetoothConnector conn;

        rotates.sendMessage(CLOCK180ONE + arm);
        rotates.readMessage();

        conn = unclamp(arm);
        conn.readMessage();

        rotates.sendMessage(ANTI180ONE + arm);
        rotates.readMessage();

        conn = clamp(arm);
        conn.readMessage();
    }

    public void rotateFace90AndRecover(boolean clockwise, int arm){
        Log.d("com.rubiks.lehoang.phonesender", "Rotating face");
        BluetoothConnector conn;
        if(clockwise){
            conn = rotateClock(arm);
            conn.readMessage();

            conn = unclamp(arm);
            conn.readMessage();

            conn = rotateAnti(arm);
            conn.readMessage();

            conn = clamp(arm);
            conn.readMessage();

        }else{
            conn = rotateAnti(arm);
            conn.readMessage();

            conn = unclamp(arm);
            conn.readMessage();

            conn = rotateClock(arm);
            conn.readMessage();

            conn = clamp(arm);
            conn.readMessage();
        }
    }










}
