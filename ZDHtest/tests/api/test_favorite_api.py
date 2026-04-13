from __future__ import annotations

import pytest

from utils.assertions import assert_business_ok
from utils.data_factory import article_payload, make_text


@pytest.mark.api
def test_favorite_add_check_list_and_remove(session_user, article_content):
    assert_business_ok(session_user.api.favorite.add(article_content))

    check_payload = assert_business_ok(session_user.api.favorite.check(article_content))
    assert check_payload["data"] is True

    list_payload = assert_business_ok(session_user.api.favorite.list())
    assert any(item["id"] == article_content for item in list_payload["data"]["list"])

    assert_business_ok(session_user.api.favorite.remove(article_content))


@pytest.mark.api
def test_favorite_list_hides_inaccessible_content_with_reason(session_user, user_factory, content_factory):
    author = user_factory(prefix="fav_private_author")
    private_content_id = content_factory(
        author,
        article_payload(make_text("private_favorite"), "private body", visibility="private"),
    )

    assert_business_ok(session_user.api.favorite.add(private_content_id))

    try:
        list_payload = assert_business_ok(session_user.api.favorite.list())
        data = list_payload["data"]

        assert data["hiddenTotal"] >= 1
        assert data["hiddenByReason"]["private"] >= 1
        assert all(item["id"] != private_content_id for item in data["list"])
    finally:
        assert_business_ok(session_user.api.favorite.remove(private_content_id))
