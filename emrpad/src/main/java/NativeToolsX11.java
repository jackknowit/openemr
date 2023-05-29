
import java.io.File;

public class NativeToolsX11 {

    static {
        //String sbin_name= Paths.get(".").toAbsolutePath().normalize().toString()+"/lib/libNativeTools.so";
        String sbin_name="/home/jack/proj/oss/emr/jni/NativeTools/Debug/libNativeTools.so";

        File f = new File(sbin_name);
        if (f.exists())
            System.load(sbin_name);
        else
            System.out.println("Linux native tools "+sbin_name+" does not exists.");

    }


    //将xlib 窗口 设置为 swt 的子窗口
    //lparent : swt GtkWidget*
    //lchild : XLib Window obj
    public native long set_parent_gtk(long lparent,long lchild);

    //设置xlib窗口的大小 和 位置
    //lwin : XLib Window obj
    public native void move_resize_window(long lwin,int ix,int iy,int iwidth,int iheight);


}
