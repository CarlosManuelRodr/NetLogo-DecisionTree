import org.nlogo.headless.HeadlessWorkspace;
public class TestNetLogo
{
    public static void main(String[] argv)
    {
        HeadlessWorkspace workspace = HeadlessWorkspace.newInstance();
        try
        {
            workspace.open("out/artifacts/decision-tree.nlogo",true);
            workspace.command("setup");
            while (true)
                workspace.command("go");
        }
        catch(Exception ex) {
            ex.printStackTrace();
        }
    }
}