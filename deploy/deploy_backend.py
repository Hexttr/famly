#!/usr/bin/env python3
"""Deploy Famly backend to remote server. Requires env: DEPLOY_HOST, DEPLOY_USER, DEPLOY_PASS."""
from __future__ import annotations

import io
import os
import secrets
import tarfile
from pathlib import Path

import paramiko

ROOT = Path(__file__).resolve().parents[1]
REMOTE = "/home/user_adm/famly"
INCLUDE = [
    "backend",
    "docker/Dockerfile.backend",
    "deploy/docker-compose.prod.yml",
    "deploy/nginx-api-jazz68.ru.conf",
]


def make_tarball() -> bytes:
    buf = io.BytesIO()
    with tarfile.open(fileobj=buf, mode="w:gz") as tar:
        for rel in INCLUDE:
            path = ROOT / rel
            if path.is_dir():
                for file in path.rglob("*"):
                    if not file.is_file():
                        continue
                    if any(p in file.parts for p in ("build", ".gradle", ".kotlin")):
                        continue
                    tar.add(file, arcname=file.relative_to(ROOT).as_posix())
            elif path.is_file():
                tar.add(path, arcname=rel.replace("\\", "/"))
    buf.seek(0)
    return buf.read()


def main() -> None:
    host = os.environ["DEPLOY_HOST"]
    user = os.environ["DEPLOY_USER"]
    password = os.environ["DEPLOY_PASS"]
    jwt = os.environ.get("JWT_SECRET") or secrets.token_hex(32)

    client = paramiko.SSHClient()
    client.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    client.connect(host, username=user, password=password, timeout=30)

    print("Uploading sources...")
    tarball = make_tarball()
    sftp = client.open_sftp()
    client.exec_command(f"mkdir -p {REMOTE}")
    with sftp.file(f"{REMOTE}/famly-src.tar.gz", "wb") as f:
        f.write(tarball)

    remote_sh = f"""#!/bin/bash
set -euo pipefail
cd {REMOTE}
tar -xzf famly-src.tar.gz
printf 'JWT_SECRET=%s\\n' '{jwt}' > .env
echo '{password}' | sudo -S docker compose --env-file .env -f deploy/docker-compose.prod.yml build
echo '{password}' | sudo -S docker compose --env-file .env -f deploy/docker-compose.prod.yml up -d --remove-orphans
sleep 4
curl -sf http://127.0.0.1:8080/health
echo '{password}' | sudo -S cp deploy/nginx-api-jazz68.ru.conf /etc/nginx/sites-available/famly-api
echo '{password}' | sudo -S ln -sf /etc/nginx/sites-available/famly-api /etc/nginx/sites-enabled/famly-api
echo '{password}' | sudo -S nginx -t
echo '{password}' | sudo -S systemctl reload nginx
echo DEPLOY_OK
"""
    with sftp.file(f"{REMOTE}/deploy.sh", "w") as f:
        f.write(remote_sh)
    sftp.close()

    print("Building and starting backend (5–10 min)...")
    _, stdout, stderr = client.exec_command(f"bash {REMOTE}/deploy.sh", get_pty=True)
    output = stdout.read().decode(errors="replace")
    err = stderr.read().decode(errors="replace")
    import sys
    sys.stdout.buffer.write(output.encode("utf-8", errors="replace"))
    sys.stdout.buffer.write(b"\n")
    if err.strip():
        print(err)
    client.close()

    if "DEPLOY_OK" not in output:
        raise SystemExit("Deploy failed — see output above")
    print("\nBackend deployed. Configure DNS api.jazz68.ru then run certbot.")


if __name__ == "__main__":
    main()
