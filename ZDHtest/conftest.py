from __future__ import annotations

import os
import time
from dataclasses import dataclass
from pathlib import Path
from typing import Callable, Generator

import pytest
import requests
from selenium import webdriver
from selenium.webdriver.chrome.options import Options as ChromeOptions
from selenium.webdriver.chrome.service import Service as ChromeService
from selenium.webdriver.edge.options import Options as EdgeOptions
from selenium.webdriver.edge.service import Service as EdgeService
from selenium.webdriver.firefox.options import Options as FirefoxOptions
from selenium.webdriver.firefox.service import Service as FirefoxService

from api.admin_api import AdminApi
from api.ai_api import AiApi
from api.chapter_api import ChapterApi
from api.comment_api import CommentApi
from api.content_api import ContentApi
from api.favorite_api import FavoriteApi
from api.follow_api import FollowApi
from api.tag_api import TagApi
from api.upload_api import UploadApi
from api.user_api import UserApi
from core.api_client import ApiClient
from reporting import clear_current_test, reset_runtime, set_current_phase, set_current_test, write_request_trace
from utils.assertions import assert_business_ok
from utils.data_factory import (
    article_payload,
    default_upload_file,
    make_email,
    make_text,
    make_username,
    novel_payload,
)


ROOT_DIR = Path(__file__).resolve().parent


@dataclass(frozen=True)
class TestConfig:
    api_base_url: str
    ui_base_url: str
    admin_username: str
    admin_password: str
    browser: str
    headless: bool
    timeout_seconds: int
    enable_ai_tests: bool
    email_bind_target: str | None
    email_bind_code: str | None
    email_change_target: str | None
    email_change_code: str | None
    email_reset_target: str | None
    email_reset_code: str | None
    email_delete_code: str | None
    upload_file: Path


@dataclass
class ApiBundle:
    client: ApiClient
    user: UserApi
    content: ContentApi
    comment: CommentApi
    chapter: ChapterApi
    favorite: FavoriteApi
    follow: FollowApi
    tag: TagApi
    upload: UploadApi
    ai: AiApi
    admin: AdminApi


@dataclass
class UserContext:
    id: int
    username: str
    password: str
    email: str | None
    api: ApiBundle


def _env_bool(name: str, default: bool) -> bool:
    value = os.getenv(name)
    if value is None:
        return default
    return value.strip().lower() in {"1", "true", "yes", "y", "on"}


def _wait_for_http(url: str, timeout_seconds: int) -> bool:
    deadline = time.monotonic() + timeout_seconds
    while time.monotonic() < deadline:
        try:
            response = requests.get(url, timeout=3)
            if response.ok:
                return True
        except requests.RequestException:
            pass
        time.sleep(1)
    return False


def build_bundle(base_url: str) -> ApiBundle:
    client = ApiClient(base_url=base_url)
    return ApiBundle(
        client=client,
        user=UserApi(client),
        content=ContentApi(client),
        comment=CommentApi(client),
        chapter=ChapterApi(client),
        favorite=FavoriteApi(client),
        follow=FollowApi(client),
        tag=TagApi(client),
        upload=UploadApi(client),
        ai=AiApi(client),
        admin=AdminApi(client),
    )


def _build_driver(config: TestConfig):
    if config.browser == "chrome":
        options = ChromeOptions()
        if config.headless:
            options.add_argument("--headless=new")
        options.add_argument("--window-size=1440,960")
        options.add_argument("--disable-gpu")
        options.add_argument("--no-sandbox")
        return webdriver.Chrome(service=ChromeService(), options=options)

    if config.browser == "edge":
        options = EdgeOptions()
        if config.headless:
            options.add_argument("--headless=new")
        options.add_argument("--window-size=1440,960")
        return webdriver.Edge(service=EdgeService(), options=options)

    if config.browser == "firefox":
        options = FirefoxOptions()
        if config.headless:
            options.add_argument("-headless")
        return webdriver.Firefox(service=FirefoxService(), options=options)

    raise pytest.UsageError(f"Unsupported browser: {config.browser}")


