# -*- coding: utf-8 -*-

import os

# ==== #

result_path = "/home/zhouhao/KeepAliveWS/appalive/fdroid/system_out_3.log"
valid_number = 0 # -> number of apps has diehard behavior
diehard_apps = []

with open(result_path, "r") as f:
    lines = f.readlines()
    valid_flag = False
    for line in lines:
        if "package name:" in line:
            # handle valid_flag
            if valid_flag == True:
               valid_number += 1
               # print package_name
               diehard_apps.append(package_name)
            valid_flag = False # reset
            package_name = line.strip().replace("package name: ", "")
        if "(1) HTI found" in line:
            valid_flag = True
        if "(2) PMI found" in line:
            valid_flag = True
        if "(3) HFA found" in line:
            valid_flag = True
        if "(4) HFS found" in line:
            valid_flag = True
        if "(5) COW found" in line:
            valid_flag = True
        if "(6) BRS found" in line:
            valid_flag = True
        if "(7) ACP found" in line:
            valid_flag = True
        if "(8) CSS found" in line:
            valid_flag = True
        if "(9) MSB found" in line:
            valid_flag = True
        if "(10) LAS found" in line:
            valid_flag = True
        if "(11) UJS found" in line:
            valid_flag = True
        if "(12) MAB found" in line:
            valid_flag = True
            
# ==== #

source_root = "/home/zhouhao/KeepAliveWS/fdroid_sources"
for source_name in os.listdir(source_root):
    new_source_name = source_name.split("_")[0]
    # print source_name
    if new_source_name in diehard_apps:
        continue
    # parse Java files under the source folder
    source_folder = os.path.join(source_root, source_name)
    for file_root, file_directorys, file_names in os.walk(source_folder):
        for file_name in file_names:
            if not (file_name.endswith(".java") or file_name.endswith(".cpp") or file_name.endswith(".c")):
                continue
            file_path = file_root + "/" + file_name
            # print file_path
            with open(file_path, "r") as f:
                lines = f.readlines()
                if file_name.endswith(".java"):
                    for line in lines:
                        if "log" in line or "Log" in line or "TAG" in line or "Exception" in line or "Error" in line or "print" in line:
                            continue
                        if "\"setExcludeFromRecents" in line:
                            print file_path
                            print line
                            None
                        if "FLAG_ACTIVITY_NEW_TASK" in line:
                            #print file_path
                            #print line
                            None
                if file_name.endswith(".cpp") or file_name.endswith(".c"):
                    for line in lines:
                        if "log" in line or "Log" in line or "TAG" in line or "Exception" in line or "Error" in line or "print" in line:
                            continue
                        if "setExcludeFromRecents" in line:
                            print file_path
                            print line
                        if "FLAG_ACTIVITY_NEW_TASK" in line:
                            print file_path
                            print line
                            None
                        if "startActivit" in line:
                            print file_path
                            print line
                            None
                        if "getActivity" in line:
                            #print file_path
                            #print line
                            None
                        if "startForeground" in line:
                            print file_path
                            print line
                            None
                        if ("TYPE_PHONE" in line) or ("TYPE_SYSTEM_ALERT" in line) or ("TYPE_SYSTEM_ERROR" in line) or ("TYPE_TOAST" in line) or ("TYPE_APPLICATION_OVERLAY" in line):
                            print file_path
                            print line
                            None
                        if ("bindService" in line) or ("unbindService" in line):
                            print file_path
                            print line
                            None
                        if ("acquireUnstableContentProviderClient" in line) or ("acquireContentProviderClient" in line):
                            print file_path
                            print line
                            None
                        if "startService" in line:
                            print file_path
                            print line
                            None
                        if ("setRepeating" in line) or ("setInexactRepeating" in line):
                            print file_path
                            print line
                            None
                        if "setPeriodic" in line:
                            print file_path
                            print line
                            None
                        if "sendBroadcast" in line:
                            print file_path
                            print line
                            None
                        if "registerReceiver" in line:
                            print file_path
                            print line
                            None
                            
            
    
     
