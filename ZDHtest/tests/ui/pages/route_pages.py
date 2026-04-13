from __future__ import annotations

from core.base_page import BasePage


class HomePage(BasePage):
    path = "/"
    root_selector = ".home-page"


class CategoryPage(BasePage):
    path = "/category"
    root_selector = ".category-page"


class DetailPage(BasePage):
    root_selector = ".detail-page"

    def open_for_content(self, content_id: int) -> None:
        self.open(f"/detail/{content_id}")


class PublishPage(BasePage):
    path = "/publish"
    root_selector = ".publish-page"


class UserCenterPage(BasePage):
    path = "/user"
    root_selector = ".user-center-page"


class BookshelfPage(BasePage):
    path = "/bookshelf"
    root_selector = ".bookshelf-page"


class UserProfilePage(BasePage):
    root_selector = ".user-profile-page"

    def open_for_user(self, user_id: int) -> None:
        self.open(f"/user/{user_id}")


class AdminUsersPage(BasePage):
    path = "/admin/users"
    root_selector = ".admin-layout"


class AdminContentsPage(BasePage):
    path = "/admin/contents"
    root_selector = ".admin-layout"


class AdminTagsPage(BasePage):
    path = "/admin/tags"
    root_selector = ".admin-layout"


class AdminCommentsPage(BasePage):
    path = "/admin/comments"
    root_selector = ".admin-layout"


class AdminAiStatsPage(BasePage):
    path = "/admin/ai-stats"
    root_selector = ".admin-layout"
