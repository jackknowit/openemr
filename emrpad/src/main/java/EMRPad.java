import com.sun.star.awt.*;
import com.sun.star.awt.Rectangle;
import com.sun.star.beans.PropertyValue;
import com.sun.star.frame.XFrame;
import com.sun.star.frame.XFrame2;
import com.sun.star.lang.SystemDependent;
import com.sun.star.lang.XMultiComponentFactory;
import com.sun.star.lang.XMultiServiceFactory;
import com.sun.star.uno.Any;
import com.sun.star.uno.UnoRuntime;
import ooo.connector.BootstrapSocketConnector;
import org.apache.commons.lang3.SystemUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;

import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.InputStream;
import java.net.URL;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.Arrays;

public class EMRPad {

    Log logger = new Log("EMRPad");

    private XMultiServiceFactory m_xFactory=null;
    private com.sun.star.uno.XComponentContext m_xContext = null;
    private XFrame m_xFrame =null;
    private com.sun.star.frame.XComponentLoader m_xCLoader=null;
    private com.sun.star.text.XTextDocument m_xDoc = null;
    private com.sun.star.lang.XComponent m_xComponent = null;


    private MouseClickHandler m_handlerMouseClick = null;
    private KeyHandler m_handlerKey = null;
    private ModifyListener m_listenerModify = null;
    private EventListener m_listenerEvent = null;


    private void initEvent()
    {
        //挂载鼠标、键盘事件
        if (null == m_xDoc)
            return;


        if (null == m_handlerMouseClick)
            m_handlerMouseClick = new MouseClickHandler(this);

        if (null == m_handlerKey)
            m_handlerKey = new KeyHandler(this);

        com.sun.star.awt.XUserInputInterception xui = UnoRuntime.queryInterface(com.sun.star.awt.XUserInputInterception.class,m_xDoc.getCurrentController());

        if (xui != null) {
            xui.addMouseClickHandler(m_handlerMouseClick);
            xui.addKeyHandler(m_handlerKey);

        }

    }

    private void initListen()
    {
        //挂载文档监听
        if (null == m_xDoc)
            return;

        if (null == m_listenerModify)
            m_listenerModify = new ModifyListener(this);
        if (null == m_listenerEvent)
            m_listenerEvent = new EventListener(this);

        com.sun.star.util.XModifyBroadcaster xmb =UnoRuntime.queryInterface(com.sun.star.util.XModifyBroadcaster.class,m_xDoc);
        if (null != xmb)
        {
            xmb.addModifyListener(m_listenerModify);
        }

        com.sun.star.document.XEventBroadcaster xeb =UnoRuntime.queryInterface(com.sun.star.document.XEventBroadcaster.class,m_xDoc);
        if(null!=xeb)
        {
            xeb.addEventListener(m_listenerEvent);
        }

    }

    private static String getTemporaryPathWithEndSlash()
    {
        String sTmpPath="";
        if(SystemUtils.IS_OS_WINDOWS)
            sTmpPath=System.getProperty("java.io.tmpdir")+"emrpad"+FileSystems.getDefault().getSeparator();
        else if(SystemUtils.IS_OS_LINUX)
            sTmpPath=System.getProperty("java.io.tmpdir")+FileSystems.getDefault().getSeparator()+"emrpad"+FileSystems.getDefault().getSeparator();

        File file=new File(sTmpPath);
        if(file.exists()==false)
            file.mkdirs();

        return sTmpPath;
    }
    private static String getTemporaryFileNameNoCreate(String prefix,String ext)
    {
        String sTmpPath= getTemporaryPathWithEndSlash();

        int i=0;
        String sTmpFile=sTmpPath+prefix+"_"+String.valueOf(i)+ext;
        File file=new File(sTmpFile);
        while(file.exists()==true)  //如果文件存在，则新增序号直到临时文件不存在
        {
            i++;
            sTmpFile=sTmpPath+prefix+"_"+String.valueOf(i)+ext;
            file=new File(sTmpFile);

        }

        return sTmpFile;


    }

    public static String newFileFromTemplate(String filename)
    {
        try
        {
            File file=new File(filename);
            if(file.exists()==false)
                return "";

           String sfileName=file.getName();

            String sfileNameNoExt=sfileName;
            if (sfileName.indexOf(".") > 0) {
                sfileNameNoExt= sfileName.substring(0, sfileName.lastIndexOf("."));
            }


           String sCopyFileName=getTemporaryFileNameNoCreate(sfileNameNoExt,".odt");

            Path copied = Paths.get(sCopyFileName);
            Path originalPath = Paths.get(filename);
            Files.copy(originalPath, copied, StandardCopyOption.REPLACE_EXISTING);

            if(copied.toFile().exists()==false)
                return "";

            return sCopyFileName;
        }
        catch (Exception ex)
        {
            ex.printStackTrace();
            return "";
        }


    }

    public  boolean saveFile()
    {
        try {
            execCmd(".uno:Save", null);

            return true;
        }
        catch (Exception ex)
        {
            logger.error(ex);
            return false;
        }

    }

    public boolean saveAsFile(String filename)
    {
        try
        {
            if(isInit()==false)
                return false;

            if(filename==null || filename.trim().length()<=0)
            {
                FileDialog dialog = new FileDialog(getShell(), SWT.SAVE);
                dialog.setFilterPath("");// 设置默认的路径
                dialog.setText("另存文件");//设置对话框的标题
                dialog.setFileName("");//设置默认的文件名
                dialog.setFilterNames(new String[]{"ODF文档 (*.odt)", "所有文件(*.*)"});//设置扩展名
                dialog.setFilterExtensions(new String[]{"*.odt", "*.*"});//设置文件扩展名
                filename = dialog.open();//
                if (null == filename || filename.trim().length()<=0) {
                    return false;
                }
            }

            String urlfile="file:///"+filename.replace('\\', '/');
            logger.info("save file: "+urlfile);


            com.sun.star.frame.XStorable xStorable = UnoRuntime.queryInterface(com.sun.star.frame.XStorable.class, m_xComponent);
            com.sun.star.beans.PropertyValue[] propertyValue = new com.sun.star.beans.PropertyValue[1];
            propertyValue[0] = new com.sun.star.beans.PropertyValue();
            propertyValue[0].Name = "Overwrite";
            propertyValue[0].Value = new Any(boolean.class,true);

            xStorable.storeAsURL(urlfile, propertyValue);
            File file=new File(filename);
            if (file.exists() == false)
                return false;

            m_bNew=false;

            return true;
        }
        catch (Exception ex)
        {
            logger.error(ex);
            return false;
        }
    }

    public void newFile()
    {
        closeDoc();
        openWriter(m_xContext,"",m_panelParent);
    }
    public boolean openFile(String filename)
    {
        try
        {





            closeDoc();
            openWriter(m_xContext,filename,m_panelParent);

            return true;

        }
        catch (Exception ex)
        {
            logger.error(ex);
            return false;
        }
    }

    /// 写office 配置
    /// C:\Users\用户名\AppData\Roaming\LibreOffice\4\ user\registrymodifications.xcu 中的配置项
    /// <param name="cfgName"></param>
    /// <param name="cfgValue">cfgValue为逻辑型属性值</param>
    private boolean setOfficeConfig(String cfgName, Class valClass ,Object val, String sPath)
    {
        try
        {
            XMultiServiceFactory xFactory= getXMultiServiceFactory();

            if (null == xFactory)
                return false;

            com.sun.star.lang.XMultiServiceFactory ocp =UnoRuntime.queryInterface(com.sun.star.lang.XMultiServiceFactory.class, xFactory.createInstance("com.sun.star.configuration.ConfigurationProvider"));

            com.sun.star.beans.PropertyValue pp = new com.sun.star.beans.PropertyValue();
            pp.Name = "nodepath";
            pp.Value = new Any(sPath.getClass(),sPath);

            Any ua = new Any(com.sun.star.beans.PropertyValue.class, pp);

            Object ocuaa = ocp.createInstanceWithArguments("com.sun.star.configuration.ConfigurationUpdateAccess", new Any[] { ua });
            //object ocuaa = ocp.createInstanceWithArguments("com.sun.star.configuration.ConfigurationAccess", new uno.Any[] { ua });
            com.sun.star.beans.XPropertySet xps =UnoRuntime.queryInterface(com.sun.star.beans.XPropertySet.class,ocuaa);

            xps.setPropertyValue(cfgName, new Any(valClass,val));


            com.sun.star.util.XChangesBatch xUpdateControl =UnoRuntime.queryInterface(com.sun.star.util.XChangesBatch.class, ocuaa);
            xUpdateControl.commitChanges();

            return true;


        }
        catch (Exception ex)
        {
            logger.error(ex);
            return false;
        }



    }

    /// 写office 配置
    /// 重载
    private boolean setOfficeConfig(String cfgName,String cfgValue,String sPath)
    {
        if(cfgValue==null)
            return false;

        return setOfficeConfig(cfgName,cfgValue.getClass(),cfgValue,sPath);
    }
    /// 写office 配置
    /// 重载
    private boolean setOfficeConfig(String cfgName,Boolean cfgValue,String sPath)
    {
        if(cfgValue==null)
            return false;

        return setOfficeConfig(cfgName,cfgValue.getClass(),cfgValue,sPath);
    }
    /// 读取office 配置
    /// C:\Users\用户名\AppData\Roaming\LibreOffice\4\ user\registrymodifications.xcu 中的配置项

    private String getOfficeConfig(String cfgName)
    {
        try
        {

            if (false == isInit())
                return "";

            XMultiServiceFactory xFactory= getXMultiServiceFactory();

            if (null == xFactory)
                return "";

            com.sun.star.lang.XMultiServiceFactory ocp =UnoRuntime.queryInterface(com.sun.star.lang.XMultiServiceFactory.class, xFactory.createInstance("com.sun.star.configuration.ConfigurationProvider"));

            com.sun.star.beans.PropertyValue pp = new com.sun.star.beans.PropertyValue();
            pp.Name = "nodepath";
            pp.Value =new Any(String.class, "/org.openoffice.UserProfile/Data");

            Any ua = new Any(com.sun.star.beans.PropertyValue.class, pp);

            Object ocuaa = ocp.createInstanceWithArguments("com.sun.star.configuration.ConfigurationUpdateAccess", new Any[] { ua });
            //object ocuaa = ocp.createInstanceWithArguments("com.sun.star.configuration.ConfigurationAccess", new uno.Any[] { ua });
            com.sun.star.beans.XPropertySet xps=UnoRuntime.queryInterface(com.sun.star.beans.XPropertySet.class, ocuaa);

            Object sany = xps.getPropertyValue(cfgName);
            if (null==sany)
                return "";

            return String.valueOf(sany);


        }
        catch (Exception ex)
        {
            logger.error(ex);
            return "";
        }
    }


