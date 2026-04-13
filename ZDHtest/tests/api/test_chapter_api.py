from __future__ import annotations

import pytest

from utils.assertions import assert_business_ok
from utils.data_factory import make_text


@pytest.mark.api
def test_chapter_add_list_update_and_delete(session_user, novel_content):
    chapter_title = make_text("chapter")
    assert_business_ok(session_user.api.chapter.add(novel_content, chapter_title, "chapter body", chapter_order=1))

    list_payload = assert_business_ok(session_user.api.chapter.list(novel_content))
    chapter = next(item for item in list_payload["data"] if item["chapterTitle"] == chapter_title)
    chapter_id = int(chapter["id"])

    assert_business_ok(session_user.api.chapter.update(chapter_id, f"{chapter_title}_updated", "updated body"))
    updated_list_payload = assert_business_ok(session_user.api.chapter.list(novel_content))
    assert any(item["id"] == chapter_id and "updated" in item["chapterTitle"] for item in updated_list_payload["data"])

    assert_business_ok(session_user.api.chapter.delete(chapter_id))
