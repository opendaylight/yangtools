#
# Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
#
# This program and the accompanying materials are made available under the
# terms of the Eclipse Public License v1.0 which accompanies this distribution,
# and is available at http://www.eclipse.org/legal/epl-v10.html
#

import logging
import pathlib
import subprocess
import ftplib
import os
from collections import defaultdict

OPENCONFIG_REPO = "https://github.com/openconfig/public"
OPENCONFIG_REPO_COMMIT_HASH = "8062b1b45208b952598ad3c3aa9e5ebc4f03cc67"

TEST_TOOL_NAME = "yang-model-validator"

# YANG_MODEL_PATHS is needed to explicitly tell which paths to find yang files to build dependencies from to validate
# the yang file being validated. There is an option (-r) to recursively parse so that you don't have to pass all of these
# paths with the -p argument, but the recursive option makes the tool so slow that it would not work when testing so
# many files
YANG_MODEL_PATHS = {
    "src/main/yang/experimental/openconfig/release/models",
    "src/main/yang/experimental/openconfig/release/models/acl",
    "src/main/yang/experimental/openconfig/release/models/aft",
    "src/main/yang/experimental/openconfig/release/models/bfd",
    "src/main/yang/experimental/openconfig/release/models/bgp",
    "src/main/yang/experimental/openconfig/release/models/catalog",
    "src/main/yang/experimental/openconfig/release/models/interfaces",
    "src/main/yang/experimental/openconfig/release/models/isis",
    "src/main/yang/experimental/openconfig/release/models/lacp",
    "src/main/yang/experimental/openconfig/release/models/lldp",
    "src/main/yang/experimental/openconfig/release/models/local-routing",
    "src/main/yang/experimental/openconfig/release/models/macsec",
    "src/main/yang/experimental/openconfig/release/models/mpls",
    "src/main/yang/experimental/openconfig/release/models/multicast",
    "src/main/yang/experimental/openconfig/release/models/network-instance",
    "src/main/yang/experimental/openconfig/release/models/openflow",
    "src/main/yang/experimental/openconfig/release/models/optical-transport",
    "src/main/yang/experimental/openconfig/release/models/ospf",
    "src/main/yang/experimental/openconfig/release/models/platform",
    "src/main/yang/experimental/openconfig/release/models/policy",
    "src/main/yang/experimental/openconfig/release/models/policy-forwarding",
    "src/main/yang/experimental/openconfig/release/models/probes",
    "src/main/yang/experimental/openconfig/release/models/qos",
    "src/main/yang/experimental/openconfig/release/models/relay-agent",
    "src/main/yang/experimental/openconfig/release/models/rib",
    "src/main/yang/experimental/openconfig/release/models/segment-routing",
    "src/main/yang/experimental/openconfig/release/models/stp",
    "src/main/yang/experimental/openconfig/release/models/system",
    "src/main/yang/experimental/openconfig/release/models/telemetry",
    "src/main/yang/experimental/openconfig/release/models/types",
    "src/main/yang/experimental/openconfig/release/models/vlan",
    "src/main/yang/experimental/openconfig/release/models/wifi",
    "src/main/yang/experimental/parameters"
}

log = logging.getLogger(__name__)


def shell(command: str | list | tuple, joiner="; ", cwd: str | None = None):
    exec_command = command
    if isinstance(command, (list, tuple)):
        exec_command = joiner.join(command)

    # print(exec_command, " |--| ", cwd)

    try:
        log.info(exec_command)
        result = subprocess.run(
            exec_command,
            shell=True,
            check=True,
            capture_output=True,
            text=True,
            cwd=cwd,
        )
        log.info(f"{result.returncode:3d} |--| {result.stdout}")
        return result.returncode, result.stdout
    except subprocess.CalledProcessError as e:
        log.error(
            f"ERROR while command execution '{exec_command}':\n{e.stderr.strip()}"
        )
        return e.returncode, e.stdout
    except FileNotFoundError:
        log.error(f"ERROR command not found: {exec_command}")
        return None


def download_ftp_directory(host, path, local_path):
    with ftplib.FTP(host) as ftp:
        ftp.login()
        ftp.cwd(path)
        filenames = ftp.nlst()

        os.makedirs(local_path, exist_ok=True)

        for filename in filenames:
            local_filepath = os.path.join(local_path, filename)
            try:
                with open(local_filepath, 'wb') as f:
                    ftp.retrbinary(f'RETR {filename}', f.write)
            except ftplib.error_perm as e:
                print(f'Download failed for {filename}: {e}')


