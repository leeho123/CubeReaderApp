package com.rubiks.lehoang.rubiksreader.Solver;

import android.util.Log;

import com.rubiks.lehoang.rubiksreader.Robot.UnsyncedRobot;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.String;import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;


/**
 * Created by LeHoang on 11/05/2015.
 */
public class CubeSolver{
    public static class InvalidMoveException extends Exception{
        public InvalidMoveException(String move){
            super("Invalid move: " + move);
        }
    }


    public static final int U = 0;
    public static final int U2= 1;
    public static final int UPRIME = 2;
    public static final int F = 3;
    public static final int F2 =4;
    public static final int FPRIME =5;
    public static final int R = 6;
    public static final int R2 = 7;
    public static final int RPRIME = 8;
    public static final int D = 9;
    public static final int D2 = 10;
    public static final int DPRIME = 11;
    public static final int B = 12;
    public static final int B2 = 13;
    public static final int BPRIME = 14;
    public static final int L = 15;
    public static final int L2 = 16;
    public static final int LPRIME = 17;


    public static final int[][] moveCostFlipped = {{1, 1, 3, 1, 1, 3, 1, 1, 3, 1, 1, 3, 1, 1, 3, 1, 1, 3},
            {3, 1, 1, 3, 1, 1, 3, 1, 1, 3, 1, 1, 3, 1, 1, 3, 1, 1}};

    public static final String IP = "192.168.0.9";
    Socket socket;
    BufferedReader in;
    PrintWriter out;
    String solution2;
    String solution;

    String ip;
    public CubeSolver(String ip){
        this.ip = ip;
    }
    public String solve(final String state) throws IOException {
        socket = new Socket(ip, 12345);
        in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        out = new PrintWriter(socket.getOutputStream());


        out.println(state);
        out.flush();

        int time = 0;
        while(true) {
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            if (in.ready()) {
                break;
            }
            time++;
            if (time == 15) {
                out.println("STOP");
                out.flush();
            }
        }
        try {
            solution = in.readLine();
        }catch(SocketTimeoutException e) {
            out.println("STOP");
            out.flush();
            solution = in.readLine();
        }

        Log.d("com.rubiks.lehoang.phonesender", "Got something back");
        socket.close();
        return solution;
    }


    public static String transform(String solution){
        String replaced = solution.replace('\'', '3');
        replaced = replaced.replaceAll("\\s+","");
        return replaced;
    }


    public static List<UnsyncedRobot.Move> parseSolution(String replaced){

        List<UnsyncedRobot.Move> moves = new ArrayList<>();
        for(int i = replaced.length()-1; i >= 0; i--){
            switch(replaced.charAt(i)){
                case '3':
                case '2':
                    moves.add(0, UnsyncedRobot.Move.valueOf(replaced.substring(i-1, i+1)));
                    i--;
                    break;
                default:
                    moves.add(0, UnsyncedRobot.Move.valueOf(replaced.substring(i, i+1)));
            }
        }

        return moves;
    }

    public static int getMove(char move) throws InvalidMoveException{
        switch(move){
            case 'R':
                return R;
            case 'L':
                return L;
            case 'U':
                return U;
            case 'F':
                return F;
            case 'B':
                return B;
            case 'D':
                return D;
            default:
                throw new InvalidMoveException(Character.toString(move));
        }
    }


}
