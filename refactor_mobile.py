#!/usr/bin/env python3
"""
Vertical Slice Architecture refactoring for Android mobile (Kotlin).
"""
import os, shutil

BASE = os.path.join(os.path.dirname(__file__),
    "mobile", "app", "src", "main", "java", "com", "example", "mobile")

FILE_MAP = [
    # source_rel, dest_rel, old_package, new_package
    ("API/AuthApiService.kt",       "feature/auth/AuthApiService.kt",  "com.example.mobile.API",           "com.example.mobile.feature.auth"),
    ("model/AuthResponse.kt",       "feature/auth/AuthResponse.kt",    "com.example.mobile.model",         "com.example.mobile.feature.auth"),
    ("model/LoginRequest.kt",       "feature/auth/LoginRequest.kt",    "com.example.mobile.model",         "com.example.mobile.feature.auth"),
    ("model/RegisterRequest.kt",    "feature/auth/RegisterRequest.kt", "com.example.mobile.model",         "com.example.mobile.feature.auth"),
    ("UserInterface/LoginActivity.kt",    "feature/auth/LoginActivity.kt",    "com.example.mobile.UserInterface", "com.example.mobile.feature.auth"),
    ("UserInterface/RegisterActivity.kt", "feature/auth/RegisterActivity.kt", "com.example.mobile.UserInterface", "com.example.mobile.feature.auth"),
    ("API/ApiClient.kt",            "shared/api/ApiClient.kt",         "com.example.mobile.API",           "com.example.mobile.shared.api"),
]

IMPORT_REPLACEMENTS = [
    ("com.example.mobile.API.ApiClient",       "com.example.mobile.shared.api.ApiClient"),
    ("com.example.mobile.API.AuthApiService",  "com.example.mobile.feature.auth.AuthApiService"),
    ("com.example.mobile.API.",                "com.example.mobile.shared.api."),
    ("com.example.mobile.model.AuthResponse",  "com.example.mobile.feature.auth.AuthResponse"),
    ("com.example.mobile.model.LoginRequest",  "com.example.mobile.feature.auth.LoginRequest"),
    ("com.example.mobile.model.RegisterRequest","com.example.mobile.feature.auth.RegisterRequest"),
    ("com.example.mobile.model.",              "com.example.mobile.feature.auth."),
    ("com.example.mobile.UserInterface.",      "com.example.mobile.feature.auth."),
]

def transform(content, old_pkg, new_pkg):
    content = content.replace(f"package {old_pkg}", f"package {new_pkg}", 1)
    for old, new in IMPORT_REPLACEMENTS:
        content = content.replace(old, new)
    return content

def main():
    for src_rel, dst_rel, old_pkg, new_pkg in FILE_MAP:
        src_path = os.path.join(BASE, src_rel.replace("/", os.sep))
        dst_path = os.path.join(BASE, dst_rel.replace("/", os.sep))
        os.makedirs(os.path.dirname(dst_path), exist_ok=True)
        with open(src_path, "r", encoding="utf-8-sig") as f:
            content = f.read()
        content = transform(content, old_pkg, new_pkg)
        with open(dst_path, "w", encoding="utf-8", newline="\n") as f:
            f.write(content)
        print(f"  {src_rel} → {dst_rel}")

    for old_dir in ["API", "model", "UserInterface"]:
        dir_path = os.path.join(BASE, old_dir)
        if os.path.exists(dir_path):
            shutil.rmtree(dir_path)
            print(f"  Removed: {old_dir}/")

    print("\nDone! Mobile refactored to vertical slice structure.")

if __name__ == "__main__":
    main()
