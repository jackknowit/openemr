import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import java.util.ArrayList;
import java.util.Arrays;

public class FrmSaveAll extends Dialog {

    public EMRPad[] m_pads=null;
    private Table m_table =null;
    private String m_message;

    public FrmSaveAll(Shell parent) {
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
    public void open() {
        Shell parent = getParent();
        shell = new Shell(parent, SWT.TITLE | SWT.BORDER | SWT.APPLICATION_MODAL);
        shell.setText("保存文件");
        shell.setLayout(new GridLayout(2, true));

        GridData data = new GridData();
        data.horizontalSpan = 2;

        Button btnCheck=new Button(shell,SWT.CHECK);
        btnCheck.setText("全选");
        btnCheck.setLayoutData(data);
        btnCheck.addSelectionListener(new SelectionAdapter() {

            @Override
            public void widgetSelected(SelectionEvent event) {
                Button btn = (Button) event.getSource();
                boolean bCheck=btn.getSelection();

                for(int i=0;i<m_table.getItemCount();i++) {
                    TableItem item = m_table.getItem(i);
                    item.setChecked(bCheck);
                }

            }
        });


        m_table = new Table(shell, SWT.CHECK | SWT.BORDER | SWT.V_SCROLL
                | SWT.H_SCROLL|SWT.FULL_SELECTION);
        m_table.setHeaderVisible(true);

        String[] titles = { "文件"};

        for (int loopIndex = 0; loopIndex < titles.length; loopIndex++) {
            TableColumn column = new TableColumn(m_table, SWT.NULL);
            column.setText(titles[loopIndex]);
        }


        if(m_pads!=null) {
            for (int i = 0; i < m_pads.length; i++) {
                TableItem item = new TableItem(m_table, SWT.NULL);
                item.setText(m_pads[i].m_sFileNameNoExt);
                item.setText(0, m_pads[i].m_sFileNameNoExt);


            }
        }



        for (int loopIndex = 0; loopIndex < titles.length; loopIndex++) {
            m_table.getColumn(loopIndex).pack();
        }


        data = new GridData(GridData.FILL_HORIZONTAL);
        data.horizontalSpan = 2;
        m_table.setLayoutData(data);



        Button ok = new Button(shell, SWT.PUSH);
        ok.setText("保存");
        data = new GridData(GridData.FILL_HORIZONTAL);
        ok.setLayoutData(data);
        ok.addListener(SWT.Selection, event -> {
            m_message =null;

            ArrayList<EMRPad> arpads=new ArrayList<>();

            for(int i=0;i<m_table.getItemCount();i++)
            {
                TableItem item=m_table.getItem(i);

                if(item.getChecked())
                {
                    arpads.add(m_pads[i]);
                }

            }



            if(arpads.size()>0) {
                m_pads=arpads.toArray(new EMRPad[0]);

                m_message = "set";
            }

            shell.dispose();
        });

        Button cancel = new Button(shell, SWT.PUSH);
        cancel.setText("放弃保存");
        data = new GridData(GridData.FILL_HORIZONTAL);
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