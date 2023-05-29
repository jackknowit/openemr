import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

public class FrmStartRevise extends Dialog {

    private String m_message;
    public FrmStartRevise(Shell parent) {
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

    public String AuthorName="";

    public void open() {
        Shell parent = getParent();
        shell = new Shell(parent, SWT.TITLE | SWT.BORDER | SWT.APPLICATION_MODAL);
        shell.setText("开始修订");
        shell.setLayout(new GridLayout(4, true));

        Label lb1=new Label(shell,SWT.NULL);
        lb1.setText("修订人员姓名");

        GridData data=new GridData(GridData.FILL_HORIZONTAL);
        data.horizontalSpan=3;
        Text txtPsnName=new Text(shell,SWT.BORDER);
        txtPsnName.setLayoutData(data);


        Button ok = new Button(shell, SWT.PUSH);
        ok.setText("确定");
        data = new GridData(GridData.FILL_HORIZONTAL);
        data.horizontalSpan=2;
        ok.setLayoutData(data);
        ok.addListener(SWT.Selection, event -> {
            m_message =null;

            if(txtPsnName.getText().trim().length()<=0 || txtPsnName.getText().trim().length()<=0)
            {
                Tools.showErrorMessage(shell,"修订人员姓名不可为空!");
                return;
            }


           AuthorName=txtPsnName.getText();

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
