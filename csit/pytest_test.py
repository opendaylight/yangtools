# Copyright (c) 2019 PANTHEON.tech s.r.o. All rights reserved.
#
# This program and the accompanying materials are made available under the
# terms of the Eclipse Public License v1.0 which accompanies this distribution,
# and is available at http://www.eclipse.org/legal/epl-v10.html

import datetime, os, pytest, socket
import pytest_lib

# create list of variables
var_list = ["CONTROLLER_USER", "ODL_SYSTEM_IP"]
hostname=socket.gethostname()
global ssh_num

@pytest.fixture
def bash_arguments(request):
    # get shell arguments using conftest.py and save it to odl_params
    global odl_params
    odl_params = {x: (request.config.getoption('--' + x)) for x in var_list}
    return odl_params

def test_getting_bash_arguments(bash_arguments):
    # save arguments to odl_params to not spam ssh connections
    assert odl_params['CONTROLLER_USER'] != ""

def test_preparing_enviroment():
    # using commands_as_list and cmd=None to executing more then one command over SSH
    commands_as_list = [
        "ps axf | grep org.apache.karaf | grep -v grep | wc -l", # check if karaf is not running
        "rm -rf target src", # rm dir
        "mkdir -p src/main", # create dir
        f"cd src/main && git clone {pytest_lib.YANGMODELS_REPO}", # clone yangmodels
        # f"cd src/main/yang && git checkout -b ytest {pytest_lib.YANGMODELS_REPO_COMMIT_HASH}", # checkout_yangmodels
        ]
    response = pytest_lib.execute_ssh_command(command=None, username=odl_params['CONTROLLER_USER'], hostname=odl_params['ODL_SYSTEM_IP'], commands_as_list=commands_as_list)
    console_output, exit_code, error = response[0], response[1], response[2]
    assert exit_code == 0

def test_delete_static_paths():
    # delete logs from previous testing on locallhost
    # os.system("rm -rf *yang-model-validator--yangtools-system-txt*")
    # using commands_as_list and cmd=None to executing more then one command over SSH
    cmd = None
    response = pytest_lib.execute_ssh_command(command=cmd, username=odl_params['CONTROLLER_USER'], hostname=odl_params['ODL_SYSTEM_IP'], commands_as_list=pytest_lib.delete_static_paths_list)
    console_output, exit_code, error = response[0], response[1], response[2]
    assert exit_code == 0

# Deploy_And_Start_Odl_Yang_Validator_Utility
def test_get_dirs_to_process():
    global dirs_to_process
    # command get full path to directories
    cmd = f'ls -d $PWD/src/main/yang/*/'
    response = pytest_lib.execute_ssh_command(command=cmd, username=odl_params['CONTROLLER_USER'], hostname=odl_params['ODL_SYSTEM_IP'])
    # convert bytes output to list and remove last item
    dirs_to_process = (response[0].decode("utf-8").split("\n"))[:-1]
    console_output, exit_code, error = response[0], response[1], response[2]
    assert exit_code == 0

def test_get_yang_files_to_validate():
    global yang_files_to_validate
    yang_files_to_validate = []
    for dir in dirs_to_process:
        cmd = f'find {dir} -name "*.yang"'
        response = pytest_lib.execute_ssh_command(command=cmd, username=odl_params['CONTROLLER_USER'], hostname=odl_params['ODL_SYSTEM_IP'])
        yang_files_in_dir = (response[0].decode("utf-8").split("\n"))[:-1]
        yang_files_to_validate.extend(yang_files_in_dir)
    console_output, exit_code, error = response[0], response[1], response[2]
    assert exit_code == 0

def test_yang_path_option():
    global yang_path_option
    yang_path_option = "--path "
    for path in pytest_lib.yang_model_paths:
        if hostname  == "ubuntu2":
            yang_path_option += (path.replace("jenkins", "ubuntu") + " ")
        else:
            yang_path_option += (path + " ")
    assert len(yang_path_option) > 0

def test_download_yang_model_validator():
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
    cmd = f"wget -q -N '{url}' 2>&1"
    print(cmd)
    response = pytest_lib.execute_ssh_command(command=cmd, username=odl_params['CONTROLLER_USER'], hostname=odl_params['ODL_SYSTEM_IP'])
    console_output, exit_code, error = response[0], response[1], response[2]
    assert exit_code == 0