    private void showFrameLayoutElement(boolean bShow,String elementPath)
    {

            try
            {
                if (isInit() == false)
                    return;

                com.sun.star.frame.XFrame2 frame=UnoRuntime.queryInterface(com.sun.star.frame.XFrame2.class, m_xFrame);
                if (null == frame || null == frame.getLayoutManager())
                    return;

                com.sun.star.frame.XLayoutManager2 layout = UnoRuntime.queryInterface(com.sun.star.frame.XLayoutManager2.class, frame.getLayoutManager());
                if (null == layout)
                    return;

                if (true==bShow)
                {
                    layout.showElement(elementPath);
                }
                else if (false == bShow)
                {
                    layout.hideElement(elementPath);
                }


            }
            catch(Exception ex)
            {
                logger.error(ex);
            }
    }
    //libreoffice工具栏部分按钮是否显示
    private void showStdToolBtn(boolean bShow)
    {
        try
        {
            if (isInit() == false)
                return;

            com.sun.star.frame.XFrame2 frame=UnoRuntime.queryInterface(com.sun.star.frame.XFrame2.class, m_xFrame);
            if (null == frame || null == frame.getLayoutManager())
                return;

            com.sun.star.frame.XLayoutManager2 layout = UnoRuntime.queryInterface(com.sun.star.frame.XLayoutManager2.class, frame.getLayoutManager());
            if (null == layout)
                return;

            ArrayList<Integer> hideBtnIndexs = new ArrayList<Integer>();

            com.sun.star.ui.XUIElement oTB = layout.getElement("private:resource/toolbar/standardbar");
            com.sun.star.ui.XUIElementSettings oToolbar =UnoRuntime.queryInterface(com.sun.star.ui.XUIElementSettings.class, oTB);

            java.util.List<String> needHideToolBtnCommandURLs=Arrays.asList(new String[]{
                    ".uno:OpenUrl",
                    ".uno:AddDirect",
                    ".uno:NewDoc",
                    ".uno:OpenFromWriter",
                    ".uno:OpenRemote",
                    ".uno:Save",
                    ".uno:SaveAs"

            });
            //".uno:EditDoc",
            //".uno:ReadOnlyDoc"

            ArrayList<com.sun.star.beans.PropertyValue[]> hideBtnPvs=new ArrayList<>();
            if (null != oTB && null != oToolbar)
            {
                com.sun.star.container.XIndexReplace oToolbarSettings =  UnoRuntime.queryInterface(com.sun.star.container.XIndexReplace.class,oToolbar.getSettings(true));
                for (int i = 0; i < oToolbarSettings.getCount(); i++)
                {
                    Object oButtonSettings =  oToolbarSettings.getByIndex(i);
                    if (null == oButtonSettings)
                        continue;

                    com.sun.star.beans.PropertyValue[] pvs =UnoRuntime.queryInterface(com.sun.star.beans.PropertyValue[].class,oButtonSettings);
                    boolean bFind=false;
                    for (int j = 0; j < pvs.length; j++)
                    {
                        com.sun.star.beans.PropertyValue pv = pvs[j];
                        if ("CommandURL".equals(pv.Name))
                        {
                            if (null == pv.Value)
                                continue;

                            String scmdID = pv.Value.toString();
                            if (needHideToolBtnCommandURLs.contains(scmdID))
                            {
                                hideBtnIndexs.add(i);
                                bFind=true;

                            }
                        }
                    }

                    if(true==bFind)
                        hideBtnPvs.add(pvs);

                }

                if (hideBtnIndexs.size() > 0 && hideBtnIndexs.size()==hideBtnPvs.size())
                {

                    for(int i = 0; i < hideBtnIndexs.size(); i++)
                    {
                        int iIndexBtn = hideBtnIndexs.get(i);
                        com.sun.star.beans.PropertyValue[] pvs=hideBtnPvs.get(i);

                        for (int j = 0; j < pvs.length; j++)
                        {
                            com.sun.star.beans.PropertyValue pv = pvs[j];
                            if ("IsVisible" .equals(pv.Name))
                            {
                                pv.Value = new Any(boolean.class,bShow);
                            }
                        }

                        Object oButtonSettings = new Any(pvs.getClass(), pvs);
                        oToolbarSettings.replaceByIndex(iIndexBtn, oButtonSettings);
                        oToolbar.setSettings(oToolbarSettings);



                    }


                }

            }


        }
        catch (Exception ex)
        {
            logger.error(ex);

        }
    }

    public void exit()
    {
        execCmd(".uno:Quit",null);
    }
    private Object execCmd(String sCmd, PropertyValue[] param)
    {
        XFrame2 fe = (com.sun.star.frame.XFrame2) UnoRuntime.queryInterface(
                com.sun.star.frame.XFrame2.class, m_xFrame);

        printMessage("exec cmd: " + sCmd);

        return execFrameCmd(fe, sCmd,param);
    }

    public void printMessage(String s)
    {
        System.out.println(s);

    }

    private Object execFrameCmd(XFrame fe,String sCmd, PropertyValue[] param)
    {

        try
        {
            com.sun.star.frame.XDispatchHelper disp = (com.sun.star.frame.XDispatchHelper)UnoRuntime.queryInterface(com.sun.star.frame.XDispatchHelper.class,getXMultiServiceFactory().createInstance("com.sun.star.frame.DispatchHelper") );
            if(null==disp)
                return Any.VOID;
            com.sun.star.frame.XDispatchProvider dp =(com.sun.star.frame.XDispatchProvider) UnoRuntime.queryInterface(
                com.sun.star.frame.XDispatchProvider.class, fe);
            if(null==dp)
                return Any.VOID;

            if (null == param)
                param = new com.sun.star.beans.PropertyValue[] { };

            Object ret= disp.executeDispatch(dp, sCmd, "", 0, param);
            return ret;
        }
        catch (Exception ex)
        {

            return Any.VOID;
        }
    }


    public void test(int tid)
    {


    }
    public void closeDoc()
    {
        try {
            if(null!=m_xComponent)
            {
                instScript("empty_script.txt"); //卸载宏脚本
                unEvent();
                unListen();

            }

            com.sun.star.frame.XModel xModel =
                    (com.sun.star.frame.XModel) UnoRuntime.queryInterface(
                            com.sun.star.frame.XModel.class, m_xDoc);

            if (xModel != null) {
                // It is a full featured office document.
                // Try to use close mechanism instead of a hard dispose().
                // But maybe such service is not available on this model.
                com.sun.star.util.XCloseable xCloseable =
                        (com.sun.star.util.XCloseable) UnoRuntime.queryInterface(
                                com.sun.star.util.XCloseable.class, xModel);

                if (xCloseable != null) {

                    // use close(boolean DeliverOwnership)
                    // The boolean parameter DeliverOwnership tells objects vetoing the close process that they may
                    // assume ownership if they object the closure by throwing a CloseVetoException
                    // Here we give up ownership. To be on the safe side, catch possible veto exception anyway.
                    xCloseable.close(true);

                }
                // If close is not supported by this model - try to dispose it.
                // But if the model disagree with a reset request for the modify state
                // we shouldn't do so. Otherwhise some strange things can happen.
                else {
                    com.sun.star.lang.XComponent xDisposeable =
                            (com.sun.star.lang.XComponent) UnoRuntime.queryInterface(
                                    com.sun.star.lang.XComponent.class, xModel);
                    xDisposeable.dispose();

                }

            }
        }
        catch (Exception ex)
        {
            logger.error(ex);

        }
    }

    public String m_sFileNameNoExt="";

    private boolean m_bNew=false;
    public boolean isNew()
    {
        return m_bNew;
    }
    public boolean setModified(Boolean bmodify)
    {
        try
        {
            com.sun.star.util.XModifiable xModifiable = (com.sun.star.util.XModifiable) UnoRuntime.queryInterface(com.sun.star.util.XModifiable.class, m_xComponent);
            xModifiable.setModified(bmodify);
            return xModifiable.isModified();

        }
        catch (Exception ex)
        {
            logger.error(ex);
            return false;
        }
    }

