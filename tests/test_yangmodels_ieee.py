#
# Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
#
# This program and the accompanying materials are made available under the
# terms of the Eclipse Public License v1.0 which accompanies this distribution,
# and is available at http://www.eclipse.org/legal/epl-v10.html
#

import pytest
from utils.shell import shell
from utils.validator import get_yang_files, validator_path_option

# YANG_MODEL_PATHS is needed to explicitly tell which paths to find yang files to build dependencies from to validate
# the yang file being validated. There is an option (-r) to recursively parse so that you don't have to pass all of these
# paths with the -p argument, but the recursive option makes the tool so slow that it would not work when testing so
# many files

YANG_MODEL_PATHS = {
    "802": {},
    "802.1": {
        "src/main/yang/standard/ietf/RFC",
        "src/main/yang/standard/ieee/published/1588",
        "src/main/yang/standard/ieee/published/802",
    },
    "802.3": {
        "src/main/yang/standard/ietf/RFC",
        "src/main/yang/standard/ieee/published/802",
        "src/main/yang/standard/ieee/published/802.1",
    },
    "1588": {
        "src/main/yang/standard/ieee/published/1906.1",
    },
    "1906.1": {}
}


@pytest.mark.parametrize("number", [
    "802",
    "802.1",
    "802.3",
    "1588",
    "1906.1",
])
def test_validate_yangmodels_ieee_yangs(number, fixture_models_repos):  # noqa: F811
    yang_files = get_yang_files(f"./src/main/yang/standard/ieee/published/{number}")
    yang_files_str = " ".join([f"'{f}'" for f in yang_files])

    if number in YANG_MODEL_PATHS and YANG_MODEL_PATHS[number]:
        rc, test_tool_output = shell(
            f"java -jar yang-model-validator.jar {validator_path_option(YANG_MODEL_PATHS[number])} -- {yang_files_str}"
        )
    else:
        rc, test_tool_output = shell(
            f"java -jar yang-model-validator.jar -- {yang_files_str}"
        )

    assert (
        rc == 0
    ), f"{test_tool_output}"
