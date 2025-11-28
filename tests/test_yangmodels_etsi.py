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
    "src/main/yang/standard/ietf/RFC",
}


@pytest.mark.parametrize("sol_version", [
    "NFV-SOL006-v4.3.1",
    "NFV-SOL006-v3.6.1",
    "NFV-SOL006-v3.5.1",
    "NFV-SOL006-v3.3.1",
    "NFV-SOL006-v2.8.1",
    "NFV-SOL006-v2.7.1",
    "NFV-SOL006-v2.6.1"
])
def test_validate_yangmodels_etsi_yangs(fixture_models_repos, sol_version):  # noqa: F811
    yang_files = get_yang_files(f"./src/main/yang/standard/etsi/{sol_version}/src/yang")
    yang_files_str = " ".join([f"'{f}'" for f in yang_files])

    rc, test_tool_output = shell(
        f"java -jar yang-model-validator.jar {validator_path_option(YANG_MODEL_PATHS)} -- {yang_files_str}"
    )
    assert (
        rc == 0
    ), f"{test_tool_output}"
