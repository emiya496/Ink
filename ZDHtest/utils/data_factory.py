from __future__ import annotations

import time
from pathlib import Path


def unique_suffix() -> str:
    return str(int(time.time() * 1000))


def make_username(prefix: str = "pytest_user") -> str:
    safe_prefix = "".join(ch for ch in prefix.lower() if ch.isalnum())
    safe_prefix = safe_prefix[:6] or "user"
    short_suffix = unique_suffix()[-8:]
    return f"{safe_prefix}{short_suffix}"


def make_email(prefix: str = "pytest_user") -> str:
    return f"{prefix}_{unique_suffix()}@example.com"


def make_tag_name(prefix: str = "pytest_tag") -> str:
    return f"{prefix}_{unique_suffix()}"


def make_text(prefix: str = "pytest_text") -> str:
    return f"{prefix}_{unique_suffix()}"


def article_payload(title: str, content: str, *, is_draft: bool = False, visibility: str = "public") -> dict:
    return {
        "title": title,
        "content": content,
        "type": "散文",
        "coverImage": None,
        "tagIds": [],
        "customTags": [],
        "isDraft": is_draft,
        "visibility": visibility,
    }


def novel_payload(title: str, synopsis: str) -> dict:
    return {
        "title": title,
        "content": synopsis,
        "type": "小说",
        "coverImage": None,
        "tagIds": [],
        "customTags": [],
        "isDraft": False,
        "visibility": "public",
    }


def default_upload_file(project_root: Path) -> Path:
    return project_root.parent / "inkforge-frontend" / "public" / "logo.png"
