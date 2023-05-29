import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.User32;
import com.sun.jna.platform.win32.WinDef;

public class NativeToolsWin {
    public static final int WS_CAPTION = 12582912;
    public static final int WS_THICKFRAME = 262144;
    public static final int WS_MINIMIZE = 536870912;
    public static final int WS_MAXIMIZE = 16777216;
    public static final int WS_SYSMENU = 524288;
    public static final int GWL_STYLE = -16;


    private static WinDef.HWND ltohwnd(long l)
    {
        WinDef.HWND hwnd=new WinDef.HWND();
        Pointer p=new Pointer(l);
        hwnd.setPointer(p);
        return hwnd;

    }
    public static int GetWindowLong(long l,int i)
    {
        return User32.INSTANCE.GetWindowLong(ltohwnd(l),i);
    }
    public static int SetWindowLong(long l,int i,int i1)
    {
        return  User32.INSTANCE.SetWindowLong(ltohwnd(l),i,i1);

    }
}
