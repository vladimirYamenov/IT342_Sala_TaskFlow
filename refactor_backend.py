#!/usr/bin/env python3
"""
Vertical Slice Architecture refactoring script for TaskFlow backend.
Moves Java files from layer-based packages to feature-based packages.
"""
import os, re, shutil

BASE = os.path.join(os.path.dirname(__file__),
    "Backend", "TaskFlow", "src", "main", "java", "edu", "cit", "sala", "TaskFlow")

# --- Import replacement map (most-specific first to avoid partial matches) ---
IMPORT_REPLACEMENTS = [
    # entity
    ("edu.cit.sala.TaskFlow.entity.GroupMember",           "edu.cit.sala.TaskFlow.feature.group.GroupMember"),
    ("edu.cit.sala.TaskFlow.entity.Group",                 "edu.cit.sala.TaskFlow.feature.group.Group"),
    ("edu.cit.sala.TaskFlow.entity.FileEntity",            "edu.cit.sala.TaskFlow.feature.file.FileEntity"),
    ("edu.cit.sala.TaskFlow.entity.Task",                  "edu.cit.sala.TaskFlow.feature.task.Task"),
    ("edu.cit.sala.TaskFlow.entity.User",                  "edu.cit.sala.TaskFlow.feature.auth.User"),
    # repository
    ("edu.cit.sala.TaskFlow.repository.GroupMemberRepository", "edu.cit.sala.TaskFlow.feature.group.GroupMemberRepository"),
    ("edu.cit.sala.TaskFlow.repository.GroupRepository",       "edu.cit.sala.TaskFlow.feature.group.GroupRepository"),
    ("edu.cit.sala.TaskFlow.repository.FileRepository",        "edu.cit.sala.TaskFlow.feature.file.FileRepository"),
    ("edu.cit.sala.TaskFlow.repository.TaskRepository",        "edu.cit.sala.TaskFlow.feature.task.TaskRepository"),
    ("edu.cit.sala.TaskFlow.repository.UserRepository",        "edu.cit.sala.TaskFlow.feature.auth.UserRepository"),
    # service
    ("edu.cit.sala.TaskFlow.service.EmailService",         "edu.cit.sala.TaskFlow.shared.email.EmailService"),
    ("edu.cit.sala.TaskFlow.service.GoogleAuthService",    "edu.cit.sala.TaskFlow.feature.auth.GoogleAuthService"),
    ("edu.cit.sala.TaskFlow.service.AuthService",          "edu.cit.sala.TaskFlow.feature.auth.AuthService"),
    ("edu.cit.sala.TaskFlow.service.FileService",          "edu.cit.sala.TaskFlow.feature.file.FileService"),
    ("edu.cit.sala.TaskFlow.service.TaskService",          "edu.cit.sala.TaskFlow.feature.task.TaskService"),
    ("edu.cit.sala.TaskFlow.service.GroupService",         "edu.cit.sala.TaskFlow.feature.group.GroupService"),
    # dto
    ("edu.cit.sala.TaskFlow.dto.ApiErrorResponse",         "edu.cit.sala.TaskFlow.shared.error.ApiErrorResponse"),
    ("edu.cit.sala.TaskFlow.dto.AddMemberRequest",         "edu.cit.sala.TaskFlow.feature.group.AddMemberRequest"),
    ("edu.cit.sala.TaskFlow.dto.GroupResponse",            "edu.cit.sala.TaskFlow.feature.group.GroupResponse"),
    ("edu.cit.sala.TaskFlow.dto.GroupRequest",             "edu.cit.sala.TaskFlow.feature.group.GroupRequest"),
    ("edu.cit.sala.TaskFlow.dto.FileResponse",             "edu.cit.sala.TaskFlow.feature.file.FileResponse"),
    ("edu.cit.sala.TaskFlow.dto.TaskResponse",             "edu.cit.sala.TaskFlow.feature.task.TaskResponse"),
    ("edu.cit.sala.TaskFlow.dto.TaskRequest",              "edu.cit.sala.TaskFlow.feature.task.TaskRequest"),
    ("edu.cit.sala.TaskFlow.dto.GoogleAuthRequest",        "edu.cit.sala.TaskFlow.feature.auth.GoogleAuthRequest"),
    ("edu.cit.sala.TaskFlow.dto.AuthResponse",             "edu.cit.sala.TaskFlow.feature.auth.AuthResponse"),
    ("edu.cit.sala.TaskFlow.dto.RegisterRequest",          "edu.cit.sala.TaskFlow.feature.auth.RegisterRequest"),
    ("edu.cit.sala.TaskFlow.dto.LoginRequest",             "edu.cit.sala.TaskFlow.feature.auth.LoginRequest"),
    # config (use prefix so all config classes get updated)
    ("edu.cit.sala.TaskFlow.config.",                      "edu.cit.sala.TaskFlow.shared.config."),
]

