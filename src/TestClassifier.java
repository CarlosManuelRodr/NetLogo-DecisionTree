import java.io.*;

import weka.core.Instances;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.unsupervised.attribute.Discretize;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.trees.J48;

public class TestClassifier
{
    public static void main(String[] args) throws Exception
    {
        // Load dataset
        DataSource trainSource = new DataSource("test/iris.arff");
        Instances train = trainSource.getDataSet();
        train.setClassIndex(train.numAttributes() - 1);

        DataSource testSource = new DataSource("test/iris-test.arff");
        Instances test = testSource.getDataSet();
        test.setClassIndex(train.numAttributes() - 1);

        // Filter
        Discretize discretize = new Discretize();

        // Classifier
        J48 j48 = new J48();
        j48.setUnpruned(true);

        // Meta-classifier
        FilteredClassifier fc = new FilteredClassifier();
        fc.setFilter(discretize);
        fc.setClassifier(j48);

        // Train and make predictions
        fc.buildClassifier(train);

        for (int i = 0; i < test.numInstances(); i++)
        {
            double pred = fc.classifyInstance(test.instance(i));
            System.out.print("ID: " + test.instance(i).value(0));
            System.out.print(", actual: " + test.classAttribute().value((int) test.instance(i).classValue()));
            System.out.println(", predicted: " + test.classAttribute().value((int) pred));
        }
    }
}
