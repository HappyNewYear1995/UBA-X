import sys, paramiko

HOST = "120.55.38.71"
USER = "root"
PW = "happy@0220"

def client():
    c = paramiko.SSHClient()
    c.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    c.connect(HOST, username=USER, password=PW, timeout=30, banner_timeout=30, auth_timeout=30)
    return c

def run(cmd, timeout=1200):
    c = client()
    stdin, stdout, stderr = c.exec_command(cmd, timeout=timeout)
    out = stdout.read().decode("utf-8", "replace")
    err = stderr.read().decode("utf-8", "replace")
    code = stdout.channel.recv_exit_status()
    c.close()
    return code, out, err

def upload(local, remote):
    c = client()
    sftp = c.open_sftp()
    sftp.put(local, remote)
    sftp.close()
    c.close()

if __name__ == "__main__":
    mode = sys.argv[1]
    if mode == "run":
        code, out, err = run(sys.argv[2])
        print("EXIT", code)
        if out: print(out)
        if err: print("STDERR:", err)
    elif mode == "upload":
        upload(sys.argv[2], sys.argv[3])
        print("uploaded", sys.argv[2], "->", sys.argv[3])
