#!/usr/bin/env python3
import os
import paramiko

password = os.environ["DEPLOY_PASS"]
client = paramiko.SSHClient()
client.set_missing_host_key_policy(paramiko.AutoAddPolicy())
client.connect("178.170.165.78", username="user_adm", password=password, timeout=20)

cmd = (
    "curl -s -X POST http://127.0.0.1:8080/auth/register "
    "-H 'Content-Type: application/json' "
    "-d '{\"email\":\"fixedtest@example.com\",\"password\":\"pass123\",\"displayName\":\"Fixed\"}'"
)
_, stdout, stderr = client.exec_command(cmd)
print(stdout.read().decode())
print(stderr.read().decode())
client.close()
