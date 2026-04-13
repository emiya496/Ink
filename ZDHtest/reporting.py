from __future__ import annotations

import json
import threading
from dataclasses import dataclass
from html import escape
from pathlib import Path
from typing import Any
from xml.etree import ElementTree as ET


STATUS_TEXT = {
    "passed": "通过",
    "failed": "失败",
    "error": "错误",
    "skipped": "跳过",
    "unknown": "未知",
}

API_MODULE_LABELS = {
    "/api/user": "用户模块",
    "/api/content": "内容模块",
    "/api/comment": "评论模块",
    "/api/chapter": "章节模块",
    "/api/favorite": "收藏模块",
    "/api/follow": "关注模块",
    "/api/tag": "标签模块",
    "/api/upload": "上传模块",
    "/api/ai": "AI 模块",
    "/api/admin": "管理端模块",
}

TEST_MODULE_LABELS = {
    "test_user_api.py": "用户模块",
    "test_content_api.py": "内容模块",
    "test_comment_api.py": "评论模块",
    "test_chapter_api.py": "章节模块",
    "test_favorite_api.py": "收藏模块",
    "test_follow_api.py": "关注模块",
    "test_tag_api.py": "标签模块",
    "test_upload_api.py": "上传模块",
    "test_ai_api.py": "AI 模块",
    "test_admin_api.py": "管理端模块",
    "test_ui_smoke.py": "UI 模块",
    "test_ui_admin.py": "UI 模块",
}

_RUNTIME = {"requests": []}
_CTX = threading.local()


@dataclass
class TestCaseResult:
    test_id: str
    module: str
    duration: str
    status: str
    detail: str


def _suite_cn_name(suite: str) -> str:
    return {
        "all": "全部测试",
        "api": "接口测试",
        "ui": "UI 测试",
    }.get(suite, suite)


def reset_runtime() -> None:
    _RUNTIME["requests"] = []
    _CTX.test_id = "未绑定测试"
    _CTX.phase = "unknown"


def set_current_test(test_id: str) -> None:
    _CTX.test_id = test_id


def clear_current_test() -> None:
    _CTX.test_id = "未绑定测试"
    _CTX.phase = "unknown"


def set_current_phase(phase: str) -> None:
    _CTX.phase = phase


def _get_current_test_id() -> str:
    return getattr(_CTX, "test_id", "未绑定测试")


def _get_current_phase() -> str:
    return getattr(_CTX, "phase", "unknown")


def detect_api_module(path: str) -> str:
    for prefix, label in API_MODULE_LABELS.items():
        if path.startswith(prefix):
            return label
    return "未分类模块"


def detect_test_module(test_id: str) -> str:
    filename = test_id.split("::", 1)[0].replace("\\", "/").split("/")[-1]
    return TEST_MODULE_LABELS.get(filename, "未分类模块")


def _safe_json(response: Any) -> dict[str, Any] | None:
    try:
        payload = response.json()
    except Exception:
        return None
    return payload if isinstance(payload, dict) else None


def _request_status(response: Any | None, error: str | None) -> str:
    if error or response is None:
        return "error"
    if response.status_code >= 400:
        return "failed"
    payload = _safe_json(response)
    if payload is not None and payload.get("code") not in (None, 200):
        return "failed"
    return "passed"


def record_api_call(
    *,
    method: str,
    path: str,
    response: Any | None,
    duration_ms: int,
    error: str | None = None,
) -> None:
    payload = _safe_json(response) if response is not None else None
    response_preview = ""
    if payload is not None:
        response_preview = json.dumps(payload, ensure_ascii=False)[:500]
    elif response is not None:
        response_preview = (response.text or "")[:500]

    _RUNTIME["requests"].append(
        {
            "test_id": _get_current_test_id(),
            "phase": _get_current_phase(),
            "module": detect_api_module(path),
            "method": method.upper(),
            "path": path,
            "duration_ms": duration_ms,
            "status": _request_status(response, error),
            "http_status": response.status_code if response is not None else None,
            "business_code": payload.get("code") if payload is not None else None,
            "business_message": payload.get("message") if payload is not None else None,
            "error": error,
            "response_preview": response_preview,
        }
    )


def write_request_trace(output_path: Path) -> Path:
    output_path.write_text(
        json.dumps({"requests": _RUNTIME["requests"]}, ensure_ascii=False, indent=2),
        encoding="utf-8",
    )
    return output_path


