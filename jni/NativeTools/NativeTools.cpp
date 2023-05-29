
#include "../include/NativeToolsX11.h"
#include "iostream"

#include "gdk/gdk.h"
#include <gdk/gdkx.h>
#include "gtk/gtk.h"

/*
unsigned long GetColor( Display* dis, char* color_name )
{
    Colormap cmap;
    XColor near_color, true_color;
    cmap = DefaultColormap( dis, 0 );
    XAllocNamedColor( dis, cmap, color_name, &near_color, &true_color );
    return( near_color.pixel );

}

void DrawAnimation(Display *dis, Window wp)
{
         
    XSetWindowAttributes att;
    GC gc;
    gc = XCreateGC( dis, wp, 0, 0 );
    XSetFunction( dis, gc, GXxor );
    for (int t = 0; t < 100; t++)
    {

        XSetForeground( dis, gc, BlackPixel(dis, 0)^GetColor( dis, "red"));
        XFillArc( dis, wp, gc, t*5+80, t*3+40, 80, 40, 0, 360*64);
        XSetForeground( dis, gc, BlackPixel(dis, 0)^GetColor( dis, "red"));
        usleep(200000);
        XFillArc( dis, wp, gc, t*5+80, t*3+40, 80, 40, 0, 360*64);

    }
    
}

*/

void reparent (Display *d, Window child, Window new_parent)
{
    XUnmapWindow(d, child);
    XMapWindow(d, new_parent);
    XSync(d, False);
    XReparentWindow(d, child, new_parent, 0, 0);
    XMapWindow(d, child);
    // 1 ms seems to be enough even during `nice -n -19 stress -c $cpuThreadsCount` (pacman -S stress) on linux-tkg-pds.
    // Probably can be decreased even further.
    //usleep(1e3);
    usleep(2e3);
    XSync(d, False);
}

JNIEXPORT jlong JNICALL Java_NativeToolsX11_set_1parent_1gtk
  (JNIEnv *, jobject job, jlong lp, jlong lc)
  {

        std::cout<<"parent h: "<<lp <<" child h:"<<lc<<std::endl;
        
        GtkWidget* pWidget = (GtkWidget*)((long)lp);        
        GdkWindow* pWindow = gtk_widget_get_window(pWidget);   
        long x11id = GDK_WINDOW_XWINDOW(pWindow);
        
        std::cout<<"parent xwindow h: "<<x11id<<std::endl;
        
        Window wp=(Window)x11id;
        Window wc=(Window)lc;
        
    
        //Display* dis = XOpenDisplay(NULL);
        GdkDisplay* gd = gdk_display_get_default();
        Display* d = GDK_DISPLAY_XDISPLAY(gd);
        if(NULL==d)
        {
            std::cout<<"get display failed." <<std::endl;
            return -1;
        }
        
        reparent(d,wc,wp); 
        

        /*
        GdkWindow* gw = gdk_x11_window_foreign_new_for_display(gd, wc);
        if(NULL==gw)
        {
            std::cout<<"from libreoffice x window creage gdkwindow failed." <<std::endl;
            return -1;
        }*/
        
        return 1024;
      
  }
  
  JNIEXPORT void JNICALL Java_NativeToolsX11_move_1resize_1window
  (JNIEnv *, jobject job, jlong lwin, jint ix, jint iy, jint iwidth, jint iheight)
  {

   
        Window wc=(Window)lwin;
        
        Display* dis = XOpenDisplay(NULL);
        if(NULL==dis)
        {
            std::cout<<"get display failed." <<std::endl;
            return;
          
        }
        
        XMoveResizeWindow(dis,wc,ix,iy,iwidth,iheight);
        
      
  }
  