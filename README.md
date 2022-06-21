# Android-Final-Project

## 1.apk可以直接在模拟器或手机上安装，但由于未部署后端，(很有可能)无法登录。

## 2.后端部署方法:首先在电脑上安装django;之后:
+ 进入Android\_Backend; 
+ python manage.py migrate;
+ python manage.py runserver 0.0.0.0:8000;


## 3.前端部署方法:首先找到本机的ipv4地址,类似下图:
进入Android\_Frontend;使用AS打开项目(或者进入\ ./app/src/main/java/com/example/myapplication\ 路径),打开Utils.java文件，将地址填入对于位置;
随后运行项目得到apk即可.
