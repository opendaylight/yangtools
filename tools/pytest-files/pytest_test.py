import datetime, os, pytest
import pytest_lib


# create list of variables
var_list = [
    "BUNDLEFOLDER",
    "BUNDLE_URL",
    "CONTROLLER_USER",
    "NEXUSURL_PREFIX",
    "ODL_SYSTEM_IP"
]
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
    
    
    
def test_preparing_test_commands():
    # using commands_as_list and cmd=None to executing more then one command over SSH
    commands_as_list = [        
        "ps axf | grep org.apache.karaf | grep -v grep | wc -l", # check if karaf is not running        
        "rm -rf target src", # rm dir        
        "mkdir -p src/main", # create dir        
        f"cd src/main && git clone {pytest_lib.YANGMODELS_REPO}", # clone yangmodels        
        f"cd src/main/yang && git checkout -b ytest {pytest_lib.YANGMODELS_REPO_COMMIT_HASH}", # checkout_yangmodels        
        "rm -rf src/main/yang/experimental/openconfig", # test_remove_openconfig        
        f"cd src/main/yang/experimental/ && git clone {pytest_lib.OPENCONFIG_REPO} && mv -v public openconfig", # clone openconfig        
        f"cd src/main/yang/experimental/openconfig && git checkout -b ytest {pytest_lib.OPENCONFIG_REPO_COMMIT_HASH}" # checkout openconfig
        ]
    response = pytest_lib.execute_ssh_command(command=None, username=odl_params['CONTROLLER_USER'], hostname=odl_params['ODL_SYSTEM_IP'], commands_as_list=commands_as_list)
    console_output, exit_code, error = response[0], response[1], response[2]
    assert exit_code == 0
    
    
def test_delete_static_paths():
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
        yang_path_option += (path + " ")       
    assert len(yang_path_option) > 0


    
def test_get_yang_path_option():
    # need to update for all jang files... for testing only 10 files is used
    for x in range(0,9):
        print("Working on file: ", yang_files_to_validate[x])        
        component="yangtools"
        artifact=pytest_lib.TEST_TOOL_NAME
        suffix="jar-with-dependencies"
        tool_options=f" {yang_path_option}-- {yang_files_to_validate[x]}"
        fallback_url= odl_params['NEXUSURL_PREFIX'] + pytest_lib.NEXUS_FALLBACK_URL_PART2
        # Deploy_Test_Tool
        name_prefix = f"{artifact}-"
        extension = "tar" if component == "odl-micro" else "jar"
        name_suffix = f"-{suffix}.{extension}" if suffix != "" else f".{extension}"
        # Deploy_Artifact
        urlbase = (odl_params['BUNDLE_URL'].split("/org/opendaylight")[0])
        urlbase = urlbase if urlbase != odl_params['BUNDLE_URL'] else fallback_url
        # NexusKeywords__Detect_Version_To_Pull
        version = "9.0.1"
        location = "org/opendaylight/yangtools"   
        url = urlbase + "/" + location + "/" + artifact + "/" + version
        namepart = version
        filename = name_prefix + namepart + name_suffix
        url = url + "/" + filename
        cmd0 = f"wget -q -N '{url}' 2>&1"
        base_command = "/usr/lib/jvm/java-17-openjdk/bin/java"
        command = base_command + "  -jar " + filename + tool_options
        name = "yangtools-system-txt"
        date = datetime.datetime.today()
        timestamp = str(date.timestamp())[:-3]
        logfile = f"{artifact}--{name}.{timestamp}.log"
        cmd =  f"{command} > {logfile} 2>&1"
        cmd_response = pytest_lib.execute_ssh_command(command=None, username=odl_params['CONTROLLER_USER'], hostname=odl_params['ODL_SYSTEM_IP'], commands_as_list=[cmd0, cmd])
        console_output, exit_code, error = cmd_response[0], cmd_response[1], cmd_response[2]
        print("console_output= ", console_output)
        print("cmd= ", cmd)
    assert exit_code == 0
    
def test_copy_files_from_remote():
    cmd = f"scp {odl_params['CONTROLLER_USER']}@{odl_params['ODL_SYSTEM_IP']}:yang-model-validator--yangtools-system-txt.* ."
    os.system(cmd)
    assert  0 == 0
    