    public boolean isModified()
    {
        try
        {
            com.sun.star.util.XModifiable xModifiable = (com.sun.star.util.XModifiable) UnoRuntime.queryInterface(com.sun.star.util.XModifiable.class, m_xComponent);
            return xModifiable.isModified();

        }
        catch (Exception ex)
        {
            logger.error(ex);
            return false;
        }
    }
    private  Boolean openWriter(com.sun.star.uno.XComponentContext xContext ,String sfilename,Widget panelParent)
    {


        try {
            String sTitle="新文件";
            // get the remote office service manager
            com.sun.star.lang.XMultiComponentFactory xMCF =
                    xContext.getServiceManager();

            Object oDesktop = xMCF.createInstanceWithContext(
                    "com.sun.star.frame.Desktop", xContext);

            m_xCLoader = UnoRuntime.queryInterface(com.sun.star.frame.XComponentLoader.class,
                    oDesktop);
            com.sun.star.beans.PropertyValue [] szEmptyArgs =
                    new com.sun.star.beans.PropertyValue [0];

            String urlfile="";

            if(null==sfilename || sfilename.length()<=0)
            {
                urlfile = "private:factory/swriter";
                m_bNew=true;
            }
            else
            {
                File file=new File(sfilename);
                String[] sar=file.getName().split("\\.");
                if(sar!=null && sar.length>0)
                    sTitle=sar[0];

                urlfile="file:///"+sfilename.replace('\\', '/');

                m_bNew=false;

            }
            logger.info("open file: "+urlfile);
            m_xComponent = m_xCLoader.loadComponentFromURL(urlfile,"_blank" , 0, szEmptyArgs);
            m_xDoc = UnoRuntime.queryInterface(com.sun.star.text.XTextDocument.class,
                    m_xComponent);


            m_xFrame = m_xDoc.getCurrentController().getFrame();

            if(null== m_xFrame)
                return false;

            m_sFileNameNoExt=sTitle;
            //getShell().setText(sTitle);


            XWindow xWin= m_xFrame.getContainerWindow();
            XSystemDependentWindowPeer xSysDepWinPeer=UnoRuntime.queryInterface(XSystemDependentWindowPeer.class, xWin);

            byte[] procID = new byte[0];

            if(SystemUtils.IS_OS_WINDOWS)
            {
                //嵌入libreoffice 窗口 至swt
                Object objWin=xSysDepWinPeer.getWindowHandle(procID,SystemDependent.SYSTEM_WIN32);
                m_lParent =((Composite)panelParent).handle;
                m_lChild =(long)objWin;

                //设置libreoffice 窗口大小 为swt控件大小
                Composite cpChild=new Composite(((Composite)panelParent).getShell(), SWT.NONE);
                cpChild.handle= m_lChild;
                cpChild.setParent(((Composite)panelParent));
                org.eclipse.swt.graphics.Rectangle rcParent=((Composite)panelParent).getBounds();
                cpChild.setBounds(rcParent);
                cpChild.setLocation(0,0);

            }
            else if(SystemUtils.IS_OS_LINUX)
            {
                //嵌入libreoffice 窗口 至swt
                Object objLinux=xSysDepWinPeer.getWindowHandle(procID,SystemDependent.SYSTEM_XWINDOW);
                SystemDependentXWindow  xSysDepWin=(SystemDependentXWindow) objLinux;

                // the lparent is swt GtkWidget*
                m_lParent =((Label)panelParent).handle;
                //the lchild is XLib Window obj
                m_lChild =xSysDepWin.WindowHandle;

                long lret= new NativeToolsX11().set_parent_gtk(m_lParent, m_lChild);

                if(lret!=1024)
                {
                    //返回值不等于约定的返回值1024，说明JNI调用出错了
                    logger.error("Calling jni gtk failed.");
                    return false;

                }



            }
            else if(SystemUtils.IS_OS_MAC)
            {

            }

            showFrameLayoutElement(false,"private:resource/menubar/menubar"); //隐藏菜单
            showFrameLayoutElement(false,"private:resource/toolbar/standardbar"); //隐藏标准工具栏
            //showStdToolBtn(false);  //隐藏标准工具栏中的按钮执行太慢，改为用showFrameLayoutElement直接隐藏标准工具栏
            removeLibreofficeBorder(true);


            initEvent();
            initListen();



            return true;

        } catch(Exception e){
            logger.error(e);
            return false;
        }
    }

    private void unEvent()
    {
        //删除鼠标、键盘事件
        if (null == m_xDoc)
            return;


        com.sun.star.awt.XUserInputInterception xui =UnoRuntime.queryInterface(com.sun.star.awt.XUserInputInterception.class,
                m_xDoc.getCurrentController());

        if (xui != null)
        {
            if(m_handlerMouseClick!=null)
                xui.removeMouseClickHandler(m_handlerMouseClick);


            if(m_handlerKey!=null)
                xui.removeKeyHandler(m_handlerKey);

        }



    }
    private void unListen()
    {
        //删除监听
        if (null == m_xDoc)
            return;

        com.sun.star.util.XModifyBroadcaster xmb =UnoRuntime.queryInterface(com.sun.star.util.XModifyBroadcaster.class,
                m_xDoc);
        if (null != xmb)
        {
            if(m_listenerModify!=null)
                xmb.removeModifyListener(m_listenerModify);
        }

        com.sun.star.document.XEventBroadcaster xeb =UnoRuntime.queryInterface(com.sun.star.document.XEventBroadcaster.class,
                m_xDoc);
        if (null != xeb)
        {
            if(m_listenerEvent!=null)
                xeb.removeEventListener(m_listenerEvent);
        }

    }

    public void reInitEvent()
    {
        unEvent();
        initEvent();
    }

    public void allowSet()
    {

        m_bCanSet = true;
    }
    public void UnAllowSet()
    {
        m_bCanSet = false;

    }
    private Boolean m_bCanSet = false;
    private Boolean m_bReadonly=false;
    public boolean isReadonly()
    {
        if (true == m_bCanSet)
            return false;

        return m_bReadonly;
    }
    public boolean isReadonly(com.sun.star.awt.KeyEvent key, com.sun.star.awt.MouseEvent mouse)
    {
        try
        {

            m_bReadonly = false;

            if (false == isInit())
            {
                m_bReadonly = true;
                return true;
            }


            AreaData area = detectArea(null); //检测当前输入点在只读字段上，返回只读
            if (null != area)
            {
                if (area.Editable.equals("R"))
                {
                    m_bReadonly = true;
                    return true;
                }
            }


            m_bReadonly = false;
            return false;
        }
        catch(Exception ex)
        {
            logger.error(ex);

            m_bReadonly = false;
            return false;
        }

    }

    private void invalidOffice()
    {
        if (m_panelParent != null)
        {
            if(SystemUtils.IS_OS_WINDOWS) {

              org.eclipse.swt.graphics.Point ptOld=((Composite)m_panelParent).getSize();
              ((Composite)m_panelParent).setSize(ptOld.x-4,ptOld.y-4);
                ((Composite)m_panelParent).setSize(ptOld);

            }
            else if(SystemUtils.IS_OS_LINUX) {
                org.eclipse.swt.graphics.Point ptOld=((Label)m_panelParent).getSize();
                ((Label)m_panelParent).setSize(ptOld.x-4,ptOld.y-4);
                ((Label)m_panelParent).setSize(ptOld);

            }

        }
    }

    public void goDocStart()
    {

        try
        {
            if (false == isInit())
                return;

            com.sun.star.frame.XController xController = m_xDoc.getCurrentController();
            if (null == xController)
                return;

            com.sun.star.text.XTextViewCursorSupplier supTextViewCursor =UnoRuntime.queryInterface(com.sun.star.text.XTextViewCursorSupplier.class, xController);

            com.sun.star.text.XTextViewCursor curTextView = supTextViewCursor.getViewCursor();

            // gets the page cursor and assigns the text view cursor to the page
            com.sun.star.text.XPageCursor curPage =UnoRuntime.queryInterface(com.sun.star.text.XPageCursor.class, curTextView);
            if (null == curPage)
                return;

            curPage.jumpToFirstPage();
            curTextView.gotoStart(false);


        }
        catch (Exception ex)
        {

        }

    }


    public void goDocEnd()
    {

        try
        {
            if (false == isInit())
                return;

            com.sun.star.frame.XController xController = m_xDoc.getCurrentController();
            if (null == xController)
                return;

            com.sun.star.text.XTextViewCursorSupplier supTextViewCursor =UnoRuntime.queryInterface(com.sun.star.text.XTextViewCursorSupplier.class, xController);

            com.sun.star.text.XTextViewCursor curTextView = supTextViewCursor.getViewCursor();

            // gets the page cursor and assigns the text view cursor to the page
            com.sun.star.text.XPageCursor curPage =UnoRuntime.queryInterface(com.sun.star.text.XPageCursor.class, curTextView);
            if (null == curPage)
                return;

            curPage.jumpToLastPage();
            curTextView.gotoEnd(false);


        }
        catch (Exception ex)
        {

        }

    }

    public String currentPageNum()
    {
        try
        {
            if (false == isInit())
                return "";

            com.sun.star.frame.XController xController = m_xDoc.getCurrentController();
            if (null == xController)
                return "";

            com.sun.star.text.XTextViewCursorSupplier supTextViewCursor =UnoRuntime.queryInterface(com.sun.star.text.XTextViewCursorSupplier.class, xController);

            com.sun.star.text.XTextViewCursor curTextView = supTextViewCursor.getViewCursor();

            // gets the page cursor and assigns the text view cursor to the page
            com.sun.star.text.XPageCursor curPage =UnoRuntime.queryInterface(com.sun.star.text.XPageCursor.class, curTextView);
            if (null == curPage)
                return "";

            return String.valueOf(curPage.getPage());

        }
        catch (Exception ex)
        {
            return "";
        }
    }

    public String getCurrentSelTxt()
    {
        try
        {
            if (false == isInit())
                return "";

            com.sun.star.frame.XController ctrl = m_xDoc.getCurrentController();
            if (null == ctrl)
                return "";
            com.sun.star.view.XSelectionSupplier selsupplier = UnoRuntime.queryInterface(com.sun.star.view.XSelectionSupplier.class,ctrl) ;
            if (null == selsupplier)
                return "";
            Object sel = selsupplier.getSelection();


            com.sun.star.container.XIndexAccess xIndexAccess = UnoRuntime.queryInterface(com.sun.star.container.XIndexAccess.class, sel);
            if (null == xIndexAccess)
                return "";
            Object Index = xIndexAccess.getByIndex(0);
            com.sun.star.text.XTextRange xTextRange =UnoRuntime.queryInterface(com.sun.star.text.XTextRange.class, Index);
            if (null == xTextRange)
                return "";

            String sres = xTextRange.getString();

            return sres;

        }
        catch (Exception ex)
        {
            logger.error(ex);
            return "";
        }
    }

    public void insertParaBreak()
    {
        try
        {
            if (isInit() == false)
                return;

            com.sun.star.text.XTextViewCursorSupplier xViewCursorSupplier =UnoRuntime.queryInterface(com.sun.star.text.XTextViewCursorSupplier.class, m_xDoc.getCurrentController());
            com.sun.star.text.XTextViewCursor intPosition = xViewCursorSupplier.getViewCursor();
            com.sun.star.text.XTextCursor insPositionTC = m_xDoc.getText().createTextCursorByRange(intPosition);


            insPositionTC.getText().insertControlCharacter(insPositionTC, com.sun.star.text.ControlCharacter.PARAGRAPH_BREAK, false);
        }
        catch(Exception ex)
        {
            logger.error(ex);
        }
    }

