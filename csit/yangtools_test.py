# Copyright (c) 2019 PANTHEON.tech s.r.o. All rights reserved.
#
# This program and the accompanying materials are made available under the
# terms of the Eclipse Public License v1.0 which accompanies this distribution,
# and is available at http://www.eclipse.org/legal/epl-v10.html

import datetime
import os
import pytest_lib

home_dir = str(os.path.expanduser('~')) # get home dir for local user
working_dir = (home_dir) + "/yangtools_csit_test"
logfile_path = f"{working_dir}/yang_validator_logs/"
clear = lambda: os.system('clear')

def prepare_enviroment():
    os.system(f"rm -rf target {working_dir}/src && mkdir -p {working_dir}/src/main")
    os.system(f"cd {working_dir}/src/main && git clone {pytest_lib.YANGMODELS_REPO}")
    os.system(f'mkdir {logfile_path}')


def delete_static_paths():
    # delete logs from previous testing on locallhost
    clear()
    delete_logs = input(f"Delete old logs from {logfile_path} ? y/n: ").upper()
    if delete_logs == "Y":
        os.system(f"rm -rf {logfile_path}*yang-model-validator--yangtools-system-txt*")
    for file in pytest_lib.delete_static_paths_list:
        print(f"rm -vrf {working_dir + file}")
        os.system(f"rm -vrf {working_dir + file}")


def get_yang_files_to_validate():
    # get full path to directories
    test_choice = int(input("Select test (type number and press enter): \n0:  ALL\n1:  ALL standard\n2:  ALL vendor\n3:  standard/ietf/RFC\n4:  standard/ieee\n5:  standard/iana\n6: vendor/ciena\n"))
    if test_choice == 0:
        yang_files_to_validate = os.popen(f'find {working_dir}/src/main/yang/* -name "*.yang"').read().split("\n")[:-1]
        print(f"Files to validate = {len(yang_files_to_validate)}\nFiles: {yang_files_to_validate}")
    elif test_choice == 1:
        yang_files_to_validate = os.popen(f'find {working_dir}/src/main/yang/standard/* -name "*.yang"').read().split("\n")[:-1]
        print(f"Files to validate = {len(yang_files_to_validate)}\nFiles: {yang_files_to_validate}")
    elif test_choice == 2:
        yang_files_to_validate = os.popen(f'find {working_dir}/src/main/yang/vendor/* -name "*.yang"').read().split("\n")[:-1]
        print(f"Files to validate = {len(yang_files_to_validate)}\nFiles: {yang_files_to_validate}")
    elif test_choice == 3:
        yang_files_to_validate = os.popen(f'find {working_dir}/src/main/yang/standard/ietf/* -name "*.yang"').read().split("\n")[:-1]
        print(f"Files to validate = {len(yang_files_to_validate)}\nFiles: {yang_files_to_validate}")
    elif test_choice == 4:
        yang_files_to_validate = os.popen(f'find {working_dir}/src/main/yang/standard/ieee/* -name "*.yang"').read().split("\n")[:-1]
        print(f"Files to validate = {len(yang_files_to_validate)}\nFiles: {yang_files_to_validate}")
    elif test_choice == 5:
        yang_files_to_validate = os.popen(f'find {working_dir}/src/main/yang/standard/iana/* -name "*.yang"').read().split("\n")[:-1]
        print(f"Files to validate = {len(yang_files_to_validate)}\nFiles: {yang_files_to_validate}")
    elif test_choice == 6:
        yang_files_to_validate = os.popen(f'find {working_dir}/src/main/yang/vendor/ciena/* -name "*.yang"').read().split("\n")[:-1]
        print(f"Files to validate = {len(yang_files_to_validate)}\nFiles: {yang_files_to_validate}")
    else:
        print("Wrong choice. Starting to test all models!")
        yang_files_to_validate = os.popen(f'find {working_dir}/src/main/yang/* -name "*.yang"').read().split("\n")[:-1]
        print(f"Files to validate = {len(yang_files_to_validate)}\nFiles: {yang_files_to_validate}")
    return (yang_files_to_validate)


def yang_path_option():
    global yang_path_option
    yang_path_option = "--path "
    for path in pytest_lib.yang_model_paths:
        yang_path_option += (working_dir + path +  " ")
    clear()
    return yang_path_option


def download_yang_model_validator():
    global url, artifact, version, filename
    urlbase = pytest_lib.NEXUS_RELEASE_BASE_URL
    location = "org/opendaylight/yangtools"
    component="yangtools"
    artifact=pytest_lib.TEST_TOOL_NAME
    version = "9.0.2"
    url = urlbase + "/" + location + "/" + artifact + "/" + version
    name_prefix = f"{artifact}-"
    suffix="jar-with-dependencies"
    extension = "tar" if component == "odl-micro" else "jar"
    name_suffix = f"-{suffix}.{extension}" if suffix != "" else f".{extension}"
    filename = name_prefix + version + name_suffix
    url = url + "/" + filename
    cmd = f"cd {working_dir} && wget -q -N '{url}' 2>&1"
    os.popen(cmd).read()


