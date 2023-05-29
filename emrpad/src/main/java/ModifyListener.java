import com.sun.star.lang.EventObject;


public class ModifyListener implements com.sun.star.util.XModifyListener {
    private Log logger=new Log("ModifyListener");
    private  EMRPad m_pad=null;
    public  ModifyListener(EMRPad pad)
    {
        m_pad=pad;

    }
    @Override
    public void modified(EventObject eventObject) {
        if (true == m_pad.isReadonly())
        {
            logger.info("modify event, current readonly.");
            //m_pad.execCmd(".uno:Undo");
        }
        else
            logger.info("modify event, current edit.");

    }

    @Override
    public void disposing(EventObject eventObject) {

    }
}
