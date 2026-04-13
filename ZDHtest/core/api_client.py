from __future__ import annotations

import time
from typing import Any

import requests
from reporting import record_api_call


class ApiClient:
    def __init__(self, base_url: str, timeout: int = 10):
        self.base_url = base_url.rstrip("/")
        self.timeout = timeout
        self.session = requests.Session()
        self.token: str | None = None

    def set_token(self, token: str | None) -> None:
        self.token = token

    def close(self) -> None:
        self.session.close()

    def _headers(self, headers: dict[str, str] | None = None) -> dict[str, str]:
        merged = {"Content-Type": "application/json"}
        if self.token:
            merged["Authorization"] = f"Bearer {self.token}"
        if headers:
            merged.update(headers)
        return {key: value for key, value in merged.items() if value is not None}

    def request(self, method: str, path: str, **kwargs: Any) -> requests.Response:
        timeout = kwargs.pop("timeout", self.timeout)
        headers = self._headers(kwargs.pop("headers", None))
        start = time.perf_counter()
        try:
            response = self.session.request(
                method=method,
                url=f"{self.base_url}{path}",
                headers=headers,
                timeout=timeout,
                **kwargs,
            )
        except Exception as exc:
            record_api_call(
                method=method,
                path=path,
                response=None,
                duration_ms=int((time.perf_counter() - start) * 1000),
                error=str(exc),
            )
            raise

        record_api_call(
            method=method,
            path=path,
            response=response,
            duration_ms=int((time.perf_counter() - start) * 1000),
        )
        return response

    def get(self, path: str, **kwargs: Any) -> requests.Response:
        return self.request("GET", path, **kwargs)

    def post(self, path: str, **kwargs: Any) -> requests.Response:
        return self.request("POST", path, **kwargs)

    def put(self, path: str, **kwargs: Any) -> requests.Response:
        return self.request("PUT", path, **kwargs)

    def delete(self, path: str, **kwargs: Any) -> requests.Response:
        return self.request("DELETE", path, **kwargs)