    public boolean insertFile(String sfilename)
    {

        com.sun.star.text.XTextDocument srcDoc = null;
        try
        {
            if (isInit() == false)
                return false;


            String insFileName = sfilename;

            insFileName = insFileName.replace('\\', '/');
            insFileName = "file:///" + insFileName;



            //打开源文档
            com.sun.star.beans.PropertyValue[] loadProps = new com.sun.star.beans.PropertyValue[1];
            loadProps[0] = new com.sun.star.beans.PropertyValue();
            loadProps[0].Name = "Hidden";
            loadProps[0].Value = new Any(boolean.class,true);



            srcDoc =UnoRuntime.queryInterface(com.sun.star.text.XTextDocument.class, m_xCLoader.loadComponentFromURL(insFileName, "_blank", 0, loadProps));
            if (srcDoc == null)
                return false;
            srcDoc.lockControllers();


            //选取原文档中所有内容
            com.sun.star.text.XTextCursor c1 = srcDoc.getText().createTextCursor();
            c1.gotoStart(false);
            c1.gotoEnd(true);


            com.sun.star.view.XSelectionSupplier sel =UnoRuntime.queryInterface(com.sun.star.view.XSelectionSupplier.class, srcDoc.getCurrentController());
            sel.select(new Any(c1.getClass(), c1)); //选中源文档中所有内容

            //复制所有选中的内容
            com.sun.star.datatransfer.XTransferableSupplier transHelp =UnoRuntime.queryInterface(com.sun.star.datatransfer.XTransferableSupplier.class, srcDoc.getCurrentController());
            com.sun.star.datatransfer.XTransferable selectedContent = transHelp.getTransferable();



            //粘贴源文档中选中内容到当前文档光标位置
            com.sun.star.text.XTextViewCursorSupplier xViewCursorSupplier =UnoRuntime.queryInterface(com.sun.star.text.XTextViewCursorSupplier.class, m_xDoc.getCurrentController());
            com.sun.star.text.XTextViewCursor intPosition = xViewCursorSupplier.getViewCursor();
            com.sun.star.text.XTextCursor insPositionTC =  m_xDoc.getText().createTextCursorByRange(intPosition);
            if (null == insPositionTC)
                return false;

            com.sun.star.view.XSelectionSupplier sel2 =UnoRuntime.queryInterface(com.sun.star.view.XSelectionSupplier.class, m_xDoc.getCurrentController());
            sel2.select(new Any(insPositionTC.getClass(), insPositionTC)); //将新文档中的插入位置设为当前光标位置，这里的光标对象表现为一个指向位置的点，而不是一个范围

            com.sun.star.datatransfer.XTransferableSupplier transHelp2 =UnoRuntime.queryInterface(com.sun.star.datatransfer.XTransferableSupplier.class, m_xDoc.getCurrentController());
            transHelp2.insertTransferable(selectedContent);


            srcDoc.unlockControllers();


            invalidOffice();

            return true;

        }
        catch(Exception ex)
        {
            logger.error(ex);
            return false;
        }
        finally
        {
            try
            {
                if (srcDoc != null)
                {


                    com.sun.star.frame.XFrame2 fe = UnoRuntime.queryInterface(com.sun.star.frame.XFrame2.class,srcDoc.getCurrentController().getFrame());

                    String stit = fe.getTitle();
                    logger.info("Close " + stit);

                    execFrameCmd(fe, ".uno:CloseDoc",null);

                    srcDoc.dispose();

                }

            }
            catch(Exception ex2)
            {
                logger.error(ex2);
            }
        }
    }
    public void setStructCanRemove(boolean bCanRemove)
    {
        StructCanRemove=bCanRemove;
    }
    private  boolean StructCanRemove=false;

    //处理按键， 返回true 则office忽略当前按键行为，返回false则office处理当前按键行为
    public Boolean processKey(com.sun.star.awt.KeyEvent key)
    {
        try
        {
            //如果当前只读，则不响应任何按键
            if (true == isReadonly(key,null))
                return true;



            if (null != key)
            {
                int iKey = key.KeyCode;
                if (1283 == iKey)
                {
                    //向前删除操作
                    com.sun.star.text.XTextCursor tc = tcInputPosition();
                    com.sun.star.text.XWordCursor wc = UnoRuntime.queryInterface(com.sun.star.text.XWordCursor.class,tc);
                    if (null!=wc && wc.goLeft((short)1, false) == true)
                    {
                        AreaData area = detectArea(wc.getStart());
                        if (null != area)
                        {
                            //在书写态，结构化元素不允许删除
                            if (EditMode.USER==m_EditMode && false == StructCanRemove)
                            {
                                com.sun.star.text.XTextRange xr = area.Field.getAnchor().getStart();
                                com.sun.star.text.XTextViewCursor cursor = getTextViewCursor();
                                cursor.gotoRange(xr, false);
                                return true;
                            }

                        }
                    }
                }
                else if (1286 == iKey)
                {
                    //向后删除操作
                    com.sun.star.text.XTextCursor tc = tcInputPosition();
                    com.sun.star.text.XWordCursor wc = UnoRuntime.queryInterface(com.sun.star.text.XWordCursor.class,tc);
                    if (null!=wc && wc.goRight((short)1, false) == true)
                    {
                        AreaData area= detectArea(wc.getStart());
                        if (null != area)
                        {
                            //在书写态，结构化元素不允许删除
                            if (EditMode.USER == m_EditMode && false == StructCanRemove)
                            {
                                com.sun.star.text.XTextRange xr = area.Field.getAnchor().getEnd();
                                com.sun.star.text.XTextViewCursor cursor = getTextViewCursor();
                                cursor.gotoRange(xr, false);
                                return true;
                            }
                        }
                    }
                }

            }

            return false;

        }
        catch (Exception ex)
        {
            logger.error(ex);
            return false;
        }

    }

    public void setEditMode(EditMode mode)
    {
        m_EditMode=mode;
    }
    private  EditMode m_EditMode = EditMode.USER;

    private Shell getShell()
    {
        if(SystemUtils.IS_OS_WINDOWS) {
           return ((Composite)m_panelParent).getShell();
        }
        else if(SystemUtils.IS_OS_LINUX) {
            return ((Label)m_panelParent).getShell();
        }

        return null;

    }

    //根据groupID和 areaID 查找结构化区域
    private AreaData findArea(String groupID,String areaID)
    {
        try
        {
            if (isInit() == false || groupID.trim().length() <= 0 || areaID.trim().length() <= 0)
                return null;

            com.sun.star.text.XTextFieldsSupplier xTextFieldsSupplier = UnoRuntime.queryInterface(com.sun.star.text.XTextFieldsSupplier.class, m_xComponent);

            com.sun.star.container.XEnumerationAccess xParaAccess = xTextFieldsSupplier.getTextFields();

            com.sun.star.container.XEnumeration xParaEnum = xParaAccess.createEnumeration();

            while (xParaEnum.hasMoreElements())
            {

                try
                {

                    Object aTextField = xParaEnum.nextElement();
                    if (null == aTextField)
                        continue;

                    com.sun.star.lang.XServiceInfo info =UnoRuntime.queryInterface(com.sun.star.lang.XServiceInfo.class, aTextField);
                    String[] srvNames = info.getSupportedServiceNames();
                    String simpName = info.getImplementationName();
                    if (!simpName.equals("SwXTextField") || !info.supportsService("com.sun.star.text.textfield.Input"))
                        continue;

                    com.sun.star.text.XTextField xTextField =UnoRuntime.queryInterface(com.sun.star.text.XTextField.class, aTextField);
                    if (null == xTextField)
                        continue;


                    com.sun.star.beans.XPropertySet xPropertySet =UnoRuntime.queryInterface(com.sun.star.beans.XPropertySet.class, xTextField);
                    if (null == xPropertySet)
                        continue;


                    String sHint = String.valueOf(xPropertySet.getPropertyValue("Hint"));
                    String sContent =String.valueOf(xPropertySet.getPropertyValue("Content"));
                    String sHelp = String.valueOf(xPropertySet.getPropertyValue("Help"));

                    String sStructInfo = "Hint: " + sHint + " Content: " + sContent + " Help: " + sHelp;
                    logger.info(sStructInfo);

                    if (null == sHelp || sHelp.trim().length() <= 0)
                        continue;

                    AreaData area = new AreaData();
                    if (area.parseBase64(sHelp) == false)
                        continue;

                    area.Prop = xPropertySet;
                    area.Value = sContent;

                    if (groupID.equals(area.GroupID) && areaID.equals(area.ID))
                        return area;

                }
                catch (Exception e)
                {
                   logger.error(e);
                   continue;
                }

            }


            return null;

        }
        catch (Exception ex)
        {
            logger.error(ex);
            return null;
        }
    }

    public boolean setAreaValue(String groupID,String areaID,String sVal,String sValCode)
    {
        try
        {
            if (groupID.trim().length() <= 0 || areaID.trim().length() <= 0 || sVal.length() <= 0)
                return false;


            AreaData area = findArea(groupID, areaID);
            if (null == area)
                return false;

            if (null == area.Prop)
                return false;

            area.ValCode = sValCode;

            String sParam=area.saveBase64();

            area.Prop.setPropertyValue("Help", new Any(String.class,sParam));
            area.Prop.setPropertyValue("Content",new Any(String.class,sVal));

            return true;

        }
        catch(Exception ex)
        {
            logger.error(ex);
            return false;
        }
    }
    public String[] getAreaValue(String groupID, String areaID)
    {
        try
        {
            if (groupID.trim().length() <= 0 || areaID.trim().length() <= 0)
                return null;


            AreaData area = findArea(groupID, areaID);
            if (null == area)
                return null;

            if (null == area.Prop)
                return null;


            String[] ar = new String[2];
            ar[0] = area.ValCode;
            ar[1] = area.Value;

            return ar;

        }
        catch (Exception ex)
        {
            logger.error(ex);
            return null;
        }
    }