def yang_files_loop(yang_files_to_validate):
    global url
    effective_model_not_resolved, all_not_pass_yang_files, leaf_is_missing, mount_points, following_components = [],[],[],[],[]
    leaf_list_is_missing, statement_has_to_be_present, augment_cannot_add_node_named, augment_target, other = [],[],[],[],[]
    # for x in range (10,15):
    for x in range (0,len(yang_files_to_validate)):
        print("Working on file: ", yang_files_to_validate[x])
        tool_options=f" {yang_path_option}-- {yang_files_to_validate[x]}"
        # set java version based on ip (testing on locallhost)
        base_command = pytest_lib.JAVA
        command = base_command + "  -jar " + working_dir + "/" + filename + tool_options
        # generate name for log file
        name = "yangtools-system-txt"
        date = datetime.datetime.today()
        timestamp = str(date.timestamp())[:-3]
        logfile = f"{artifact}--{name}.{timestamp}.log"
        cmd =  f"{command} > {logfile_path + logfile} 2>&1"
        os.popen(cmd).read()
        # copy log file and print error to console
        # copy log file and print error to console
        if os.stat(logfile_path + logfile).st_size != 0:
            print("java command: ", cmd)
            with open(logfile_path + logfile) as f:
                text_log = f.read()
                # add log name prefix for change name of file due to error
                logname_prefix = ""
                if 'Mount points may only be defined at either a container or a list' in text_log:
                    logname_prefix = "mount-points-"
                    mount_points.append(yang_files_to_validate[x])
                elif "Leaf-list is missing a 'type' statement" in text_log:
                    logname_prefix = "Leaf-list-is-missing-"
                    leaf_list_is_missing.append(yang_files_to_validate[x])
                elif "At least one enum statement has to be present" in text_log:
                    logname_prefix = "statement-has-to-be-present-"
                    statement_has_to_be_present.append(yang_files_to_validate[x])
                elif "An augment cannot add node named" in text_log:
                    logname_prefix = "augment-cannot-add-node-named-"
                    augment_cannot_add_node_named.append(yang_files_to_validate[x])
                elif "Augment target" in text_log:
                    logname_prefix = "Augment-target-"
                    augment_target.append(yang_files_to_validate[x])
                elif 'Following components of unique statement argument refer to non-existent nodes:' in text_log:
                    logname_prefix = "following-components-"
                    following_components.append(yang_files_to_validate[x])
                elif 'Leaf is missing a' not in text_log and "Some of EFFECTIVE_MODEL modifiers for statements were not resolved" in text_log:
                    logname_prefix = "effective-model-"
                    effective_model_not_resolved.append(yang_files_to_validate[x])
                elif 'Leaf is missing a' in text_log:
                    logname_prefix = "leaf-"
                    leaf_is_missing.append(yang_files_to_validate[x])
                else:
                    logname_prefix = "others-"
                    other.append(yang_files_to_validate[x])
                os.system(f"mv {logfile_path + logfile} {logfile_path + logname_prefix + logfile}")
                print(40 * "*", f" ERROR IN YANG FILE {yang_files_to_validate[x]} ", 40 * "*")
                print("LOG FILE = ", logfile_path + logname_prefix + logfile)
                os.system(f"cat {logfile_path + logname_prefix + logfile}")
                print(40 * "*", " ERROR ", 40 * "*")
                all_not_pass_yang_files.append(yang_files_to_validate[x])
        else:
            os.system(f"rm -vrf {logfile_path + logfile}")
    print_files_name = ["all_not_pass_yang_files", "effective_model_not_resolved", "leaf_is_missing", "mount_points", "following_components",
                        "leaf_list_is_missing", "statement_has_to_be_present", "augment_cannot_add_node_named", "augment_target", "other"]
    print_files_count = [all_not_pass_yang_files, effective_model_not_resolved, leaf_is_missing, mount_points, following_components,
                        leaf_list_is_missing, statement_has_to_be_present, augment_cannot_add_node_named, augment_target, other]

    # print test result counts and save txt list with not pass files
    print("\n\n\n",40 * "*", " TEST RESULTS  ", 40 * "*")
    for x in range(0, len(print_files_count)):
        if print_files_count[x]:
            print(f"{print_files_name[x]} = {len(print_files_count[x])}")
            with open(f'{logfile_path}_{print_files_name[x]}.txt', 'w') as f:
                f.write(str(print_files_count[x]) + '\n')
    print(f"Detailed LOGS here: {logfile_path}")

prepare_enviroment()
delete_static_paths()
download_yang_model_validator()
yang_path_option()
yang_files_loop(get_yang_files_to_validate())