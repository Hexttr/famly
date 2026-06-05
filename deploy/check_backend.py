#!/usr/bin/env python3
"""Quick backend health + docker logs check."""
from __future__ import annotations

import os
import sys

import paramiko


def main() -> None:
    host = os.environ["DEPLOY_HOST"]
    user = os.environ["DEPLOY_USER"]
    password = os.environ["DEPLOY_PASS"]

    client = paramiko.SSHClient()
    client.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    client.connect(host, username=user, password=password, timeout=30)

    cmds = [
        "curl -sf https://api.jazz68.ru/health",
        "docker ps --filter name=famly --format '{{.Names}} {{.Status}}'",
        "docker logs famly-backend 2>&1 | tail -30",
    ]
    for cmd in cmds:
        print(f"\n>>> {cmd}")
        _, stdout, stderr = client.exec_command(cmd)
        out = stdout.read().decode(errors="replace")
        err = stderr.read().decode(errors="replace")
        sys.stdout.buffer.write(out.encode("utf-8", errors="replace"))
        if err.strip():
            sys.stdout.buffer.write(err.encode("utf-8", errors="replace"))

    client.close()


if __name__ == "__main__":
    main()
