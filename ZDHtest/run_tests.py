from __future__ import annotations

import argparse
import os
import subprocess
import sys
from datetime import datetime
from pathlib import Path

from reporting import generate_zh_report


ROOT_DIR = Path(__file__).resolve().parent
REPORTS_DIR = ROOT_DIR / "reports"


def main() -> int:
    parser = argparse.ArgumentParser(description="Run InkForge automation tests.")
    parser.add_argument(
        "--suite",
        choices=["all", "api", "ui"],
        default="all",
        help="Choose which suite to run.",
    )
    args = parser.parse_args()

    REPORTS_DIR.mkdir(parents=True, exist_ok=True)
    timestamp = datetime.now().strftime("%Y%m%d-%H%M%S")
    report_path = REPORTS_DIR / f"report-{args.suite}-{timestamp}.html"
    xml_path = REPORTS_DIR / f"report-{args.suite}-{timestamp}.xml"
    trace_path = REPORTS_DIR / f"report-{args.suite}-{timestamp}-trace.json"
    zh_report_path = REPORTS_DIR / f"report-{args.suite}-{timestamp}-zh.html"

    command = [
        sys.executable,
        "-m",
        "pytest",
        "--html",
        str(report_path),
        "--self-contained-html",
        "--junitxml",
        str(xml_path),
    ]

    if args.suite != "all":
        command.extend(["-m", args.suite])

    env = os.environ.copy()
    env["INKFORGE_REQUEST_TRACE_PATH"] = str(trace_path)

    print(f"Running command: {' '.join(command)}")
    completed = subprocess.run(command, cwd=ROOT_DIR, env=env)
    zh_output = generate_zh_report(xml_path, trace_path, zh_report_path, suite=args.suite)
    print(f"HTML report: {report_path}")
    if zh_output is not None:
        print(f"中文 HTML 报告: {zh_output}")
    return completed.returncode


if __name__ == "__main__":
    raise SystemExit(main())