def _parse_junit_cases(xml_path: Path) -> list[TestCaseResult]:
    root = ET.parse(xml_path).getroot()
    cases: list[TestCaseResult] = []

    for case in root.iter("testcase"):
        classname = case.attrib.get("classname", "")
        name = case.attrib.get("name", "")
        duration = case.attrib.get("time", "0")
        status = "passed"
        detail = "无"

        if case.find("failure") is not None:
            failure = case.find("failure")
            status = "failed"
            detail = failure.attrib.get("message", "") if failure is not None else ""
            detail = detail or (failure.text if failure is not None and failure.text else "无")
        elif case.find("error") is not None:
            error = case.find("error")
            status = "error"
            detail = error.attrib.get("message", "") if error is not None else ""
            detail = detail or (error.text if error is not None and error.text else "无")
        elif case.find("skipped") is not None:
            skipped = case.find("skipped")
            status = "skipped"
            detail = skipped.attrib.get("message", "") if skipped is not None else ""
            detail = detail or (skipped.text if skipped is not None and skipped.text else "无")

        test_id = f"{classname}::{name}" if classname else name
        cases.append(
            TestCaseResult(
                test_id=test_id,
                module=detect_test_module(test_id),
                duration=f"{float(duration):.3f}s",
                status=status,
                detail=detail.strip(),
            )
        )
    return cases


def _load_request_trace(trace_path: Path) -> list[dict[str, Any]]:
    if not trace_path.exists():
        return []
    payload = json.loads(trace_path.read_text(encoding="utf-8"))
    requests_data = payload.get("requests", [])
    return [item for item in requests_data if isinstance(item, dict)]


def _status_rank(status: str) -> int:
    return {
        "error": 3,
        "failed": 2,
        "skipped": 1,
        "passed": 0,
    }.get(status, -1)


def _aggregate_interfaces(requests_data: list[dict[str, Any]]) -> list[dict[str, Any]]:
    grouped: dict[tuple[str, str, str], dict[str, Any]] = {}
    for item in requests_data:
        key = (item["module"], item["method"], item["path"])
        group = grouped.setdefault(
            key,
            {
                "module": item["module"],
                "method": item["method"],
                "path": item["path"],
                "calls": 0,
                "tests": set(),
                "status": "passed",
                "sample_http_status": item.get("http_status"),
                "sample_business_code": item.get("business_code"),
                "sample_message": item.get("business_message") or item.get("error") or "",
            },
        )
        group["calls"] += 1
        group["tests"].add(item["test_id"])

        if _status_rank(item["status"]) > _status_rank(group["status"]):
            group["status"] = item["status"]
            group["sample_http_status"] = item.get("http_status")
            group["sample_business_code"] = item.get("business_code")
            group["sample_message"] = item.get("business_message") or item.get("error") or ""

    rows = []
    for item in grouped.values():
        item["tests"] = len(item["tests"])
        rows.append(item)
    rows.sort(key=lambda x: (x["module"], x["method"], x["path"]))
    return rows


def _aggregate_modules(
    tests_data: list[TestCaseResult], requests_data: list[dict[str, Any]], interface_data: list[dict[str, Any]]
) -> list[dict[str, Any]]:
    grouped: dict[str, dict[str, Any]] = {}

    for test in tests_data:
        group = grouped.setdefault(
            test.module,
            {
                "module": test.module,
                "test_count": 0,
                "request_count": 0,
                "interface_count": 0,
                "passed": 0,
                "failed": 0,
                "error": 0,
                "skipped": 0,
            },
        )
        group["test_count"] += 1
        group[test.status] += 1

    for request in requests_data:
        group = grouped.setdefault(
            request["module"],
            {
                "module": request["module"],
                "test_count": 0,
                "request_count": 0,
                "interface_count": 0,
                "passed": 0,
                "failed": 0,
                "error": 0,
                "skipped": 0,
            },
        )
        group["request_count"] += 1

    for interface in interface_data:
        group = grouped.setdefault(
            interface["module"],
            {
                "module": interface["module"],
                "test_count": 0,
                "request_count": 0,
                "interface_count": 0,
                "passed": 0,
                "failed": 0,
                "error": 0,
                "skipped": 0,
            },
        )
        group["interface_count"] += 1

    return sorted(grouped.values(), key=lambda x: x["module"])


