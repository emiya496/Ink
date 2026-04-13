from __future__ import annotations

from typing import Any

import requests


def assert_http_ok(response: requests.Response, expected_status: int = 200) -> dict[str, Any]:
    assert response.status_code == expected_status, response.text
    return response.json()


def assert_business_ok(response: requests.Response, expected_status: int = 200) -> dict[str, Any]:
    payload = assert_http_ok(response, expected_status)
    assert payload["code"] == 200, payload
    return payload


def assert_business_error(response: requests.Response, expected_status: int | None = None) -> dict[str, Any]:
    if expected_status is not None:
        assert response.status_code == expected_status, response.text
    payload = response.json()
    assert payload["code"] != 200, payload
    return payload
