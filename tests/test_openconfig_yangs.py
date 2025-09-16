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

OPENCONFIG_REPO = "https://github.com/openconfig/public"
OPENCONFIG_REPO_COMMIT_TAG = "v5.3.0"

TEST_TOOL_NAME = "yang-model-validator"

# YANG_MODEL_PATHS is needed to explicitly tell which paths to find yang files to build dependencies from to validate
# the yang file being validated. There is an option (-r) to recursively parse so that you don't have to pass all of these
# paths with the -p argument, but the recursive option makes the tool so slow that it would not work when testing so
# many files
YANG_MODEL_PATHS = {
    "src/main/yang/experimental/openconfig/third_party/ietf",
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
            if path.is_file() and not contains_path_hidden_dir_or_file(path)
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
        f"git checkout -b ytest {OPENCONFIG_REPO_COMMIT_TAG}",
        cwd="./src/main/yang/experimental/openconfig",
    )


class TestYangModelValidator:

    def test_validating_yang_models(self):
        preconditions()

        yang_files = get_yang_files("./src/main/yang/experimental/openconfig/release/models/")
        yang_files_str = " ".join([f"'{f}'" for f in yang_files])

        rc, test_tool_output = shell(
            f"java -jar yang-model-validator.jar {validator_path_option()} -- {yang_files_str}"
        )
        assert (
            rc == 0
        ), f"{test_tool_output}"
