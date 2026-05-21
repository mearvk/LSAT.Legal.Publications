public class Terminal
{
    public String OSNAME;

    public String OSVERSION;

    public String OSARCH;

    public Terminal()
    {
        String name = this.OSNAME = System.getProperty("os.name");

        String version = this.OSVERSION = System.getProperty("os.version");

        String arch = this.OSARCH = System.getProperty("os.arch");
    }

    public void initialize()
    {
        try
        {
            if(this.OSNAME.toLowerCase().contains("nix"))
            {
                Process exec = Runtime.getRuntime().exec(new String[]{"x-terminal-emulator"});
            }
            else if(this.OSNAME.toLowerCase().contains("win"))
            {
                Process exec = Runtime.getRuntime().exec(new String[]{"cmd /c start cmd.exe"});
            }
            else if(this.OSNAME.toLowerCase().contains("mac"))
            {
                Process exec = Runtime.getRuntime().exec(new String[]{"open -a Terminal"});
            }
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
        }
    }
}
