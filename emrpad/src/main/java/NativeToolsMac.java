public class NativeToolsMac {
    static {
        String sbin_name="/home/jack/proj/emr/jni/build-Debug/lib/libNativeToolsMac.dylib";
        System.load(sbin_name);
    }

    public native long set_parent_nsview(long lparent,long lchild);
}
