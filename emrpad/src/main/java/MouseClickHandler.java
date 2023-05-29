import com.sun.star.awt.MouseEvent;
import com.sun.star.awt.XMouseClickHandler;
import com.sun.star.lang.EventObject;

public class MouseClickHandler implements XMouseClickHandler {
    private Log logger=new Log("MouseClickHandler");
    private  EMRPad m_pad=null;
    public MouseClickHandler(EMRPad pad)
    {
        m_pad=pad;

    }

    @Override
    public boolean mousePressed(MouseEvent mouseEvent) {
        //if (m_pad.ifReadonly(null, e) == true) //如果当前点击的位置为只读，则清除undo历史
        //    m_pad.execCmd(".uno:ClearHistory");

        return false;  //false 继续处理事件，true 停止处理事件
    }

    @Override
    public boolean mouseReleased(MouseEvent mouseEvent) {
        if (m_pad.isReadonly(null, mouseEvent) == true) //如果当前为只读，则进行默认点击处理，不弹出结构化辅助输入
            return false;

        if (2 == mouseEvent.Modifiers) //如果点击结构化区域时，同时按下CTRL键，则不弹出结构化辅助输入
            return false;


        ThreadActiveField th=new ThreadActiveField(m_pad);
        th.start();


        return false;
    }

    @Override
    public void disposing(EventObject eventObject) {

    }
}
