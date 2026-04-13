from __future__ import annotations

import pytest

from utils.assertions import assert_business_ok


@pytest.mark.api
def test_upload_image(session_user, config):
    if not config.upload_file.exists():
        pytest.skip(f"Upload file not found: {config.upload_file}")

    payload = assert_business_ok(session_user.api.upload.upload_image(config.upload_file))
    assert "/uploads/" in payload["data"]
