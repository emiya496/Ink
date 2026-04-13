from __future__ import annotations

from core.api_client import ApiClient


class FollowApi:
    def __init__(self, client: ApiClient):
        self.client = client

    def follow(self, user_id: int):
        return self.client.post(f"/api/follow/{user_id}")

    def unfollow(self, user_id: int):
        return self.client.delete(f"/api/follow/{user_id}")

    def remove_follower(self, follower_id: int):
        return self.client.delete(f"/api/follow/follower/{follower_id}")

    def check(self, user_id: int):
        return self.client.get(f"/api/follow/check/{user_id}")

    def followers(self, user_id: int, page: int = 1, size: int = 20):
        return self.client.get(f"/api/follow/{user_id}/followers", params={"page": page, "size": size})

    def following(self, user_id: int, page: int = 1, size: int = 20):
        return self.client.get(f"/api/follow/{user_id}/following", params={"page": page, "size": size})
