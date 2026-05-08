#!/usr/bin/env python3
"""
Vertical Slice Architecture refactoring for web (React) frontend.
Moves pages to features/ and components to shared/components/.
Updates import paths in all JS files.
"""
import os, re, shutil

BASE = os.path.join(os.path.dirname(__file__), "web", "src")

# (source_rel, dest_rel)
FILE_MAP = [
    ("pages/Login.js",      "features/auth/Login.js"),
    ("pages/Register.js",   "features/auth/Register.js"),
    ("pages/Tasks.js",      "features/tasks/Tasks.js"),
    ("pages/Groups.js",     "features/groups/Groups.js"),
    ("pages/Files.js",      "features/files/Files.js"),
    ("pages/Dashboard.js",  "features/dashboard/Dashboard.js"),
    ("pages/Settings.js",   "features/settings/Settings.js"),
    ("components/Layout.js","shared/components/Layout.js"),
    ("components/Toast.js", "shared/components/Toast.js"),
    ("api.js",              "shared/api.js"),
]

# Import path replacements to apply in ALL JS files (including App.js)
IMPORT_REPLACEMENTS = [
    ("./pages/Login",       "./features/auth/Login"),
    ("./pages/Register",    "./features/auth/Register"),
    ("./pages/Tasks",       "./features/tasks/Tasks"),
    ("./pages/Groups",      "./features/groups/Groups"),
    ("./pages/Files",       "./features/files/Files"),
    ("./pages/Dashboard",   "./features/dashboard/Dashboard"),
    ("./pages/Settings",    "./features/settings/Settings"),
    ("./components/Layout", "./shared/components/Layout"),
    ("./components/Toast",  "./shared/components/Toast"),
    ("../components/Layout","../shared/components/Layout"),
    ("../components/Toast", "../shared/components/Toast"),
    ("../api",              "../shared/api"),
    ("../../api",           "../../shared/api"),
    ("./api",               "./shared/api"),
]

# Depth-aware relative import fixes for moved files
# When a file moves from pages/ to features/X/, relative imports to api.js shift
# Each moved page now needs ../../shared/api (was ../api)
PAGE_API_FIX = {
    "features/auth/Login.js":        ("../api",  "../../shared/api"),
    "features/auth/Register.js":     ("../api",  "../../shared/api"),
    "features/tasks/Tasks.js":       ("../api",  "../../shared/api"),
    "features/groups/Groups.js":     ("../api",  "../../shared/api"),
    "features/files/Files.js":       ("../api",  "../../shared/api"),
    "features/dashboard/Dashboard.js": ("../api","../../shared/api"),
    "features/settings/Settings.js": ("../api",  "../../shared/api"),
    "shared/components/Layout.js":   ("../api",  "../api"),   # no change needed
}

PAGE_LAYOUT_FIX = {
    "features/auth/Login.js":        ("../components/Layout",  "../../shared/components/Layout"),
    "features/auth/Register.js":     ("../components/Layout",  "../../shared/components/Layout"),
    "features/tasks/Tasks.js":       ("../components/Layout",  "../../shared/components/Layout"),
    "features/groups/Groups.js":     ("../components/Layout",  "../../shared/components/Layout"),
    "features/files/Files.js":       ("../components/Layout",  "../../shared/components/Layout"),
    "features/dashboard/Dashboard.js":("../components/Layout","../../shared/components/Layout"),
    "features/settings/Settings.js": ("../components/Layout",  "../../shared/components/Layout"),
    "features/auth/Login.js:toast":  ("../components/Toast",   "../../shared/components/Toast"),
    "features/auth/Register.js:toast":("../components/Toast",  "../../shared/components/Toast"),
}

def fix_imports(content, dst_rel):
    # Fix api imports
    if dst_rel in PAGE_API_FIX:
        old, new = PAGE_API_FIX[dst_rel]
        content = content.replace(f'"{old}"', f'"{new}"').replace(f"'{old}'", f"'{new}'")

    # Fix Layout imports (features/X/Y.js → ../../shared/components/Layout)
    for key in PAGE_LAYOUT_FIX:
        base_dst = key.split(":")[0]
        if dst_rel == base_dst:
            old, new = PAGE_LAYOUT_FIX[key]
            content = content.replace(f'"{old}"', f'"{new}"').replace(f"'{old}'", f"'{new}'")

    return content

def fix_app_js():
    app_path = os.path.join(BASE, "App.js")
    with open(app_path, "r", encoding="utf-8-sig") as f:
        content = f.read()
    for old, new in IMPORT_REPLACEMENTS:
        content = content.replace(f'"{old}"', f'"{new}"').replace(f"'{old}'", f"'{new}'")
    with open(app_path, "w", encoding="utf-8", newline="\n") as f:
        f.write(content)
    print("  Updated App.js imports")

def main():
    for src_rel, dst_rel in FILE_MAP:
        src_path = os.path.join(BASE, src_rel.replace("/", os.sep))
        dst_path = os.path.join(BASE, dst_rel.replace("/", os.sep))
        os.makedirs(os.path.dirname(dst_path), exist_ok=True)

        with open(src_path, "r", encoding="utf-8-sig") as f:
            content = f.read()
        content = fix_imports(content, dst_rel)
        with open(dst_path, "w", encoding="utf-8", newline="\n") as f:
            f.write(content)
        print(f"  {src_rel} → {dst_rel}")

    fix_app_js()

    # Remove old directories
    for old_dir in ["pages", "components"]:
        dir_path = os.path.join(BASE, old_dir)
        if os.path.exists(dir_path):
            shutil.rmtree(dir_path)
            print(f"  Removed: {old_dir}/")
    # Remove old api.js (now at shared/api.js)
    old_api = os.path.join(BASE, "api.js")
    if os.path.exists(old_api):
        os.remove(old_api)
        print("  Removed: api.js (moved to shared/api.js)")

    print(f"\nDone! Web frontend refactored to vertical slice structure.")

if __name__ == "__main__":
    main()
