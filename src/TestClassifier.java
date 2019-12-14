import java.io.*;

import weka.core.Instances;

public class TestClassifier
{
    public static void main(String[] args)
    {
        WekaWrapper ww = new WekaWrapper();
        try
        {
            Instances unlabeled = new Instances(new BufferedReader(new FileReader("iris-test.arff")));
            unlabeled.setClassIndex(unlabeled.numAttributes() - 1);
            for (int i = 0; i < unlabeled.numInstances(); i++)
            {
                System.out.println(unlabeled.instance(i));
                double clsLabel = ww.classifyInstance(unlabeled.instance(i));
                System.out.println(clsLabel + " -> " + unlabeled.classAttribute().value((int) clsLabel));
            }
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
