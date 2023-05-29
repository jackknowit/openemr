import org.apache.commons.lang3.SystemUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import java.awt.*;
import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;


public class Application {
    private static Log logger = new Log("Application");
    private static TabFolder m_tabFolder = null;

    private static Display getDisplay() {
        Display display = Display.getCurrent();
        if (null == display)
            display = Display.getDefault();

        return display;
    }

    private static int saveAllFile()
    {
        try
        {
            if(null==m_tabFolder)
                return 0;


            ArrayList<EMRPad> arFileNames=new ArrayList<>();

            int itemCount = m_tabFolder.getItemCount();

            for (int i=0;i< itemCount;i++) {
                TabItem ti = m_tabFolder.getItem(i);
                if (null == ti)
                    continue;

                Control emrpadParent = ti.getControl();
                if (emrpadParent == null)
                    continue;

                if (emrpadParent.getData() == null)
                    continue;

                EMRPad pad = (EMRPad) emrpadParent.getData();
                if (null == pad)
                    continue;

                if(pad.isModified())
                {
                    arFileNames.add(pad);
                }

            }


            if(arFileNames.size()<=0)
                return 0;


            FrmSaveAll fsa = new FrmSaveAll(m_tabFolder.getShell());
            fsa.m_pads=arFileNames.toArray(new EMRPad[0]);

            fsa.centerInParent(true);
            fsa.open();
            String message = fsa.getMessage();
            if (message == null) {
                return 0;
            }

            int iSaveFileCount=0;
            if(message.equals("set"))
            {

                for (EMRPad pad:fsa.m_pads) {

                    if(pad.isNew())
                    {
                        if(pad.saveAsFile(""))
                            iSaveFileCount++;
                    }
                    else
                    {
                        if(pad.saveFile())
                            iSaveFileCount++;
                    }

                }


            }

            return iSaveFileCount;
        }
        catch (Exception ex)
        {
            logger.error(ex);
            return -1;
        }

    }


    //新增TAB 页面，新建空文件或者打开文件，返回emr panel
    private static Control newOrOpenFile(TabFolder tab, String sfilename) {
        try {


            Control panel = null;

            GridData gd = new GridData(GridData.FILL_BOTH);
            if (SystemUtils.IS_OS_WINDOWS) {
                panel = new Composite(tab, SWT.EMBEDDED);
                ((Composite) panel).setLayoutData(gd);
                ((Composite) panel).setBackground(new Color(tab.getDisplay(), 255, 128, 128));
            } else if (SystemUtils.IS_OS_LINUX) {
                panel = new Label(tab, SWT.BORDER);
                ((Label) panel).setLayoutData(gd);
                ((Label) panel).setBackground(new Color(tab.getDisplay(), 255, 128, 128));

            }

            EMRPad pad = new EMRPad();
            if (pad.init(panel, sfilename) == true) {
                panel.setData(pad);
            }


            TabItem tabItem = new TabItem(m_tabFolder, SWT.NULL);
            tabItem.setText(pad.m_sFileNameNoExt);
            tabItem.setControl(panel);

            m_tabFolder.setSelection(tabItem);

            setTitle(m_tabFolder.getShell(),tabItem.getText());

            return panel;


        } catch (Exception ex) {
            logger.error(ex);
            return null;
        }
    }

    private static EMRPad getCurrentPad() {
        if (null == m_tabFolder)
            return null;
        int isel = m_tabFolder.getSelectionIndex();
        if (isel < 0)
            return null;

        TabItem ti = m_tabFolder.getItem(isel);
        if (null == ti)
            return null;

        Control emrpadParent = ti.getControl();
        if (emrpadParent == null)
            return null;

        if (emrpadParent.getData() == null)
            return null;

        return (EMRPad) emrpadParent.getData();
    }

    public static Boolean closeCurrentPad() {

        if (null == m_tabFolder)
            return false;

        if (m_tabFolder.isDisposed())
            return false;

        int isel = m_tabFolder.getSelectionIndex();
        if (isel < 0)
            return false;

        TabItem ti = m_tabFolder.getItem(isel);
        if (null == ti)
            return false;

        Control emrpadParent = ti.getControl();
        if (emrpadParent == null)
            return false;

        if (emrpadParent.getData() == null)
            return false;

        EMRPad pad = (EMRPad) emrpadParent.getData();
        if (null == pad)
            return false;

        if (1 == m_tabFolder.getItemCount())
            pad.exit();
        else
            pad.closeDoc();

        emrpadParent.setData(null);
        emrpadParent.dispose();

        ti.setControl(null);
        ti.dispose();

        return true;

    }

