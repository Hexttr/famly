#!/usr/bin/env python3
import os
import paramiko

host = os.environ["DEPLOY_HOST"]
user = os.environ["DEPLOY_USER"]
password = os.environ["DEPLOY_PASS"]

c = paramiko.SSHClient()
c.set_missing_host_key_policy(paramiko.AutoAddPolicy())
c.connect(host, username=user, password=password, timeout=30)

cmds = [
    "grep -r server_name /etc/nginx/sites-enabled/ 2>/dev/null | head -30",
    "cat /etc/nginx/sites-enabled/famly-api 2>/dev/null || cat /etc/nginx/sites-available/famly-api 2>/dev/null",
    "certbot certificates 2>/dev/null || true",
    "curl -sI http://api.jazz68.ru/health | head -5",
    "curl -skI https://api.jazz68.ru/health | head -10",
    "echo | openssl s_client -connect api.jazz68.ru:443 -servername api.jazz68.ru 2>/dev/null | openssl x509 -noout -subject -ext subjectAltName 2>/dev/null || true",
]
for cmd in cmds:
    print(">>>", cmd)
    _, o, e = c.exec_command(cmd)
    print(o.read().decode())
    err = e.read().decode()
    if err.strip():
        print("ERR:", err)
    print()
c.close()
