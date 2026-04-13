from __future__ import annotations

from pathlib import Path

from core.api_client import ApiClient


class UploadApi:
    def __init__(self, client: ApiClient):
        self.client = client

    def upload_image(self, file_path: str | Path):
        with open(file_path, "rb") as file_obj:
            files = {"file": (Path(file_path).name, file_obj, "image/png")}
            return self.client.post(
                "/api/upload/image",
                files=files,
                headers={"Content-Type": None},
            )
