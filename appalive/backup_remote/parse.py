# -*- coding: utf-8 -*-

result_paths = [
                "/home/zhouhao/KeepAliveWS/appalive/backup_remote/system_out_0.txt",
                "/home/zhouhao/KeepAliveWS/appalive/backup_remote/system_out_1.txt",
                "/home/zhouhao/KeepAliveWS/appalive/backup_remote/system_out_2.txt",
                "/home/zhouhao/KeepAliveWS/appalive/backup_remote/system_out_3.txt",
                "/home/zhouhao/KeepAliveWS/appalive/backup_remote/system_out_4.txt",
                "/home/zhouhao/KeepAliveWS/appalive/backup_remote/system_out_5.txt",
                "/home/zhouhao/KeepAliveWS/appalive/backup_remote/system_out_6.txt",
                "/home/zhouhao/KeepAliveWS/appalive/backup_remote/system_out_7.txt",
                "/home/zhouhao/KeepAliveWS/appalive/backup_remote/system_out_8.txt",
                "/home/zhouhao/KeepAliveWS/appalive/backup_remote/system_out_9.txt",
               ]

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

#pf = open("/home/zhouhao/KeepAliveWS/appalive/backup_remote/package_name.txt", 'a+')

co_cnt = 0
he_cnt = 0
mu_cnt = 0
so_cnt = 0
lo_cnt = 0
vi_cnt = 0
bo_cnt = 0
bu_cnt = 0
ed_cnt = 0
en_cnt = 0
fi_cnt = 0
ga_cnt = 0
li_cnt = 0
ne_cnt = 0
pe_cnt = 0
ph_cnt = 0
sh_cnt = 0
to_cnt = 0
ot_cnt = 0

nc_map = {}
catef = open("/home/zhouhao/KeepAliveWS/appalive/backup_remote/package_cate", 'r')
for line in catef.readlines():
    package_name = line.split(" ")[0].replace('\n', '').replace('\t', '').strip()
    category = line.split(" ")[1].replace('\n', '').replace('\t', '').strip()
    if category == "BR" or category == "IM" or category == "SE":
    	nc_map[package_name] = "OT"
    else:
    	nc_map[package_name] = category

    if category == "CO":
    	co_cnt += 1
    if category == "HE":
    	he_cnt += 1
    if category == "MU":
    	mu_cnt += 1
    if category == "SO":
    	so_cnt += 1
    if category == "LO":
    	lo_cnt += 1
    if category == "VI":
    	vi_cnt += 1
    if category == "BO":
    	bo_cnt += 1
    if category == "BU":
    	bu_cnt += 1
    if category == "ED":
    	ed_cnt += 1
    if category == "EN":
    	en_cnt += 1
    if category == "FI":
    	fi_cnt += 1
    if category == "GA":
    	ga_cnt += 1
    if category == "LI":
    	li_cnt += 1
    if category == "NE":
        ne_cnt += 1
    if category == "PE":
        pe_cnt += 1
    if category == "PH":
        ph_cnt += 1
    if category == "SH":
        sh_cnt += 1
    if category == "TO":
        to_cnt += 1
    if (category == "BR") or (category == "IM") or (category == "SE") or (category == "OT"):
        ot_cnt += 1
catef.close()
# patch
nc_map["com.SnowflakesLiveWallpaper"] = "OT"
nc_map["com.Pink.Hearts.Live.Wallpaper"] = "OT"
nc_map["air.SantaclausgiftEscape"] = "OT"
nc_map["com.applica4.wasla.capitals"] = "OT"
nc_map["com.free.tictactoe"] = "OT"
nc_map["com.Company.ProductNaME"] = "OT"
nc_map["com.companyname.game"] = "OT"
nc_map["com.SOS"] = "OT"
nc_map["com.Bubble"] = "OT"
nc_map["com.colorpicker"] = "OT"
nc_map["mil.army"] = "OT"
nc_map["com.Sokoban"] = "OT"
nc_map["com.Teddy.Bear.Live.Wallpaper"] = "OT"
nc_map["com.baby.care.tips"] = "OT"
ot_cnt += 14

print co_cnt
print he_cnt
print mu_cnt
print so_cnt
print lo_cnt
print vi_cnt
print bo_cnt
print bu_cnt
print ed_cnt
print en_cnt
print fi_cnt
print ga_cnt
print li_cnt
print ne_cnt
print pe_cnt
print ph_cnt
print sh_cnt
print to_cnt
print ot_cnt

cnt = 0
for key, value in nc_map.items():
	if value == "OT":
		cnt += 1
print cnt

category_set = set()

for result_path in result_paths:
    with open(result_path, "r") as f:
        lines = f.readlines()
        s_count = 0
        valid_flag = False
        skip_flag = True
        for line in lines:
            if "package name:" in line:
                total_number += 1
                skip_flag = True # reset
                
                # handle valid_flag
                if valid_flag == True and nc_map[package_name] == "OT":
                    valid_number += 1
                    # category_set.add(nc_map[package_name])
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
                
                # update package name (current)
                package_name = line.split(":")[1].strip()
                #pf.write(package_name + "\n")
                if nc_map[package_name] == "OT":
                    skip_flag = False
                    
            if skip_flag == True:
                continue
                
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
                
#pf.flush()
#pf.close()

print category_set

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


