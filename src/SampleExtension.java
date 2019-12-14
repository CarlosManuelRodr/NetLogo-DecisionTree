import org.nlogo.api.*;

public class SampleExtension extends DefaultClassManager
{
    public void load(PrimitiveManager primitiveManager)
    {
        primitiveManager.addPrimitive(
                "first-n-integers", new IntegerList());
    }
}