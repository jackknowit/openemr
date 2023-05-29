import com.sun.star.document.EventObject;

public class EventListener implements com.sun.star.document.XEventListener {
    private Log logger=new Log("EventListener");
    private  EMRPad m_pad=null;
    private String m_printPreviewEventFlag = "";
    public EventListener(EMRPad pad)
    {
        m_pad=pad;

    }
    @Override
    public void notifyEvent(EventObject eventObject) {
        logger.info(eventObject.EventName);

        //打印预览会冲掉文档的鼠标和键盘事件处理对象，需要在打印预览后重新挂载文档的鼠标和键盘事件处理对象
        if (eventObject.EventName.equals("OnViewCreated") ==true && m_printPreviewEventFlag.length() <= 0)
            m_printPreviewEventFlag = "OnViewCreated";
        if (eventObject.EventName.equals("OnPageCountChange") ==true && m_printPreviewEventFlag.equals("OnViewCreated") ==true)
            m_printPreviewEventFlag = "OnPageCountChange";
        if (eventObject.EventName.equals("OnLayoutFinished") == true && m_printPreviewEventFlag.equals("OnPageCountChange") == true)
        {
            //打印预览后的事件顺序为
            //OnViewCreated
            //OnPageCountChange
            //OnLayoutFinished
            //所以当顺序发生此3个事件后，则认为关闭了打印预览窗口， 重新注册鼠标，键盘处理事件对象
            m_printPreviewEventFlag = "";

            logger.info("打印预览窗口关闭");

            m_pad.reInitEvent();

        }



    }

    @Override
    public void disposing(com.sun.star.lang.EventObject eventObject) {

    }
}
