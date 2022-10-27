# Copyright (c) 2019 PANTHEON.tech s.r.o. All rights reserved.
#
# This program and the accompanying materials are made available under the
# terms of the Eclipse Public License v1.0 which accompanies this distribution,
# and is available at http://www.eclipse.org/legal/epl-v10.html

import paramiko

# variables
# NEW hashes
YANGMODELS_REPO_COMMIT_HASH = "76b82325c19bd2907acd344e6ef9bf2adfaae5cc"
OPENCONFIG_REPO_COMMIT_HASH = "00a27a5f0f3c472205ef293ab238e0530b610811"
# OLD hashes
# YANGMODELS_REPO_COMMIT_HASH = "cdd14114cdaf130be2b6bfce92538c05f6d7c07d"
# OPENCONFIG_REPO_COMMIT_HASH = "8062b1b45208b952598ad3c3aa9e5ebc4f03cc67"

YANGMODELS_REPO = "https://github.com/YangModels/yang"
OPENCONFIG_REPO = "https://github.com/openconfig/public"
TEST_TOOL_NAME = "yang-model-validator"
NEXUS_FALLBACK_URL_PART2 = "/content/repositories/opendaylight.release"
RELEASE_INTEGRATED_COMPONENTS = ["mdsal", "odlparent", "yangtools", "carpeople", "netconf"]
NEXUS_RELEASE_BASE_URL = "https://nexus.opendaylight.org/content/repositories/opendaylight.release"
ROOT="src/main/yang"

def execute_ssh_command(hostname, command, username, commands_as_list=False):
    # created client using paramiko
    client = paramiko.SSHClient()
    client.set_missing_host_key_policy(paramiko.AutoAddPolicy())
    client.connect(hostname, username=username)
    if commands_as_list:
        for command in commands_as_list:
            (stdin, stdout, stderr) = client.exec_command(command)
            cmd_output = stdout.read()
            error = stderr.read()
    else:
        (stdin, stdout, stderr) = client.exec_command(command)
    exit_status=(stdout.channel.recv_exit_status())
    cmd_output = stdout.read()
    error = stderr.read()
    client.close()
    return [cmd_output, exit_status, error]

COMPONENT_MAPPING = {"netconf":"netconf-impl",
                     "bgpcep":"pcep-impl",
                     "carpeople":"clustering-it-model",
                     "yangtools":"yang-data-impl",
                     "bindingv1":"mdsal-binding-generator-impl",
                     "odl-micro":"odlmicro-impl"}

delete_static_paths_list = [
    "rm -vrf src/main/yang/.git",
    "rm -vrf src/main/yang/experimental",
    # excluding ieee/draft from test
    "rm -vrf src/main/yang/standard/ieee/draft",
    "rm -vrf src/main/yang/standard/ietf/DRAFT",
    # excluding 4 files due error "Unexpected error processing source SourceIdentifier [ietf-network@2018-02-26]"
    # bug reported here https://jira.opendaylight.org/browse/YANGTOOLS-1465
    "rm -vfr src/main/yang/standard/ietf/RFC/ietf-te-topology-state.yang",
    "rm -vfr src/main/yang/standard/ietf/RFC/ietf-te-topology-state@2020-08-06.yang",
    "rm -vfr src/main/yang/standard/ietf/RFC/ietf-te-topology.yang",
    "rm -vfr src/main/yang/standard/ietf/RFC/ietf-te-topology@2020-08-06.yang",
    ## Removing entire juniper folder because it creates an OOM Crash with the validator tool.*** Keywords ***
    ## Unsure if the yang models are the problem or something in the tool. This is being tracked here:
    ## https://jira.opendaylight.org/browse/YANGTOOLS-1093
    "rm -vrf src/main/yang/vendor/juniper"
    ## Removing the cisco folder because there are over 30k yang files there and would increase the test time to something
    ## unmanageable.
    "rm -vrf src/main/yang/vendor/cisco",
    ## Mount points may only be defined at either a container or a list, not anydata, lines 948
    ## https://tools.ietf.org/html/rfc8528#section-3.1
    "rm -vrf src/main/yang/standard/ietf/RFC/ietf-connectionless-oam@2019-04-16.yang",
    ## removed dependency file from test standard/ietf/RFC/ietf-connectionless-oam@2019-04-16.yang
    "rm -vrf src/main/yang/standard/ietf/RFC/ietf-connectionless-oam.yang",
    ## removed dependecy file from test standard/ietf/RFC/ietf-connectionless-oam-methods@2019-04-16.yang
    "rm -vrf src/main/yang/standard/ietf/RFC/ietf-connectionless-oam-methods.yang",
    "rm -vrf src/main/yang/standard/ietf/RFC/ietf-connectionless-oam-methods@2019-04-16.yang",
]

yang_model_paths = [
    "/home/jenkins/src/main/yang/",
    "/home/jenkins/src/main/yang/standard",
    "/home/jenkins/src/main/yang/standard/etsi",
    "/home/jenkins/src/main/yang/standard/etsi/NFV-SOL006-v3.6.1",
    "/home/jenkins/src/main/yang/standard/etsi/NFV-SOL006-v2.6.1",
    "/home/jenkins/src/main/yang/standard/etsi/NFV-SOL006-v2.7.1",
    "/home/jenkins/src/main/yang/standard/etsi/NFV-SOL006-v2.8.1",
    "/home/jenkins/src/main/yang/standard/etsi/NFV-SOL006-v3.3.1",
    "/home/jenkins/src/main/yang/standard/etsi/NFV-SOL006-v3.5.1",
    "/home/jenkins/src/main/yang/standard/odp",
    "/home/jenkins/src/main/yang/standard/bbf",
    "/home/jenkins/src/main/yang/standard/ietf",
    "/home/jenkins/src/main/yang/standard/ietf/RFC",
    "/home/jenkins/src/main/yang/standard/mef",
    "/home/jenkins/src/main/yang/standard/ieee",
    "/home/jenkins/src/main/yang/standard/ieee/published",
    "/home/jenkins/src/main/yang/standard/ieee/published/802.1",
    "/home/jenkins/src/main/yang/standard/ieee/published/802.11",
    "/home/jenkins/src/main/yang/standard/ieee/published/802",
    "/home/jenkins/src/main/yang/standard/ieee/published/1588",
    "/home/jenkins/src/main/yang/standard/ieee/published/802.3",
    "/home/jenkins/src/main/yang/standard/ieee/published/1906.1",
    "/home/jenkins/src/main/yang/standard/ieee/published/1906.1/Examples",
    "/home/jenkins/src/main/yang/standard/iana",
    "/home/jenkins/src/main/yang/vendor",
    "/home/jenkins/src/main/yang/vendor/fujitsu",
    "/home/jenkins/src/main/yang/vendor/huawei",
    "/home/jenkins/src/main/yang/vendor/ciena",
    "/home/jenkins/src/main/yang/vendor/nokia"
]