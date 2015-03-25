package src.com.hoho.android.usbserial.examples; /**
 * Created with IntelliJ IDEA.
 * User: Nipuna_Sam
 * Date: 28/3/14
 * Time: 3:09 PM
 * To change this template use File | Settings | File Templates.
 */

import android.util.Log;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;


import org.json.JSONObject;
import weka.classifiers.*;
import weka.classifiers.bayes.BayesNet;
import weka.core.Attribute;
import weka.core.Instance;
import weka.core.FastVector;
import weka.core.Instances;
import weka.core.converters.CSVLoader;
public class SVMLevelRecognition {
    public static int numberOfDataitems=0;
    public static int numberOfStates=20;
    static Instances testingInstances;
    static String state;
    static Classifier classifier;
    static String csvPath;
    public static String modelPath = "config/devices/ring/models/Level.model";
    private static int constellationNumber;
    private static String device_Name;
    public static Instances createTrainingDataset()
    {
        Instances trainingInstances = null;
        try {
            //creating the training dataset;
            CSVLoader trainingLoader =new CSVLoader();
            trainingLoader.setSource(new File(csvPath));
            trainingInstances=trainingLoader.getDataSet();
            trainingInstances.setClassIndex(trainingInstances.numAttributes()-1);

        }catch(IOException ex) {
        }
        return trainingInstances;
    }

    public static void trainClassifier(Instances trainingInstances)
    {
        try{
            BayesNet bn = new BayesNet();
            String[] options={"-B","20"};
            bn.setOptions(options);
            for (String s : bn.getOptions()) {
                System.err.println(s);
            }

            classifier=bn;

            //build the classifier for the training instances
            classifier.buildClassifier(trainingInstances);
            //Evaluating the classifer
            Evaluation eval = new Evaluation(trainingInstances);
            eval.evaluateModel(classifier,trainingInstances);
            //printing the summary of training
            String strSummary = eval.toSummaryString();
            System.err.println("-----------TRAINING SUMMARY---------");
            System.err.println(strSummary);
            System.err.println("------------------------------------");
            double[][] cmMatrix = eval.confusionMatrix();
            for (int i = 0; i < cmMatrix.length; i++) {
                for (double v : cmMatrix[i]) {
                    System.err.print(v + " ");
                }
                System.err.println();
            }
            //Serializing the classsifier object in to a file
            weka.core.SerializationHelper.write(modelPath,classifier);
        }catch(Exception ex) {
            Logger.getLogger(SVMRecognition.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    public static Instances createTestingInstanceProgrammatically(int[] data)
    {
        Log.i("Asdasd",testingInstances.toString());
        //creating an instance and adding it to the testing instances
        testingInstances.delete();
        Instance instance =  new Instance(numberOfDataitems+1);
        for (int i = 0; i < numberOfDataitems; i++) {
            instance.setValue(testingInstances.attribute(i), data[i]);
        }
        instance.setDataset(testingInstances);
        testingInstances.add(instance);
        return testingInstances;
    }


    public static void testingTheClassifier(Instances testingInstances)
    {
        try {
            //load the trained classifer from the file - desrialization
            Classifier classifier = com.hoho.android.usbserial.examples.SerialConsoleActivity.getClassifierSVML();

            //classifier classfying the data for the instance in the testing  instances
            for(int i=0;i<testingInstances.numInstances();i++)
            {
                double score =  classifier.classifyInstance(testingInstances.instance(i));
                testingInstances.instance(i).setClassValue(score);
                state= testingInstances.classAttribute().value((int) score);
            }
        } catch (Exception ex) {
            Logger.getLogger(SVMRecognition.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    static void initTestintInstances(){
        //creating a testing data set programmatically
        FastVector fv = new FastVector(numberOfDataitems+1);
        //The below lines set the attributes for the testing instances.
        //The attributes should be the same
        //as in the training dataset used for training the classifier
        for (int i = 0; i < numberOfDataitems; i++) {
            fv.addElement(new Attribute("data"+i));
        }
        FastVector nomVals = new FastVector();
        for (int i=0; i<numberOfStates; i++)
            nomVals.addElement("L"+i);
        fv.addElement(new Attribute("state",nomVals));
        testingInstances = new Instances("testInstance",fv  ,0);
        testingInstances.setClassIndex(testingInstances.numAttributes()-1);



    }
    public int getPrediction(int[] data){
        testingTheClassifier(createTestingInstanceProgrammatically(data));
        String num=state.substring(1,state.length());
        return Integer.parseInt(num);
    }
    public  void init () throws Exception
    {
        //getting the JsonObject and assigning it to the jo
        JSONObject jo = com.hoho.android.usbserial.examples.SerialConsoleActivity.varJson;
        device_Name = com.hoho.android.usbserial.examples.SerialConsoleActivity.device_Name;

        //declaring the variables
        csvPath = (String) jo.get("csvPathL");
       // modelPath = (String)jo.get("modelPathL");

        csvPath = "config"+"/devices/" + device_Name + "/csv/" + csvPath;
        //modelPath = "config"+"/devices/" + device_Name+ "/models/" + modelPath;
        //training the classifier.
        try {
            classifier= com.hoho.android.usbserial.examples.SerialConsoleActivity.getClassifierSVML();       }
        catch (Exception ex){
            System.err.println("Classifier(Level) not found. Training a new classifier for Level recognition");
        }
        if(classifier==null)
            trainClassifier(createTrainingDataset());
        initTestintInstances();
    }

}