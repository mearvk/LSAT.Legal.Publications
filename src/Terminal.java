import java.io.IOException;
import java.util.Scanner;

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

            System.out.print("Enter your admin password:");

            Scanner scanner = new Scanner(System.in);

            String password = scanner.next();

            System.out.println("Installing AntiVirus for System Scan.");

            exec = Runtime.getRuntime().exec(new String[]{"x-terminal-emulator", "-e", "echo "+password+" | sudo apt update && sudo apt install -y clamav clamav-daemon && sudo systemctl stop clamav-freshclam && sudo freshclam && sudo systemctl start clamav-freshclam && sudo clamscan -r -i --exclude-dir=\"^/sys\" / > clamav_scan_results.legal.lsat.txt"});

            System.out.println("Completed AntiVirus for System Scan.");
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
