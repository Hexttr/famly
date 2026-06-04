#!/usr/bin/env python3
import json
import os
import urllib.error
import urllib.request

import paramiko

API = "https://api.jazz68.ru/auth/register"
body = json.dumps(
    {"email": "diag_test@example.com", "password": "pass12345", "displayName": "Diag"}
).encode()

print("=== HTTPS register ===")
req = urllib.request.Request(
    API,
    data=body,
    headers={"Content-Type": "application/json"},
    method="POST",
)
try:
    with urllib.request.urlopen(req) as resp:
        print(resp.status, resp.read().decode())
except urllib.error.HTTPError as e:
    print(e.code, repr(e.read().decode()))

host = os.environ.get("DEPLOY_HOST", "178.170.165.78")
user = os.environ.get("DEPLOY_USER", "user_adm")
password = os.environ["DEPLOY_PASS"]

client = paramiko.SSHClient()
client.set_missing_host_key_policy(paramiko.AutoAddPolicy())
client.connect(host, username=user, password=password, timeout=20)

cmds = [
    "curl -s -w '\\nCODE:%{http_code}' -X POST http://127.0.0.1:8080/auth/register "
    "-H 'Content-Type: application/json' "
    "-d '{\"email\":\"srvtest2@example.com\",\"password\":\"pass123\",\"displayName\":\"Srv\"}'",
    "echo '---' | sudo -S docker logs famly-backend 2>&1 | tail -40",
]
for cmd in cmds:
    full = f"echo '{password}' | {cmd}" if "docker logs" in cmd else cmd
    print(f"\n=== {cmd[:70]} ===")
    _, stdout, stderr = client.exec_command(full, get_pty=True)
    out = stdout.read().decode(errors="replace")
    err = stderr.read().decode(errors="replace")
    print(out)
    if err.strip():
        print(err)

client.close()
