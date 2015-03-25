/* Copyright 2011-2013 Google Inc.
 * Copyright 2013 mike wakerly <opensource@hoho.com>
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301,
 * USA.
 *
 * Project home page: https://github.com/mik3y/usb-serial-for-android
 */

package com.hoho.android.usbserial.examples;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ScrollView;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.Node;
import com.google.android.gms.wearable.NodeApi;
import com.google.android.gms.wearable.Wearable;
import com.hoho.android.usbserial.driver.UsbSerialPort;
import com.hoho.android.usbserial.util.SerialInputOutputManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.lang.reflect.Array;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.TreeSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import src.com.hoho.android.usbserial.examples.SVMGestureRecognition;
import src.com.hoho.android.usbserial.examples.SVMLevelRecognition;
import src.com.hoho.android.usbserial.examples.SVMRecognition;
import weka.classifiers.Classifier;
import weka.core.SerializationHelper;

/**
 * @Author :Akshika Wijesundara
 */

public class SerialConsoleActivity extends Activity implements GoogleApiClient.ConnectionCallbacks{
    private static final String START_ACTIVITY = "/start_activity";
    private static final String WEAR_MESSAGE_PATH = "/message";
    static int g;
    private GoogleApiClient mApiClient;

    private ArrayAdapter<String> mAdapter;
    public static byte[] publicB;
    public static JSONObject configJson, varJson, mainConfig, devicesObject, deviceObject;
    public static int s;
    public static String deviceConfigPath, device_Name;
    private static int constellationNumber;
    int zeroCount=0;
    String message = "";
    public static Classifier getClassifierSVMG() {
        return classifierSVMG;
    }

    public static void setClassifierSVMG(Classifier classifierSVMG) {
        SerialConsoleActivity.classifierSVMG = classifierSVMG;
    }

    public static Classifier getClassifierSVMH() {
        return classifierSVMH;
    }

    public static void setClassifierSVMH(Classifier classifierSVMH) {
        SerialConsoleActivity.classifierSVMH = classifierSVMH;
    }

    public static Classifier getClassifierSVML() {
        return classifierSVML;
    }

    public static void setClassifierSVML(Classifier classifierSVML) {
        SerialConsoleActivity.classifierSVML = classifierSVML;
    }

    public static Classifier classifierSVMG;
    public static Classifier classifierSVMH;
    public static Classifier classifierSVML;
    public static int counter;

    // size of the queue passed to recognize the gesture
    static int numberOfGuestureInputs;       // extrapolated to this amount
    static boolean updateGesture;
    public static SVMRecognition svm;
    public static SVMLevelRecognition svml;
    public static SVMGestureRecognition svmg;
    static PrintWriter out;
    static ArrayList<Integer> gestureRaw, gestureParse, gestureToWrite;
    static int stopSize;
    static int gestureSize;
    public static int gestureF;
    // These parameters are set based on the constellationNumber
    public static int NUMBER_OF_STATES_F1;
    public static int NUMBER_OF_STATES_F2;
    public static int NUMBER_OF_STATES_F3;
    // These parameters are set based on the flags set
    // Select training mode;
    static boolean levelTraining;
    static boolean horizontalTraining;
    static boolean gestureTraining;
    static String filenameFinal;
    // step2 train for levels & 2,3 fingers filenameFinal="VisualisationV1/level.csv";
    // step3 train for horizontal filenameFinal="VisualisationV1/horizontal.csv";

    static boolean daq;                           // true for daq
    static int numStates;                            // 0th state for background
    static int getDataForState;                      // current training state
    static int numOfDataItems;                     // number of training data per state
    static int numOfAquiredData;
    static int datai;

    static int numberOfdataperState;                        // number of training data per state
    static int currentGesture, acquiredData;
    static boolean recognize = !gestureTraining;              // step 4 training for gestures
    static boolean acquireData;
    static int numberOfGestures;                           // max number of gestures

    //following are file names
    static String filenameIntermid;
    static int tempCount=0;
    static String csvPathH;
    static String modelPathH;

    static String csvPathG;
    static String modelPathG;

    static String csvPathL;
    static String modelPathL;

    public static String mainConfigPath = "config";

    public static String[] deviceNames;