    private String getEmbedText(String sTxtFileName)
    {
        String result = "";
        try
        {


            URL url = getClass().getResource(sTxtFileName);
            InputStream inStream= url.openStream();
            if(null==inStream)
                return result;

            byte[] buff=inStream.readAllBytes();
            result=new String(buff);

            inStream.close();

        }
        catch (Exception ex)
        {
            logger.error(ex);
        }
        return result;
    }

    private String getOfficePathNoBacklash(String varname)
    {
        try
        {
            XMultiServiceFactory xFactory= getXMultiServiceFactory();
            if (null== xFactory)
                return "";

            Object ops = xFactory.createInstance("com.sun.star.util.PathSubstitution");
            if (null == ops)
                return "";

            com.sun.star.util.XStringSubstitution xss =UnoRuntime.queryInterface(com.sun.star.util.XStringSubstitution.class,
                     ops);
            if (null == xss)
                return "";

            return xss.getSubstituteVariableValue(varname);


        }
        catch (Exception ex)
        {
            logger.error(ex);
        }

        return "";

    }

    private String getOfficeUserPathNoBacklash()
    {
        return getOfficePathNoBacklash("user");

    }
    private String getMacroModule1FilePath()
    {
        return getOfficeUserPathNoBacklash() + "/basic/Standard/Module1.xba";
    }

    private boolean instScript(String ScriptFileName)
    {
        try
        {
            String sc = getEmbedText(ScriptFileName);
            if (null == sc || sc.trim().length() <= 0)
                return false;

            String smf = getMacroModule1FilePath();
            if(SystemUtils.IS_OS_WINDOWS) {
                smf = smf.replace("file:///", "");
            }
            else if(SystemUtils.IS_OS_LINUX)
            {
                smf = smf.replace("file:///", "/");

            }

            File file = new File(smf);
            if(file.exists())
            {
                file.delete();
            }

            file.createNewFile();

            FileWriter fw=new FileWriter(file,true);
            fw.write(sc);
            fw.close();


            return true;

        }
        catch (Exception ex)
        {
            logger.error(ex);
        }
        return false;
    }


    private ResultExecScript  _execScriptStarBasic(String sScript, Any[] aParam)
    {
        try
        {
            //test sScript "vnd.sun.star.script:Standard.Module1.test?language=Basic&location=application"

            if (false == isInit() || null==m_xFactory)
                return null;

            Object oMSPFac= m_xFactory.createInstance("com.sun.star.script.provider.MasterScriptProviderFactory");
            if (null == oMSPFac)
                return null;

            com.sun.star.script.provider.XScriptProviderFactory xScriptProviderFactory =UnoRuntime.queryInterface(com.sun.star.script.provider.XScriptProviderFactory.class,
                    oMSPFac);
            if (null == xScriptProviderFactory)
                return null;

            Any aCtx = new Any(String.class,"com.sun.star.script.provider.ScriptProviderForBasic");

            Object oMSP = xScriptProviderFactory.createScriptProvider(aCtx);
            if (null == oMSP)
                return null;

            com.sun.star.script.provider.XScriptProvider xScriptProvider =UnoRuntime.queryInterface(com.sun.star.script.provider.XScriptProvider.class,
                    oMSP);
            if (null == xScriptProvider)
                return null;

            com.sun.star.script.provider.XScript xScript = xScriptProvider.getScript(sScript);


           ResultExecScript res=new ResultExecScript();

           xScript.invoke(aParam,res.aOutParamIndex, res.outParam);

            res.bRet=true;

            return res;



        }
        catch (Exception ex)
        {
            logger.error(ex);
            return null;
        }

}

    public void insertTable()
    {
        try
        {
            if (false == isInit())
                return;

            com.sun.star.frame.XController ctrl = m_xDoc.getCurrentController();
            if (null == ctrl)
                return;

            com.sun.star.frame.XFrame fe = ctrl.getFrame();
            if (null == fe)
                return;

            com.sun.star.beans.PropertyValue[] param = new com.sun.star.beans.PropertyValue[1];
            param[0] = new com.sun.star.beans.PropertyValue();
            param[0].Name = "TrackChanges";
            param[0].Value = new Any(Boolean.class,false);
            execFrameCmd(fe, ".uno:InsertTable", param);

        }
        catch (Exception ex)
        {
            logger.error(ex);
        }

    }



    private ResultExecScript execScriptStarBasic(String FuncName, Any[] aParam)
    {
        try
        {
            return _execScriptStarBasic("vnd.sun.star.script:Standard.Module1."+FuncName+"?language=Basic&location=application", aParam);

        }
        catch (Exception ex)
        {
            logger.error(ex);
        }
        return null;
    }

    /// <summary>
    /// 插入ole对象
    /// </summary>
    /// <param name="sclsid">ole class id 字符串</param>
    /// <param name="iw">ole 对象宽度</param>
    /// <param name="ih">ole 对象高度</param>
    public void insertOle(String sclsid,int iw,int ih)
    {
        try
        {
            if(!isInit())
                return;

            if(null==sclsid || sclsid.trim().length()<=0)
            {

                FrmInsertOle dlg = new FrmInsertOle(getShell());
                dlg.centerInParent(true);
                dlg.open();

                String message = dlg.getMessage();
                if(!"set".equals(message))
                    return;

                sclsid = dlg.OleCLSID;
                iw = dlg.OleWidth;
                ih = dlg.OleHeight;

                if (null == sclsid || sclsid.trim().length() <= 0)
                    return;

            }

            Any[] ip = new Any[3];
            ip[0] = new Any(String.class,sclsid);
            ip[1] = new Any(Integer.class,iw);
            ip[2] = new Any(Integer.class,ih);



            //execScriptStarBasic("lp_hello",new Any[]{new Any(String.class,"Jack")});

            execScriptStarBasic("lp_insertOle", ip);


        }
        catch(Exception ex)
        {
            logger.error(ex);
        }
    }

    public void stopRevise()
    {
        try
        {
            if (false == isInit())
                return;

            com.sun.star.frame.XController ctrl = m_xDoc.getCurrentController();
            if (null == ctrl)
                return;

            com.sun.star.frame.XFrame fe = ctrl.getFrame();
            if (null == fe)
                return;

            com.sun.star.beans.PropertyValue[] param = new com.sun.star.beans.PropertyValue[1];
            param[0] = new com.sun.star.beans.PropertyValue();
            param[0].Name = "TrackChanges";
            param[0].Value = new Any(boolean.class,false);
            execFrameCmd(fe, ".uno:TrackChanges", param);

        }
        catch (Exception ex)
        {
            logger.error(ex);
        }
    }

    public void startRevise(String authorName)
    {
        try
        {
            if(isInit()==false)
                return;

            String sAuthorName;
            if(null==authorName || authorName.trim().length()<=0)
            {
                FrmStartRevise dlg = new FrmStartRevise(getShell());
                dlg.centerInParent(true);
                dlg.open();

                String message = dlg.getMessage();
                if(message==null || !message.equals("set"))
                    return;

                sAuthorName =dlg.AuthorName.trim();
            }
            else
                sAuthorName = authorName.trim();

            if (sAuthorName.length() <= 0)
                return;


            com.sun.star.frame.XController ctrl = m_xDoc.getCurrentController();
            if (null == ctrl)
                return;

            com.sun.star.frame.XFrame fe=ctrl.getFrame();
            if (null == fe)
                return;


            if(setOfficeConfig("givenname",sAuthorName, "/org.openoffice.UserProfile/Data") ==false)
            {
                //设置当前修订人员姓名失败，则不进入修订模式
                return;
            }
            String sAuthorName2 = getOfficeConfig("givenname");
            if (!sAuthorName2.equals(sAuthorName))
                return;


            com.sun.star.beans.PropertyValue[] param = new com.sun.star.beans.PropertyValue[1];
            param[0]= new com.sun.star.beans.PropertyValue();
            param[0].Name = "TrackChanges";
            param[0].Value = new Any(boolean.class,true);
            execFrameCmd(fe, ".uno:TrackChanges",param);

        }
        catch (Exception ex)
        {
            logger.error(ex);
        }
    }

    public String getXml()
    {
        String sXml = "";
        try
        {
            if(isInit()==false)
                return sXml;

            com.sun.star.text.XTextFieldsSupplier xTextFieldsSupplier = UnoRuntime.queryInterface(com.sun.star.text.XTextFieldsSupplier.class, m_xComponent);

            com.sun.star.container.XEnumerationAccess xParaAccess = xTextFieldsSupplier.getTextFields();


            com.sun.star.container.XEnumeration xParaEnum = xParaAccess.createEnumeration();


            while (xParaEnum.hasMoreElements())
            {

                try
                {

                    Object aTextField = xParaEnum.nextElement();
                    if (null == aTextField)
                        continue;

                    com.sun.star.lang.XServiceInfo info =UnoRuntime.queryInterface(com.sun.star.lang.XServiceInfo.class,
                           aTextField);
                    String[] srvNames = info.getSupportedServiceNames();
                    String simpName = info.getImplementationName();
                    if (!"SwXTextField".equals(simpName) || !info.supportsService("com.sun.star.text.textfield.Input"))
                        continue;

                    com.sun.star.beans.XPropertySet xPropertySet =UnoRuntime.queryInterface(com.sun.star.beans.XPropertySet.class,
                            aTextField);

                    String sHint =String.valueOf(xPropertySet.getPropertyValue("Hint"));
                    String sContent = String.valueOf(xPropertySet.getPropertyValue("Content"));
                    String sHelp = String.valueOf(xPropertySet.getPropertyValue("Help"));

                    AreaData arTmp=new AreaData();
                    if(arTmp.parseBase64(sHelp))
                    {
                        sXml = sXml+"<Elem GroupID=\""+arTmp.GroupID+"\" ID=\""+arTmp.ID+"\""
                        +" ValCode=\""+arTmp.ValCode+"\" Val=\""+sContent+"\""+"/>";

                    }



                }
                catch(Exception ex)
                {
                    logger.error(ex);
                    continue;
                }

            }




        }
        catch (Exception ex1)
        {
            logger.error(ex1);
            return null;
        }
        if(sXml.trim().length()>0)
        {
            sXml="<Data>"+sXml+"</Data>";
        }

        return sXml;
    }

