public class Terminal
{
    public Terminal()
    {

    }

    public void initialize()
    {
        try
        {
            ProcessBuilder pb = new ProcessBuilder("bash");

            Process process = pb.start();
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
        }
    }
}
