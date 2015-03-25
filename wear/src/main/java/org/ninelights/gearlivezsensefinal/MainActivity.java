package org.ninelights.gearlivezsensefinal;

/*
* @author : Akshika47
* */

import android.app.Activity;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.wearable.MessageApi;
import com.google.android.gms.wearable.MessageEvent;
import com.google.android.gms.wearable.Wearable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.TreeSet;

import weka.classifiers.Classifier;
import weka.core.SerializationHelper;

public class MainActivity extends Activity implements MessageApi.MessageListener, GoogleApiClient.ConnectionCallbacks {

    private static final String WEAR_MESSAGE_PATH = "/message";
    private GoogleApiClient mApiClient;
    public static JSONObject configJson, varJson, mainConfig, devicesObject, deviceObject;
    private static int g,numberOfGuestureInputs,stopSize,gestureSize,currentGesture,numberOfGestures,constellationNumber;
    public static String deviceConfigPath, device_Name;
    private static Classifier classifierSVMG,classifierSVMH,classifierSVML;
    static boolean updateGesture,acquireData;
    private static SVMRecognition svm;
    private static SVMLevelRecognition svml;
    private static SVMGestureRecognition svmg;
    private static ArrayList<Integer> gestureRaw, gestureParse, gestureToWrite;
    private static boolean recognize = true;              // step 4 training for gestures
    private static String modelPathH,modelPathG,modelPathL,gesture1,gesture2,gesture3;
    public static String[] deviceNames;
    private static int NUMBER_OF_INPUTS = 24;
    public int bufferInt[] = new int[24];   //change this number when necessary, this is a hard coded value( this has to be 2^E*S where number of E0)
    private int bufferFilled;
    int[] bufferSerial = new int[NUMBER_OF_INPUTS];
    private final String TAG = MainActivity.class.getSimpleName();
    int zeroCount=0;
    public TextView tx;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        try {
            initVariables();
        } catch (IOException | JSONException | ParseException e) {
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
        super.onCreate(savedInstanceState);
        setContentView(R.layout.final_layout);
        tx = (TextView) findViewById(R.id.textName);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        initGoogleApiClient();
    }

    private void initGoogleApiClient() {
        mApiClient = new GoogleApiClient.Builder( this )
                .addApi( Wearable.API )
                .addConnectionCallbacks( this )
                .build();

        if( mApiClient != null && !( mApiClient.isConnected() || mApiClient.isConnecting() ) )
            mApiClient.connect();
    }

