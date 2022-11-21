# Copyright (c) 2019 PANTHEON.tech s.r.o. All rights reserved.
#
# This program and the accompanying materials are made available under the
# terms of the Eclipse Public License v1.0 which accompanies this distribution,
# and is available at http://www.eclipse.org/legal/epl-v10.html

# variables

YANGMODELS_REPO = "https://github.com/YangModels/yang"
TEST_TOOL_NAME = "yang-model-validator"
NEXUS_RELEASE_BASE_URL = "https://nexus.opendaylight.org/content/repositories/opendaylight.release"

delete_static_paths_list = [
    "/src/main/yang/.git",
    "/src/main/yang/experimental",
    # excluding ieee/draft from test
    "/src/main/yang/standard/ieee/draft",
    "/src/main/yang/standard/ietf/DRAFT",
    # excluding 4 files due error "Unexpected error processing source SourceIdentifier [ietf-network@2018-02-26]"
    # bug reported here https://jira.opendaylight.org/browse/YANGTOOLS-1465
    "/src/main/yang/standard/ietf/RFC/ietf-te-topology-state.yang",
    "/src/main/yang/standard/ietf/RFC/ietf-te-topology-state@2020-08-06.yang",
    "/src/main/yang/standard/ietf/RFC/ietf-te-topology.yang",
    "/src/main/yang/standard/ietf/RFC/ietf-te-topology@2020-08-06.yang",
    ## Removing entire juniper folder because it creates an OOM Crash with the validator tool.*** Keywords ***
    ## Unsure if the yang models are the problem or something in the tool. This is being tracked here:
    ## https://jira.opendaylight.org/browse/YANGTOOLS-1093
    "/src/main/yang/vendor/juniper",
    ## Removing the cisco folder because there are over 30k yang files there and would increase the test time to something
    ## unmanageable.
    "/src/main/yang/vendor/cisco",
    ## Mount points may only be defined at either a container or a list, not anydata, lines 948
    ## https://tools.ietf.org/html/rfc8528#section-3.1
    "/src/main/yang/standard/ietf/RFC/ietf-connectionless-oam@2019-04-16.yang",
    ## removed dependency file from test standard/ietf/RFC/ietf-connectionless-oam@2019-04-16.yang
    "/src/main/yang/standard/ietf/RFC/ietf-connectionless-oam.yang",
    ## removed dependecy file from test standard/ietf/RFC/ietf-connectionless-oam-methods@2019-04-16.yang
    "/src/main/yang/standard/ietf/RFC/ietf-connectionless-oam-methods.yang",
    "/src/main/yang/standard/ietf/RFC/ietf-connectionless-oam-methods@2019-04-16.yang",
]

yang_model_paths = [
    "/src/main/yang/",
    "/src/main/yang/standard",
    "/src/main/yang/standard/etsi",
    "/src/main/yang/standard/etsi/NFV-SOL006-v3.6.1",
    "/src/main/yang/standard/etsi/NFV-SOL006-v2.6.1",
    "/src/main/yang/standard/etsi/NFV-SOL006-v2.7.1",
    "/src/main/yang/standard/etsi/NFV-SOL006-v2.8.1",
    "/src/main/yang/standard/etsi/NFV-SOL006-v3.3.1",
    "/src/main/yang/standard/etsi/NFV-SOL006-v3.5.1",
    "/src/main/yang/standard/odp",
    "/src/main/yang/standard/bbf",
    "/src/main/yang/standard/ietf",
    "/src/main/yang/standard/ietf/RFC",
    "/src/main/yang/standard/mef",
    "/src/main/yang/standard/ieee",
    "/src/main/yang/standard/ieee/published",
    "/src/main/yang/standard/ieee/published/802.1",
    "/src/main/yang/standard/ieee/published/802.11",
    "/src/main/yang/standard/ieee/published/802",
    "/src/main/yang/standard/ieee/published/1588",
    "/src/main/yang/standard/ieee/published/802.3",
    "/src/main/yang/standard/ieee/published/1906.1",
    "/src/main/yang/standard/ieee/published/1906.1/Examples",
    "/src/main/yang/standard/iana",
    "/src/main/yang/vendor",
    "/src/main/yang/vendor/fujitsu",
    "/src/main/yang/vendor/huawei",
    "/src/main/yang/vendor/ciena",
    "/src/main/yang/vendor/nokia"
]