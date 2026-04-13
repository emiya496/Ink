from __future__ import annotations

import pytest

from utils.assertions import assert_business_ok
from utils.data_factory import make_text


@pytest.mark.api
def test_user_register_login_info_profile_and_search(user_factory):
    user = user_factory(prefix="user_flow")

    login_payload = assert_business_ok(user.api.user.login(user.username, user.password))
    assert login_payload["data"]["username"] == user.username

    info_payload = assert_business_ok(user.api.user.info())
    assert info_payload["data"]["username"] == user.username

    profile_payload = assert_business_ok(user.api.user.profile(user.id))
    assert profile_payload["data"]["username"] == user.username

    search_payload = assert_business_ok(user.api.user.search(keyword=user.username))
    assert any(item["username"] == user.username for item in search_payload["data"]["list"])


@pytest.mark.api
def test_user_update_info_bio_username_and_password(user_factory):
    user = user_factory(prefix="user_update")

    assert_business_ok(user.api.user.update_info("http://localhost:9090/uploads/demo.png"))
    assert_business_ok(user.api.user.update_bio("Pytest bio"))

    renamed = f"{user.username}_renamed"
    assert_business_ok(user.api.user.update_username(renamed))
    user.username = renamed

    new_password = "Pytest456!"
    assert_business_ok(user.api.user.change_password(user.password, new_password))
    user.password = new_password

    relogin_payload = assert_business_ok(user.api.user.login(user.username, user.password))
    assert relogin_payload["data"]["username"] == user.username


@pytest.mark.api
def test_user_email_send_code_and_password_reset_send_code(session_user, config):
    if not config.email_bind_target:
        pytest.skip("Set INKFORGE_BIND_EMAIL to run email send-code tests.")

    assert_business_ok(session_user.api.user.send_email_code(config.email_bind_target, "bind"))

    reset_target = config.email_reset_target or config.email_bind_target
    assert_business_ok(session_user.api.user.send_password_reset_code(reset_target))


@pytest.mark.api
def test_user_bind_change_reset_and_delete_account_optional(user_factory, config):
    user = user_factory(prefix="user_email")

    if not (config.email_bind_target and config.email_bind_code):
        pytest.skip("Set INKFORGE_BIND_EMAIL and INKFORGE_BIND_EMAIL_CODE to run bind-email test.")
    assert_business_ok(user.api.user.bind_email(config.email_bind_target, config.email_bind_code))

    if config.email_change_target and config.email_change_code:
        assert_business_ok(user.api.user.change_email(config.email_change_target, config.email_change_code))

    if config.email_reset_target and config.email_reset_code:
        new_password = "ResetPass123!"
        assert_business_ok(
            user.api.user.reset_password(config.email_reset_target, config.email_reset_code, new_password)
        )
        user.password = new_password
        assert_business_ok(user.api.user.login(user.username, user.password))

    if config.email_delete_code:
        assert_business_ok(user.api.user.send_delete_code())
        delete_payload = assert_business_ok(user.api.user.delete_account(user.password, config.email_delete_code))
        assert delete_payload["data"] is None


@pytest.mark.api
def test_user_search_returns_paginated_structure(session_user):
    keyword = session_user.username.split("_")[0]
    payload = assert_business_ok(session_user.api.user.search(keyword=keyword, page=1, size=5))
    assert "total" in payload["data"]
    assert "list" in payload["data"]
    assert payload["data"]["page"] == 1
