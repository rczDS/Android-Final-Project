from django.shortcuts import render
from django.http import JsonResponse
import json
import os
from pathlib import Path
from time import time
from django import forms
from random import randint
from copy import deepcopy
import re
import hashlib
BASE_DIR = Path(__file__).resolve().parent.parent
STATIC_ROOT = os.path.join(BASE_DIR, 'static_files')


ACTIVE_LOCAL_DIR = os.path.join(STATIC_ROOT, "active").replace("\\", '/')
ACTIVE_NET_RELATIVE_URL = "static/active"

USER_LOCAL_DIR = os.path.join(STATIC_ROOT,'user').replace('\\','/')
USER_NET_RELATIVE_URL = "static/user"

# ===================用户 相关代码 开始===================================
#用户名和密码对应字典
# clients_dict = {"gmx19":123456}

USER_FILE_PATH = "user.json"
ACTIVE_FILE_PATH = "active.json"
# Create your views here.
#获取用户名密码字典 test
def get_clients(request):
    if request.method == 'GET':
        return JsonResponse(clients_dict)

def index(request):
    if request.method == 'GET':
        return JsonResponse(clients_dict)

# 创建新用户
def create_user(request):
    users_dict = get_all_user_dict()
    # print(dir(request))
    print(request.content_params)
    # print ("request.body", request.body)
    # print(dir(request))
    json_data = json.loads(request.body)
    user_email = json_data["user_email"]
    password = json_data["password"]
    user_name = json_data.get("user_name", "default user name")
    ret_dict = {"result": -1}
    # 用户已经注册过了
    if user_email in users_dict:
        return JsonResponse(ret_dict)
    ret_dict["result"] = 0
    # 用邮箱, 密码创建一个新用户
    new_user = create_default_user(user_email, password)
    new_user["user_name"] = user_name
    # 用 用户的email 作为key 保存用户
    users_dict[user_email] = new_user
    save_users(users_dict)
    print("save users ok")
    return JsonResponse(ret_dict)


# 用户登录
def login_user(request):
    # 返回值 -1 是密码错误
    # 0 是ok
    # -2 是用户不存在
    users_dict = get_all_user_dict()
    # print(dir(request))
    print ("request.body", request.body)
    json_data = json.loads(request.body)
    user_email = json_data["user_email"]
    password = json_data["password"]
    ret_dict = {"result": -1}
    if user_email not in users_dict:
        ret_dict["result"] = -2
    elif users_dict[user_email]["password"] == password:
         # 初始化 草稿
        if "draft_dict" not in users_dict[user_email]:
            users_dict[user_email]["draft_dict"] = {}
            save_users(users_dict)
        ret_dict["result"] = 0
        ret_dict["user_info"] = users_dict[user_email]
   
    return JsonResponse(ret_dict)

# 更新用户信息
def update_user(request):
    photo_form = forms.Form(request.POST, request.FILES)
    json_data = {}
    print(request.FILES)
    if photo_form.is_valid() and "json_data" in request.FILES:
        json_data = json.loads(request.FILES["json_data"].read())
        print("json data from form", json_data)
    else:
        json_data = json.loads(request.body)
    user_email = json_data["user_email"]
    users_dict = get_all_user_dict()
    ret_dict = {"result": -1}
    if user_email not in users_dict:
        return JsonResponse(ret_dict)

    cnt_user_dict = users_dict[user_email]
    # 改username
    cnt_user_dict["user_name"] = json_data["user_name"]
    cnt_user_dict["desc"] = json_data["desc"]
    if "photo" in json_data:
        photo_url = handle_user_photo_upload(request.FILES['upload_file'], json_data["photo"])
        cnt_user_dict["photo"] = photo_url
    if "password" in json_data:
        cnt_user_dict["password"] = json_data["password"]
    save_users(users_dict)
    ret_dict["result"] = 0
    ret_dict["user_info"] = cnt_user_dict
    return JsonResponse(ret_dict)

# 关注用户
def follow_user(request):
    users_dict = get_all_user_dict()
    json_data = json.loads(request.body)
    from_email = json_data["from_email"]
    to_email = json_data["to_email"]
    result_dict = {"result": -1}
    if (from_email not in users_dict) or (to_email not in users_dict):
        return JsonResponse(result_dict)
    from_user = users_dict[from_email]
    to_user = users_dict[to_email]
    if to_email not in from_user["outlinks"]:
        from_user["outlinks"].append(to_email)
    if from_email not in to_user["inlinks"]:
        to_user["inlinks"].append(from_email)
    save_users(users_dict)
    result_dict["result"] = 0
    result_dict["user_info"] = users_dict[from_email]
    return JsonResponse(result_dict)

