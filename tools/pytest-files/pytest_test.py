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
        f"cd src/main/yang && git checkout -b ytest {pytest_lib.YANGMODELS_REPO_COMMIT_HASH}", # checkout_yangmodels
        "rm -rf src/main/yang/experimental/openconfig", # remove_openconfig
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
    cmd = f'ls -d \$PWD/src/main/yang/*/'    
    if hostname == "ubuntu2": # remove backslash in $PWD for testing on locallhost
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
    
def test_download_yang_model_validator():
    global url, artifact, version, filename
    urlbase = pytest_lib.NEXUS_RELEASE_BASE_URL
    location = "org/opendaylight/yangtools"
    component="yangtools"
    artifact=pytest_lib.TEST_TOOL_NAME
    version = "9.0.1"
    url = urlbase + "/" + location + "/" + artifact + "/" + version
    name_prefix = f"{artifact}-"
    suffix="jar-with-dependencies"
    extension = "tar" if component == "odl-micro" else "jar"
    name_suffix = f"-{suffix}.{extension}" if suffix != "" else f".{extension}"
    filename = name_prefix + version + name_suffix
    url = url + "/" + filename
    cmd = f"wget -q -N '{url}' 2>&1"
    response = pytest_lib.execute_ssh_command(command=cmd, username=odl_params['CONTROLLER_USER'], hostname=odl_params['ODL_SYSTEM_IP'])
    console_output, exit_code, error = response[0], response[1], response[2]
    assert exit_code == 0

def test_yang_files_loop():
    global url
    for file in yang_files_to_validate:
        print("Working on file: ", file)
        tool_options=f" {yang_path_option}-- {file}"
        # set java version based on ip (testing on locallhost)
        base_command = "/usr/lib/jvm/java-17-openjdk/bin/java"
        if hostname == "ubuntu2":
            base_command = "/usr/lib/jvm/java-17-openjdk-amd64/bin/java"
        command = base_command + "  -jar " + filename + tool_options
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
            print(40 * "*", " ERROR ", 40 * "*")
            os.system(f"cat {logfile}")
            print(40 * "*", " ERROR ", 40 * "*")
            break
    assert exit_code == 0