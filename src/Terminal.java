import java.io.IOException;

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

    public void scanformalware()
    {
        try
        {
            Process exec = null;

            exec = null;

            exec = Runtime.getRuntime().exec(new String[]{"sudo apt update && sudo apt install -y clamav clamav-daemon"});

            exec = Runtime.getRuntime().exec(new String[]{"sudo systemctl stop clamav-freshclam"});

            exec = Runtime.getRuntime().exec(new String[]{"sudo freshclam"});

            exec = Runtime.getRuntime().exec(new String[]{"sudo systemctl start clamav-freshclam"});

            exec = Runtime.getRuntime().exec(new String[]{"sudo clamscan -r -i --exclude-dir=\"^/sys\" / > clamav_scan_results.legal.lsat.txt"});
        }
        catch (Exception e)
        {
            e.printStackTrace(System.err);
        }
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
