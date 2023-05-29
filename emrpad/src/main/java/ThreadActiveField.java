

public class ThreadActiveField extends Thread{

    private EMRPad m_pad=null;
    public ThreadActiveField(EMRPad pad)
    {
        m_pad=pad;
    }
    public void run()
    {
        if(null==m_pad)
            return;

        m_pad.actField();

    }
}