    // Serial read
    // Serial read end
    private static int NUMBER_OF_INPUTS = 4;
    public int bufferInt[] = new int[24];   //change this number when necessary, this is a hard coded value( this has to be 2^E*S where number of E0)
    public int count = 0;
    public int bufferFilled;

    int[] bufferSerial = new int[NUMBER_OF_INPUTS];

    private final String TAG = SerialConsoleActivity.class.getSimpleName();

    /**
     * Driver instance, passed in statically via
     * {@link #show(Context, UsbSerialPort)}.
     * <p/>
     * <p/>
     * This is a devious hack; it'd be cleaner to re-create the driver using
     * arguments passed in with the {@link #startActivity(Intent)} intent. We
     * can get away with it because both activities will run in the same
     * process, and this is a simple demo.
     */
    private static UsbSerialPort sPort = null;
    private TextView mTitleTextView;
    private TextView mDumpTextView;
    private ScrollView mScrollView;
   // private String message;
    private final ExecutorService mExecutor = Executors.newSingleThreadExecutor();
     private SerialInputOutputManager mSerialIoManager;

    final SerialInputOutputManager.Listener mListener =
            new SerialInputOutputManager.Listener() {

                @Override
                public void onRunError(Exception e) {
                    Log.d(TAG, "Runner stopped.");
                }

                @Override
                public void onNewData(final byte[] data) {
                    SerialConsoleActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                SerialConsoleActivity.this.updateReceivedData(data);
                            } catch (IOException e) {
                                e.printStackTrace();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                    });
                }
            };

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        try {
            initVariables();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        } catch (ParseException e) {
            e.printStackTrace();
        }
        try {
            setAbsolutePath();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            setConstellation();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            setTraininginfo();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        try {
            initGesture();
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            svmInit();
        } catch (Exception e) {
            e.printStackTrace();
        }
        for (int i = 0; i < bufferInt.length; i++) {
            bufferInt[i] = 0;
        }
        initGoogleApiClient();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.serial_console);
        mTitleTextView = (TextView) findViewById(R.id.demoTitle);
        mDumpTextView = (TextView) findViewById(R.id.consoleText);
        mScrollView = (ScrollView) findViewById(R.id.demoScroller);
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopIoManager();
        if (sPort != null) {
            try {
                sPort.close();
            } catch (IOException e) {
            }
            sPort = null;
        }
        finish();
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d(TAG, "Resumed, port=" + sPort);
        if (sPort == null) {
            mTitleTextView.setText("No serial device.");
        } else {
            final UsbManager usbManager = (UsbManager) getSystemService(Context.USB_SERVICE);

            UsbDeviceConnection connection = usbManager.openDevice(sPort.getDriver().getDevice());
            if (connection == null) {
                mTitleTextView.setText("Opening device failed");
                return;
            }

            try {
                sPort.open(connection);
                sPort.setParameters(57600, UsbSerialPort.DATABITS_8, UsbSerialPort.STOPBITS_1, UsbSerialPort.PARITY_NONE);
                }
                catch (IOException e) {
                Log.e(TAG, "Error setting up device: " + e.getMessage(), e);
                mTitleTextView.setText("Error opening device: " + e.getMessage());
                try {
                    sPort.close();
                } catch (IOException e2) {
                    // Ignore.
                }
                sPort = null;
                return;
            }
            mTitleTextView.setText("Serial device: " + sPort.getClass().getSimpleName());
        }
        onDeviceStateChange();
    }

    private void stopIoManager() {
        if (mSerialIoManager != null) {
            Log.i(TAG, "Stopping io manager ..");
            mSerialIoManager.stop();
            mSerialIoManager = null;
        }
    }

    private void startIoManager() {
        if (sPort != null) {
            Log.i(TAG, "Starting io manager ..");
            mSerialIoManager = new SerialInputOutputManager(sPort, mListener);
            mExecutor.submit(mSerialIoManager);
        }
    }

    private void onDeviceStateChange() {
        stopIoManager();
        startIoManager();
    }

    private void predict() throws InterruptedException {
            int[] data = new int[SVMRecognition.numberOfDataitems];
            for (int i = 0; i < bufferSerial.length; i++) {
                data[i] = bufferSerial[i];
                if (i < bufferSerial.length - 1)
                    data[i + bufferSerial.length] = bufferSerial[i + 1] - bufferSerial[i];
            }
            int horizontal = svm.getPrediction(data);
            int level = svml.getPrediction(data);
            // recognize gesture
            int state = level * 10 + horizontal;
            if (horizontal == 0) state = 0;
            gestureRaw.add(state);
            parseGesture();
            if (updateGesture) {
                updateGesture = false;
                String text = "not initialized";
                switch(g)
                {
                    case 0:
                        text = "ONE FINGER RIGHT SWIPE";
                        break;
                    case 1:
                        text = "ONE FINGER LEFT SWIPE";
                        break;
                    case 2:
                        text = "THREE FINGER SWIPE ";
                        break;

                    case 3:
                        text = "TWO FINGER CUT";
                        break;
                    case 4:
                        text = "CURVE SWIPE";
                        break;

                };
               mDumpTextView.append(text + "\n");
               mScrollView.smoothScrollTo(0, mDumpTextView.getBottom());


                return;
            }


    }
    private void updateReceivedData(byte[] data) throws IOException, InterruptedException {
        int i=0;
        int ct = 0; //varaible used in the serial data reading
        sendMessage(WEAR_MESSAGE_PATH,data);

        for (; i < data.length; i++) {
            int nn = new Byte(data[i]).intValue() & 0xFF;;
            if (nn == 0) {
                zeroCount++;
                bufferFilled = 0;
                predict();
            }
            else if (bufferFilled >= NUMBER_OF_INPUTS) {
                continue;
            }
            else {
                   int k = ct / 3;
                   if ((k & 2) == 0 && ct % 3 == 1) {
                   bufferSerial[bufferFilled++] = nn;
                   }
                   ct++;
                }
            }

    }

    /**
     * Starts the activity, using the supplied driver instance.
     *
     * @param context
     */
    static void show(Context context, UsbSerialPort port) {
        sPort = port;
        final Intent intent = new Intent(context, SerialConsoleActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP | Intent.FLAG_ACTIVITY_NO_HISTORY);
        context.startActivity(intent);
    }

    void svmInit() throws Exception {
        SVMRecognition.numberOfDataitems=2*NUMBER_OF_INPUTS-1;
        SVMLevelRecognition.numberOfDataitems=2*NUMBER_OF_INPUTS-1;
        svml= new SVMLevelRecognition();
        AssetManager assetManager = getAssets();

        classifierSVML = (Classifier) SerializationHelper.read(assetManager.open(SVMLevelRecognition.modelPath));
        svml.init();
        svm= new SVMRecognition();
        classifierSVMH = (Classifier) SerializationHelper.read(assetManager.open(SVMRecognition.modelPath));
        svm.init();

        if(recognize){

            SVMGestureRecognition.numberOfStates=numberOfGestures;
            SVMGestureRecognition.numberOfDataitems=numberOfGuestureInputs;

            svmg= new SVMGestureRecognition();
            classifierSVMG = (Classifier) SerializationHelper.read(assetManager.open(SVMGestureRecognition.modelPath));
            // should be called after training generation
            svmg.init();
        }

    }
    static void initGesture() throws IOException {
        // Gesture Training output
        if(acquireData){
            out = new PrintWriter((new FileWriter(filenameIntermid)));
            if(currentGesture==0){
                for (int i = 0; i < numberOfGuestureInputs; i++) {
                    out.print("data" + i + ",");
                }
                out.println("gesture");  }
            // Gesture Training output end
        }
//        initKey();
        gestureRaw= new ArrayList<Integer>();
        gestureParse = new ArrayList<Integer>();
        gestureToWrite = new ArrayList<Integer>();

    }

    static void setConstellation() throws JSONException {

        JSONObject ports    =   configJson.getJSONObject("ConstellationConfig");
        NUMBER_OF_INPUTS    =   ports.getInt("NUMBER_OF_INPUTS");
        NUMBER_OF_STATES_F1 =   ports.getInt("NUMBER_OF_STATES_F1");
        NUMBER_OF_STATES_F2 =   ports.getInt("NUMBER_OF_STATES_F2");
        NUMBER_OF_STATES_F3 =   ports.getInt("NUMBER_OF_STATES_F3");
        numberOfGestures    =   ports.getInt("numberOfGestures");

    }

    static void setTraininginfo() throws JSONException {

        if(levelTraining) {
            filenameFinal=csvPathL;
            numStates = configJson.getInt("ConstellationNumStatesL");
        }

        if(horizontalTraining){
            filenameFinal=csvPathH;
            numStates = configJson.getInt("ConstellationNumStatesH");
        }

        if(gestureTraining){
            filenameFinal=csvPathG;
        }

    }

    static void  parseGesture() {
        int len=gestureRaw.size();
        if(len>=stopSize){
            boolean check=true;
            int finalState=gestureRaw.get(len-1);
            for (int i = 0; i < stopSize; i++) {
                if(gestureRaw.get(len-1-i)!=finalState){
                    check=false;
                    break;
                }
            }
            if(check){
                gestureParse.clear();
                int c1=1,pstate=gestureRaw.get(0);
                if(pstate!=0)
                    gestureParse.add(pstate);
                for (Integer state : gestureRaw) {
                    if(c1==1&&state==pstate)continue;
                    else {
                        if(state!=0)
                           gestureParse.add(state);

                            pstate=state;
                            c1=0;
                    }
                }
                gestureRaw.clear();
                while(gestureParse.size()>=2&&gestureParse.get(gestureParse.size()-1)==gestureParse.get(gestureParse.size()-2)){
                    gestureParse.remove(gestureParse.size()-1);
                }
                TreeSet<Integer> size= new TreeSet<Integer>();
                for (Integer integer : gestureParse) {
                    size.add(integer);
                }
                if(gestureParse.size()>=gestureSize&&size.size()>2)
                {   System.err.println(gestureParse);
                    gestureToWrite.clear();
                    int length=gestureParse.size(); //  gestureToWrite size is always  numberOfGuestureInputs
                    int filled=0;
                    for (int i = 1; i <= length; i++) {
                        int upto= (numberOfGuestureInputs-1)*i/length;
                        for ( ; filled <=upto ; filled++) {
                            gestureToWrite.add(gestureParse.get(i-1));
                        }

                    }
                     if(recognize){
                         g=svmg.getPrediction(gestureToWrite);
                        gestureF=g+1;
                        updateGesture=true;
                        System.out.println("gesture: "+g);

                    }
                }
            }
        }
    }

    public void initVariables() throws IOException, JSONException, java.text.ParseException {
        /*following code will read a json object and assign the values to the variables in the code.
          if you want to change some value. please open json file named configuration.json in data
          folder. Then it will be assigned in to the working code
        */
        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new InputStreamReader(getAssets().open("config/mainConfig.json")));
            if (reader!= null)
                Log.d(TAG, "Main Config file was open");
        } catch (IOException e) {
            e.printStackTrace();
        }

        String line = reader.readLine();
        String temp = "";
        while ( line!= null) {
            temp = temp+line;
            line = reader.readLine();
        }
         mainConfig          =   new JSONObject(temp);
         devicesObject       =   mainConfig.getJSONObject("devices");    //devicesObject gets the JSON object which has the device names and the relative paths to the device config file(many devices)
         JSONArray s         =   devicesObject.names();
         deviceNames         =   new String[s.length()];
        for (int i=0;i<s.length();i++)
        {
           deviceNames[i] = s.getString(i);
        }

        device_Name         =   "ring";
        deviceObject        =   devicesObject.getJSONObject(device_Name);    //deviceObject gets the JSON object which has the device name and the relative path to the device config file(Single device)
        deviceConfigPath    =   deviceObject.getString("path");             //getting the path to the device config files
        deviceConfigPath    =   mainConfigPath+"/"+deviceConfigPath;         //adding the relative path to the ♦ configuration folder's path

        try {
            reader = new BufferedReader(new InputStreamReader(getAssets().open("config/devices/"+device_Name+"/json/variables.json")));
            if ( reader != null)
                Log.d(TAG, "Config file was open");
        } catch (IOException e) {
            e.printStackTrace();
        }
        line                =   reader.readLine();
        temp                =    "";
        while ( line!= null) {
            temp = temp+line;
            line = reader.readLine();
        }
        //getting the JsonObject and assigning it to the configJson
        varJson = new JSONObject(temp);

        filenameFinal       =   varJson.getString("filenameFinal");
        filenameIntermid    =   varJson.getString("filenameIntermid");
        csvPathG            =   varJson.getString("csvPathG");
        csvPathH            =   varJson.getString("csvPathH");
        csvPathL            =   varJson.getString("csvPathL");
        modelPathG          =   varJson.getString("modelPathG");
        modelPathH          =   varJson.getString("modelPathH");
        modelPathL          =   varJson.getString("modelPathL");


        //following is making the path to work properly depending on the constellation number

        numStates           =   varJson.getInt("numStates"); // 0th state for background
        acquiredData        =   varJson.getInt("acquiredData");

        try {
            reader = new BufferedReader(new InputStreamReader(getAssets().open(deviceConfigPath)));
            if ( reader != null)
                Log.d(TAG, "It worked!");
        } catch (IOException e) {
            e.printStackTrace();
        }

        line = reader.readLine();
        temp = "";
        while ( line!= null) {
            temp = temp+line;
            line = reader.readLine();
        }
        //getting the JsonObject and assigning it to the configJson
        configJson = new JSONObject(temp);

        //below the variables will be assigned by the values saved in the Json Object
        constellationNumber = configJson.getInt("constellationNumber");
        numberOfGuestureInputs = configJson.getInt("numberOfGuestureInputs");
        updateGesture = configJson.getBoolean("updateGesture");
        stopSize = configJson.getInt("stopSize");
        gestureSize = configJson.getInt("gestureSize");
        gestureF = configJson.getInt("gestureF");
        levelTraining= configJson.getBoolean("levelTraining");
        horizontalTraining = configJson.getBoolean("horizontalTraining");
        gestureTraining = configJson.getBoolean("gestureTraining");
        getDataForState = configJson.getInt("getDataForState"); // current training state
        numOfDataItems = configJson.getInt("numOfDataItems"); // number of training data per state
        numOfAquiredData = configJson.getInt("numOfAquiredData");
        datai = configJson.getInt("datai");
        numberOfdataperState = configJson.getInt("numberOfdataperState"); // number of training data per state
        currentGesture = configJson.getInt("currentGesture");

    }
    private static void setAbsolutePath() throws JSONException {

        //here are the absolute path values for the models and csv files

        filenameIntermid    = "config/devices/"+ device_Name+ "/csv/" + filenameIntermid;
        csvPathH            = "config/devices/"+ device_Name+ "/csv/"  + csvPathH;
        csvPathL            = "config/devices/"+ device_Name+ "/csv/"  +csvPathL;
        csvPathG            = "config/devices/"+ device_Name+ "/csv/"  +csvPathG;
        modelPathH          = "config/devices/"+ device_Name+"/models/"+ modelPathH;
        modelPathL          = "config/devices/"+ device_Name+"/models/"+ modelPathL;
        modelPathG          = "config/devices/"+ device_Name+"/models/"+modelPathG;
    }

    private void initGoogleApiClient() {
        mApiClient = new GoogleApiClient.Builder( this )
                .addApi( Wearable.API )
                .build();

        mApiClient.connect();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mApiClient.disconnect();
    }


    private void sendMessage( final String path, final byte[] text ) throws InterruptedException {

        new Thread( new Runnable() {
            @Override
            public void run() {
                NodeApi.GetConnectedNodesResult nodes = Wearable.NodeApi.getConnectedNodes( mApiClient ).await();
                for(Node node : nodes.getNodes()) {
                   // MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(
                     //       mApiClient, node.getId(), path, text.getBytes() ).await();
                    MessageApi.SendMessageResult result = Wearable.MessageApi.sendMessage(
                            mApiClient, node.getId(), path, text).await();
                    if(!result.getStatus().isSuccess()){
                        Log.e("test", "error");
                    } else {
                        Log.i("test", "success!! sent to: " + node.getDisplayName());
                    }
                }
            }
        }).start();
       // Thread.sleep(50);
    }

    @Override
    public void onConnected(Bundle bundle) {
        try {
            sendMessage(START_ACTIVITY,"Start".getBytes());
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onConnectionSuspended(int i) {

    }
}
