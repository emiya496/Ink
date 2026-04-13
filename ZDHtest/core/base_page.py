from __future__ import annotations

from selenium.webdriver.common.by import By
from selenium.webdriver.remote.webdriver import WebDriver
from selenium.webdriver.support import expected_conditions as EC
from selenium.webdriver.support.ui import WebDriverWait


class BasePage:
    path = "/"
    root_selector = "body"

    def __init__(self, driver: WebDriver, base_url: str, timeout: int = 15):
        self.driver = driver
        self.base_url = base_url.rstrip("/")
        self.wait = WebDriverWait(driver, timeout)

    def open(self, path: str | None = None) -> None:
        target = path if path is not None else self.path
        self.driver.get(f"{self.base_url}{target}")

    def wait_until_ready(self) -> None:
        self.wait.until(EC.presence_of_element_located((By.CSS_SELECTOR, self.root_selector)))

    def is_present(self, css: str) -> bool:
        return len(self.driver.find_elements(By.CSS_SELECTOR, css)) > 0

    def set_local_storage(self, key: str, value: str) -> None:
        self.driver.execute_script("window.localStorage.setItem(arguments[0], arguments[1]);", key, value)

    def get_local_storage(self, key: str):
        return self.driver.execute_script("return window.localStorage.getItem(arguments[0]);", key)
