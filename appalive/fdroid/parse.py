# -*- coding: utf-8 -*-

result_path = "/home/zhouhao/KeepAliveWS/appalive/fdroid/system_out_3.log"
#result_path = "/home/zhouhao/KeepAliveWS/appalive/fdroid/system_out_4.log"

total_number = 0 # -> total number of analyzed apps
valid_number = 0 # -> number of apps has diehard behavior
b01_number = 0
b02_number = 0
b03_number = 0
b04_number = 0
b05_number = 0
b06_number = 0
b07_number = 0
b08_number = 0
b09_number = 0
b10_number = 0
b11_number = 0
b12_number = 0
s01_number = 0
s02_number = 0
s03_number = 0
s04_number = 0
s05_number = 0
s06_number = 0
s07_number = 0
s08_number = 0
s09_number = 0
s10_number = 0
s11_number = 0
s12_number = 0

with open(result_path, "r") as f:
    lines = f.readlines()
    s_count = 0
    valid_flag = False
    for line in lines:
        if "package name:" in line:
            total_number += 1
            # handle valid_flag
            if valid_flag == True:
               valid_number += 1
            valid_flag = False # reset
            # handle s_count
            if s_count == 1:
                s01_number += 1
            if s_count == 2:
                s02_number += 1
            if s_count == 3:
                s03_number += 1
            if s_count == 4:
                s04_number += 1
            if s_count == 5:
                s05_number += 1
            if s_count == 6:
                s06_number += 1
            if s_count == 7:
                s07_number += 1
            if s_count == 8:
                s08_number += 1
            if s_count == 9:
                s09_number += 1
            if s_count == 10:
                s10_number += 1
            if s_count == 11:
                s11_number += 1
            if s_count == 12:
                s12_number += 1
            s_count = 0 # reset
        if "(1) HTI found" in line:
            valid_flag = True
            b01_number += 1
            s_count += 1
        if "(2) PMI found" in line:
            valid_flag = True
            b02_number += 1
            s_count += 1
        if "(3) HFA found" in line:
            valid_flag = True
            b03_number += 1
            s_count += 1
        if "(4) HFS found" in line:
            valid_flag = True
            b04_number += 1
            s_count += 1
        if "(5) COW found" in line:
            valid_flag = True
            b05_number += 1
            s_count += 1
        if "(6) BRS found" in line:
            valid_flag = True
            b06_number += 1
            s_count += 1
        if "(7) ACP found" in line:
            valid_flag = True
            b07_number += 1
            s_count += 1
        if "(8) CSS found" in line:
            valid_flag = True
            b08_number += 1
            s_count += 1
        if "(9) MSB found" in line:
            valid_flag = True
            b09_number += 1
            s_count += 1
        if "(10) LAS found" in line:
            valid_flag = True
            b10_number += 1
            s_count += 1
        if "(11) UJS found" in line:
            valid_flag = True
            b11_number += 1
            s_count += 1
        if "(12) MAB found" in line:
            valid_flag = True
            b12_number += 1
            s_count += 1

#total_number = 2014
print "total_number -> " + str(total_number)
print "valid_number -> " + str(valid_number)
print "-- -- -- -- -- -- -- -- -- --"
print "b01_number -> " + str(b01_number) + ", " + str(1.0 * b01_number / total_number * 100)
print "b02_number -> " + str(b02_number) + ", " + str(1.0 * b02_number / total_number * 100)
print "b03_number -> " + str(b03_number) + ", " + str(1.0 * b03_number / total_number * 100)
print "b04_number -> " + str(b04_number) + ", " + str(1.0 * b04_number / total_number * 100)
print "b05_number -> " + str(b05_number) + ", " + str(1.0 * b05_number / total_number * 100)
print "b06_number -> " + str(b06_number) + ", " + str(1.0 * b06_number / total_number * 100)
print "b07_number -> " + str(b07_number) + ", " + str(1.0 * b07_number / total_number * 100)
print "b08_number -> " + str(b08_number) + ", " + str(1.0 * b08_number / total_number * 100)
print "b09_number -> " + str(b09_number) + ", " + str(1.0 * b09_number / total_number * 100)
print "b10_number -> " + str(b10_number) + ", " + str(1.0 * b10_number / total_number * 100)
print "b11_number -> " + str(b11_number) + ", " + str(1.0 * b11_number / total_number * 100)
print "b12_number -> " + str(b12_number) + ", " + str(1.0 * b12_number / total_number * 100)
print "-- -- -- -- -- -- -- -- -- --"
print "s01_number -> " + str(s01_number) + ", " + str(1.0 * s01_number / valid_number * 100)
print "s02_number -> " + str(s02_number) + ", " + str(1.0 * s02_number / valid_number * 100)
print "s03_number -> " + str(s03_number) + ", " + str(1.0 * s03_number / valid_number * 100)
print "s04_number -> " + str(s04_number) + ", " + str(1.0 * s04_number / valid_number * 100)
print "s05_number -> " + str(s05_number) + ", " + str(1.0 * s05_number / valid_number * 100)
print "s06_number -> " + str(s06_number) + ", " + str(1.0 * s06_number / valid_number * 100)
print "s07_number -> " + str(s07_number) + ", " + str(1.0 * s07_number / valid_number * 100)
print "s08_number -> " + str(s08_number) + ", " + str(1.0 * s08_number / valid_number * 100)
print "s09_number -> " + str(s09_number) + ", " + str(1.0 * s09_number / valid_number * 100)
print "s10_number -> " + str(s10_number) + ", " + str(1.0 * s10_number / valid_number * 100)
print "s11_number -> " + str(s11_number) + ", " + str(1.0 * s11_number / valid_number * 100)
print "s12_number -> " + str(s12_number) + ", " + str(1.0 * s12_number / valid_number * 100)


