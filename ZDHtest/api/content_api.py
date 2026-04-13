from __future__ import annotations

from typing import Any

from core.api_client import ApiClient


class ContentApi:
    def __init__(self, client: ApiClient):
        self.client = client

    def create(self, payload: dict[str, Any]):
        return self.client.post("/api/content/create", json=payload)

    def list(self, **params: Any):
        return self.client.get("/api/content/list", params=params)

    def rank(self, type_name: str = "", rank_type: str = "reads"):
        params = {"rankType": rank_type}
        if type_name:
            params["type"] = type_name
        return self.client.get("/api/content/rank", params=params)

    def hot_banner(self, type_name: str = ""):
        params = {"type": type_name} if type_name else None
        return self.client.get("/api/content/hot-banner", params=params)

    def detail(self, content_id: int):
        return self.client.get(f"/api/content/{content_id}")

    def update(self, content_id: int, payload: dict[str, Any]):
        return self.client.put(f"/api/content/{content_id}", json=payload)

    def delete(self, content_id: int):
        return self.client.delete(f"/api/content/{content_id}")

    def my(self, page: int = 1, size: int = 10):
        return self.client.get("/api/content/my", params={"page": page, "size": size})

    def my_drafts(self, page: int = 1, size: int = 10):
        return self.client.get("/api/content/my/drafts", params={"page": page, "size": size})

    def my_likes(self, page: int = 1, size: int = 20):
        return self.client.get("/api/content/my/likes", params={"page": page, "size": size})

    def user_works(self, user_id: int, page: int = 1, size: int = 20):
        return self.client.get(f"/api/content/user/{user_id}", params={"page": page, "size": size})

    def like(self, content_id: int):
        return self.client.post(f"/api/content/{content_id}/like")
