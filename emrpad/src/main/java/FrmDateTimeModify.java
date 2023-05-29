import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FrmDateTimeModify extends Dialog {
    public AreaData m_Area =null;

    private String m_message;
    public FrmDateTimeModify(Shell parent) {
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
    private DateTime calendar;
    private DateTime time;

    public void open() {
        Shell parent = getParent();
        shell = new Shell(parent, SWT.TITLE | SWT.BORDER | SWT.APPLICATION_MODAL);
        shell.setText("日期时间");
        shell.setLayout(new GridLayout(4, true));

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
        lb4.setText("日期格式码");
        Text txtDateMask=new Text(shell,SWT.BORDER);
        txtDateMask.setLayoutData(data);
        txtDateMask.setText("yyyy年M月d日");

        Label lb5=new Label(shell,SWT.NULL);
        lb5.setText("时间格式码");
        Text txtTimeMask=new Text(shell,SWT.BORDER);
        txtTimeMask.setLayoutData(data);
        txtTimeMask.setText("H:m:s");

        Label lb3=new Label(shell,SWT.NULL);
        lb3.setText("值");

        Text txtValue=new Text(shell,SWT.BORDER);
        txtValue.setLayoutData(data);

        Label lbTmp=new Label(shell,SWT.NULL);
        lbTmp.setText("");

        Button btnCheck=new Button(shell,SWT.CHECK);
        btnCheck.setText("可编辑");
        btnCheck.setSelection(true);
        data=new GridData(GridData.FILL_HORIZONTAL);
        data.horizontalSpan=3;
        btnCheck.setLayoutData(data);




        if(null!=m_Area)
        {
            txtGroupID.setText(m_Area.GroupID);
            txtElementID.setText(m_Area.ID);
            btnCheck.setSelection(m_Area.Editable.equals("W"));

            txtValue.setText(m_Area.Value);
            txtDateMask.setText(m_Area.DateMask);
            txtTimeMask.setText(m_Area.TimeMask);



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
            m_Area.ActionType = "时间";

            m_Area.Value = "  ";
            m_Area.ValCode = "";

            m_Area.Editable = (btnCheck.getSelection() == true ? "W" : "R");

            String sfullformatstr = txtDateMask.getText() + " " + txtTimeMask.getText();
            sfullformatstr = sfullformatstr.trim();

            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(sfullformatstr);

            txtValue.setText(formatter.format(LocalDateTime.now()));
            m_Area.Value = txtValue.getText();


            m_Area.DateMask = txtDateMask.getText();
            m_Area.TimeMask = txtTimeMask.getText();


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
        shell.pack();
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
