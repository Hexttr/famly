import paramiko
import os
import sys

host = os.environ["DEPLOY_HOST"]
user = os.environ["DEPLOY_USER"]
password = os.environ["DEPLOY_PASS"]

c = paramiko.SSHClient()
c.set_missing_host_key_policy(paramiko.AutoAddPolicy())
c.connect(host, username=user, password=password, timeout=20)

cmds = [
    "uname -a",
    "free -h",
    "df -h /",
    "nproc",
    "docker --version 2>&1 || echo NO_DOCKER",
    "docker compose version 2>&1 || docker-compose --version 2>&1 || echo NO_COMPOSE",
    "java -version 2>&1 | head -1 || echo NO_JAVA",
    "command -v nginx; nginx -v 2>&1 || true",
    "ls -la /etc/nginx/sites-enabled 2>/dev/null || ls -la /etc/nginx/conf.d 2>/dev/null || echo NO_NGINX_DIR",
    "ss -tlnp 2>/dev/null | head -25",
    "id; groups",
    "sudo -n true 2>&1 && echo SUDO_NOPASS || echo SUDO_NEEDS_PASS",
]

for cmd in cmds:
    _, stdout, stderr = c.exec_command(cmd)
    out = stdout.read().decode()
    err = stderr.read().decode()
    print("===", cmd, "===")
    print((out + err).strip() or "(empty)")

c.close()