def _module_cards_html(modules: list[dict[str, Any]]) -> str:
    parts = []
    for item in modules:
        parts.append(
            """
            <div class="module-card">
              <div class="module-name">{module}</div>
              <div class="module-meta">用例 {test_count} 条 | 接口 {interface_count} 条 | 调用 {request_count} 次</div>
              <div class="module-stats">
                <span class="badge passed">通过 {passed}</span>
                <span class="badge failed">失败 {failed}</span>
                <span class="badge error">错误 {error}</span>
                <span class="badge skipped">跳过 {skipped}</span>
              </div>
            </div>
            """.format(
                module=escape(item["module"]),
                test_count=item["test_count"],
                interface_count=item["interface_count"],
                request_count=item["request_count"],
                passed=item["passed"],
                failed=item["failed"],
                error=item["error"],
                skipped=item["skipped"],
            )
        )
    return "\n".join(parts) or "<p>本次没有模块数据。</p>"


def _rows_filter_script() -> str:
    return """
    <script>
      function bindFilters(inputId, selectId, tableId) {
        const input = document.getElementById(inputId);
        const select = document.getElementById(selectId);
        const table = document.getElementById(tableId);
        if (!input || !select || !table) return;
        const rows = Array.from(table.querySelectorAll("tbody tr"));

        function applyFilter() {
          const keyword = input.value.trim().toLowerCase();
          const status = select.value;
          rows.forEach((row) => {
            const search = row.dataset.search || "";
            const rowStatus = row.dataset.status || "";
            const matchedKeyword = !keyword || search.includes(keyword);
            const matchedStatus = !status || rowStatus === status;
            row.style.display = matchedKeyword && matchedStatus ? "" : "none";
          });
        }

        input.addEventListener("input", applyFilter);
        select.addEventListener("change", applyFilter);
      }

      bindFilters("interface-search", "interface-status", "interface-table");
      bindFilters("request-search", "request-status", "request-table");
      bindFilters("test-search", "test-status", "test-table");
    </script>
    """


