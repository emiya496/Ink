from __future__ import annotations

import subprocess
import sys
from pathlib import Path


ROOT_DIR = Path(__file__).resolve().parent


if __name__ == "__main__":
    raise SystemExit(
        subprocess.run(
            [sys.executable, str(ROOT_DIR / "run_tests.py"), "--suite", "api", *sys.argv[1:]],
            cwd=ROOT_DIR,
        ).returncode
    )