    @Override
    protected void onResume() {
        super.onResume();
        if( mApiClient != null && !( mApiClient.isConnected() || mApiClient.isConnecting() ) )
            mApiClient.connect();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    /**
     *
     * @param messageEvent
     */
    @Override
    public void onMessageReceived( final MessageEvent messageEvent ) {

        runOnUiThread( new Runnable() {
            @Override
            public void run() {
                if( messageEvent.getPath().equalsIgnoreCase( WEAR_MESSAGE_PATH ) ) {
                    byte[] data = messageEvent.getData();
                    int i=0;
                    int ct = 0;
                    for (; i < data.length; i++) {
                        int nn = new Byte(data[i]).intValue() & 0xFF;;
                        if (nn == 0) {
                            zeroCount++;
                            bufferFilled = 0;
                            try {
                                predict();
                            } catch (InterruptedException e) {
                                e.printStackTrace();
                            }
                        }
                        else if (bufferFilled >= NUMBER_OF_INPUTS) {
                            continue;
                        }
                        else {
                            int k;
                            switch(constellationNumber) {

                                case 1:
                                    bufferSerial[bufferFilled++]=nn;
                                    break;

                                case 2:
                                    bufferSerial[bufferFilled++]=nn;
                                    break;

                                case 3:
                                    bufferSerial[bufferFilled++]=nn;
                                    break;

                                case 4:
                                    k=ct/3;
                                    if(k%2==0&&ct%3!=0)
                                    {
                                        bufferSerial[bufferFilled++]=nn;
                                    }
                                    break;

                                case 5:
                                    k=ct/3;
                                    if((k&2)==0&&ct%3!=1)
                                    {
                                        bufferSerial[bufferFilled++]=nn;
                                    }
                                    break;

                                case 6:
                                    k=ct/3;
                                    if((k&2)==0&&ct%3==1)
                                    {
                                        bufferSerial[bufferFilled++]=nn;
                                    }
                                    break;

                                case 7:
                                    k=ct/3;
                                    if((k&2)==0&&ct%3==1)
                                    {
                                        bufferSerial[bufferFilled++]=nn;
                                    }
                                    break;

                                case 8:
                                    k=ct/3;
                                    if((k&2)==0&&ct%3==1)
                                    {
                                        bufferSerial[bufferFilled++]=nn;
                                    }
                                    break;

                                case 9:
                                    k = ct / 3;
                                    if ((k & 2) == 0 && ct % 3 == 1)
                                    {
                                        bufferSerial[bufferFilled++] = nn;
                                    }
                                    break;
                            };
                            ct++;
                        }
                    }

                }
            }
        });
    }

    @Override
    public void onConnected(Bundle bundle) {
        Wearable.MessageApi.addListener( mApiClient, this );
    }

    @Override
    protected void onStop() {
        if ( mApiClient != null ) {
            Wearable.MessageApi.removeListener( mApiClient, this );
            if ( mApiClient.isConnected() ) {
                mApiClient.disconnect();
            }
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        if( mApiClient != null )
            mApiClient.unregisterConnectionCallbacks( this );
        super.onDestroy();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    /**
     *
     * @throws InterruptedException
     */
    private void predict() throws InterruptedException {
        long timeStart = System.currentTimeMillis();
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
        String text = "not initialized";
        ImageView iv = (ImageView) findViewById(R.id.imageDirection);
        if (updateGesture) {
            updateGesture = false;
            switch (g) {
                case 0:
                    text = "Right Close Swipe";
                    iv.setImageResource(R.drawable.right);
                    break;
                case 1:
                    text = "Right Distance Swipe";
                    iv.setImageResource(R.drawable.right);
                    break;
                case 2:
                    text = "Two Finger";
                    iv.setImageResource(R.drawable.twofinger);
                    break;
                case 3:
                    text = "Up Close Swipe";
                    iv.setImageResource(R.drawable.up);
                    break;
                case 4:
                    text = "Down Close Swipe";
                    iv.setImageResource(R.drawable.down);
                    break;

            };
            tx.setText(text);
            iv.invalidate();
            tx.invalidate();
            return;
        }

    }

    /**
     *
     * @throws Exception
     */
    void svmInit() throws Exception {
        SVMRecognition.numberOfDataitems=2*NUMBER_OF_INPUTS-1;
        SVMLevelRecognition.numberOfDataitems=2*NUMBER_OF_INPUTS-1;
        svml= new SVMLevelRecognition();
        AssetManager assetManager = getAssets();

        classifierSVML = (Classifier) SerializationHelper.read(assetManager.open(modelPathL));
        svml.init();
        svm= new SVMRecognition();
        classifierSVMH = (Classifier) SerializationHelper.read(assetManager.open(modelPathH));
        svm.init();

        if(recognize){

            SVMGestureRecognition.numberOfStates=numberOfGestures;
            SVMGestureRecognition.numberOfDataitems=numberOfGuestureInputs;
            svmg= new SVMGestureRecognition();
            classifierSVMG = (Classifier) SerializationHelper.read(assetManager.open(modelPathG));
            // should be called after training generation
            svmg.init();
        }

    }

    /**
     *
     * @throws IOException
     */
    static void initGesture() throws IOException {
        // Gesture Training output
        if(acquireData){
            if(currentGesture==0){
                for (int i = 0; i < numberOfGuestureInputs; i++) {
                }
            }
            // Gesture Training output end
        }
        gestureRaw= new ArrayList<Integer>();
        gestureParse = new ArrayList<Integer>();
        gestureToWrite = new ArrayList<Integer>();

    }

    static void setConstellation() throws JSONException {

        JSONObject ports    =   configJson.getJSONObject("ConstellationConfig");
        NUMBER_OF_INPUTS    =   ports.getInt("NUMBER_OF_INPUTS");
        numberOfGestures    =   ports.getInt("numberOfGestures");

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
                        updateGesture=true;
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

        device_Name         =   mainConfig.getString("device_Name");
        deviceObject        =   devicesObject.getJSONObject(device_Name);    //deviceObject gets the JSON object which has the device name and the relative path to the device config file(Single device)
        deviceConfigPath    =   deviceObject.getString("path");             //getting the path to the device config files
        String mainConfigPath = "config";
        deviceConfigPath    =   mainConfigPath +"/"+deviceConfigPath;         //adding the relative path to the ♦ configuration folder's path

        try {
            reader = new BufferedReader(new InputStreamReader(getAssets().open("config/devices/"+device_Name+"/json/variables.json")));
            if ( reader != null)
                Log.d(TAG, "Variable file was open");
        } catch (IOException e) {
            e.printStackTrace();
        }
        line                =   reader.readLine();
        temp                =    "";
        while ( line!= null) {
            temp = temp+line;
            line = reader.readLine();
        }

        varJson             =   new JSONObject(temp); //getting the JsonObject and assigning it to the configJson
        modelPathG          =   varJson.getString("modelPathG");
        modelPathH          =   varJson.getString("modelPathH");
        modelPathL          =   varJson.getString("modelPathL");

        try {
            reader = new BufferedReader(new InputStreamReader(getAssets().open(deviceConfigPath)));
            if ( reader != null)
                Log.d(TAG, "Device Configuration file was opened");
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
        currentGesture = configJson.getInt("currentGesture");

    }

    private static void setAbsolutePath() throws JSONException {

        //here are the absolute path values for the models and csv files

        modelPathH          = "config/devices/"+ device_Name+"/models/"+ modelPathH;
        modelPathL          = "config/devices/"+ device_Name+"/models/"+ modelPathL;
        modelPathG          = "config/devices/"+ device_Name+"/models/"+modelPathG;

    }

    public static Classifier getClassifierSVMG() {return classifierSVMG; }

    public static Classifier getClassifierSVMH() {
        return classifierSVMH;
    }

    public static Classifier getClassifierSVML() {
        return classifierSVML;
    }
}
