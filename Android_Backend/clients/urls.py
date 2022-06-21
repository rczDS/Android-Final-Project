# -*- coding: utf-8 -*-
from django.urls import path

from . import views


# 这个是 clients 的路由, 比如 定义了 path('get_clients', views.get_clients, name='get_clients') 那就是在app路由过来的 [网站]/clients/get_clients 这个url, 映射到 get_clients 这个函数.
urlpatterns = [
    path('get_clients', views.get_clients, name='get_clients'),
    path('create_user', views.create_user, name='create_user'),
    path('login_user', views.login_user, name='login_user'),
    path('update_user', views.update_user, name='update_user'),
    path('follow_user', views.follow_user, name='follow_user'),
    path('unfollow_user', views.unfollow_user, name='unfollow_user'),
    path('blacklist_user', views.blacklist_user, name='blacklist_user'),
    path('unblacklist_user', views.unblacklist_user, name='unblacklist_user'),
    path('post_active', views.post_active, name='post_active'),
    path('like_active', views.like_active, name='like_active'),
    path('dislike_active', views.dislike_active, name='dislike_active'),
    path('reply_active', views.reply_active, name='reply_active'),
    path('withdraw_reply', views.withdraw_reply, name='withdraw_reply'),
    path('get_personal_info', views.get_personal_info, name='get_personal_info'),
    path('show_all_active', views.show_all_active, name='show_all_active'),
    path('save_draft', views.save_draft, name='save_draft'),
    path('touch_new_message', views.touch_new_message, name='touch_new_message'),

]
