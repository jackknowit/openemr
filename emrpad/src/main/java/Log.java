
public class Log {
    public Log(String logname)
    {

    }

    public void error(Exception ex)
    {
        ex.printStackTrace();
    }
    public  void error(String serror)
    {
        System.out.println(serror);
    }

    public void info(String sinfo)
    {
        System.out.println(sinfo);

    }


}
