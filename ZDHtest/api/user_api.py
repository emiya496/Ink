from __future__ import annotations

from typing import Any

from core.api_client import ApiClient


class UserApi:
    def __init__(self, client: ApiClient):
        self.client = client

    def register(self, username: str, password: str, email: str | None = None):
        payload: dict[str, Any] = {"username": username, "password": password}
        if email:
            payload["email"] = email
        return self.client.post("/api/user/register", json=payload)

    def login(self, username: str, password: str):
        response = self.client.post(
            "/api/user/login",
            json={"username": username, "password": password},
        )
        try:
            token = response.json()["data"]["token"]
        except Exception:
            token = None
        if token:
            self.client.set_token(token)
        return response

    def info(self):
        return self.client.get("/api/user/info")

    def update_info(self, avatar: str):
        return self.client.put("/api/user/info", json={"avatar": avatar})

    def profile(self, user_id: int):
        return self.client.get(f"/api/user/{user_id}/profile")

    def search(self, keyword: str = "", page: int = 1, size: int = 20):
        params = {"page": page, "size": size}
        if keyword:
            params["keyword"] = keyword
        return self.client.get("/api/user/search", params=params)

    def update_bio(self, bio: str):
        return self.client.put("/api/user/bio", json={"bio": bio})

    def send_email_code(self, email: str, purpose: str):
        return self.client.post("/api/user/email/send-code", json={"email": email, "purpose": purpose})

    def bind_email(self, email: str, code: str):
        return self.client.post("/api/user/email/bind", json={"email": email, "code": code})

    def change_email(self, email: str, code: str):
        return self.client.put("/api/user/email/change", json={"email": email, "code": code})

    def send_password_reset_code(self, email: str):
        return self.client.post("/api/user/password/send-code", json={"email": email})

    def reset_password(self, email: str, code: str, new_password: str):
        return self.client.post(
            "/api/user/password/reset",
            json={"email": email, "code": code, "newPassword": new_password},
        )

    def update_username(self, new_username: str):
        return self.client.put("/api/user/username", json={"newUsername": new_username})

    def change_password(self, old_password: str, new_password: str):
        return self.client.put(
            "/api/user/password",
            json={"oldPassword": old_password, "newPassword": new_password},
        )

    def send_delete_code(self):
        return self.client.post("/api/user/account/send-delete-code")

    def delete_account(self, password: str, code: str):
        return self.client.delete("/api/user/account", json={"password": password, "code": code})