    public void aboutLibreoffice()
    {
        execCmd(".uno:About",null);

    }
    public void actField()
    {

        try
        {

            if (false == isInit())
                return ;



            //detectArea(null);  //在前置的判断只读的Readonly方法中已经检测过结构化区域了，不重复检测
            if (null == m_currentElement)
                return;

            if (EditMode.USER!= m_EditMode) //如果当前不为用户输入模式，则不弹出结构化辅助输入
            {
                return;
            }

            Display display=null;
            if(SystemUtils.IS_OS_WINDOWS)
                display=  ((Composite)m_panelParent).getDisplay();
            else if(SystemUtils.IS_OS_LINUX)
                display=  ((Label)m_panelParent).getDisplay();


            display.asyncExec(() -> {

                try {

                    Boolean bSet = false;
                    AreaData areaModify = null;

                    if (m_currentElement.ActionType.equals("单选")) {

                        FrmSingleSel fss = new FrmSingleSel(getShell());
                        fss.m_Area = m_currentElement;
                        java.awt.Point ptCurrent= MouseInfo.getPointerInfo().getLocation();
                        fss.setLocation(new Point(ptCurrent.x,ptCurrent.y));
                        fss.open();
                        String message = fss.getMessage();
                        if (message == null) {
                            return;
                        }
                        if(message.equals("set"))
                        {
                            areaModify = fss.m_Area;
                            bSet = true;

                        }

                    }
                    else if(m_currentElement.ActionType.equals("多选"))
                    {
                        FrmMultiSel fms = new FrmMultiSel(getShell());
                        fms.m_Area = m_currentElement;
                        java.awt.Point ptCurrent= MouseInfo.getPointerInfo().getLocation();
                        fms.setLocation(new Point(ptCurrent.x,ptCurrent.y));
                        fms.open();
                        String message = fms.getMessage();
                        if (message == null) {
                            return;
                        }
                        if(message.equals("set"))
                        {
                            areaModify = fms.m_Area;
                            bSet = true;

                        }

                    }
                    else if(m_currentElement.ActionType.equals("时间"))
                    {
                        FrmDateTime fdt = new FrmDateTime(getShell());
                        fdt.m_Area = m_currentElement;
                        java.awt.Point ptCurrent= MouseInfo.getPointerInfo().getLocation();
                        fdt.setLocation(new Point(ptCurrent.x,ptCurrent.y));
                        fdt.open();
                        String message = fdt.getMessage();
                        if (message == null) {
                            return;
                        }
                        if(message.equals("set"))
                        {
                            areaModify = fdt.m_Area;
                            bSet = true;

                        }
                    }
                    else
                    {
                        return;
                    }

                    if (true == bSet) {
                        String sHelp = areaModify.saveBase64();
                        String sContent = areaModify.Value;


                        if (areaModify.Prop != null) {
                            areaModify.Prop.setPropertyValue("Content", new Any(sContent.getClass(), sContent));
                            areaModify.Prop.setPropertyValue("Help", new Any(sHelp.getClass(), sHelp));
                        }

                    }

                }
                catch (Exception ex1)
                {
                    logger.error(ex1);

                }

            });


        }
        catch (Exception ex)
        {
            logger.error(ex);
        }



    }


    private com.sun.star.text.XTextViewCursor getTextViewCursor()
    {
        com.sun.star.frame.XModel xModel =UnoRuntime.queryInterface(com.sun.star.frame.XModel.class, m_xComponent);
        com.sun.star.frame.XController xController = xModel.getCurrentController();
        // the controller gives us the TextViewCursor
        com.sun.star.text.XTextViewCursorSupplier xViewCursorSupplier =UnoRuntime.queryInterface(com.sun.star.text.XTextViewCursorSupplier.class, xController);
        com.sun.star.text.XTextViewCursor xViewCursor = xViewCursorSupplier.getViewCursor();

        return xViewCursor;
    }
    private void showXServiceInfo(Object obj)
    {
        if (null == obj)
            return;

        com.sun.star.lang.XServiceInfo xinfo =UnoRuntime.queryInterface(com.sun.star.lang.XServiceInfo.class,obj);
        if (null == xinfo)
            return;

        String[] sernames= xinfo.getSupportedServiceNames();
        String impname = xinfo.getImplementationName();

        logger.info("IMP NAME: " + impname);
        for(String s : sernames)
        {
            logger.info("SRV NAME: "+s);
        }
    }

    private com.sun.star.text.XTextCursor tcInputPosition()
    {
        try
        {
            if (false == isInit())
                return null;


            com.sun.star.text.XTextViewCursor vViewCursor = getTextViewCursor();
            com.sun.star.text.XTextRange tr = vViewCursor.getStart();


            com.sun.star.text.XTextCursor oCurs = null;

            try
            {
                oCurs=m_xDoc.getText().createTextCursorByRange(tr);
                if (null != oCurs)
                    return oCurs;
            }
            catch(Exception ex1)
            {
                oCurs = null;
                logger.error(ex1); //如果根据 viewCursor 查找输入点异常，则尝试从表格中查找输入点
            }


            com.sun.star.text.XTextTablesSupplier xTextFieldsSupplier =UnoRuntime.queryInterface(com.sun.star.text.XTextTablesSupplier.class,m_xComponent);
            com.sun.star.container.XNameAccess xParaAccess = xTextFieldsSupplier.getTextTables();
            String[] tabnames = xParaAccess.getElementNames();
            for (String s : tabnames)
            {
                Object aTextField=null;
                try {
                    aTextField = xParaAccess.getByName(s);
                }
                catch (Exception ex3)
                {
                    logger.error(ex3);
                    continue;
                }
                showXServiceInfo(aTextField);

                com.sun.star.text.XTextTable tb = UnoRuntime.queryInterface(com.sun.star.text.XTextTable.class,aTextField);
                String[] cells = tb.getCellNames();
                for (String scell : cells)
                {

                    com.sun.star.table.XCell ce = tb.getCellByName(scell);
                    com.sun.star.text.XText xCellText = UnoRuntime.queryInterface(com.sun.star.text.XText.class,ce);

                    try
                    {
                        oCurs = xCellText.createTextCursorByRange(tr);
                        if (oCurs != null)
                        {
                            break;
                        }
                    }
                    catch (Exception ex2)
                    {

                        oCurs = null;
                        logger.error(ex2); //从表格中查找输入点异常
                    }

                }
            }

            return oCurs;
        }
        catch(Exception ex)
        {
            logger.error(ex);
            return null;
        }
    }

    //根据位置查找field
    private Object findField(com.sun.star.text.XTextRange range)
    {

        try
        {

            com.sun.star.text.XTextFieldsSupplier xTextFieldsSupplier =UnoRuntime.queryInterface(com.sun.star.text.XTextFieldsSupplier.class,
                    m_xComponent);
            com.sun.star.container.XEnumerationAccess xParaAccess = xTextFieldsSupplier.getTextFields();
            com.sun.star.container.XEnumeration xParaEnum = xParaAccess.createEnumeration();

            while (xParaEnum.hasMoreElements())
            {

                try
                {
                    Object aTextField=null;

                    try {
                        aTextField = xParaEnum.nextElement();
                    }
                    catch (Exception ex1)
                    {
                        logger.error(ex1);
                        continue;
                    }

                    if (null == aTextField)
                        continue;

                    com.sun.star.lang.XServiceInfo info =UnoRuntime.queryInterface(com.sun.star.lang.XServiceInfo.class,
                            aTextField);
                    String[] srvNames = info.getSupportedServiceNames();
                    String simpName = info.getImplementationName();
                    if (false == simpName.equals("SwXTextField") || info.supportsService("com.sun.star.text.textfield.Input") == false)
                        continue;

                    com.sun.star.text.XTextField xTextField =UnoRuntime.queryInterface(com.sun.star.text.XTextField.class,
                            aTextField);
                    com.sun.star.text.XTextRangeCompare xTextRangeCompare =UnoRuntime.queryInterface(com.sun.star.text.XTextRangeCompare.class,
                            range.getText());


                    com.sun.star.text.XTextRange tr = xTextField.getAnchor();
                    com.sun.star.text.XTextRange trStart=tr.getStart();
                    com.sun.star.text.XTextRange trEnd = tr.getEnd();


                    com.sun.star.text.XTextRange trOldStart = range.getStart();
                    com.sun.star.text.XTextRange trOldEnd = range.getEnd();

                    int comparison = xTextRangeCompare.compareRegionStarts(trOldStart, trStart);
                    int comparison2 = xTextRangeCompare.compareRegionEnds(trOldEnd, trEnd);


                    if (-1 == comparison && 1== comparison2)
                        return xTextField;



                }
                catch (Exception e)
                {
                    logger.error(e);
                    continue;
                }

            }


            return null;

        }
        catch (Exception ex)
        {
            logger.error(ex);
            return null;
        }



    }

    private AreaData m_currentElement = null;
    public AreaData detectArea(com.sun.star.text.XTextRange range)
    {
        try
        {
            m_currentElement = null;

            if (false == isInit())
                return null;

            //unoidl.com.sun.star.frame.XController ctrl = m_xDocument.getCurrentController();
            //if (null == ctrl)
            //    return null;
            //unoidl.com.sun.star.view.XSelectionSupplier selsupplier = ctrl as unoidl.com.sun.star.view.XSelectionSupplier;
            //if (null == selsupplier)
            //    return null;
            //uno.Any sel = selsupplier.getSelection();


            //unoidl.com.sun.star.container.XIndexAccess xIndexAccess = sel.Value as unoidl.com.sun.star.container.XIndexAccess;
            //if (null == xIndexAccess)
            //    return null;

            //if (xIndexAccess.getCount() <= 0)
            //    return null;

            Object field = null;

            //uno.Any Index = xIndexAccess.getByIndex(0);
            com.sun.star.text.XTextRange xTextRange = tcInputPosition();// Index.Value as unoidl.com.sun.star.text.XTextRange;
            if (null == xTextRange)
                return null;


            if(null== range)
            {
                field = findField(xTextRange);


            }
            else
            {
                field = findField(range);
            }

            if (null == field)
                return null;

            com.sun.star.beans.XPropertySet xPropertySet =UnoRuntime.queryInterface(com.sun.star.beans.XPropertySet.class,field);
            if (null == xPropertySet)
                return null;



            String sHint = String.valueOf(xPropertySet.getPropertyValue("Hint"));
            String sContent = String.valueOf(xPropertySet.getPropertyValue("Content"));
            String sHelp =String.valueOf(xPropertySet.getPropertyValue("Help"));



            //string sStructInfo = "Hint: " + sHint + " Content: " + sContent + " Help: " + sHelp;


            if (null == sHelp || sHelp.trim().length() <= 0)
                return null;

            AreaData area = new AreaData();
            if (area.parseBase64(sHelp) == false)
                return null;

            area.Prop = xPropertySet;
            area.Value = sContent;

            area.Field =UnoRuntime.queryInterface(com.sun.star.text.XTextField.class,field);

            m_currentElement = area;

            String sStructInfo = "ele:"+area.ActionType+" groupid:"+area.GroupID+" id:"+area.ID+" edit:"+area.Editable+ " valcode:"+area.ValCode;
            logger.info(sStructInfo);

            return area;

        }
        catch(Exception ex)
        {
            logger.error(ex);
            return null;
        }
    }


