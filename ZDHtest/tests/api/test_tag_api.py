from __future__ import annotations

import pytest

from utils.assertions import assert_business_ok
from utils.data_factory import make_tag_name


@pytest.mark.api
def test_tag_list_create_and_hot(session_user):
    list_payload = assert_business_ok(session_user.api.tag.list())
    assert isinstance(list_payload["data"], list)

    create_payload = assert_business_ok(session_user.api.tag.create(make_tag_name()))
    assert create_payload["data"]["id"] > 0

    hot_payload = assert_business_ok(session_user.api.tag.hot(limit=10))
    assert isinstance(hot_payload["data"], list)
