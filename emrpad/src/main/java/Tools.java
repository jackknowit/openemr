import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;
import org.eclipse.swt.widgets.Shell;

public class Tools {
    public static int  showQueryMessage(Shell shell,String message)
    {
        MessageBox messageBox = new MessageBox(shell,   SWT.YES|SWT.NO);

        messageBox.setText("提示");
        messageBox.setMessage(message);
        int buttonID = messageBox.open();
        return buttonID;

    }
    public static void  showMessage(Shell shell,String title,String message)
    {
        MessageBox messageBox = new MessageBox(shell,   SWT.CANCEL);

        messageBox.setText(title);
        messageBox.setMessage(message);
        int buttonID = messageBox.open();
        switch(buttonID) {
            case SWT.YES:
            case SWT.NO:
                break;
            case SWT.CANCEL:

        }

    }

    public static int  showErrorMessage(Shell shell,String message)
    {
        MessageBox messageBox = new MessageBox(shell, SWT.ICON_ERROR| SWT.CANCEL);

        messageBox.setText("错误");
        messageBox.setMessage(message);
        int buttonID = messageBox.open();
        return buttonID;

    }
}