@pytest.fixture(scope="session")
def config() -> TestConfig:
    return TestConfig(
        api_base_url=os.getenv("INKFORGE_API_BASE_URL", "http://localhost:9090").rstrip("/"),
        ui_base_url=os.getenv("INKFORGE_UI_BASE_URL", "http://localhost:5173").rstrip("/"),
        admin_username=os.getenv("INKFORGE_ADMIN_USERNAME", "admin"),
        admin_password=os.getenv("INKFORGE_ADMIN_PASSWORD", "admin123"),
        browser=os.getenv("INKFORGE_BROWSER", "chrome").strip().lower(),
        headless=False,
        timeout_seconds=int(os.getenv("INKFORGE_WAIT_TIMEOUT", "20")),
        enable_ai_tests=True,
        email_bind_target=os.getenv("INKFORGE_BIND_EMAIL"),
        email_bind_code=os.getenv("INKFORGE_BIND_EMAIL_CODE"),
        email_change_target=os.getenv("INKFORGE_CHANGE_EMAIL"),
        email_change_code=os.getenv("INKFORGE_CHANGE_EMAIL_CODE"),
        email_reset_target=os.getenv("INKFORGE_RESET_EMAIL"),
        email_reset_code=os.getenv("INKFORGE_RESET_EMAIL_CODE"),
        email_delete_code=os.getenv("INKFORGE_DELETE_EMAIL_CODE"),
        upload_file=Path(os.getenv("INKFORGE_UPLOAD_FILE", str(default_upload_file(ROOT_DIR)))),
    )


@pytest.fixture(scope="session", autouse=True)
def ensure_services_ready(config: TestConfig) -> None:
    if not _wait_for_http(f"{config.api_base_url}/api/content/list?page=1&size=1", config.timeout_seconds):
        pytest.skip(f"Backend is not reachable: {config.api_base_url}")
    if not _wait_for_http(config.ui_base_url, config.timeout_seconds):
        pytest.skip(f"Frontend is not reachable: {config.ui_base_url}")


def pytest_sessionstart(session: pytest.Session) -> None:
    reset_runtime()


def pytest_sessionfinish(session: pytest.Session, exitstatus: int) -> None:
    output_path = os.getenv("INKFORGE_REQUEST_TRACE_PATH")
    if output_path:
        write_request_trace(Path(output_path))


@pytest.hookimpl(hookwrapper=True)
def pytest_runtest_protocol(item: pytest.Item, nextitem):
    set_current_test(item.nodeid)
    yield
    clear_current_test()


def pytest_runtest_setup(item: pytest.Item) -> None:
    set_current_phase("setup")


def pytest_runtest_call(item: pytest.Item) -> None:
    set_current_phase("call")


def pytest_runtest_teardown(item: pytest.Item, nextitem) -> None:
    set_current_phase("teardown")


@pytest.fixture(scope="session")
def admin_bundle(config: TestConfig) -> Generator[ApiBundle, None, None]:
    bundle = build_bundle(config.api_base_url)
    login_payload = assert_business_ok(bundle.user.login(config.admin_username, config.admin_password))
    assert login_payload["data"]["role"] == "admin"
    yield bundle
    bundle.client.close()


@pytest.fixture(scope="session")
def session_user(config: TestConfig, admin_bundle: ApiBundle) -> Generator[UserContext, None, None]:
    username = make_username("session_user")
    password = "Pytest123!"
    email = make_email("session_user")

    public_bundle = build_bundle(config.api_base_url)
    assert_business_ok(public_bundle.user.register(username, password, email))
    public_bundle.client.close()

    bundle = build_bundle(config.api_base_url)
    login_payload = assert_business_ok(bundle.user.login(username, password))
    user_id = int(login_payload["data"]["id"])
    user = UserContext(id=user_id, username=username, password=password, email=email, api=bundle)
    yield user

    try:
        admin_bundle.admin.delete_user(user.id)
    except Exception:
        pass
    bundle.client.close()


