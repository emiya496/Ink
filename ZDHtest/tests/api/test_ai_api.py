from __future__ import annotations

import pytest

from utils.assertions import assert_business_ok


@pytest.mark.api
def test_ai_endpoints(session_user, config):
    if not config.enable_ai_tests:
        pytest.skip("Set INKFORGE_ENABLE_AI_TESTS=true to run AI endpoint tests.")

    content = "夜色很深，图书馆的灯还亮着。"
    assert assert_business_ok(session_user.api.ai.generate(content))["data"]
    assert assert_business_ok(session_user.api.ai.summary(content))["data"]
    assert assert_business_ok(session_user.api.ai.polish(content))["data"]
    assert assert_business_ok(session_user.api.ai.keywords(content))["data"]
    assert assert_business_ok(session_user.api.ai.sentiment(content))["data"]
    assert assert_business_ok(session_user.api.ai.style(content))["data"]
    assert assert_business_ok(session_user.api.ai.qa(content, "这段文字主要描写了什么氛围？"))["data"]
