"""Demo-mode smoke test for CI.

Installs the APK on a running emulator, taps through
Login -> "Try demo (no account)" -> vehicle list -> first "Report",
checks the expected activity is on top at each step, and saves screenshots.

Usage: python3 demo_smoke.py <apk> <out_dir>
"""

import re
import subprocess
import sys
import time
import xml.etree.ElementTree as ET
from pathlib import Path

PACKAGE = "edu.skku.map.carchecklab"


def adb(*args, capture=True):
    return subprocess.run(
        ["adb", *args], check=True, capture_output=capture, text=capture
    ).stdout


def screenshot(out: Path, name: str):
    data = subprocess.run(
        ["adb", "exec-out", "screencap", "-p"], check=True, capture_output=True
    ).stdout
    (out / f"{name}.png").write_bytes(data)
    print(f"screenshot: {name}.png")


def top_activity() -> str:
    dump = adb("shell", "dumpsys", "activity", "activities")
    for key in ("topResumedActivity", "mResumedActivity", "ResumedActivity"):
        m = re.search(key + r".*?" + re.escape(PACKAGE) + r"/([\w.]+)", dump)
        if m:
            return m.group(1).split(".")[-1]
    return ""


def assert_no_crash():
    log = adb("logcat", "-d", "-b", "crash")
    if PACKAGE in log:
        print(log)
        sys.exit("app crashed")


def wait_for_activity(name: str, timeout=30):
    end = time.time() + timeout
    while time.time() < end:
        assert_no_crash()
        if top_activity() == name:
            print(f"on {name}")
            return
        time.sleep(1)
    sys.exit(f"expected {name}, top is {top_activity() or 'unknown'}")


def find_node(text: str):
    adb("shell", "uiautomator", "dump", "/sdcard/ui.xml")
    xml = adb("shell", "cat", "/sdcard/ui.xml")
    root = ET.fromstring(xml[xml.index("<"):])
    for node in root.iter("node"):
        if node.get("text") == text:
            x1, y1, x2, y2 = map(int, re.findall(r"\d+", node.get("bounds")))
            return (x1 + x2) // 2, (y1 + y2) // 2
    return None


def screen_size():
    m = re.search(r"(\d+)x(\d+)", adb("shell", "wm", "size"))
    return int(m.group(1)), int(m.group(2))


def tap_text(text: str, scrolls=6):
    width, height = screen_size()
    for _ in range(scrolls + 1):
        pos = find_node(text)
        if pos:
            adb("shell", "input", "tap", str(pos[0]), str(pos[1]))
            print(f"tapped '{text}' at {pos}")
            return
        x = str(width // 2)
        adb("shell", "input", "swipe", x, str(height * 3 // 4), x, str(height // 4), "300")
        time.sleep(1)
    sys.exit(f"could not find '{text}' on screen")


def main():
    apk, out = sys.argv[1], Path(sys.argv[2])
    out.mkdir(parents=True, exist_ok=True)

    adb("install", "-r", apk)
    adb("logcat", "-c")
    adb("shell", "am", "start", "-W", "-n", f"{PACKAGE}/.LoginActivity")
    wait_for_activity("LoginActivity")
    time.sleep(2)
    screenshot(out, "01-login")

    tap_text("Try demo (no account)")
    wait_for_activity("VehicleSelectActivity")
    time.sleep(2)
    screenshot(out, "02-vehicle-select")

    tap_text("Report")
    wait_for_activity("RiskDashboardActivity")
    time.sleep(12)  # NHTSA responses arrive over the network
    assert_no_crash()
    screenshot(out, "03-risk-report")

    print("demo smoke passed")


if __name__ == "__main__":
    main()
