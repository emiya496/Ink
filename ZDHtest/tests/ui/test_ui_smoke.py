from __future__ import annotations

import pytest
from tests.ui.pages.login_page import LoginPage
from tests.ui.pages.route_pages import (
    BookshelfPage,
    CategoryPage,
    DetailPage,
    HomePage,
    PublishPage,
    UserCenterPage,
    UserProfilePage,
)
from utils.data_factory import article_payload, make_text


@pytest.mark.ui
def test_login_page_can_sign_in(config, driver, session_user):
    page = LoginPage(driver, config.ui_base_url, config.timeout_seconds)
    page.login(session_user.username, session_user.password)

    from selenium.webdriver.support.ui import WebDriverWait

    wait = WebDriverWait(driver, config.timeout_seconds)
    wait.until(lambda d: "/login" not in d.current_url)
    wait.until(lambda d: page.get_local_storage("inkforge_token"))

    assert "/login" not in driver.current_url
    assert page.get_local_storage("inkforge_token")


@pytest.mark.ui
def test_public_routes_load(config, driver, session_user, content_factory):
    content_id = content_factory(session_user, article_payload(make_text("ui_detail"), "detail body"))

    HomePage(driver, config.ui_base_url, config.timeout_seconds).open()
    HomePage(driver, config.ui_base_url, config.timeout_seconds).wait_until_ready()

    CategoryPage(driver, config.ui_base_url, config.timeout_seconds).open()
    CategoryPage(driver, config.ui_base_url, config.timeout_seconds).wait_until_ready()

    detail_page = DetailPage(driver, config.ui_base_url, config.timeout_seconds)
    detail_page.open_for_content(content_id)
    detail_page.wait_until_ready()

    profile_page = UserProfilePage(driver, config.ui_base_url, config.timeout_seconds)
    profile_page.open_for_user(session_user.id)
    profile_page.wait_until_ready()


@pytest.mark.ui
def test_authenticated_user_routes_load(config, driver, session_user, user_factory, content_factory):
    another_user = user_factory(prefix="ui_fav_owner")
    another_content_id = content_factory(another_user, article_payload(make_text("ui_external"), "external body"))
    session_user.api.favorite.add(another_content_id)

    publish_page = PublishPage(driver, config.ui_base_url, config.timeout_seconds)
    publish_page.open("/")
    publish_page.set_local_storage("inkforge_token", session_user.api.client.token or "")
    publish_page.open()
    publish_page.wait_until_ready()

    user_center = UserCenterPage(driver, config.ui_base_url, config.timeout_seconds)
    user_center.open()
    user_center.wait_until_ready()

    bookshelf = BookshelfPage(driver, config.ui_base_url, config.timeout_seconds)
    bookshelf.open()
    bookshelf.wait_until_ready()

    session_user.api.favorite.remove(another_content_id)


@pytest.mark.ui
def test_detail_page_shows_created_content(config, driver, session_user, content_factory):
    title = make_text("ui_title")
    content_id = content_factory(session_user, article_payload(title, "ui article body"))

    page = DetailPage(driver, config.ui_base_url, config.timeout_seconds)
    page.open_for_content(content_id)
    page.wait_until_ready()

    assert page.is_present(".article-title")
