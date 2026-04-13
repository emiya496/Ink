import requests

#@pytest.mark.parametrize(
#    "xxx,xx,x",
#    [
#        ("x", "x", 1),
#        ("x", "x", 1),
#        ("x", "x", 1),
#        ("x", "x", 1)
#    ]
#)

# def test_user_login():
#
#
#     #url
#     login_url = "http://47.108.217.148/api/user/login"
#
#     payload={
#         "username": "admin",
#         "password": "admin123"
#     }
#     #请求
#     response = requests.post(login_url,json = payload)
#     assert response.status_code == 200
#     result_json = response.json()
#
#     assert "data" in result_json
#
#     assert result_json["data"],"data为空"
#
#     assert result_json["data"]["token"],"token为空"
#
#     login_token = result_json["data"]["token"]
#
#     #print("token:", login_token)


def test_user_info(base_url,login_session):

    # url
    url = f"{base_url}/api/user/info"

    # payheader = {
    #     "Authorization": f"Bearer {login_token}"
    # }

    u_response = login_session.get(url)

    assert u_response.status_code == 200

    u_result = u_response.json()

    print("响应体：", u_result)

    assert u_result["code"] == 200

    assert "data" in u_result

    assert u_result["data"]["id"] == 1

def test_content_list(base_url):

    url = f"{base_url}/api/content/list"

    params = {
        "page": "",
        "size": "",
        "type": "",
        "keyword": "",
        "sortBy": "",
        "tagId": ""
    }

    cl_response = requests.get(url, params=params)

    assert cl_response.status_code == 200

    cl_request = cl_response.json()

    assert cl_request["message"] == "success"

    assert cl_request["data"]

    assert cl_request["data"]["page"] == 1

    print("响应体", cl_request)



















