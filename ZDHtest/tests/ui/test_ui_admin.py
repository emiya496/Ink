from __future__ import annotations

import pytest

from tests.ui.pages.route_pages import (
    AdminAiStatsPage,
    AdminCommentsPage,
    AdminContentsPage,
    AdminTagsPage,
    AdminUsersPage,
)


@pytest.mark.ui
def test_admin_routes_load(config, driver, admin_bundle):
    token = admin_bundle.client.token or ""

    for page_cls in [AdminUsersPage, AdminContentsPage, AdminTagsPage, AdminCommentsPage, AdminAiStatsPage]:
        page = page_cls(driver, config.ui_base_url, config.timeout_seconds)
        page.open("/")
        page.set_local_storage("inkforge_token", token)
        page.open()
        page.wait_until_ready()
