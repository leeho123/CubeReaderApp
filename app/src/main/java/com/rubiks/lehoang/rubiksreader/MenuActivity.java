package com.rubiks.lehoang.rubiksreader;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;

import com.rubiks.lehoang.rubiksreader.Robot.RobotControllerActivity;
import com.rubiks.lehoang.rubiksreader.Robot.UnsyncedRobot;
import com.rubiks.lehoang.rubiksreader.Solver.CubeSolver;
import com.rubiks.lehoang.rubiksreader.Vision.VisionActivity;
import com.rubiks.lehoang.rubiksreader.Vision.VisionTestActivity;

import java.io.IOException;
import java.util.Arrays;

/**
 * Created by LeHoang on 27/05/2015.
 */
public class MenuActivity extends Activity {
    Button connectBtn;
    Button profileBtn;
    Button readBtn;
    Button visionBtn;
    Button robotBtn;
    Button solveBtn;
    Button robotSolveBtn;
    Button robotClampBtn;

    UnsyncedRobot robot = null;

    String state = null;
    String solution = null;
    String ip = null;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_menu);

        connectBtn = (Button) findViewById(R.id.connectBtn);
        profileBtn = (Button) findViewById(R.id.profileBtn);
        readBtn   = (Button) findViewById(R.id.readBtn);
        visionBtn  = (Button) findViewById(R.id.visionBtn);
        robotBtn = (Button) findViewById(R.id.robotBtn);
        solveBtn = (Button) findViewById(R.id.solutionBtn);
        robotSolveBtn = (Button) findViewById(R.id.robotSolveBtn);
        robotClampBtn = (Button) findViewById(R.id.clampRobotBtn);

        solveBtn.setEnabled(false);
        profileBtn.setEnabled(false);
        robotSolveBtn.setEnabled(false);

        robotClampBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(robot != null){
                    robot.clampAll();
                }
            }
        });

        connectBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    robot = new UnsyncedRobot();
                    profileBtn.setEnabled(true);
                    readBtn.setEnabled(true);
                    robot.clampAll();
                    robotSolveBtn.setEnabled(true);

                } catch (Exception e) {
                    AlertDialog.Builder builder = new AlertDialog.Builder(MenuActivity.this);
                    builder.setTitle("Error");
                    builder.setMessage("Cannot connect to Robot. Check all NXT Bricks are on.");
                    builder.show();
                    builder.setCancelable(true);
                }
            }
        });

        profileBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                if(robot != null){
                    long[][] profile = robot.profile();
                    Log.e("com.rubiks.lehoang.rubiksreader" , Arrays.toString(profile[0]));
                    Log.e("com.rubiks.lehoang.rubiksreader" , Arrays.toString(profile[1]));

                }
            }
        });

        readBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MenuActivity.this, VisionActivity.class);
                startActivityForResult(intent, 1);
            }
        });

        visionBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MenuActivity.this, VisionTestActivity.class);
                startActivity(intent);
            }
        });

        robotBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MenuActivity.this, RobotControllerActivity.class);
                startActivity(intent);
            }
        });

        solveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (state != null) {
                    try {
                        if(ip == null) {
// get prompts.xml view
                            LayoutInflater li = LayoutInflater.from(MenuActivity.this);
                            View promptsView = li.inflate(R.layout.prompts, null);

                            AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(
                                    MenuActivity.this);

                            // set prompts.xml to alertdialog builder
                            alertDialogBuilder.setView(promptsView);

                            final EditText userInput = (EditText) promptsView
                                    .findViewById(R.id.editTextDialogUserInput);

                            // set dialog message
                            alertDialogBuilder
                                    .setCancelable(false)
                                    .setPositiveButton("OK",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog,int id) {
                                                    // get user input and set it to result
                                                    // edit text
                                                    ip = userInput.getText().toString();
                                                }
                                            })
                                    .setNegativeButton("Cancel",
                                            new DialogInterface.OnClickListener() {
                                                public void onClick(DialogInterface dialog,int id) {
                                                    dialog.cancel();
                                                }
                                            });

                            // create alert dialog
                            AlertDialog alertDialog = alertDialogBuilder.create();

                            // show it
                            alertDialog.show();
                        }
                        final CubeSolver solver = new CubeSolver(ip);
                        Thread newThread = new Thread(new Runnable() {

                            @Override
                            public void run() {
                                try {
                                    solution = solver.solve(state);
                                    MenuActivity.this.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            AlertDialog.Builder builder = new AlertDialog.Builder(MenuActivity.this);
                                            builder.setTitle("Got solution!");
                                            builder.setMessage(solution);
                                            builder.show();
                                            builder.setCancelable(true);
                                        }
                                    });

                                } catch (IOException e) {
                                    e.printStackTrace();
                                    MenuActivity.this.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            AlertDialog.Builder builder = new AlertDialog.Builder(MenuActivity.this);
                                            builder.setTitle("Error");
                                            builder.setMessage("Cannot connect to solution server. " +
                                                    "Check your internet or that the server is online.");
                                            builder.show();
                                            builder.setCancelable(true);
                                            ip = null;
                                        }
                                    });

                                }
                                Log.e("com.rubiks.lehoang.rubiksreader", "Solution: " + solution);
                                solveBtn.setVisibility(View.VISIBLE);
                            }
                        });
                        newThread.start();

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        });

        robotSolveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(solution != null) {
                    robot.robotSolve(solution);
                    robot.finish();
                }
            }
        });


    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data){
        //Case where we asked for the state
        if(requestCode == 1){
            if(resultCode == RESULT_OK){
                String cubestate = data.getStringExtra("cubestate");
                AlertDialog.Builder builder = new AlertDialog.Builder(MenuActivity.this);
                builder.setTitle("We have a cube state!");
                builder.setMessage(cubestate);
                builder.show();
                builder.setCancelable(true);
                state = cubestate;
                solveBtn.setEnabled(true);
            }else if(resultCode == RESULT_CANCELED){
                String cubestate = data.getStringExtra("cubestate");
                AlertDialog.Builder builder = new AlertDialog.Builder(MenuActivity.this);
                builder.setTitle("Error building state!");
                builder.setMessage(cubestate + " Please try again!");
                builder.show();
                builder.setCancelable(true);
            }
        }
    }
}