# 取关用户
def unfollow_user(request):
    users_dict = get_all_user_dict()
    json_data = json.loads(request.body)
    from_email = json_data["from_email"]
    to_email = json_data["to_email"]
    result_dict = {"result": -1}
    if (from_email not in users_dict) or (to_email not in users_dict):
        return JsonResponse(result_dict)
    from_user = users_dict[from_email]
    to_user = users_dict[to_email]
    if to_email in from_user["outlinks"]:
        outset = set(from_user["outlinks"])
        outset.remove(to_email)
        from_user["outlinks"] = list(outset)
    if from_email in from_user["inlinks"]:
        inset = set(to_user["inlinks"])
        inset.remove(from_email)
        from_user["inlinks"] = list(inset)
    save_users(users_dict)
    result_dict["result"] = 0
    result_dict["user_info"] = users_dict[from_email]
    return JsonResponse(result_dict)

# 屏蔽用户
def blacklist_user(request):
    users_dict = get_all_user_dict()
    json_data = json.loads(request.body)
    from_email = json_data["from_email"]
    to_email = json_data["to_email"]
    result_dict = {"result": -1}
    if (from_email not in users_dict) or (to_email not in users_dict):
        return JsonResponse(result_dict)
    from_user = users_dict[from_email]
    if to_email not in from_user["black_list"]:
        from_user["black_list"].append(to_email)
    save_users(users_dict)
    result_dict["result"] = 0
    result_dict["user_info"] = users_dict[from_email]
    return JsonResponse(result_dict)

# 解屏蔽用户
def unblacklist_user(request):
    users_dict = get_all_user_dict()
    json_data = json.loads(request.body)
    from_email = json_data["from_email"]
    to_email = json_data["to_email"]
    print ("unblack {} {}".format(from_email, to_email))
    result_dict = {"result": -1}
    if (from_email not in users_dict) or (to_email not in users_dict):
        return JsonResponse(result_dict)
    from_user = users_dict[from_email]
    if to_email in from_user["black_list"]:
        black_set = set(from_user["black_list"])
        black_set.remove(to_email)
        from_user["black_list"] = list(black_set)
    save_users(users_dict)
    result_dict["result"] = 0
    result_dict["user_info"] = users_dict[from_email]
    return JsonResponse(result_dict)


def handle_user_photo_upload(photo_file, photo_name):
    now_str = str(int(time()))
    subfix = os.path.splitext(photo_name)[-1]
    photo_filename = now_str + subfix
    local_path = get_user_photo_local_path(photo_filename)
    with open(local_path, 'wb') as destination:
        for chunk in photo_file.chunks():
            destination.write(chunk)
    final_filename = rename_to_hash_file(local_path)
    print("file size of ", final_filename, "is", os.path.getsize(get_user_photo_local_path(final_filename)))
    photo_url = get_user_photo_net_url(final_filename)
    return photo_url

# 创建用户并用
def create_default_user(user_email, password):
    new_user = {}
    new_user["user_email"] = user_email
    new_user["password"] = password
    # 默认的用户名
    new_user["user_name"] = "new user_name"
    # 关注列表
    new_user["outlinks"] = []
    # 被关注列表
    new_user["inlinks"] = []
    # 屏蔽用户列表
    new_user["black_list"] = []
    # 简介
    new_user["desc"] = "default desc"
    # 照片
    new_user["photo"] = get_user_photo_net_url("default.png")
    # 用于记录通知的 信息
    new_user["messages"] = []
    # 未读信息列表
    new_user["unread_messages"] = False
    return new_user

# 网络url, 返回给前端使用的
def get_user_photo_net_url(photo_name):
    return os.path.join(USER_NET_RELATIVE_URL, photo_name).replace('\\', '/')


# 本地保存的位置
def get_user_photo_local_path(photo_name):
    return os.path.join(USER_LOCAL_DIR, photo_name).replace('\\', '/')