    private  void insertIntoCell(String CellName, String theText,
                                       com.sun.star.text.XTextTable xTTbl) {

        com.sun.star.text.XText xTableText = UnoRuntime.queryInterface(com.sun.star.text.XText.class,
                xTTbl.getCellByName(CellName));

        //create a cursor object
        com.sun.star.text.XTextCursor xTC = xTableText.createTextCursor();

        com.sun.star.beans.XPropertySet xTPS = UnoRuntime.queryInterface(com.sun.star.beans.XPropertySet.class, xTC);

        try {
            xTPS.setPropertyValue("CharColor",Integer.valueOf(16777215));
        } catch (Exception e) {
            System.err.println(" Exception " + e);
            e.printStackTrace(System.err);
        }

        //inserting some Text
        xTableText.setString( theText );

    }

    private XMultiComponentFactory getXMultiComponentFactory() throws Exception {

        if(null!=m_xContext)
            return m_xContext.getServiceManager();

        return null;

    }

    private XMultiServiceFactory getXMultiServiceFactory() throws Exception {
        return (XMultiServiceFactory) UnoRuntime.queryInterface(
                XMultiServiceFactory.class, getXMultiComponentFactory());

    }
    // the lparent is swt GtkWidget*
    private long m_lParent =0;
    //the lchild is XLib Window obj
    private long m_lChild =0;

    public boolean isInit()
    {
        try
        {
            if (null == m_xFrame || null == m_xComponent)
                return false;

            return true;

        }
        catch (Exception ex)
        {
            return false;
        }
    }

    private Widget m_panelParent=null;

    private String m_oooExeFolder ="";
    private  void detectLibreofficeExeFolder()
    {
        if(SystemUtils.IS_OS_WINDOWS)
            m_oooExeFolder = "C:\\Program Files\\LibreOffice\\program";
        else if(SystemUtils.IS_OS_LINUX)
        {
            m_oooExeFolder = "/usr/lib/libreoffice/program";
            File dir=new File(m_oooExeFolder);
            if(!dir.exists())
                m_oooExeFolder = "/usr/lib64/libreoffice/program";
        }
        else if(SystemUtils.IS_OS_MAC)
            m_oooExeFolder = "/Users/username/Applications/LibreOffice.app/Contents/MacOS";


       // System.out.println(m_oooExeFolder);

    }

    public boolean init(Widget panelParent,String sfilename)
    {
        m_panelParent=panelParent;

        panelParent.addListener(SWT.Resize, new Listener() {
            public void handleEvent(Event e) {

                System.out.println( "panelParent resized.");

                if(SystemUtils.IS_OS_LINUX)
                {
                    if(isInit()==false)
                        return;

                    org.eclipse.swt.graphics.Rectangle rcPanel=((Label)panelParent).getBounds();
                    System.out.println( "panelParent, width: "+String.valueOf(rcPanel.width)
                            + " height: "+String.valueOf(rcPanel.height));
                    //在linux下调整嵌入libreoffice窗口的大小
                    Rectangle rcframe= m_xFrame.getContainerWindow().getPosSize();
                    System.out.println( "swrite width: "+String.valueOf(rcframe.Width)
                            + " height: "+String.valueOf(rcframe.Height));
                    m_xFrame.getContainerWindow().setPosSize(0,0,rcPanel.width,rcPanel.height,PosSize.POSSIZE);

                }

            }
        });


        try {

            detectLibreofficeExeFolder();

            if(SystemUtils.IS_OS_WINDOWS)
                m_xContext = BootstrapSocketConnector.bootstrap(m_oooExeFolder);
            else if(SystemUtils.IS_OS_LINUX)
                m_xContext = BootstrapSocketConnector.bootstrap(m_oooExeFolder);


            m_xFactory=getXMultiServiceFactory();

            if(null==m_xContext || null==m_xFactory)
            {
                logger.error("启动libreoffice失败!");
                return false;
            }

            instScript("inst_script.txt"); //安装宏脚本

            //优化swriter启动速度
            setOfficeConfig("UseSkia", false, "/org.openoffice.Office.Common/VCL");
            setOfficeConfig("ForceSkiaRaster",false, "/org.openoffice.Office.Common/VCL");
            setOfficeConfig("ForceSkia", false, "/org.openoffice.Office.Common/VCL");
            setOfficeConfig("AntiAliasing", false, "/org.openoffice.Office.Common/Drawinglayer");

            //Open Writer document
            openWriter(m_xContext,sfilename,panelParent);
            if(null== m_xDoc)
                return false;

            return true;

        }
        catch( Exception e) {
            e.printStackTrace(System.err);
            System.exit(1);
            return  false;
        }



    }

    private void insertField(String sContent,String sHint,String sHelp)
    {
        try {
            if (isInit() == false)
                return;


            com.sun.star.lang.XMultiServiceFactory fact = UnoRuntime.queryInterface(com.sun.star.lang.XMultiServiceFactory.class, m_xDoc);

            com.sun.star.text.XTextField xTextField = UnoRuntime.queryInterface(com.sun.star.text.XTextField.class, fact.createInstance("com.sun.star.text.textfield.Input"));

            com.sun.star.beans.XPropertySet xMasterPropSet =UnoRuntime.queryInterface(com.sun.star.beans.XPropertySet.class, xTextField);


            xMasterPropSet.setPropertyValue("Content", new Any(String.class,sContent));
            xMasterPropSet.setPropertyValue("Hint", new Any(String.class,sHint));
            xMasterPropSet.setPropertyValue("Help", new Any(String.class,sHelp));


            com.sun.star.text.XTextCursor oCurs = tcInputPosition();
            oCurs.getText().insertTextContent(oCurs, xTextField, false);

        }
        catch (Exception ex)
        {
            logger.error(ex);
        }


    }

    public void modifyElement()
    {
        try
        {

            if (false == isInit())
                return;

            if (null == m_currentElement)
                return;

            AreaData area=m_currentElement;
            String message="";

            if (area.ActionType.equals("单选"))
            {
                FrmSingleSelModify dlg = new FrmSingleSelModify(getShell());
                dlg.centerInParent(true);
                dlg.m_Area=area;
                dlg.open();

                message = dlg.getMessage();
                area=dlg.m_Area;

            }
            else if (area.ActionType.equals("多选"))
            {
                FrmMultiSelModify dlg = new FrmMultiSelModify(getShell());
                dlg.centerInParent(true);
                dlg.m_Area=area;
                dlg.open();

                message = dlg.getMessage();
                area=dlg.m_Area;

            }
            else if (area.ActionType.equals("时间"))
            {
                FrmDateTimeModify dlg = new FrmDateTimeModify(getShell());
                dlg.centerInParent(true);
                dlg.m_Area=area;
                dlg.open();

                message = dlg.getMessage();
                area=dlg.m_Area;
            }
            else if(area.ActionType.equals("自由文本"))
            {
                FrmSimpleTextModify dlg = new FrmSimpleTextModify(getShell());
                dlg.centerInParent(true);
                dlg.m_Area=area;
                dlg.open();

                message = dlg.getMessage();
                area=dlg.m_Area;
            }


            if (message.equals("set"))
            {
                String sHelp = area.saveBase64();
                String sContent = area.Value;


                if (area.Prop != null)
                {
                    area.Prop.setPropertyValue("Content", new Any(String.class,sContent));
                    area.Prop.setPropertyValue("Help", new Any(String.class,sHelp));
                }

            }

            }
        catch(Exception ex)
        {
           logger.error(ex);
        }

    }
    public  void insertElementDateTime()
    {
        try
        {
            if(isInit()==false)
                return;

            FrmDateTimeModify fdtm = new FrmDateTimeModify(getShell());
            fdtm.centerInParent(true);
            fdtm.open();


            String message = fdtm.getMessage();
            if (message.equals("set")) {

                String sAreaData = fdtm.m_Area.saveBase64();
                insertField(fdtm.m_Area.Value, "", sAreaData);


            }


        }
        catch (Exception ex)
        {
            logger.error(ex);
        }
    }

    public void insertElementMultiSel()
    {
        try
        {
            if(isInit()==false)
                return;

            FrmMultiSelModify fmsm = new FrmMultiSelModify(getShell());
            fmsm.centerInParent(true);
            fmsm.open();


            String message = fmsm.getMessage();
            if (message.equals("set")) {

                String sAreaData = fmsm.m_Area.saveBase64();
                insertField(fmsm.m_Area.Value, "", sAreaData);


            }


        }
        catch (Exception ex)
        {
            logger.error(ex);
        }
    }
    public void insertElementSingleSel()
    {
        try
        {
            if(isInit()==false)
                return;

            FrmSingleSelModify fssm = new FrmSingleSelModify(getShell());
            fssm.centerInParent(true);
            fssm.open();


            String message = fssm.getMessage();
            if (message.equals("set")) {

                String sAreaData = fssm.m_Area.saveBase64();
                insertField(fssm.m_Area.Value, "", sAreaData);


            }


        }
        catch (Exception ex)
        {
            logger.error(ex);
        }
    }
    public void insertElementText()
    {
        try {
            if (isInit() == false)
                return;


            FrmSimpleTextModify fstm = new FrmSimpleTextModify(getShell());
            fstm.centerInParent(true);
            fstm.open();


            String message = fstm.getMessage();
            if (message.equals("set")) {

                String sAreaData = fstm.m_Area.saveBase64();
                insertField(fstm.m_Area.Value, "", sAreaData);


            }
        }
        catch (Exception ex)
        {
            logger.error(ex);

        }



    }


