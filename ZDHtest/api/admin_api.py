from __future__ import annotations

from core.api_client import ApiClient


class AdminApi:
    def __init__(self, client: ApiClient):
        self.client = client

    def list_users(self, page: int = 1, size: int = 10, keyword: str | None = None):
        params = {"page": page, "size": size}
        if keyword:
            params["keyword"] = keyword
        return self.client.get("/api/admin/users", params=params)

    def update_user_status(self, user_id: int, status: str):
        return self.client.put(f"/api/admin/users/{user_id}/status", json={"status": status})

    def delete_user(self, user_id: int):
        return self.client.delete(f"/api/admin/users/{user_id}")

    def list_contents(self, page: int = 1, size: int = 10, **filters):
        params = {"page": page, "size": size}
        params.update({k: v for k, v in filters.items() if v is not None and v != ""})
        return self.client.get("/api/admin/contents", params=params)

    def update_content_status(self, content_id: int, status: str):
        return self.client.put(f"/api/admin/contents/{content_id}/status", json={"status": status})

    def delete_content(self, content_id: int):
        return self.client.delete(f"/api/admin/contents/{content_id}")

    def list_tags(self, page: int = 1, size: int = 20, keyword: str | None = None):
        params = {"page": page, "size": size}
        if keyword:
            params["keyword"] = keyword
        return self.client.get("/api/admin/tags", params=params)

    def create_tag(self, tag_name: str):
        return self.client.post("/api/admin/tags", json={"tagName": tag_name})

    def update_tag_status(self, tag_id: int, status: str):
        return self.client.put(f"/api/admin/tags/{tag_id}/status", json={"status": status})

    def delete_tag(self, tag_id: int):
        return self.client.delete(f"/api/admin/tags/{tag_id}")

    def list_comments(self, page: int = 1, size: int = 20, keyword: str | None = None):
        params = {"page": page, "size": size}
        if keyword:
            params["keyword"] = keyword
        return self.client.get("/api/admin/comments", params=params)

    def delete_comment(self, comment_id: int):
        return self.client.delete(f"/api/admin/comments/{comment_id}")

    def ai_stats(self):
        return self.client.get("/api/admin/ai/stats")