# 辅助函数, 获取用户信息的内容
def get_all_user_dict():
    user_dict = {}
    if os.path.exists(USER_FILE_PATH):
        user_dict = json.load(open(USER_FILE_PATH, "r"))
    return user_dict

# 保存用户数据到文件中

def save_users(users_dict):
    writer = open(USER_FILE_PATH, 'w')
    # print("user_dict", users_dict)
    json.dump(users_dict, writer, indent=4)
    writer.close()

# ===================用户 相关代码 结束===================================

# ===================动态 相关代码 开始===================================
# 类型 0 文字 1 图片 2 视频 3 声音
def  post_active(request):
    actives_dict = get_all_active_dict()
    users_dict = get_all_user_dict()
    post_form = forms.Form(request.POST, request.FILES)
    json_data = {}
    if post_form.is_valid() and "json_data" in request.FILES:
        json_str = request.FILES["json_data"].read()
        json_data = json.loads(json_str)
    else:
        json_data = json.loads(request.body)
    poster_email = json_data["poster_email"]
    title = json_data["title"]
    active_text = json_data["active_text"]
    active_type = json_data["active_type"]
    active_position = json_data["active_position"]
    draft_id = json_data["draft_id"]
    file_url = ""
    if active_type in (1, 2, 3):
        active_local_file_name = json_data["active_file_name"]
        subfix = os.path.splitext(active_local_file_name)[-1]
        print ("active_type", active_type, "subfix", subfix)
        '''
        if active_type == 1:
            assert(subfix == ".jpg")
        elif active_type == 2:
            assert(subfix == ".mp4")
        elif active_type == 3:
            assert(subfix == ".aac")
        '''
        file_url = handle_active_files(request.FILES["upload_file"], subfix)
    active_id = str(randint(0, 1000000000000000))
    # 保证不重复
    while active_id in actives_dict:
        active_id = str(randint())
    reply_list = []
    thumbup_list = []
    new_active_dict = {
        "poster_email": poster_email,
        "title": title,
        "active_text": active_text,
        "active_type": active_type,
        "file_url": file_url,
        "active_id": active_id,
        "reply_list": reply_list,
        "thumbup_list": thumbup_list,
        "active_timestamp": time(),
        "active_position": active_position,
    }
    actives_dict[active_id] = new_active_dict
    update_poster_follower_message(users_dict, poster_email)
    # // 删掉已发送的草稿
    poster_dict = users_dict[poster_email]
    if draft_id in poster_dict["draft_dict"]:
        del poster_dict["draft_dict"][draft_id]
    result_dict = {"result": 0}
    save_actives(actives_dict)
    save_users(users_dict)
    result_dict["user_info"] = poster_dict
    return JsonResponse(result_dict)

# 点赞
def like_active(request):
    actives_dict = get_all_active_dict()
    users_dict = get_all_user_dict()
    json_data = json.loads(request.body)
    active_id = json_data["active_id"]
    from_email = json_data["from_email"]
    result_dict = {"result": -1}
    if active_id not in actives_dict:
        return JsonResponse(result_dict)
    if from_email not in users_dict:
        return JsonResponse(result_dict)
    cnt_active = actives_dict[active_id]
    if from_email in cnt_active["thumbup_list"]:
        #点赞过了
        return JsonResponse(result_dict)
    cnt_active["thumbup_list"].append(from_email)
    add_like_message_to_user(cnt_active, from_email, users_dict)
    save_actives(actives_dict)
    save_users(users_dict)
    result_dict["result"] = 0
    result_dict["active"] = get_showable_active_data(cnt_active, users_dict)
    return JsonResponse(result_dict)

# 取消赞
def dislike_active(request):
    actives_dict = get_all_active_dict()
    users_dict = get_all_user_dict()
    json_data = json.loads(request.body)
    active_id = json_data["active_id"]
    from_email = json_data["from_email"]
    result_dict = {"result": -1}
    if active_id not in actives_dict:
        return JsonResponse(result_dict)
    if from_email not in users_dict:
        return JsonResponse(result_dict)
    cnt_active = actives_dict[active_id]
    if from_email not in cnt_active["thumbup_list"]:
        #没有点赞过
        return JsonResponse(result_dict)
    cnt_active["thumbup_list"].remove(from_email)
    save_actives(actives_dict)
    result_dict["result"] = 0
    result_dict["active"] = get_showable_active_data(cnt_active, users_dict)
    return JsonResponse(result_dict)



