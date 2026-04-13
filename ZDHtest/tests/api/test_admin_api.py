from __future__ import annotations

import pytest

from utils.assertions import assert_business_ok
from utils.data_factory import article_payload, make_tag_name, make_text


@pytest.mark.api
def test_admin_user_endpoints(admin_bundle, user_factory):
    temp_user = user_factory(prefix="admin_user_target")

    list_payload = assert_business_ok(admin_bundle.admin.list_users(keyword=temp_user.username))
    assert any(item["id"] == temp_user.id for item in list_payload["data"]["list"])

    assert_business_ok(admin_bundle.admin.update_user_status(temp_user.id, "禁用"))
    assert_business_ok(admin_bundle.admin.update_user_status(temp_user.id, "正常"))

    delete_target = user_factory(prefix="admin_delete_target")
    delete_payload = assert_business_ok(admin_bundle.admin.delete_user(delete_target.id))
    assert delete_payload["data"] is None


@pytest.mark.api
def test_admin_content_endpoints(admin_bundle, session_user, content_factory):
    title = make_text("admin_content")
    content_id = content_factory(session_user, article_payload(title, "admin content body"))

    list_payload = assert_business_ok(admin_bundle.admin.list_contents(keyword=title))
    assert any(item["id"] == content_id for item in list_payload["data"]["list"])

    assert_business_ok(admin_bundle.admin.update_content_status(content_id, "下架"))
    assert_business_ok(admin_bundle.admin.update_content_status(content_id, "正常"))

    delete_title = make_text("admin_delete_content")
    delete_content_id = content_factory(session_user, article_payload(delete_title, "delete me by admin"))
    delete_payload = assert_business_ok(admin_bundle.admin.delete_content(delete_content_id))
    assert delete_payload["data"] is None


@pytest.mark.api
def test_admin_tag_endpoints(admin_bundle):
    list_payload = assert_business_ok(admin_bundle.admin.list_tags())
    assert "list" in list_payload["data"]

    unique_tag = make_tag_name("system_tag")
    create_payload = assert_business_ok(admin_bundle.admin.create_tag(unique_tag))
    assert create_payload["data"] is None

    refreshed_payload = assert_business_ok(admin_bundle.admin.list_tags(keyword=unique_tag))
    created_tag = next(item for item in refreshed_payload["data"]["list"] if item["tagName"] == unique_tag)
    tag_id = int(created_tag["id"])

    try:
        assert_business_ok(admin_bundle.admin.update_tag_status(tag_id, "禁用"))
        assert_business_ok(admin_bundle.admin.update_tag_status(tag_id, "正常"))
    finally:
        assert_business_ok(admin_bundle.admin.delete_tag(tag_id))


@pytest.mark.api
def test_admin_comment_and_ai_stats_endpoints(admin_bundle, session_user, article_content, comment_factory):
    comment_text = make_text("admin_comment")
    comment_id = comment_factory(session_user, article_content, comment_text)

    comments_payload = assert_business_ok(admin_bundle.admin.list_comments(keyword=comment_text))
    assert any(item["id"] == comment_id for item in comments_payload["data"]["list"])

    assert_business_ok(admin_bundle.admin.delete_comment(comment_id))

    stats_payload = assert_business_ok(admin_bundle.admin.ai_stats())
    assert "total" in stats_payload["data"]
