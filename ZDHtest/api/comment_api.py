from __future__ import annotations

from core.api_client import ApiClient


class CommentApi:
    def __init__(self, client: ApiClient):
        self.client = client

    def add(self, content_id: int, content: str):
        return self.client.post("/api/comment", json={"contentId": content_id, "content": content})

    def list(self, content_id: int, page: int = 1, size: int = 20):
        return self.client.get(f"/api/comment/{content_id}", params={"page": page, "size": size})

    def delete(self, comment_id: int):
        return self.client.delete(f"/api/comment/{comment_id}")