# 通知被点赞的结果
def add_like_message_to_user(cnt_active, from_email, users_dict):
    poster_email = cnt_active["poster_email"]
    from_user_name = users_dict[from_email]["user_name"]
    poster_dict = users_dict[poster_email]
    cnt_active_title = cnt_active["title"]
    if "messages" not in poster_dict:
        poster_dict["messages"] = []
    poster_dict["unread_messages"] = True
    message = "your activity {0} is liked by {1}".format(cnt_active_title, from_user_name)
    poster_dict["messages"].append(message)


def get_showable_active_data(cnt_active, users_dict):
    # 拷贝出来进行修改, 不动元数据
    ret_active = deepcopy(cnt_active)
    ret_active["thumbup_list"] = get_showable_liked_data(cnt_active, users_dict)
    ret_active["reply_list"] =  get_showable_reply_data(cnt_active, users_dict)
    ret_active["active_user"] = users_dict[cnt_active["poster_email"]]
    return ret_active

# 获取可给与前端进行展示的回复数据。
def get_showable_reply_data(cnt_active, users_dict):
    ret_reply_list = []
    for reply_data in cnt_active["reply_list"]:
        reply_id = reply_data["reply_id"]
        from_email = reply_data["from_email"]
        from_user_dict = users_dict[from_email]
        from_user_name = from_user_dict["user_name"]
        from_user_photo = from_user_dict["photo"]
        new_data = {
            "reply_id": reply_id,
            "from_email": from_email,
            "user_name": from_user_name,
            "photo": from_user_photo,
            "reply_text": reply_data["reply_text"]
        }
        ret_reply_list.append(new_data)
    return ret_reply_list

def get_showable_liked_data(cnt_active, users_dict):
    ret_liked_list = []
    for liked_email in cnt_active["thumbup_list"]:
        from_user_dict = users_dict[liked_email]
        from_user_name = from_user_dict["user_name"]
        from_user_photo = from_user_dict["photo"]
        new_data = {
            "from_email": liked_email,
            "user_name": from_user_name,
            "photo": from_user_photo
        }
        ret_liked_list.append(new_data)
    return ret_liked_list

# 回复
def reply_active(request):
    actives_dict = get_all_active_dict()
    users_dict = get_all_user_dict()
    json_data = json.loads(request.body)
    active_id = json_data["active_id"]
    from_email = json_data["from_email"]
    reply_text = json_data["reply_text"]
    reply_id = str(int(time()))
    new_reply = {
        "reply_id": reply_id,
        "from_email": from_email,
        "reply_text": reply_text
    }
    cnt_active = actives_dict[active_id]
    cnt_active["reply_list"].append(new_reply)
    add_reply_message_to_user(users_dict, from_email, cnt_active)
    save_actives(actives_dict)
    save_users(users_dict)
    result_dict = {}
    result_dict["result"] = 0
    result_dict["active"] = get_showable_active_data(cnt_active, users_dict)
    result_dict["reply_id"] = reply_id
    return JsonResponse(result_dict)

# 撤销回复
def withdraw_reply(request):
    actives_dict = get_all_active_dict()
    users_dict = get_all_user_dict()
    json_data = json.loads(request.body)
    active_id = json_data["active_id"]
    from_email = json_data["from_email"]
    reply_id = json_data["reply_id"]
    cnt_active = actives_dict[active_id]
    reply_index = None
    for idx, reply_data in enumerate(cnt_active["reply_list"]):
        if reply_data["reply_id"] == reply_id:
            reply_index = idx
            break
    result_dict = {"result": -1}
    if reply_index is None:
        return JsonResponse(result_dict)
    cnt_active["reply_list"].pop(reply_index)
    save_actives(actives_dict)
    
    result_dict["result"] = 0
    result_dict["active"] = get_showable_active_data(cnt_active, users_dict)
    return JsonResponse(result_dict)




def add_reply_message_to_user(users_dict, from_email, cnt_active):
    poster_email = cnt_active["poster_email"]
    from_user_name = users_dict[from_email]["user_name"]
    poster_dict = users_dict[poster_email]
    cnt_active_title = cnt_active["title"]
    if "messages" not in poster_dict:
        poster_dict["messages"] = []
    poster_dict["unread_messages"] = True
    message = "your activity {0} is reply by {1}".format(cnt_active_title, from_user_name)
    poster_dict["messages"].append(message)



