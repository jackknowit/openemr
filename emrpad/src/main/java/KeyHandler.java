import com.sun.star.awt.KeyEvent;
import com.sun.star.awt.XKeyHandler;
import com.sun.star.lang.EventObject;

public class KeyHandler implements XKeyHandler {

    private Log logger=new Log("KeyHandler");
    private  EMRPad m_pad=null;

    public KeyHandler(EMRPad pad)
    {
        m_pad=pad;

    }

    @Override
    public boolean keyPressed(KeyEvent keyEvent) {
        return m_pad.processKey(keyEvent);
        //return false;
    }

    @Override
    public boolean keyReleased(KeyEvent keyEvent) {

        return false;
    }

    @Override
    public void disposing(EventObject eventObject) {

    }
}