# --- File → (destination sub-path, old package, new package) ---
FILE_MAP = [
    # Controllers
    ("controller/AuthController.java",   "feature/auth/AuthController.java",   "edu.cit.sala.TaskFlow.controller", "edu.cit.sala.TaskFlow.feature.auth"),
    ("controller/TaskController.java",   "feature/task/TaskController.java",   "edu.cit.sala.TaskFlow.controller", "edu.cit.sala.TaskFlow.feature.task"),
    ("controller/GroupController.java",  "feature/group/GroupController.java", "edu.cit.sala.TaskFlow.controller", "edu.cit.sala.TaskFlow.feature.group"),
    ("controller/FileController.java",   "feature/file/FileController.java",   "edu.cit.sala.TaskFlow.controller", "edu.cit.sala.TaskFlow.feature.file"),
    # Services
    ("service/AuthService.java",         "feature/auth/AuthService.java",      "edu.cit.sala.TaskFlow.service",    "edu.cit.sala.TaskFlow.feature.auth"),
    ("service/GoogleAuthService.java",   "feature/auth/GoogleAuthService.java","edu.cit.sala.TaskFlow.service",    "edu.cit.sala.TaskFlow.feature.auth"),
    ("service/TaskService.java",         "feature/task/TaskService.java",      "edu.cit.sala.TaskFlow.service",    "edu.cit.sala.TaskFlow.feature.task"),
    ("service/GroupService.java",        "feature/group/GroupService.java",    "edu.cit.sala.TaskFlow.service",    "edu.cit.sala.TaskFlow.feature.group"),
    ("service/FileService.java",         "feature/file/FileService.java",      "edu.cit.sala.TaskFlow.service",    "edu.cit.sala.TaskFlow.feature.file"),
    ("service/EmailService.java",        "shared/email/EmailService.java",     "edu.cit.sala.TaskFlow.service",    "edu.cit.sala.TaskFlow.shared.email"),
    # Entities
    ("entity/User.java",                 "feature/auth/User.java",             "edu.cit.sala.TaskFlow.entity",     "edu.cit.sala.TaskFlow.feature.auth"),
    ("entity/Task.java",                 "feature/task/Task.java",             "edu.cit.sala.TaskFlow.entity",     "edu.cit.sala.TaskFlow.feature.task"),
    ("entity/Group.java",                "feature/group/Group.java",           "edu.cit.sala.TaskFlow.entity",     "edu.cit.sala.TaskFlow.feature.group"),
    ("entity/GroupMember.java",          "feature/group/GroupMember.java",     "edu.cit.sala.TaskFlow.entity",     "edu.cit.sala.TaskFlow.feature.group"),
    ("entity/FileEntity.java",           "feature/file/FileEntity.java",       "edu.cit.sala.TaskFlow.entity",     "edu.cit.sala.TaskFlow.feature.file"),
    # Repositories
    ("repository/UserRepository.java",   "feature/auth/UserRepository.java",   "edu.cit.sala.TaskFlow.repository", "edu.cit.sala.TaskFlow.feature.auth"),
    ("repository/TaskRepository.java",   "feature/task/TaskRepository.java",   "edu.cit.sala.TaskFlow.repository", "edu.cit.sala.TaskFlow.feature.task"),
    ("repository/GroupRepository.java",  "feature/group/GroupRepository.java", "edu.cit.sala.TaskFlow.repository", "edu.cit.sala.TaskFlow.feature.group"),
    ("repository/GroupMemberRepository.java", "feature/group/GroupMemberRepository.java", "edu.cit.sala.TaskFlow.repository", "edu.cit.sala.TaskFlow.feature.group"),
    ("repository/FileRepository.java",   "feature/file/FileRepository.java",   "edu.cit.sala.TaskFlow.repository", "edu.cit.sala.TaskFlow.feature.file"),
    # DTOs
    ("dto/AuthResponse.java",            "feature/auth/AuthResponse.java",     "edu.cit.sala.TaskFlow.dto",        "edu.cit.sala.TaskFlow.feature.auth"),
    ("dto/LoginRequest.java",            "feature/auth/LoginRequest.java",     "edu.cit.sala.TaskFlow.dto",        "edu.cit.sala.TaskFlow.feature.auth"),
    ("dto/RegisterRequest.java",         "feature/auth/RegisterRequest.java",  "edu.cit.sala.TaskFlow.dto",        "edu.cit.sala.TaskFlow.feature.auth"),
    ("dto/GoogleAuthRequest.java",       "feature/auth/GoogleAuthRequest.java","edu.cit.sala.TaskFlow.dto",        "edu.cit.sala.TaskFlow.feature.auth"),
    ("dto/TaskRequest.java",             "feature/task/TaskRequest.java",      "edu.cit.sala.TaskFlow.dto",        "edu.cit.sala.TaskFlow.feature.task"),
    ("dto/TaskResponse.java",            "feature/task/TaskResponse.java",     "edu.cit.sala.TaskFlow.dto",        "edu.cit.sala.TaskFlow.feature.task"),
    ("dto/GroupRequest.java",            "feature/group/GroupRequest.java",    "edu.cit.sala.TaskFlow.dto",        "edu.cit.sala.TaskFlow.feature.group"),
    ("dto/GroupResponse.java",           "feature/group/GroupResponse.java",   "edu.cit.sala.TaskFlow.dto",        "edu.cit.sala.TaskFlow.feature.group"),
    ("dto/AddMemberRequest.java",        "feature/group/AddMemberRequest.java","edu.cit.sala.TaskFlow.dto",        "edu.cit.sala.TaskFlow.feature.group"),
    ("dto/FileResponse.java",            "feature/file/FileResponse.java",     "edu.cit.sala.TaskFlow.dto",        "edu.cit.sala.TaskFlow.feature.file"),
    ("dto/ApiErrorResponse.java",        "shared/error/ApiErrorResponse.java", "edu.cit.sala.TaskFlow.dto",        "edu.cit.sala.TaskFlow.shared.error"),
    # Config
    ("config/SecurityConfig.java",           "shared/config/SecurityConfig.java",           "edu.cit.sala.TaskFlow.config", "edu.cit.sala.TaskFlow.shared.config"),
    ("config/JwtAuthenticationFilter.java",  "shared/config/JwtAuthenticationFilter.java",  "edu.cit.sala.TaskFlow.config", "edu.cit.sala.TaskFlow.shared.config"),
    ("config/JwtUtil.java",                  "shared/config/JwtUtil.java",                  "edu.cit.sala.TaskFlow.config", "edu.cit.sala.TaskFlow.shared.config"),
    ("config/GlobalExceptionHandler.java",   "shared/config/GlobalExceptionHandler.java",   "edu.cit.sala.TaskFlow.config", "edu.cit.sala.TaskFlow.shared.config"),
]

