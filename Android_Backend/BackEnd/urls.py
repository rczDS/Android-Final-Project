# -*- coding: utf-8 -*-
"""BackEnd URL Configuration

The `urlpatterns` list routes URLs to views. For more information please see:
    https://docs.djangoproject.com/en/3.2/topics/http/urls/
Examples:
Function views
    1. Add an import:  from my_app import views
    2. Add a URL to urlpatterns:  path('', views.home, name='home')
Class-based views
    1. Add an import:  from other_app.views import Home
    2. Add a URL to urlpatterns:  path('', Home.as_view(), name='home')
Including another URLconf
    1. Import the include() function: from django.urls import include, path
    2. Add a URL to urlpatterns:  path('blog/', include('blog.urls'))
"""
from django.contrib import admin
from django.urls import path
from django.urls import include, path

# 这个是整个app的路径, 比如说clients, 意思就是访问的时候网站的根目录加上 clients/ 然后再再clients的路由文件 urls.py下面去找对应的
urlpatterns = [
    path('admin/', admin.site.urls),
    path('clients/', include('clients.urls')),
]
