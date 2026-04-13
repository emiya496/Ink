from __future__ import annotations

import pytest

from utils.assertions import assert_business_ok
from utils.data_factory import article_payload, make_text


@pytest.mark.api
def test_content_public_endpoints(session_user, article_content):
    list_payload = assert_business_ok(session_user.api.content.list(page=1, size=10))
    assert "list" in list_payload["data"]

    rank_payload = assert_business_ok(session_user.api.content.rank(rank_type="reads"))
    assert isinstance(rank_payload["data"], list)

    hot_payload = assert_business_ok(session_user.api.content.hot_banner())
    assert hot_payload["code"] == 200

    detail_payload = assert_business_ok(session_user.api.content.detail(article_content))
    assert detail_payload["data"]["id"] == article_content


@pytest.mark.api
def test_content_create_update_delete_my_my_drafts_userworks_and_like(session_user, content_factory):
    title = make_text("content_public")
    content_id = content_factory(session_user, article_payload(title, "public content body"))

    update_payload = article_payload(f"{title}_updated", "updated body")
    assert_business_ok(session_user.api.content.update(content_id, update_payload))

    detail_payload = assert_business_ok(session_user.api.content.detail(content_id))
    assert detail_payload["data"]["title"] == f"{title}_updated"

    my_payload = assert_business_ok(session_user.api.content.my())
    assert any(item["id"] == content_id for item in my_payload["data"]["list"])

    user_works_payload = assert_business_ok(session_user.api.content.user_works(session_user.id))
    assert any(item["id"] == content_id for item in user_works_payload["data"]["list"])

    like_payload = assert_business_ok(session_user.api.content.like(content_id))
    assert like_payload["data"] is True

    my_likes_payload = assert_business_ok(session_user.api.content.my_likes())
    assert any(item["id"] == content_id for item in my_likes_payload["data"]["list"])

    unlike_payload = assert_business_ok(session_user.api.content.like(content_id))
    assert unlike_payload["data"] is False

    draft_title = make_text("content_draft")
    draft_id = content_factory(session_user, article_payload(draft_title, "draft body", is_draft=True))
    drafts_payload = assert_business_ok(session_user.api.content.my_drafts())
    assert any(item["id"] == draft_id for item in drafts_payload["data"]["list"])

    delete_title = make_text("content_delete")
    delete_id = content_factory(session_user, article_payload(delete_title, "delete body"))
    delete_payload = assert_business_ok(session_user.api.content.delete(delete_id))
    assert delete_payload["data"] is None
