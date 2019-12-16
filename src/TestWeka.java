import java.util.ArrayList;
import java.util.List;

import weka.core.DenseInstance;
import weka.core.Instances;
import weka.core.Instance;
import weka.core.Attribute;
import weka.core.converters.ConverterUtils.DataSource;
import weka.filters.unsupervised.attribute.Discretize;
import weka.classifiers.meta.FilteredClassifier;
import weka.classifiers.trees.J48;

public class TestWeka
{
    public static void main(String[] args) throws Exception
    {
        // Load dataset
        DataSource trainSource = new DataSource("test/iris.arff");
        Instances train = trainSource.getDataSet();
        train.setClassIndex(train.numAttributes() - 1);

        // Create test dataset manually
        ArrayList<Attribute> atts = new ArrayList<Attribute>(5);
        atts.add(new Attribute("sepallength"));
        atts.add(new Attribute("sepalwidth"));
        atts.add(new Attribute("petallength"));
        atts.add(new Attribute("petalwidth"));

        List class_values = new ArrayList(3);
        class_values.add("Iris-setosa");
        class_values.add("Iris-versicolor");
        class_values.add("Iris-virginica");
        atts.add(new Attribute("class", class_values));

        Instances test = new Instances("testdata", atts, 0);
        Instance inst = new DenseInstance(5);
        inst.setValue(0, 5.1);
        inst.setValue(1, 3.5);
        inst.setValue(2, 1.4);
        inst.setValue(3, 0.2);
        test.add(inst);

        inst = new DenseInstance(5);
        inst.setValue(0, 4.9);
        inst.setValue(1, 3.0);
        inst.setValue(2, 1.4);
        inst.setValue(3, 0.2);
        test.add(inst);

        inst = new DenseInstance(5);
        inst.setValue(0, 7.0);
        inst.setValue(1, 3.2);
        inst.setValue(2, 4.7);
        inst.setValue(3, 1.4);
        test.add(inst);

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
