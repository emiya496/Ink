from __future__ import annotations

from core.api_client import ApiClient


class FavoriteApi:
    def __init__(self, client: ApiClient):
        self.client = client

    def add(self, content_id: int):
        return self.client.post(f"/api/favorite/{content_id}")

    def remove(self, content_id: int):
        return self.client.delete(f"/api/favorite/{content_id}")

    def list(self, page: int = 1, size: int = 10):
        return self.client.get("/api/favorite/list", params={"page": page, "size": size})

    def check(self, content_id: int):
        return self.client.get(f"/api/favorite/check/{content_id}")