    public static void closeAllPad() {
        int itemCount = m_tabFolder.getItemCount();

        while (itemCount > 0) {
            TabItem ti = m_tabFolder.getItem(0);
            if (null == ti)
                continue;

            Control emrpadParent = ti.getControl();
            if (emrpadParent == null)
                continue;

            if (emrpadParent.getData() == null)
                continue;

            EMRPad pad = (EMRPad) emrpadParent.getData();
            if (null == pad)
                continue;

            pad.setModified(false);

            if (1 == itemCount) {

                pad.exit();
            }
            else {

                pad.closeDoc();
            }

            emrpadParent.setData(null);
            emrpadParent.dispose();

            ti.setControl(null);
            ti.dispose();

            itemCount = m_tabFolder.getItemCount();


        }


    }

    private static void setTitle(Shell shell,String sFileNameNoExt)
    {
        String sTitle="结构化文书编辑器 1.0 ["+sFileNameNoExt+"]";
        shell.setText(sTitle);
    }
    public static void main(String args[]) {

        Display display = getDisplay();

        Shell shell = new Shell(display);

        GridLayout layout = new GridLayout();
        layout.numColumns = 1;
        shell.setLayout(layout);

        Menu menuBar = new Menu(shell, SWT.BAR);
        shell.setMenuBar(menuBar);

        _makeFileMenu(shell, menuBar);
        _makeStructMenu(shell, menuBar);
        _makeEditMenu(shell, menuBar);
        _makeTableMenu(shell, menuBar);
        _makeInsertMenu(shell, menuBar);
        _makeHelpMenu(shell, menuBar);


        GridData gd = new GridData(GridData.FILL_BOTH);
        m_tabFolder = new TabFolder(shell, SWT.BORDER);
        m_tabFolder.setLayoutData(gd);

        newOrOpenFile(m_tabFolder, "");

        Menu popupMenu = new Menu(m_tabFolder);
        MenuItem mnuItemCloseTabPage = new MenuItem(popupMenu, SWT.NONE);
        mnuItemCloseTabPage.setText("关闭");
        mnuItemCloseTabPage.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                logger.info("Close tab page.");
                EMRPad pad=getCurrentPad();
                if(null!=pad)
                {
                    if(pad.isModified())
                    {
                        if(Tools.showQueryMessage(shell,"文件已修改,是否保存?")==SWT.YES) {
                            if (pad.isNew())
                                if(pad.saveAsFile("")==false)
                                    return;
                            else {
                                pad.saveFile();
                            }
                        }
                        else
                        {
                            pad.setModified(false);
                        }
                    }
                }
                closeCurrentPad();


            }
        });


        m_tabFolder.setMenu(popupMenu);
        m_tabFolder.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent selectionEvent) {
                super.widgetSelected(selectionEvent);

                setTitle(m_tabFolder.getShell(),m_tabFolder.getSelection()[0].getText());


            }
        });


        Label statusLabel = new Label(shell, SWT.BORDER);
        statusLabel.setText("Ready");
        statusLabel.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));

        shell.addListener(SWT.Close, new Listener() {
            @Override
            public void handleEvent(Event event) {
                saveAllFile();
                closeAllPad();
            }
        });

        shell.open();


        while (!shell.isDisposed()) {

            if (!display.readAndDispatch()) display.sleep();

        }

        System.out.println("done");

    }

    private static void _makeHelpMenu(Shell shell, Menu menuBar) {
        //创建帮助菜单组
        MenuItem helpMenuHeader = new MenuItem(menuBar, SWT.CASCADE);
        helpMenuHeader.setText("帮助");

        Menu helpMenu = new Menu(shell, SWT.DROP_DOWN);
        helpMenuHeader.setMenu(helpMenu);

        MenuItem testMenuItem = new MenuItem(helpMenu, SWT.PUSH);
        testMenuItem.setText("功能测试");
        testMenuItem.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
               getCurrentPad().test(0);
            }
        });


        MenuItem aboutLibreofficeMenuItem = new MenuItem(helpMenu, SWT.PUSH);
        aboutLibreofficeMenuItem.setText("关于 LibreOffice");
        aboutLibreofficeMenuItem.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                getCurrentPad().aboutLibreoffice();
            }
        });

    }

    private static void _makeInsertMenu(Shell shell, Menu menuBar) {
        //创建插入菜单组
        MenuItem insertMenuHeader = new MenuItem(menuBar, SWT.CASCADE);
        insertMenuHeader.setText("插入");

        Menu insertMenu = new Menu(shell, SWT.DROP_DOWN);
        insertMenuHeader.setMenu(insertMenu);

        MenuItem insertOLEMenuItem = new MenuItem(insertMenu, SWT.PUSH);
        insertOLEMenuItem.setText("插入OLE对象");
        insertOLEMenuItem.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                getCurrentPad().insertOle("", 100, 100);
            }
        });
    }

    private static void _makeTableMenu(Shell shell, Menu menuBar) {
        //创建表格菜单组
        MenuItem tableMenuHeader = new MenuItem(menuBar, SWT.CASCADE);
        tableMenuHeader.setText("表格");

        Menu tableMenu = new Menu(shell, SWT.DROP_DOWN);
        tableMenuHeader.setMenu(tableMenu);

        MenuItem insertTableMenuItem = new MenuItem(tableMenu, SWT.PUSH);
        insertTableMenuItem.setText("插入表格");
        insertTableMenuItem.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                getCurrentPad().insertTable();
            }
        });
    }

    private static void _makeEditMenu(Shell shell, Menu menuBar) {
        //创建编辑菜单组
        MenuItem editMenuHeader = new MenuItem(menuBar, SWT.CASCADE);
        editMenuHeader.setText("编辑");

        Menu editMenu = new Menu(shell, SWT.DROP_DOWN);
        editMenuHeader.setMenu(editMenu);

        MenuItem insertSubFileMenuItem = new MenuItem(editMenu, SWT.PUSH);
        insertSubFileMenuItem.setText("插入文件");
        insertSubFileMenuItem.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {

                FileDialog dialog = new FileDialog(shell, SWT.OPEN);
                dialog.setFilterPath("");// 设置默认的路径
                dialog.setText("插入文件");//设置对话框的标题
                dialog.setFileName("");//设置默认的文件名
                dialog.setFilterNames(new String[]{"ODF文档 (*.odt)", "所有文件(*.*)"});//设置扩展名
                dialog.setFilterExtensions(new String[]{"*.odt", "*.*"});//设置文件扩展名
                String fileName = dialog.open();//
                if (null == fileName || fileName.isEmpty())
                    return;

                getCurrentPad().insertFile(fileName);

            }
        });

        MenuItem insertBreakMenuItem = new MenuItem(editMenu, SWT.PUSH);
        insertBreakMenuItem.setText("插入段落换行符");
        insertBreakMenuItem.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                getCurrentPad().insertParaBreak();
            }
        });

        MenuItem getSelTextMenuItem = new MenuItem(editMenu, SWT.PUSH);
        getSelTextMenuItem.setText("当前选中文本");
        getSelTextMenuItem.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                Tools.showMessage(shell, "当前选中文本", getCurrentPad().getCurrentSelTxt());
            }
        });

        MenuItem getCurrPageNumberMenuItem = new MenuItem(editMenu, SWT.PUSH);
        getCurrPageNumberMenuItem.setText("当前页号");
        getCurrPageNumberMenuItem.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                Tools.showMessage(shell, "当前页号", getCurrentPad().currentPageNum());
            }
        });

        MenuItem gotoDocEndMenuItem = new MenuItem(editMenu, SWT.PUSH);
        gotoDocEndMenuItem.setText("转文档尾");
        gotoDocEndMenuItem.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                getCurrentPad().goDocEnd();
            }
        });

        MenuItem gotoDocBeginMenuItem = new MenuItem(editMenu, SWT.PUSH);
        gotoDocBeginMenuItem.setText("转文档首");
        gotoDocBeginMenuItem.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                getCurrentPad().goDocStart();
            }
        });


        MenuItem startReviseMenuItem = new MenuItem(editMenu, SWT.PUSH);
        startReviseMenuItem.setText("开始修订");
        startReviseMenuItem.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                getCurrentPad().startRevise("");
            }
        });

        MenuItem endReviseMenuItem = new MenuItem(editMenu, SWT.PUSH);
        endReviseMenuItem.setText("结束修订");
        endReviseMenuItem.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                getCurrentPad().stopRevise();
            }
        });
    }

    private static void _makeStructMenu(Shell shell, Menu menuBar) {
        //创建结构化菜单组
        MenuItem structMenuHeader = new MenuItem(menuBar, SWT.CASCADE);
        structMenuHeader.setText("结构化");

        Menu structMenu = new Menu(shell, SWT.DROP_DOWN);
        structMenuHeader.setMenu(structMenu);

        MenuItem insertTextElementMenuItem = new MenuItem(structMenu, SWT.PUSH);
        insertTextElementMenuItem.setText("插入元素（自由文本）");
        insertTextElementMenuItem.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                getCurrentPad().insertElementText();

            }
        });

        MenuItem insertSingleSelElementMenuItem = new MenuItem(structMenu, SWT.PUSH);
        insertSingleSelElementMenuItem.setText("插入元素（单选）");
        insertSingleSelElementMenuItem.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                getCurrentPad().insertElementSingleSel();
            }
        });

        MenuItem insertMultiSelElementMenuItem = new MenuItem(structMenu, SWT.PUSH);
        insertMultiSelElementMenuItem.setText("插入元素（多选）");
        insertMultiSelElementMenuItem.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                getCurrentPad().insertElementMultiSel();
            }
        });

        MenuItem insertDatetimeElementMenuItem = new MenuItem(structMenu, SWT.PUSH);
        insertDatetimeElementMenuItem.setText("插入元素（日期时间）");
        insertDatetimeElementMenuItem.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                getCurrentPad().insertElementDateTime();
            }
        });

        MenuItem modifyElementMenuItem = new MenuItem(structMenu, SWT.PUSH);
        modifyElementMenuItem.setText("修改元素");
        modifyElementMenuItem.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                getCurrentPad().modifyElement();
            }
        });

        MenuItem testSetGetElementValueMenuItem = new MenuItem(structMenu, SWT.PUSH);
        testSetGetElementValueMenuItem.setText("赋值取值测试");
        testSetGetElementValueMenuItem.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {

                try {
                    FrmAreaTest dlg = new FrmAreaTest(shell, getCurrentPad());
                    dlg.centerInParent(true);
                    dlg.open();

                } catch (Exception ex) {
                    logger.error(ex);

                }

            }
        });

        MenuItem exportXmlDataMenuItem = new MenuItem(structMenu, SWT.PUSH);
        exportXmlDataMenuItem.setText("导出结构化数据XML");
        exportXmlDataMenuItem.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {

                try {
                    FileDialog dialog = new FileDialog(shell, SWT.SAVE);
                    dialog.setFilterPath("");// 设置默认的路径
                    dialog.setText("导出XML");//设置对话框的标题
                    dialog.setFileName("");//设置默认的文件名
                    dialog.setFilterNames(new String[]{"XML文件 (*.xml)", "所有文件(*.*)"});//设置扩展名
                    dialog.setFilterExtensions(new String[]{"*.xml", "*.*"});//设置文件扩展名
                    String filename = dialog.open();//
                    if (null == filename || filename.trim().length() <= 0) {
                        return;
                    }

                    String sXml = getCurrentPad().getXml();
                    if (sXml == null) {
                        Tools.showErrorMessage(shell, "导出结构化数据失败!");
                        return;
                    }
                    if (sXml.trim().length() <= 0) {
                        Tools.showErrorMessage(shell, "当前文书无结构化元素，未能导出结构化数据!");
                        return;
                    }

                    File fe=new File(filename);
                    if(fe.exists())
                        fe.delete();

                    Files.writeString(Path.of(filename), sXml);

                    if(fe.exists()==false)
                    {
                        Tools.showErrorMessage(shell, "导出结构化数据失败!");

                    }

                    Tools.showMessage(shell, "提示","导出结构化数据成功!文件:"+filename);

                }
                catch (Exception ex)
                {
                    logger.error(ex);
                    Tools.showErrorMessage(shell, "导出结构化数据失败!");

                }



            }
        });



        MenuItem modeUserMenuItem = new MenuItem(structMenu, SWT.CHECK);
        modeUserMenuItem.setText("用户输入模式");
        modeUserMenuItem.setSelection(true);
        modeUserMenuItem.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {

                getCurrentPad().setEditMode((true == modeUserMenuItem.getSelection() ? EditMode.USER : EditMode.TEMPLATE));

            }
        });


        MenuItem modeStructElementDisableDeleteMenuItem = new MenuItem(structMenu, SWT.CHECK);
        modeStructElementDisableDeleteMenuItem.setText("结构化元素不可删除");
        modeStructElementDisableDeleteMenuItem.setSelection(true);
        modeStructElementDisableDeleteMenuItem.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                getCurrentPad().setStructCanRemove(!modeStructElementDisableDeleteMenuItem.getSelection());

            }
        });
    }

    private static void _makeFileMenu(Shell shell, Menu menuBar) {
        //创建文件菜单组
        MenuItem fileMenuHeader = new MenuItem(menuBar, SWT.CASCADE);
        fileMenuHeader.setText("文件");

        Menu fileMenu = new Menu(shell, SWT.DROP_DOWN);
        fileMenuHeader.setMenu(fileMenu);

        MenuItem newMenuItem = new MenuItem(fileMenu, SWT.PUSH);
        newMenuItem.setText("新建文件");
        newMenuItem.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                newOrOpenFile(m_tabFolder, "");
            }
        });

        MenuItem openMenuItem = new MenuItem(fileMenu, SWT.PUSH);
        openMenuItem.setText("打开文件");
        openMenuItem.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {

                FileDialog dialog = new FileDialog(shell, SWT.OPEN);
                dialog.setFilterPath("");// 设置默认的路径
                dialog.setText("打开文件");//设置对话框的标题
                dialog.setFileName("");//设置默认的文件名
                dialog.setFilterNames(new String[]{"ODF文档 (*.odt)", "所有文件(*.*)"});//设置扩展名
                dialog.setFilterExtensions(new String[]{"*.odt", "*.*"});//设置文件扩展名
                String fileName = dialog.open();//
                if (null == fileName || fileName.isEmpty())
                    return;

                newOrOpenFile(m_tabFolder, fileName);


            }
        });

        MenuItem saveMenuItem = new MenuItem(fileMenu, SWT.PUSH);
        saveMenuItem.setText("保存文件");
        saveMenuItem.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {

                getCurrentPad().saveFile();


            }
        });


        MenuItem saveAsMenuItem = new MenuItem(fileMenu, SWT.PUSH);
        saveAsMenuItem.setText("另存文件");
        saveAsMenuItem.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {


                getCurrentPad().saveAsFile("");


            }
        });

        MenuItem saveAllModifyedFileMenuItem = new MenuItem(fileMenu, SWT.PUSH);
        saveAllModifyedFileMenuItem.setText("保存所有已修改文件");
        saveAllModifyedFileMenuItem.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {

               saveAllFile();


            }
        });


        MenuItem newFromTemplateMenuItem = new MenuItem(fileMenu, SWT.PUSH);
        newFromTemplateMenuItem.setText("从模板文件新建");
        newFromTemplateMenuItem.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                FileDialog dialog = new FileDialog(shell, SWT.OPEN);
                dialog.setFilterPath("");// 设置默认的路径
                dialog.setText("选择模板文件");//设置对话框的标题
                dialog.setFileName("");//设置默认的文件名
                dialog.setFilterNames(new String[]{"ODF文档 (*.odt)", "所有文件(*.*)"});//设置扩展名
                dialog.setFilterExtensions(new String[]{"*.odt", "*.*"});//设置文件扩展名
                String fileName = dialog.open();//
                if (null == fileName || fileName.isEmpty())
                    return;

                String sNewfile = EMRPad.newFileFromTemplate(fileName);

                newOrOpenFile(m_tabFolder, sNewfile);


            }
        });


        MenuItem closeMenuItem = new MenuItem(fileMenu, SWT.PUSH);
        closeMenuItem.setText("关闭文件");
        closeMenuItem.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {

                EMRPad pad=getCurrentPad();
                if(null!=pad)
                {
                    if(pad.isModified())
                    {
                        if(Tools.showQueryMessage(shell,"文件已修改,是否保存?")==SWT.YES) {
                            if (pad.isNew())
                                if(pad.saveAsFile("")==false)
                                    return;
                                else {
                                    pad.saveFile();
                                }
                        }
                        else
                        {
                            pad.setModified(false);
                        }
                    }
                }
                closeCurrentPad();

            }
        });

        MenuItem exitMenuItem = new MenuItem(fileMenu, SWT.PUSH);
        exitMenuItem.setText("退出");
        exitMenuItem.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                shell.close();
            }
        });
    }

}




/* vim:set shiftwidth=4 softtabstop=4 expandtab: */
