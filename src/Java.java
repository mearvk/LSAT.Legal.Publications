import java.io.IOException;

public class Java
{
    public static void main(String...args) throws IOException
    {
        Terminal terminal = new Terminal();

        terminal.initialize();

        //

        Runtime.getRuntime().exec("cmd /c start cmd.exe");

    }
}
