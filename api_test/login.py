# import requests
# import pytest #type: ignore
#
# #定义接口地址
# url = "http://47.108.217.148/api/user/login"
#
# @pytest.mark.parametrize(
#     "username, password, expected_code",
#     [
#         ("admin", "admin123", 200),
#         ("admin", "admin12", 400),
#         ("admi", "admin12", 400),
#         ("", "admin12", 400),
#         ("admin", "", 400),
#     ]
# )
#
# def test_login(username, password, expected_code):
#     #定义请求体
#     payload =  {
#         "username" : username,
#         "password" : password
#     }
#     #发送请求
#     response = requests.post(url, json = payload)
#
#     print("状态码", response.status_code)
#     print("响应体", response.text)
#     #检查
#     if username == "" or password == "":
#         assert response.status_code == 400
#     else:
#         assert response.status_code == 200
#         result = response.json()
#         if result["code"] == 200:
#             assert result["code"] == expected_code
#             assert "data" in result
#             assert "token" in result["data"]
#             assert result["data"]["token"] != ""
#             assert result["data"]["username"] == "admin", "登录用户名不是admin1"
#         else:
#             assert result["code"] == 400