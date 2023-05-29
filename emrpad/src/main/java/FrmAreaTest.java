import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

public class FrmAreaTest extends Dialog {
    private EMRPad m_pad=null;

    private String m_message;
    public FrmAreaTest(Shell parent,EMRPad pad) {
        super(parent);
        m_pad=pad;
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

    public void open() {
        Shell parent = getParent();
        shell = new Shell(parent, SWT.TITLE | SWT.BORDER | SWT.APPLICATION_MODAL);
        shell.setText("结构化取值赋值测试");
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

        Label lb3=new Label(shell,SWT.NULL);
        lb3.setText("值");

        Text txtValue=new Text(shell,SWT.BORDER);
        txtValue.setLayoutData(data);

        Label lb4=new Label(shell,SWT.NULL);
        lb4.setText("值编码");

        Text txtValueCode=new Text(shell,SWT.BORDER);
        txtValueCode.setLayoutData(data);

        Label lbSpace=new Label(shell,SWT.NULL);
        lbSpace.setText(" ");


        Button btnGet = new Button(shell, SWT.PUSH);
        btnGet.setText("取值");
        btnGet.addListener(SWT.Selection, event -> {
            m_message =null;

            if(m_pad==null)
                return;

            if(txtGroupID.getText().trim().length()<=0 || txtElementID.getText().trim().length()<=0)
            {
                Tools.showErrorMessage(shell,"分组ID 和 区域ID 不可为空!");
                return;
            }

            String[] sar=m_pad.getAreaValue(txtGroupID.getText(),txtElementID.getText());
            if(null==sar)
            {
                txtValueCode.setText("");
                txtValue.setText("取值错误");
                return;
            }

            txtValueCode.setText(sar[0]);
            txtValue.setText(sar[1]);
        });


        Button btnSet = new Button(shell, SWT.PUSH);
        btnSet.setText("赋值");
        btnSet.addListener(SWT.Selection, event -> {
            m_message =null;

            if(m_pad==null)
                return;

            if(txtGroupID.getText().trim().length()<=0 || txtElementID.getText().trim().length()<=0)
            {
                Tools.showErrorMessage(shell,"分组ID 和 区域ID 不可为空!");
                return;
            }

            String sVal = txtValue.getText();
            if (sVal.length() <= 0)
                sVal = " ";


            m_pad.setAreaValue(txtGroupID.getText(), txtElementID.getText(), sVal, txtValueCode.getText());


        });




        Button cancel = new Button(shell, SWT.PUSH);
        cancel.setText("关闭");
        cancel.addListener(SWT.Selection, event -> {
            m_message = null;
            shell.dispose();
        });

        shell.setDefaultButton(btnGet);
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