    private void removeLibreofficeBorder(boolean removeBorder) {
        if(SystemUtils.IS_OS_WINDOWS)
        {
            //在Windows下需移除标题栏(包括最大化最小化按钮)
            int currentstyle=NativeToolsWin.GetWindowLong(m_lChild,NativeToolsWin.GWL_STYLE);

            int[] styles = new int[] { NativeToolsWin.WS_CAPTION,NativeToolsWin.WS_THICKFRAME,NativeToolsWin.WS_MINIMIZE,NativeToolsWin.WS_MAXIMIZE, NativeToolsWin.WS_SYSMENU };

            for (int style:styles) {

                if ((currentstyle & style) != 0)
                {

                    if (removeBorder)
                    {

                        currentstyle &= ~style;
                    }
                    else
                    {

                        currentstyle |= style;
                    }
                }


            }

            NativeToolsWin.SetWindowLong(m_lChild, NativeToolsWin.GWL_STYLE, currentstyle);


        }
    }


    public  void testData()
    {
        if(null== m_xDoc)
            return;
        //oooooooooooooooooooooooooooStep 3oooooooooooooooooooooooooooooooooooooooo
        // insert some text.
        // For this purpose get the Text-Object of the document and create the
        // cursor. Now it is possible to insert a text at the cursor-position
        // via insertString



        //getting the text object
        com.sun.star.text.XText xText = m_xDoc.getText();

        //create a cursor object
        com.sun.star.text.XTextCursor xTCursor = xText.createTextCursor();

        //inserting some Text
        xText.insertString( xTCursor, "第一行文字是JACK测试插入的。 The first line in the newly created text document.\n", false );

        //inserting a second line
        xText.insertString( xTCursor, "Now we're in the second line\n", false );




        //oooooooooooooooooooooooooooStep 4oooooooooooooooooooooooooooooooooooooooo
        // insert a text table.
        // For this purpose get MultiServiceFactory of the document, create an
        // instance of com.sun.star.text.TextTable and initialize it. Now it can
        // be inserted at the cursor position via insertTextContent.
        // After that some properties are changed and some data is inserted.


        //inserting a text table
        System.out.println("Inserting a text table");

        //getting MSF of the document
        com.sun.star.lang.XMultiServiceFactory xDocMSF =
                UnoRuntime.queryInterface(
                        com.sun.star.lang.XMultiServiceFactory.class, m_xDoc);

        //create instance of a text table
        com.sun.star.text.XTextTable xTT = null;

        try {
            Object oInt = xDocMSF.createInstance("com.sun.star.text.TextTable");
            xTT = UnoRuntime.queryInterface(com.sun.star.text.XTextTable.class,oInt);
        } catch (Exception e) {
            System.err.println("Couldn't create instance "+ e);
            e.printStackTrace(System.err);
        }

        //initialize the text table with 4 columns an 4 rows
        xTT.initialize(4,4);

        com.sun.star.beans.XPropertySet xTTRowPS = null;

        //insert the table
        try {
            xText.insertTextContent(xTCursor, xTT, false);
            // get first Row
            com.sun.star.container.XIndexAccess xTTRows = xTT.getRows();
            xTTRowPS = UnoRuntime.queryInterface(
                    com.sun.star.beans.XPropertySet.class, xTTRows.getByIndex(0));

        } catch (Exception e) {
            System.err.println("Couldn't insert the table " + e);
            e.printStackTrace(System.err);
        }


        // get the property set of the text table

        com.sun.star.beans.XPropertySet xTTPS = UnoRuntime.queryInterface(com.sun.star.beans.XPropertySet.class, xTT);

        // Change the BackColor
        try {
            xTTPS.setPropertyValue("BackTransparent", Boolean.FALSE);
            xTTPS.setPropertyValue("BackColor",Integer.valueOf(13421823));
            xTTRowPS.setPropertyValue("BackTransparent", Boolean.FALSE);
            xTTRowPS.setPropertyValue("BackColor",Integer.valueOf(6710932));

        } catch (Exception e) {
            System.err.println("Couldn't change the color " + e);
            e.printStackTrace(System.err);
        }

        // write Text in the Table headers
        System.out.println("Write text in the table headers");

        insertIntoCell("A1","FirstColumn", xTT);
        insertIntoCell("B1","SecondColumn", xTT) ;
        insertIntoCell("C1","ThirdColumn", xTT) ;
        insertIntoCell("D1","SUM", xTT) ;


        //Insert Something in the text table
        System.out.println("Insert something in the text table");

        (xTT.getCellByName("A2")).setValue(22.5);
        (xTT.getCellByName("B2")).setValue(5615.3);
        (xTT.getCellByName("C2")).setValue(-2315.7);
        (xTT.getCellByName("D2")).setFormula("sum <A2:C2>");

        (xTT.getCellByName("A3")).setValue(21.5);
        (xTT.getCellByName("B3")).setValue(615.3);
        (xTT.getCellByName("C3")).setValue(-315.7);
        (xTT.getCellByName("D3")).setFormula("sum <A3:C3>");

        (xTT.getCellByName("A4")).setValue(121.5);
        (xTT.getCellByName("B4")).setValue(-615.3);
        (xTT.getCellByName("C4")).setValue(415.7);
        (xTT.getCellByName("D4")).setFormula("sum <A4:C4>");


        //oooooooooooooooooooooooooooStep 5oooooooooooooooooooooooooooooooooooooooo
        // insert a colored text.
        // Get the propertySet of the cursor, change the CharColor and add a
        // shadow. Then insert the Text via InsertString at the cursor position.


        // get the property set of the cursor
        com.sun.star.beans.XPropertySet xTCPS = UnoRuntime.queryInterface(com.sun.star.beans.XPropertySet.class,
                xTCursor);

        // Change the CharColor and add a Shadow
        try {
            xTCPS.setPropertyValue("CharColor",Integer.valueOf(255));
            xTCPS.setPropertyValue("CharShadowed", Boolean.TRUE);
        } catch (Exception e) {
            System.err.println("Couldn't change the color " + e);
            e.printStackTrace(System.err);
        }

        //create a paragraph break
        try {
            xText.insertControlCharacter(xTCursor,
                    com.sun.star.text.ControlCharacter.PARAGRAPH_BREAK, false);

        } catch (Exception e) {
            System.err.println("Couldn't insert break "+ e);
            e.printStackTrace(System.err);
        }

        //inserting colored Text
        System.out.println("Inserting colored Text");

        xText.insertString(xTCursor, " This is a colored Text - blue with shadow\n",
                false );

        //create a paragraph break
        try {
            xText.insertControlCharacter(xTCursor,
                    com.sun.star.text.ControlCharacter.PARAGRAPH_BREAK, false);

        } catch (Exception e) {
            System.err.println("Couldn't insert break "+ e);
            e.printStackTrace(System.err);
        }

        //oooooooooooooooooooooooooooStep 6oooooooooooooooooooooooooooooooooooooooo
        // insert a text frame.
        // create an instance of com.sun.star.text.TextFrame using the MSF of the
        // document. Change some properties an insert it.
        // Now get the text-Object of the frame and the corresponding cursor.
        // Insert some text via insertString.


        // Create a TextFrame
        com.sun.star.text.XTextFrame xTF = null;
        com.sun.star.drawing.XShape xTFS = null;

        try {
            Object oInt = xDocMSF.createInstance("com.sun.star.text.TextFrame");
            xTF = UnoRuntime.queryInterface(
                    com.sun.star.text.XTextFrame.class,oInt);
            xTFS = UnoRuntime.queryInterface(
                    com.sun.star.drawing.XShape.class,oInt);

            com.sun.star.awt.Size aSize = new com.sun.star.awt.Size();
            aSize.Height = 400;
            aSize.Width = 15000;

            xTFS.setSize(aSize);
        } catch (Exception e) {
            System.err.println("Couldn't create instance "+ e);
            e.printStackTrace(System.err);
        }

        // get the property set of the text frame
        com.sun.star.beans.XPropertySet xTFPS = UnoRuntime.queryInterface(com.sun.star.beans.XPropertySet.class, xTF);

        // Change the AnchorType
        try {
            xTFPS.setPropertyValue("AnchorType",
                    com.sun.star.text.TextContentAnchorType.AS_CHARACTER);
        } catch (Exception e) {
            System.err.println("Couldn't change the color " + e);
            e.printStackTrace(System.err);
        }

        //insert the frame
        System.out.println("Insert the text frame");

        try {
            xText.insertTextContent(xTCursor, xTF, false);
        } catch (Exception e) {
            System.err.println("Couldn't insert the frame " + e);
            e.printStackTrace(System.err);
        }

        //getting the text object of Frame
        com.sun.star.text.XText xTextF = xTF.getText();

        //create a cursor object
        com.sun.star.text.XTextCursor xTCF = xTextF.createTextCursor();

        //inserting some Text
        xTextF.insertString(xTCF,
                "The first line in the newly created text frame.", false);


        xTextF.insertString(xTCF,
                "\nWith this second line the height of the frame raises.", false);

        //insert a paragraph break
        try {
            xText.insertControlCharacter(xTCursor,
                    com.sun.star.text.ControlCharacter.PARAGRAPH_BREAK, false );

        } catch (Exception e) {
            System.err.println("Couldn't insert break "+ e);
            e.printStackTrace(System.err);
        }

        // Change the CharColor and add a Shadow
        try {
            xTCPS.setPropertyValue("CharColor",Integer.valueOf(65536));
            xTCPS.setPropertyValue("CharShadowed", Boolean.FALSE);
        } catch (Exception e) {
            System.err.println("Couldn't change the color " + e);
            e.printStackTrace(System.err);
        }

        xText.insertString(xTCursor, " That's all for now !!", false );

    }


}

