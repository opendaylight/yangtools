#
# Copyright (c) 2025 PANTHEON.tech, s.r.o. and others.  All rights reserved.
#
# This program and the accompanying materials are made available under the
# terms of the Eclipse Public License v1.0 which accompanies this distribution,
# and is available at http://www.eclipse.org/legal/epl-v10.html
#

import pytest
from utils.shell import shell

YANGMODELS_REPO = "https://github.com/YangModels/yang"

OPENCONFIG_REPO = "https://github.com/openconfig/public"
OPENCONFIG_REPO_COMMIT_TAG = "v5.3.0"


@pytest.fixture(scope='session')
def fixture_models_repos():
    shell(("rm -rf target src", "mkdir -p ./src/main/yang"))

    shell((f"git clone {YANGMODELS_REPO}"), cwd="./src/main/")
    shell(("git submodule update --init --recursive"), cwd="./src/main/yang")

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
