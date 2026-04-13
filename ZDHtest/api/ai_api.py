from __future__ import annotations

from core.api_client import ApiClient


class AiApi:
    def __init__(self, client: ApiClient):
        self.client = client

    def generate(self, content: str):
        return self.client.post("/api/ai/generate", json={"content": content})

    def summary(self, content: str):
        return self.client.post("/api/ai/summary", json={"content": content})

    def polish(self, content: str):
        return self.client.post("/api/ai/polish", json={"content": content})

    def keywords(self, content: str):
        return self.client.post("/api/ai/keywords", json={"content": content})

    def sentiment(self, content: str):
        return self.client.post("/api/ai/sentiment", json={"content": content})

    def style(self, content: str):
        return self.client.post("/api/ai/style", json={"content": content})

    def qa(self, content: str, question: str):
        return self.client.post("/api/ai/qa", json={"content": content, "question": question})