def transform(content, old_pkg, new_pkg):
    # 1. Replace package declaration
    content = content.replace(f"package {old_pkg};", f"package {new_pkg};", 1)
    # 2. Apply all import replacements (word-boundary safe via exact string match)
    for old, new in IMPORT_REPLACEMENTS:
        content = content.replace(old, new)
    return content

def main():
    # Create destination directories
    for _, dst, _, _ in FILE_MAP:
        dst_dir = os.path.join(BASE, os.path.dirname(dst).replace("/", os.sep))
        os.makedirs(dst_dir, exist_ok=True)

    moved = []
    for src_rel, dst_rel, old_pkg, new_pkg in FILE_MAP:
        src_path = os.path.join(BASE, src_rel.replace("/", os.sep))
        dst_path = os.path.join(BASE, dst_rel.replace("/", os.sep))

        with open(src_path, "r", encoding="utf-8-sig") as f:   # utf-8-sig strips BOM if present
            content = f.read()

        content = transform(content, old_pkg, new_pkg)

        with open(dst_path, "w", encoding="utf-8", newline="\n") as f:  # LF line endings
            f.write(content)

        print(f"  {src_rel} → {dst_rel}")
        moved.append(src_rel)

    # Remove old layer-based directories
    for old_dir in ["controller", "service", "entity", "repository", "dto", "config"]:
        dir_path = os.path.join(BASE, old_dir)
        if os.path.exists(dir_path):
            shutil.rmtree(dir_path)
            print(f"  Removed: {old_dir}/")

    print(f"\nDone! {len(moved)} files migrated to vertical slice structure.")

if __name__ == "__main__":
    main()
