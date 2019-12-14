// Generated with Weka 3.9.3
//
// This code is public domain and comes with no warranty.
//
// Timestamp: Tue Nov 26 22:56:05 CST 2019

// package weka.classifiers;

import weka.core.Attribute;
import weka.core.Capabilities;
import weka.core.Capabilities.Capability;
import weka.core.Instance;
import weka.core.Instances;
import weka.core.RevisionUtils;
import weka.classifiers.Classifier;
import weka.classifiers.AbstractClassifier;

public class WekaWrapper extends AbstractClassifier
{
    /**
     * Returns only the toString() method.
     *
     * @return a string describing the classifier
     */
    public String globalInfo() {
        return toString();
    }

    /**
     * Returns the capabilities of this classifier.
     *
     * @return the capabilities
     */
    public Capabilities getCapabilities()
    {
        weka.core.Capabilities result = new weka.core.Capabilities(this);

        result.enable(weka.core.Capabilities.Capability.NOMINAL_ATTRIBUTES);
        result.enable(weka.core.Capabilities.Capability.NUMERIC_ATTRIBUTES);
        result.enable(weka.core.Capabilities.Capability.DATE_ATTRIBUTES);
        result.enable(weka.core.Capabilities.Capability.MISSING_VALUES);
        result.enable(weka.core.Capabilities.Capability.NOMINAL_CLASS);
        result.enable(weka.core.Capabilities.Capability.MISSING_CLASS_VALUES);


        result.setMinimumNumberInstances(0);

        return result;
    }

    /**
     * only checks the data against its capabilities.
     *
     * @param i the training data
     */
    public void buildClassifier(Instances i) throws Exception
    {
        // can classifier handle the data?
        getCapabilities().testWithFail(i);
    }

    /**
     * Classifies the given instance.
     *
     * @param i the instance to classify
     * @return the classification result
     */
    public double classifyInstance(Instance i) throws Exception
    {
        Object[] s = new Object[i.numAttributes()];
        for (int j = 0; j < s.length; j++)
        {
            if (!i.isMissing(j))
            {
                if (i.attribute(j).isNominal())
                    s[j] = new String(i.stringValue(j));
                else if (i.attribute(j).isNumeric())
                    s[j] = new Double(i.value(j));
            }
        }

        // set class value to missing

        s[i.classIndex()] = null;

        return WekaClassifier.classify(s);
    }

    /**
     * Returns the revision string.
     *
     * @return        the revision
     */
    public String getRevision() {
        return RevisionUtils.extract("1.0");
    }

    /**
     * Returns only the classnames and what classifier it is based on.
     *
     * @return a short description
     */
    public String toString()
    {
        return "Auto-generated classifier wrapper, based on weka.classifiers.trees.J48 (generated with Weka 3.9.3).\n" + this.getClass().getName() + "/WekaClassifier";
    }

    /**
     * Runs the classfier from commandline.
     *
     * @param args the commandline arguments
     */
    public static void main(String args[]) {
        runClassifier(new WekaWrapper(), args);
    }
}

class WekaClassifier
{

    public static double classify(Object[] i) throws Exception
    {

        double p = Double.NaN;
        p = WekaClassifier.N2a88b61c0(i);
        return p;
    }
    static double N2a88b61c0(Object []i)
    {
        double p = Double.NaN;
        if (i[3] == null) {
            p = 0;
        } else if (((Double) i[3]).doubleValue() <= 0.6) {
            p = 0;
        } else if (((Double) i[3]).doubleValue() > 0.6) {
            p = WekaClassifier.N3a1995401(i);
        }
        return p;
    }
    static double N3a1995401(Object []i) {
        double p = Double.NaN;
        if (i[3] == null) {
            p = 1;
        } else if (((Double) i[3]).doubleValue() <= 1.7) {
            p = WekaClassifier.N18951d8c2(i);
        } else if (((Double) i[3]).doubleValue() > 1.7) {
            p = 2;
        }
        return p;
    }
    static double N18951d8c2(Object []i) {
        double p = Double.NaN;
        if (i[2] == null) {
            p = 1;
        } else if (((Double) i[2]).doubleValue() <= 4.9) {
            p = 1;
        } else if (((Double) i[2]).doubleValue() > 4.9) {
            p = WekaClassifier.N656b7be53(i);
        }
        return p;
    }
    static double N656b7be53(Object []i) {
        double p = Double.NaN;
        if (i[3] == null) {
            p = 2;
        } else if (((Double) i[3]).doubleValue() <= 1.5) {
            p = 2;
        } else if (((Double) i[3]).doubleValue() > 1.5) {
            p = 1;
        }
        return p;
    }
}
