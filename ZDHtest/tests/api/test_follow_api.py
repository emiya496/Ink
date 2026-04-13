from __future__ import annotations

import pytest

from utils.assertions import assert_business_ok


@pytest.mark.api
def test_follow_check_followers_following_unfollow_and_remove_follower(user_factory):
    author = user_factory(prefix="author")
    fan = user_factory(prefix="fan")
    another_fan = user_factory(prefix="another_fan")

    assert_business_ok(fan.api.follow.follow(author.id))
    check_payload = assert_business_ok(fan.api.follow.check(author.id))
    assert check_payload["data"] is True

    followers_payload = assert_business_ok(author.api.follow.followers(author.id))
    assert any(item["id"] == fan.id for item in followers_payload["data"]["list"])

    following_payload = assert_business_ok(fan.api.follow.following(fan.id))
    assert any(item["id"] == author.id for item in following_payload["data"]["list"])

    assert_business_ok(fan.api.follow.unfollow(author.id))

    assert_business_ok(another_fan.api.follow.follow(author.id))
    assert_business_ok(author.api.follow.remove_follower(another_fan.id))
