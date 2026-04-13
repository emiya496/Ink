import requests
import pytest

def test_login_success():
    #定义接口地址
    url = "http://47.108.217.148/api/user/login"

    #数据驱动
    def test_demo():
        assert 1 == 1
    #定义请求体
    payload =  {
        "username" : "admin",
        "password" : "admin123"
    }
    #发送请求
    response = requests.post(url, json = payload)

    print("状态码", response.status_code)
    print("响应体", response.text)
    #检查
    assert response.status_code == 200
    result = response.json()
    assert result["code"] == 200
    assert "data" in result
    assert "token" in result["data"]
    assert result["data"]["token"] != ""
    assert result["data"]["username"] == "admin", "登录用户名不是admin1"