from __future__ import annotations

from selenium.webdriver.common.by import By

from core.base_page import BasePage


class LoginPage(BasePage):
    path = "/login"
    root_selector = ".login-page"

    def _active_inputs(self):
        panes = self.driver.find_elements(By.CSS_SELECTOR, ".login-page .el-tab-pane")
        for pane in panes:
            if pane.value_of_css_property("display") == "none":
                continue
            inputs = pane.find_elements(By.CSS_SELECTOR, "input")
            if len(inputs) >= 2:
                return inputs
        raise AssertionError("Visible login inputs not found.")

    def login(self, username: str, password: str) -> None:
        self.open()
        self.wait_until_ready()
        username_input, password_input = self._active_inputs()[:2]
        username_input.clear()
        username_input.send_keys(username)
        password_input.clear()
        password_input.send_keys(password)

        panes = self.driver.find_elements(By.CSS_SELECTOR, ".login-page .el-tab-pane")
        active_pane = next(p for p in panes if p.value_of_css_property("display") != "none")
        active_pane.find_element(By.CSS_SELECTOR, "button.el-button--primary").click()
