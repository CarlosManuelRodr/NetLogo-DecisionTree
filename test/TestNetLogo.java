import org.nlogo.headless.HeadlessWorkspace;
public class TestNetLogo
{
    public static void main(String[] argv)
    {
        HeadlessWorkspace workspace = HeadlessWorkspace.newInstance();
        try
        {
            workspace.open("iris-decision-tree.nlogo",true);
            workspace.command("setup");
            workspace.command("go");
            workspace.command("test");
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
    }
}