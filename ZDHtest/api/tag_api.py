from __future__ import annotations

from core.api_client import ApiClient


class TagApi:
    def __init__(self, client: ApiClient):
        self.client = client

    def list(self, type_name: str | None = None):
        params = {"type": type_name} if type_name else None
        return self.client.get("/api/tag/list", params=params)

    def create(self, tag_name: str):
        return self.client.post("/api/tag/create", json={"tagName": tag_name})

    def hot(self, limit: int = 20):
        return self.client.get("/api/tag/hot", params={"limit": limit})
