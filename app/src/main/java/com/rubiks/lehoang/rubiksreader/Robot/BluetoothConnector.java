package com.rubiks.lehoang.rubiksreader.Robot;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.lang.Integer;import java.lang.Override;import java.lang.String;import java.lang.System;import java.util.UUID;

/**
 * Created by LeHoang on 06/05/2015.
 */
public class BluetoothConnector {
    final static String slaveFAddress = "00:16:53:08:DE:65";
    final static String slaveBAddress = "00:16:53:15:B6:C2";
    final static String slaveRAddress = "00:16:53:09:B8:5B";
    final static String slaveLAddress = "00:16:53:18:A7:50";

    String address;
    BluetoothAdapter adapter;
    BluetoothSocket socket;

    public BluetoothConnector(String address){
        this.address = address;
        this.adapter = BluetoothAdapter.getDefaultAdapter();
    }

    public void setState(boolean isOn){
        if(isOn){
            if(!adapter.isEnabled()){
                adapter.enable();
            }
        }else{
            if(adapter.isEnabled()){
                adapter.disable();
            }
        }
    }

    public boolean getState(){
        return adapter.isEnabled();
    }

    public boolean connect(){
        boolean connected = false;

        BluetoothDevice nxt = adapter.getRemoteDevice(address);

        try{
            socket = nxt.createRfcommSocketToServiceRecord(UUID.fromString("00001101-0000-1000-8000-00805F9B34FB"));
            socket.connect();
            connected = true;
        }catch(IOException e){
            connected = false;
        }

        return connected;
    }

    public Integer readMessage(){
        Integer message = null;
        if(socket != null) {
            try {
                DataInputStream input = new DataInputStream(socket.getInputStream());
                message = input.readInt();


            }catch(IOException e){

            }
        }
        return message;
    }

    public boolean sendMessage(int instruction){
        boolean success = false;
        if(socket != null) {
            try {
                System.out.println("Sending " + instruction);
                DataOutputStream stream = new DataOutputStream(socket.getOutputStream());
                stream.writeInt(instruction);
                stream.flush();
                success = true;
            } catch (IOException e) {

            }
        }
        return success;
    }

    public boolean sendLong(long instruction){
        boolean success = false;
        if(socket != null) {
            try {
                DataOutputStream stream = new DataOutputStream(socket.getOutputStream());
                stream.writeLong(instruction);
                stream.flush();
                success = true;
            } catch (IOException e) {

            }
        }
        return success;
    }

    public static class FakeConnector extends BluetoothConnector{
        public FakeConnector(){
            super(null);
        }
        @Override
        public Integer readMessage(){
            return 0;
        }


    }


}
