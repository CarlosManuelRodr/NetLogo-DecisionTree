import weka.core.Attribute;
import weka.core.DenseInstance;
import weka.core.Instance;
import weka.core.Instances;

import java.io.*;

public class Classify
{
    public static void main(String[] args) throws IOException
    {
        String arg = args[0];
        WekaWrapper ww = new WekaWrapper();
        Instances testSet = new Instances(new BufferedReader(new FileReader("iris-test.arff")));
        int numAttributes = testSet.numAttributes();
        Instance inst = new DenseInstance(numAttributes);
        inst.setDataset(testSet);
        testSet.setClassIndex(numAttributes - 1);
        String[] vals = arg.split(",");
        Attribute[] attributes = new Attribute[numAttributes];
        for(int i=0; i<numAttributes-1; i++)
        {
            attributes[i] = testSet.attribute(i);
        }

        int j = 0;
        for(String val:vals)
        {
            if(attributes[j].isNumeric())
            {
                double valDouble = Double.parseDouble(val);
                inst.setValue(attributes[j], valDouble);
            }
            else
            {
                inst.setValue(attributes[j], val);
            }
            j++;
        }
        try
        {
            double clsLabel = ww.classifyInstance(inst);
            System.out.println(testSet.classAttribute().value((int) clsLabel));
        }
        catch (Exception e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
}