def cleanup_old_yang_files(directory):
    files_by_name = defaultdict(list)
    filenames = os.listdir(directory)

    for filename in filenames:
        if '@' in filename and filename.endswith('.yang'):
            try:
                name, date_ext = filename.split('@')
                date_str = date_ext.replace('.yang', '')

                files_by_name[name].append((date_str, filename))
            except ValueError:
                continue

    for name, files in files_by_name.items():
        if len(files) > 1:
            files.sort(key=lambda x: x[0])

            for date_str, filename in files[:-1]:
                file_path = os.path.join(directory, filename)
                os.remove(file_path)


def contains_path_hidden_dir_or_file(path: pathlib.PosixPath) -> bool:
    """Check if any directory on the provided path is hidden.

    Args:
        path (pathlib.PosixPath): A PosixPath to be checked.

    Returns:
        bool: boolean value if the path contains hidden directory
    """
    return any(part for part in path.parts if part.startswith("."))


def get_yang_files(root: str = ".") -> list[str]:
    """Lists all yang files found in root directory recursively.

    Args:
        root (str): Root directory containing yang files

    Returns:
        list[str]: List of all yang files full paths
    """
    root_path = pathlib.Path(root)
    dirs = list(
        [
            str(path.resolve())
            for path in root_path.rglob("*.yang")
            if path.is_file() and not contains_path_hidden_dir_or_file(path) and "experimental/parameters" not in str(path.resolve())
        ]
    )

    return dirs


def validator_path_option() -> str:
    """Returns --path argument with all provided yang files used for yang-model-validator tool.

    Args:
        yang_paths (set[str]): Set of all yang files which should be included in path arg

    Returns:
        str: Path arugment containing all provided yang files
    """
    yang_path_option = "--path " + " ".join(YANG_MODEL_PATHS)

    return yang_path_option


