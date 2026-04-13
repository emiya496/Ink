import pytest
import requests

@pytest.fixture(scope="session")
def session_obj():

    session = requests.session()

    return session

@pytest.fixture(scope="session")
def base_url():

    base_url = "http://47.108.217.148"

    return base_url

@pytest.fixture(scope="session")
def login_data():

    login_data = {
        "username": "admin",
        "password": "admin123"
    }

    return login_data

@pytest.fixture(scope="session")
def login_token(base_url,login_data,session_obj):

    login_url = f"{base_url}/api/user/login"

    response = session_obj.post(login_url,json=login_data)

    assert response.status_code == 200

    result = response.json()

    assert result["code"] == 200

    assert "data" in result,"响应体没有data字段"

    assert "token" in result["data"],"响应体没有token字段"

    assert result["data"]["token"],"token为空"

    return result["data"]["token"]

@pytest.fixture(scope="session")
def login_session(session_obj,login_token):

    session_obj.headers.update({
        "Authorization": f"Bearer {login_token}"
    })

    return session_obj