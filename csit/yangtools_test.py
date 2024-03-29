# Copyright (c) 2019 PANTHEON.tech s.r.o. All rights reserved.
#
# This program and the accompanying materials are made available under the
# terms of the Eclipse Public License v1.0 which accompanies this distribution,
# and is available at http://www.eclipse.org/legal/epl-v10.html

import datetime
import os
import sys, getopt
import pytest_lib
import time

start_time = time.time()

def get_java():
    global java_home
    try:
        java_home = os.environ['JAVA_HOME']
        print(f"JAVA_HOME found: {java_home}")
    except:
        print("JAVA_HOME not found, trying to get it from system.")
        try:
            java_home = os.popen("(dirname $(dirname $(readlink -f $(which javac))))").read().split("\n")[0] + "/bin/java"
        except:
            print("JAVA_HOME not found or java not installed, please install java or set JAVA_HOME first.")
            sys.exit(2)

# retrieve args from command
def main(argv):
    global log, models, validator
    log = "N"
    models = "/src/main/yang/"
    validator = ""
    try:
        opts, args = getopt.getopt(argv,"hl:m:v:",["help", "log=","models=", "validator="])
    except getopt.GetoptError:
        print("wrong usage type -h or --help for help")
        sys.exit(2)
    for opt, arg in opts:
        if opt == '-h' or opt == '--help':
            print("Usage: python3 yangtools_test.py [log] [models] [validator_version]")
            print("[log]\n-l, --log=y/n    for delete previous logs in ~/yangtools_csit_test/yang_validator_logs/ (omitting will not delete previous logs)")
            print("[validator_version]\n-v, --validator=10.0.2    will download yang-model-validator-10.0.0-jar-with-dependencies.jar from nexus.opendaylight.org (omitting will build it locally")
            print("[models]\n-m, --models=/path/to/models/    select directory for testing (omitting will test all models)")
            print('''models option examples:
/src/main/yang/standard/
/src/main/yang/vendor/
/src/main/yang/standard/ietf/
/src/main/yang/standard/ieee/
/src/main/yang/standard/iana/
/src/main/yang/vendor/ciena/''')
            sys.exit()
        elif opt in ("-l", "--log"):
            log = arg.upper()
        elif opt in ("-m", "--models"):
            models = arg
        elif opt in ("-v", "--validator"):
            validator = arg

if __name__ == "__main__":
   main(sys.argv[1:])

home_dir = str(os.path.expanduser('~')) # get home dir for local user
working_dir = (home_dir) + "/yangtools_csit_test"
logfile_path = f"{working_dir}/yang_validator_logs/"

def prepare_enviroment():
    os.system(f"rm -rf target {working_dir}/src && mkdir -p {working_dir}/src/main")
    os.system(f"cd {working_dir}/src/main && git clone --depth 10 {pytest_lib.YANGMODELS_REPO}")
    os.system(f'mkdir {logfile_path}')


def delete_static_paths():
    # delete logs from previous testing on locallhost
    delete_logs = log.upper()
    if delete_logs == "Y":
        os.system(f"rm -rf {logfile_path}*")
    # delete not tested models from pytest_lib.delete_static_paths_list
    for file in pytest_lib.delete_static_paths_list:
        print(f"rm -vrf {working_dir + file}")
        os.system(f"rm -vrf {working_dir + file}")


def get_yang_files_to_validate():
    # get full path to directories
    yang_files_to_validate = os.popen(f'find {working_dir}{models}* -name "*.yang"').read().split("\n")[:-1]
    print(f"Files to validate = {len(yang_files_to_validate)}\nFiles: {yang_files_to_validate}")
    return (yang_files_to_validate)


def get_yang_path():
    global yang_path_option
    yang_path_option = "--path "
    for path in pytest_lib.yang_model_paths:
        yang_path_option += (working_dir + path +  " ")

def download_yang_model_validator():
    global validator_file_name, validator
    # check if validator argument received
    if validator == "":
        # check if validator was build with maven or built it
        if check_maven() and get_validator_file_name():
            return validator_file_name
        elif check_maven() and not get_validator_file_name():
            print("building yang-validator with command 'cd ../ && mvn clean install -pl tools/yang-model-validator/'")
            print(os.popen(f"cd ../ && mvn clean install -pl tools/yang-model-validator/").read())
            get_validator_file_name()
        else:
            print("MAVEN not found, please install MAVEN first and restart python command")
            sys.exit()
    else:
        validator_file_name = f"yang-model-validator-{validator}-jar-with-dependencies.jar"
        if validator_file_name in os.popen("ls -la").read():
            print(f"file {validator_file_name} exist...")
        else:
            print(f"downloading {validator_file_name}")
            print(os.system(f"wget https://nexus.opendaylight.org/content/repositories/opendaylight.release/org/opendaylight/yangtools/yang-model-validator/{validator}/{validator_file_name}"))



def get_validator_file_name():
    global validator_file_name, yang_target_dir
    yang_target_dir = "../tools/yang-model-validator/target/"
    get_dir_files = os.popen(f"ls {yang_target_dir}").read()
    if "-SNAPSHOT-jar-with-dependencies.jar" in get_dir_files:
        for line in get_dir_files.split("\n"):
            if "-SNAPSHOT-jar-with-dependencies.jar" in line:
                validator_file_name = line
                return True

def check_maven():
    if "Apache Maven" in os.popen("mvn --version").read():
        return True


def yang_files_loop(yang_files_to_validate):
    global yang_path_option, yang_target_dir
    file_counter = 0
    effective_model_not_resolved, all_not_pass_yang_files, leaf_is_missing, mount_points, following_components = [],[],[],[],[]
    leaf_list_is_missing, statement_has_to_be_present, augment_cannot_add_node_named, augment_target, other = [],[],[],[],[]
    for x in range (0,len(yang_files_to_validate)):
        file_counter +=1
        print("Working on file: ", yang_files_to_validate[x])
        print(f"Testing file {file_counter} from total {len(yang_files_to_validate)} files")
        # update --path option for ietf/RFC/ models to shorten test duration
        if "/src/main/yang/standard/ietf/RFC" in yang_files_to_validate[x]:
            yang_path_option = f"--path {working_dir}/src/main/yang/standard/ietf/RFC {working_dir}/src/main/yang/standard/ieee/published/802.1 {working_dir}/src/main/yang/standard/ieee/published/802 {working_dir}/src/main/yang/vendor/ciena "
        tool_options=f" {yang_path_option}-- {yang_files_to_validate[x]}"
        # update command if argument --validator given
        if validator != "":
            yang_target_dir =  ""
        command = java_home + "  -jar " + yang_target_dir + validator_file_name + tool_options
        # generate name for log file
        name = "yangtools-log"
        date = datetime.datetime.today()
        timestamp = str(date.timestamp())[:-3]
        logfile = f"{name}.{timestamp}.log"
        cmd =  f"{command} > {logfile_path + logfile} 2>&1"
        os.popen(cmd).read()
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
    print(f"All tested files: {len(yang_files_to_validate)}")
    for x in range(0, len(print_files_count)):
        if print_files_count[x]:
            print(f"{print_files_name[x]} = {len(print_files_count[x])}")
            with open(f'{logfile_path}_{print_files_name[x]}.txt', 'w') as f:
                f.write(str(print_files_count[x]) + '\n')
    print(f"Detailed LOGS here: {logfile_path}")

get_java()
prepare_enviroment()
delete_static_paths()
download_yang_model_validator()
get_yang_path()
yang_files_loop(get_yang_files_to_validate())
print("test duration --- %s seconds ---" % (time.time() - start_time))