#!/usr/bin/env python3
"""Deploy Famly backend to remote server. Requires env: DEPLOY_HOST, DEPLOY_USER, DEPLOY_PASS."""
from __future__ import annotations

import io
import os
import shlex
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
    "deploy/nginx-rate-limit.conf",
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
    jwt_secret = os.environ.get("JWT_SECRET", "")
    admin_email = os.environ.get("ADMIN_EMAIL", "")
    admin_password = os.environ.get("ADMIN_PASSWORD", "")

    client = paramiko.SSHClient()
    client.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    client.connect(host, username=user, password=password, timeout=30)

    print("Uploading sources...")
    tarball = make_tarball()
    sftp = client.open_sftp()
    client.exec_command(f"mkdir -p {REMOTE}")
    with sftp.file(f"{REMOTE}/famly-src.tar.gz", "wb") as f:
        f.write(tarball)

    email = os.environ.get("CERTBOT_EMAIL", "admin@jazz68.ru")
    remote_sh = f"""#!/bin/bash
set -euo pipefail
cd {REMOTE}
tar -xzf famly-src.tar.gz
touch .env
update_env() {{
  local key="$1"
  local val="$2"
  if [ -z "$val" ]; then return; fi
  grep -v "^${{key}}=" .env > .env.tmp 2>/dev/null || true
  mv .env.tmp .env 2>/dev/null || true
  printf '%s=%s\\n' "$key" "$val" >> .env
}}
if [ -f .env ]; then set -a; source .env 2>/dev/null || true; set +a; fi
DEPLOY_JWT='{jwt_secret}'
if [ -n "$DEPLOY_JWT" ]; then
  update_env JWT_SECRET "$DEPLOY_JWT"
elif [ -z "${{JWT_SECRET:-}}" ]; then
  update_env JWT_SECRET "$(openssl rand -hex 32)"
fi
update_env ADMIN_EMAIL {shlex.quote(admin_email)}
update_env ADMIN_PASSWORD {shlex.quote(admin_password)}
update_env MONETIZATION_ENABLED "${{MONETIZATION_ENABLED:-false}}"
echo '{password}' | sudo -S docker compose --env-file .env -f deploy/docker-compose.prod.yml build
echo '{password}' | sudo -S docker compose --env-file .env -f deploy/docker-compose.prod.yml up -d --remove-orphans
sleep 4
curl -sf http://127.0.0.1:8080/health
echo '{password}' | sudo -S cp deploy/nginx-api-jazz68.ru.conf /etc/nginx/sites-available/famly-api
echo '{password}' | sudo -S cp deploy/nginx-rate-limit.conf /etc/nginx/conf.d/famly-rate-limit.conf
echo '{password}' | sudo -S ln -sf /etc/nginx/sites-available/famly-api /etc/nginx/sites-enabled/famly-api
if [ ! -f /etc/letsencrypt/live/api.jazz68.ru/fullchain.pem ]; then
  echo '{password}' | sudo -S certbot --nginx -d api.jazz68.ru --non-interactive --agree-tos --email {email} --redirect || true
else
  echo '{password}' | sudo -S certbot --nginx -d api.jazz68.ru --non-interactive --agree-tos --email {email} --redirect
fi
echo '{password}' | sudo -S nginx -t
echo '{password}' | sudo -S systemctl reload nginx
curl -sf https://api.jazz68.ru/health
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
