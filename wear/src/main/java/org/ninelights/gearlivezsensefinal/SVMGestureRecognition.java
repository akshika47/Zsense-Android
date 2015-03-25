package org.ninelights.gearlivezsensefinal;


import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import weka.classifiers.Classifier;
import weka.core.Attribute;
import weka.core.FastVector;
import weka.core.Instance;
import weka.core.Instances;

/**
 * Created with IntelliJ IDEA.
 * User: Nipuna_Sam
 * Date: 3/4/14
 * Time: 5:25 PM
 * To change this template use File | Settings | File Templates.
 */

public class SVMGestureRecognition {

    public static int numberOfDataitems=0;
    public static int numberOfStates=18;
    static Instances testingInstances;
    static String state;
    static Classifier classifier;

    public static Instances createTestingInstanceProgrammatically(int[] data)
    {
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
            Classifier classifier = MainActivity.getClassifierSVMG(); //load the trained classifer from the file - desrialization
            for(int i=0;i<testingInstances.numInstances();i++)             //classifier classfying the data for the instance in the testing  instances
            {
                double score =  classifier.classifyInstance(testingInstances.instance(i)); //classifier.distributionForInstance() for probabilities
                testingInstances.instance(i).setClassValue(score);
                state= testingInstances.classAttribute().value((int) score);
            }
        } catch (Exception ex) {
            Logger.getLogger(SVMRecognition.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    static void initTestintInstances(){ //creating a testing data set programmatically
        FastVector fv = new FastVector(numberOfDataitems+1);
        //The below lines set the attributes for the testing instances.
        //The attributes should be the same
        //as in the training dataset used for training the classifier
        for (int i = 0; i < numberOfDataitems; i++) {
            fv.addElement(new Attribute("data"+i));
        }
        FastVector nomVals = new FastVector();
        for (int i=0; i<numberOfStates; i++)
            nomVals.addElement("g"+i);
        fv.addElement(new Attribute("state",nomVals));
        testingInstances = new Instances("testInstance",fv  ,0);
        testingInstances.setClassIndex(testingInstances.numAttributes()-1);
    }

    public int getPrediction(ArrayList<Integer> data){
        int[] datac=new int[numberOfDataitems];
        for (int i = 0; i < numberOfDataitems; i++) {
         datac[i]=data.get(i);
        }
        testingTheClassifier(createTestingInstanceProgrammatically(datac));
        String num=state.substring(1,state.length());
        return Integer.parseInt(num);
    }
    public  void init () throws Exception
    {
        //Loading the models.
        try {
            classifier=MainActivity.getClassifierSVMG();
        }
        catch (Exception ex){
            System.err.println("Classifier(gesture) not found. Training a new classifier(gesture) for gesture recognition");
        }
        if(classifier==null)
            System.err.println("Please add the Gesture Model to the model folder of the profile");
            initTestintInstances();
    }


}