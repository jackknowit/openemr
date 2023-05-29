import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import java.util.Arrays;

public class FrmMultiSelModify extends Dialog {
    public AreaData m_Area =null;

    private String m_message;
    private Table m_table;

    public FrmMultiSelModify(Shell parent) {
        super(parent);
    }

    public void centerInParent(boolean m_bCenter) {
        this.m_bCenterInParent = m_bCenter;
    }

    private boolean m_bCenterInParent =false;

    private Point m_location=null;
    public  void setLocation( Point ptPos)
    {
        m_location=ptPos;

    }
    public String getMessage() {
        return m_message;
    }

    private Shell shell=null;
    private Text txtValueCode=null;
    public void open() {
        Shell parent = getParent();
        shell = new Shell(parent, SWT.TITLE | SWT.BORDER | SWT.APPLICATION_MODAL);
        shell.setSize(250,400);
        shell.setText("多选项");
        GridLayout gl=new GridLayout();
        gl.numColumns=4;
        shell.setLayout(gl);


        Label lb1=new Label(shell,SWT.NULL);
        lb1.setText("分组ID");

        GridData data=new GridData(GridData.FILL_HORIZONTAL);
        data.horizontalSpan=3;
        Text txtGroupID=new Text(shell,SWT.BORDER);
        txtGroupID.setLayoutData(data);


        Label lb2=new Label(shell,SWT.NULL);
        lb2.setText("元素ID");

        Text txtElementID=new Text(shell,SWT.BORDER);
        txtElementID.setLayoutData(data);

        Label lb4=new Label(shell,SWT.NULL);
        lb4.setText("值");

        data=new GridData(GridData.FILL_HORIZONTAL);
        data.horizontalSpan=3;
        Text txtValue=new Text(shell,SWT.BORDER);
        txtValue.setLayoutData(data);

        Label lb5=new Label(shell,SWT.NULL);
        lb5.setText("值编码");

        data=new GridData(GridData.FILL_HORIZONTAL);
        data.horizontalSpan=3;
        txtValueCode=new Text(shell,SWT.BORDER);
        txtValueCode.setLayoutData(data);



        Button btnDel=new Button(shell,SWT.PUSH);
        btnDel.setText("删除");
        btnDel.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        btnDel.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                int isel=m_table.getSelectionIndex();
                if(isel<0)
                {
                    Tools.showErrorMessage(shell,"需选择要删除的项目!");
                    return;
                }

                m_table.remove(isel);
            }
        });


        Button btnUp=new Button(shell,SWT.PUSH);
        btnUp.setText("上移");
        btnUp.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        btnUp.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                int isel=m_table.getSelectionIndex();
                if(isel<=0)
                    return;

                TableItem itemSel=m_table.getItem(isel);


                TableItem item=new TableItem(m_table,SWT.NULL,isel-1);
                item.setText(itemSel.getText());
                item.setText(0,itemSel.getText(0));
                item.setText(1,itemSel.getText(1));

                m_table.remove(isel+1);
            }
        });

        Button btnDown=new Button(shell,SWT.PUSH);
        btnDown.setText("下移");
        btnDown.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        btnDown.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                int isel=m_table.getSelectionIndex();
                if(isel==m_table.getItemCount()-1)
                    return;

                TableItem itemSel=m_table.getItem(isel);


                TableItem item=new TableItem(m_table,SWT.NULL,isel+2);
                item.setText(itemSel.getText());
                item.setText(0,itemSel.getText(0));
                item.setText(1,itemSel.getText(1));

                m_table.remove(isel);
            }
        });


        Button btnAdd=new Button(shell,SWT.PUSH);
        btnAdd.setText("增加");
        btnAdd.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        btnAdd.addListener(SWT.Selection, new Listener() {
            @Override
            public void handleEvent(Event event) {
                if(txtValue.getText().trim().length()<=0 || txtValueCode.getText().trim().length()<=0)
                {
                    Tools.showErrorMessage(shell,"值 和 值编码 不可为空!");
                    return;
                }

                TableItem item = new TableItem(m_table, SWT.NULL);
                item.setText(txtValue.getText());
                item.setText(0, txtValue.getText());
                item.setText(1, txtValueCode.getText());


            }
        });




        Label lb3=new Label(shell,SWT.NULL);
        lb3.setText("选项");
        data=new GridData();
        data.horizontalSpan=4;
        lb3.setLayoutData(data);





        m_table = new Table(shell,  SWT.CHECK | SWT.BORDER | SWT.V_SCROLL
                | SWT.H_SCROLL|SWT.FULL_SELECTION);
        data=new GridData(GridData.FILL_BOTH);
        data.horizontalSpan=4;
        m_table.setLayoutData(data);
        m_table.setHeaderVisible(true);


        String[] titles = { "值", "编码"};

        for (int loopIndex = 0; loopIndex < titles.length; loopIndex++) {
            TableColumn column = new TableColumn(m_table, SWT.NULL);
            column.setText(titles[loopIndex]);
        }

        if(null!= m_Area) {

            txtGroupID.setText(m_Area.GroupID);
            txtElementID.setText(m_Area.ID);

            m_table.clearAll();


            java.util.List<String> archkItems=null;
            if (m_Area.ValCode != null && m_Area.ValCode.trim().length() > 0)
            {
                String[] schkItems = m_Area.ValCode.split("\\$");
                archkItems=Arrays.asList(schkItems);

            }


            if (null != m_Area.EnumValue && m_Area.EnumValue.length > 0)
            {
                for (int i = 0; i < m_Area.EnumValue.length; i += 2)
                {
                    TableItem item = new TableItem(m_table, SWT.NULL);
                    item.setText(m_Area.EnumValue[i]);
                    item.setText(0, m_Area.EnumValue[i]);
                    item.setText(1, m_Area.EnumValue[i + 1]);

                    if(null!=archkItems)
                        item.setChecked(archkItems.contains(m_Area.EnumValue[i + 1]));

                }

            }

        }

        for (int loopIndex = 0; loopIndex < titles.length; loopIndex++) {
            m_table.getColumn(loopIndex).pack();
        }



        Button btnCheck=new Button(shell,SWT.CHECK);
        btnCheck.setText("可编辑");
        btnCheck.setSelection(true);
        data=new GridData(GridData.FILL_HORIZONTAL);
        data.horizontalSpan=4;
        btnCheck.setLayoutData(data);

        if(null!= m_Area) {
            btnCheck.setSelection(m_Area.Editable.equals("W"));
        }


        Button ok = new Button(shell, SWT.PUSH);
        ok.setText("确定");
        data = new GridData(GridData.FILL_HORIZONTAL);
        data.horizontalSpan=2;
        ok.setLayoutData(data);
        ok.addListener(SWT.Selection, event -> {
            m_message =null;

            if(txtGroupID.getText().trim().length()<=0 || txtElementID.getText().trim().length()<=0)
            {
                Tools.showErrorMessage(shell,"分组ID 和 区域ID 不可为空!");
                return;
            }


            if (null == m_Area)
                m_Area = new AreaData();

            m_Area.GroupID = txtGroupID.getText();
            m_Area.ID = txtElementID.getText();
            m_Area.ActionType = "多选";

            m_Area.Value = " ";
            m_Area.ValCode = "";
            m_Area.EnumValue = new String[] { };

            int itemCount=m_table.getItemCount();

            if(itemCount>0)
            {
                m_Area.EnumValue = new String[itemCount * 2];
                int i = 0;
                for (int j=0;j<itemCount;j++)
                {
                    TableItem item=m_table.getItem(j);
                    m_Area.EnumValue[i] = item.getText(0);
                    m_Area.EnumValue[i + 1] = item.getText(1);

                    if(item.getChecked()==true)
                    {
                        if (m_Area.Value != null && m_Area.Value.trim().length() > 0)
                            m_Area.Value = m_Area.Value + "、";

                        m_Area.Value=m_Area.Value+ m_Area.EnumValue[i];

                        if (m_Area.ValCode != null && m_Area.ValCode.trim().length() > 0)
                            m_Area.ValCode = m_Area.ValCode + "$";

                        m_Area.ValCode =m_Area.ValCode + m_Area.EnumValue[i + 1];

                    }

                    i = i + 2;
                }
            }

            m_Area.Editable = (btnCheck.getSelection() == true ? "W" : "R");

            m_message ="set";


            shell.dispose();
        });

        Button cancel = new Button(shell, SWT.PUSH);
        cancel.setText("取消");
        data = new GridData(GridData.FILL_HORIZONTAL);
        data.horizontalSpan=2;
        cancel.setLayoutData(data);
        cancel.addListener(SWT.Selection, event -> {
            m_message = null;
            shell.dispose();
        });

        shell.setDefaultButton(ok);
        //shell.pack();
        shell.open();

        if(null!=m_location)
            shell.setLocation(m_location);

        if(true== m_bCenterInParent)
        {
            Rectangle parentSize = getParent().getBounds();
            Rectangle shellSize = shell.getBounds();
            int locationX = (parentSize.width - shellSize.width)/2+parentSize.x;
            int locationY = (parentSize.height - shellSize.height)/2+parentSize.y;
            shell.setLocation(locationX,locationY);
        }

        Display display = parent.getDisplay();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
    }
}