def save_actives(actives_dict):
    writer = open(ACTIVE_FILE_PATH, 'w')
    json.dump(actives_dict, writer, indent=4)
    writer.close()


def add_post_message_to_user(user_dict, poster_user_name):
    message = "your friend {0} post a new active".format(poster_user_name)
    if "messages" not in user_dict:
        user_dict["messages"] = []
    if "unread_messages" not in user_dict:
        user_dict["unread_messages"] = False
    user_dict["messages"].append(message)
    user_dict["unread_messages"] = True

# 更新关注发布者的人的未读信息
def update_poster_follower_message(users_dict, poster_email):
    poster_dict = users_dict[poster_email]
    poster_user_name = poster_dict["user_name"]
    inlinks = poster_dict['inlinks']
    for follower_email in inlinks:
        follower_dict = users_dict[follower_email]
        add_post_message_to_user(follower_dict, poster_user_name)



# 网络url, 返回给前端使用的
def get_active_file_net_url(active_filename):
    return os.path.join(ACTIVE_NET_RELATIVE_URL, active_filename).replace('\\', '/')


# 本地保存的位置
def get_active_file_local_path(active_filename):
    return os.path.join(ACTIVE_LOCAL_DIR, active_filename).replace('\\', '/')

def handle_active_files(active_file, subfix):
    now_str = str(int(time()))
    active_filename = now_str + subfix
    local_path = get_active_file_local_path(active_filename)
    
    with open(local_path, 'wb') as destination:
        for chunk in active_file.chunks():
            destination.write(chunk)
    final_filename = rename_to_hash_file(local_path)
    active_url = get_active_file_net_url(final_filename)
    return active_url
# 获取
def get_all_active_dict():
    actives_dict = {}
    if os.path.exists(ACTIVE_FILE_PATH):
        actives_dict = json.load(open(ACTIVE_FILE_PATH, "r"))
    return actives_dict

# 匹配带模糊
def match_str(pattern, string):
    pattern_str_list = []
    for cnt_str in pattern:
        cnt_pattern = cnt_str + ".*"
        pattern_str_list.append(cnt_pattern)
    match_pattern = "".join(pattern_str_list)
    regex = re.compile(match_pattern)
    match = regex.search(string)
    if match:
        return True
    return False


def get_active_user_name(cnt_active, users_dict):
    poster_email = cnt_active["poster_email"]
    if poster_email in users_dict:
        user_name = users_dict[poster_email]["user_name"]
        return user_name
    return ""

# 搜索
def filter_active(cnt_active, title_pattern, text_pattern, user_name_pattern, active_type, active_poster_user_name):
    if active_type != -1:
        if cnt_active["active_type"] != active_type:
            return False
    title_pattern = title_pattern.strip()
    text_pattern = text_pattern.strip()
    user_name_pattern = user_name_pattern.strip()
    if len(title_pattern) > 0:
        if not match_str(title_pattern, cnt_active["title"]):
            return False
    if len(text_pattern) > 0:
        if not match_str(text_pattern, cnt_active["active_text"]):
            return False
    if len(user_name_pattern) > 0:
        if not match_str(user_name_pattern, active_poster_user_name):
            return False
    return True

def rename_to_hash_file(in_file_path):
    filename = os.path.basename(in_file_path)
    subfix = os.path.splitext(filename)[-1]
    file_dir = os.path.dirname(in_file_path)
    original_file = open(in_file_path, "rb")
    file_data = original_file.read()
    original_file.close()
    md5_hash = hashlib.md5(file_data).hexdigest()
    final_filename = md5_hash + subfix
    print("final_filename", final_filename, "file_dir", file_dir, "subfix", subfix)
    final_path = os.path.join(file_dir, final_filename)
    if not os.path.exists(final_path):
        open(final_path, "wb").write(file_data)
    os.remove(in_file_path)
    return final_filename

# ===================动态 相关代码 结束===================================

# ===================app 访问功能 相关代码 开始===================================



