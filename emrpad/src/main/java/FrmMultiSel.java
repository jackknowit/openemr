import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;
import java.util.Arrays;

public class FrmMultiSel extends Dialog {
    public AreaData m_Area=null;

    private Table m_table =null;
    private String m_message;

    public FrmMultiSel(Shell parent) {
        super(parent);
    }

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
        shell.setText("单选");
        shell.setLayout(new GridLayout(2, true));

        //Label label = new Label(shell, SWT.NONE);
        //label.setText("选项:");
        GridData data = new GridData();
        data.horizontalSpan = 2;
        //label.setLayoutData(data);

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

        String[] titles = { "值", "编码"};

        for (int loopIndex = 0; loopIndex < titles.length; loopIndex++) {
            TableColumn column = new TableColumn(m_table, SWT.NULL);
            column.setText(titles[loopIndex]);
        }

        java.util.List<String> listValCodes=null;
        if(m_Area.ValCode.length()>0)
        {
            String[] valcodes=m_Area.ValCode.split("\\$");
            if(valcodes!=null)
                listValCodes= Arrays.asList(valcodes);


        }
        if(null!=m_Area && null!=m_Area.EnumValue && m_Area.EnumValue.length>0)
        {
            for(int i=0;i<m_Area.EnumValue.length;i+=2)
            {
                TableItem item = new TableItem(m_table, SWT.NULL);
                item.setText(m_Area.EnumValue[i]);
                item.setText(0, m_Area.EnumValue[i]);
                item.setText(1, m_Area.EnumValue[i+1]);

                if(listValCodes!=null && listValCodes.contains(m_Area.EnumValue[i+1]))
                    item.setChecked(true);

            }

        }

        for (int loopIndex = 0; loopIndex < titles.length; loopIndex++) {
            m_table.getColumn(loopIndex).pack();
        }


        data = new GridData(GridData.FILL_HORIZONTAL);
        data.horizontalSpan = 2;
        m_table.setLayoutData(data);



        Button ok = new Button(shell, SWT.PUSH);
        ok.setText("确定");
        data = new GridData(GridData.FILL_HORIZONTAL);
        ok.setLayoutData(data);
        ok.addListener(SWT.Selection, event -> {
            m_message =null;

            String areaValue="";
            String areaValueCode="";

            for(int i=0;i<m_table.getItemCount();i++)
            {
                TableItem item=m_table.getItem(i);

                if(item.getChecked())
                {
                    if (areaValue != null && areaValue.trim().length() > 0)
                        areaValue = areaValue + "、";

                    areaValue = areaValue + item.getText(0);

                    if (areaValueCode != null && areaValueCode.trim().length() > 0)
                        areaValueCode = areaValueCode + "$";

                    areaValueCode = areaValueCode + item.getText(1);


                }

            }

            m_Area.Value=areaValue;
            m_Area.ValCode=areaValueCode;

            if(areaValue.length()>0)
                m_message ="set";

            shell.dispose();
        });

        Button cancel = new Button(shell, SWT.PUSH);
        cancel.setText("取消");
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

        Display display = parent.getDisplay();
        while (!shell.isDisposed()) {
            if (!display.readAndDispatch()) {
                display.sleep();
            }
        }
    }
}