def test_yang_files_loop():
    global url
    EFFECTIVE_MODEL_not_resolved = []
    not_pass_yang_files = []
    leaf_is_missing = []
    Mount_points_may_only_be_defined_at_either_a_container_or_a_list = []
    Following_components_of_unique_statement = []
    Leaf_list_is_missing = []
    statement_has_to_be_present = []
    An_augment_cannot_add_node_named = []
    Augment_target = []
    other = []
    for x in range (0,10):
    # for x in range (0,len(yang_files_to_validate)):
        print("Working on file: ", yang_files_to_validate[x])
        tool_options=f" {yang_path_option}-- {yang_files_to_validate[x]}"
        # set java version based on ip (testing on locallhost)
        base_command = "/usr/lib/jvm/java-17-openjdk/bin/java"
        if hostname == "ubuntu2":
            base_command = "/usr/lib/jvm/java-17-openjdk-amd64/bin/java"
        command = base_command + "  -jar " + filename + tool_options
        print(command)
        # generate name for log file
        name = "yangtools-system-txt"
        date = datetime.datetime.today()
        timestamp = str(date.timestamp())[:-3]
        logfile = f"{artifact}--{name}.{timestamp}.log"
        cmd =  f"{command} > {logfile} 2>&1"
        response = pytest_lib.execute_ssh_command(command=cmd, username=odl_params['CONTROLLER_USER'], hostname=odl_params['ODL_SYSTEM_IP'])
        console_output, exit_code, error = response[0], response[1], response[2]
        # copy log file and print error to console
        if exit_code != 0:
            os.system(f"scp {odl_params['CONTROLLER_USER']}@{odl_params['ODL_SYSTEM_IP']}:{logfile} .")
            with open(logfile) as f:
                text_log = f.read()
                # add log name prefix for change name of file due to error
                logname_prefix = ""
                if 'Mount points may only be defined at either a container or a list' in text_log:
                    logname_prefix = "mount-points-"
                    Mount_points_may_only_be_defined_at_either_a_container_or_a_list.append(yang_files_to_validate[x])
                elif "Leaf-list is missing a 'type' statement" in text_log:
                    logname_prefix = "Leaf-list-is-missing-"
                    Leaf_list_is_missing.append(yang_files_to_validate[x])
                elif "At least one enum statement has to be present" in text_log:
                    logname_prefix = "statement-has-to-be-present-"
                    statement_has_to_be_present.append(yang_files_to_validate[x])
                elif "An augment cannot add node named" in text_log:
                    logname_prefix = "augment-cannot-add-node-named-"
                    An_augment_cannot_add_node_named.append(yang_files_to_validate[x])
                elif "Augment target" in text_log:
                    logname_prefix = "Augment-target-"
                    Augment_target.append(yang_files_to_validate[x])
                elif 'Following components of unique statement argument refer to non-existent nodes:' in text_log:
                    logname_prefix = "following-components-"
                    Following_components_of_unique_statement.append(yang_files_to_validate[x])
                elif 'Leaf is missing a' not in text_log and "Some of EFFECTIVE_MODEL modifiers for statements were not resolved" in text_log:
                    logname_prefix = "effective-model-"
                    EFFECTIVE_MODEL_not_resolved.append(yang_files_to_validate[x])
                elif 'Leaf is missing a' in text_log:
                    logname_prefix = "leaf-"
                    leaf_is_missing.append(yang_files_to_validate[x])
                else:
                    logname_prefix = "others-"
                    other.append(yang_files_to_validate[x])
                os.system(f"mv {logfile} {logname_prefix + logfile}")
                print(40 * "*", f" ERROR IN YANG FILE {yang_files_to_validate[x]} ", 40 * "*")
                print("LOG FILE = ", logname_prefix + logfile)
                os.system(f"cat {logname_prefix + logfile}")
                print(40 * "*", " ERROR ", 40 * "*")
                not_pass_yang_files.append(yang_files_to_validate[x])
    print("Augment_target count = ", len(Augment_target))
    print("Augment_target print:  ", Augment_target)
    print("An_augment_cannot_add_node_named count = ", len(An_augment_cannot_add_node_named))
    print("An_augment_cannot_add_node_named print:  ", An_augment_cannot_add_node_named)
    print("statement_has_to_be_present count = ", len(statement_has_to_be_present))
    print("statement_has_to_be_present print:  ", statement_has_to_be_present)
    print("Leaf_list_is_missing count = ", len(Leaf_list_is_missing))
    print("Leaf_list_is_missing print:  ", Leaf_list_is_missing)
    print("EFFECTIVE_MODEL_not_resolved count = ", len(EFFECTIVE_MODEL_not_resolved))
    print("EFFECTIVE_MODEL_not_resolved print:  ", EFFECTIVE_MODEL_not_resolved)
    print("leaf_is_missing count = ", len(leaf_is_missing))
    print("leaf_is_missing print:  ", leaf_is_missing)
    print("Mount_points_may_only_be_defined_at_either_a_container_or_a_list count = ", len(Mount_points_may_only_be_defined_at_either_a_container_or_a_list))
    print("Mount_points_may_only_be_defined_at_either_a_container_or_a_list print:  ", Mount_points_may_only_be_defined_at_either_a_container_or_a_list)
    print("Following components of unique statement argument refer to non-existent nodes: count = ", len(Following_components_of_unique_statement))
    print("Following components of unique statement argument refer to non-existent nodes::  ", Following_components_of_unique_statement)
    print("other count = ", len(other))
    print("other print:  ", other)
    print("all yang_files_to_validate = ", len(yang_files_to_validate))
    print("all not_pass_yang_files count= ", len(not_pass_yang_files))
    print("all not_pass_yang_files = ", not_pass_yang_files)
    assert exit_code == 0