@pytest.fixture()
def user_factory(config: TestConfig, admin_bundle: ApiBundle) -> Generator[Callable[..., UserContext], None, None]:
    created: list[UserContext] = []

    def _create(prefix: str = "pytest_user", password: str = "Pytest123!", email: str | None = None) -> UserContext:
        username = make_username(prefix)
        final_email = email or make_email(prefix)

        public_bundle = build_bundle(config.api_base_url)
        assert_business_ok(public_bundle.user.register(username, password, final_email))
        public_bundle.client.close()

        bundle = build_bundle(config.api_base_url)
        login_payload = assert_business_ok(bundle.user.login(username, password))
        user = UserContext(
            id=int(login_payload["data"]["id"]),
            username=username,
            password=password,
            email=final_email,
            api=bundle,
        )
        created.append(user)
        return user

    yield _create

    for user in reversed(created):
        try:
            admin_bundle.admin.delete_user(user.id)
        except Exception:
            pass
        user.api.client.close()


@pytest.fixture()
def content_factory():
    created: list[tuple[UserContext, int]] = []

    def _create(user: UserContext, payload: dict) -> int:
        response = user.api.content.create(payload)
        content_id = int(assert_business_ok(response)["data"])
        created.append((user, content_id))
        return content_id

    yield _create

    for user, content_id in reversed(created):
        try:
            user.api.content.delete(content_id)
        except Exception:
            pass


@pytest.fixture()
def comment_factory():
    created: list[tuple[UserContext, int]] = []

    def _create(user: UserContext, content_id: int, text: str) -> int:
        assert_business_ok(user.api.comment.add(content_id, text))
        comments_payload = assert_business_ok(user.api.comment.list(content_id))
        for item in comments_payload["data"]["list"]:
            if item["content"] == text and item["userId"] == user.id:
                comment_id = int(item["id"])
                created.append((user, comment_id))
                return comment_id
        raise AssertionError("Created comment was not found in comment list.")

    yield _create

    for user, comment_id in reversed(created):
        try:
            user.api.comment.delete(comment_id)
        except Exception:
            pass


@pytest.fixture()
def novel_content(session_user: UserContext, content_factory) -> int:
    return content_factory(session_user, novel_payload(make_text("novel"), "A pytest novel synopsis."))


@pytest.fixture()
def article_content(session_user: UserContext, content_factory) -> int:
    return content_factory(session_user, article_payload(make_text("article"), "A pytest article body."))


@pytest.fixture()
def driver(config: TestConfig, request: pytest.FixtureRequest):
    browser = _build_driver(config)
    browser.implicitly_wait(2)
    request.node._driver = browser
    yield browser
    browser.quit()


@pytest.hookimpl(hookwrapper=True)
def pytest_runtest_makereport(item: pytest.Item, call: pytest.CallInfo):
    outcome = yield
    report = outcome.get_result()
    if report.when != "call" or report.passed:
        return

    driver = getattr(item, "_driver", None)
    if driver is None:
        return

    pytest_html = item.config.pluginmanager.getplugin("html")
    extras_plugin = getattr(pytest_html, "extras", None)
    if extras_plugin is None:
        return

    screenshot_dir = ROOT_DIR / "reports" / "screenshots"
    screenshot_dir.mkdir(parents=True, exist_ok=True)
    screenshot_path = screenshot_dir / f"{item.name}.png"
    driver.save_screenshot(str(screenshot_path))

    extras = getattr(report, "extras", [])
    extras.append(
        extras_plugin.image(
            driver.get_screenshot_as_base64(),
            mime_type="image/png",
            extension="png",
        )
    )
    extras.append(extras_plugin.text(driver.current_url, name="current_url"))
    report.extras = extras


def pytest_html_report_title(report):
    report.title = "InkForge 自动化测试原始报告"
