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
# Paths within YANGMODELS_REPO that the tests below actually reference.
# Everything else in that repo (e.g. "vendor", several GB on its own) is
# skipped by the sparse-checkout below - add a path here if a test needs
# something not already covered by one of these prefixes.
MODEL_PATHS = ("standard",)


OPENCONFIG_REPO = "https://github.com/openconfig/public"
OPENCONFIG_REPO_COMMIT_TAG = "v5.3.0"


@pytest.fixture(scope='session')
def fixture_models_repos():
    shell(("rm -rf target src", "mkdir -p ./src/main/yang"))

    # Only "standard" is used by any test, and it's a small fraction of
    # the full repo (the "vendor" directory alone is several GB). Fetch
    # it with a partial+shallow clone and sparse-checkout it down to just
    # that directory instead of pulling the whole repo.
    shell(
        f"git clone --no-checkout --depth 1 --filter=blob:none {YANGMODELS_REPO}",
        cwd="./src/main/",
    )
    shell(
        (
            "git sparse-checkout init --cone",
            f"git sparse-checkout set {' '.join(MODEL_PATHS)}",
            "git checkout",
            # sparse-checkout excludes "experimental", but the openconfig
            # clone below needs it as a landing directory.
            "mkdir -p experimental",
        ),
        cwd="./src/main/yang",
    )
    shell(
        tuple(f"git submodule update --init --recursive {path}" for path in MODEL_PATHS),
        cwd="./src/main/yang",
    )

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
