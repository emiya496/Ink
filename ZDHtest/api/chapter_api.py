from __future__ import annotations

from typing import Any

from core.api_client import ApiClient


class ChapterApi:
    def __init__(self, client: ApiClient):
        self.client = client

    def add(self, content_id: int, chapter_title: str, chapter_content: str, chapter_order: int | None = None):
        payload: dict[str, Any] = {
            "contentId": content_id,
            "chapterTitle": chapter_title,
            "chapterContent": chapter_content,
        }
        if chapter_order is not None:
            payload["chapterOrder"] = chapter_order
        return self.client.post("/api/chapter", json=payload)

    def list(self, content_id: int):
        return self.client.get(f"/api/chapter/{content_id}")

    def update(self, chapter_id: int, chapter_title: str, chapter_content: str):
        return self.client.put(
            f"/api/chapter/{chapter_id}",
            json={"chapterTitle": chapter_title, "chapterContent": chapter_content},
        )

    def delete(self, chapter_id: int):
        return self.client.delete(f"/api/chapter/{chapter_id}")
