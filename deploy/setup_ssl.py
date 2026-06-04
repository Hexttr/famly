#!/usr/bin/env python3
"""Run certbot for api.jazz68.ru on remote server."""
from __future__ import annotations

import os
import sys

import paramiko

REMOTE = "/home/user_adm/famly"


def run(client: paramiko.SSHClient, cmd: str, password: str) -> tuple[int, str, str]:
    full = f"echo '{password}' | sudo -S bash -lc {repr(cmd)}"
    _, stdout, stderr = client.exec_command(full, get_pty=True)
    out = stdout.read().decode(errors="replace")
    err = stderr.read().decode(errors="replace")
    code = stdout.channel.recv_exit_status()
    return code, out, err


def main() -> None:
    host = os.environ["DEPLOY_HOST"]
    user = os.environ["DEPLOY_USER"]
    password = os.environ["DEPLOY_PASS"]
    email = os.environ.get("CERTBOT_EMAIL", "admin@jazz68.ru")

    client = paramiko.SSHClient()
    client.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    client.connect(host, username=user, password=password, timeout=30)

    steps = [
        "curl -sf http://127.0.0.1:8080/health",
        "curl -sf http://api.jazz68.ru/health",
        "certbot certificates 2>/dev/null | grep -E 'Certificate Name|Domains|Expiry' || true",
        (
            "certbot --nginx -d api.jazz68.ru "
            f"--non-interactive --agree-tos --email {email} --redirect"
        ),
        "nginx -t",
        "systemctl reload nginx",
        "curl -sf https://api.jazz68.ru/health",
        "curl -sf -o /dev/null -w '%{http_code}' https://api.jazz68.ru/legal/privacy",
    ]

    for step in steps:
        print(f"\n>>> {step}")
        code, out, err = run(client, step, password)
        sys.stdout.buffer.write(out.encode("utf-8", errors="replace"))
        sys.stdout.buffer.write(b"\n")
        if err.strip():
            sys.stdout.buffer.write(err.encode("utf-8", errors="replace"))
            sys.stdout.buffer.write(b"\n")
        if code != 0:
            print(f"FAILED (exit {code})", file=sys.stderr)
            client.close()
            raise SystemExit(1)

    client.close()
    print("\nSSL_OK")


if __name__ == "__main__":
    main()
