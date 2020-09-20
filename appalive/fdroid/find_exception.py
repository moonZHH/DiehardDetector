import os
import shutil

log_path = "/home/zhouhao/KeepAliveWS/appalive/fdroid/system_out_3.log"
log_file = open(log_path, "r")

pre2_line = None
pre1_line = None
cur_line = None
for line in log_file.readlines():
    line = line.replace("\n", "").strip()
    if pre1_line != None:
        pre2_line = pre1_line
    if cur_line != None:
        pre1_line = cur_line
    cur_line = line
    
    if "EXCEPTION" in cur_line:
        print pre1_line
        
        src_path = pre1_line.split(" -> ")[1].replace(".apk", "").replace("fdroid_samples", "fdroid_sources")
        if os.path.exists(src_path):
            print src_path
            dst_path = src_path.replace("fdroid_sources", "fdroid_manual")
            try:
                shutil.copytree(src_path, dst_path)
            except Exception:
                pass
        
    if "TIMEOUT:" in cur_line:
        print pre2_line
        
        src_path = pre2_line.split(" -> ")[1].replace(".apk", "").replace("fdroid_samples", "fdroid_sources")
        if os.path.exists(src_path):
            print src_path
            dst_path = src_path.replace("fdroid_sources", "fdroid_manual")
            try:
                shutil.copytree(src_path, dst_path)
            except Exception:
                pass
        
log_file.close()
