# -*- coding: utf-8 -*-
import requests
import json
def create_request(url, myobj, files_param=None):
	if files_param:
		x = requests.post(url, data = myobj, files=files_param)
	else:
		x = requests.post(url, json = myobj)
	print(x.status_code)
	print(x.ok)
	print(x.text)

url = 'http://127.0.0.1:8000/clients/create_user'
myobj = {
	'user_email': '33914164911@qq.com',
	"password": '12331231'
}
create_request(url, myobj)


url = 'http://127.0.0.1:8000/clients/login_user'
myobj = {
	'user_email': '339141649@qq.com',
	"password": '12331231'
}
create_request(url, myobj)

url = 'http://127.0.0.1:8000/clients/login_user'
myobj = {
	'user_email': '3391416491@qq.com',
	"password": '12331231'
}
create_request(url, myobj)

url = 'http://127.0.0.1:8000/clients/login_user'
myobj = {
	'user_email': '339141649@qq.com',
	"password": '123312311111'
}
create_request(url, myobj)

url = 'http://127.0.0.1:8000/clients/update_user'
myobj = {
	'user_email': '339141649@qq.com',
	"password": '123312311111',
	"user_name": 'user1',
	"desc": "user1 desc"
}
create_request(url, myobj)

myobj = {
	'user_email': '339141649@qq.com',
	"user_name": 'user1',
	"desc": "user1 desc222"
}
create_request(url, myobj)

file_path = "test.jpg"
myobj = {
	'user_email': '33914164911@qq.com',
	"password": '12331231111111',
	"user_name": 'user2',
	"desc": "user1 desc",
	"photo": file_path
}

files = {'upload_file': open('test.jpg', 'rb'), "json_data": json.dumps(myobj)}
create_request(url, myobj, files)