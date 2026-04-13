from __future__ import annotations

import pytest

from utils.assertions import assert_business_ok
from utils.data_factory import make_text


@pytest.mark.api
def test_comment_add_list_and_delete(session_user, article_content, comment_factory):
    comment_text = make_text("comment")
    comment_id = comment_factory(session_user, article_content, comment_text)

    list_payload = assert_business_ok(session_user.api.comment.list(article_content))
    assert any(item["id"] == comment_id for item in list_payload["data"]["list"])

    assert_business_ok(session_user.api.comment.delete(comment_id))
