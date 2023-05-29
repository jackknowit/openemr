# openemr
项目：基于libreoffice的跨平台电子病历书写系统  

原则：  
* 非侵入式。 对libreoffice源码不做定制， 可升级libreoffice以获得最新办公软件功能和bug修复。


在Linux下java swt测试程序中嵌入libreoffice的运行效果:  
单选元素：
![alt 无法显示图片，检查你的网络](https://github.com/jacknsf/openemr/blob/main/doc/images/singlesel.png)
多选元素：
![alt 无法显示图片，检查你的网络](https://github.com/jacknsf/openemr/blob/main/doc/images/multisel.png)
日期时间元素：
![alt 无法显示图片，检查你的网络](https://github.com/jacknsf/openemr/blob/main/doc/images/datetime.png)


测试环境一：    
Linux 6.1.0-kali5-amd64 Debian 6.1.12-1kali2(2023-02-23) & Xfce 4.18 & GTK 3.24.36  
LibreOffice 7.4.5.1 VCL:x11  

测试环境二：  
Linux fedora 6.2.2-301.fc38.x86_64 & KDE Plasma 5.27.3 Wayland & Qt 5.15.8  
LibreOffice 7.5.1.2 VCL:x11  

测试环境三：  
Windows 11 专业版  
LibreOffice 7.4.3.2 VCL:win  

其它：  
Libreoffice在Linux下，VCL需指定为x11  
可设置环境变量：  
export SAL_USE_VCLPLUGIN=gen