def preconditions():
    # Kill previous ODL
    shell(
        (
            "ps axf",
            "grep org.apache.karaf",
            "grep -v grep",
            "awk '{print \"kill -9 \" $1}'",
            "sh",
        ),
        " | ",
    )

    # # Cleanup folders
    shell(("rm -rf target src", "mkdir -p ./src/main/yang/experimental"))

    # # Clone repos
    shell(
        (
            "rm -rf openconfig",
            f"git clone {OPENCONFIG_REPO}",
            "mv -v public openconfig",
        ),
        cwd="./src/main/yang/experimental",
    )
    shell(
        f"git checkout -b ytest {OPENCONFIG_REPO_COMMIT_HASH}",
        cwd="./src/main/yang/experimental/openconfig",
    )

    local_directory = './src/main/yang/experimental/parameters'
    download_ftp_directory('ftp.iana.org', '/assignments/yang-parameters/', local_directory)
    cleanup_old_yang_files(local_directory)

    # # Removing yangmodels which fail ODL
    shell(
        (
            # Please keep below list in alpha order
            "rm -vrf .git",
            # tries to deviate itself
            "rm -vrf experimental/openconfig/release/models/wifi/openconfig-ap-interfaces.yang",
            # remove failing files
            # "rm -vrf experimental/openconfig/release/models/acl/openconfig-acl.yang",
            # "rm -vrf experimental/openconfig/release/models/aft/openconfig-aft-common.yang",
            # "rm -vrf experimental/openconfig/release/models/aft/openconfig-aft-ethernet.yang",
            # "rm -vrf experimental/openconfig/release/models/aft/openconfig-aft-ipv4.yang",
            # "rm -vrf experimental/openconfig/release/models/aft/openconfig-aft-ipv6.yang",
            # "rm -vrf experimental/openconfig/release/models/aft/openconfig-aft-mpls.yang",
            # "rm -vrf experimental/openconfig/release/models/aft/openconfig-aft-network-instance.yang",
            # "rm -vrf experimental/openconfig/release/models/aft/openconfig-aft-pf.yang",
            # "rm -vrf experimental/openconfig/release/models/aft/openconfig-aft.yang",
            # "rm -vrf experimental/openconfig/release/models/bfd/openconfig-bfd.yang",
            # "rm -vrf experimental/openconfig/release/models/bgp/openconfig-bgp-common-multiprotocol.yang",
            # "rm -vrf experimental/openconfig/release/models/bgp/openconfig-bgp-common-structure.yang",
            # "rm -vrf experimental/openconfig/release/models/bgp/openconfig-bgp-common.yang",
            # "rm -vrf experimental/openconfig/release/models/bgp/openconfig-bgp-global.yang",
            # "rm -vrf experimental/openconfig/release/models/bgp/openconfig-bgp-neighbor.yang",
            # "rm -vrf experimental/openconfig/release/models/bgp/openconfig-bgp-peer-group.yang",
            # "rm -vrf experimental/openconfig/release/models/bgp/openconfig-bgp-policy.yang",
            # "rm -vrf experimental/openconfig/release/models/bgp/openconfig-bgp.yang",
            # "rm -vrf experimental/openconfig/release/models/interfaces/openconfig-if-8021x.yang",
            # "rm -vrf experimental/openconfig/release/models/interfaces/openconfig-if-aggregate.yang",
            # "rm -vrf experimental/openconfig/release/models/interfaces/openconfig-if-ethernet-ext.yang",
            # "rm -vrf experimental/openconfig/release/models/interfaces/openconfig-if-ethernet.yang",
            # "rm -vrf experimental/openconfig/release/models/interfaces/openconfig-if-ip-ext.yang",
            # "rm -vrf experimental/openconfig/release/models/interfaces/openconfig-if-ip.yang",
            # "rm -vrf experimental/openconfig/release/models/interfaces/openconfig-if-poe.yang",
            # "rm -vrf experimental/openconfig/release/models/interfaces/openconfig-if-tunnel.yang",
            # "rm -vrf experimental/openconfig/release/models/interfaces/openconfig-interfaces.yang",
            # "rm -vrf experimental/openconfig/release/models/isis/openconfig-isis-lsp.yang",
            # "rm -vrf experimental/openconfig/release/models/isis/openconfig-isis-policy.yang",
            # "rm -vrf experimental/openconfig/release/models/isis/openconfig-isis-routing.yang",
            # "rm -vrf experimental/openconfig/release/models/isis/openconfig-isis.yang",
            # "rm -vrf experimental/openconfig/release/models/lacp/openconfig-lacp.yang",
            # "rm -vrf experimental/openconfig/release/models/lldp/openconfig-lldp.yang",
            # "rm -vrf experimental/openconfig/release/models/local-routing/openconfig-local-routing.yang",
            # "rm -vrf experimental/openconfig/release/models/macsec/openconfig-macsec.yang",
            # "rm -vrf experimental/openconfig/release/models/mpls/openconfig-mpls-igp.yang",
            # "rm -vrf experimental/openconfig/release/models/mpls/openconfig-mpls-ldp.yang",
            # "rm -vrf experimental/openconfig/release/models/mpls/openconfig-mpls-rsvp.yang",
            # "rm -vrf experimental/openconfig/release/models/mpls/openconfig-mpls-static.yang",
            # "rm -vrf experimental/openconfig/release/models/mpls/openconfig-mpls-te.yang",
            # "rm -vrf experimental/openconfig/release/models/mpls/openconfig-mpls.yang",
            # "rm -vrf experimental/openconfig/release/models/multicast/openconfig-igmp.yang",
            # "rm -vrf experimental/openconfig/release/models/multicast/openconfig-pim.yang",
            # "rm -vrf experimental/openconfig/release/models/network-instance/openconfig-network-instance-l2.yang",
            # "rm -vrf experimental/openconfig/release/models/network-instance/openconfig-network-instance-policy.yang",
            # "rm -vrf experimental/openconfig/release/models/network-instance/openconfig-network-instance.yang",
            # "rm -vrf experimental/openconfig/release/models/openflow/openconfig-openflow.yang",
            # "rm -vrf experimental/openconfig/release/models/optical-transport/openconfig-channel-monitor.yang",
            # "rm -vrf experimental/openconfig/release/models/optical-transport/openconfig-optical-amplifier.yang",
            # "rm -vrf experimental/openconfig/release/models/optical-transport/openconfig-terminal-device.yang",
            # "rm -vrf experimental/openconfig/release/models/optical-transport/openconfig-transport-line-common.yang",
            # "rm -vrf experimental/openconfig/release/models/ospf/openconfig-ospf-policy.yang",
            # "rm -vrf experimental/openconfig/release/models/ospf/openconfig-ospf-types.yang",
            # "rm -vrf experimental/openconfig/release/models/ospf/openconfig-ospfv2-area-interface.yang",
            # "rm -vrf experimental/openconfig/release/models/ospf/openconfig-ospfv2-area.yang",
            # "rm -vrf experimental/openconfig/release/models/ospf/openconfig-ospfv2-common.yang",
            # "rm -vrf experimental/openconfig/release/models/ospf/openconfig-ospfv2-global.yang",
            # "rm -vrf experimental/openconfig/release/models/ospf/openconfig-ospfv2-lsdb.yang",
            # "rm -vrf experimental/openconfig/release/models/ospf/openconfig-ospfv2.yang",
            # "rm -vrf experimental/openconfig/release/models/platform/openconfig-platform-port.yang",
            # "rm -vrf experimental/openconfig/release/models/platform/openconfig-platform-transceiver.yang",
            # "rm -vrf experimental/openconfig/release/models/policy-forwarding/openconfig-pf-forwarding-policies.yang",
            # "rm -vrf experimental/openconfig/release/models/policy-forwarding/openconfig-pf-interfaces.yang",
            # "rm -vrf experimental/openconfig/release/models/policy-forwarding/openconfig-pf-path-groups.yang",
            # "rm -vrf experimental/openconfig/release/models/policy-forwarding/openconfig-pf-srte.yang",
            # "rm -vrf experimental/openconfig/release/models/policy-forwarding/openconfig-policy-forwarding.yang",
            # "rm -vrf experimental/openconfig/release/models/policy/openconfig-policy-types.yang",
            # "rm -vrf experimental/openconfig/release/models/policy/openconfig-routing-policy.yang",
            # "rm -vrf experimental/openconfig/release/models/probes/openconfig-probes.yang",
            # "rm -vrf experimental/openconfig/release/models/qos/openconfig-qos-elements.yang",
            # "rm -vrf experimental/openconfig/release/models/qos/openconfig-qos-interfaces.yang",
            # "rm -vrf experimental/openconfig/release/models/qos/openconfig-qos.yang",
            # "rm -vrf experimental/openconfig/release/models/relay-agent/openconfig-relay-agent.yang",
            # "rm -vrf experimental/openconfig/release/models/rib/openconfig-rib-bgp-attributes.yang",
            # "rm -vrf experimental/openconfig/release/models/rib/openconfig-rib-bgp-ext.yang",
            # "rm -vrf experimental/openconfig/release/models/rib/openconfig-rib-bgp-shared-attributes.yang",
            # "rm -vrf experimental/openconfig/release/models/rib/openconfig-rib-bgp-table-attributes.yang",
            # "rm -vrf experimental/openconfig/release/models/rib/openconfig-rib-bgp-tables.yang",
            # "rm -vrf experimental/openconfig/release/models/rib/openconfig-rib-bgp.yang",
            # "rm -vrf experimental/openconfig/release/models/sampling/openconfig-sampling-sflow.yang",
            # "rm -vrf experimental/openconfig/release/models/segment-routing/openconfig-rsvp-sr-ext.yang",
            # "rm -vrf experimental/openconfig/release/models/segment-routing/openconfig-segment-routing.yang",
            # "rm -vrf experimental/openconfig/release/models/segment-routing/openconfig-srte-policy.yang",
            # "rm -vrf experimental/openconfig/release/models/stp/openconfig-spanning-tree.yang",
            # "rm -vrf experimental/openconfig/release/models/vlan/openconfig-vlan.yang",
            # "rm -vrf experimental/openconfig/third_party/ietf/iana-if-type.yang",
            # "rm -vrf experimental/openconfig/third_party/ietf/ietf-interfaces.yang",
        ),
        cwd="src/main/yang",
    )

    return {
        "files": get_yang_files("src/main/yang")
    }


# def pytest_generate_tests(metafunc):
#     if 'yang_file' in metafunc.fixturenames:
#         test_data = preconditions()
#         metafunc.parametrize("yang_file", test_data['files'])


class TestYangModelValidator:

    def test_validating_yang_models(self):
        yang_files = " ".join(preconditions()['files'])
        # log.info(f"working on: {yang_file}")
        rc, test_tool_output = shell(
            f"java -jar yang-model-validator.jar {validator_path_option()} -- {yang_files}"
        )
        assert (
            rc == 0
        ), f"{test_tool_output}"
