import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.*;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;

public class FrmDateTime extends Dialog {
    public AreaData m_Area =null;

    private String m_message;
    public FrmDateTime(Shell parent) {
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
    private DateTime calendar;
    private DateTime time;

    public void open() {
        Shell parent = getParent();
        shell = new Shell(parent, SWT.TITLE | SWT.BORDER | SWT.APPLICATION_MODAL);
        shell.setText("日期时间");
        shell.setLayout(new GridLayout(2, true));

        GridData data =null;
        calendar = new DateTime( shell, SWT.CALENDAR);
        time = new DateTime( shell, SWT.TIME | SWT.TIME );


        if(null!=m_Area)
        {
            String sMask=m_Area.DateMask+" "+m_Area.TimeMask;
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern(sMask);
            LocalDateTime dateTime = LocalDateTime.parse(m_Area.Value, formatter);

            if(null!=dateTime) {


                int imonth= dateTime.getMonthValue()-1; //奇怪：为什么月份要减1才能在swt的caledar形式的日期时间控件上显示正确的月份

                calendar.setDate(dateTime.getYear(),imonth, dateTime.getDayOfMonth());
                calendar.setTime(dateTime.getHour(), dateTime.getMinute(), dateTime.getSecond());

                time.setDate(dateTime.getYear(), imonth, dateTime.getDayOfMonth());
                time.setTime(dateTime.getHour(), dateTime.getMinute(), dateTime.getSecond());

            }

            if(m_Area.DateMask==null || m_Area.DateMask.trim().length()<=0)
                calendar.setVisible(false);

            if(m_Area.TimeMask==null || m_Area.TimeMask.trim().length()<=0)
                time.setVisible(false);


        }


        Button ok = new Button(shell, SWT.PUSH);
        ok.setText("确定");
        data = new GridData(GridData.FILL_HORIZONTAL);
        ok.setLayoutData(data);
        ok.addListener(SWT.Selection, event -> {
            m_message =null;

            String sdt = "";
            String stm = "";

            if(calendar.getVisible()==true)
            {
                Calendar cal = Calendar.getInstance();
                cal.set( Calendar.YEAR, calendar.getYear() );
                cal.set( Calendar.MONTH, calendar.getMonth() );
                cal.set( Calendar.DAY_OF_MONTH, calendar.getDay() );
                cal.set( Calendar.HOUR_OF_DAY, time.getHours() );
                cal.set( Calendar.MINUTE, time.getMinutes() );
                cal.set( Calendar.SECOND, time.getSeconds() );

                sdt= new SimpleDateFormat(m_Area.DateMask).format( cal.getTime());

                m_message ="set";
            }

            if(time.getVisible()==true)
            {
                Calendar cal = Calendar.getInstance();
                cal.set( Calendar.YEAR, calendar.getYear() );
                cal.set( Calendar.MONTH, calendar.getMonth() );
                cal.set( Calendar.DAY_OF_MONTH, calendar.getDay() );
                cal.set( Calendar.HOUR_OF_DAY, time.getHours() );
                cal.set( Calendar.MINUTE, time.getMinutes() );
                cal.set( Calendar.SECOND, time.getSeconds() );
                stm= new SimpleDateFormat(m_Area.TimeMask).format( cal.getTime());

                m_message ="set";
            }

            m_Area.Value = sdt + " " + stm;
            m_Area.Value = m_Area.Value.trim();

            m_Area.ValCode = "";

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