# 进入个人信息的页面的请求
def get_personal_info(request):
    users_dict = get_all_user_dict()
    actives_dict = get_all_active_dict()
    json_data = json.loads(request.body)
    user_email = json_data["user_email"]
    result_dict = {"result": -1}
    if user_email not in users_dict:
        return JsonResponse(result_dict)

    result_dict["result"] = 0
    personal_data = deepcopy(users_dict[user_email])
    outlinks = []
    for outlink_email in personal_data["outlinks"]:
        outlinks.append(deepcopy(users_dict[outlink_email]))
    # // 单独拉出关注数据, 因为可能要通过点击来跳转
    result_dict["outlinks"] = outlinks
    # 单独的自己发布的数据
    actives_list = []
    result_dict["user_info"] = personal_data;
    for active_id, cnt_active in actives_dict.items():
        if cnt_active["poster_email"] != user_email:
            continue
        cnt_to_show_active = get_showable_active_data(cnt_active, users_dict)
        actives_list.append(cnt_to_show_active)
    # 按照时间倒叙来拍所有的发布
    actives_list.sort(key= lambda x: x["active_timestamp"], reverse=True)
    result_dict["actives_list"] = actives_list

    return JsonResponse(result_dict) 

def is_user_follow(user_dict, email):
    return email in user_dict["outlinks"]

def is_user_blacked(user_dict, email):
    return email in user_dict["black_list"]

def sort_active_key_time(active):
    return active["active_timestamp"]

def sort_active_key_liked(active):
    return len(active["thumbup_list"])

# filter type -1 0, 1, 2, 3 对应 全部/文本/图文/视频/音频
# sort type 0 时间 , 1 点赞
# poster_type 0 全部 1 关注的人
def show_all_active(request):
    users_dict = get_all_user_dict()
    actives_dict = get_all_active_dict()
    json_data = json.loads(request.body)
    user_email = json_data["user_email"]
    cnt_user_dict = users_dict[user_email]
    active_type = json_data["active_type"]
    active_title_pattern = json_data["title"]
    active_user_pattern = json_data["poster_name"]
    actitve_text_pattern = json_data["active_text"]
    active_sort_type = json_data["sort_type"]
    active_poster_type = json_data["poster_type"]
    result_dict = {"result": 0}
    actives_list = []
    for k, active in actives_dict.items():
        poster_email = active["poster_email"]
        if is_user_blacked(cnt_user_dict, poster_email):
            continue
        if active_poster_type == 1:
            if not is_user_follow(cnt_user_dict, poster_email):
                continue
        active_poster_user_name = get_active_user_name(active, users_dict)
        if filter_active(active, active_title_pattern, actitve_text_pattern, active_user_pattern, active_type, active_poster_user_name):
            actives_list.append(get_showable_active_data(active, users_dict))
    if active_sort_type == 0:
        actives_list.sort(key=sort_active_key_time, reverse=True)
    else:
        actives_list.sort(key=sort_active_key_liked, reverse=True)
    result_dict["actives_list"] = actives_list
    return JsonResponse(result_dict) 


# 保存草稿
def save_draft(request):
    users_dict = get_all_user_dict()
    json_data = json.loads(request.body)
    user_email = json_data["user_email"]
    active_type = json_data["active_type"]
    active_text = json_data["active_text"]
    draft_id = json_data["draft_id"]
    title = json_data["title"]
    draft_dict = {
        "active_type": active_type,
        "active_text": active_text,
        "draft_id": draft_id,
        "title": title
    }
    users_dict[user_email]["draft_dict"][draft_id] = draft_dict
    save_users(users_dict)
    result_dict = {"result": 0, "user_info": users_dict[user_email]}
    return JsonResponse(result_dict) 

# 触发新消息发送
def touch_new_message(request):
    users_dict = get_all_user_dict()
    json_data = json.loads(request.body)
    user_email = json_data["user_email"]
    cnt_user_dict = users_dict[user_email]
    result_dict = {"result": -1}
    if cnt_user_dict["unread_messages"]:
        cnt_user_dict["unread_messages"] = False
        result_dict["result"] = 0
        result_dict["user_info"] = cnt_user_dict
        save_users(users_dict)
    return JsonResponse(result_dict) 


# ===================app 访问功能 相关代码 结束===================================

if __name__ == '__main__':
    string = "ABCDEFGA"
    print(match_str("AC", string))
    print(match_str("ABD", string))
    print(match_str("CEG", string))
    print(match_str("BAB", string))
    print(match_str("BG", string))
    print(match_str("B", string))
    print(match_str("A", string))
    print(match_str("EFG", string))