def generate_zh_report(xml_path: Path, trace_path: Path, output_path: Path, *, suite: str) -> Path | None:
    if not xml_path.exists():
        return None

    tests_data = _parse_junit_cases(xml_path)
    requests_data = _load_request_trace(trace_path)
    interface_data = _aggregate_interfaces(requests_data)
    module_data = _aggregate_modules(tests_data, requests_data, interface_data)

    test_counts = {
        "passed": sum(1 for item in tests_data if item.status == "passed"),
        "failed": sum(1 for item in tests_data if item.status == "failed"),
        "error": sum(1 for item in tests_data if item.status == "error"),
        "skipped": sum(1 for item in tests_data if item.status == "skipped"),
    }

    interface_rows = []
    for item in interface_data:
        search = f"{item['module']} {item['method']} {item['path']} {item['sample_message']}".lower()
        interface_rows.append(
            """
            <tr data-status="{status}" data-search="{search}">
              <td class="status {status}">{status_text}</td>
              <td>{module}</td>
              <td>{method}</td>
              <td>{path}</td>
              <td>{calls}</td>
              <td>{tests}</td>
              <td>{http_status}</td>
              <td>{biz_code}</td>
              <td><pre>{message}</pre></td>
            </tr>
            """.format(
                status=item["status"],
                status_text=STATUS_TEXT.get(item["status"], "未知"),
                search=escape(search),
                module=escape(item["module"]),
                method=escape(item["method"]),
                path=escape(item["path"]),
                calls=item["calls"],
                tests=item["tests"],
                http_status=escape(str(item["sample_http_status"] or "-")),
                biz_code=escape(str(item["sample_business_code"] or "-")),
                message=escape(item["sample_message"] or "-"),
            )
        )

    request_rows = []
    for item in requests_data:
        search = (
            f"{item['module']} {item['method']} {item['path']} {item['test_id']} "
            f"{item.get('business_message') or ''} {item.get('error') or ''}"
        ).lower()
        request_rows.append(
            """
            <tr data-status="{status}" data-search="{search}">
              <td class="status {status}">{status_text}</td>
              <td>{module}</td>
              <td>{method}</td>
              <td>{path}</td>
              <td>{http_status}</td>
              <td>{biz_code}</td>
              <td>{duration}</td>
              <td>{test_id}</td>
              <td><pre>{message}</pre></td>
            </tr>
            """.format(
                status=item["status"],
                status_text=STATUS_TEXT.get(item["status"], "未知"),
                search=escape(search),
                module=escape(item["module"]),
                method=escape(item["method"]),
                path=escape(item["path"]),
                http_status=escape(str(item.get("http_status") or "-")),
                biz_code=escape(str(item.get("business_code") or "-")),
                duration=escape(f"{item.get('duration_ms', 0)} ms"),
                test_id=escape(item["test_id"]),
                message=escape(item.get("business_message") or item.get("error") or "-"),
            )
        )

    test_rows = []
    for item in tests_data:
        search = f"{item.module} {item.test_id} {item.detail}".lower()
        test_rows.append(
            """
            <tr data-status="{status}" data-search="{search}">
              <td class="status {status}">{status_text}</td>
              <td>{module}</td>
              <td>{test_id}</td>
              <td>{duration}</td>
              <td><pre>{detail}</pre></td>
            </tr>
            """.format(
                status=item.status,
                status_text=STATUS_TEXT.get(item.status, "未知"),
                search=escape(search),
                module=escape(item.module),
                test_id=escape(item.test_id),
                duration=escape(item.duration),
                detail=escape(item.detail),
            )
        )

    html_text = """<!DOCTYPE html>
<html lang="zh-CN">
<head>
  <meta charset="utf-8">
  <title>InkForge 自动化测试增强报告</title>
  <style>
    body {{
      font-family: "Microsoft YaHei", "PingFang SC", sans-serif;
      margin: 24px;
      color: #1f2937;
      background: #f8fafc;
    }}
    h1, h2 {{
      color: #111827;
      margin-bottom: 12px;
    }}
    p {{
      color: #4b5563;
    }}
    .cards {{
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
      gap: 12px;
      margin: 18px 0 24px;
    }}
    .card, .module-card {{
      background: #fff;
      border: 1px solid #e5e7eb;
      border-radius: 12px;
      padding: 14px 16px;
      box-shadow: 0 2px 8px rgba(15, 23, 42, 0.05);
    }}
    .label {{
      color: #6b7280;
      font-size: 13px;
      margin-bottom: 6px;
    }}
    .value {{
      font-size: 24px;
      font-weight: 700;
    }}
    .module-grid {{
      display: grid;
      grid-template-columns: repeat(auto-fit, minmax(280px, 1fr));
      gap: 12px;
      margin-bottom: 24px;
    }}
    .module-name {{
      font-size: 18px;
      font-weight: 700;
      margin-bottom: 8px;
    }}
    .module-meta {{
      color: #6b7280;
      font-size: 13px;
      margin-bottom: 10px;
    }}
    .module-stats {{
      display: flex;
      gap: 8px;
      flex-wrap: wrap;
    }}
    .badge {{
      display: inline-block;
      border-radius: 999px;
      padding: 4px 10px;
      font-size: 12px;
      font-weight: 700;
      background: #f3f4f6;
    }}
    .passed {{
      color: #15803d;
    }}
    .failed {{
      color: #b91c1c;
    }}
    .error {{
      color: #c2410c;
    }}
    .skipped {{
      color: #a16207;
    }}
    .filters {{
      display: flex;
      gap: 12px;
      flex-wrap: wrap;
      margin: 8px 0 12px;
    }}
    .filters input, .filters select {{
      border: 1px solid #cbd5e1;
      border-radius: 8px;
      padding: 8px 10px;
      min-width: 220px;
      background: #fff;
    }}
    table {{
      width: 100%;
      border-collapse: collapse;
      background: #fff;
      border: 1px solid #e5e7eb;
      margin-bottom: 28px;
    }}
    th, td {{
      border: 1px solid #e5e7eb;
      padding: 10px 12px;
      text-align: left;
      vertical-align: top;
    }}
    th {{
      background: #f3f4f6;
    }}
    .status {{
      width: 72px;
      font-weight: 700;
    }}
    pre {{
      margin: 0;
      white-space: pre-wrap;
      word-break: break-word;
      font-family: Consolas, "Courier New", monospace;
      font-size: 12px;
      color: #334155;
    }}
    .tip {{
      margin-top: -12px;
      margin-bottom: 18px;
      color: #6b7280;
      font-size: 13px;
    }}
  </style>
</head>
<body>
  <h1>InkForge 自动化测试增强报告</h1>
  <p>测试范围：{suite_name}</p>

  <div class="cards">
    <div class="card"><div class="label">总用例数</div><div class="value">{test_total}</div></div>
    <div class="card"><div class="label">总模块数</div><div class="value">{module_total}</div></div>
    <div class="card"><div class="label">接口调用次数</div><div class="value">{request_total}</div></div>
    <div class="card"><div class="label">唯一接口数</div><div class="value">{interface_total}</div></div>
    <div class="card"><div class="label">用例通过</div><div class="value">{passed}</div></div>
    <div class="card"><div class="label">用例失败/错误</div><div class="value">{failed_and_error}</div></div>
  </div>

  <h2>模块汇总</h2>
  <p class="tip">先看模块层面的通过、失败、接口数量，再往下看具体接口。</p>
  <div class="module-grid">
    {module_cards}
  </div>

  <h2>接口汇总</h2>
  <p class="tip">按模块和接口路径聚合后的结果，一行代表一个唯一接口。</p>
  <div class="filters">
    <input id="interface-search" type="text" placeholder="搜索模块、方法、路径、错误信息">
    <select id="interface-status">
      <option value="">全部状态</option>
      <option value="passed">通过</option>
      <option value="failed">失败</option>
      <option value="error">错误</option>
    </select>
  </div>
  <table id="interface-table">
    <thead>
      <tr>
        <th>结果</th>
        <th>模块</th>
        <th>方法</th>
        <th>接口路径</th>
        <th>调用次数</th>
        <th>关联用例数</th>
        <th>HTTP</th>
        <th>业务码</th>
        <th>错误摘要</th>
      </tr>
    </thead>
    <tbody>
      {interface_rows}
    </tbody>
  </table>

  <h2>接口调用明细</h2>
  <p class="tip">一行代表一次实际接口调用，适合看是哪个测试函数把它打出来的。</p>
  <div class="filters">
    <input id="request-search" type="text" placeholder="搜索模块、路径、测试函数、消息">
    <select id="request-status">
      <option value="">全部状态</option>
      <option value="passed">通过</option>
      <option value="failed">失败</option>
      <option value="error">错误</option>
    </select>
  </div>
  <table id="request-table">
    <thead>
      <tr>
        <th>结果</th>
        <th>模块</th>
        <th>方法</th>
        <th>接口路径</th>
        <th>HTTP</th>
        <th>业务码</th>
        <th>耗时</th>
        <th>所属用例</th>
        <th>消息</th>
      </tr>
    </thead>
    <tbody>
      {request_rows}
    </tbody>
  </table>

  <h2>用例结果明细</h2>
  <p class="tip">保留 pytest 用例视角，方便和原始报告对照。</p>
  <div class="filters">
    <input id="test-search" type="text" placeholder="搜索模块、测试名、失败原因">
    <select id="test-status">
      <option value="">全部状态</option>
      <option value="passed">通过</option>
      <option value="failed">失败</option>
      <option value="error">错误</option>
      <option value="skipped">跳过</option>
    </select>
  </div>
  <table id="test-table">
    <thead>
      <tr>
        <th>结果</th>
        <th>模块</th>
        <th>用例</th>
        <th>耗时</th>
        <th>详情</th>
      </tr>
    </thead>
    <tbody>
      {test_rows}
    </tbody>
  </table>

  {script}
</body>
</html>
""".format(
        suite_name=_suite_cn_name(suite),
        test_total=len(tests_data),
        module_total=len(module_data),
        request_total=len(requests_data),
        interface_total=len(interface_data),
        passed=test_counts["passed"],
        failed_and_error=test_counts["failed"] + test_counts["error"],
        module_cards=_module_cards_html(module_data),
        interface_rows="\n".join(interface_rows) or '<tr><td colspan="9">本次没有接口调用数据。</td></tr>',
        request_rows="\n".join(request_rows) or '<tr><td colspan="9">本次没有接口调用明细。</td></tr>',
        test_rows="\n".join(test_rows) or '<tr><td colspan="5">本次没有用例结果。</td></tr>',
        script=_rows_filter_script(),
    )

    output_path.write_text(html_text, encoding="utf-8")
    return output_path
